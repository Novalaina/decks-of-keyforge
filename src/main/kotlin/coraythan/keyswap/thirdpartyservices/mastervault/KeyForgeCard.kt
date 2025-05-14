package coraythan.keyswap.thirdpartyservices.mastervault

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import coraythan.keyswap.House
import coraythan.keyswap.cards.Card
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.Rarity
import coraythan.keyswap.cards.evilTwinCardName

const val giganticCreatureTopCardType = "Gigantic Creature Art"
const val giganticCreatureBottomCardType = "Gigantic Creature Base"

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeyForgeCard(
    val id: String,
    val card_title: String,
    val house: String,
    val card_type: String,
    val front_image: String,
    val card_text: String,
    val amber: Int,
    val power: String?,
    val armor: String?,
    val rarity: String,
    val flavor_text: String? = null,
    val card_number: String,
    val expansion: Int,
    val is_maverick: Boolean,
    val is_anomaly: Boolean,
    val is_enhanced: Boolean,
    val is_non_deck: Boolean,
    val traits: String? = null
) {
    fun toCard(): Card? {
        val powerNumber = power?.toIntOrNull() ?: 0
        val armorNumber = armor?.toIntOrNull() ?: 0
        val realCardType = theirCardTypeToReasonableOne(card_type) ?: return null
        val realRarity = theirRarityToReasonableOne(rarity) ?: return null
        val cardTitleFixed = if (realRarity == Rarity.EvilTwin) {
            "$card_title$evilTwinCardName"
        } else {
            card_title
        }
        return Card(
            id,
            cardTitleFixed,
            House.fromMasterVaultValue(house)!!,
            realCardType,
            front_image,
            card_text,
            amber,
            powerNumber,
            power
                ?: "",
            armorNumber,
            armor ?: "",
            realRarity,
            flavor_text,
            card_number,
            expansion,
            is_maverick,
            is_anomaly,
            traits = traits?.uppercase()?.split(" • ")?.toSet() ?: setOf(),
            big = card_type == giganticCreatureTopCardType || card_type == giganticCreatureBottomCardType,
            enhanced = is_enhanced,
            token = realCardType == CardType.TokenCreature,
            extraCardInfo = null,
        )
    }

    private fun theirCardTypeToReasonableOne(dumbCardType: String): CardType? {
        return when (dumbCardType) {
            "Action" -> CardType.Action
            "Artifact" -> CardType.Artifact
            "Creature" -> CardType.Creature
            giganticCreatureTopCardType -> CardType.Creature
            giganticCreatureBottomCardType -> CardType.Creature
            "Upgrade" -> CardType.Upgrade
            "The Tide" -> null
            "Token Creature" -> CardType.TokenCreature
            "Evil Twin Creature" -> CardType.Creature
            "Prophecy" -> CardType.Prophecy
            else -> throw IllegalStateException("Weird card type $dumbCardType")
        }
    }

    private fun theirRarityToReasonableOne(dumbRarity: String): Rarity? {
        return when (dumbRarity) {
            "Common" -> Rarity.Common
            "Uncommon" -> Rarity.Uncommon
            "Rare" -> Rarity.Rare
            "Variant" -> Rarity.Variant
            "FIXED" -> Rarity.FIXED
            "Special" -> Rarity.Special
            "Evil Twin" -> Rarity.EvilTwin
            "Token" -> Rarity.Token
            "The Tide" -> null
            else -> throw IllegalStateException("Weird rarity $dumbRarity")
        }
    }
}