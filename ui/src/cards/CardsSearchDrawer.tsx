import { Box, Checkbox, FormControlLabel, IconButton, Switch, Tooltip, Typography } from "@material-ui/core"
import List from "@material-ui/core/List/List"
import ListItem from "@material-ui/core/ListItem/ListItem"
import TextField from "@material-ui/core/TextField/TextField"
import { Close, Image, ViewList, ViewModule } from "@material-ui/icons"
import { Autocomplete, ToggleButton, ToggleButtonGroup } from "@material-ui/lab"
import * as History from "history"
import { range, startCase } from "lodash"
import { observer } from "mobx-react"
import * as React from "react"
import { ChangeEvent } from "react"
import { KeyDrawer, keyDrawerStore, KeyDrawerVersion } from "../components/KeyDrawer"
import { SearchDrawerExpansionPanel } from "../components/SearchDrawerExpansionPanel"
import { SortDirectionView } from "../components/SortDirectionView"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { spacing } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { Utils } from "../config/Utils"
import { ConstraintDropdowns, FiltersConstraintsStore } from "../decks/search/ConstraintDropdowns"
import { expansionInfoMapNumbers, expansionsWithCards } from "../expansions/Expansions"
import { ExpansionSelectOrExclude, SelectedOrExcludedExpansions } from "../expansions/ExpansionSelectOrExclude"
import { validSynergies, validTraits } from "../extracardinfo/SynergyTraitUtils"
import { CardType } from "../generated-src/CardType"
import { Rarity } from "../generated-src/Rarity"
import { SynergyTrait } from "../generated-src/SynergyTrait"
import { CsvDownloadButton } from "../generic/CsvDownloadButton"
import { HouseSelect, SelectedHouses } from "../houses/HouseSelect"
import { KeyButton } from "../mui-restyled/KeyButton"
import { KeyMultiCheckbox } from "../mui-restyled/KeyMultiCheckbox"
import { KeyMultiSelect, SelectedValues } from "../mui-restyled/KeyMultiSelect"
import { screenStore } from "../ui/ScreenStore"
import { userStore } from "../user/UserStore"
import { CardFilters, CardSort } from "./CardFilters"
import { cardStore } from "./CardStore"
import { CardUtils } from "./KCard"
import { CardSortSelect, CardSortSelectStore } from "./selects/CardSortSelect"
import { PublishDateSelect, SelectedPublishDate } from "./selects/PublishDateSelect"
import { ArrayUtils } from "../config/ArrayUtils"

interface CardsSearchDrawerProps {
    filters: CardFilters
    history: History.History
}

@observer
export class CardsSearchDrawer extends React.Component<CardsSearchDrawerProps> {

    selectedHouses = new SelectedHouses(this.props.filters.houses)
    selectedCardTypes = new SelectedValues<CardType>(this.props.filters.types)
    selectedRarities = new SelectedValues<Rarity>(this.props.filters.rarities)
    selectedPowers = new SelectedValues<number>(this.props.filters.powers)
    selectedAmbers = new SelectedValues<number>(this.props.filters.ambers)
    selectedSortStore = new CardSortSelectStore(this.props.filters.sort)
    selectedExpansions = new SelectedOrExcludedExpansions(
        this.props.filters.expansions.map(expNum => expansionInfoMapNumbers.get(expNum)!.backendEnum),
        this.props.filters.excludedExpansions.map(expNum => expansionInfoMapNumbers.get(expNum)!.backendEnum),
        expansionsWithCards,
    )
    selectedPublishDate = new SelectedPublishDate(this.props.filters.aercHistoryDate)

    constraintsStore = new FiltersConstraintsStore(this.props.filters.constraints)

    search = (event?: React.FormEvent) => {
        if (event) {
            event.preventDefault()
        }
        const {filters} = this.props
        filters.houses = this.selectedHouses.toArray()
        filters.types = this.selectedCardTypes.selectedValues
        filters.rarities = this.selectedRarities.selectedValues
        filters.powers = this.selectedPowers.selectedValues
        filters.ambers = this.selectedAmbers.selectedValues
        filters.sort = this.selectedSortStore.toEnumValue() as CardSort
        filters.expansions = this.selectedExpansions.expansionsAsNumberArray()
        filters.excludedExpansions = this.selectedExpansions.excludedExpansionsAsNumberArray()
        filters.aercHistoryDate = this.selectedPublishDate.date === "" ? undefined : this.selectedPublishDate.date
        filters.constraints = this.constraintsStore.cleanConstraints()
        cardStore.searchCards(filters)
        keyDrawerStore.closeIfSmall()
        this.props.history.push(Routes.cardSearch(filters))
    }

    clearSearch = () => {
        const {filters} = this.props
        this.selectedHouses.reset()
        this.selectedSortStore.selectedValue = ""
        filters.reset()
        this.selectedCardTypes.reset()
        this.selectedRarities.reset()
        this.selectedPowers.reset()
        this.selectedAmbers.reset()
        this.selectedExpansions.reset()
        this.constraintsStore.reset()
    }

    render() {
        const {filters} = this.props
        const {title, description, handleTitleUpdate, handleDescriptionUpdate} = filters

        const constraintOptions = [
            "amberControl",
            "expectedAmber",
            "artifactControl",
            "creatureControl",
            "efficiency",
            "recursion",
            "disruption",
            "creatureProtection",
            "effectivePower",
        ].flatMap(value => [value, `${value}Max`])

        return (
            <KeyDrawer version={KeyDrawerVersion.CARD}>
                <form onSubmit={this.search}>
                    <List dense={true}>
                        <ListItem>
                            <TextField
                                label={"Card Name"}
                                onChange={handleTitleUpdate}
                                value={title}
                                fullWidth={!screenStore.screenSizeXs()}
                            />
                            <div style={{flexGrow: 1}}/>
                            {screenStore.screenSizeXs() ? (
                                <IconButton onClick={() => keyDrawerStore.open = false}>
                                    <Close/>
                                </IconButton>
                            ) : null}
                        </ListItem>
                        <ListItem>
                            <TextField
                                label={"Card Description"}
                                onChange={handleDescriptionUpdate}
                                value={description}
                                fullWidth={true}
                            />
                        </ListItem>
                        <ListItem style={{marginTop: spacing(1)}}>
                            <ExpansionSelectOrExclude
                                selectedExpansions={this.selectedExpansions}
                                allowExclusions={true}
                                availableExpansions={expansionsWithCards}
                            />
                        </ListItem>
                        <ListItem>
                            <HouseSelect selectedHouses={this.selectedHouses}/>
                        </ListItem>
                        <ListItem>
                            <div>
                                <SearchDrawerExpansionPanel
                                    initiallyOpen={this.selectedCardTypes.selectedValues.length > 0}
                                    title={"Attributes"}
                                >
                                    <Box>
                                        <KeyMultiCheckbox
                                            name={"Card Type"}
                                            selected={this.selectedCardTypes}
                                            options={Utils.enumValues(CardType) as CardType[]}
                                        />
                                        <KeyMultiCheckbox
                                            name={"Rarity"}
                                            selected={this.selectedRarities}
                                            options={[
                                                Rarity.Common,
                                                Rarity.Uncommon,
                                                Rarity.Rare,
                                                Rarity.Special,
                                            ]}
                                        />
                                        <FormControlLabel
                                            style={{width: "100%"}}
                                            control={
                                                <Checkbox
                                                    checked={filters.gigantic}
                                                    onChange={filters.handleGiganticUpdate}
                                                />
                                            }
                                            label={"Gigantic Cards"}
                                        />
                                        <KeyMultiSelect
                                            name={"Aember"}
                                            selected={this.selectedAmbers}
                                            options={range(0, 5)
                                                .map(amber => amber.toString())}
                                        />
                                        <KeyMultiSelect
                                            name={"Power"}
                                            selected={this.selectedPowers}
                                            options={range(1, 17)
                                                .map(power => power.toString())}
                                        />
                                        <FormControlLabel
                                            style={{marginTop: spacing(1)}}
                                            control={
                                                <Checkbox
                                                    checked={filters.anomalies}
                                                    onChange={filters.handleAnomaliesUpdate}
                                                />
                                            }
                                            label={"Anomalies Only"}
                                        />
                                    </Box>
                                </SearchDrawerExpansionPanel>
                                <SearchDrawerExpansionPanel
                                    initiallyOpen={!this.constraintsStore.isDefaultConstraints()} title={"Constraints"}>
                                    <ConstraintDropdowns
                                        store={this.constraintsStore}
                                        properties={constraintOptions}
                                    />
                                </SearchDrawerExpansionPanel>
                                <SearchDrawerExpansionPanel
                                    initiallyOpen={filters.trait != null || filters.synergy != null}
                                    title={"Synergies & Past AERC"}
                                    lastPanel={true}
                                >
                                    <Autocomplete
                                        options={ArrayUtils.arrPlus(validTraits, ["", SynergyTrait.alpha, SynergyTrait.omega]) as (SynergyTrait | "")[]}
                                        value={filters.trait ?? ""}
                                        renderInput={(params) => <TextField {...params} label={"Trait"}/>}
                                        renderOption={(option) => <Typography
                                            noWrap>{startCase(option).replace(" R ", " ??? ")}</Typography>}
                                        onChange={(event: ChangeEvent<{}>, newValue: string | null) => {
                                            const newValueTyped = newValue as (SynergyTrait | "" | null)
                                            filters.trait = newValueTyped == "" || newValueTyped == null ? undefined : newValueTyped
                                        }}
                                        fullWidth={true}
                                        size={"small"}
                                    />
                                    <Autocomplete
                                        options={ArrayUtils.arrPlus(validSynergies, "") as (SynergyTrait | "")[]}
                                        value={filters.synergy ?? ""}
                                        renderInput={(params) => <TextField {...params} label={"Synergy"}/>}
                                        renderOption={(option) => <Typography
                                            noWrap>{startCase(option).replace(" R ", " ??? ")}</Typography>}
                                        onChange={(event: ChangeEvent<{}>, newValue: string | null) => {
                                            const newValueTyped = newValue as (SynergyTrait | "" | null)
                                            filters.synergy = newValueTyped == "" || newValueTyped == null ? undefined : newValueTyped
                                        }}
                                        fullWidth={true}
                                        size={"small"}
                                    />
                                    <FormControlLabel
                                        style={{marginTop: spacing(1)}}
                                        control={
                                            <Checkbox
                                                checked={filters.aercHistory}
                                                onChange={() => {
                                                    filters.aercHistory = !filters.aercHistory
                                                    if (!filters.aercHistory) {
                                                        this.selectedPublishDate.reset()
                                                    }
                                                }}
                                            />
                                        }
                                        label={"Past AERC"}
                                    />
                                    {filters.aercHistory && (
                                        <PublishDateSelect selected={this.selectedPublishDate}/>
                                    )}
                                </SearchDrawerExpansionPanel>
                            </div>
                        </ListItem>
                        <ListItem>
                            <CardSortSelect store={this.selectedSortStore}/>
                            <div style={{marginTop: "auto", marginLeft: spacing(2)}}>
                                <SortDirectionView hasSort={filters}/>
                            </div>
                        </ListItem>
                        <ListItem>
                            <CsvDownloadButton
                                data={cardStore.cards == null ? undefined : CardUtils.arrayToCSV(cardStore.cards)}
                                name={"cards"}
                            />
                            <KeyButton
                                variant={"outlined"}
                                onClick={this.clearSearch}
                                style={{marginRight: spacing(2), marginLeft: spacing(2)}}
                            >
                                Clear
                            </KeyButton>
                            <KeyButton
                                variant={"contained"}
                                color={"secondary"}
                                type={"submit"}
                                loading={cardStore.searchingForCards}
                                disabled={cardStore.searchingForCards}
                            >
                                Search
                            </KeyButton>
                        </ListItem>
                        {cardStore.cards && !cardStore.searchingForCards ? (
                            <ListItem>
                                <Typography variant={"subtitle2"}>You found {cardStore.cards.length} cards</Typography>
                            </ListItem>
                        ) : null}
                        <ListItem>
                            <Tooltip title={"View type"}>
                                <ToggleButtonGroup
                                    value={keyLocalStorage.cardListViewType}
                                    exclusive={true}
                                    onChange={(event, viewType) => {
                                        keyLocalStorage.setCardListViewType(viewType)
                                    }}
                                    style={{marginRight: spacing(2)}}
                                    size={"small"}
                                >
                                    <ToggleButton value={"full"}>
                                        <ViewModule/>
                                    </ToggleButton>
                                    <ToggleButton value={"image"}>
                                        <Image/>
                                    </ToggleButton>
                                    <ToggleButton value={"table"}>
                                        <ViewList/>
                                    </ToggleButton>
                                </ToggleButtonGroup>
                            </Tooltip>
                        </ListItem>
                        {userStore.isAdmin && (
                            <ListItem>
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={cardStore.showFutureCardInfo}
                                            onChange={() => cardStore.showFutureCardInfo = !cardStore.showFutureCardInfo}
                                        />
                                    }
                                    label={"Future Card Info"}
                                />
                            </ListItem>
                        )}
                    </List>
                </form>
            </KeyDrawer>
        )
    }
}
