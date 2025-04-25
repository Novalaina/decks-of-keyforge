package coraythan.keyswap.alliancedecks

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.decks.models.*
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.now
import coraythan.keyswap.stats.DeckStatistics
import coraythan.keyswap.synergy.DeckSynergyInfo
import coraythan.keyswap.synergy.SynTraitPlayer
import coraythan.keyswap.synergy.SynergyTrait
import coraythan.keyswap.synergy.containsTrait
import coraythan.keyswap.users.KeyUser
import coraythan.keyswap.zeroToNull
import jakarta.persistence.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.roundToInt

@Entity
data class AllianceDeck(

    override val name: String,
    override val expansion: Int,
    val sasRating: Int = 0,
    val sasVersion: Int = 0,

    val validAlliance: Boolean = true,
    val createdDateTime: ZonedDateTime? = now(),

    // Json of card ids for performance loading decks, loading cards from cache
    @Column(columnDefinition = "TEXT")
    override val cardIds: String = "",
    override val tokenNumber: Int? = null,

    override val houseNamesString: String = "",

    override val bonusIconsString: String? = null,

    @JsonIgnoreProperties("allianceDeck")
    @OneToMany(mappedBy = "allianceDeck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val allianceHouses: List<AllianceHouse> = listOf(),

    val housesUniqueId: String = "",

    val public: Boolean = false,

    @JsonIgnoreProperties("deck")
    @OneToMany(mappedBy = "deck", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val ownedDecks: List<OwnedAllianceDeck> = listOf(),

    @ManyToOne()
    val discoverer: KeyUser,

    val checkedUniqueness: Boolean = false,

    @Id
    val id: UUID = UUID.randomUUID()

) : GenericDeck {

    companion object {

        val restrictedCardsList = mapOf(
            "Befuddle" to 100,
            "Chronus" to 100,
            "Ghostform" to 100,
            "Hallafest" to 1,
            "Heart of the Forest" to 100,
            "Infurnace" to 100,
            "Jervi" to 100,
            "Key Abduction" to 1,
            "Legionary Trainer" to 100,
            "Stealth Mode" to 100,
            "United Action" to 100,
            "Winds of Death" to 100
        )

        fun determineIfValid(deck: GenericDeck, cards: List<DokCardInDeck>): Boolean {
            if (deck.expansionEnum == Expansion.MARTIAN_CIVIL_WAR) return false
            val restrictedCards = cards
                .filter { restrictedCardsList.containsKey(it.card.cardTitle) }
                .groupBy { it.card.cardTitle }
                .map { it.key to it.value.size }

            if (restrictedCards.size > 1) return false
            if (restrictedCards.any { it.second > (restrictedCardsList[it.first] ?: 0) }) return false
            return true
        }

        fun uniqueHousesId(housesAndDeckIds: List<Pair<House, String>>, tokenName: String?): String {
            val sorted = housesAndDeckIds.sortedBy { it.first }
            val toJoin = sorted.map { "${it.first}::${it.second}" }
            val houses = toJoin.joinToString("&&")
            return if (tokenName == null) {
                houses
            } else {
                "$houses&($tokenName)"
            }
        }

        fun fromDeck(deck: Deck, cards: List<DokCardInDeck>, discoverer: KeyUser): AllianceDeck {
            return AllianceDeck(
                name = deck.name,
                expansion = deck.expansion,
                cardIds = deck.cardIds,
                houseNamesString = deck.houseNamesString,
                bonusIconsString = deck.bonusIconsString,
                discoverer = discoverer,
                validAlliance = determineIfValid(deck, cards),
                tokenNumber = deck.tokenNumber,
            )
        }
    }

    val dateAdded: LocalDate?
        get() = this.createdDateTime?.toLocalDate()

    fun withBonusIcons(icons: DeckBonusIcons): AllianceDeck {
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
            deckType = DeckType.ALLIANCE,
            validAlliance = if (cards == null) validAlliance else determineIfValid(this, cards),
            keyforgeId = id.toString(),
            expansion = expansionEnum,
            name = name,

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
            keyCheatCount = (cards?.count {
                it.extraCardInfo.traits.containsTrait(SynergyTrait.forgesKeys)
                        || it.extraCardInfo.traits.containsTrait(SynergyTrait.forgesKeysWithoutAember)
            }
                ?: 0).zeroToNull(),
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
            aercScore = synergies?.rawAerc ?: -1.0.toInt(),
            sasRating = synergies?.sasRating ?: -1,
            synergyRating = synergies?.synergyRating ?: -1,
            antisynergyRating = synergies?.antisynergyRating ?: -1,
            housesAndCards = housesAndCards?.addBonusIcons(bonusIcons()) ?: listOf(),
            tokenInfo = token?.toTokenInfo(),
            hauntingOdds = synergies?.hauntingOdds,

            sasPercentile = stats?.sasStats?.closestPercentileForValue(synergies?.sasRating ?: 0) ?: 0.0,

            synergyDetails = if (includeDetails) synergies?.synergyCombos else synergies?.synergyCombos?.map {
                it.copy(
                    synergies = listOf()
                )
            },
            tokenCreationValues = synergies?.tokenCreationValues,
            efficiencyBonus = synergies?.efficiencyBonus ?: 0.0,
            allianceHouses = allianceHouses.map {
                AllianceHouseInfo(
                    keyforgeId = it.keyforgeId,
                    house = it.house,
                    name = it.name,
                )
            },
            dateAdded = dateAdded,
            discoveredBy = discoverer.username,
        )
    }
}
