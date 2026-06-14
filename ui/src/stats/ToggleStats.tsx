import { ToggleButton, ToggleButtonGroup } from "@material-ui/lab"
import { observer } from "mobx-react"
import * as React from "react"
import { activeSasExpansions, expansionInfoMap } from "../expansions/Expansions"
import { statsStore } from "./StatsStore"

@observer
export class ToggleStats extends React.Component<{ style?: React.CSSProperties }> {

    toggleStatsExpansion = (event: React.MouseEvent, expansionNumber: number) => {
        if (expansionNumber === -1) {
            statsStore.changeStats(undefined)
        } else {
            statsStore.changeStats(expansionNumber)
        }
    }

    render() {
        const value = statsStore.currentStatsExpansion == null ? -1 : statsStore.currentStatsExpansion
        return (
            <ToggleButtonGroup
                value={value}
                exclusive={true}
                onChange={this.toggleStatsExpansion}
                style={this.props.style}
                size={"small"}
            >
                <ToggleButton value={-1}>
                    All
                </ToggleButton>
                {activeSasExpansions.map(expansion => () => {
                    const expansionInfo = expansionInfoMap.get(expansion)
                    if (expansionInfo == null) return null
                    return (
                        <ToggleButton value={expansionInfo.expansionNumber} key={expansionInfo.expansionNumber}>
                            {expansionInfo.abbreviation}
                        </ToggleButton>
                    )
                })}
            </ToggleButtonGroup>
        )
    }
}