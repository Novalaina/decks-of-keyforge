package coraythan.keyswap.decks

import coraythan.keyswap.decks.models.Deck
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

@Transactional
interface DeckRepo : JpaRepository<Deck, Long>, QuerydslPredicateExecutor<Deck> {
    fun findByKeyforgeId(keyforgeId: String): Deck?
    fun existsByKeyforgeId(keyforgeId: String): Boolean
    fun findAllByKeyforgeIdIn(kfIds: Collection<String>): List<Deck>

    fun findByWinsGreaterThanOrLossesGreaterThan(wins: Int, losses: Int): List<Deck>

    @Query("SELECT d.id FROM #{#entityName} d WHERE d.forTrade = true OR d.forSale = true")
    fun listedDeckIds(): List<Long>

    @Query(
        value = "SELECT reltuples\\:\\:BIGINT AS estimate FROM pg_class WHERE relname='deck'",
        nativeQuery = true
    )
    fun estimateRowCount(): Long

    fun countByExpansion(expansion: Int): Long

    @Modifying
    @Query("UPDATE Deck d SET d.forSale = :forSale, d.forTrade = :forTrade WHERE d.id = :id")
    fun updateForSaleAndTrade(@Param(value = "id") id: Long, @Param(value = "forSale") forSale: Boolean, @Param(value = "forTrade") forTrade: Boolean)

    @Modifying
    @Query("UPDATE Deck d SET d.hasOwnershipVerification = :updateOwnershipVerification WHERE d.id = :id")
    fun updateHasOwnershipVerification(@Param(value = "id") id: Long, @Param(value = "updateOwnershipVerification") updateOwnershipVerification: Boolean)
}
