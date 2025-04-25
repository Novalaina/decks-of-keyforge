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
    val singleHouse: Boolean = false
) {
    CALL_OF_THE_ARCHONS(
        341,
        "CotA",
        setOf(House.Brobnar, House.Dis, House.Logos, House.Mars, House.Sanctum, House.Shadows, House.Untamed)
    ),
    AGE_OF_ASCENSION(
        435,
        "AoA",
        setOf(House.Brobnar, House.Dis, House.Logos, House.Mars, House.Sanctum, House.Shadows, House.Untamed)
    ),
    WORLDS_COLLIDE(
        452,
        "WC",
        setOf(House.Brobnar, House.Dis, House.Logos, House.StarAlliance, House.Saurian, House.Shadows, House.Untamed)
    ),
    ANOMALY_EXPANSION(453, "AE", setOf(), tournamentLegal = false),
    MASS_MUTATION(
        479,
        "MM",
        setOf(House.StarAlliance, House.Dis, House.Logos, House.Saurian, House.Sanctum, House.Shadows, House.Untamed)
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
        )
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
        hasTokens = true
    ),
    GRIM_REMINDERS(
        700, "GR", setOf(
            House.Brobnar,
            House.Ekwidon,
            House.Geistoid,
            House.Mars,
            House.StarAlliance,
            House.Unfathomable,
            House.Untamed,
        )
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
        )
    ),
    AEMBER_SKIES(
        800,
        "AS",
        setOf(House.Brobnar, House.Dis, House.Ekwidon, House.Geistoid, House.Logos, House.Mars, House.Skyborn),
    ),
    TOKENS_OF_CHANGE(
        855,
        "ToC",
        setOf(House.Logos, House.Redemption, House.Shadows, House.Skyborn, House.Untamed),
        hasTokens = true,
    ),
    MORE_MUTATION(
        874,
        "MoM",
        setOf(House.StarAlliance, House.Dis, House.Logos, House.Saurian, House.Sanctum, House.Shadows, House.Untamed),
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
        )
    );

    companion object {
        fun forExpansionNumber(expansionNumber: Int?) = entries.find { it.expansionNumber == expansionNumber }
            ?: throw IllegalStateException("No expansion for number $expansionNumber")

        fun forExpansionNumberNullable(expansionNumber: Int?) = entries.find { it.expansionNumber == expansionNumber }

        fun expansionsWithTokens() = entries.filter { it.hasTokens }
    }
}

val activeExpansions = setOf(
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.UNCHAINED_2022,
    Expansion.VAULT_MASTERS_2023,
    Expansion.GRIM_REMINDERS,
    Expansion.MENAGERIE_2024,
    Expansion.VAULT_MASTERS_2024,
    Expansion.MARTIAN_CIVIL_WAR,
    Expansion.AEMBER_SKIES,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.MORE_MUTATION,
    Expansion.DISCOVERY,
    Expansion.VAULT_MASTERS_2025,
)

// Include Expansions which have had SAS scores created and are tournament legal
val includeInGlobalStats = setOf(
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.VAULT_MASTERS_2023,
    Expansion.GRIM_REMINDERS,
    Expansion.VAULT_MASTERS_2024,
    Expansion.AEMBER_SKIES,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.MORE_MUTATION,
    Expansion.DISCOVERY,
    Expansion.VAULT_MASTERS_2025,
)
