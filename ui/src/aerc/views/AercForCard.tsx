import {Box, Typography} from "@material-ui/core"
import * as React from "react"
import {spacing, theme} from "../../config/MuiConfig"
import {roundToHundreds, roundToTens} from "../../config/Utils"
import {CardType} from "../../generated-src/CardType"
import {SynergyCombo} from "../../generated-src/SynergyCombo"
import {ExtraCardInfoUtils} from "../../extracardinfo/ExtraCardInfoUtils"
import {FrontendCard} from "../../generated-src/FrontendCard"

export const AercForCard = (props: {
    card: FrontendCard,
    short?: boolean,
    realValue?: SynergyCombo,
    copies?: number
}) => {
    const {card, short, realValue, copies} = props
    const info = card.extraCardInfo
    let effectivePower = info.effectivePower
    if (effectivePower === 0 && (card.cardType === CardType.Creature || card.cardType === CardType.TokenCreature)) {
        effectivePower = card.power
    }
    return (
        <div style={{display: "grid", gridTemplateColumns: "6fr 4fr 2fr"}}>
            <AercScore
                score={info.amberControl}
                max={info.amberControlMax}
                synergizedScore={realValue && realValue.amberControl}
                name={short ? "A" : "Aember Control (A)"}
            />
            <AercScore
                score={info.expectedAmber}
                max={info.expectedAmberMax}
                synergizedScore={realValue && realValue.expectedAmber}
                name={short ? "E" : "Expected Aember (E)"}
            />
            <AercScore
                score={info.artifactControl}
                max={info.artifactControlMax}
                synergizedScore={realValue && realValue.artifactControl}
                name={short ? "R" : "Artifact Control (R)"}
            />
            <AercScore
                score={info.creatureControl}
                max={info.creatureControlMax}
                synergizedScore={realValue && realValue.creatureControl}
                name={short ? "C" : "Creature Control (C)"}
            />
            <AercScore
                score={roundToTens((effectivePower) / 10)}
                max={info.effectivePowerMax != null ? roundToTens(info.effectivePowerMax / 10) : undefined}
                synergizedScore={realValue && roundToTens(realValue.effectivePower / 10)}
                name={short ? "P" : "Effective Power (P)"}
            />
            {(card.cardType === CardType.Creature || card.cardType === CardType.TokenCreature) && (
                <AercScore
                    score={ExtraCardInfoUtils.creatureBonus}
                    singleColumn={realValue != null}
                    name={short ? "CB" : "Creature Bonus"}
                />
            )}
            <AercScore
                score={info.efficiency}
                max={info.efficiencyMax}
                synergizedScore={realValue && realValue.efficiency}
                name={short ? "F" : "Efficiency (F)"}
            />
            <AercScore
                score={info.recursion}
                max={info.recursionMax}
                synergizedScore={realValue && realValue.recursion}
                name={short ? "U" : "Recursion (U)"}
            />
            <AercScore
                score={info.disruption}
                max={info.disruptionMax}
                synergizedScore={realValue && realValue.disruption}
                name={short ? "D" : "Disruption (D)"}
            />
            <AercScore
                score={info.creatureProtection}
                max={info.creatureProtectionMax}
                synergizedScore={realValue && realValue.creatureProtection}
                name={short ? "CP" : "Creature Protection"}
            />
            <AercScore
                score={info.other}
                max={info.otherMax}
                synergizedScore={realValue && realValue.other}
                name={short ? "O" : "Other"}
            />
            {realValue == null ? (
                <AercScore
                    score={ExtraCardInfoUtils.minAERC(card.cardType, info)}
                    max={ExtraCardInfoUtils.maxAERC(card.cardType, info)}
                    name={"AERC"}
                />
            ) : (
                <AercAercScore score={realValue.aercScore} synergy={realValue.netSynergy}/>
            )}
            {copies != null && realValue?.aercScore != null && (
                <>
                    <Typography
                        variant={"body2"}
                        color={"textSecondary"}
                        style={{marginTop: theme.spacing(0.5), marginRight: spacing(1)}}
                    >
                        AERC for {copies} tokens:
                    </Typography>
                    <Box/>
                    <Typography
                        variant={"body2"} color={"textSecondary"}
                        style={{marginTop: theme.spacing(0.5), marginRight: spacing(1)}}
                    >
                        {roundToHundreds(copies * realValue.aercScore)}
                    </Typography>
                </>
            )}
        </div>
    )
}

const AercScore = (props: {
    score: number,
    max?: number,
    name: string,
    synergizedScore?: number,
    singleColumn?: boolean
}) => {
    const {score, max, name, synergizedScore, singleColumn} = props
    if (score === 0 && max == null && (synergizedScore == null || synergizedScore === 0)) {
        return null
    }
    let secondColumn
    if (max != null) {
        const scoreRounded = roundToHundreds(score)
        const maxRounded = roundToHundreds(max)
        if (scoreRounded === maxRounded) {
            secondColumn = `${scoreRounded}`
        } else {
            secondColumn = `${scoreRounded} to ${maxRounded}`
        }
    } else if (max == null && synergizedScore == null && !singleColumn) {
        secondColumn = roundToHundreds(score)
    }
    let thirdColumn = max == null && synergizedScore != null ? score : synergizedScore

    if (max == null && synergizedScore != null && score === 0) {
        thirdColumn = synergizedScore
    }

    if (thirdColumn != null) {
        thirdColumn = roundToHundreds(thirdColumn)
    } else if (singleColumn) {
        thirdColumn = roundToHundreds(score)
    }
    return (
        <>
            <Typography variant={"body2"} color={"textSecondary"}
                        style={{marginTop: theme.spacing(0.5), marginRight: spacing(1)}}>
                {name}:
            </Typography>
            <Typography variant={"body2"} color={"textSecondary"} style={{marginTop: theme.spacing(0.5)}}>
                {secondColumn}
            </Typography>
            <Typography variant={"body2"} color={"textSecondary"} style={{marginTop: theme.spacing(0.5)}}>
                {thirdColumn}
            </Typography>
        </>
    )
}

const AercAercScore = (props: { score: number, synergy: number }) => {
    const {score, synergy} = props
    if (score === 0) {
        return null
    }
    return (
        <>
            <Typography variant={"body2"} color={"textSecondary"}
                        style={{marginTop: theme.spacing(0.5), marginRight: spacing(1)}}>
                AERC:
            </Typography>
            <Typography variant={"body2"} color={"textSecondary"} style={{marginTop: theme.spacing(0.5)}}>
                {synergy === 0 ? "" : `${roundToHundreds(score - synergy)} ${synergy > 0 ? "+" : "-"} ${roundToHundreds(Math.abs(synergy))} =`}
            </Typography>
            <Typography variant={"body2"} color={"textSecondary"} style={{marginTop: theme.spacing(0.5)}}>
                {roundToHundreds(score)}
            </Typography>
        </>
    )
}
