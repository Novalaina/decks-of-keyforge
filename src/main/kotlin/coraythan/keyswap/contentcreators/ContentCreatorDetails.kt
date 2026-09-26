package coraythan.keyswap.contentcreators

import com.fasterxml.jackson.annotation.JsonIgnore
import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.nowLocal
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.patreon.levelAtLeast
import coraythan.keyswap.users.KeyUser
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Patrons at this tier or higher may upload a promo image, and only their images are displayed.
 */
val promoImagePatreonTier = PatreonRewardsTier.SUPPORT_SOPHISTICATION

@Entity
data class ContentCreatorDetails(

    val contentName: String,

    @Column(columnDefinition = "VARCHAR(2000)")
    val description: String,

    @Column(length = 500)
    val link: String? = null,

    @Column(length = 500)
    val discordServer: String? = null,

    @Column(length = 500)
    val promoImageKey: String? = null,

    @Enumerated(EnumType.STRING)
    val contentCreatorType: ContentCreatorType,

    val timesFeatured: Int = 0,

    val featured: Boolean = false,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    val user: KeyUser,

    val created: LocalDateTime = nowLocal(),

    val updated: LocalDateTime = nowLocal(),

    @Id
    val id: UUID = UUID.randomUUID(),
) {

    /**
     * Promo images only display while the creator is an active patron of the required tier.
     */
    fun activePromoImageKey(): String? =
        if (promoImageKey != null && user.realPatreonTier().levelAtLeast(promoImagePatreonTier)) promoImageKey else null

    fun toDto(contentCreatorScore: Int) = ContentCreatorDetailsDto(
        id = id,
        contentName = contentName,
        description = description,
        link = link,
        discordServer = discordServer,
        promoImageKey = activePromoImageKey(),
        contentCreatorType = contentCreatorType,
        username = user.username,
        userId = user.id,
        created = created,
        updated = updated,
        timesFeatured = timesFeatured,
        contentCreatorScore = contentCreatorScore,
    )

    override fun equals(other: Any?) = other is ContentCreatorDetails && id == other.id

    override fun hashCode() = id.hashCode()
}

@GenerateTs
data class ContentCreatorDetailsDto(
    val id: UUID,
    val contentName: String,
    val description: String,
    val link: String?,
    val discordServer: String?,
    val promoImageKey: String?,
    val contentCreatorType: ContentCreatorType,
    val username: String,
    val userId: UUID,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val timesFeatured: Int,
    val contentCreatorScore: Int,
)

@GenerateTs
data class SaveContentCreatorDetails(
    val id: UUID? = null,
    val contentName: String,
    val description: String,
    val link: String? = null,
    val discordServer: String? = null,
    val contentCreatorType: ContentCreatorType,
)
