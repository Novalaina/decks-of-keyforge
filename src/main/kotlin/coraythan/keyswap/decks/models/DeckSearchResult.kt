package coraythan.keyswap.decks.models

import com.fasterxml.jackson.annotation.JsonInclude
import coraythan.keyswap.House
import coraythan.keyswap.auctions.DeckListing
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.generatets.GenerateTs
import coraythan.keyswap.roundToOneSigDig
import coraythan.keyswap.synergy.SynergyCombo
import coraythan.keyswap.synergy.TokenCreationValues
import java.time.LocalDate

// It takes a long time to load all the crap in hibernate, so avoid that.
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeckSearchResult(
    val deckType: DeckType,
    val id: Long = -1,
    val keyforgeId: String = "",
    val expansion: Expansion,

    val name: String = "",

    val creatureCount: Int? = null,
    val actionCount: Int? = null,
    val artifactCount: Int? = null,
    val upgradeCount: Int? = null,

    val powerLevel: Int? = null,
    val chains: Int? = null,
    val wins: Int? = null,
    val losses: Int? = null,

    val expectedAmber: Double = 0.0,
    val amberControl: Double = 0.0,
    val creatureControl: Double = 0.0,
    val artifactControl: Double? = null,
    val efficiency: Double? = null,
    val recursion: Double? = null,
    val effectivePower: Int = 0,
    val creatureProtection: Double? = null,
    val disruption: Double? = null,
    val other: Double? = null,

    val aercScore: Int = 0,
    val previousSasRating: Int = 0,
    val previousMajorSasRating: Int? = null,
    val aercVersion: Int = 12,
    val sasRating: Int = 0,
    val synergyRating: Int = 0,
    val antisynergyRating: Int = 0,
    val efficiencyBonus: Double = 0.0,

    val totalPower: Int = 0,
    val cardDrawCount: Int? = null,
    val cardArchiveCount: Int? = null,
    val keyCheatCount: Int? = null,
    val rawAmber: Int = 0,
    val totalArmor: Int? = null,

    val forSale: Boolean? = null,
    val forTrade: Boolean? = null,
    val wishlistCount: Int? = null,
    val funnyCount: Int? = null,

    val lastSasUpdate: String? = null,
    val sasPercentile: Double = 0.0,

    val hasOwnershipVerification: Boolean? = null,

    val housesAndCards: List<HouseAndCards> = listOf(),
    val deckSaleInfo: List<DeckSaleInfo>? = null,
    val owners: List<String>? = null,
    val synergyDetails: List<SynergyCombo>? = null,
    val tokenCreationValues: TokenCreationValues? = null,
    val dateAdded: LocalDate? = null,
    val twinId: String? = null,
    val tokenInfo: TokenInfo? = null,
    val prophecies: List<SimpleCard>? = null,
    val hauntingOdds: Int? = null,

    val allianceHouses: List<AllianceHouseInfo>? = null,
    val validAlliance: Boolean? = null,
    val discoveredBy: String? = null,
) {

    fun printDeckSimple(saleInfo: DeckListing? = null): String {

        val buyItNow = saleInfo?.buyItNow
        val highOffer = saleInfo?.highestOffer
        val currencySymbol = saleInfo?.currencySymbol
        val buyItNowMessage = if (buyItNow == null) "" else ", BIN: $currencySymbol$buyItNow"

        val forSaleMessage = when {
            forSale == true -> " • For Sale${if (highOffer == null) "" else ", high offer:  $currencySymbol$highOffer"}$buyItNowMessage"
            else -> ""
        }

        val tokenName = if (this.tokenInfo == null) "" else " • ${this.tokenInfo.name}"

        return "$sasRating SAS • ${expansion.readable} • ${
            housesAndCards.map { it.house }.joinToString(" – ") { it.masterVaultValue }
        }" + tokenName + forSaleMessage
    }

    fun printDeck(saleInfo: DeckListing? = null): String {
        return """
            ${printDeckSimple(saleInfo)}
            ${amberControl.roundToOneSigDig()} A • ${expectedAmber.roundToOneSigDig()} E • ${(artifactControl ?: 0.0).roundToOneSigDig()} R • ${creatureControl.roundToOneSigDig()} C
            ${(efficiency ?: 0.0).roundToOneSigDig()} F • ${(recursion ?: 0.0).roundToOneSigDig()} U • ${(disruption ?: 0.0).roundToOneSigDig()} D
        """.trimIndent()
    }

    fun toSimpleResult() = SimpleDeckSearchResult(
        name,
        keyforgeId,
        housesAndCards.filter { it.house == House.Prophecy }.map { it.house },
        sasRating
    )
}

@GenerateTs
data class SimpleDeckSearchResult(
    val name: String,
    val keyforgeId: String,
    val houses: List<House> = listOf(),
    val sas: Int = 0,
)

@GenerateTs
data class AllianceHouseInfo(
    val keyforgeId: String,
    val name: String,
    val house: House,
)

@GenerateTs
data class TokenInfo(
    val name: String,
    val nameUrl: String,
    val house: House,
)
