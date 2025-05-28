package coraythan.keyswap.deckimports

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

@Entity
data class ImportSkippedDeck(

    val deckKeyforgeId: String,

    @Id
    val id: UUID = UUID.randomUUID()
)

interface ImportSkippedDeckRepo : CrudRepository<ImportSkippedDeck, UUID> {
    @Query("SELECT * FROM import_skipped_deck LIMIT 1", nativeQuery = true)
    fun findAllLimit1(): List<ImportSkippedDeck>
    fun existsByDeckKeyforgeId(deckKeyforgeId: String): Boolean
}
