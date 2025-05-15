import * as React from "react"
import { EnhancementType } from "../../cards/EnhancementType"
import EnhancedAmber from "../imgs/enhancements/enhanced-aember.png"
import EnhancedCapture from "../imgs/enhancements/enhanced-capture.png"
import EnhancedDamage from "../imgs/enhancements/enhanced-damage.png"
import EnhancedDraw from "../imgs/enhancements/enhanced-draw.png"
import EnhancedDiscard from "../imgs/enhancements/enhanced-discard.png"
import EnhancedBrobnar from "../imgs/enhancements/brobnar.svg"
import EnhancedDis from "../imgs/enhancements/dis.svg"
import EnhancedEkwidon from "../imgs/enhancements/ekwidon.svg"
import EnhancedGeistoid from "../imgs/enhancements/geistoid.svg"
import EnhancedLogos from "../imgs/enhancements/logos.svg"
import EnhancedMars from "../imgs/enhancements/mars.svg"
import EnhancedSkyborn from "../imgs/enhancements/skyborn.svg"
import { House } from "../../generated-src/House"
import { RedemptionEnhancedIcon } from "../imgs/enhancements/RedemptionEnhancedIcon"
import { StarAllianceEnhancedIcon } from "../imgs/enhancements/StarAllianceEnhancedIcon"
import { SanctumEnhancedIcon } from "../imgs/enhancements/SanctumEnhancedIcon"
import { SaurianEnhancedIcon } from "../imgs/enhancements/SaurianEnhancedIcon"
import { ShadowsEnhancedIcon } from "../imgs/enhancements/ShadowsEnhancedIcon"
import { UntamedEnhancedIcon } from "../imgs/enhancements/UntamedEnhancedIcon"

export const EnhancementIconSize = 16

export const EnhancementIcon = (props: { type: EnhancementType | House }) => {
    let iconSrc

    if (props.type === EnhancementType.AEMBER) {
        iconSrc = EnhancedAmber
    } else if (props.type === EnhancementType.CAPTURE) {
        iconSrc = EnhancedCapture
    } else if (props.type === EnhancementType.DAMAGE) {
        iconSrc = EnhancedDamage
    } else if (props.type === EnhancementType.DRAW) {
        iconSrc = EnhancedDraw
    } else if (props.type === EnhancementType.DISCARD) {
        iconSrc = EnhancedDiscard
    } else if (props.type === House.Brobnar) {
        iconSrc = EnhancedBrobnar
    } else if (props.type === House.Dis) {
        iconSrc = EnhancedDis
    } else if (props.type === House.Ekwidon) {
        iconSrc = EnhancedEkwidon
    } else if (props.type === House.Geistoid) {
        iconSrc = EnhancedGeistoid
    } else if (props.type === House.Logos) {
        iconSrc = EnhancedLogos
    } else if (props.type === House.Mars) {
        iconSrc = EnhancedMars
    } else if (props.type === House.Redemption) {
        return <RedemptionEnhancedIcon/>
    } else if (props.type === House.Skyborn) {
        iconSrc = EnhancedSkyborn
    } else if (props.type === House.Sanctum) {
        return <SanctumEnhancedIcon/>
    } else if (props.type === House.Saurian) {
        return <SaurianEnhancedIcon/>
    } else if (props.type === House.Shadows) {
        return <ShadowsEnhancedIcon/>
    } else if (props.type === House.StarAlliance) {
        return <StarAllianceEnhancedIcon/>
    } else if (props.type === House.Untamed) {
        return <UntamedEnhancedIcon/>
    } else {
        throw new Error(`No enhancement icon for ${props.type}`)
    }

    return (
        <img alt={props.type} src={iconSrc} style={{width: EnhancementIconSize, height: EnhancementIconSize}}/>
    )
}
