package coraythan.keyswap.alliancedecks

import coraythan.keyswap.House
import coraythan.keyswap.generatets.GenerateTs

@GenerateTs
data class AllianceDeckHouses(
    val houseOneDeckId: String,
    val houseOne: House,
    val houseTwoDeckId: String,
    val houseTwo: House,
    val houseThreeDeckId: String,
    val houseThree: House,
    val owned: Boolean,
    val tokenName: String?,
    val propheciesDeckId: String?,
)
