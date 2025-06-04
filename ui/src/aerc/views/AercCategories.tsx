import { Box, Tooltip, Typography } from "@material-ui/core"
import * as React from "react"
import { CardUtils } from "../../cards/KCard"
import { spacing } from "../../config/MuiConfig"
import { TimeUtils } from "../../config/TimeUtils"
import { DeckSearchResult } from "../../decks/models/DeckSearchResult"
import { CardType } from "../../generated-src/CardType"
import { Expansion } from "../../generated-src/Expansion"
import { SynergyCombo } from "../../generated-src/SynergyCombo"
import { SynergyTrait } from "../../generated-src/SynergyTrait"
import { AercIcon, AercType } from "../../generic/icons/aerc/AercIcon"
import { AmberIcon } from "../../generic/icons/AmberIcon"
import { ArchiveIcon } from "../../generic/icons/ArchiveIcon"
import { BoardWipeIcon } from "../../generic/icons/BoardWipeIcon"
import { ActionIcon } from "../../generic/icons/card-types/ActionIcon"
import { ArtifactIcon } from "../../generic/icons/card-types/ArtifactIcon"
import { CreatureIcon } from "../../generic/icons/card-types/CreatureIcon"
import { UpgradeIcon } from "../../generic/icons/card-types/UpgradeIcon"
import { KeyCheatIcon } from "../../generic/icons/KeyCheatIcon"
import { MutantIcon } from "../../generic/icons/MutantIcon"
import { ScalingStealIcon } from "../../generic/icons/ScalingStealIcon"
import { TideIcon } from "../../generic/icons/TideIcon"
import { InfoIconList, InfoIconValue } from "../../generic/InfoIcon"
import { HasAerc } from "../HasAerc"
import { DeckType } from "../../generated-src/DeckType"
import { TokenIcon } from "../../generic/icons/TokenIcon"
import { FrontendCard } from "../../generated-src/FrontendCard"
import { HauntedIcon } from "../../generic/icons/HauntedIcon"
import { SynTraitPlayer } from "../../generated-src/SynTraitPlayer"
import { expansionInfoMap } from "../../expansions/Expansions"
import { ProphecyIcon } from "../../generic/icons/ProphecyIcon"
import { cardStore } from "../../cards/CardStore"

interface AercCatProps {
    deck: DeckSearchResult
    cards: FrontendCard[]
    hasAerc: HasAerc
    combos?: SynergyCombo[]
    twoHigh?: boolean
}

export const AercCategoryExtras = (props: AercCatProps) => {
    const {deck, cards, twoHigh} = props
    const width = twoHigh ? undefined : 20

    const firstTwo: InfoIconValue[] = [
        {
            icon: <AmberIcon width={width}/>,
            info: deck.rawAmber,
            cardsTips: {
                matches: card => card.amber > 0 || card.extraCardInfo.enhancementAmber > 0,
                cards,
                title: "Bonus Aember"
            }
        },
    ]

    if (twoHigh) {
        const expansionSpecific = expansionSpecificCounts(props, width)

        if (expansionSpecific != null) {
            firstTwo.push(expansionSpecific)
        }
    }

    const secondTwo: InfoIconValue[] = [
        {
            icon: <ArchiveIcon width={width}/>,
            info: `${deck.cardArchiveCount ?? 0}/${CardUtils.findCardsWithFriendlyTrait(cards, SynergyTrait.archivesRandom).length}`,
            cardsTips: {
                matches: card => CardUtils.cardMatchesFriendlyTrait(card, SynergyTrait.archives),
                matches2: card => CardUtils.cardMatchesFriendlyTrait(card, SynergyTrait.archivesRandom),
                cards,
                title: "Archives",
                subtitle1: "Archives Targetted",
                subtitle2: "Archives Random"
            }
        },
        {
            icon: <KeyCheatIcon width={width}/>,
            info: deck.keyCheatCount ?? 0,
            cardsTips: {
                matches: card => CardUtils.cardMatchesFriendlyTrait(card, SynergyTrait.forgesKeys) ||
                    CardUtils.cardMatchesFriendlyTrait(card, SynergyTrait.forgesKeysWithoutAember),
                cards,
                title: "Key Cheat Cards"
            }
        },
    ]

    const thirdTwo: InfoIconValue[] = [
        {
            icon: <ScalingStealIcon width={width}/>,
            info: cards.filter(cardMatchesScalingSteal).length,
            cardsTips: {
                matches: card => cardMatchesScalingSteal(card) != null,
                cards,
                title: "Scaling Aember Control"
            }
        },
        {
            icon: <BoardWipeIcon width={width}/>,
            info: cards.filter(cardMatchesBoardWipe).length,
            cardsTips: {
                matches: card => cardMatchesBoardWipe(card) != null,
                cards,
                title: "Board Wipes"
            }
        },
    ]

    if (twoHigh) {
        return (
            <>
                <AercCategory
                    name={"Extras"}
                    infos={secondTwo}
                />
                <AercCategory
                    name={"Extras"}
                    infos={thirdTwo}
                />
                <AercCategory
                    name={"Amber"}
                    infos={firstTwo}
                />
            </>
        )
    }


    return (
        <AercCategory
            name={"Extras"}
            small={true}
            infos={firstTwo.concat(secondTwo).concat(thirdTwo)}
        />
    )
}

const cardMatchesScalingSteal = (card: FrontendCard) => card.extraCardInfo?.traits?.find(traitValue => (
    traitValue.trait === SynergyTrait.scalingAmberControl && traitValue.rating > 1
))

const cardMatchesBoardWipe = (card: FrontendCard) => card.extraCardInfo?.traits?.find(traitValue => (
    traitValue.trait === SynergyTrait.boardClear && traitValue.rating > 1
))

export const AercCategoryCounts = (props: AercCatProps) => {
    const {deck, cards, twoHigh} = props
    const width = twoHigh ? undefined : 20
    const firstTwo: InfoIconValue[] = [
        {
            icon: <ActionIcon width={width}/>,
            info: deck.actionCount ?? 0,
            cardsTips: {
                matches: card => card.cardType === CardType.Action,
                cards,
                title: "Actions"
            }
        },
        {
            icon: <CreatureIcon width={width}/>,
            info: deck.creatureCount ?? 0,
            cardsTips: {
                matches: card => card.cardType === CardType.Creature,
                cards,
                title: "Creatures"
            }
        },
    ]

    const secondTwo: InfoIconValue[] = [
        {
            icon: <ArtifactIcon width={width}/>,
            info: deck.artifactCount ?? 0,
            cardsTips: {
                matches: card => card.cardType === CardType.Artifact,
                cards,
                title: "Artifacts"
            }
        },
        {
            icon: <UpgradeIcon width={width}/>,
            info: deck.upgradeCount ?? 0,
            cardsTips: {
                matches: card => card.cardType === CardType.Upgrade,
                cards,
                title: "Upgrades"
            }
        }
    ]

    const thirdTwo: InfoIconValue[] = []

    if (twoHigh) {
        return (
            <>
                <AercCategory
                    name={"Counts"}
                    infos={firstTwo}
                />
                <AercCategory
                    name={"Counts"}
                    infos={secondTwo}
                />
            </>
        )
    }

    const expansionSpecific = expansionSpecificCounts(props, width)

    if (expansionSpecific != null) {
        thirdTwo.push(expansionSpecific)
    }

    return (
        <div>
            <AercCategory
                name={"Counts"}
                small={true}
                infos={firstTwo.concat(secondTwo).concat(thirdTwo)}
            />
            {!twoHigh && deck.dateAdded != null && deck.deckType === DeckType.STANDARD && (
                <Box display={"flex"} flexDirection={"column"}>
                    <Box flexGrow={1}/>
                    <Tooltip
                        title={`Date imported to DoK. Not recorded prior to Jun 1, 19. Exact date: ${deck.dateAdded}`}
                    >
                        <div style={{marginTop: spacing(1), display: "flex", justifyContent: "flex-end"}}>
                            <Typography variant={"body2"} color={"textSecondary"} style={{fontSize: "0.75rem"}}>
                                {TimeUtils.formatShortDate(deck.dateAdded)}
                            </Typography>
                        </div>
                    </Tooltip>
                </Box>
            )}
        </div>
    )
}

const expansionSpecificCounts = (props: AercCatProps, width: number | undefined): InfoIconValue | undefined => {
    const {deck, cards} = props

    if (deck.expansion === Expansion.GRIM_REMINDERS) {

        const customMatches = []
        if ((deck.artifactCount ?? 0) > 0) {
            customMatches.push(`${deck.artifactCount} Artifacts (less haunted)`)
        }
        if ((deck.actionCount ?? 0) > 0) {
            customMatches.push(`${deck.actionCount} Actions (more haunted)`)
        }
        let discardPips = 0
        cards.forEach(card => discardPips += (card.extraCardInfo?.enhancementDiscard ?? 0))
        if (discardPips > 0) {
            customMatches.push(`${discardPips} Discard Pips (more haunted)`)
        }
        return {
            icon: <HauntedIcon width={width}/>,
            info: `${deck.hauntingOdds ?? -1}`,
            cardsTips: {
                title: "Haunted Odds",
                cards,
                subtitle1: "Mills Friendly",
                matches: card => card.extraCardInfo?.traits
                    ?.find(trait => {
                        if (trait.player === SynTraitPlayer.ENEMY) return false
                        return trait.trait === SynergyTrait.mills
                    }) != null,
                subtitle2: "Discards Friendly",
                matches2: card => card.extraCardInfo?.traits
                    ?.find(trait => {
                        if (trait.player === SynTraitPlayer.ENEMY) return false
                        return trait.trait === SynergyTrait.discardsCards
                    }) != null,
                subtitle3: "Cards",
                customMatches
            }
        }
    }

    if (deck.expansion === Expansion.MASS_MUTATION || deck.expansion === Expansion.MORE_MUTATION) {
        return {
            icon: <MutantIcon width={width}/>,
            info: cards.filter(card => card.traits?.includes("MUTANT") ?? false).length,
            cardsTips: {
                matches: card => card.traits?.includes("MUTANT") ?? false,
                cards,
                title: "Mutants"
            }
        }
    }

    if (deck.expansion === Expansion.DARK_TIDINGS) {

        const manipulatesTideCheckCard = (card: FrontendCard) => card.extraCardInfo?.traits?.find(trait => trait.trait === SynergyTrait.lowersTide || trait.trait === SynergyTrait.raisesTide)
        const manipulatesTide = (cards: FrontendCard[]) => cards.filter(manipulatesTideCheckCard)
        const usesTideCheckCard = (card: FrontendCard) => card.extraCardInfo?.synergies?.find(synergy => synergy.trait === SynergyTrait.lowersTide || synergy.trait === SynergyTrait.raisesTide)
        const usesTide = (cards: FrontendCard[]) => cards.filter(usesTideCheckCard)

        return {
            icon: <TideIcon width={width}/>,
            info: `${manipulatesTide(cards).length}/${usesTide(cards).length}`,
            cardsTips: {
                matches: card => manipulatesTideCheckCard(card) != null,
                matches2: card => usesTideCheckCard(card) != null,
                cards,
                title: "Tide",
                subtitle1: "Manipulates Tide",
                subtitle2: "Uses Tide"
            }
        }
    }

    if (deck.expansion === Expansion.PROPHETIC_VISIONS) {

        const fullProphecyCards = deck.prophecies?.map(simpleProphecy => {
            const fullCard = cardStore.fullCardFromCardName(simpleProphecy.cardTitle)!!
            return cardStore.findExtraInfoToUse(fullCard)
        }) ?? []

        const prophecies: { prophecyName: string, prophecyOdds: number }[] = fullProphecyCards.map(extraInfo => {
            const prophecy = extraInfo.traits?.find(trait => trait.trait === SynergyTrait.prophecy)

            if (prophecy == null) {
                return {prophecyName: extraInfo.cardName, prophecyOdds: 0}
            }

            let value = 0
            if (prophecy.rating === 0) {
                value = 5
            } else if (prophecy.rating === 1) {
                value = 10
            } else if (prophecy.rating === 2) {
                value = 15
            } else if (prophecy.rating === 3) {
                value = 20
            } else if (prophecy.rating === 4) {
                value = 25
            }
            return {prophecyName: extraInfo.cardName, prophecyOdds: value}
        })

        const prophecyOdds = prophecies.map(prophecy => prophecy.prophecyOdds)
            .reduce((a, b) => a + b, 0)

        const fatesCheckCard = (card: FrontendCard) => card.extraCardInfo?.traits?.find(trait => trait.trait === SynergyTrait.fate)
        const fates = (cards: FrontendCard[]) => cards.filter(fatesCheckCard)

        return {
            icon: <ProphecyIcon width={width}/>,
            info: `${prophecyOdds}/${fates(cards).length}`,
            cardsTips: {
                customMatches: prophecies.map(prophecy => `${prophecy.prophecyName}: ${prophecy.prophecyOdds}%`),
                matches: card => fatesCheckCard(card) != null,
                cards,
                title: "Prophecy",
                subtitle1: "Fates",
                subtitle3: "Prophetic Frequency"
            }
        }
    }

    const expansionInfo = expansionInfoMap.get(deck.expansion)
    if (expansionInfo?.hasTokens) {

        const makesTokensFakeCombos = cards
            .map(card => {
                const makesTokens = card.extraCardInfo?.traits?.find(trait => trait.trait === SynergyTrait.makesTokens)
                if (makesTokens == null) {
                    return null
                }
                let value = 0
                if (makesTokens.rating === 1) {
                    value = 1
                } else if (makesTokens.rating === 2) {
                    value = 1.5
                } else if (makesTokens.rating === 3) {
                    value = 2
                } else if (makesTokens.rating === 4) {
                    value = 3
                }
                return {
                    other: value,
                    cardName: card.cardTitle,
                    copies: 1,

                }
            })
            .filter(combo => combo != null) as SynergyCombo[]

        return {
            icon: <TokenIcon width={width}/>,
            info: `${deck.tokenCreationValues?.tokensPerGame ?? 0}`,
            combosTips: {
                title: "Token Generation",
                combos: makesTokensFakeCombos,
                accessor: combo => combo.other,
            }
        }
    }

    return undefined
}

interface AercScoresCategoryProps {
    hasAerc: HasAerc
    combos: SynergyCombo[]
    twoHigh?: boolean
}

export const AercCategoryAmber = (props: AercScoresCategoryProps) => {
    const {hasAerc, combos} = props
    return (
        <AercCategory
            name={"Aember"}
            infos={
                [
                    {
                        icon: <AercIcon type={AercType.A}/>,
                        info: hasAerc.amberControl,
                        combosTips: {
                            title: "Aember Control (A)",
                            combos: combos.filter(combo => combo.amberControl != null && combo.amberControl != 0),
                            accessor: combo => combo.amberControl
                        }
                    },
                    {
                        icon: <AercIcon type={AercType.E}/>,
                        info: hasAerc.expectedAmber,
                        combosTips: {
                            title: "Expected Aember (E)",
                            combos: combos?.filter(combo => combo.expectedAmber != null && combo.expectedAmber != 0) ?? [],
                            accessor: combo => combo.expectedAmber
                        }
                    },
                ]
            }
        />
    )
}

export const AercCategorySpeed = (props: AercScoresCategoryProps) => {
    const {hasAerc, combos, twoHigh} = props

    const speeds = [
        {
            icon: <AercIcon type={AercType.F}/>,
            info: hasAerc.efficiency ?? 0,
            combosTips: {
                title: "Efficiency (F)",
                combos: combos?.filter(combo => combo.efficiency != null && combo.efficiency != 0),
                accessor: (combo: SynergyCombo) => combo.efficiency
            }
        },
        {
            icon: <AercIcon type={AercType.U}/>,
            info: hasAerc.recursion ?? 0,
            combosTips: {
                title: "Recursion (U)",
                combos: combos?.filter(combo => combo.recursion != null && combo.recursion != 0),
                accessor: (combo: SynergyCombo) => combo.recursion
            }
        },
    ]

    if (!twoHigh) {
        speeds.push({
            icon: <AercIcon type={AercType.D}/>,
            info: hasAerc.disruption ?? 0,
            combosTips: {
                title: "Disruption (D)",
                combos: combos?.filter(combo => combo.disruption != null && combo.disruption != 0),
                accessor: (combo: SynergyCombo) => combo.disruption
            }
        })
    }

    return (
        <AercCategory
            name={"Speed"}
            infos={speeds}
        />
    )
}

export const AercCategoryControl = (props: AercScoresCategoryProps) => {
    const {hasAerc, combos} = props
    return (
        <AercCategory
            name={"Control"}
            infos={
                [
                    {
                        icon: <AercIcon type={AercType.R}/>,
                        info: hasAerc.artifactControl ?? 0,
                        combosTips: {
                            title: "Artifact Control (R)",
                            combos: combos.filter(combo => combo.artifactControl != null && combo.artifactControl != 0),
                            accessor: combo => combo.artifactControl
                        }
                    },
                    {
                        icon: <AercIcon type={AercType.C}/>,
                        info: hasAerc.creatureControl ?? 0,
                        combosTips: {
                            title: "Creature Control (C)",
                            combos: combos.filter(combo => combo.creatureControl != null && combo.creatureControl != 0),
                            accessor: combo => combo.creatureControl
                        }
                    },
                ]
            }
        />
    )
}

export const AercCategoryBoard = (props: AercScoresCategoryProps) => {
    const {hasAerc, combos, twoHigh} = props
    const boards: InfoIconValue[] = [
        {
            icon: <AercIcon type={AercType.P}/>,
            info: hasAerc.effectivePower,
            combosTips: {
                title: "Effective Power (P)",
                combos: combos.filter(combo => combo.effectivePower != null && combo.effectivePower != 0),
                accessor: combo => combo.effectivePower
            }
        },
        {
            icon: <AercIcon type={AercType.S}/>,
            info: hasAerc.creatureProtection ?? 0,
            combosTips: {
                title: "Creature Protection",
                combos: combos.filter(combo => combo.creatureProtection != null && combo.creatureProtection != 0),
                accessor: combo => combo.creatureProtection
            }
        }
    ]

    if (!twoHigh) {
        boards.push({
            icon: <AercIcon type={AercType.O}/>,
            info: hasAerc.other ?? 0,
            combosTips: {
                title: "Other",
                combos: combos?.filter(combo => combo.other != null && combo.other != 0) ?? [],
                accessor: combo => combo.other
            }
        })
    }

    return (
        <AercCategory
            name={"Board"}
            infos={boards}
        />
    )
}

export const AercCategoryOther = (props: AercScoresCategoryProps) => {
    const {hasAerc, combos} = props
    return (
        <AercCategory
            name={"Other"}
            infos={
                [
                    {
                        icon: <AercIcon type={AercType.D}/>,
                        info: hasAerc.disruption ?? 0,
                        combosTips: {
                            title: "Disruption (D)",
                            combos: combos.filter(combo => combo.disruption != null && combo.disruption != 0),
                            accessor: combo => combo.disruption
                        }
                    },
                    {
                        icon: <AercIcon type={AercType.O}/>,
                        info: hasAerc.other ?? 0,
                        combosTips: {
                            title: "Other (O)",
                            combos: combos.filter(combo => combo.other != null && combo.other != 0),
                            accessor: combo => combo.other
                        }
                    },
                ]
            }
        />
    )
}

const AercCategory = (props: { name: string, small?: boolean, infos: InfoIconValue[] }) => {
    return (
        <div>
            <div
                style={{
                    display: "flex",
                    justifyContent: "center"
                }}
            >
                <Typography variant={"overline"}>
                    {props.name}
                </Typography>
            </div>
            <InfoIconList values={props.infos} small={props.small}/>
        </div>
    )
}
