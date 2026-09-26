package coraythan.keyswap.contentcreators

import coraythan.keyswap.generatets.GenerateTs
import jakarta.persistence.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Featured content and click months are calculated in Pacific time.
 */
val contentCreatorZone: ZoneId = ZoneId.of("America/Los_Angeles")

fun currentContentCreatorYearMonth(): YearMonth = YearMonth.from(LocalDate.now(contentCreatorZone))

/**
 * Number of months of click history that count towards the content creator score. Anything older is
 * neither queried nor counted.
 */
const val contentCreatorScoreMonths = 12

const val activePromoImageScoreBonus = 20

/**
 * Multiplier applied to a month's clicks based on how many months ago it was.
 *
 * Current and previous month x10, the month before that x5, the one before that x3, and every other
 * month within the last year x1.
 */
fun contentCreatorMonthMultiplier(monthsAgo: Long): Int = when {
    monthsAgo < 0L -> 0
    monthsAgo <= 1L -> 10
    monthsAgo == 2L -> 5
    monthsAgo == 3L -> 3
    monthsAgo < contentCreatorScoreMonths -> 1
    else -> 0
}

@Entity
@Table(
    name = "content_creator_monthly_clicks",
    uniqueConstraints = [UniqueConstraint(
        name = "content_creator_monthly_clicks_uk",
        columnNames = ["content_creator_id", "year_month"]
    )]
)
data class ContentCreatorMonthlyClicks(

    val contentCreatorId: UUID,

    /**
     * ISO year month, e.g. 2026-09
     */
    val yearMonth: String,

    val clicks: Int = 0,

    @Id
    val id: UUID = UUID.randomUUID(),
) {
    fun toDto() = ContentCreatorMonthlyClicksDto(yearMonth = yearMonth, clicks = clicks)

    override fun equals(other: Any?) = other is ContentCreatorMonthlyClicks && id == other.id

    override fun hashCode() = id.hashCode()
}

@GenerateTs
data class ContentCreatorMonthlyClicksDto(
    val yearMonth: String,
    val clicks: Int,
)

/**
 * Calculates the content creator score from a creator's monthly clicks within the last year.
 */
fun calculateContentCreatorScore(
    monthlyClicks: List<ContentCreatorMonthlyClicks>,
    hasActivePromoImage: Boolean,
    currentMonth: YearMonth = currentContentCreatorYearMonth(),
): Int {
    val clickScore = monthlyClicks.sumOf { clicks ->
        val monthsAgo = ChronoUnit.MONTHS.between(YearMonth.parse(clicks.yearMonth), currentMonth)
        clicks.clicks * contentCreatorMonthMultiplier(monthsAgo)
    }
    return clickScore + if (hasActivePromoImage) activePromoImageScoreBonus else 0
}
