package coraythan.keyswap.publicapis

import coraythan.keyswap.alliancedecks.AllianceDeckRepo
import coraythan.keyswap.alliancedecks.OwnedAllianceDeckRepo
import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.models.DeckSearchResult
import coraythan.keyswap.decks.models.GenericDeck
import coraythan.keyswap.stats.StatsService
import coraythan.keyswap.synergy.synergysystem.DeckSynergyService
import coraythan.keyswap.userdeck.FavoritedDeckRepo
import coraythan.keyswap.userdeck.FunnyDeckRepo
import coraythan.keyswap.userdeck.OwnedDeckPageableRepo
import coraythan.keyswap.userdeck.OwnedDeckRepo
import coraythan.keyswap.userdeck.UserDeckRepo
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.KeyUser
import coraythan.keyswap.users.KeyUserRepo
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PublicApiService(
    private val currentUserService: CurrentUserService,
    private val keyUserRepo: KeyUserRepo,
    private val deckRepo: DeckRepo,
    private val allianceDeckRepo: AllianceDeckRepo,
    private val statsService: StatsService,
    private val userDeckRepo: UserDeckRepo,
    private val ownedDeckRepo: OwnedDeckRepo,
    private val ownedDeckPageableRepo: OwnedDeckPageableRepo,
    private val ownedAllianceDeckRepo: OwnedAllianceDeckRepo,
    private val favoritedDeckRepo: FavoritedDeckRepo,
    private val funnyDeckRepo: FunnyDeckRepo,
    private val cardCache: DokCardCacheService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun findMyDeckIds(user: KeyUser): List<String> {
        return ownedDeckRepo.findAllByOwnerId(user.id)
            .map { it.deck.keyforgeId }
    }

    fun findMyDecks(user: KeyUser, page: Int? = null): List<PublicMyDeckInfo> {
        val userDecks = if (page == null) {
            // All decks
            ownedDeckRepo.findAllByOwnerId(user.id)
        } else {
            // Page of 100 decks
            ownedDeckPageableRepo.findAllByOwnerId(user.id, PageRequest.of(page, 100))
        }

        return userDecks
            .map {
                val cards = cardCache.cardsForDeck(it.deck)
                val token = cardCache.tokenForDeck(it.deck)
                val synergies = DeckSynergyService.fromDeckWithCards(it.deck, cards, token)

                PublicMyDeckInfo(
                    deck = it.deck.toDeckSearchResult(
                        housesAndCards = cardCache.deckToHouseAndCards(it.deck),
                        cards = cards,
                        token = token,
                        synergies = synergies,
                        stats = statsService.cachedStats
                    ),
                    wishlist = favoritedDeckRepo.existsByDeckIdAndUserId(it.deck.id, user.id),
                    funny = funnyDeckRepo.existsByDeckIdAndUserId(it.deck.id, user.id),
                    notes = userDeckRepo.findByUserIdAndDeckId(user.id, it.deck.id)?.notes,
                    ownedByMe = true
                )
            }
    }

    fun findMyAlliances(user: KeyUser): List<PublicMyDeckInfo> {

        return ownedAllianceDeckRepo.findAllByOwnerId(user.id)
            .map {
                val cards = cardCache.cardsForDeck(it.deck)
                val token = cardCache.tokenForDeck(it.deck)
                val synergies = DeckSynergyService.fromDeckWithCards(it.deck, cards, token)

                PublicMyDeckInfo(
                    deck = it.deck.toDeckSearchResult(
                        housesAndCards = cardCache.deckToHouseAndCards(it.deck),
                        cards = cards,
                        token = token,
                        synergies = synergies,
                        stats = statsService.cachedStats
                    ),
                    ownedByMe = true
                )
            }
    }

    fun findDeckSimple(keyforgeId: String): DeckSearchResult? {
        if (keyforgeId.length != 36) {
            log.info("Request for deck with malformed id: $keyforgeId")
            return null
        }
        val deck = deckRepo.findByKeyforgeId(keyforgeId)
        if (deck == null) {
            log.debug("Request for deck that doesn't exist $keyforgeId")
            return null
        }
        return findDeckSimple(deck)
    }

    fun findAllianceDeckSimple(keyforgeId: UUID): DeckSearchResult? {
        val deck = allianceDeckRepo.findByIdOrNull(keyforgeId)
        if (deck == null) {
            log.debug("Request for an alliance deck that doesn't exist {}", keyforgeId)
            return null
        }
        return findDeckSimple(deck)
    }

    fun generateApiKey(): String {
        val currentUser = currentUserService.loggedInUserOrUnauthorized()
        val apiKey = UUID.randomUUID().toString()
        val updatedUser = currentUser.copy(apiKey = apiKey)
        keyUserRepo.save(updatedUser)
        return apiKey
    }

    fun userForApiKey(apiKey: String): KeyUser {
        return keyUserRepo.findByApiKey(apiKey)
            ?: throw BadRequestException("Your api key is invalid. Please generate a new one.")
    }

    private fun findDeckSimple(deck: GenericDeck): DeckSearchResult {
        val cards = cardCache.cardsForDeck(deck)
        val token = cardCache.tokenForDeck(deck)
        val synergies = DeckSynergyService.fromDeckWithCards(deck, cards, token)
        return deck.toDeckSearchResult(
            housesAndCards = cardCache.deckToHouseAndCards(deck),
            cards = cards,
            token = token,
            synergies = synergies,
            stats = statsService.findCurrentStats(),
        )
    }

}
