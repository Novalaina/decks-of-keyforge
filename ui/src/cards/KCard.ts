import { round } from "lodash"
import { HasAerc } from "../aerc/HasAerc"
import { Utils } from "../config/Utils"
import { activeSasExpansions } from "../expansions/Expansions"
import { Expansion } from "../generated-src/Expansion"
import { SimpleCard } from "../generated-src/SimpleCard"
import { SynergyTrait } from "../generated-src/SynergyTrait"
import { SynTraitPlayer } from "../generated-src/SynTraitPlayer"
import { CsvData } from "../generic/CsvDownloadButton"
import { statsStore } from "../stats/StatsStore"
import { synTraitValueToString } from "../synergy/SynTraitValue"
import { FrontendCard } from "../generated-src/FrontendCard"

const cardNameReplacementRegex = /[^\d\w\s]/g
const spaceRegex = /\s/g

/**
 * Equivalent to cardNameUrl on the backend
 * @param name
 */
export const cardNameToCardNameKey = (name: string) => {
    return name
        .trim()
        .replace(cardNameReplacementRegex, "")
        .replace(spaceRegex, "-")
        .toLowerCase()
}

export interface CardWinRates {
    expansion?: Expansion
    winRatePercent?: number
    relativeToAveragePercent?: number
    wins?: number
    losses?: number
}

export const minCardWinsToDisplay = 250

export class CardUtils {

    static bonusIconCount = (card: SimpleCard): number => {
        return (card.bonusAember ?? 0) + (card.bonusDamage ?? 0) + (card.bonusDraw ?? 0) + (card.bonusCapture ?? 0)
            + (card.bonusDiscard ?? 0)
            + (card.bonusHouses?.length ?? 0)
    }

    static cardMatchesFriendlyTrait = (card: FrontendCard, trait: SynergyTrait): boolean => {
        return card.extraCardInfo.traits.find(traitValue =>
            (traitValue.trait === trait && traitValue.player !== SynTraitPlayer.ENEMY)
        ) != null
    }

    static findCardsWithFriendlyTrait = (cards: FrontendCard[], trait: SynergyTrait): FrontendCard[] => {
        return cards.filter(card => CardUtils.cardMatchesFriendlyTrait(card, trait))
    }

    static cardAverageRelativeWinRate = (card: FrontendCard): number => {

        const winRates = CardUtils.cardWinRates(card)
            .map(winRate => winRate.relativeToAveragePercent)
            .filter(rate => rate != null)

        if (winRates.length === 0) {
            return 0
        }

        return winRates.reduce(function (prev, current) {
            return prev! + current!
        })! / winRates.length
    }

    static cardWinRates = (card: FrontendCard): CardWinRates[] => {
        const winRates: CardWinRates[] = []
        const expansionWins = card.expansions
        if (expansionWins != null && card.houses.length > 0) {
            activeSasExpansions.forEach(expansion => {
                const wins = expansionWins.find(cardExpInfo => cardExpInfo.expansion === expansion)
                if (wins != null && (wins.wins + wins.losses > minCardWinsToDisplay)) {
                    const winRatePercent = CardUtils.calcWinRate(wins.wins, wins.losses)
                    const expHouseWinPercent = statsStore.winRateForExpansionAndHouse(expansion, card.houses[0])
                    let relativeToAveragePercent
                    if (expHouseWinPercent != null) {
                        relativeToAveragePercent = round(winRatePercent - expHouseWinPercent, 1)
                    }
                    winRates.push({
                        expansion,
                        wins: wins.wins,
                        losses: wins.losses,
                        winRatePercent: round(winRatePercent, 1),
                        relativeToAveragePercent
                    })
                }
            })
        }

        if (card.winRate != null && card.winRate > 0) {
            winRates.unshift({
                winRatePercent: card.winRate,
                wins: card.wins,
                losses: card.losses,
            })
        }

        return winRates
    }

    static fakeRatingFromAerc = (card: HasAerc) => {
        const aerc = card.averageAercScore!
        if (aerc <= 0.5) {
            return 0
        } else if (aerc <= 1) {
            return 1
        } else if (aerc <= 2) {
            return 2
        } else if (aerc <= 3) {
            return 3
        }
        return 4
    }

    static arrayToCSV = (cards: FrontendCard[]): CsvData => {
        const data = cards.map(card => {

            return [
                card.cardTitle,
                card.houses,
                card.expansions.map(expansions => `${expansions.expansion} – ${expansions.cardNumber} – ${expansions.rarity}`),
                card.cardType,
                card.extraCardInfo.aercScore,
                card.extraCardInfo.aercScoreMax,
                card.extraCardInfo.amberControl,
                card.extraCardInfo.amberControlMax,
                card.extraCardInfo.expectedAmber,
                card.extraCardInfo.expectedAmberMax,
                card.extraCardInfo.creatureProtection,
                card.extraCardInfo.creatureProtectionMax,
                card.extraCardInfo.artifactControl,
                card.extraCardInfo.artifactControlMax,
                card.extraCardInfo.creatureControl,
                card.extraCardInfo.creatureControlMax,
                card.extraCardInfo.effectivePower,
                card.extraCardInfo.effectivePowerMax,
                card.extraCardInfo.efficiency,
                card.extraCardInfo.recursion,
                card.extraCardInfo.efficiencyMax,
                card.extraCardInfo.recursionMax,
                card.extraCardInfo.disruption,
                card.extraCardInfo.disruptionMax,
                card.extraCardInfo.other,
                card.extraCardInfo.otherMax,

                card.amber,
                card.power,
                card.armor,
                card.wins,
                card.losses,

                card.extraCardInfo.traits.map(trait => synTraitValueToString(trait)),
                card.extraCardInfo.synergies.map(value => synTraitValueToString(value)),
                card.traits,
                Utils.removeLineBreaks(card.cardText),
                Utils.removeLineBreaks(card.flavorText ?? ""),
            ]
        })
        data.unshift([
            "Name",
            "House",
            "Expansion – # – Rarity",
            "Type",
            "Aerc Min",
            "Aerc Max",
            "Amber Control",
            "Amber Control Max",
            "Expected Amber",
            "Expected Amber Max",
            "Creature Protection",
            "Creature Protection Max",
            "Artifact Control",
            "Artifact Control Max",
            "Creature Control",
            "Creature Control Max",
            "Effective Power",
            "Effective Power Max",
            "Efficiency",
            "Efficiency Max",
            "Recursion",
            "Recursion Max",
            "Disruption",
            "Disruption Max",
            "Other",
            "Other Max",

            "Raw Amber",
            "Power",
            "Armor",

            "Wins",
            "Losses",

            "SAS traits",
            "Synergies",

            "Traits",
            "Text",
            "Flavor Text"
        ])
        return data
    }

    private static calcWinRate = (wins: number, losses: number) => {
        return (wins / (wins + losses)) * 100
    }
}
