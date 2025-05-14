package coraythan.keyswap.synergies

import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.Rarity
import coraythan.keyswap.cards.dokcards.DokCard
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.decks.models.Deck

fun testCard(
    name: String = "tremor",
    cardType: CardType = CardType.Action,
    house: House = House.Brobnar,
    amber: Int? = null,
    power: Int? = null,
    bonusMars: Boolean = false,
    traits: Set<String> = setOf(),
    extraCardInfo: ExtraCardInfo = ExtraCardInfo(),
): DokCardInDeck {
    val dokCard = DokCard(
        cardTitle = name,
        cardTitleUrl = name,
        cardType = cardType,
        amber = amber ?: 0,
        power = power ?: 0,
        houses = if (bonusMars) listOf(house, House.Mars) else listOf(house),
        traits = traits.toList(),
    )
    return DokCardInDeck(
        card = dokCard,
        extraCardInfo = extraCardInfo.copy(
            dokCard = dokCard,
            expectedAmber = amber?.toDouble() ?: extraCardInfo.expectedAmber,
            effectivePower = power ?: extraCardInfo.effectivePower,
        ),
        house = house,
        rarity = Rarity.Common,
    )
}

val boringDeck = Deck(
    keyforgeId = "",
    expansion = 341,
    name = "boring",
    houseNamesString = "Brobnar|Sanctum|Untamed"
)

val worldsCollideDeck = Deck(
    keyforgeId = "",
    expansion = 341,
    name = "boring",
    houseNamesString = "Brobnar|Sanctum|StarAlliance"
)

val aemberSkiesDeck = Deck(
    keyforgeId = "",
    expansion = 800,
    name = "Aember Skies",
    houseNamesString = "Ekwidon|Mars|Skyborn"
)
