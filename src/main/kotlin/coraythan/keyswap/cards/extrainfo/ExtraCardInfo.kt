package coraythan.keyswap.cards.extrainfo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import coraythan.keyswap.cards.CardNumberSetPair
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.FrontendCard
import coraythan.keyswap.cards.dokcards.DokCard
import coraythan.keyswap.cards.dokcards.cardUrlFull
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.now
import coraythan.keyswap.roundToTwoSigDig
import coraythan.keyswap.synergy.SynTraitValue
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.*
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class CardNumberSetPairOld(
    val expansion: Int,
    val cardNumber: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val enhanced: Boolean? = null
) {
    fun toNew() = CardNumberSetPair(Expansion.forExpansionNumber(expansion), this.padded().cardNumber)
    private fun padded() = this.copy(cardNumber = cardNumber.padStart(3, '0'))
}

@GenerateTs
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
data class ExtraCardInfo(

    var cardName: String = "",
    val cardNameUrl: String = "",

    val expectedAmber: Double = 0.0,
    val expectedAmberMax: Double? = null,

    val amberControl: Double = 0.0,
    val amberControlMax: Double? = null,

    val creatureControl: Double = 0.0,
    val creatureControlMax: Double? = null,

    val artifactControl: Double = 0.0,
    val artifactControlMax: Double? = null,

    val efficiency: Double = 0.0,
    val efficiencyMax: Double? = null,

    val recursion: Double = 0.0,
    val recursionMax: Double? = null,

    val effectivePower: Int = 0,
    val effectivePowerMax: Double? = null,

    val disruption: Double = 0.0,
    val disruptionMax: Double? = null,

    val creatureProtection: Double = 0.0,
    val creatureProtectionMax: Double? = null,

    val other: Double = 0.0,
    val otherMax: Double? = null,

    val enhancementAmber: Int = 0,
    val enhancementCapture: Int = 0,
    val enhancementDraw: Int = 0,
    val enhancementDamage: Int = 0,
    val enhancementDiscard: Int = 0,

    /**
     * Changes the starting base match strength. For example, starts at -50 for Grumpy Buggy. This means you need
     * 5 matches of 10% value to break even at 0% and start synergizing. This can be used to modify the starting point
     * for synergies and antisynergies away from the default 0, 50, and 100.
     */
    val baseSynPercent: Int? = null,

    @Type(
        value = ListArrayType::class,
        parameters = [Parameter(
            value = "card_type",
            name = ListArrayType.SQL_ARRAY_TYPE,
        )]
    )
    @Column(columnDefinition = "card_type[]")
    val extraCardTypes: List<CardType>? = null,

    @JsonIgnoreProperties("traitInfo")
    @OneToMany(mappedBy = "traitInfo", cascade = [CascadeType.ALL])
    val traits: List<SynTraitValue> = listOf(),

    @JsonIgnoreProperties("synergyInfo")
    @OneToMany(mappedBy = "synergyInfo", cascade = [CascadeType.ALL])
    val synergies: List<SynTraitValue> = listOf(),

    val version: Int = 1,
    var active: Boolean = false,
    val created: ZonedDateTime? = now(),
    val updated: ZonedDateTime? = now(),
    var published: ZonedDateTime? = null,

    @JsonIgnore
    @ManyToOne
    val dokCard: DokCard = DokCard(),

    @Id
    val id: UUID = UUID.randomUUID()
) {

    val realEffectivePower: Int
        get() = if (effectivePower != 0) {
            effectivePower
        } else {
            dokCard.power + dokCard.armor
        }

    fun allCardTypes(): Set<CardType> =
        if (this.extraCardTypes == null) setOf(dokCard.cardType) else setOf(dokCard.cardType).plus(this.extraCardTypes)

    val publishedDate: LocalDate?
        get() = published?.toLocalDate()

    fun readyForCreate(version: Int): ExtraCardInfo {
        now().toString()
        return this.copy(
            created = now(),
            updated = now(),
            version = version,
            active = false,
            synergies = mutableListOf(),
            traits = mutableListOf(),
            id = UUID.randomUUID(),
            published = null,
        )
    }

    fun nullMaxes() = this.copy(
        amberControlMax = if (this.amberControlMax == 0.0) null else this.amberControlMax,
        expectedAmberMax = if (this.expectedAmberMax == 0.0) null else this.expectedAmberMax,
        creatureProtectionMax = if (this.creatureProtectionMax == 0.0) null else this.creatureProtectionMax,
        artifactControlMax = if (this.artifactControlMax == 0.0) null else this.artifactControlMax,
        creatureControlMax = if (this.creatureControlMax == 0.0) null else this.creatureControlMax,
        effectivePowerMax = if (this.effectivePowerMax == 0.0) null else this.effectivePowerMax,
        efficiencyMax = if (this.efficiencyMax == 0.0) null else this.efficiencyMax,
        recursionMax = if (this.recursionMax == 0.0) null else this.recursionMax,
        disruptionMax = if (this.disruptionMax == 0.0) null else this.disruptionMax,
        otherMax = if (this.otherMax == 0.0) null else this.otherMax
    )

    fun replaceAercInfo(info: ExtraCardInfo): ExtraCardInfo {
        return this.copy(
            updated = now(),
            expectedAmber = info.expectedAmber,
            expectedAmberMax = info.expectedAmberMax,
            amberControl = info.amberControl,
            amberControlMax = info.amberControlMax,
            creatureControl = info.creatureControl,
            creatureControlMax = info.creatureControlMax,
            artifactControl = info.artifactControl,
            artifactControlMax = info.artifactControlMax,
            efficiency = info.efficiency,
            efficiencyMax = info.efficiencyMax,
            recursion = info.recursion,
            recursionMax = info.recursionMax,
            effectivePower = info.effectivePower,
            effectivePowerMax = info.effectivePowerMax,
            disruption = info.disruption,
            disruptionMax = info.disruptionMax,
            creatureProtection = info.creatureProtection,
            creatureProtectionMax = info.creatureProtectionMax,
            other = info.other,
            otherMax = info.otherMax,
            enhancementAmber = info.enhancementAmber,
            enhancementCapture = info.enhancementCapture,
            enhancementDamage = info.enhancementDamage,
            enhancementDraw = info.enhancementDraw,
            enhancementDiscard = info.enhancementDiscard,
            baseSynPercent = info.baseSynPercent,
            traits = mutableListOf(),
            synergies = mutableListOf()
        )
    }

    fun printValues() = listOfNotNull(
        printValue("AERC", aercScore, aercScoreMax),
        printValue("A", amberControl, amberControlMax),
        printValue("E", expectedAmber, expectedAmberMax),
        printValue("R", artifactControl, artifactControlMax),
        printValue("C", creatureControl, creatureControlMax),
        printValue(
            "P",
            this.realEffectivePower.toDouble() / 10,
            if (effectivePowerMax != null && effectivePowerMax != 0.0) effectivePowerMax / 10 else null
        ),
        printValue("F", efficiency, efficiencyMax),
        printValue("U", recursion, recursionMax),
        printValue("D", disruption, disruptionMax),
        printValue("CP", creatureProtection, creatureProtectionMax),
        printValue("O", other, otherMax)
    )
        .joinToString(" • ") +
            printTraits("Traits", traits) +
            printTraits("Syns", synergies)

    val aercScoreAverage: Double
        get() {
            val max = aercScoreMax
            return if (max == null) aercScore else (aercScore + max) / 2
        }

    val aercScore: Double
        get() = amberControl +
                expectedAmber +
                artifactControl +
                creatureControl +
                efficiency +
                recursion +
                disruption +
                creatureProtection +
                other +
                this.effectivePowerAercScore +
                this.dokCard.cardType.creatureBonus()

    val aercScoreMax: Double?
        get() {
            val maxAerc = (amberControlMax ?: amberControl) +
                    (artifactControlMax ?: artifactControl) +
                    (creatureControlMax ?: creatureControl) +
                    (efficiencyMax ?: efficiency) +
                    (recursionMax ?: recursion) +
                    (disruptionMax ?: disruption) +
                    (creatureProtectionMax ?: creatureProtection) +
                    (otherMax ?: other) +
                    (expectedAmberMax ?: expectedAmber) +
                    (if (effectivePowerMax == null) {
                        this.effectivePowerAercScore
                    } else {
                        effectivePowerMax / 10
                    }) +
                    this.dokCard.cardType.creatureBonus()

            if (maxAerc == this.aercScore) {
                return null
            } else {
                return maxAerc
            }
        }

    val effectivePowerAercScore: Double
        get() = this.effectivePower.toDouble() / 10.0

    private fun printValue(name: String, min: Double, max: Double?) = if (min == 0.0 && (max == 0.0 || max == null)) {
        null
    } else {
        "$name: ${min.roundToTwoSigDig()}${if (max == null || max == 0.0) "" else " to ${max.roundToTwoSigDig()}"}"
    }

    private fun printTraits(name: String, traits: List<SynTraitValue>) = if (traits.isEmpty()) {
        ""
    } else {
        "\n$name: ${traits.joinToString(" • ") { it.print() }}"
    }

    fun toCardForFrontend(): FrontendCard {
        val card = dokCard
        val firstHouse = card.houses.firstOrNull()
        return FrontendCard(
            id = card.id,
            houses = card.houses,
            cardTitle = card.cardTitle,
            cardTitleUrl = cardUrlFull(card.cardTitle, firstHouse, firstHouse == null),
            cardType = card.cardType,
            cardText = card.cardText ?: "",
            traits = card.traits,
            amber = card.amber,
            power = card.power,
            armor = card.armor,
            flavorText = card.flavorText,
            big = card.big,
            token = card.token,
            wins = card.expansions.sumOf { it.wins },
            losses = card.expansions.sumOf { it.losses },
            extraCardInfo = this,
            expansions = card.expansions,
            cardNumbers = card.expansions.map { CardNumberSetPair(it.expansion, it.cardNumber) }
        )
    }

}
