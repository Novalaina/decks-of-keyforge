package coraythan.keyswap.publicapis

import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.patreon.levelAtLeast
import coraythan.keyswap.tags.CreateTag
import coraythan.keyswap.tags.TagDto
import coraythan.keyswap.tags.TagService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

data class BulkDeckRequest(val deckIds: List<String>)

@Transactional
@RestController
@RequestMapping("/public-api/v1/tags")
class PublicTagApiEndpoints(
    private val publicApiService: PublicApiService,
    private val tagService: TagService,
    private val deckRepo: DeckRepo,
) {

    @CrossOrigin
    @PostMapping
    fun createTag(
        @RequestHeader("Api-Key") apiKey: String,
        @RequestBody createTag: CreateTag,
    ): TagDto {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        if (user.realPatreonTier()?.levelAtLeast(PatreonRewardsTier.NOTICE_BARGAINS) != true) {
            throw UnauthorizedException("Insufficient patreon tier to manage tags.")
        }
        return tagService.createTagForUser(createTag, user)
    }

    @CrossOrigin
    @GetMapping
    fun listTags(@RequestHeader("Api-Key") apiKey: String): List<TagDto> {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        if (user.realPatreonTier()?.levelAtLeast(PatreonRewardsTier.NOTICE_BARGAINS) != true) {
            throw UnauthorizedException("Insufficient patreon tier to manage tags.")
        }
        return tagService.findTagsByUser(user)
    }

    @CrossOrigin
    @DeleteMapping("/{tagId}")
    fun deleteTag(
        @RequestHeader("Api-Key") apiKey: String,
        @PathVariable tagId: Long,
    ) {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        tagService.deleteTagForUser(tagId, user)
    }

    @CrossOrigin
    @PostMapping("/{tagId}/decks")
    fun tagDecks(
        @RequestHeader("Api-Key") apiKey: String,
        @PathVariable tagId: Long,
        @RequestBody request: BulkDeckRequest,
    ) {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        val decks = deckRepo.findAllByKeyforgeIdIn(request.deckIds)
        val foundIds = decks.map { it.keyforgeId }.toSet()
        val missingIds = request.deckIds.filter { it !in foundIds }
        if (missingIds.isNotEmpty()) throw BadRequestException("Decks not found: ${missingIds.joinToString()}")
        tagService.tagDecksForUser(tagId, decks, user)
    }

    @CrossOrigin
    @DeleteMapping("/{tagId}/decks")
    fun untagDecks(
        @RequestHeader("Api-Key") apiKey: String,
        @PathVariable tagId: Long,
        @RequestBody request: BulkDeckRequest,
    ) {
        publicApiService.rateLimit(apiKey)
        val user = publicApiService.userForApiKey(apiKey)
        val decks = deckRepo.findAllByKeyforgeIdIn(request.deckIds)
        val foundIds = decks.map { it.keyforgeId }.toSet()
        val missingIds = request.deckIds.filter { it !in foundIds }
        if (missingIds.isNotEmpty()) throw BadRequestException("Decks not found: ${missingIds.joinToString()}")
        tagService.untagDecksForUser(tagId, decks, user)
    }
}
