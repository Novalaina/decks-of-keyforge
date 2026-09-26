package coraythan.keyswap.contentcreators

import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.decks.SortDirection

@GenerateTs
data class ContentCreatorFilters(
    val contentName: String = "",
    val username: String = "",
    val types: List<ContentCreatorType> = listOf(),
    val sort: ContentCreatorSort = ContentCreatorSort.UPDATED,
    val sortDirection: SortDirection = SortDirection.DESC,
)

@GenerateTs
enum class ContentCreatorSort {
    CONTENT_NAME,
    UPDATED,
}
