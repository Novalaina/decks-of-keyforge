import Typography from "@material-ui/core/Typography/Typography"
import * as React from "react"
import { Expansion } from "../generated-src/Expansion"
import { userStore } from "../user/UserStore"
import { ExpansionIcon } from "./ExpansionIcon"

export interface ExpansionInfo {
    expansionNumber: ExpansionNumber
    name: string
    abbreviation: string
    backendEnum: Expansion
    hasTokens?: boolean
    tournamentIllegal?: boolean
}

export enum ExpansionNumber {
    COTA = 341,
    AOA = 435,
    WC = 452,
    ANOM = 453,
    MM = 479,
    DT = 496,
    WOE = 600,
    UC22 = 601,
    VM23 = 609,
    GR = 700,
    MN24 = 722,
    VM24 = 737,
    AS = 800,
    ToC = 855,
    MoM = 874,
    MCW = 892,
    D = 907,
    VM25 = 939,
    PV = 886,
}

export const displayMyDecksLinksFor = [
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.GRIM_REMINDERS,
    Expansion.AEMBER_SKIES,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.MORE_MUTATION,
]

export const activeExpansions = [
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.GRIM_REMINDERS,
    Expansion.VAULT_MASTERS_2023,
    Expansion.VAULT_MASTERS_2024,
    Expansion.VAULT_MASTERS_2025,
    Expansion.UNCHAINED_2022,
    Expansion.MENAGERIE_2024,
    Expansion.MARTIAN_CIVIL_WAR,
    Expansion.AEMBER_SKIES,
    Expansion.MORE_MUTATION,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.DISCOVERY,
    Expansion.PROPHETIC_VISIONS,
]

export const activeSasExpansions = [
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WORLDS_COLLIDE,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.GRIM_REMINDERS,
    Expansion.UNCHAINED_2022,
    Expansion.VAULT_MASTERS_2023,
    Expansion.MENAGERIE_2024,
    Expansion.VAULT_MASTERS_2024,
    Expansion.AEMBER_SKIES,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.MORE_MUTATION,
    Expansion.DISCOVERY,
    Expansion.VAULT_MASTERS_2025,
]

export const activeCardExpansions = [
    ExpansionNumber.COTA,
    ExpansionNumber.AOA,
    ExpansionNumber.WC,
    ExpansionNumber.ANOM,
    ExpansionNumber.MM,
    ExpansionNumber.DT,
    ExpansionNumber.WOE,
    ExpansionNumber.GR,
    ExpansionNumber.AS,
    ExpansionNumber.ToC,
    ExpansionNumber.MoM,
    ExpansionNumber.D,
    ExpansionNumber.PV,
]

export const expansionsWithCards = [
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.GRIM_REMINDERS,
    Expansion.VAULT_MASTERS_2023,
    Expansion.VAULT_MASTERS_2024,
    Expansion.VAULT_MASTERS_2025,
    Expansion.MENAGERIE_2024,
    Expansion.MARTIAN_CIVIL_WAR,
    Expansion.AEMBER_SKIES,
    Expansion.MORE_MUTATION,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.DISCOVERY,
    Expansion.PROPHETIC_VISIONS,
]

export const recentExpansions = [
    Expansion.PROPHETIC_VISIONS,
    Expansion.AEMBER_SKIES,
    Expansion.GRIM_REMINDERS,
    Expansion.TOKENS_OF_CHANGE,
]

export const activeCardLinksExpansions = [
    Expansion.CALL_OF_THE_ARCHONS,
    Expansion.AGE_OF_ASCENSION,
    Expansion.WORLDS_COLLIDE,
    Expansion.MASS_MUTATION,
    Expansion.DARK_TIDINGS,
    Expansion.WINDS_OF_EXCHANGE,
    Expansion.GRIM_REMINDERS,
    Expansion.AEMBER_SKIES,
    Expansion.TOKENS_OF_CHANGE,
    Expansion.MORE_MUTATION,
    Expansion.PROPHETIC_VISIONS,
]

export const possibleCardExpansionsForExpansion = (exp: ExpansionNumber): ExpansionNumber[] => {
    return activeCardExpansions
        .filter(possibleExpansion => (
            possibleExpansion <= exp || (exp === ExpansionNumber.WC && possibleExpansion === ExpansionNumber.ANOM)
        ))
}

export const displaySas = (expansion: Expansion) => {
    return userStore.displayFutureSas || activeSasExpansions.includes(expansion)
}

export const expansionInfos: ExpansionInfo[] = [
    {expansionNumber: ExpansionNumber.COTA, name: "Call of the Archons", abbreviation: "CotA", backendEnum: Expansion.CALL_OF_THE_ARCHONS},
    {expansionNumber: ExpansionNumber.AOA, name: "Age of Ascension", abbreviation: "AoA", backendEnum: Expansion.AGE_OF_ASCENSION},
    {expansionNumber: ExpansionNumber.WC, name: "Worlds Collide", abbreviation: "WC", backendEnum: Expansion.WORLDS_COLLIDE},
    {expansionNumber: ExpansionNumber.ANOM, name: "Anomalies", abbreviation: "ANOM", backendEnum: Expansion.ANOMALY_EXPANSION},
    {expansionNumber: ExpansionNumber.MM, name: "Mass Mutation", abbreviation: "MM", backendEnum: Expansion.MASS_MUTATION},
    {expansionNumber: ExpansionNumber.DT, name: "Dark Tidings", abbreviation: "DT", backendEnum: Expansion.DARK_TIDINGS},
    {expansionNumber: ExpansionNumber.WOE, name: "Winds of Exchange", abbreviation: "WoE", backendEnum: Expansion.WINDS_OF_EXCHANGE, hasTokens: true},
    {expansionNumber: ExpansionNumber.UC22, name: "Unchained", abbreviation: "UC22", backendEnum: Expansion.UNCHAINED_2022, hasTokens: true, tournamentIllegal: true},
    {expansionNumber: ExpansionNumber.VM23, name: "Vault M 23", abbreviation: "VM23", backendEnum: Expansion.VAULT_MASTERS_2023},
    {expansionNumber: ExpansionNumber.VM24, name: "Vault M 24", abbreviation: "VM24", backendEnum: Expansion.VAULT_MASTERS_2024},
    {expansionNumber: ExpansionNumber.VM25, name: "Vault M 25", abbreviation: "VM25", backendEnum: Expansion.VAULT_MASTERS_2025},
    {expansionNumber: ExpansionNumber.GR, name: "Grim Reminders", abbreviation: "GR", backendEnum: Expansion.GRIM_REMINDERS},
    {expansionNumber: ExpansionNumber.MN24, name: "Menagerie", abbreviation: "MN24", backendEnum: Expansion.MENAGERIE_2024, hasTokens: true, tournamentIllegal: true},
    {expansionNumber: ExpansionNumber.MCW, name: "Martian Civil War", abbreviation: "MCW", backendEnum: Expansion.MARTIAN_CIVIL_WAR, hasTokens: true, tournamentIllegal: true},
    {expansionNumber: ExpansionNumber.AS, name: "Aember Skies", abbreviation: "AS", backendEnum: Expansion.AEMBER_SKIES},
    {expansionNumber: ExpansionNumber.MoM, name: "More Mutation", abbreviation: "MoM", backendEnum: Expansion.MORE_MUTATION},
    {expansionNumber: ExpansionNumber.ToC, name: "Tokens of Change", abbreviation: "ToC", backendEnum: Expansion.TOKENS_OF_CHANGE, hasTokens: true},
    {expansionNumber: ExpansionNumber.D, name: "Discovery", abbreviation: "D", backendEnum: Expansion.DISCOVERY},
    {expansionNumber: ExpansionNumber.PV, name: "Prophetic Visions", abbreviation: "PV", backendEnum: Expansion.PROPHETIC_VISIONS},
]

export const activeExpansionInfos: ExpansionInfo[] = expansionInfos.filter(info => activeExpansions.includes(info.backendEnum))

export const expansionInfoMap: Map<Expansion, ExpansionInfo> = new Map(expansionInfos.map(info => (
    [info.backendEnum, info] as [Expansion, ExpansionInfo]
)))

export const expansionInfoMapNumbers: Map<number, ExpansionInfo> = new Map(expansionInfos.map(info => (
    [info.expansionNumber, info] as [number, ExpansionInfo]
)))

export const expansionToBackendExpansion = (expansion: ExpansionNumber) => expansionInfoMapNumbers.get(expansion)!.backendEnum

export const ExpansionLabel = (props: { expansion: Expansion, width?: number, iconSize?: number }) => {
    const {expansion, width, iconSize} = props

    return (
        <div style={{display: "flex", alignItems: "center"}}>
            <ExpansionIcon expansion={expansion} size={iconSize ?? 32} style={{marginRight: 8}}/>
            <Typography noWrap={false} variant={"body2"} style={{width, fontSize: "0.75rem"}}>
                {expansionInfoMap.get(expansion)!.name}
            </Typography>
        </div>
    )
}

export const expansionNumberForExpansion = (expansion: Expansion) => expansionInfoMap.get(expansion)!.expansionNumber
