package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.synergy.SynergyTrait
import coraythan.keyswap.synergy.TraitStrength

object GenerateDeckAndHouseTraits {

    fun addDeckTraits(
        traits: MutableMap<SynergyTrait, MatchSynergiesToTraits>,
        cards: List<DokCardInDeck>
    ) {

        // TODO Remove / move these and figure out why I set up bonus aember / capture twice *sigh*
        val bonusAmber = cards.sumOf { it.totalAmber }
        val bonusCapture = cards.sumOf { it.bonusCapture }
        val bonusDraw = cards.sumOf { it.bonusDraw }
        val bonusDamage = cards.sumOf { it.bonusDamage }
        val bonusDiscard = cards.sumOf { it.bonusDiscard }
        val bonusPower = cards.sumOf { it.totalPower }

        traits.addDeckTrait(SynergyTrait.bonusAmber, bonusAmber, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusCapture, bonusCapture, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusDraw, bonusDraw, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusDiscard, bonusDiscard, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusDamage, bonusDamage, strength = TraitStrength.EXTRA_WEAK)
        traits.addDeckTrait(SynergyTrait.bonusPower, bonusPower, strength = TraitStrength.EXTRA_WEAK)
    }

}