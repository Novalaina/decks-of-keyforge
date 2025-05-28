package coraythan.keyswap.cards.dokcards

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import coraythan.keyswap.House
import coraythan.keyswap.cards.*
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.cards.extrainfo.ExtraCardInfoRepo
import coraythan.keyswap.cards.extrainfo.NextAndPreviousCardInfos
import coraythan.keyswap.cards.extrainfo.QExtraCardInfo
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.decks.models.GenericDeck
import coraythan.keyswap.decks.models.HouseAndCards
import coraythan.keyswap.decks.models.withBonusIcons
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.sasupdate.SasVersionService
import coraythan.keyswap.users.KeyUser
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class  DokCardCacheService(
    private val sasVersionService: SasVersionService,
    private val objectMapper: ObjectMapper,
    private val entityManager: EntityManager,
    private val extraCardInfoRepo: ExtraCardInfoRepo,
    private val tokenService: TokenService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val query = JPAQueryFactory(entityManager)

    private var previousCardsCachedByUrlName: Map<String, ExtraCardInfo> = mapOf()

    private var cardsCachedByNumberSet: Map<CardNumberSetPair, ExtraCardInfo> = mapOf()
    private var cardsCachedByUrlName: Map<String, ExtraCardInfo> = mapOf()

    private var futureCardsCachedByNumberSet: Map<CardNumberSetPair, ExtraCardInfo> = mapOf()
    private var futureCardsCachedByUrlName: Map<String, ExtraCardInfo> = mapOf()

    private var tokenCards: Map<String, ExtraCardInfo> = mapOf()
    private var futureTokenCards: Map<String, ExtraCardInfo> = mapOf()

    private var frontendCardsMapPrevious: CardsMap = CardsMap(mapOf())
    private var frontendCardsMapFuture: CardsMap = CardsMap(mapOf())

    private var loaded = false

    @Transactional
    fun loadCards() {
        log.info("Begin loading cached cards.")
        val publishedAercVersion = sasVersionService.findSasVersion()

        val extraQ = QExtraCardInfo.extraCardInfo
        val currentInfos = findInfos(extraQ.active.isTrue)
        val nextInfos = findInfos(extraQ.published.isNull)
        val previousInfos = findInfos(extraQ.active.isFalse.and(extraQ.version.lt(publishedAercVersion)))

        log.info(
            "Loading cards there are ${
                Expansion.entries.map { expan ->
                    "${currentInfos.count { it.dokCard.expansions.any { it.expansion == expan } }} cards for $expan"
                }
            }"
        )

        cardsCachedByUrlName = currentInfos
            .associateBy { extraInfo -> extraInfo.dokCard.cardTitleUrl }

        cardsCachedByNumberSet = currentInfos.flatMap { extraInfo ->
            extraInfo.dokCard.expansions
                .map { CardNumberSetPair(it.expansion, it.cardNumber) to extraInfo }
        }.toMap()

        tokenCards = currentInfos
            .filter { it.dokCard.token }
            .associateBy { extraInfo -> extraInfo.dokCard.cardTitleUrl }

        previousCardsCachedByUrlName = previousInfos
            .associateBy { extraInfo -> extraInfo.dokCard.cardTitleUrl }

        futureCardsCachedByUrlName = nextInfos
            .associateBy { extraInfo -> extraInfo.dokCard.cardTitleUrl }

        futureCardsCachedByNumberSet = nextInfos.flatMap { extraInfo ->
            extraInfo.dokCard.expansions
                .map { CardNumberSetPair(it.expansion, it.cardNumber) to extraInfo }
        }.toMap()

        futureTokenCards = nextInfos
            .filter { it.dokCard.token }
            .associateBy { extraInfo -> extraInfo.dokCard.cardTitleUrl }

        frontendCardsMapPrevious = CardsMap(previousCardsCachedByUrlName.map { it.key to it.value.toCardForFrontend() }.toMap())
        frontendCardsMapFuture = CardsMap(futureCardsCachedByUrlName.map { it.key to it.value.toCardForFrontend() }.toMap())

        loaded = true
        log.info("Loading cached cards complete.")
    }

    fun updateCache(expansion: Expansion, cardNumber: String, extraInfo: ExtraCardInfo) {
        loadLazyEntities(listOf(extraInfo))
        this.cardsCachedByUrlName = this.cardsCachedByUrlName.plus(Pair(extraInfo.cardNameUrl, extraInfo))
        this.cardsCachedByNumberSet =
            this.cardsCachedByNumberSet.plus(Pair(CardNumberSetPair(expansion, cardNumber), extraInfo))
    }

    fun currentCards() = if (!loaded) {
        throw IllegalStateException("Site still loading cards")
    } else {
        cardsCachedByUrlName.values.map { it.toCardForFrontend() }
    }

    fun futureCards(): CardsMap = if (!loaded) {
        throw IllegalStateException("Site still loading cards")
    } else {
        frontendCardsMapFuture
    }

    fun previousCards() = if (!loaded) {
        throw IllegalStateException("Site still loading cards")
    } else {
        frontendCardsMapPrevious
    }

    fun findByCardName(cardName: String) =
        if (!loaded) throw IllegalStateException("Site still loading cards") else cardsCachedByUrlName[cardName.toLegacyUrlFriendlyCardTitle()]
            ?: error("No card for ${cardName.toLegacyUrlFriendlyCardTitle()}")

    fun findByCardNameUrlOrNull(cardNameUrl: String) =
        if (!loaded) throw IllegalStateException("Site still loading cards") else cardsCachedByUrlName[cardNameUrl]

    fun findByCardNameUrl(cardNameUrl: String) =
        if (!loaded) throw IllegalStateException("Site still loading cards") else findByCardNameUrlOrNull(cardNameUrl)
            ?: error("No card for card name url $cardNameUrl")

    fun twinnedCardsForDeck(deck: Deck): List<DokCardInDeck> {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        return cardsForDeck(deck).map {
            val twin = if (it.card.cardTitle.contains(evilTwinCardName)) {
                val goodTwinName = it.card.cardTitle.dropLast(evilTwinCardName.length)
                findByCardName(goodTwinName)
            } else {
                findByCardName(it.card.cardTitle + evilTwinCardName)
            }
            it.copy(card = twin.dokCard, extraCardInfo = twin)
        }
    }

    fun tokenForDeck(deck: GenericDeck): DokCardInDeck? {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        val deckTokenNum = deck.tokenNumber ?: return null
        val token = tokenCards[tokenService.tokenIdToCardTitle(deckTokenNum).toLegacyUrlFriendlyCardTitle()] ?: return null
        return DokCardInDeck(
            card = token.dokCard,
            extraCardInfo = token,
            house = token.dokCard.houses.first(),
            rarity = Rarity.Special,
        )
    }

    fun cardsForDeck(deck: GenericDeck, useFuture: Boolean = false): List<DokCardInDeck> {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        require(deck.cardIds.isNotBlank()) { "Card id string was blank! deck name: ${deck.name}" }
        val cardIds = objectMapper.readValue<CardIds>(deck.cardIds)

        val bonusIcons = deck.bonusIcons()
        val futureCards = futureCardsCachedByNumberSet
        val cards = cardsCachedByNumberSet

        return cardIds
            .cardIds
            .flatMap { entry ->
                entry.value.map { cardNum ->
                    val card = if (useFuture) {
                        futureCards[cardNum.toNew()] ?: cards[cardNum.toNew()]
                        ?: throw IllegalStateException("No card for $cardNum")
                    } else {
                        cards[cardNum.toNew()] ?: throw IllegalStateException("No card for $cardNum")
                    }

                    DokCardInDeck(
                        deck,
                        card,
                        entry.key,
                        Expansion.forExpansionNumber(cardNum.expansion),
                    )
                }
            }
            .withBonusIcons(bonusIcons)
    }

    fun cardsAndTokenFutureProof(deck: GenericDeck, user: KeyUser?): CardsAndToken {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        val cards = cardsForDeck(deck, user?.displayFutureSas() == true)
        val token = if (user?.displayFutureSas() == true) {
            futureTokenForDeck(deck)
        } else {
            tokenForDeck(deck)
        }
        return CardsAndToken(cards, token)
    }

    fun deckToHouseAndCards(deck: GenericDeck): List<HouseAndCards> {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        val houses = deck.houses.let {
            if (Expansion.forExpansionNumber(deck.expansion) == Expansion.PROPHETIC_VISIONS) {
                // Add in Prophecies
                it.plus(House.Prophecy)
            } else if (Expansion.forExpansionNumber(deck.expansion) == Expansion.CRUCIBLE_CLASH) {
                // Add in Archon Power
                it.plus(House.ArchonPower)
            } else {
                it
            }
        }

        val cards = cardsForDeck(deck)
        return houses
            .map { house ->
                HouseAndCards(
                    house,
                    cards
                        .filter { it.house == house }
                        .sorted()
                        .map {
                            it.toSimpleCard(house)
                        }
                )
            }
            .sortedBy { it.house }
    }

    fun findNextAndPreviousCards(infoId: UUID, expansion: Expansion): NextAndPreviousCardInfos {
        val extraInfo = extraCardInfoRepo.findByIdOrNull(infoId) ?: error("No info for $infoId")
        val dokCardExpansion = extraInfo.dokCard.expansions.find { it.expansion == expansion }
        if (dokCardExpansion == null) {
            return NextAndPreviousCardInfos()
        }
        val cardNumber = dokCardExpansion.cardNumber
        val allCardNumbersForExpansion = cardsCachedByNumberSet.keys
            .filter { it.expansion == expansion }
            .map { it.cardNumber }
            .sorted()
        val indexOfCardNum = allCardNumbersForExpansion.indexOf(cardNumber)
        if (indexOfCardNum == -1) {
            return NextAndPreviousCardInfos()
        }
        val previousIndex = indexOfCardNum - 1
        val nextIndex = indexOfCardNum + 1
        val previousCardNum = allCardNumbersForExpansion.getOrNull(previousIndex)
        val nextCardNum = allCardNumbersForExpansion.getOrNull(nextIndex)
        return NextAndPreviousCardInfos(
            nextInfo = findNextInfoIdByCardNumExpansion(nextCardNum, expansion),
            previousInfo = findNextInfoIdByCardNumExpansion(previousCardNum, expansion)
        )
    }

    private fun findInfos(predicate: BooleanExpression): List<ExtraCardInfo> {
        val extraQ = QExtraCardInfo.extraCardInfo
        val infos = query.selectFrom(extraQ)
            .innerJoin(extraQ.dokCard).fetchJoin()
            .leftJoin(extraQ.synergies).fetchJoin()
            .where(predicate)
            .fetch()

        infos.forEach { it.traits.size }
        return infos
    }

    private fun futureTokenForDeck(deck: GenericDeck): DokCardInDeck? {
        if (!loaded) throw IllegalStateException("Site still loading cards")
        val deckTokenNum = deck.tokenNumber ?: return null
        val token = futureTokenCards[tokenService.tokenIdToCardTitle(deckTokenNum).toLegacyUrlFriendlyCardTitle()] ?: return tokenForDeck(deck)
        return DokCardInDeck(
            card = token.dokCard,
            extraCardInfo = token,
            house = token.dokCard.houses.first(),
            rarity = Rarity.Special,
        )
    }

    private fun loadLazyEntities(loadIn: List<ExtraCardInfo>) {
        loadIn.forEach {
            it.dokCard.expansions.size
            it.traits.size
            it.synergies.size
        }
    }

    private fun findNextInfoIdByCardNumExpansion(cardNumber: String?, expansion: Expansion): UUID? {
        if (cardNumber == null) return null
        val numSetPair = CardNumberSetPair(expansion, cardNumber)
        return (futureCardsCachedByNumberSet[numSetPair] ?: cardsCachedByNumberSet[numSetPair])?.id
    }
}
