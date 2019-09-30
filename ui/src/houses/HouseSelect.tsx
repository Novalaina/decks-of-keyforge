import Checkbox from "@material-ui/core/Checkbox/Checkbox"
import FormControl from "@material-ui/core/FormControl/FormControl"
import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel"
import FormGroup from "@material-ui/core/FormGroup/FormGroup"
import FormLabel from "@material-ui/core/FormLabel/FormLabel"
import { observable } from "mobx"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing } from "../config/MuiConfig"
import { House, houseValues, houseValuesArray } from "./House"

interface HouseSelectProps {
    selectedHouses: SelectedHouses
    includeWorldsCollide?: boolean
}

@observer
export class HouseSelect extends React.Component<HouseSelectProps> {
    render() {
        const {selectedHouses, includeWorldsCollide} = this.props
        const selectedHousesGotten = selectedHouses.getSelectedHouses()
        let houses = houseValuesArray
        if (!includeWorldsCollide) {
            houses = houseValuesArray.filter(houseValue => houseValue.house !== House.SaurianRepublic && houseValue.house !== House.StarAlliance)
        }
        return (
            <FormControl>
                <FormLabel style={{marginBottom: spacing(1)}}>Houses</FormLabel>
                <FormGroup
                    row={true}
                >
                    {houses.map((houseValue) => {
                        const select = selectedHousesGotten.filter((selectedHouse) => selectedHouse.house === houseValue.house)
                        return (<HouseCheckbox key={houseValue.house} selectedHouse={select[0]}/>)
                    })}
                </FormGroup>
            </FormControl>
        )
    }
}

interface HouseCheckboxProps {
    selectedHouse: SelectedHouse
}

@observer
export class HouseCheckbox extends React.Component<HouseCheckboxProps> {

    handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        this.props.selectedHouse.selected = event.target.checked
    }

    render() {
        const {selectedHouse} = this.props
        return (
            <FormControlLabel
                control={
                    <Checkbox
                        checked={selectedHouse.selected}
                        onChange={this.handleChange}
                    />
                }
                label={houseValues.get(selectedHouse.house)!.label}
            />
        )
    }
}

export interface SelectedHouse {
    house: House
    selected: boolean
}

export class SelectedHouses {
    @observable
    selectedHouses: SelectedHouse[] = houseValuesArray.map(houseValue => ({house: houseValue.house, selected: false}))

    constructor(initialHouses?: House[]) {
        this.selectedHouses = houseValuesArray.map(houseValue => {
            return {house: houseValue.house, selected: initialHouses ? initialHouses.indexOf(houseValue.house) !== -1 : false}
        })
    }

    reset = () => this.selectedHouses = houseValuesArray.map(houseValue => ({house: houseValue.house, selected: false}))

    getSelectedHouses = () => this.selectedHouses.slice()

    toArray = () => this.getSelectedHouses()
        .filter(selectedHouse => selectedHouse.selected)
        .map(selectedHouse => selectedHouse.house)
}
