package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.House
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.synergy.*
import kotlin.math.roundToInt

object ProphecyValueAlgorithm {
    fun generateProphecyCombo(card: DokCardInDeck, cards: List<DokCardInDeck>): SynergyCombo {
        val fates = cards.mapNotNull {
            val fate = it.extraCardInfo.traits.firstOrNull { trait -> trait.trait == SynergyTrait.fate }
            if (fate == null) null else Pair(it, fate.strength())
        }
        val fateValue = fates.sumOf { fate ->
            // We are multiplying each fate by 2x the likelihood it is used as a fate. This makes 50/50 fate cards worth 1
            val usedAsFateLikelihood = 1 - traitStrengthToLikelihoodPlayed(fate.second)
            usedAsFateLikelihood * 2
        }

        val prophecyStrength = card.extraCardInfo.traits.firstOrNull { it.trait == SynergyTrait.prophecy }?.strength()
            ?: TraitStrength.NORMAL

        val maxEff = when (prophecyStrength) {
            TraitStrength.EXTRA_WEAK -> 0.25
            TraitStrength.WEAK -> 0.5
            TraitStrength.NORMAL -> 1.0
            TraitStrength.STRONG -> 2.0
            TraitStrength.EXTRA_STRONG -> 2.5
        }

        val fateModifier = 0.5 + (if (fateValue > 10.0) 1.0 else fateValue * 0.1)

        val efficiency = maxEff * fateModifier

        return SynergyCombo(
            house = House.Prophecy,
            cardName = card.card.cardTitle,
            synergies = listOf(
                SynergyMatch(
                    trait = SynTraitValue(SynergyTrait.fate),
                    percentSynergized = if (fateValue > 10) 100 else (fateValue * 10).roundToInt(),
                    traitCards = fates.map { fate -> fate.first.card.cardTitle }.toSet(),
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