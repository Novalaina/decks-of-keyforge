package coraythan.keyswap.decks.models

import com.fasterxml.jackson.annotation.JsonInclude
import coraythan.keyswap.House
import coraythan.keyswap.cards.Rarity
import coraythan.keyswap.generatets.GenerateTs

@GenerateTs
data class HouseAndCards(
    val house: House,
    val cards: List<SimpleCard>,
)

fun List<HouseAndCards>.addBonusIcons(bonusIcons: DeckBonusIcons): List<HouseAndCards> {

    return this.map { houseAndCards ->
        val cardIcons = bonusIcons.bonusIconHouses
            .find { bonusIconHouse -> bonusIconHouse.house == houseAndCards.house }
            ?.bonusIconCards
            ?.toMutableList()

        if (cardIcons == null) {
            houseAndCards
        } else {
            val cards = houseAndCards.cards
                .map { card ->
                    val iconsForCardIdx = cardIcons.indexOfFirst { it.cardTitle == card.cardTitle && card.enhanced == true }
                    if (iconsForCardIdx == -1) {
                        card
                    } else {
                        val iconsForCard = cardIcons.removeAt(iconsForCardIdx)
                        card.copy(
                            bonusAember = iconsForCard.bonusAember,
                            bonusCapture = iconsForCard.bonusCapture,
                            bonusDamage = iconsForCard.bonusDamage,
                            bonusDraw = iconsForCard.bonusDraw,
                            bonusDiscard = iconsForCard.bonusDiscard,
                            bonusHouses = iconsForCard.bonusHouses(),
                        )
                    }
                }
            houseAndCards.copy(cards = cards)
        }
    }

}

@GenerateTs
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SimpleCard(
    val cardTitle: String,
    val cardTitleUrl: String,
    val rarity: Rarity? = null,
    val legacy: Boolean? = null,
    val maverick: Boolean? = null,
    val anomaly: Boolean? = null,
    val enhanced: Boolean? = null,
    val bonusAember: Int? = null,
    val bonusCapture: Int? = null,
    val bonusDamage: Int? = null,
    val bonusDraw: Int? = null,
    val bonusDiscard: Int? = null,
    val bonusHouses: Set<House>? = emptySet(),
)
