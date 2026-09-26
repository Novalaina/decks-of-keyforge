package coraythan.keyswap.contentcreators

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import java.util.*

interface ContentCreatorDetailsRepo : JpaRepository<ContentCreatorDetails, UUID>,
    QuerydslPredicateExecutor<ContentCreatorDetails> {

    fun findByUserId(userId: UUID): List<ContentCreatorDetails>

    fun findByFeaturedTrue(): List<ContentCreatorDetails>

    @Modifying
    @Query("update ContentCreatorDetails ccd set ccd.featured = false where ccd.featured = true")
    fun clearFeatured()

    @Modifying
    @Query("update ContentCreatorDetails ccd set ccd.featured = true, ccd.timesFeatured = ccd.timesFeatured + 1 where ccd.id = :id")
    fun markFeatured(@Param("id") id: UUID)
}

interface ContentCreatorMonthlyClicksRepo : JpaRepository<ContentCreatorMonthlyClicks, UUID> {

    fun findByContentCreatorIdAndYearMonth(contentCreatorId: UUID, yearMonth: String): ContentCreatorMonthlyClicks?

    fun findByContentCreatorId(contentCreatorId: UUID): List<ContentCreatorMonthlyClicks>

    /**
     * Only queries months within the scoring window. Older months are ignored entirely.
     */
    fun findByContentCreatorIdInAndYearMonthGreaterThanEqual(
        contentCreatorIds: Collection<UUID>,
        yearMonth: String
    ): List<ContentCreatorMonthlyClicks>

    @Modifying
    @Query("update ContentCreatorMonthlyClicks cc set cc.clicks = cc.clicks + 1 where cc.id = :id")
    fun incrementClicks(@Param("id") id: UUID)

    fun deleteByContentCreatorId(contentCreatorId: UUID)
}
