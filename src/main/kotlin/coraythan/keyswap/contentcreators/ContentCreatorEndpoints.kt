package coraythan.keyswap.contentcreators

import coraythan.keyswap.Api
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("${Api.base}/content-creators")
class ContentCreatorEndpoints(
    private val contentCreatorService: ContentCreatorService
) {

    @PostMapping("/public/search")
    fun searchContentCreators(@RequestBody filters: ContentCreatorFilters) =
        contentCreatorService.searchContentCreators(filters)

    @GetMapping("/featured")
    fun featuredContent(): ResponseEntity<List<ContentCreatorDetailsDto>> {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .body(contentCreatorService.findFeaturedContent())
    }

    @GetMapping("/public/{id}")
    fun findContentCreator(@PathVariable id: UUID) = contentCreatorService.findContentCreator(id)

    @GetMapping("/public/{id}/monthly-clicks")
    fun findMonthlyClicks(@PathVariable id: UUID) = contentCreatorService.findMonthlyClicks(id)

    @PostMapping("/public/{id}/click")
    fun recordClick(@PathVariable id: UUID) = contentCreatorService.recordClick(id)

    @GetMapping("/secured/mine")
    fun findMyContentCreators() = contentCreatorService.findMyContentCreators()

    @PostMapping("/secured")
    fun saveContentCreator(@RequestBody toSave: SaveContentCreatorDetails) =
        contentCreatorService.saveContentCreator(toSave)

    @DeleteMapping("/secured/{id}")
    fun deleteContentCreator(@PathVariable id: UUID) = contentCreatorService.deleteContentCreator(id)

    @PostMapping("/secured/{id}/promo-image")
    fun addPromoImage(
        @PathVariable id: UUID,

        @RequestParam("promoImage")
        promoImage: MultipartFile,

        @RequestHeader("Extension")
        extension: String,
    ) = contentCreatorService.addPromoImage(id, promoImage, extension)

    @DeleteMapping("/secured/{id}/promo-image")
    fun deletePromoImage(@PathVariable id: UUID) = contentCreatorService.deletePromoImage(id)
}
