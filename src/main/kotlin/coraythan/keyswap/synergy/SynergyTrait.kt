package coraythan.keyswap.synergy

import coraythan.keyswap.generatets.GenerateTs

enum class TraitStrength(val value: Int) {
    EXTRA_STRONG(6),
    STRONG(4),
    NORMAL(3),
    WEAK(2),
    EXTRA_WEAK(1)
}

@GenerateTs
enum class SynergyTrait {

    // Amber and Keys
    stealsAmber,
    capturesAmber,
    increasesKeyCost,
    scalingAmberControl,
    exalt,
    putsAmberOnTarget,
    forgesKeys,
    forgesKeysWithoutAember,
    preventsForging,
    massCapturedAmberRemoval,

    // Board Control
    destroys,
    dealsDamage,
    boardClear,
    damagesMultiple,
    exhausts,

    // Efficiency
    archives,
    archivesRandom,
    drawsCards,
    increasesHandSize,
    playsCards,
    shufflesDiscard,
    mills,
    revealsTopDeck,

    // Disruption
    discardsCards,
    purges,
    chains,
    reduces_R_HandSize,
    revealsHand,
    controlsHouseChoice,

    // Recursion
    returns_R_ToHand,
    shuffles_R_IntoDeck,
    replays,
    puts_R_OnBottomOfDeck,

    // Creatures and Artifacts
    uses,
    readies,
    causesFighting,
    causesReaping,
    goodReap,
    goodAction,
    goodPlay,
    goodFight,
    goodDestroyed,
    protectsCreatures,
    increasesCreaturePower,
    moves,

    // Other Traits
    any,
    makesTokens,
    scrapValue,
    preventsDamage,
    alpha,
    omega,
    raisesTide,
    lowersTide,
    replaysSelf,
    dangerousRandomPlay,
    prophecy,

    // Automatically applied traits below here

    // This must stay as the first of the auto applied traits
    enhanced,

    enhancedHouses,
    selfEnhanced,
    bonusAmber,
    bonusDraw,
    bonusDamage,
    bonusCapture,
    bonusDiscard,
    expectedAember,
    capturedAmber,
    targettedCapturedAmber,

    // Deck traits In general these are 50 to 60 percentile = 0, 60+ = 1, 70+ = 2, 80+ = 3 90+ = 4 +=1

    creatureCount,
    haunted,
    tokenCount,
    totalCreaturePower,
    totalArmor,
    fate,

    // Special traits
    card,
    // Automatically applied
    highValue,

    // DEPRECATED

    // This must stay as first deprecated!
    playsFromOtherHouse,

    hasMars,

    returns_R_FromDiscard,
    highExpectedAmber, // for house: 7=0, 8=1/4, 9=1/2, 10=3/4, 11=1
    // for deck: 22+=1/4, 24+=1/2, 26=3/4, 27+=1

    lowExpectedAmber, // for house: 7=0, 6=1/4, 5=1/2, 4=3/4, 3=1
    // for deck: 18-=1/4, 17-=1/2, 16-=3/4, 14-=1

    removesCapturedAmber,

    highTotalArmor, // 4=1/4, 5=1/2, 7=3/4, 9=1

    // creature count has out-of-house numbers

    // Deck or House only traits
    highTotalCreaturePower, // for house: 22+=1/4, 24+=1/2, 26+=3/4, 28+=1
    // for deck: 68+=1/4, 73+=1/2, 78+=3/4, 84+=1
    lowTotalCreaturePower, // 60-=1/4, 56-=1/2, 51-=3/4, 46-=1

    highCreatureCount, // for house: =<6=0, 7=1/4, 8=1/2, 9 =3/4, 10,11,12=1
    // for deck: 17+=1/4, 18+=1/2, 19+=3/4, 21+=1

    lowCreatureCount, // for house: =>6=0, 5=1/4, 4=1/2, 3=3/4, 2,1,0=1
    // for deck: 16-=1/4, 15-=1/2, 14-=3/4, 13-=1

    upgradeCount, // for house or deck: 1+ = 1/4 2+ = 1/2 3+ = 3/4 4+ = 1
    highArtifactCount, // 4=0, 5=1/4, 6=1/2, 7=3/4, 8+=1
    lowArtifactCount, // 4=0, 3=1/4, 2=1/2, 1=3/4, 0=1

    elusive,
    skirmish,
    poison,
    deploy,
    ward,
    preventsFighting,
    protectsFromEffects,
    preventsRemoval,
    heals,
    distributableDamage,
    stuns,
    sacrificesCreatures,
    addsPowerTokens,
    discardsFromDeck,
    addsArmor,
    usableArtifact,
    staticArtifact,
    reorderDeck,
    revealsBottomDeck,
    controls,
    inAllHouses,
    regenerates;

    // DEPRECATED END

    companion object {
        fun fromTrait(trait: String): SynergyTrait? {
            return try {
                valueOf(trait.lowercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
