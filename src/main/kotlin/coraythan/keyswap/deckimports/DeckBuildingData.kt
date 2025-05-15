package coraythan.keyswap.deckimports

import coraythan.keyswap.House
import coraythan.keyswap.decks.models.BonusIconHouse
import coraythan.keyswap.decks.models.BonusIconsCard
import coraythan.keyswap.decks.models.DeckBonusIcons
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.generatets.GenerateTs

data class DeckBuildingData(
    val cards: Map<House, List<TheoryCard>>,
    val name: String,
    val expansion: Expansion = Expansion.CALL_OF_THE_ARCHONS,
    val tokenTitle: String?,
    val alliance: Boolean = false,
) {
    val bonusIcons = DeckBonusIcons(
        cards
            .entries
            .map { houses ->
                BonusIconHouse(
                    house = houses.key,
                    bonusIconCards = houses.value
                        .map { theoryCard ->
                            BonusIconsCard(
                                cardTitle = theoryCard.name,
                                bonusAember = theoryCard.bonusAember,
                                bonusCapture = theoryCard.bonusCapture,
                                bonusDamage = theoryCard.bonusDamage,
                                bonusDraw = theoryCard.bonusDraw,
                                bonusDiscard = theoryCard.bonusDiscard,
                                bonusBobnar = theoryCard.bonusHouses.contains(House.Brobnar),
                                bonusDis = theoryCard.bonusHouses.contains(House.Dis),
                                bonusEkwidon = theoryCard.bonusHouses.contains(House.Ekwidon),
                                bonusGeistoid = theoryCard.bonusHouses.contains(House.Geistoid),
                                bonusLogos = theoryCard.bonusHouses.contains(House.Logos),
                                bonusMars = theoryCard.bonusHouses.contains(House.Mars),
                                bonusRedemption = theoryCard.bonusHouses.contains(House.Redemption),
                                bonusSkyborn = theoryCard.bonusHouses.contains(House.Skyborn),
                                bonusSanctum = theoryCard.bonusHouses.contains(House.Sanctum),
                                bonusSaurian = theoryCard.bonusHouses.contains(House.Saurian),
                                bonusShadows = theoryCard.bonusHouses.contains(House.Shadows),
                                bonusStarAlliance = theoryCard.bonusHouses.contains(House.StarAlliance),
                                bonusUntamed = theoryCard.bonusHouses.contains(House.Untamed),
                            )
                        }
                )
            })
}

@GenerateTs
data class TheoryCard(
    val name: String,
    val enhanced: Boolean = false,
    val bonusAember: Int = 0,
    val bonusCapture: Int = 0,
    val bonusDamage: Int = 0,
    val bonusDraw: Int = 0,
    val bonusDiscard: Int = 0,
    val bonusHouses: Set<House> = emptySet(),
)
