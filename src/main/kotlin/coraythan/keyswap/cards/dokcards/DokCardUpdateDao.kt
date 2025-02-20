package coraythan.keyswap.cards.dokcards

import coraythan.keyswap.cards.CardRepo
import coraythan.keyswap.cards.CardsVersionService
import coraythan.keyswap.cards.cardwins.CardWinsService
import coraythan.keyswap.cards.extrainfo.ExtraCardInfoService
import coraythan.keyswap.expansions.Expansion
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class DokCardUpdateDao(
    private val dokCardRepo: DokCardRepo,
    private val extraCardInfoService: ExtraCardInfoService,
    private val dokCardExpansionRepo: DokCardExpansionRepo,
    private val cardWinsService: CardWinsService,
    private val cardCache: DokCardCacheService,
    private val cardRepo: CardRepo,
    private val versionService: CardsVersionService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private var updatedCards = false

    @Scheduled(
        fixedDelayString = "PT5M",
        initialDelayString = "PT10M"
    )
    fun reloadCards() {
        if (updatedCards) {
            log.info("A card got updated so reload from cache in case proactive caching worked badly")
            cardCache.loadCards()
            versionService.revVersion()
        }
    }

    fun saveDokCard(cardId: String) {
        val card = cardRepo.findByIdOrNull(cardId) ?: throw IllegalStateException("No card for $cardId")
        cardWinsService.addWinsToCards(listOf(card))

        val cardExpansion = Expansion.forExpansionNumber(card.expansion)
        val dokCard = card.toDoKCard()
        val savedDokCard = dokCardRepo.saveAndFlush(dokCard)
        val cardNumber = card.dokCardNumber()
        var dokCardExpansion = dokCardExpansionRepo.findByExpansionAndCardNumber(cardExpansion, cardNumber)
        if (dokCardExpansion == null) {
            dokCardExpansion = DokCardExpansion(
                cardNumber = cardNumber,
                expansion = cardExpansion,
                rarity = card.rarity,
                wins = card.expansionWins?.get(cardExpansion)?.wins ?: 0,
                losses = card.expansionWins?.get(cardExpansion)?.losses ?: 0,
                card = savedDokCard,
            )
            log.info("Save dok card expansion $dokCardExpansion for card ${card.cardTitle}")
            dokCardExpansionRepo.saveAndFlush(dokCardExpansion)
        }
        val extraInfo = extraCardInfoService.saveNewExtraCardInfo(savedDokCard)

        cardCache.updateCache(cardExpansion, cardNumber, extraInfo)
        updatedCards = true

        this.log.info("Creating Card: ${card.cardTitle} -- ${dokCardExpansion.expansion}-${dokCardExpansion.cardNumber}-${if (card.maverick) "mav" else if (card.anomaly) "anom" else savedDokCard.houses.toString()}")
    }

    fun updateDokCard(cardId: String): Boolean {
        var updated = false
        val card = cardRepo.findByIdOrNull(cardId) ?: throw IllegalStateException("No card for $cardId")
        cardWinsService.addWinsToCards(listOf(card))
        val cardNameUrl = card.cardTitle.toLegacyUrlFriendlyCardTitle()

        val existingCard = dokCardRepo.findByCardTitleUrl(cardNameUrl)
            ?: throw IllegalStateException("Updating Card: No DoK card for ${card.cardTitle}")
        val cardExpansion = Expansion.forExpansionNumber(card.expansion)
        val existingExpansions = existingCard.expansions

        val cardNumber = card.dokCardNumber()
        val preexistingExpansion = dokCardExpansionRepo.findByExpansionAndCardNumber(cardExpansion, cardNumber)

        val extraInfo = cardCache.findByCardNameUrl(cardNameUrl)
        var updatedDokCard = existingCard
        if (preexistingExpansion == null) {
            val newExpansion = DokCardExpansion(
                cardNumber = cardNumber,
                expansion = Expansion.forExpansionNumber(card.expansion),
                rarity = card.rarity,
                wins = card.expansionWins?.get(cardExpansion)?.wins ?: 0,
                losses = card.expansionWins?.get(cardExpansion)?.losses ?: 0,
                card = existingCard,
            )

            this.log.info("Updating Card: ${card.cardTitle} Adding expansion: ${newExpansion.expansion}-${newExpansion.cardNumber} existing expansions: $existingExpansions")
            val savedExpansion = dokCardExpansionRepo.saveAndFlush(newExpansion)

            updated = true
            updatedDokCard = extraInfo.dokCard.copy(
                expansions = extraInfo.dokCard.expansions.plus(savedExpansion)
            )
        } else if (preexistingExpansion.card.cardTitleUrl != cardNameUrl) {
            log.warn(
                "We've got a really weird card going on. The expansion number ${preexistingExpansion.cardNumber} ${preexistingExpansion.expansion} " +
                        "is associated with card ${preexistingExpansion.card.cardTitleUrl} instead of with $cardNameUrl"
            )
            val updatedExpansion = dokCardExpansionRepo.save(preexistingExpansion.copy(card = updatedDokCard))
            updated = true
            updatedDokCard = extraInfo.dokCard.copy(
                expansions = extraInfo.dokCard.expansions.plus(updatedExpansion)
            )
        }
        if (!card.maverick && !card.anomaly && !existingCard.houses.contains(card.house)) {
            this.log.info("Updating Card: ${card.cardTitle} Adding house: ${card.house}")
            dokCardRepo.saveAndFlush(
                existingCard.copy(
                    houses = existingCard.houses.toSet().plus(card.house).toList()
                )
            )

            updated = true
            updatedDokCard = updatedDokCard.copy(houses = existingCard.houses.toSet().plus(card.house).toList())
        }
        if (existingCard.cardTitleUrl != cardNameUrl) {
            log.warn("We've got a really weird card going on. Existing card ${existingCard.cardTitleUrl} is not the same as $cardNameUrl")
            updatedDokCard = dokCardRepo.save(card.toDoKCard(updatedDokCard))
            updated = true
        }
        if (updated) {
            cardCache.updateCache(cardExpansion, cardNumber, extraInfo.copy(dokCard = updatedDokCard))
            updatedCards = true
        }
        return updated
    }
}
