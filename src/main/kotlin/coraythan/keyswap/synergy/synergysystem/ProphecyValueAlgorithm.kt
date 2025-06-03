package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.House
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.synergy.*

object ProphecyValueAlgorithm {
    fun generateProphecyCombo(card: DokCardInDeck, cards: List<DokCardInDeck>): SynergyCombo {
        val fates = cards.filter { it.extraCardInfo.traits.any { trait -> trait.trait == SynergyTrait.fate } }
        val fateCount = fates.count()
        val prophecyStrength = card.extraCardInfo.traits.firstOrNull { it.trait == SynergyTrait.prophecy }?.strength()
            ?: TraitStrength.NORMAL

        val maxEff = when (prophecyStrength) {
            TraitStrength.EXTRA_WEAK -> 0.5
            TraitStrength.WEAK -> 0.75
            TraitStrength.NORMAL -> 1.0
            TraitStrength.STRONG -> 1.25
            TraitStrength.EXTRA_STRONG -> 1.5
        }

        val fateModifier = 0.5 + (if (fateCount > 10) 10.0 else fateCount * 0.05)

        val efficiency = maxEff * fateModifier

        return SynergyCombo(
            house = House.Prophecy,
            cardName = card.card.cardTitle,
            synergies = listOf(
                SynergyMatch(
                    trait = SynTraitValue(SynergyTrait.fate),
                    percentSynergized = if (fateCount > 10) 100 else fateCount * 10,
                    traitCards = fates.map { fate -> fate.card.cardTitle }.toSet(),
                )
            ),
            netSynergy = efficiency,
            aercScore = efficiency,

            amberControl = 0.0,
            expectedAmber = 0.0,
            artifactControl = 0.0,
            creatureControl = 0.0,
            efficiency = efficiency,
            recursion = 0.0,
            effectivePower = 0,

            disruption = 0.0,
            creatureProtection = 0.0,
            other = 0.0,
            copies = 1,

            notCard = true,
        )
    }
}