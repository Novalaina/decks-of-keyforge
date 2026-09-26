package coraythan.keyswap.contentcreators

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.decks.SortDirection
import coraythan.keyswap.nowLocal
import coraythan.keyswap.patreon.levelAtLeast
import coraythan.keyswap.scheduledException
import coraythan.keyswap.scheduledStart
import coraythan.keyswap.scheduledStop
import coraythan.keyswap.thirdpartyservices.S3Service
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.UserType
import jakarta.persistence.EntityManager
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Number of content creators shown in the Featured Content section.
 */
const val featuredContentCount = 5

/**
 * Number of the featured slots reserved for the highest scoring content creators.
 */
const val featuredContentTopScoreCount = 3

/**
 * Once a content creator has been featured this many times via the random slots they are no longer
 * eligible for them.
 */
const val maxRandomFeatures = 10

const val maxContentNameLength = 255
const val maxDescriptionLength = 2000
const val maxLinkLength = 500

@Transactional
@Service
class ContentCreatorService(
    private val contentCreatorDetailsRepo: ContentCreatorDetailsRepo,
    private val monthlyClicksRepo: ContentCreatorMonthlyClicksRepo,
    private val currentUserService: CurrentUserService,
    private val s3Service: S3Service,
    entityManager: EntityManager,
) {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val query = JPAQueryFactory(entityManager)

    fun searchContentCreators(filters: ContentCreatorFilters): List<ContentCreatorDetailsDto> {
        val ccQ = QContentCreatorDetails.contentCreatorDetails
        val predicate = BooleanBuilder()

        val contentName = filters.contentName.trim()
        if (contentName.isNotBlank()) {
            predicate.and(ccQ.contentName.likeIgnoreCase("%$contentName%"))
        }
        val username = filters.username.trim()
        if (username.isNotBlank()) {
            predicate.and(ccQ.user.username.likeIgnoreCase("%$username%"))
        }
        if (filters.types.isNotEmpty()) {
            predicate.and(ccQ.contentCreatorType.`in`(filters.types))
        }

        val descending = filters.sortDirection == SortDirection.DESC
        val order: OrderSpecifier<*> = when (filters.sort) {
            ContentCreatorSort.CONTENT_NAME -> if (descending) ccQ.contentName.desc() else ccQ.contentName.asc()
            ContentCreatorSort.UPDATED -> if (descending) ccQ.updated.desc() else ccQ.updated.asc()
        }

        val found = query.selectFrom(ccQ)
            .leftJoin(ccQ.user).fetchJoin()
            .where(predicate)
            .orderBy(order)
            .fetch()

        return found.toDtos()
    }

    fun findFeaturedContent(): List<ContentCreatorDetailsDto> {
        return contentCreatorDetailsRepo.findByFeaturedTrue()
            .toDtos()
            .sortedByDescending { it.contentCreatorScore }
    }

    fun findMyContentCreators(): List<ContentCreatorDetailsDto> {
        val user = currentUserService.loggedInUserOrUnauthorized()
        return contentCreatorDetailsRepo.findByUserId(user.id).toDtos()
    }

    fun findContentCreator(id: UUID): ContentCreatorDetailsDto {
        val found = contentCreatorDetailsRepo.findByIdOrNull(id)
            ?: throw BadRequestException("No content creator details for id $id")
        return listOf(found).toDtos().first()
    }

    fun saveContentCreator(toSave: SaveContentCreatorDetails): ContentCreatorDetailsDto {
        val user = currentUserService.loggedInUserOrUnauthorized()

        val contentName = toSave.contentName.trim()
        val description = toSave.description.trim()
        val link = toSave.link?.trim()?.ifBlank { null }
        val discordServer = toSave.discordServer?.trim()?.ifBlank { null }

        if (contentName.isBlank() || contentName.length > maxContentNameLength) {
            throw BadRequestException("Content name must be between 1 and $maxContentNameLength characters.")
        }
        if (description.isBlank() || description.length > maxDescriptionLength) {
            throw BadRequestException("Description must be between 1 and $maxDescriptionLength characters.")
        }
        if (link == null && discordServer == null) {
            throw BadRequestException("Please include a link or a Discord server link.")
        }
        if (link != null) {
            validateLink(link, "Link")
        }
        if (discordServer != null) {
            validateLink(discordServer, "Discord server link")
        }

        val existingId = toSave.id
        val saved = if (existingId == null) {
            contentCreatorDetailsRepo.save(
                ContentCreatorDetails(
                    contentName = contentName,
                    description = description,
                    link = link,
                    discordServer = discordServer,
                    contentCreatorType = toSave.contentCreatorType,
                    user = user,
                )
            )
        } else {
            val preexisting = findEditable(existingId)
            contentCreatorDetailsRepo.save(
                preexisting.copy(
                    contentName = contentName,
                    description = description,
                    link = link,
                    discordServer = discordServer,
                    contentCreatorType = toSave.contentCreatorType,
                    updated = nowLocal(),
                )
            )
        }

        return listOf(saved).toDtos().first()
    }

    fun deleteContentCreator(id: UUID) {
        findEditable(id)
        monthlyClicksRepo.deleteByContentCreatorId(id)
        contentCreatorDetailsRepo.deleteById(id)
    }

    fun addPromoImage(id: UUID, promoImage: MultipartFile, extension: String): ContentCreatorDetailsDto {
        val details = findEditable(id)
        val user = currentUserService.loggedInUserOrUnauthorized()
        if (!user.realPatreonTier().levelAtLeast(promoImagePatreonTier) && user.type != UserType.ADMIN) {
            throw UnauthorizedException("You must be a $promoImagePatreonTier patron or higher to add a promo image.")
        }

        if (details.promoImageKey != null) {
            s3Service.deleteUserContent(details.promoImageKey)
        }
        val key = s3Service.addContentCreatorPromoImage(promoImage, details.id, extension)
        val saved = contentCreatorDetailsRepo.save(details.copy(promoImageKey = key, updated = nowLocal()))
        return listOf(saved).toDtos().first()
    }

    fun deletePromoImage(id: UUID): ContentCreatorDetailsDto {
        val details = findEditable(id)
        if (details.promoImageKey != null) {
            s3Service.deleteUserContent(details.promoImageKey)
        }
        val saved = contentCreatorDetailsRepo.save(details.copy(promoImageKey = null, updated = nowLocal()))
        return listOf(saved).toDtos().first()
    }

    /**
     * Records a click on a content creator's link for the current month.
     */
    fun recordClick(id: UUID) {
        val yearMonth = currentContentCreatorYearMonth().toString()
        val existing = monthlyClicksRepo.findByContentCreatorIdAndYearMonth(id, yearMonth)
        if (existing != null) {
            monthlyClicksRepo.incrementClicks(existing.id)
            return
        }

        val details = contentCreatorDetailsRepo.findByIdOrNull(id)
            ?: throw BadRequestException("No content creator details for id $id")
        try {
            monthlyClicksRepo.save(
                ContentCreatorMonthlyClicks(contentCreatorId = details.id, yearMonth = yearMonth, clicks = 1)
            )
        } catch (e: DataIntegrityViolationException) {
            // Another request created the row for this month first.
            val raced = monthlyClicksRepo.findByContentCreatorIdAndYearMonth(id, yearMonth)
            if (raced != null) monthlyClicksRepo.incrementClicks(raced.id)
        }
    }

    fun findMonthlyClicks(id: UUID): List<ContentCreatorMonthlyClicksDto> {
        return monthlyClicksRepo.findByContentCreatorId(id)
            .sortedByDescending { it.yearMonth }
            .map { it.toDto() }
    }

    /**
     * Refreshes the Featured Content list once a day at midnight Pacific time.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Los_Angeles")
    @SchedulerLock(name = "refreshFeaturedContent", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
    fun refreshFeaturedContent() {
        try {
            log.info("$scheduledStart refresh featured content.")
            val millisTaken = measureTimeMillis { selectFeaturedContent() }
            log.info("$scheduledStop refreshing featured content. It took millis: $millisTaken")
        } catch (e: Throwable) {
            log.error("$scheduledException refreshing featured content", e)
        }
    }

    /**
     * Picks the featured content creators.
     *
     * The three highest scoring creators take the first slots, ties broken randomly. The remaining
     * two slots go to randomly chosen creators with the lowest timesFeatured that are still under
     * [maxRandomFeatures]. If there aren't enough of those, the slots are filled with the next
     * highest scoring creators instead.
     */
    fun selectFeaturedContent() {
        val all = contentCreatorDetailsRepo.findAll()
        if (all.isEmpty()) {
            contentCreatorDetailsRepo.clearFeatured()
            return
        }

        val scores = all.scores()

        // Shuffling before sorting gives us random tie breaking, since sortedBy is stable.
        val byScore = all.shuffled().sortedByDescending { scores[it.id] ?: 0 }

        val topScoring = byScore.take(featuredContentTopScoreCount)
        val topScoringIds = topScoring.map { it.id }.toSet()
        val remaining = byScore.filter { !topScoringIds.contains(it.id) }

        val randomSlots = featuredContentCount - topScoring.size
        val randomPicks = remaining
            .filter { it.timesFeatured < maxRandomFeatures }
            .shuffled()
            .sortedBy { it.timesFeatured }
            .take(randomSlots)
        val randomPickIds = randomPicks.map { it.id }.toSet()

        // remaining is already ordered by score descending, so this fills with the best scores left.
        val fillIns = remaining
            .filter { !randomPickIds.contains(it.id) }
            .take(randomSlots - randomPicks.size)

        contentCreatorDetailsRepo.clearFeatured()
        (topScoring + randomPicks + fillIns).forEach {
            contentCreatorDetailsRepo.markFeatured(it.id)
        }
    }

    private fun findEditable(id: UUID): ContentCreatorDetails {
        val user = currentUserService.loggedInUserOrUnauthorized()
        val details = contentCreatorDetailsRepo.findByIdOrNull(id)
            ?: throw BadRequestException("No content creator details for id $id")
        if (details.user.id != user.id && user.type != UserType.ADMIN) {
            throw UnauthorizedException("You don't own content creator details with id $id")
        }
        return details
    }

    private fun validateLink(link: String, name: String) {
        if (link.length > maxLinkLength) {
            throw BadRequestException("$name must be $maxLinkLength characters or less.")
        }
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            throw BadRequestException("$name must be a URL starting with http:// or https://")
        }
    }

    private fun List<ContentCreatorDetails>.toDtos(): List<ContentCreatorDetailsDto> {
        val scores = this.scores()
        return this.map { it.toDto(scores[it.id] ?: 0) }
    }

    /**
     * Only months within the scoring window are queried, older click records are ignored.
     */
    private fun List<ContentCreatorDetails>.scores(): Map<UUID, Int> {
        if (this.isEmpty()) return mapOf()
        val currentMonth = currentContentCreatorYearMonth()
        val oldestMonth = currentMonth.minusMonths((contentCreatorScoreMonths - 1).toLong()).toString()
        val ids = this.map { it.id }.toSet()
        val clicksByCreator = monthlyClicksRepo
            .findByContentCreatorIdInAndYearMonthGreaterThanEqual(ids, oldestMonth)
            .groupBy { it.contentCreatorId }

        return this.associate { details ->
            details.id to calculateContentCreatorScore(
                monthlyClicks = clicksByCreator[details.id] ?: listOf(),
                hasActivePromoImage = details.activePromoImageKey() != null,
                currentMonth = currentMonth,
            )
        }
    }
}
