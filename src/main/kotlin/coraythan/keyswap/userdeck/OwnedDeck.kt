package coraythan.keyswap.userdeck

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.nowLocal
import coraythan.keyswap.users.KeyUser
import jakarta.persistence.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.LocalDateTime
import java.util.*

@Entity
data class OwnedDeck(

        @ManyToOne
        val owner: KeyUser,

        @JsonIgnoreProperties("ownedDecks")
        @ManyToOne(optional = false)
        @JoinColumn(name = "deck_id", insertable = false, updatable = false)
        val deck: Deck,

        @Column(name = "deck_id", nullable = false)
        val deckId: Long,

        val teamId: UUID?,

        val added: LocalDateTime = nowLocal(),

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "hibernate_sequence")
        @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
        val id: Long = -1
)

interface OwnedDeckRepo : CrudRepository<OwnedDeck, Long> {
        fun findAllByOwnerId(ownerId: UUID): List<OwnedDeck>
        fun existsByDeckIdAndOwnerId(deckId: Long, ownerId: UUID): Boolean
        fun deleteByDeckIdAndOwnerId(deckId: Long, ownerId: UUID)

        fun findByDeckId(deckId: Long): List<OwnedDeck>
        fun findByDeckIdAndTeamId(deckId: Long, teamId: UUID): List<OwnedDeck>

        @Query("SELECT ownedDeck FROM OwnedDeck ownedDeck WHERE ownedDeck.deckId = ?1 AND ownedDeck.owner.username IN ?2")
        fun findByDeckIdAndOwnedByIn(deckId: Long, ownerUsernames: List<String>): List<OwnedDeck>

        @Modifying
        @Query("UPDATE OwnedDeck ownedDeck SET ownedDeck.teamId = ?1 WHERE ownedDeck.owner.id = ?2")
        fun addTeamForUser(teamId: UUID, userId: UUID)

        @Modifying
        @Query("UPDATE OwnedDeck ownedDeck SET ownedDeck.teamId = null WHERE ownedDeck.owner.id = ?1")
        fun removeTeamForUser(userId: UUID)
}

interface OwnedDeckPageableRepo : PagingAndSortingRepository<OwnedDeck, Long> {
        fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): List<OwnedDeck>
}
