package coraythan.keyswap.expansions

import coraythan.keyswap.House
import coraythan.keyswap.generatets.GenerateTs

@GenerateTs
enum class Expansion(
    val expansionNumber: Int,
    val readable: String,
    val houses: Set<House>,
    val hasTokens: Boolean = false,
    val tournamentLegal: Boolean = true,
    val singleHouse: Boolean = false,
    val active: Boolean = true,
    val includeInStatistics: Boolean = false,
) {
    CALL_OF_THE_ARCHONS(
        341,
        "CotA",
        setOf(House.Brobnar, House.Dis, House.Logos, House.Mars, House.Sanctum, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    AGE_OF_ASCENSION(
        435,
        "AoA",
        setOf(House.Brobnar, House.Dis, House.Logos, House.Mars, House.Sanctum, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    WORLDS_COLLIDE(
        452,
        "WC",
        setOf(House.Brobnar, House.Dis, House.Logos, House.StarAlliance, House.Saurian, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    ANOMALY_EXPANSION(453, "AE", setOf(), tournamentLegal = false, active = false),
    MASS_MUTATION(
        479,
        "MM",
        setOf(House.StarAlliance, House.Dis, House.Logos, House.Saurian, House.Sanctum, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    DARK_TIDINGS(
        496,
        "DT",
        setOf(
            House.StarAlliance,
            House.Unfathomable,
            House.Logos,
            House.Saurian,
            House.Sanctum,
            House.Shadows,
            House.Untamed
        ),
        includeInStatistics = true
    ),
    WINDS_OF_EXCHANGE(
        600,
        "WoE",
        setOf(
            House.Brobnar,
            House.Ekwidon,
            House.Mars,
            House.Saurian,
            House.Sanctum,
            House.StarAlliance,
            House.Unfathomable
        ),
        hasTokens = true,
        includeInStatistics = true
    ),
    UNCHAINED_2022(
        601, "UC22", setOf(
            House.Brobnar, House.Dis, House.Logos, House.Mars, House.Sanctum, House.Shadows, House.Untamed,
            House.StarAlliance, House.Saurian, House.Ekwidon, House.Unfathomable
        ),
        hasTokens = true,
        tournamentLegal = false
    ),
    VAULT_MASTERS_2023(
        609,
        "VM23",
        setOf(House.Brobnar, House.Mars, House.Logos, House.Untamed, House.Dis, House.StarAlliance, House.Saurian),
        hasTokens = true,
        includeInStatistics = true
    ),
    GRIM_REMINDERS(
        700,
        "GR",
        setOf(
            House.Brobnar,
            House.Ekwidon,
            House.Geistoid,
            House.Mars,
            House.StarAlliance,
            House.Unfathomable,
            House.Untamed,
        ),
        includeInStatistics = true
    ),
    MENAGERIE_2024(
        722, "MN24", setOf(
            House.Brobnar, House.Dis, House.Ekwidon, House.Geistoid, House.Mars, House.Sanctum, House.Saurian,
            House.Shadows, House.StarAlliance, House.Unfathomable, House.Untamed,
        ),
        hasTokens = true,
        tournamentLegal = false
    ),
    VAULT_MASTERS_2024(
        737,
        "VM24",
        setOf(
            House.Brobnar,
            House.Dis,
            House.Sanctum,
            House.Shadows,
            House.StarAlliance,
            House.Unfathomable,
            House.Untamed
        ),
        includeInStatistics = true
    ),
    AEMBER_SKIES(
        800,
        "AS",
        setOf(House.Brobnar, House.Dis, House.Ekwidon, House.Geistoid, House.Logos, House.Mars, House.Skyborn),
        includeInStatistics = true
    ),
    TOKENS_OF_CHANGE(
        855,
        "ToC",
        setOf(House.Logos, House.Redemption, House.Shadows, House.Skyborn, House.Untamed),
        hasTokens = true,
        includeInStatistics = true
    ),
    MORE_MUTATION(
        874,
        "MoM",
        setOf(House.StarAlliance, House.Dis, House.Logos, House.Saurian, House.Sanctum, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    MARTIAN_CIVIL_WAR(
        892,
        "MCW",
        setOf(House.Elders, House.IronyxRebels),
        hasTokens = true,
        tournamentLegal = false,
        singleHouse = true
    ),
    DISCOVERY(
        907,
        "D",
        setOf(House.Brobnar, House.Dis, House.Logos, House.Sanctum, House.StarAlliance, House.Shadows, House.Untamed),
        includeInStatistics = true
    ),
    VAULT_MASTERS_2025(
        939,
        "VM25",
        setOf(
            House.Ekwidon,
            House.Geistoid,
            House.Logos,
            House.Mars,
            House.Saurian,
            House.Shadows,
            House.Unfathomable,
        ),
        includeInStatistics = true
    ),
    PROPHETIC_VISIONS(
        886,
        "PV",
        setOf(
            House.Dis,
            House.Logos,
            House.Redemption,
            House.Sanctum,
            House.Saurian,
            House.Shadows,
            House.StarAlliance,
            House.Untamed,
        )
    );

    companion object {
        fun forExpansionNumber(expansionNumber: Int?) = entries.find { it.expansionNumber == expansionNumber }
            ?: throw IllegalStateException("No expansion for number $expansionNumber")

        fun forExpansionNumberNullable(expansionNumber: Int?) = entries.find { it.expansionNumber == expansionNumber }

        fun expansionsWithTokens() = entries.filter { it.hasTokens }
    }
}

val activeExpansions = Expansion.entries.filter { it.active }

// Include Expansions which have had SAS scores created and are tournament legal
val includeInGlobalStats = Expansion.entries.filter { it.includeInStatistics }
