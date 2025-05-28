import {DeckSaleInfo} from "../../generated-src/DeckSaleInfo"
import {Expansion} from "../../generated-src/Expansion"
import {House} from "../../generated-src/House"
import {HouseAndCards} from "../../generated-src/HouseAndCards"
import {SynergyCombo} from "../../generated-src/SynergyCombo"
import {SynergyTrait} from "../../generated-src/SynergyTrait"
import {DeckType} from "../../generated-src/DeckType"
import {AllianceHouseInfo} from "../../generated-src/AllianceHouseInfo"
import {TokenInfo} from "../../generated-src/TokenInfo"
import {TokenCreationValues} from "../../generated-src/TokenCreationValues"
import { SimpleCard } from "../../generated-src/SimpleCard"

export interface DeckWithSynergyInfo {
    deck: DeckSearchResult
    cardRatingPercentile: number
    synergyPercentile: number
    antisynergyPercentile: number
    alliance: boolean
}

export interface DeckSearchResult {

    deckType: DeckType
    id: number
    keyforgeId: string
    expansion: Expansion

    name: string

    creatureCount?: number
    actionCount?: number
    artifactCount?: number
    upgradeCount?: number

    powerLevel?: number
    chains?: number
    wins?: number
    losses?: number

    aercScore: number
    previousSasRating?: number
    previousMajorSasRating?: number
    sasRating: number
    synergyRating: number
    antisynergyRating: number
    efficiencyBonus: number

    totalPower: number
    cardDrawCount?: number
    cardArchiveCount?: number
    keyCheatCount?: number
    rawAmber: number
    totalArmor?: number

    forSale?: boolean
    forTrade?: boolean
    wishlistCount?: number
    funnyCount?: number

    lastSasUpdate?: string
    sasPercentile: number

    housesAndCards: HouseAndCards[]
    deckSaleInfo?: DeckSaleInfo[]
    owners?: string[]
    synergyDetails?: SynergyCombo[]
    tokenCreationValues?: TokenCreationValues
    hasOwnershipVerification: boolean
    dateAdded?: string

    amberControl: number
    expectedAmber: number
    artifactControl?: number
    creatureControl: number
    efficiency?: number
    recursion?: number
    effectivePower: number
    creatureProtection?: number
    disruption?: number
    other?: number

    twinId?: string
    tokenInfo?: TokenInfo
    prophecies?: SimpleCard[]
    archonPower?: SimpleCard
    hauntingOdds?: number

    allianceHouses?: AllianceHouseInfo[]
    validAlliance?: boolean
    discoveredBy?: string
}

export interface DeckIdentifyingInfo {
    id: number
    keyforgeId: string
    deckType: DeckType
    name: string
}

export interface TraitInDeckInfo {
    trait: SynergyTrait
    count: number
    strength: number
    houseCounts: Map<House, number>
}

export interface DeckPage {
    decks: DeckSearchResult[]
    page: number
}

export interface DeckCount {
    pages: number
    count: number
}
