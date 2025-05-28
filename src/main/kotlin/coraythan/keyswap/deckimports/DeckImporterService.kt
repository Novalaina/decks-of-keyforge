package coraythan.keyswap.deckimports

import coraythan.keyswap.cards.CardService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.SchedulingConfig
import coraythan.keyswap.decks.DeckPageService
import coraythan.keyswap.decks.DeckPageType
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.expansions.activeExpansions
import coraythan.keyswap.scheduledStart
import coraythan.keyswap.scheduledStop
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeDeckDto
import coraythan.keyswap.thirdpartyservices.mastervault.KeyforgeApi
import coraythan.keyswap.thirdpartyservices.mastervault.keyforgeApiDeckPageSize
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID
import kotlin.system.measureTimeMillis

private const val lockImportNewDecksFor = "PT1M"
private const val lockImportSkippedDecksFor = "PT3M"

var deckImportingUpToDate = false

@Service
class DeckImporterService(
    private val keyforgeApi: KeyforgeApi,
    private val cardService: CardService,
    private val deckRepo: DeckRepo,
    private val deckPageService: DeckPageService,
    private val deckCreationService: DeckCreationService,
    private val importSkippedDeckRepo: ImportSkippedDeckRepo,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(
        fixedDelayString = lockImportNewDecksFor,
        initialDelayString = SchedulingConfig.importNewDecksInitialDelay
    )
    @SchedulerLock(
        name = "importNewDecks",
        lockAtLeastFor = lockImportNewDecksFor,
        lockAtMostFor = lockImportNewDecksFor
    )
    fun importNewDecks() {
        log.info("$scheduledStart new deck import.")

        deckImportingUpToDate = false
        var decksAdded = 0
        var pagesRequested = 0
        val importDecksDuration = measureTimeMillis {
            var currentPage = deckPageService.findCurrentPage(DeckPageType.IMPORT)

            val maxPageRequests = 20
            while (pagesRequested < maxPageRequests) {
                if (pagesRequested != 0) Thread.sleep(3000)
                log.info("Importing decks, making page request $currentPage")
                try {
                    val decks = keyforgeApi.findDecks(currentPage, withCards = true)
                    if (decks == null) {
                        deckImportingUpToDate = true
                        log.info("Got null decks from the api for page $currentPage decks per page $keyforgeApiDeckPageSize")
                        break
                    } else if (decks.data.any {
                            // Only import decks from these sets
                            // this.log.info("Active expansions is: ${activeExpansions.toString()}")
                            !activeExpansions.map { expansion ->
                                expansion.expansionNumber
                            }.contains(it.expansion)
                        }) {

                        log.info(
                            "Stopping deck import. Unknown expansion number among ${decks.data.map { it.expansion }}. Request URL: https://www.keyforgegame.com/api/decks/v2${
                                keyforgeApi.findDecksRequestUrl(
                                    currentPage,
                                    withCards = true
                                )
                            }"
                        )
                        break
                    } else {

                        val decksToSaveCount = decks.data.count()

                        // Import cards first, then save decks
                        decks.data.forEach {
                            val cards = KeyForgeDeckDto(it)
                            cardService.importNewCardsForDeck(cards)
                        }

                        val results = deckCreationService.saveDecks(decks.data)

                        if (decksToSaveCount >= keyforgeApiDeckPageSize) {
                            val nextPage = currentPage + 1
                            log.info("Updating next deck page to $nextPage. Saved ${results.size} out of $decksToSaveCount")
                            deckPageService.setCurrentPage(nextPage, DeckPageType.IMPORT)
                        }

                        decksAdded += results.size
                        currentPage++
                        pagesRequested++

                        if (decksToSaveCount < keyforgeApiDeckPageSize) {
                            deckImportingUpToDate = true
                            log.info(
                                "Stopped getting decks. Found $decksToSaveCount in this page. Added ${results.size} < $keyforgeApiDeckPageSize new decks. Expansions: ${
                                    decks.data.map {
                                        Expansion.forExpansionNumberNullable(it.expansion)
                                            ?.toString() ?: "Unknown expansion with id: ${it.expansion}"
                                    }.toSortedSet()
                                }"
                            )
                            break
                        }
                    }
                } catch (e: HttpClientErrorException.TooManyRequests) {
                    log.warn("KeyForge API says we made too many requests. Sad day." + e.message)
                    break
                }
            }
        }
        val deckCountNow = deckRepo.estimateRowCount()
        log.info(
            "$scheduledStop Added $decksAdded decks. Total decks: $deckCountNow.  " +
                    "Pages requested $pagesRequested It took ${importDecksDuration / 1000} seconds."
        )
    }

    //    @Scheduled(fixedDelayString = "PT3M", initialDelayString = SchedulingConfig.postProcessDecksDelay)
    fun processSkippedDecks() {
        log.info("$scheduledStart Process import skipped decks.")

        val process = importSkippedDeckRepo.findAllLimit1()

        val processed = mutableListOf<UUID>()

        for (toProcess in process) {
            importDeck(toProcess.deckKeyforgeId)
        }

        processed.forEach {
            importSkippedDeckRepo.deleteById(it)
        }

        log.info("$scheduledStop Imported ${processed.size} skipped decks.")
    }

    fun updateDeck(deckId: String) {
        val deck = keyforgeApi.findDeckToImport(deckId)?.deck ?: throw BadRequestException("No deck in MV for $deckId")
        cardService.refreshAllCardsForDeck(deck)
        deckCreationService.updateDeck(deck)
    }

    fun importDeck(deckId: String): Long? {
        val preExistingDeck = deckRepo.findByKeyforgeId(deckId)
        if (preExistingDeck != null) {
            return preExistingDeck.id
        } else {
            val deck = keyforgeApi.findDeckToImport(deckId)?.deck
            if (deck != null) {
                val expansion = Expansion.forExpansionNumber(deck.data.expansion)
                if (!activeExpansions.contains(expansion)) {
                    throw BadRequestException("$expansion is not yet enabled for import in DoK.")
                }
                // Import cards first, then save deck
                cardService.importNewCardsForDeck(deck)

                val deckList = listOf(deck.data.copy(cards = deck.data._links?.cards))

                return try {
                    deckCreationService.saveDecks(deckList).first()
                } catch (e: RuntimeException) {
                    if (e::class.java == DataIntegrityViolationException::class.java || e::class.java == ConstraintViolationException::class.java) {
                        // We must have a pre-existing deck now
                        log.info("Encountered exception saving deck to import, but it was just the deck already being saved")
                        deckRepo.findByKeyforgeId(deckId)?.id
                    } else {
                        throw e
                    }
                }
            }
        }
        return null
    }
}
