import { getMinutes, setMinutes, startOfMinute } from "date-fns"
import format from "date-fns/format"
import parse from "date-fns/parse"
import * as loglevel from "loglevel"

export const log = loglevel
log.setDefaultLevel("debug")

// tslint:disable-next-line
export const prettyJson = (write: any): string => JSON.stringify(write, null, 2)

export class Utils {
    static readonly localDateFormat = "yyyy-MM-dd"

    // tslint:disable-next-line
    static enumValues<T extends EnumType>(enunn: any): T[] {
        return Object.keys(enunn).filter(key => isNaN(+key)).map(name => enunn[name]) as T[]
    }

    static validateEmail = (email: string) => {
        // tslint:disable-next-line
        const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
        return re.test(String(email).toLowerCase())
    }

    static formatDate = (date: string) => format(Utils.parseLocalDate(date), "MMM d, yyyy")

    static parseLocalDate = (date: string) => parse(date, Utils.localDateFormat, new Date())

    static roundToNearestMinutes = (date: Date, interval: number) => {
        const roundedMinutes = Math.floor(getMinutes(date) / interval) * interval
        return setMinutes(startOfMinute(date), roundedMinutes)
    }

    static isDev = () => process.env.NODE_ENV === "development"

    // tslint:disable-next-line
    static jsonCopy = (toCopy: any) => {
        return JSON.parse(JSON.stringify(toCopy))
    }
}

type EnumType = string | number
