package coraythan.keyswap.cards

import coraythan.keyswap.cards.dokcards.DokCardRepo
import coraythan.keyswap.cards.dokcards.DokCardUpdateService
import coraythan.keyswap.cards.dokcards.toLegacyUrlFriendlyCardTitle
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.cards.extrainfo.ExtraCardInfoRepo
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeCard
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeDeckDto
import coraythan.keyswap.thirdpartyservices.mastervault.KeyforgeApi
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Transactional
@Service
class CardService(
    private val deckRepo: DeckRepo,
    private val cardRepo: CardRepo,
    private val extraCardInfoRepo: ExtraCardInfoRepo,
    private val versionService: CardsVersionService,
    private val keyforgeApi: KeyforgeApi,
    private val dokCardUpdateService: DokCardUpdateService,
    private val tokenService: TokenService,
    private val dokCardRepo: DokCardRepo
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refreshAllCardsForDeck(mvDeck: KeyForgeDeckDto) {
        if (mvDeck._linked.cards == null) throw BadRequestException("Must include card links in deck to refresh cards.")
        this.importNewCards(mvDeck._linked.cards)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun importNewCardsForDeck(mvDeck: KeyForgeDeckDto): Boolean {
        var updated = false
        if (!deckRepo.existsByKeyforgeId(mvDeck.data.id)) {
            val checkCards =
                (mvDeck.data.cards ?: mvDeck.data._links?.cards)
                    ?: error("Cards in the deck ${mvDeck.data.id} are null.")
            val cleanCards = checkCards.filter {
                // Skip stupid tide card
                it != "37377d67-2916-4d45-b193-bea6ecd853e3"
            }

            val newCardExists = cleanCards.any {
                val card = cardRepo.findByIdOrNull(it)
                card == null || !dokCardRepo.existsByCardTitleUrl(card.cardTitle.toLegacyUrlFriendlyCardTitle())
            }

            if (newCardExists) {
                val deckWithCards: KeyForgeDeckDto = if (mvDeck._linked.cards == null) {
                    keyforgeApi.findDeck(mvDeck.data.id, true)
                        ?: error("No deck for ${mvDeck.data.id} in KeyForge API")
                } else {
                    mvDeck
                }

                val updatedCard = this.importNewCards(deckWithCards._linked.cards!!)
                if (updatedCard) updated = true
            }
        }
        return updated
    }

    private fun importNewCards(keyforgeApiCards: List<KeyForgeCard>): Boolean {

        val cards = keyforgeApiCards.mapNotNull { it.toCard() }

        val savedMvCardNames = mutableListOf<String>()
        var tokenUpdated = false

        cards.forEach {
            if (it.token) {
                val updatedTokenId = tokenService.updateTokenCard(it)
                if (updatedTokenId != null) tokenUpdated = true
            }
            val fromCardRepo = cardRepo.findByIdOrNull(it.id)
            if (fromCardRepo == null) {
                savedMvCardNames.add(it.cardTitle)
                cardRepo.saveAndFlush(it)
            } else if (fromCardRepo.cardTitle != it.cardTitle || fromCardRepo.maverick != it.maverick || fromCardRepo.house != it.house) {
                cardRepo.deleteById(it.id)
                savedMvCardNames.add(it.cardTitle)
                cardRepo.saveAndFlush(it)
            }
        }
        log.info("Saved new MV Cards $savedMvCardNames")

        val updateResult = dokCardUpdateService.createDoKCardsFromKCards(cards)
        if (updateResult || tokenUpdated) {
            versionService.revVersion()
        }
        return updateResult || tokenUpdated
    }

    fun publishNextInfo(publishVersion: Int) {
        log.info("Publishing next extra info version $publishVersion")

        try {
            val currentInfo = mapInfos(extraCardInfoRepo.findByActiveTrue())

            val activeVersion = extraCardInfoRepo.findFirstByActiveTrueOrderByVersionDesc().version

            val infosToPublish = extraCardInfoRepo.findByPublishedNullAndVersionLessThanEqual(publishVersion)
            val toPublishCount = infosToPublish.size
            val toPublishVersions = infosToPublish.groupBy { it.version }
                .map { "Publishing ${it.value.size} card updates of version ${it.key}" }

            log.info("Highest active version $activeVersion new published version $publishVersion toPublish Count $toPublishCount publishing $toPublishVersions")
            if (toPublishCount > 0) {

                val toPublish = mapInfos(infosToPublish)
                val publishTime = ZonedDateTime.now()
                toPublish.forEach {
                    it.value.active = true
                    it.value.published = publishTime
                }
                val unpublish = toPublish.mapNotNull {
                    val oldInfo = currentInfo[it.key]
                    oldInfo?.active = false
                    oldInfo
                }

                val updated = toPublish.map { it.value }.plus(unpublish)
                extraCardInfoRepo.saveAllAndFlush(updated)
                versionService.revVersion()
                log.info(
                    "Publishing next extra info fully complete. Active aerc version $activeVersion published verison $publishVersion " +
                            "done publishing published " +
                            "${toPublish.size} unpublished ${unpublish.size}"
                )
            }

        } catch (exception: Exception) {
            log.error("Nothing is going to work because we couldn't publish extra info!", exception)
            throw IllegalStateException(exception)
        }
    }


    private fun mapInfos(extraInfos: List<ExtraCardInfo>) = extraInfos.associateBy { cardInfo ->
        cardInfo.synergies.size
        cardInfo.traits.size
        cardInfo.cardNameUrl
    }

    fun findByExpansionCardName(expansion: Int, cardName: String, enhanced: Boolean = false) =
        cardRepo.findByExpansionAndCardTitleAndEnhanced(expansion, cardName, enhanced).firstOrNull()

    fun findByCardName(cardName: String): Card {
        try {
            return cardRepo.findFirstByCardTitleAndMaverickFalse(cardName)
        } catch (exception: Exception) {
            // This is weird, but let's try with maverick true and switch the maverick. Some cards are only named one
            // way as mavericks.
            return cardRepo.findFirstByCardTitle(cardName)
                .copy(maverick = false)
        }
    }

}
