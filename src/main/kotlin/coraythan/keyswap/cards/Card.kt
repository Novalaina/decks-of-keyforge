package coraythan.keyswap.cards

import coraythan.keyswap.House
import coraythan.keyswap.cards.dokcards.DokCard
import coraythan.keyswap.cards.dokcards.toLegacyUrlFriendlyCardTitle
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.decks.Wins
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.now
import coraythan.keyswap.synergy.synergysystem.StaticAercValues
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
data class Card(
    @Id
    val id: String,
    val cardTitle: String,
    @Enumerated(EnumType.STRING)
    val house: House,
    @Enumerated(EnumType.STRING)
    val cardType: CardType,
    val frontImage: String,
    val cardText: String,
    val amber: Int,
    val power: Int,
    val powerString: String,
    val armor: Int,
    val armorString: String,
    @Enumerated(EnumType.STRING)
    val rarity: Rarity,
    val flavorText: String? = null,
    val cardNumber: String,
    val expansion: Int,
    val maverick: Boolean,
    val anomaly: Boolean,
    val enhanced: Boolean? = null,
    val big: Boolean? = null,
    val token: Boolean = false,

    val wins: Int? = 0,
    val losses: Int? = 0,

    val created: ZonedDateTime = now(),

    @ElementCollection
    val traits: Set<String> = setOf(),

    @Transient
    var extraCardInfo: ExtraCardInfo?,

    @Transient
    var cardNumbers: List<CardNumberSetPair>? = null,

    @Transient
    var houses: List<House>? = null,

    @Transient
    var expansionWins: Map<Expansion, Wins>? = null
) : Comparable<Card> {

    override fun compareTo(other: Card): Int {
        if (house != other.house) return house.compareTo(other.house)
        if (cardType != other.cardType) return cardType.compareTo(other.cardType)
        return cardTitle.compareTo(other.cardTitle)
    }

    fun isEvilTwin() = this.cardTitle.contains(evilTwinCardName)

    fun dokCardNumber() = if (expansion == Expansion.MENAGERIE_2024.expansionNumber && cardTitle.toLegacyUrlFriendlyCardTitle() == "its-coming") {
        // Fix stupid problem with veylan analyst and its coming having same card number in menagerie
        "999"
    } else {
        cardNumber
    }

    fun toDoKCard(copyInto: DokCard? = null) = copyInto?.copy(
        cardTitle = this.cardTitle,
        cardTitleUrl = this.cardTitle.toLegacyUrlFriendlyCardTitle(),
        houses = if (this.maverick || this.anomaly) listOf() else listOf(this.house),
        cardType = this.cardType,
        amber = this.amber,
        power = this.power,
        armor = this.armor,
        big = this.big == true,
        token = this.token,
        evilTwin = this.isEvilTwin(),
        cardText = this.cardText,
        flavorText = this.flavorText,
        traits = this.traits.toList(),
    )
        ?: DokCard(
            cardTitle = this.cardTitle,
            cardTitleUrl = this.cardTitle.toLegacyUrlFriendlyCardTitle(),
            houses = if (this.maverick || this.anomaly) listOf() else listOf(this.house),
            cardType = this.cardType,
            amber = this.amber,
            power = this.power,
            armor = this.armor,
            big = this.big == true,
            token = this.token,
            evilTwin = this.isEvilTwin(),
            cardText = this.cardText,
            flavorText = this.flavorText,
            traits = this.traits.toList(),
        )

}

const val evilTwinCardName = " – Evil Twin"

@GenerateTs
enum class CardType {
    Action,
    Artifact,
    Creature,
    Upgrade,
    TokenCreature,
    Prophecy;

    fun creatureBonus() = if (this == TokenCreature || this == Creature) StaticAercValues.creatureBonus else 0.0
}

@GenerateTs
enum class Rarity {
    Common,
    Uncommon,
    Rare,
    Variant,
    FIXED,
    Special,
    EvilTwin,
    Token;
}
