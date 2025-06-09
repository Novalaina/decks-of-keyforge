package coraythan.keyswap.publicapis

import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.RateExceededException
import coraythan.keyswap.config.UnauthorizedException
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
import coraythan.keyswap.users.KeyUserRepo
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

val maxApiRequests = 25

@RestController
@RequestMapping("/public-api")
class PublicApiEndpoints(
    private val publicApiService: PublicApiService,
    private val statsService: StatsService,
    private val cardCache: DokCardCacheService,
    private val keyUserRepo: KeyUserRepo,
    private val currentUserService: CurrentUserService,
    private val tournamentService: TournamentService,
    private val sasVersionService: SasVersionService,
) {

    @Scheduled(fixedDelayString = "PT1M")
    fun clearPublicRateLimiters() {
        try {
            this.rateLimiters.clear()
        } catch (e: Throwable) {
            log.error("$scheduledException clearing rate limiters", e)
        }
    }

    private val rateLimiters = ConcurrentHashMap<String, Int>()

    private val dontRateLimitEmails = listOf("detour27@gmail.com", "skyjedi@gmail.com")

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/api-keys/secured")
    fun generateApiKey() = publicApiService.generateApiKey()

    @CrossOrigin
    @GetMapping("/v3/decks/{id}")
    fun findDeckSimple3(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: String): SimpleDeckResponse {

        val publishedAercVersion = sasVersionService.findSasVersion()
        this.rateLimit(apiKey)

        val deck = publicApiService.findDeckSimple(id)
        return SimpleDeckResponse(deck ?: Nothing(), publishedAercVersion)
    }

    @CrossOrigin
    @GetMapping("/v1/alliance-decks/{id}")
    fun findAllianceDeckSimple(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: UUID): SimpleDeckResponse {

        val publishedAercVersion = sasVersionService.findSasVersion()
        this.rateLimit(apiKey)

        val deck = publicApiService.findAllianceDeckSimple(id)
        return SimpleDeckResponse(deck ?: Nothing(), publishedAercVersion)
    }

    @CrossOrigin
    @GetMapping("/v1/stats")
    fun findStats1(@RequestHeader("Api-Key") apiKey: String): DeckStatistics {

        this.rateLimit(apiKey)

        val user = publicApiService.userForApiKey(apiKey)
        log.info("Deck stats request from user ${user.email}")
        return statsService.findCurrentStats()
            ?: throw BadRequestException("Sorry, deck statistics are not available at this time.")
    }

    @CrossOrigin
    @GetMapping("/v1/cards")
    fun findCards1(@RequestHeader("Api-Key") apiKey: String): List<FrontendCard> {

        this.rateLimit(apiKey)

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
        this.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        return publicApiService.findMyDecks(user, page)
    }

    @CrossOrigin
    @GetMapping("/v1/my-alliances")
    fun findMyAlliances(@RequestHeader("Api-Key") apiKey: String): List<PublicMyDeckInfo> {
        this.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)

        return publicApiService.findMyAlliances(user)
    }

    @CrossOrigin
    @GetMapping("/v1/tournaments/{id}")
    fun findTournamentInfo(@RequestHeader("Api-Key") apiKey: String, @PathVariable id: Long): TournamentInfo {
        this.rateLimit(apiKey)
        return tournamentService.findTourneyInfo(id)
    }

    // Non documented, for library access extension
    @GetMapping("/v1/my-deck-ids")
    fun findMyDeckIds(): List<String> {
        val user = currentUserService.loggedInUserOrUnauthorized()
        return publicApiService.findMyDeckIds(user)
    }

    private fun rateLimit(apiKey: String) {
        if (!this.rateLimiters.containsKey(apiKey) && !keyUserRepo.existsByApiKey(apiKey)) {
            throw UnauthorizedException("Your API key does not exist.")
        }

        val requests = this.rateLimiters.getOrPut(apiKey, { 0 }) + 1
        this.rateLimiters[apiKey] = requests

        if (requests > maxApiRequests) {
            val user = publicApiService.userForApiKey(apiKey)
            val realMaxRequests = user.realPatreonTier()?.maxApiRequests ?: maxApiRequests
            if (requests > realMaxRequests) {
                val userEmail = user.email
                if (realMaxRequests + 1 == requests) log.warn("The user $userEmail sent too many requests.")
                if (!dontRateLimitEmails.contains(userEmail)) {
                    throw RateExceededException(
                        "You've sent too many requests in the last minute. You've sent $requests in the last minute. Decks of KeyForge has been taken down " +
                                "by this type of activity in the past. If you need to know the SAS for all decks, I provide a CSV file you should use for " +
                                "that purpose. Otherwise, please contact me on discord, or at decksofkeyforge@gmail.com"
                    )
                }
            }
        }
    }
}
