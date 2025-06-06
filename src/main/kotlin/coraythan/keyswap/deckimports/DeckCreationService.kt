package coraythan.keyswap.deckimports

import com.fasterxml.jackson.databind.ObjectMapper
import coraythan.keyswap.House
import coraythan.keyswap.cards.*
import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.cards.dokcards.toLegacyUrlFriendlyCardTitle
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.DeckSasValuesSearchableRepo
import coraythan.keyswap.decks.DeckSasValuesUpdatableRepo
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.decks.models.DeckSasValuesSearchable
import coraythan.keyswap.decks.models.DeckSasValuesUpdatable
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.sasupdate.SasVersionService
import coraythan.keyswap.synergy.DeckSynergyInfo
import coraythan.keyswap.synergy.synergysystem.DeckSynergyService
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeDeck
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeDeckDto
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Service
class DeckCreationService(
    private val cardService: CardService,
    private val deckRepo: DeckRepo,
    private val deckSasValuesSearchableRepo: DeckSasValuesSearchableRepo,
    private val deckSasValuesUpdatableRepo: DeckSasValuesUpdatableRepo,
    private val objectMapper: ObjectMapper,
    private val cardRepo: CardRepo,
    private val postProcessDecksService: PostProcessDecksService,
    private val dokCardCacheService: DokCardCacheService,
    private val sasVersionService: SasVersionService,
    private val tokenService: TokenService,
    private val importSkippedDeckRepo: ImportSkippedDeckRepo,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateDeck(mvDeck: KeyForgeDeckDto) {
        log.info("Update deck ${mvDeck.data.name}")
        saveKeyForgeDeck(mvDeck.data, deckRepo.findByKeyforgeId(mvDeck.data.id))
    }

    /**
     * Only set current page if this is auto importing new decks
     *
     * returns Pair(count, newCard)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveDecks(deck: List<KeyForgeDeck>): List<Long> {
        val savedIds = mutableListOf<Long>()
        deck
            .forEach { keyforgeDeck ->
                val savedId = if (deckRepo.findByKeyforgeId(keyforgeDeck.id) == null) saveKeyForgeDeck(
                    keyforgeDeck,
                ) else null
                if (savedId != null) {
                    savedIds.add(savedId)
                }
            }
        return savedIds
    }

    private fun saveKeyForgeDeck(
        keyforgeDeck: KeyForgeDeck,
        updateDeck: Deck? = null,
    ): Long? {

        val checkCards =
            (keyforgeDeck.cards ?: keyforgeDeck._links?.cards)
                ?: error("Cards in the deck ${keyforgeDeck.id} are null.")
        val cleanCards = checkCards
            .filter {
                // Skip stupid tide card
                it != "37377d67-2916-4d45-b193-bea6ecd853e3"
            }

        // We handle Tokens in a special way, they are in this card list
        val cardsListWithExtras =
            cleanCards.map { cardRepo.findByIdOrNull(it) ?: error("No card for card id $it") }

        val cardsList = cardsListWithExtras
            .filter { !it.token }
            .let {
                if (keyforgeDeck.expansion == Expansion.MENAGERIE_2024.expansionNumber) {
                    it.map { card ->
                        if (card.cardTitle.toLegacyUrlFriendlyCardTitle() == "its-coming") {
                            // Fix the stupid card number in its coming Menagerie
                            card.copy(cardNumber = "999")
                        } else {
                            card
                        }
                    }
                } else {
                    it
                }
            }

        val housesGrouped =
            cardsList
                .filter { it.house.realHouse }
                .groupBy { it.house }
                .values

        val allHousesHave12 =
            housesGrouped
                .all { it.size == 12 }

        if (allHousesHave12) {
            val token = cardsListWithExtras.firstOrNull { it.token }
            val tokenId = if (token == null) null else tokenService.cardTitleToTokenId(token.cardTitle)

            val expansion = Expansion.forExpansionNumber(keyforgeDeck.expansion)
            val houses = keyforgeDeck._links?.houses?.mapNotNull { House.fromMasterVaultValue(it) }
                ?: throw IllegalStateException("Deck didn't have houses.")

            checkHouseAndCardCounts(keyforgeDeck.id, expansion, houses, cardsList)

            val bonusIconSimpleCards = keyforgeDeck.createBonusIconsInfo(houses, cardsList)

            val deckToSave = keyforgeDeck.toDeck(updateDeck, tokenId).withBonusIcons(bonusIconSimpleCards)

            try {
                val savedDeck = if (updateDeck != null) {
                    // Do this so we make sure we fix any bad card ids
                    val validatedDeck = validateAndRateDeck(deckToSave, houses, cardsList, token?.cardTitle)
                    deckRepo.save(validatedDeck)
                } else {
                    saveDeck(
                        deckToSave,
                        houses,
                        cardsList,
                        token
                    )
                }
                return savedDeck.id
            } catch (e: DataIntegrityViolationException) {
                if (e.message?.contains("deck_keyforge_id_uk") == true) {
                    log.info("Ignoring unique key exception adding deck with id ${keyforgeDeck.id}.")
                } else {
                    throw e
                }
            }
        } else {

            if (!importSkippedDeckRepo.existsByDeckKeyforgeId(keyforgeDeck.id)) {
                log.warn("Saving deck: ${keyforgeDeck.id} for later. Not all houses have 12 cards. Cards: ${cardsList.groupBy { it.house }.map { it.value.map { card -> card.cardTitle } }}")
                importSkippedDeckRepo.save(ImportSkippedDeck(keyforgeDeck.id))
            } else {
                log.warn("Deck: ${keyforgeDeck.id} was already saved for later. Don't import it.")
            }
        }
        return null
    }

    fun viewTheoreticalDeck(deck: DeckBuildingData): Deck {
        val deckAndCards = makeBasicDeckFromDeckBuilderData(deck)
        return validateAndRateDeck(
            deckAndCards.first,
            deck.cards.keys.toList().filter { it.realHouse },
            deckAndCards.second,
            deck.tokenTitle,
            deck.alliance
        )

    }

    fun rateDeck(inputDeck: Deck): DeckSynergyInfo {
        val cards = dokCardCacheService.cardsForDeck(inputDeck)
        val token = dokCardCacheService.tokenForDeck(inputDeck)
        val deckSynergyInfo = DeckSynergyService.fromDeckWithCards(inputDeck, cards, token)
        return deckSynergyInfo
    }

    private fun saveDeck(deck: Deck, houses: List<House>, cardsList: List<Card>, token: Card?): Deck {
        val ratedDeck = validateAndRateDeck(deck, houses, cardsList, token?.cardTitle)
        val saved = deckRepo.save(ratedDeck)
        val deckSyns = rateDeck(saved)
        val sasVersion = sasVersionService.findSasVersion()
        val dokCards = dokCardCacheService.cardsForDeck(saved)
        deckSasValuesSearchableRepo.save(DeckSasValuesSearchable(saved, dokCards, deckSyns, sasVersion))
        deckSasValuesUpdatableRepo.save(DeckSasValuesUpdatable(saved, dokCards, deckSyns, sasVersion))
        postProcessDecksService.addPostProcessDeck(saved)
        return saved
    }

    private fun validateAndRateDeck(
        deck: Deck,
        houses: List<House>,
        cardsList: List<Card>,
        tokenName: String? = null,
        alliance: Boolean = false
    ): Deck {
        checkHouseAndCardCounts(deck.keyforgeId, deck.expansionEnum, houses, cardsList, alliance)

        val saveable = deck
            .copy(
                evilTwin = cardsList.any { it.isEvilTwin() },
                houseNamesString = houses.sorted().joinToString("|"),
                cardIds = objectMapper.writeValueAsString(CardIds.fromCards(cardsList)),
                tokenNumber = if (tokenName == null) {
                    null
                } else {
                    tokenService.cardTitleToTokenId(tokenName)
                }
            )

        check(saveable.cardIds.isNotBlank()) { "Can't save a deck without its card ids: $deck" }

        return saveable
    }

    private fun makeBasicDeckFromDeckBuilderData(deckBuilderData: DeckBuildingData): Pair<Deck, List<Card>> {
        val cards = deckBuilderData.cards.flatMap { entry ->
            entry.value.map {
                val card: Card =
                    cardService.findByExpansionCardName(
                        deckBuilderData.expansion.expansionNumber,
                        it.name,
                        it.enhanced
                    )
                        ?: cardService.findByCardName(it.name)

                card.copy(house = entry.key)
            }
        }
        return Deck(
            keyforgeId = UUID.randomUUID().toString(),
            name = deckBuilderData.name,
            expansion = deckBuilderData.expansion.expansionNumber,
            tokenNumber = if (deckBuilderData.tokenTitle == null) null else tokenService.cardTitleToTokenId(
                deckBuilderData.tokenTitle
            ),
        ) to cards
    }

    private fun checkHouseAndCardCounts(
        id: String,
        expansion: Expansion,
        houses: List<House>,
        cards: List<Card>,
        alliance: Boolean = false
    ) {
        if (expansion == Expansion.CRUCIBLE_CLASH && alliance) {
            if (cards.size != 36) {
                error("Deck $id must have ${expansion.expectedCardCount} cards.")
            }
        } else if (cards.size != expansion.expectedCardCount) {
            error("Deck $id must have ${expansion.expectedCardCount} cards.")
        }
        if (expansion.singleHouse && !alliance) {
            if (houses.toSet().size != 1) error("Deck $id must have 1 house.")
        } else  {
            if (houses.toSet().size != 3) error("Deck $id must have 3 houses.")
        }
    }

}
