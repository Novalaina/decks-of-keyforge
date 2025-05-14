package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.synergy.SynergyCombo

object HouseEnhancementAlgorithm {
    fun generateHouseEnhancementCombos(cards: List<DokCardInDeck>): List<SynergyCombo> {
        return cards
            .mapNotNull { cardWithIcons ->
                if (cardWithIcons.bonusHouses.isNotEmpty()) {
                    val card = cardWithIcons.card

                    val ep = cardWithIcons.extraCardInfo.realEffectivePower

                    val amberMod = when {
                        card.cardType != CardType.Creature -> 0.0
                        ep < 2 -> 0.25
                        ep < 5 -> 0.5
                        else -> 0.75
                    }

                    val creatureControlMod = when {
                        card.cardType != CardType.Creature -> 0.0
                        ep > 8 -> 0.5
                        ep > 5 -> 0.25
                        else -> 0.0
                    }

                    val effMod = 0.5

                    val total = amberMod + creatureControlMod + effMod

                    SynergyCombo(
                        house = cardWithIcons.house,
                        cardName = card.cardTitle + " House Enhanced",
                        synergies = listOf(),
                        netSynergy = total,
                        aercScore = total,

                        amberControl = 0.0,
                        expectedAmber = amberMod,
                        artifactControl = 0.0,
                        creatureControl = creatureControlMod,
                        efficiency = effMod,
                        recursion = 0.0,
                        effectivePower = 0,

                        disruption = 0.0,
                        creatureProtection = 0.0,
                        other = 0.0,
                        copies = 1,

                        notCard = true,
                    )
                } else {
                    null
                }
            }
    }
}