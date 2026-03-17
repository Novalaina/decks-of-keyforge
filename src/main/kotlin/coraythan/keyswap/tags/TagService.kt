package coraythan.keyswap.tags

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import coraythan.keyswap.auctions.DeckListingRepo
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.models.Deck
import coraythan.keyswap.nowLocal
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.scheduledStart
import coraythan.keyswap.scheduledStop
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.KeyUser
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class TagService(
    private val currentUserService: CurrentUserService,
    private val tagRepo: KTagRepo,
    private val deckTagRepo: DeckTagRepo,
    private val deckRepo: DeckRepo,
    private val deckListingRepo: DeckListingRepo,
    private val entityManager: EntityManager
) {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val query = JPAQueryFactory(entityManager)

    @Scheduled(cron = "0 0 1 * * *")
    fun cleanMonthlyViews() {
        log.info("$scheduledStart clean monthly tag views.")
        tagRepo.saveAll(tagRepo.findByPublicityType(PublicityType.PUBLIC).map {
            it.copy(viewsThisMonth = 0)
        })
        log.info("$scheduledStop done cleaning monthly tag views")
    }

    fun viewedTag(id: Long) {
        val user = currentUserService.loggedInUser()
        if (user != null) {
            val tag = tagRepo.findByIdOrNull(id)
            if (tag != null && tag.creator.id != user.id) {
                tagRepo.save(tag.copy(views = tag.views + 1, viewsThisMonth = tag.viewsThisMonth + 1, lastSeen = nowLocal()))
            }
        }
    }

    fun createTag(createTag: CreateTag): TagDto {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)
        if (createTag.public == PublicityType.PUBLIC) {
            currentUserService.hasContributed()
        }

        return tagRepo.save(
            KTag(
                createTag.name,
                user,
                createTag.public,
                archived = createTag.archived,
            )
        ).toDto(0)
    }

    fun updateTagPublicityType(id: Long, publicityType: PublicityType) {
        val user = currentUserService.loggedInUserOrUnauthorized()
        val tag = tagRepo.findByIdOrNull(id) ?: throw IllegalStateException("No tag with id $id")
        if (tag.creator.id != user.id) throw UnauthorizedException("Can only edit your own tags.")
        tagRepo.save(tag.copy(publicityType = publicityType))
    }

    fun findPublicTags() = tagRepo.findByPublicityType(PublicityType.PUBLIC)
        .map { it.toDto(it.decks.size) }
        .sortedWith(compareBy({ it.viewsThisMonth }, { it.created }))

    fun findTag(id: Long) = tagRepo.findByIdOrNull(id)

    fun findMyTags(): List<TagDto> {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)
        return tagRepo.findByCreatorId(user.id)
            .map { it.toDto() }
            .sortedWith(compareBy({ it.publicityType }, { it.name }))
    }

    fun findMyDeckTags(): List<DeckTagDto> {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)
        val deckTagQ = QDeckTag.deckTag
        val predicate = BooleanBuilder().and(deckTagQ.tag.creator.id.eq(user.id))

        return query
            .select(
                Projections.constructor(
                    DeckTagDto::class.java,
                    deckTagQ.tag.id, deckTagQ.deck.id
                )
            )
            .from(deckTagQ)
            .where(predicate)
            .fetch()
    }

    fun deleteTag(tagId: Long) {
        userOwnsTag(tagId)

        val tag = tagRepo.findByIdOrNull(tagId) ?: error("No tag for id $tagId")

        if (tag.publicityType == PublicityType.BULK_SALE) {
            val toUpdate = deckListingRepo.findByTagId(tagId)
            deckListingRepo.saveAll(toUpdate.map { it.copy(tag = null) })
        }

        tagRepo.delete(tag)
    }

    fun archiveTag(id: Long) {
        val user = currentUserService.loggedInUserOrUnauthorized()
        val tag = tagRepo.findByIdOrNull(id) ?: throw IllegalStateException("No tag with id $id")
        if (tag.creator.id != user.id) throw UnauthorizedException("Can only edit your own tags.")
        tagRepo.save(tag.copy(archived = !tag.archived))
    }

    fun tagDeck(deckId: Long, tagId: Long) {
        val tag = userOwnsTag(tagId)
        val deck = deckRepo.findByIdOrNull(deckId) ?: throw IllegalStateException("No deck with id $deckId")
        deckTagRepo.save(DeckTag(tag, deck))
    }

    fun untagDeck(deckId: Long, tagId: Long) {
        userOwnsTag(tagId)
        deckTagRepo.deleteByDeckIdAndTagId(deckId, tagId)
    }

    private fun userOwnsTag(tagId: Long): KTag {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)
        val tag = tagRepo.findByIdOrNull(tagId) ?: throw IllegalStateException("No tag with id $tagId")
        check(tag.creator.id == user.id) {
            throw UnauthorizedException("User ${user.username} does not own tag with id $tagId")
        }
        return tag
    }

    // Methods for API-key-authenticated callers (user already resolved, no SecurityContext)

    fun createTagForUser(createTag: CreateTag, user: KeyUser): TagDto {
        return tagRepo.save(
            KTag(
                createTag.name,
                user,
                createTag.public,
                archived = createTag.archived,
            )
        ).toDto(0)
    }

    fun findTagsByUser(user: KeyUser): List<TagDto> {
        return tagRepo.findByCreatorId(user.id)
            .map { it.toDto() }
            .sortedWith(compareBy({ it.publicityType }, { it.name }))
    }

    fun deleteTagForUser(tagId: Long, user: KeyUser) {
        val tag = tagRepo.findByIdOrNull(tagId) ?: throw IllegalStateException("No tag with id $tagId")
        if (tag.creator.id != user.id) throw UnauthorizedException("User ${user.username} does not own tag with id $tagId")

        if (tag.publicityType == PublicityType.BULK_SALE) {
            val toUpdate = deckListingRepo.findByTagId(tagId)
            deckListingRepo.saveAll(toUpdate.map { it.copy(tag = null) })
        }

        tagRepo.delete(tag)
    }

    fun tagDeckForUser(tagId: Long, deck: Deck, user: KeyUser) {
        val tag = tagRepo.findByIdOrNull(tagId) ?: throw IllegalStateException("No tag with id $tagId")
        if (tag.creator.id != user.id) throw UnauthorizedException("User ${user.username} does not own tag with id $tagId")
        deckTagRepo.save(DeckTag(tag, deck))
    }

    fun untagDeckForUser(tagId: Long, deck: Deck, user: KeyUser) {
        val tag = tagRepo.findByIdOrNull(tagId) ?: throw IllegalStateException("No tag with id $tagId")
        if (tag.creator.id != user.id) throw UnauthorizedException("User ${user.username} does not own tag with id $tagId")
        deckTagRepo.deleteByDeckIdAndTagId(deck.id, tagId)
    }
}
