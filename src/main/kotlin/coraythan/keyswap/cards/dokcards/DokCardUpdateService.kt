package coraythan.keyswap.cards.dokcards

import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.cards.CardsVersionService
import coraythan.keyswap.cards.Rarity
import coraythan.keyswap.thirdpartyservices.S3Service
import coraythan.keyswap.thirdpartyservices.S3Service.Companion.cardImagesFolder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class DokCardUpdateService(
    private val versionService: CardsVersionService,
    private val dokCardRepo: DokCardRepo,
    private val dokCardUpdateDao: DokCardUpdateDao,
    private val s3Service: S3Service,
    private val restTemplate: RestTemplate,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun createDoKCardsFromKCards(cards: List<Card>): Boolean {
        var updatedCards = false

        cards.forEach { card ->
            val existingCard = dokCardRepo.findByCardTitleUrl(card.cardTitle.toLegacyUrlFriendlyCardTitle())
            if (card.big == true && card.rarity == Rarity.FIXED) {
                // Skip fixed rarity big cards
                log.info("Skipping saving ${card.cardTitle} because it is a fixed rarity big")
            } else if (existingCard == null) {
                // Brand new card! Let's save it.
                dokCardUpdateDao.saveDokCard(card.id)
                updatedCards = true
            } else {
                // Existing card. Let's update it if necessary
                if (dokCardUpdateDao.updateDokCard(card.id)) updatedCards = true
            }
        }

        if (updatedCards) {
            uploadImagesForCards(cards)
            versionService.revVersion()
        }
        return updatedCards
    }

    fun uploadImagesForCards(cardsToUpload: List<Card>) {

        log.info("Upload images for ${cardsToUpload.map { it.cardTitle }}")

        cardsToUpload.forEach { card ->

            val titleMod = cardUrl(card.cardTitle, card.house, card.anomaly)

            val alreadyExists = s3Service.checkIfCardImageExists(titleMod)

            if (alreadyExists) {
                log.info("Card image $titleMod already exists, don't upload image.")
            } else {
                log.info("Attempt upload for card image $titleMod")
                attemptCardUpload(titleMod, card)
            }
        }
    }

    private fun attemptCardUpload(titleMod: String, card: Card) {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_OCTET_STREAM)

        val entity = HttpEntity<String>(headers)

        try {
            val response = restTemplate.exchange(
                card.frontImage,
                HttpMethod.GET, entity, ByteArray::class.java, "1"
            )

            val byteArray = response.body

            if (response.statusCode == HttpStatus.OK && byteArray != null) {

                runBlocking {
                    s3Service.addCardImage(byteArray, titleMod)
                }

            } else {
                log.warn("No card for ${card.cardTitle} ${card.id}")
            }
        } catch (e: Exception) {
            log.error("Couldn't get card ${card.cardTitle} via endpoint ${card.frontImage}", e)
        }
    }

}

private val cardNameUrlRegex = "[^\\d\\w\\s]".toRegex()
private val whitespaceRegex = "\\s+".toRegex()

fun String.toLegacyUrlFriendlyCardTitle(): String {
    return this
        .trim()
        .replace(cardNameUrlRegex, "")
        .replace(" ", "-")
        .lowercase()
}

fun cardUrl(cardTitle: String, house: House? = null, anomaly: Boolean = false): String {
    return "${if (anomaly) "anomaly" else house.toString().lowercase()}/${
        cardTitle
            .trim()
            .replace("Æ", "ae", ignoreCase = true)
            .replace(cardNameUrlRegex, "")
            .replace(whitespaceRegex, "-")
            .lowercase()
    }"
}

fun cardUrlFull(cardTitle: String, house: House? = null, anomaly: Boolean = false): String {
    return "https://keyforge-card-images.s3-us-west-2.amazonaws.com/$cardImagesFolder/${
        cardUrl(
            cardTitle,
            house,
            anomaly
        )
    }.png"
}
