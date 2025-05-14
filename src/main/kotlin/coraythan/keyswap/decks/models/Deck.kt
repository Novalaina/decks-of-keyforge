package coraythan.keyswap.decks.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import coraythan.keyswap.*
import coraythan.keyswap.auctions.DeckListing
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.cards.dokcards.toLegacyUrlFriendlyCardTitle
import coraythan.keyswap.keyforgeevents.tournaments.TournamentDeck
import coraythan.keyswap.stats.DeckStatistics
import coraythan.keyswap.synergy.DeckSynergyInfo
import coraythan.keyswap.synergy.SynTraitPlayer
import coraythan.keyswap.synergy.SynergyTrait
import coraythan.keyswap.synergy.containsTrait
import coraythan.keyswap.tags.DeckTag
import coraythan.keyswap.thirdpartyservices.mastervault.KeyForgeDeck
import coraythan.keyswap.userdeck.*
import jakarta.persistence.*
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Entity
data class Deck(

    @Column(unique = true)
    val keyforgeId: String,

    override val name: String,
    override val expansion: Int,
    val powerLevel: Int = 0,
    val chains: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,

    val previousSasRating: Int? = 0,
    val previousMajorSasRating: Int? = 0,

    val forSale: Boolean = false,
    val forTrade: Boolean = false,
    val forAuction: Boolean = false,
    val completedAuction: Boolean = false,
    val wishlistCount: Int = 0,
    val funnyCount: Int = 0,
    val evilTwin: Boolean? = false,

    override val cardIds: String = "",
    override val tokenNumber: Int? = null,

    override val houseNamesString: String = "",

    override val bonusIconsString: String? = null,

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val deckNotes: List<DeckNote> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val tournamentDecks: List<TournamentDeck> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val ownedDecks: List<OwnedDeck> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val funnyDecks: List<FunnyDeck> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val favoritedDecks: List<FavoritedDeck> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val previouslyOwnedDecks: List<PreviouslyOwnedDeck> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val tags: List<DeckTag> = listOf(),

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY)
    val auctions: List<DeckListing> = listOf(),

    val hasOwnershipVerification: Boolean? = false,

    val listedOn: ZonedDateTime? = null,
    val auctionEnd: ZonedDateTime? = null,
    val auctionEndedOn: ZonedDateTime? = null,

    val importDateTime: ZonedDateTime? = now(),

    /**
     * Last SAS update
     */
    val lastUpdate: ZonedDateTime? = now(),

    val twinId: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "hibernate_sequence")
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
    val id: Long = -1
) : GenericDeck {

    val dateAdded: LocalDate?
        get() = this.importDateTime?.toLocalDate()

    fun withBonusIcons(icons: DeckBonusIcons): Deck {
        return this
            .copy(bonusIconsString = jacksonObjectMapper().writeValueAsString(icons))
    }

    override fun toDeckSearchResult(
        housesAndCards: List<HouseAndCards>?,
        cards: List<DokCardInDeck>?,
        stats: DeckStatistics?,
        synergies: DeckSynergyInfo?,
        includeDetails: Boolean,
        token: DokCardInDeck?,
    ): DeckSearchResult {

        return DeckSearchResult(
            deckType = DeckType.STANDARD,
            id = id,
            keyforgeId = keyforgeId,
            expansion = expansionEnum,
            name = name,

            powerLevel = powerLevel.zeroToNull(),
            chains = chains.zeroToNull(),
            wins = wins.zeroToNull(),
            losses = losses.zeroToNull(),

            creatureCount = cards?.sumOf {
                if (it.card.cardType == CardType.Creature) {
                    if (it.card.big) {
                        0.5
                    } else {
                        1.0
                    }
                } else {
                    0.0
                }
            }?.roundToInt(),
            actionCount = cards?.count { it.card.cardType == CardType.Action },
            artifactCount = cards?.count { it.card.cardType == CardType.Artifact },
            upgradeCount = cards?.count { it.card.cardType == CardType.Upgrade },

            cardDrawCount = (cards?.filter {
                it.extraCardInfo.traits.containsTrait(SynergyTrait.drawsCards) || it.extraCardInfo.traits.containsTrait(
                    SynergyTrait.increasesHandSize
                )
            }?.size ?: 0).zeroToNull(),
            cardArchiveCount = (cards?.filter {
                it.extraCardInfo.traits.containsTrait(
                    SynergyTrait.archives,
                    player = SynTraitPlayer.FRIENDLY
                )
            }?.size
                ?: 0).zeroToNull(),
            keyCheatCount = (cards?.filter {
                it.extraCardInfo.traits.containsTrait(SynergyTrait.forgesKeys)
                        || it.extraCardInfo.traits.containsTrait(SynergyTrait.forgesKeysWithoutAember)
            }?.size ?: 0).zeroToNull(),
            rawAmber = if (cards == null) -1 else cards.sumOf { it.card.amber } + this.bonusIcons().bonusIconHouses.sumOf { it.bonusIconCards.sumOf { it.bonusAember } },
            totalArmor = cards?.sumOf { it.card.armor },
            totalPower = cards?.sumOf { it.card.power } ?: -1,

            expectedAmber = synergies?.expectedAmber ?: -1.0,
            amberControl = synergies?.amberControl ?: -1.0,
            creatureControl = synergies?.creatureControl ?: -1.0,
            artifactControl = (synergies?.artifactControl ?: -1.0).zeroToNull(),
            efficiency = (synergies?.efficiency ?: -1.0).zeroToNull(),
            recursion = (synergies?.recursion ?: -1.0).zeroToNull(),
            effectivePower = synergies?.effectivePower ?: -1,
            creatureProtection = (synergies?.creatureProtection ?: -1.0).zeroToNull(),
            disruption = (synergies?.disruption ?: -1.0).zeroToNull(),
            other = (synergies?.other ?: -1.0).zeroToNull(),
            aercScore = synergies?.rawAerc ?: -1,
            previousSasRating = previousSasRating ?: -1,
            previousMajorSasRating = previousMajorSasRating,
            aercVersion = -1,
            sasRating = synergies?.sasRating ?: -1,
            synergyRating = synergies?.synergyRating ?: -1,
            antisynergyRating = synergies?.antisynergyRating ?: -1,
            forSale = forSale.falseToNull(),
            forTrade = forTrade.falseToNull(),
            wishlistCount = wishlistCount.zeroToNull(),
            funnyCount = funnyCount.zeroToNull(),
            housesAndCards = housesAndCards?.filter { it.house != House.Prophecy }?.addBonusIcons(bonusIcons()) ?: listOf(),
            prophecies = housesAndCards?.firstOrNull { it.house == House.Prophecy }?.cards,

            lastSasUpdate = lastUpdate?.toLocalDateWithOffsetMinutes(-420)?.toString().emptyToNull(),

            sasPercentile = stats?.sasStats?.closestPercentileForValue(synergies?.sasRating ?: 0) ?: 0.0,

            synergyDetails = if (includeDetails) synergies?.synergyCombos else synergies?.synergyCombos?.map {
                it.copy(
                    synergies = listOf()
                )
            },
            tokenCreationValues = synergies?.tokenCreationValues,
            efficiencyBonus = synergies?.efficiencyBonus ?: 0.0,

            hasOwnershipVerification = hasOwnershipVerification.falseToNull(),

            dateAdded = dateAdded,

            twinId = twinId,
            tokenInfo = token?.toTokenInfo(),
            hauntingOdds = synergies?.hauntingOdds,
        )
    }

    fun addGameStats(keyforgeDeck: KeyForgeDeck): Deck? {
        if (this.wins == keyforgeDeck.wins && this.losses == keyforgeDeck.losses
            && this.chains == keyforgeDeck.chains && this.powerLevel == keyforgeDeck.power_level
        ) {
            return null
        }
        return this.copy(
            wins = keyforgeDeck.wins,
            losses = keyforgeDeck.losses,
            chains = keyforgeDeck.chains,
            powerLevel = keyforgeDeck.power_level
        )
    }

}

fun List<DokCardInDeck>.genCardNamesIndexableString(): String {
    return "~" +
            // Add the cards themselves
            this
                .groupBy { it.card.cardTitle }
                .map { entry ->
                    "${entry.key}${(1..entry.value.size).joinToString("")}"
                }.sorted().joinToString("~") + "~" +
            // Add duplicates for mavericks with the house
            this
                .asSequence()
                .filter { it.maverick && !it.anomaly }
                .groupBy { it.card.cardTitle }
                .flatMap { entry ->
                    entry.value
                        .groupBy { it.house }
                        .map { houseToCards ->
                            val firstCard = houseToCards.value[0]
                            "${firstCard.card.cardTitle}${firstCard.house}"
                        }
                }.sorted().joinToString("~") + "~" +
            // Add duplicates for anomalies with the house
            this
                .asSequence()
                .filter { it.anomaly }
                .groupBy { it.card.cardTitle }
                .flatMap { entry ->
                    entry.value
                        .groupBy { it.house }
                        .map { houseToCards ->
                            val firstCard = houseToCards.value[0]
                            "${firstCard.card.cardTitle}${firstCard.house}"
                        }
                }.sorted().joinToString("~") + "~"
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DecksPage(
    val decks: List<DeckSearchResult>,
    val page: Long
)

data class DeckCount(
    val pages: Long,
    val count: Long
)

data class DeckBonusIcons(
    val bonusIconHouses: List<BonusIconHouse> = listOf()
)

data class BonusIconHouse(
    val house: House,
    val bonusIconCards: List<BonusIconsCard> = listOf(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BonusIconsCard(
    val cardTitle: String,
    val bonusAember: Int = 0,
    val bonusCapture: Int = 0,
    val bonusDamage: Int = 0,
    val bonusDraw: Int = 0,
    val bonusDiscard: Int = 0,
    val bonusBobnar: Boolean = false,
    val bonusDis: Boolean = false,
    val bonusEkwidon: Boolean = false,
    val bonusGeistoid: Boolean = false,
    val bonusLogos: Boolean = false,
    val bonusMars: Boolean = false,
    val bonusSkyborn: Boolean = false,
    val bonusSanctum: Boolean = false,
    val bonusSaurian: Boolean = false,
    val bonusShadows: Boolean = false,
    val bonusStarAlliance: Boolean = false,
    val bonusUntamed: Boolean = false,
) {
    fun bonusHouses() = setOfNotNull(
        if (bonusBobnar) House.Brobnar else null,
        if (bonusDis) House.Dis else null,
        if (bonusEkwidon) House.Ekwidon else null,
        if (bonusGeistoid) House.Geistoid else null,
        if (bonusLogos) House.Logos else null,
        if (bonusMars) House.Mars else null,
        if (bonusSkyborn) House.Skyborn else null,
        if (bonusSanctum) House.Sanctum else null,
        if (bonusSaurian) House.Saurian else null,
        if (bonusShadows) House.Shadows else null,
        if (bonusStarAlliance) House.StarAlliance else null,
        if (bonusUntamed) House.Untamed else null,
    )
}

fun List<DokCardInDeck>.withBonusIcons(icons: DeckBonusIcons): List<DokCardInDeck> {
    if (icons.bonusIconHouses.isEmpty()) return this
    return this.groupBy { it.house }
        .map { houseAndCards ->
            val bonusIconsCards =
                icons.bonusIconHouses.firstOrNull { it.house == houseAndCards.key }?.bonusIconCards?.toMutableList() ?: mutableListOf()
            houseAndCards.value.map { dokCardInDeck ->
                val bonusIcons =
                    bonusIconsCards.find { cardIcons -> cardIcons.cardTitle.toLegacyUrlFriendlyCardTitle() == dokCardInDeck.card.cardTitle.toLegacyUrlFriendlyCardTitle() }
                bonusIconsCards.remove(bonusIcons)
                dokCardInDeck.copy(
                    bonusAember = bonusIcons?.bonusAember ?: 0,
                    bonusCapture = bonusIcons?.bonusCapture ?: 0,
                    bonusDamage = bonusIcons?.bonusDamage ?: 0,
                    bonusDraw = bonusIcons?.bonusDraw ?: 0,
                    bonusDiscard = bonusIcons?.bonusDiscard ?: 0,
                    bonusHouses = bonusIcons?.bonusHouses() ?: emptySet(),
                )
            }
        }
        .flatten()
}
