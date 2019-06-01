package coraythan.keyswap.synergy

import coraythan.keyswap.House

data class SynergyCombo(
        val house: House,
        val cardName: String,
        val synergies: Set<Synergies>,
        val antisynergies: Set<Synergies>,
        val netSynergy: Double,
        val synergy: Double,
        val antisynergy: Double,
        val cardRating: Double,

        val expectedAmber: Double,
        val amberControl: Double,
        val creatureControl: Double,
        val artifactControl: Double,
        val deckManipulation: Double,
        val effectivePower: Int,

        val copies: Int = 1
)

data class DeckSynergyInfo(
        val synergyRating: Double,
        val antisynergyRating: Double,
        val synergyCombos: List<SynergyCombo>
)
