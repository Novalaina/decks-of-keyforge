export class SearchFiltersBuilder<T extends object> {

    private searchParams: URLSearchParams
    private filters: T

    constructor(queryString: string, build: T) {
        this.searchParams = new URLSearchParams(queryString)
        this.filters = build
    }

    value = (name: string) => {
        if (this.searchParams.has(name)) {
            const val = this.searchParams.get(name)!
            if (val === "true" || val === "false") {
                (this.filters as any)[name] = (val === "true")
            } else if (!isNaN(+val)) {
                (this.filters as any)[name] = Number(val)
            } else if (val != null) {
                (this.filters as any)[name] = val
            }
        }
        return this
    }

    stringValue = (name: string) => {
        if (this.searchParams.has(name)) {
            const val = this.searchParams.get(name)!
            if (val != null) {
                (this.filters as any)[name] = val
            }
        }
        return this
    }

    stringArrayValue = (name: string) => {
        if (this.searchParams.has(name) && this.searchParams.getAll(name).length > 0) {
            (this.filters as any)[name] = this.searchParams.getAll(name)
        } else {
            (this.filters as any)[name] = []
        }
        return this
    }

    numberArrayValue = (name: string) => {
        if (this.searchParams.has(name) && this.searchParams.getAll(name).length > 0) {
            (this.filters as any)[name] = this.searchParams.getAll(name)
                .map(value => Number(value))
        } else {
            (this.filters as any)[name] = []
        }
        return this
    }

    customArrayValue = (name: string, convert: (val: string) => unknown) => {
        if (this.searchParams.has(name) && this.searchParams.getAll(name).length > 0) {
            (this.filters as any)[name] = this.searchParams.getAll(name)
                .map(value => convert(value))
        } else {
            (this.filters as any)[name] = []
        }
        return this
    }

    build = (): T => {
        return this.filters
    }

}

export const queryParamsFromObject = (obj: any): string => {
    let query = ""
    for (const property in obj) {
        const val = obj[property]
        if (Array.isArray(val)) {
            query += val.map(arrVal => `${property}=${arrVal}`).join("&")
        } else {
            query += `${property}=${val}`
        }
        query += "&"
    }
    if (query.length > 0) {
        query = query.substring(0, query.length - 1)
    }
    return query
}