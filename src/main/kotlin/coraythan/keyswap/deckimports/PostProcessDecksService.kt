package coraythan.keyswap.deckimports

import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.decks.DeckCardQuantity
import coraythan.keyswap.decks.DeckFilters
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.DeckSearchService
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.decks.models.DeckSearchResult
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.scheduledStart
import coraythan.keyswap.scheduledStop
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.scheduling.annotation.Scheduled
import coraythan.keyswap.config.SchedulingConfig
import java.util.*

private const val lockPostProcessDecksFor = "PT1H"

@Transactional
@Service
class PostProcessDecksService(
    private val repo: PostProcessDeckRepo,
    private val deckRepo: DeckRepo,
    private val deckSearchService: DeckSearchService,
    private val cardCache: DokCardCacheService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelayString = lockPostProcessDecksFor, initialDelayString = SchedulingConfig.postProcessDecksDelay)
    fun postProcessDecks() {

        var twins = 0

        val process = repo.findAllLimit100()
        log.info("$scheduledStart post process ${process.size} decks.")

        val processed = mutableListOf<UUID>()

        for (toProcess in process) {
            val deck = deckRepo.findByIdOrNull(toProcess.deckId)

            if (deck == null) {
                log.error("Couldn't find a deck for post process deck ${toProcess.deckId}")
            } else {
                processed.add(toProcess.id)

                if (deck.expansionEnum == Expansion.DARK_TIDINGS) {
                    val twin = findTwin(deck)

                    if (twin != null) {
                        twins++
                        deckRepo.save(deck.copy(twinId = twin.keyforgeId))
                        deckRepo.save(deckRepo.findByIdOrNull(twin.id)!!.copy(twinId = deck.keyforgeId))
                    } else if (deck.twinId != null) {
                        deckRepo.save(deck.copy(twinId = null))
                    }
                }
            }
        }

        processed.forEach {
            repo.deleteById(it)
        }

        log.info("$scheduledStop Post processed ${processed.size} decks. Processed $twins twins.")
    }

    fun addPostProcessDeck(deck: Deck) {
        if (deck.expansionEnum == Expansion.DARK_TIDINGS) {
            repo.save(PostProcessDeck(deck.id))
        }
    }

    private fun findTwin(deck: Deck): DeckSearchResult? {
        if (deck.expansionEnum != Expansion.DARK_TIDINGS) return null
        val twinCards = cardCache.twinnedCardsForDeck(deck)
            .groupBy { it.card.cardTitle + it.house }

        val twinFilters = DeckFilters(
            cards = twinCards.map {
                val card = it.value.first()
                DeckCardQuantity(listOf(card.card.cardTitle), it.value.size, if (card.maverick) card.house else null)
            },
            expansions = listOf(496)
        )

        return deckSearchService.filterDecks(twinFilters, 0).decks.firstOrNull { it.id != deck.id }
    }
}


@Entity
data class PostProcessDeck(

    val deckId: Long,

    @Id
    val id: UUID = UUID.randomUUID()
)

interface PostProcessDeckRepo : CrudRepository<PostProcessDeck, UUID> {
    @Query("SELECT * FROM post_process_deck LIMIT 100", nativeQuery = true)
    fun findAllLimit100(): List<PostProcessDeck>
}
