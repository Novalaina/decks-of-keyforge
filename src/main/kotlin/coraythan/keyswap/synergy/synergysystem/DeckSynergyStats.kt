package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.decks.models.GenericDeck
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.roundToTwoSigDig
import coraythan.keyswap.synergy.*
import kotlin.math.roundToInt

private data class TraitVals(
    val maxDeck: Int,
    val maxHouse: Int,
    val minDeck: Int = 0,
    val minHouse: Int = 0,
)

data class DeckSynStatValue(
    val value: Int,
    val matches: List<String>,
) {

    companion object {
        fun create(value: Int, matches: List<String> = listOf()): DeckSynStatValue {
            return DeckSynStatValue(
                value,
                matches
                    .groupBy { it }
                    .map { if (it.value.size == 1) it.key else "${it.value.size} x ${it.key}" }
            )
        }
    }
}

data class DeckSynergyStats(
    val deckStats: Map<SynergyTrait, DeckSynStatValue>,
    val deckStatsFriendly: Map<SynergyTrait, DeckSynStatValue>,
    val deckStatsEnemy: Map<SynergyTrait, DeckSynStatValue>,
    val houseStats: Map<House, Map<SynergyTrait, DeckSynStatValue>>,
    val houseStatsFriendly: Map<House, Map<SynergyTrait, DeckSynStatValue>>,
    val houseStatsEnemy: Map<House, Map<SynergyTrait, DeckSynStatValue>>,
) {
    companion object {

        private val bonusIconBaseTraitStrengths = TraitVals(30, 10)

        private val traits = mapOf(
            SynergyTrait.creatureCount to TraitVals(25, 10, 11, 3),
            SynergyTrait.tokenCount to TraitVals(25, 25),
            SynergyTrait.bonusAmber to bonusIconBaseTraitStrengths,
            SynergyTrait.bonusCapture to bonusIconBaseTraitStrengths,
            SynergyTrait.bonusDamage to bonusIconBaseTraitStrengths,
            SynergyTrait.bonusDraw to bonusIconBaseTraitStrengths,
            SynergyTrait.bonusDiscard to bonusIconBaseTraitStrengths,
            SynergyTrait.totalCreaturePower to TraitVals(100, 30, 30, 15),
            SynergyTrait.totalArmor to TraitVals(10, 5),
            SynergyTrait.haunted to (TraitVals(100, 0)),
            SynergyTrait.propheticOdds to (TraitVals(100, 0)),
            SynergyTrait.expectedAember to TraitVals(30, 12, 10, 3),
            SynergyTrait.capturedAmber to TraitVals(16, 8),
            SynergyTrait.targettedCapturedAmber to TraitVals(16, 8),
        )

        private fun matches(trait: SynergyTrait) = traits.keys.contains(trait)

        fun createStats(
            deck: GenericDeck,
            inputCards: List<DokCardInDeck>,
            tokenValues: TokenValues?
        ): DeckSynergyStats {
            return DeckSynergyStats(
                deckStats = statsFromCards(
                    inputCards,
                    tokensCount = tokenValues?.tokensPerGame?.roundToInt() ?: 0,
                    player = SynTraitPlayer.ANY,
                    expansion = deck.expansionEnum,
                ),
                deckStatsFriendly = statsFromCards(
                    inputCards,
                    tokensCount = tokenValues?.tokensPerGame?.roundToInt() ?: 0,
                    player = SynTraitPlayer.FRIENDLY,
                    expansion = deck.expansionEnum,
                ),
                deckStatsEnemy = statsFromCards(
                    inputCards,
                    tokensCount = tokenValues?.tokensPerGame?.roundToInt() ?: 0,
                    player = SynTraitPlayer.ENEMY,
                    expansion = deck.expansionEnum,
                ),
                houseStats = deck.houses.associateWith {
                    statsFromCards(
                        inputCards.filter { card -> card.allHouses.contains(it) },
                        tokensCount = if (tokenValues?.tokenHouse == it) tokenValues.tokensPerGame.roundToInt() else 0,
                        player = SynTraitPlayer.ANY,
                        expansion = deck.expansionEnum,
                    )
                },
                houseStatsFriendly = deck.houses.associateWith {
                    statsFromCards(
                        inputCards.filter { card -> card.allHouses.contains(it) },
                        tokensCount = if (tokenValues?.tokenHouse == it) tokenValues.tokensPerGame.roundToInt() else 0,
                        player = SynTraitPlayer.FRIENDLY,
                        expansion = deck.expansionEnum,
                    )
                },
                houseStatsEnemy = deck.houses.associateWith {
                    statsFromCards(
                        inputCards.filter { card -> card.allHouses.contains(it) },
                        tokensCount = 0,
                        player = SynTraitPlayer.ENEMY,
                        expansion = deck.expansionEnum,
                    )
                },
            )
        }

        private fun statsFromCards(
            inputCards: List<DokCardInDeck>,
            tokensCount: Int,
            player: SynTraitPlayer,
            expansion: Expansion,
        ): Map<SynergyTrait, DeckSynStatValue> {
            val capturePips = inputCards.sumOf { it.bonusCapture }
            val creatures = inputCards.filter {
                it.card.cardType == CardType.Creature || it.card.cardType == CardType.TokenCreature || it.extraCardInfo.extraCardTypes?.contains(
                    CardType.Creature
                ) == true
            }
            val powerValueMap = inputCards.mapNotNull { card ->
                val increasesPowerTrait =
                    card.extraCardInfo.traits.firstOrNull { it.trait == SynergyTrait.increasesCreaturePower && it.player != SynTraitPlayer.ENEMY }
                val power =
                    card.card.power + (if (increasesPowerTrait == null) 0 else when (increasesPowerTrait.rating) {
                        TraitStrength.EXTRA_WEAK.value -> 1
                        TraitStrength.WEAK.value -> 2
                        TraitStrength.NORMAL.value -> 3
                        TraitStrength.STRONG.value -> 4
                        TraitStrength.EXTRA_STRONG.value -> 6
                        else -> 0
                    })
                if (power == 0) {
                    null
                } else {
                    card.card.cardTitle to power
                }
            }
            val expectedAemberMap = inputCards.mapNotNull { card ->
                val max = card.extraCardInfo.expectedAmberMax ?: 0.0
                val min = card.extraCardInfo.expectedAmber
                val expectedAember = if (max == 0.0) min else (min + max) / 2
                if (expectedAember == 0.0) {
                    null
                } else {
                    card.card.cardTitle to expectedAember
                }
            }
            return mapOf(
                SynergyTrait.creatureCount to DeckSynStatValue.create(
                    creatures.size,
                    creatures.map { it.card.cardTitle }
                ),
                SynergyTrait.bonusAmber to DeckSynStatValue.create(inputCards.sumOf { it.card.amber + it.bonusAember }),
                SynergyTrait.bonusCapture to DeckSynStatValue.create(capturePips),
                SynergyTrait.bonusDamage to DeckSynStatValue.create(inputCards.sumOf { it.bonusDamage }),
                SynergyTrait.bonusDraw to DeckSynStatValue.create(inputCards.sumOf { it.bonusDraw }),
                SynergyTrait.bonusDiscard to DeckSynStatValue.create(inputCards.sumOf { it.bonusDiscard }),
                SynergyTrait.totalCreaturePower to DeckSynStatValue.create(
                    powerValueMap.sumOf { it.second },
                    powerValueMap.map { "${it.first} +${it.second} Power" }
                ),
                SynergyTrait.tokenCount to DeckSynStatValue.create(tokensCount),
                SynergyTrait.totalArmor to DeckSynStatValue.create(inputCards.sumOf { it.card.armor }),
                SynergyTrait.expectedAember to DeckSynStatValue.create(
                    expectedAemberMap.sumOf { it.second }.roundToInt(),
                    expectedAemberMap.map { "${it.first} +${it.second} Aember" }
                ),
                SynergyTrait.haunted to calculateHauntingPercent(inputCards, player),
                SynergyTrait.propheticOdds to calculateProphecyPercent(inputCards, expansion),
                SynergyTrait.capturedAmber to calculateCapturePercentWithTraits(
                    inputCards, setOf(SynergyTrait.capturesAmber, SynergyTrait.exalt), player
                ),
                SynergyTrait.targettedCapturedAmber to calculateCapturePercentWithTraits(
                    inputCards, setOf(SynergyTrait.putsAmberOnTarget), player
                ),
            )
        }

        private fun calculateCapturePercentWithTraits(
            cards: List<DokCardInDeck>,
            checkTraits: Set<SynergyTrait>,
            target: SynTraitPlayer = SynTraitPlayer.ANY
        ): DeckSynStatValue {
            val friendlyIncluded = target == SynTraitPlayer.ANY || target == SynTraitPlayer.FRIENDLY

            val captureScore: List<Pair<Double, String>> = cards.mapNotNull { dokCardInDeck ->
                val traits = dokCardInDeck.extraCardInfo.traits

                val capTrait = traits.firstOrNull {
                    val traitMatch = checkTraits.contains(it.trait)
                    val playerMatch =
                        target == SynTraitPlayer.ANY || it.player == SynTraitPlayer.ANY || target == it.player
                    traitMatch && playerMatch
                }
                val capturePips = if (friendlyIncluded) dokCardInDeck.bonusCapture else 0

                if (capTrait == null && capturePips < 1) {
                    null
                } else {
                    val expectedCapture = if (capTrait != null) {
                        when (capTrait.strength()) {
                            TraitStrength.EXTRA_WEAK -> 0.5
                            TraitStrength.WEAK -> 1.0
                            TraitStrength.NORMAL -> 2.0
                            TraitStrength.STRONG -> 3.0
                            TraitStrength.EXTRA_STRONG -> 4.0
                        }
                    } else 0.0

                    (expectedCapture + capturePips) to "${dokCardInDeck.card.cardTitle}${if (expectedCapture > 0.0) " $expectedCapture Expected Capture" else ""}${if (capturePips > 0) " $capturePips Bonus Capture Pips" else ""}"
                }
            }

            return DeckSynStatValue.create(
                captureScore.sumOf { it.first }.roundToInt(),
                captureScore.map { it.second }
            )
        }

        private fun calculateHauntingPercent(
            cards: List<DokCardInDeck>,
            target: SynTraitPlayer = SynTraitPlayer.ANY
        ): DeckSynStatValue {
            val friendlyIncluded = target == SynTraitPlayer.ANY || target == SynTraitPlayer.FRIENDLY
            val hauntingScore = cards.mapNotNull { dokCardInDeck ->
                val traitValues = dokCardInDeck.extraCardInfo.traits.sumOf {
                    val playerMatch =
                        target == SynTraitPlayer.ANY || it.player == SynTraitPlayer.ANY || target == it.player
                    val millsOrHauntingTrait =
                        if ((it.trait == SynergyTrait.mills || it.trait == SynergyTrait.haunted) && playerMatch) {
                            when (it.strength()) {
                                TraitStrength.EXTRA_WEAK -> 5
                                TraitStrength.WEAK -> 10
                                TraitStrength.NORMAL -> 15
                                TraitStrength.STRONG -> 20
                                TraitStrength.EXTRA_STRONG -> 30
                            }
                        } else {
                            0
                        }

                    val discards =
                        if (
                            (it.trait == SynergyTrait.discardsCards || it.trait == SynergyTrait.discardsFromDeck)
                            && playerMatch
                        ) {

                            when (it.strength()) {
                                TraitStrength.EXTRA_WEAK -> 4
                                TraitStrength.WEAK -> 6
                                TraitStrength.NORMAL -> 8
                                TraitStrength.STRONG -> 12
                                TraitStrength.EXTRA_STRONG -> 16
                            }
                        } else {
                            0
                        }

                    millsOrHauntingTrait + discards
                }

                val artifact = if (dokCardInDeck.card.cardType == CardType.Artifact && friendlyIncluded) -2 else 0
                val action = if (dokCardInDeck.card.cardType == CardType.Action && friendlyIncluded) 2 else 0
                val discardPips = if (friendlyIncluded) dokCardInDeck.bonusDiscard * 4 else 0
                val total = traitValues + artifact + action + discardPips
                if (total == 0) {
                    null
                } else {
                    val traitsDesc = if (traitValues == 0) "" else " Traits = $traitValues"
                    val discardPipsDesc = if (discardPips == 0) "" else " Discard Pips = $discardPips"
                    val artifactDesc = if (artifact == 0) "" else " Artifact = $artifact"
                    val actionDesc = if (action == 0) "" else " Action = $action"
                    total to "${dokCardInDeck.card.cardTitle}: $traitsDesc$discardPipsDesc$artifactDesc$actionDesc".trim()
                }
            }

            return DeckSynStatValue.create(
                hauntingScore.sumOf { it.first },
                hauntingScore.map { it.second }
            )
        }

        private fun calculateProphecyPercent(
            cards: List<DokCardInDeck>,
            expansion: Expansion,
        ): DeckSynStatValue {
            if (expansion != Expansion.PROPHETIC_VISIONS) {
                return DeckSynStatValue.create(0)
            }
            val prophecyScore = cards
                .filter { dokCardInDeck -> dokCardInDeck.card.cardType == CardType.Prophecy }
                .mapNotNull { dokCardInDeck ->

                    val traitValues = dokCardInDeck.extraCardInfo.traits.map {
                        if (it.trait == SynergyTrait.prophecy) {
                            // When changing these, remember to change it in AercCategories too
                            when (it.strength()) {
                                TraitStrength.EXTRA_WEAK -> 5
                                TraitStrength.WEAK -> 10
                                TraitStrength.NORMAL -> 15
                                TraitStrength.STRONG -> 25
                                TraitStrength.EXTRA_STRONG -> 35
                            }
                        } else {
                            0
                        }
                    }.sum()

                    if (traitValues == 0) {
                        null
                    } else {
                        traitValues to "${dokCardInDeck.card.cardTitle}: Frequency = $traitValues%"
                    }
                }

            return DeckSynStatValue.create(
                prophecyScore.sumOf { it.first },
                prophecyScore.map { it.second }
            )
        }
    }

    fun synPercent(trait: SynTraitValue, house: House): DeckSynStatValue? {
        if (!matches(trait.trait)) return null

        val vals = traits[trait.trait] ?: return null
        val multiplier = when (trait.rating) {
            6 -> 3.0
            4 -> 2.0
            3 -> 1.0
            2 -> 0.5
            1 -> 0.25
            -1 -> -0.25
            -2 -> -0.5
            -3 -> -1.0
            -4 -> -2.0
            -6 -> -3.0
            else -> throw IllegalStateException("Bad rating ${trait.rating} $trait")
        }

        val relevantHouseStats = when (trait.player) {
            SynTraitPlayer.ANY -> houseStats
            SynTraitPlayer.FRIENDLY -> houseStatsFriendly
            SynTraitPlayer.ENEMY -> houseStatsEnemy
        }

        when (trait.house) {

            // Checking for in house
            SynTraitHouse.house -> {
                val entry = relevantHouseStats[house]?.get(trait.trait) ?: return DeckSynStatValue.create(0)
                val actual = entry.value.toDouble()
                return DeckSynStatValue.create(
                    calculatePercent(actual, vals.minHouse.toDouble(), vals.maxHouse.toDouble(), multiplier),
                    listOf(
                        percentDescription(actual, vals.minHouse.toDouble(), vals.maxHouse.toDouble(), trait.trait)
                    )
                        .plus(entry.matches)
                )
            }

            // Checking for deck trait
            SynTraitHouse.anyHouse -> {
                val relevantDeckStats = when (trait.player) {
                    SynTraitPlayer.ANY -> deckStats
                    SynTraitPlayer.FRIENDLY -> deckStatsFriendly
                    SynTraitPlayer.ENEMY -> deckStatsEnemy
                }
                val entry = relevantDeckStats[trait.trait] ?: return DeckSynStatValue.create(0)
                val actual = entry.value.toDouble()
                return DeckSynStatValue.create(
                    calculatePercent(actual, vals.minDeck.toDouble(), vals.maxDeck.toDouble(), multiplier),
                    listOf(
                        percentDescription(actual, vals.minDeck.toDouble(), vals.maxDeck.toDouble(), trait.trait)
                    )
                        .plus(entry.matches)
                )
            }

            // Checking for out of house trait
            else -> {
                val entry = relevantHouseStats.filter { it.key != house }
                val actual = entry.values.sumOf { it[trait.trait]?.value?.toDouble() ?: 0.0 }
                val matches = entry.values.flatMap { it[trait.trait]?.matches ?: listOf() }
                val outOfHouseMin = (vals.minHouse * 2).toDouble()
                val outOfHouseMax = (vals.maxHouse.toDouble() * 2)
                return DeckSynStatValue.create(
                    calculatePercent(
                        actual,
                        outOfHouseMin,
                        outOfHouseMax,
                        multiplier
                    ),
                    listOf(
                        percentDescription(actual, outOfHouseMin, outOfHouseMax, trait.trait)
                    )
                        .plus(matches)
                )
            }
        }
    }

    private fun percentDescription(actual: Double, min: Double, max: Double, trait: SynergyTrait): String {
        val outOf = "out of " + if (min == 0.0) {
            max
        } else {
            "$min-$max"
        }
        return "${actual.roundToTwoSigDig()} $trait $outOf = ${
            calculateSynPercent(
                actual,
                min,
                max
            ).roundToInt()
        }%"
    }

    private fun calculateSynPercent(actual: Double, min: Double, max: Double): Double {
        val range = max - min
        val actualMinusMin = actual - min
        return (actualMinusMin * 100.0) / range
    }

    private fun calculatePercent(actual: Double, min: Double, max: Double, multiplier: Double): Int {
        val finalPercent = calculateSynPercent(actual, min, max) * multiplier
        return finalPercent.roundToInt()
    }
}
