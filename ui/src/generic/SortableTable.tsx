import { Box, Paper, TableSortLabel, Tooltip, Typography } from "@material-ui/core"
import Table from "@material-ui/core/Table"
import TableBody from "@material-ui/core/TableBody"
import TableCell from "@material-ui/core/TableCell"
import TableHead from "@material-ui/core/TableHead"
import TableRow from "@material-ui/core/TableRow"
import { sortBy, startCase } from "lodash"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import React from "react"
import { SortOrder } from "../config/Utils"


export interface SortableTableHeaderInfo<T> {
    /**
     * Either property or transform must be included. If property is not included and sortable is not false, sortWith must be included.
     *
     * Property, title or key must be included and unique.
     */
    property?: keyof T
    title?: string
    key?: string

    titleNode?: React.ReactNode

    /**
     * Default true
     */
    sortable?: boolean

    /**
     * Alternative value to sort by instead of property. Property must still be included as a key to tell what is sorted.
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    sortFunction?: TransformTableData<T>

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    transform?: TransformTableData<T>

    /**
     * property must be included with transformProperty
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    transformProperty?: (propertyValue: any) => any
    width?: number,
    hide?: boolean,
    padding?: "checkbox" | "default" | "none"
}

interface SortableTableProps<T> {
    headers: SortableTableHeaderInfo<T>[]
    data: T[]

    /**
     * Should be property or title of the header
     */
    defaultSort: string | keyof T
    defaultSortDir?: SortOrder
    defaultSortFunction?: TransformTableData<T>
    noInitialSort?: boolean
    noOverflow?: boolean
    limit?: number
    rowBackgroundColor?: (data: T) => string | undefined
}

@observer
export class SortableTable<T> extends React.Component<SortableTableProps<T>> {

    // @ts-ignore
    store: SortableTableStore<T>

    constructor(props: SortableTableProps<T>) {
        super(props)
        this.updateStore(props)
    }

    componentDidUpdate(prevProps: SortableTableProps<T>) {
        if (prevProps.data !== this.props.data) {
            this.updateStore(this.props)
        }
    }

    updateStore = (props: SortableTableProps<T>) => {
        const defaultSortHeader = props.headers.find(header => header.property === props.defaultSort || header.title === props.defaultSort)

        if (this.store == null) {
            this.store = new SortableTableStore<T>(props.data, defaultSortHeader!, !!props.noInitialSort, props.defaultSortDir)
        } else {
            this.store.update(props.data, defaultSortHeader!, !!props.noInitialSort, props.defaultSortDir)
        }
    }

    render() {
        const store = this.store
        const {headers, noOverflow, limit, rowBackgroundColor} = this.props
        const usableHeaders = headers.filter(header => header.hide !== true)

        return (
            <div style={{overflowX: noOverflow ? undefined : "auto"}}>
                <Table size={"small"}>
                    <TableHead>
                        <TableRow>
                            {usableHeaders.map((header, idx) => (
                                <TableCell
                                    key={header.property?.toString() ?? header.title?.toString() ?? header.key ?? idx}
                                    style={{width: header.width}}
                                    padding={header.padding}
                                >
                                    {(header.property != null || header.sortFunction != null) && header.sortable !== false ? (
                                        <TableSortLabel
                                            active={store.activeTableSort === header.property && store.sortFunctionName === header.title}
                                            direction={store.tableSortDir}
                                            onClick={store.changeSortHandler(header) as () => void}
                                        >
                                            {header.titleNode}{header.title ?? startCase(header.property as string)}
                                        </TableSortLabel>
                                    ) : (
                                        <>{header.titleNode}{header.title}</>
                                    )}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {(limit == null ? store.sortedItems : store.sortedItems.slice(0, limit)).map((datum, idx) => (
                            <TableRow
                                key={idx}
                                style={{backgroundColor: rowBackgroundColor == null ? undefined : rowBackgroundColor(datum)}}
                            >
                                {usableHeaders.map(header => {
                                    let value
                                    if (header.transform != null) {
                                        value = header.transform(datum)
                                    } else if (header.transformProperty != null) {
                                        value = header.transformProperty(datum[header.property!])
                                    } else {
                                        value = datum[header.property!]
                                    }
                                    return (
                                        <TableCell
                                            key={header.property?.toString() ?? header.title?.toString() ?? header.key ?? idx}
                                            padding={header.padding}
                                        >
                                            <div style={{width: header.width}}>
                                                {value}
                                            </div>
                                        </TableCell>
                                    )
                                })}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
        )
    }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type TransformTableData<T> = (data: T) => any

class SortableTableStore<T> {

    @observable
    activeTableSort?: keyof T

    sortFunction?: TransformTableData<T>
    sortFunctionName?: React.ReactNode

    @observable
    tableSortDir: SortOrder = "asc"

    @observable
        // @ts-ignore
    sortedItems: T[]

    constructor(private data: T[], private defaultSortHeader: SortableTableHeaderInfo<T>, private noInitialSort: boolean, private initialSortOrder?: SortOrder) {
        makeObservable(this)
        this.update(data, defaultSortHeader, noInitialSort, initialSortOrder == null ? "desc" : initialSortOrder)
    }

    update = (data: T[], defaultSortHeader: SortableTableHeaderInfo<T>, noInitialSort: boolean, initialSortOrder?: SortOrder) => {
        if (initialSortOrder != null) {
            this.tableSortDir = initialSortOrder
        }
        this.activeTableSort = undefined

        this.sortedItems = data
        const sortHandler = this.changeSortHandler(defaultSortHeader)
        sortHandler(noInitialSort, true)
    }

    resort = () => {
        if (this.sortedItems) {
            this.sortedItems = sortBy(this.sortedItems.slice(), this.sortFunction == null ? this.activeTableSort! : this.sortFunction)
            if (this.tableSortDir === "desc") {
                this.sortedItems = this.sortedItems.slice().reverse()
            }
        }
    }

    changeSortHandler = (header: SortableTableHeaderInfo<T>) => {
        const {property, sortFunction, title} = header
        return (noResort?: boolean, noSwapDir?: boolean) => {
            if ((property != null && this.activeTableSort === property) || (title != null && this.sortFunctionName === title)) {
                if (!noSwapDir) {
                    this.tableSortDir = this.tableSortDir === "desc" ? "asc" : "desc"
                }
            }

            if (this.tableSortDir == null) {
                this.tableSortDir = "desc"
            }
            this.activeTableSort = property
            this.sortFunction = sortFunction
            this.sortFunctionName = title

            // log.info(`Table sort called. \nNo resort? ${noResort} \nhas sortFunction? ${this.sortFunction != null} \nsortFunctionName? ${this.sortFunctionName} \nactive table sort ${this.activeTableSort} \nsort dir ${this.tableSortDir}`)

            if (noResort !== true) {
                this.resort()
            }
        }
    }
}

export const SortableTableContainer = (props: {
    title: string,
    children: React.ReactNode,
    controls?: React.ReactNode,
    titleTooltip?: string
}) => {
    const {title, children, controls, titleTooltip} = props
    return (
        <Box>
            <Paper style={{overflowX: "auto"}}>
                <Box display={"flex"} p={2}>
                    <Tooltip title={titleTooltip ?? ""}>
                        <Typography variant={"h5"}>{title}</Typography>
                    </Tooltip>
                    <Box flexGrow={1}/>
                    {controls}
                </Box>
                {children}
            </Paper>
        </Box>
    )
}
