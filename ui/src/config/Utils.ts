import * as Bowser from "bowser"
import { round } from "lodash"
import * as loglevel from "loglevel"

export const log = loglevel
log.setDefaultLevel("debug")

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const prettyJson = (write: any): string => JSON.stringify(write, null, 2)

export const roundToInt = (round: number) => Math.round(round)
export const roundToTens = (round: number) => Math.round(round * 10) / 10
export const roundToHundreds = (round?: number) => round == null ? 0 : Math.round(round * 100) / 100
export const roundToThousands = (round: number) => Math.round(round * 1000) / 1000

export class Utils {

    static readonly bowser = Bowser.getParser(window.navigator.userAgent)

    static pseudoUuid = () => {
        return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
            const r = Math.random() * 16 | 0, v = c == "x" ? r : (r & 0x3 | 0x8)
            return v.toString(16)
        })
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    static equals = (first: any, second: any): boolean => {
        return JSON.stringify(first) === JSON.stringify(second)
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    static enumValues<T extends EnumType>(enunn: any): T[] {
        return Object.keys(enunn).filter(key => isNaN(+key)).map(name => enunn[name]) as T[]
    }

    static validateEmail = (email: string) => {
        // eslint-disable-next-line
        const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
        return re.test(String(email).toLowerCase())
    }

    static validateUrl = (url: string) => {
        // eslint-disable-next-line
        const re = /^(http|https):\/\/[^ "]+$/
        return re.test(String(url).toLowerCase())
    }

    static validateDiscordServer = (discordServerLink: string) => {
        // eslint-disable-next-line
        const re = /^https:\/\/discord.gg\/[a-zA-Z0-9]+$/
        return re.test(String(discordServerLink).toLowerCase())
    }

    static toNumberOrUndefined = (num?: number | string) => num == null || num === 0 || num === "" || num === "0" ? undefined : Number(num)

    static isDev = () => process.env.NODE_ENV === "development"

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    static jsonCopy = (toCopy: any) => {
        return JSON.parse(JSON.stringify(toCopy))
    }

    static valueFromPercentiles = (value: number, percentiles: { [key: number]: number }) => {
        const percentile = percentiles[Math.round(value)]
        if (percentile != null) {
            return Math.round(percentile)
        }
        return 0
    }

    static shareUrl = (url: string, title?: string) => {
        try {
            // @ts-ignore
            navigator.share({
                title,
                url
            })
        } catch (e) {
            log.debug(`Couldn't share with bowser info ${prettyJson(Utils.bowser.getBrowser())} `, e)
        }
    }

    static canShare = () => {
        return Utils.bowser
            .satisfies({
                mobile: {
                    safari: ">=12.2",
                    chrome: ">=61"
                },
                macos: {
                    safari: ">=12.1"
                },
            })
    }

    static roundToKs = (toRound: number) => {
        if (toRound < 1000) {
            return toRound
        }
        return round(toRound / 1000, 0) + "k"
    }

    static cardNameIncludes = (cardName: string, search: string) => {
        if (search.trim().length < 3) {
            return true
        }
        const cardNameClean = cardName
            .trim()
            .toLowerCase()
            .replace(/æ/g, "ae")
            .replace(/\W+/g, "")

        const searchTokenized = Utils.tokenizeCardSearch(search)

        let match = true
        searchTokenized.forEach(searchToken => {
            if (!cardNameClean.includes(searchToken)) {
                match = false
            }
        })
        return match
    }

    static tokenizeCardSearch = (search: string) => {
        return search
            .trim()
            .toLowerCase()
            .replace(/\W+/g, " ")
            .split(" ")
            .filter(token => token.length > 2)
    }

    static filenameExtension = (file: File) => {
        const filename = file.name
        if (!filename.includes(".")) {
            return ""
        }
        return filename.split(".").pop() ?? ""
    }

    static removeLineBreaks = (toModify: string) => {
        return toModify.replace(/(\r\n|\n|\r)/gm, " ")
    }

    static enumNameToReadable = (enumName: string) => {
        let lowered = enumName.toLowerCase()
        lowered = lowered.slice(0, 1).toUpperCase() + lowered.slice(1)
        for (let x = 0; x < lowered.length; x++) {
            if (lowered[x] === "_" && lowered.length > x + 1) {
                lowered = lowered.slice(0, x) + " " + lowered[x + 1].toUpperCase() + lowered.slice(x + 2)
            }
        }
        return lowered
    }

    static findUuid = (findIn: string) => {
        // only works for simple urls atm
        const splitOnSlash = findIn.split("/")
        return splitOnSlash[splitOnSlash.length - 1]
    }

    static camelCaseToTitleCase = (convert: string) => {
        const result = convert.replace(/([A-Z])/g, " $1")
        return result.charAt(0).toUpperCase() + result.slice(1)
    }

    static generateTraitsTypeList = (toJoin: string[], addToValues?: string): string => {
        let joined = ""
        for (let idx = 0; idx < toJoin.length; idx++) {
            const joinValue = Utils.camelCaseToTitleCase(toJoin[idx].toLowerCase()) + (addToValues ?? "")
            if (idx === toJoin.length - 1) {
                // last
                joined += joinValue
            } else if (idx === toJoin.length - 2) {
                // second to last
                joined += joinValue + " and "
            } else {
                joined += joinValue + ", "
            }
        }
        return joined
    }

}

type EnumType = string | number

export type SortOrder = "desc" | "asc"
