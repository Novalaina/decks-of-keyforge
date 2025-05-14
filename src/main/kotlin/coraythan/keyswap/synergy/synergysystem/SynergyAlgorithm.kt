package coraythan.keyswap.synergy.synergysystem

import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.decks.models.GenericDeck
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.roundToTwoSigDig
import coraythan.keyswap.synergy.*
import coraythan.keyswap.synergy.synergysystem.GenerateDeckAndHouseTraits.addDeckTraits
import coraythan.keyswap.synergy.synergysystem.HouseEnhancementAlgorithm.generateHouseEnhancementCombos
import coraythan.keyswap.synergy.synergysystem.SelfEnhancementAlgorithm.generateSelfEnhancementCombos
import coraythan.keyswap.synergy.synergysystem.TokenSynergyService.makeTokenValues
import org.slf4j.LoggerFactory
import java.math.RoundingMode
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object DeckSynergyService {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun ratingsToPercent(synRating: Int, traitStrength: TraitStrength): Int {
        return (if (synRating < 0) -1 else 1) * when (synRating.absoluteValue + traitStrength.value) {
            2 -> 2
            3 -> 5
            4 -> 10
            5 -> 15
            6 -> 25
            7 -> 33
            8 -> 50
            9 -> 75
            10 -> 100
            11 -> 100
            12 -> 100
            else -> {
                log.warn("Bad ratings! $synRating $traitStrength")
                0
            }
        }
    }

    private fun synergizedValue(
        totalSynPercent: Int,
        min: Double,
        max: Double?,
        hasPositive: Boolean,
        hasNegative: Boolean,
        baseSynPercent: Int?
    ): SynergizedValue {
        return if (max.isZeroOrNull()) {
            SynergizedValue(min, 0.0)
        } else {
            val range = max!! - min

            val divideBy = if (hasPositive && hasNegative && baseSynPercent == null) 200 else 100

            // Divide by 200 if positive + negative and no starting so that 100% positive 0% negative maxes out synergy
            val synValue = (totalSynPercent * range) / divideBy
            val startingPoint = when {
                baseSynPercent != null -> (range * (baseSynPercent.toDouble() / divideBy.toDouble())) + min
                hasPositive && hasNegative -> (range / 2) + min
                hasPositive -> min
                else -> max
            }
            val uncappedValue = synValue + startingPoint
            val value = when {
                uncappedValue < min -> min
                uncappedValue > max -> max
                else -> uncappedValue
            }
            val cappedStartPoint = if (startingPoint > max) max else if (startingPoint < min) min else startingPoint
//            log.info("Starting point: $startingPoint total percent: $totalSynPercent base $baseSynPercent min $min max $max syn value: $value range $range capped start $cappedStartPoint")
            SynergizedValue(value, value - cappedStartPoint)
        }
    }

    fun fromDeckWithCards(
        deck: GenericDeck,
        inputCards: List<DokCardInDeck>,
        token: DokCardInDeck? = null
    ): DeckSynergyInfo {

        val cardsNoToken = if (inputCards.any { it.card.big }) {
            inputCards
                .groupBy { it.card.cardTitle }
                .flatMap {
                    if (it.value.first().card.big) {
                        it.value.drop(it.value.size / 2)
                    } else {
                        it.value
                    }
                }
        } else {
            inputCards
        }

        val tokenValues = makeTokenValues(cardsNoToken, token)

        val cards: List<DokCardInDeck> = cardsNoToken
            .let {
                if (token == null) {
                    it
                } else {
                    val tokenCards = (0..<((tokenValues?.tokensPerGame?.roundToInt() ?: 0))).map { token }
                    it.plus(tokenCards)
                }

            }
        val deckStats = DeckSynergyStats.createStats(deck, cards, tokenValues)

        val traitsMap = mutableMapOf<SynergyTrait, MatchSynergiesToTraits>()

        val cardsMap: Map<House, Map<String, CardToMatchInfo>> = deck.houses
            .associateWith { house ->
                cards
                    .filter { it.allHouses.contains(house) }
                    .groupBy { it.card.cardTitle }
                    .map { it.key to CardToMatchInfo(it.value.size, it.value.first().card.cardType) }.toMap()
            }

        // Add traits from each card
        cards.forEach { dokCardInDeck ->
            val card = dokCardInDeck.card
            val cardInfo = dokCardInDeck.extraCardInfo
            val cardSpecialTraits = card.traits.mapNotNull {
                val trait = SynergyTrait.fromTrait(it)
                if (trait == null) null else SynTraitValue(trait)
            }
            val cardAllTraits = cardInfo.traits
                .plus(cardSpecialTraits)
                .let {
                    if (dokCardInDeck.enhanced) {
                        it.plus(
                            SynTraitValue(
                                SynergyTrait.enhanced,
                                rating = 3,
                                cardTypes = cardInfo.allCardTypes().toList()
                            )
                        )
                    } else {
                        it
                    }
                }
            cardAllTraits
                .forEach { traitValue ->
                    traitsMap.addTrait(traitValue, dokCardInDeck, dokCardInDeck.allHouses)
                    if (traitValue.trait == SynergyTrait.uses
                        && (traitValue.cardTypes.isNullOrEmpty()
                                || traitValue.cardTypes.any { it == CardType.Creature || it == CardType.TokenCreature })
                    ) {

                        traitsMap.addTrait(
                            traitValue.copy(trait = SynergyTrait.causesReaping),
                            dokCardInDeck,
                            dokCardInDeck.allHouses
                        )
                        if (traitValue.rating > 1) {
                            traitsMap.addTrait(
                                traitValue.copy(
                                    trait = SynergyTrait.causesFighting,
                                    rating = traitValue.rating - 1
                                ), dokCardInDeck, dokCardInDeck.allHouses
                            )
                        }
                    }
                }
            traitsMap.addTrait(SynTraitValue(SynergyTrait.any), dokCardInDeck, dokCardInDeck.allHouses)
        }

        // log.info("Traits map is: ${ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(traitsMap)}")

        AutomaticTraitsAlgorithm.addAutomaticTraits(traitsMap, cards)
        addDeckTraits(traitsMap, cards)

        val synergyCombos: List<SynergyCombo> = cards
            .groupBy { Pair(it.card.cardTitle, it.house) }
            .map { cardsById ->
                val card = cardsById.value[0]
                val count = cardsById.value.size
                val cardInfo = card.extraCardInfo

                val matchedTraits: Map<String?, List<SynergyMatch>> = cardInfo.synergies
                    .map { synergy ->

                        val synergyTrait = synergy.trait
                        val cardNames = mutableSetOf<String>()

                        // First check if the synergy matches a deck stat. If not then calculate a cards based trait
                        val deckStatsSyn = deckStats.synPercent(synergy, cardsById.key.second)
                        if (deckStatsSyn != null) {
                            cardNames.addAll(deckStatsSyn.matches)
                        }
                        val synPercent = deckStatsSyn?.value
                            ?: TraitStrength.entries.sumOf { strength ->
                                val matches: SynMatchInfo? = if (synergy.trait == SynergyTrait.enhancedHouses) {
                                    SynMatchInfo(
                                        matches = mapOf(TraitStrength.STRONG to card.bonusHouses.size),
                                        cardNames = card.allHouses.minus(card.house).map { it.masterVaultValue }
                                    )
                                } else if (synergy.cardName == null) {
                                    traitsMap[synergyTrait]?.matches(card, synergy)
                                } else {
                                    cardMatches(card, synergy, cardsMap)
                                }
                                if (matches == null) {
                                    0
                                } else {
                                    val matchesAtStrength = matches.matches[strength] ?: 0
                                    if (matchesAtStrength > 0) {
                                        cardNames.addAll(matches.cardNames)
                                    }
                                    // log.info("${card.cardTitle}: $synergyTrait syn rating: ${synergy.rating} $strength = $matchesAtStrength")
                                    matchesAtStrength * ratingsToPercent(synergy.rating, strength)
                                }
                            }

                        SynergyMatch(synergy, synPercent, cardNames)
                    }.groupBy { it.trait.synergyGroup }

                var generalGroupMax = 1000

                val groupSynPercents = matchedTraits.map { groupSyns ->
                    val isPrimary = groupSyns.value.any { it.trait.primaryGroup }
                    val groupMax = groupSyns.value.find { it.trait.synergyGroupMax != 0 }?.trait?.synergyGroupMax
                    val groupSynergy = groupSyns.value.sumOf { it.percentSynergized }
                    if (isPrimary) {
                        generalGroupMax = groupSynergy
                    }
                    if (groupMax == null) {
                        groupSynergy
                    } else if ((groupMax in 1 until groupSynergy) || (groupMax in (groupSynergy + 1)..-1)) {
                        groupMax
                    } else {
                        groupSynergy
                    }
                }

                val groupSynPercentsMaxReduced = if (generalGroupMax == 1000) {
                    groupSynPercents
                } else {
                    groupSynPercents.map { if (it > generalGroupMax) generalGroupMax else it }
                }

                val totalSynPercent = groupSynPercentsMaxReduced.sum()

                val hasPositive = cardInfo.synergies.find { it.rating > 0 } != null
                val hasNegative = cardInfo.synergies.find { it.rating < 0 } != null

                val aValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.amberControl,
                    cardInfo.amberControlMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val eValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.expectedAmber,
                    cardInfo.expectedAmberMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                // log.info("For card ${card.cardTitle} e value is $eValue expected aember ${cardInfo.expectedAmber}")
                val rValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.artifactControl,
                    cardInfo.artifactControlMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val cValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.creatureControl,
                    cardInfo.creatureControlMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val fValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.efficiency,
                    cardInfo.efficiencyMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val uValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.recursion,
                    cardInfo.recursionMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val pValue =
                    if (cardInfo.realEffectivePower == 0 && (cardInfo.effectivePowerMax == null || cardInfo.effectivePowerMax == 0.0)) {
                        SynergizedValue(cardInfo.realEffectivePower.toDouble(), 0.0)
                    } else {
                        synergizedValue(
                            totalSynPercent,
                            cardInfo.realEffectivePower.toDouble(),
                            cardInfo.effectivePowerMax,
                            hasPositive,
                            hasNegative,
                            cardInfo.baseSynPercent
                        )
                    }
                val dValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.disruption,
                    cardInfo.disruptionMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val apValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.creatureProtection,
                    cardInfo.creatureProtectionMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )
                val oValue = synergizedValue(
                    totalSynPercent,
                    cardInfo.other,
                    cardInfo.otherMax,
                    hasPositive,
                    hasNegative,
                    cardInfo.baseSynPercent
                )

                val synergizedValues = listOf(
                    aValue,
                    eValue,
                    rValue,
                    cValue,
                    fValue,
                    uValue,
                    pValue.copy(
                        value = (pValue.value / 10).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble(),
                        synergy = (pValue.synergy / 10).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()
                    ),
                    dValue,
                    apValue,
                    oValue
                )
                val synergyValues = synergizedValues.map { it.synergy }

                SynergyCombo(
                    house = card.house,
                    cardName = card.card.cardTitle,
                    synergies = matchedTraits.values.flatten()
                        .sortedBy { it.trait.synergyGroup },
                    netSynergy = synergyValues.sum(),
                    aercScore = synergizedValues.sumOf { it.value } + (card.card.cardType.creatureBonus()),

                    amberControl = aValue.value,
                    expectedAmber = eValue.value,
                    artifactControl = rValue.value,
                    creatureControl = cValue.value,
                    efficiency = fValue.value,
                    recursion = uValue.value,
                    effectivePower = pValue.value.toInt(),

                    disruption = dValue.value,
                    creatureProtection = apValue.value,
                    other = oValue.value,
                    copies = count,
                    synStart = cardInfo.baseSynPercent
                )
            }
            .plus(generateSelfEnhancementCombos(cards))
            .plus(generateHouseEnhancementCombos(cards))

        val a = synergyCombos.sumOf { it.amberControl * it.copies }
        val e = synergyCombos.sumOf { it.expectedAmber * it.copies }
        val r = synergyCombos.sumOf { it.artifactControl * it.copies }
        val c = synergyCombos.sumOf { it.creatureControl * it.copies }
        val f = synergyCombos.sumOf { it.efficiency * it.copies }
        val u = synergyCombos.sumOf { it.recursion * it.copies }
        val d = synergyCombos.sumOf { it.disruption * it.copies }
        val p = synergyCombos.sumOf { it.effectivePower * it.copies }
        val o = synergyCombos.sumOf { it.other * it.copies }
        val cp = synergyCombos.sumOf { it.creatureProtection * it.copies }

        val creatureCount =
            cards.filter { it.card.cardType == CardType.Creature }.size + (tokenValues?.tokensPerGame?.roundToInt()
                ?: 0)
        val powerValue = p.toDouble() / 10.0

        // Remember! When updating this also update Card
        val synergyUnroundedRaw = synergyCombos.filter { it.netSynergy > 0 }.sumOf { it.netSynergy * it.copies }

        val antiSynergyToRound = synergyCombos.filter { it.netSynergy < 0 }.sumOf { it.netSynergy * it.copies }
        val antisynergy = antiSynergyToRound.roundToInt().absoluteValue
        val preSas =
            a + e + r + c + f + u + d + cp + o + powerValue + (creatureCount.toDouble() * StaticAercValues.creatureBonus)

        val efficiencyBonus = calculateEfficiencyBonus(synergyCombos, preSas)

        val synergy = (synergyUnroundedRaw + efficiencyBonus).roundToInt()

        val newSas = (preSas + efficiencyBonus).roundToInt()
        val rawAerc = newSas + antisynergy - synergy

        return DeckSynergyInfo(
            synergyRating = synergy,
            antisynergyRating = antisynergy,
            synergyCombos = synergyCombos.sortedByDescending { it.netSynergy },
            rawAerc = rawAerc,
            sasRating = newSas,

            amberControl = a,
            expectedAmber = e,
            artifactControl = r,
            creatureControl = c,
            efficiency = f,
            recursion = u,
            effectivePower = p,
            disruption = d,
            creatureProtection = cp,
            other = o,

            efficiencyBonus = efficiencyBonus,
            tokenCreationValues = if (tokenValues == null) null else TokenCreationValues(
                tokensPerGame = tokenValues.tokensPerGame,
                tokensPerHouse = tokenValues.tokensPerGamePerHouse.map { TokensPerGameForHouse(it.key, it.value) },
            ),
            hauntingOdds = if (deck.expansionEnum == Expansion.GRIM_REMINDERS) {
                deckStats.deckStats[SynergyTrait.haunted]?.value?.toDouble()?.div(10)?.roundToInt()
            } else {
                null
            }
        )
    }

    private fun cardMatches(
        card: DokCardInDeck,
        synergy: SynTraitValue,
        cardsMap: Map<House, Map<String, CardToMatchInfo>>
    ): SynMatchInfo? {
        val matches: List<CardToMatchInfo> = when (synergy.house) {
            SynTraitHouse.house ->
                card.allHouses.mapNotNull { cardsMap[it]?.get(synergy.cardName) }

            SynTraitHouse.outOfHouse ->
                cardsMap.filter { !card.allHouses.contains(it.key) }
                    .mapNotNull { it.value[synergy.cardName] }

            else -> cardsMap.mapNotNull {
                it.value[synergy.cardName]
            } // Continuous and any are synonymous for card matches
        }
        val targetType = matches.firstOrNull()?.type
        val cardCount = if (targetType == CardType.TokenCreature) 2 else matches.sumOf { it.quantity }
        val strength = if (targetType == CardType.TokenCreature) {
            TraitStrength.STRONG
        } else {
            TraitStrength.NORMAL
        }
        return if (cardCount != 0) {
            SynMatchInfo(mapOf(strength to if (card.card.cardTitle == synergy.cardName) cardCount - 1 else cardCount))
        } else {
            null
        }
    }

    private fun calculateEfficiencyBonus(combos: List<SynergyCombo>, sas: Double): Double {
        return combos
            .filter { it.efficiency > 0 }
            .sumOf { combo ->
                val f = combo.efficiency
                val efficiencyBonus = (f * (((sas - combo.aercScore) / 35) * 0.4) / StaticAercValues.draw) - f
                // log.info("FB $efficiencyBonus x copies ${combo.copies}")
                efficiencyBonus * combo.copies
            }
    }


}

data class CardToMatchInfo(
    val quantity: Int,
    val type: CardType,
)

data class SynergizedValue(val value: Double, val synergy: Double)

fun Double?.isZeroOrNull() = this == null || this.roundToTwoSigDig() == 0.0
