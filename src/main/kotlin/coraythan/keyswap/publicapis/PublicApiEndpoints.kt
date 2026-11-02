package coraythan.keyswap.publicapis

import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.decks.Nothing
import coraythan.keyswap.decks.SimpleDeckResponse
import coraythan.keyswap.cards.FrontendCard
import coraythan.keyswap.keyforgeevents.tournaments.TournamentInfo
import coraythan.keyswap.keyforgeevents.tournaments.TournamentService
import coraythan.keyswap.sasupdate.SasVersionService
import coraythan.keyswap.scheduledException
import coraythan.keyswap.stats.DeckStatistics
import coraythan.keyswap.stats.StatsService
import coraythan.keyswap.users.CurrentUserService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import java.util.*

val maxApiRequests = 25

@RestController
@RequestMapping("/public-api")
class PublicApiEndpoints(
    private val publicApiService: PublicApiService,
    private val statsService: StatsService,
    private val cardCache: DokCardCacheService,
    private val currentUserService: CurrentUserService,
    private val tournamentService: TournamentService,
    private val sasVersionService: SasVersionService,
) {

    @Scheduled(fixedDelayString = "PT1M")
    fun clearPublicRateLimiters() {
        try {
            publicApiService.clearRateLimiters()
        } catch (e: Throwable) {
            log.error("$scheduledException clearing rate limiters", e)
        }
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/api-keys/secured")
    fun generateApiKey() = publicApiService.generateApiKey()

    @CrossOrigin
    @GetMapping("/v3/decks/{id}")
    fun findDeckSimple3(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: String): SimpleDeckResponse {

        val publishedAercVersion = sasVersionService.findSasVersion()
        publicApiService.rateLimit(apiKey)

        val deck = publicApiService.findDeckSimple(id)
        return SimpleDeckResponse(deck ?: Nothing(), publishedAercVersion)
    }

    @CrossOrigin
    @GetMapping("/v1/alliance-decks/{id}")
    fun findAllianceDeckSimple(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: UUID): SimpleDeckResponse {

        val publishedAercVersion = sasVersionService.findSasVersion()
        publicApiService.rateLimit(apiKey)

        val deck = publicApiService.findAllianceDeckSimple(id)
        return SimpleDeckResponse(deck ?: Nothing(), publishedAercVersion)
    }

    @CrossOrigin
    @GetMapping("/v1/stats")
    fun findStats1(@RequestHeader("Api-Key") apiKey: String): DeckStatistics {

        publicApiService.rateLimit(apiKey)

        val user = publicApiService.userForApiKey(apiKey)
        log.info("Deck stats request from user ${user.email}")
        return statsService.findCurrentStats()
            ?: throw BadRequestException("Sorry, deck statistics are not available at this time.")
    }

    @CrossOrigin
    @GetMapping("/v1/cards")
    fun findCards1(@RequestHeader("Api-Key") apiKey: String): List<FrontendCard> {

        publicApiService.rateLimit(apiKey)

        val user = publicApiService.userForApiKey(apiKey)
        log.info("Cards request from user ${user.email}")
        return cardCache.currentCards()
    }

    @CrossOrigin
    @GetMapping("/v1/my-decks")
    fun findMyDecks(
        @RequestHeader("Api-Key") apiKey: String,
        @RequestParam page: Int? = null
    ): List<PublicMyDeckInfo> {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        return publicApiService.findMyDecks(user, page)
    }

    @CrossOrigin
    @GetMapping("/v1/my-alliances")
    fun findMyAlliances(@RequestHeader("Api-Key") apiKey: String): List<PublicMyDeckInfo> {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)

        return publicApiService.findMyAlliances(user)
    }

    @CrossOrigin
    @GetMapping("/v1/tournaments/{id}")
    fun findTournamentInfo(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: Long): TournamentInfo {
        publicApiService.rateLimit(apiKey)
        return tournamentService.findTourneyInfo(id)
    }

    // Non documented, for library access extension
    @GetMapping("/v1/my-deck-ids")
    fun findMyDeckIds(): List<String> {
        val user = currentUserService.loggedInUserOrUnauthorized()
        return publicApiService.findMyDeckIds(user)
    }

}
