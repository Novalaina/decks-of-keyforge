import { Card, Typography } from "@material-ui/core"
import { ResponsiveBar } from "@nivo/bar"
import { observer } from "mobx-react"
import * as React from "react"
import { RouteComponentProps } from "react-router"
import { aercProperties } from "../aerc/AercUtils"
import { spacing, themeStore } from "../config/MuiConfig"
import { activeSasExpansions, expansionInfoMap } from "../expansions/Expansions"
import { AercData } from "../generated-src/AercData"
import { Expansion } from "../generated-src/Expansion"
import { Loader } from "../mui-restyled/Loader"
import { uiStore } from "../ui/UiStore"
import { statsStore } from "./StatsStore"

@observer
export class CardStatsView extends React.Component<{}> {

    constructor(props: RouteComponentProps<{}>) {
        super(props)
        uiStore.setTopbarValues("Stats of KeyForge", "Stats", "AERC stats")
    }

    render() {

        const datas = statsStore.statsBySetNum?.get(null)?.aercDatas

        if (statsStore.stats == null || datas == null) {
            return <Loader/>
        }

        const bySetDataReal = activeSasExpansions
            .map((expansion: Expansion) => {

                if (datas == null) {
                    return undefined
                }

                return datas.find(data => data.expansion == expansion)
            })
            .filter(data => data != null) as unknown as AercData[]

        if (bySetDataReal.length === 0) {
            return <Typography>Stats not yet calculated</Typography>
        }

        return (
            <div style={{display: "flex", flexWrap: "wrap", justifyContent: "center"}}>
                <AercBySetBar
                    name={"Expansion Card AERC"}
                    data={
                        bySetDataReal
                            .map(data => {
                                const dataCopy = {...data}
                                // @ts-ignore
                                dataCopy.expansion = expansionInfoMap.get(dataCopy.expansion)!.abbreviation
                                return dataCopy
                            })
                    }
                    indexBy={"expansion"}
                    width={(102 * activeSasExpansions.length)}
                />

                {activeSasExpansions.filter((expansion: Expansion) => {
                    const tournamentIllegal = expansionInfoMap.get(expansion)?.tournamentIllegal
                    return tournamentIllegal == null || !tournamentIllegal
                }).map((expansion: Expansion) => {

                    const expansionDatas = datas?.filter(data => data.expansion === expansion && data.house != null)

                    if (expansionDatas == null) {
                        return null
                    }

                    return (
                        <AercBySetBar
                            name={expansionInfoMap.get(expansion)!.abbreviation + " Card AERC"}
                            data={expansionDatas}
                            indexBy={"house"}
                            width={640}
                            key={expansion}
                        />
                    )
                })}
            </div>
        )
    }
}

const AercBySetBar = (props: { name: string, data: AercData[], indexBy: string, width: number, gridXValues?: string[] }) => {
    return (
        <Card style={{margin: spacing(2), padding: spacing(2), display: "flex", flexDirection: "column", alignItems: "center"}}>
            <Typography variant={"h4"} color={"primary"}>{props.name}</Typography>
            <div style={{width: props.width, height: 560}}>
                <ResponsiveBar
                    data={props.data}
                    keys={aercProperties}
                    indexBy={props.indexBy}
                    margin={{top: 50, right: 130, bottom: 50, left: 60}}
                    padding={0.3}
                    colors={{scheme: "paired"}}
                    axisTop={null}
                    axisRight={null}
                    axisBottom={{
                        tickSize: 5,
                        tickPadding: 5,
                        tickRotation: 0,
                        legend: props.indexBy,
                        legendPosition: "middle",
                        legendOffset: 32,
                    }}
                    maxValue={1.8}
                    axisLeft={{
                        tickSize: 5,
                        tickPadding: 5,
                        tickRotation: 0,
                        legend: "AERC per Card",
                        legendPosition: "middle",
                        legendOffset: -40,
                    }}
                    labelSkipWidth={12}
                    labelSkipHeight={12}
                    labelTextColor={{from: "color", modifiers: [["darker", 1.6]]}}
                    theme={{
                        textColor: themeStore.graphText
                    }}
                    legends={[
                        {
                            dataFrom: "keys",
                            anchor: "bottom-right",
                            direction: "column",
                            justify: false,
                            translateX: 120,
                            translateY: 0,
                            itemsSpacing: 2,
                            itemWidth: 100,
                            itemHeight: 20,
                            itemDirection: "left-to-right",
                            itemOpacity: 0.85,
                            symbolSize: 20,
                            effects: [
                                {
                                    on: "hover",
                                    style: {
                                        itemOpacity: 1
                                    }
                                }
                            ]
                        }
                    ]}
                    animate={true}
                />
            </div>
        </Card>
    )
}