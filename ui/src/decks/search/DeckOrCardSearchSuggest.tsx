import { createStyles, Fade, IconButton, InputBase, List, makeStyles, Paper, Popper } from "@material-ui/core"
import ClickAwayListener from "@material-ui/core/ClickAwayListener"
import { blue, deepPurple } from "@material-ui/core/colors"
import Divider from "@material-ui/core/Divider"
import ListSubheader from "@material-ui/core/ListSubheader"
import { PopperPlacementType } from "@material-ui/core/Popper"
import { Clear, Search } from "@material-ui/icons"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import React from "react"
import { RouteComponentProps, withRouter } from "react-router"
import { CardFilters } from "../../cards/CardFilters"
import { cardStore } from "../../cards/CardStore"
import { FancyCardMenuItem } from "../../cards/FancyCardMenuItem"
import { spacing, themeStore } from "../../config/MuiConfig"
import { Routes } from "../../config/Routes"
import { log } from "../../config/Utils"
import { LinkMenuItem } from "../../mui-restyled/LinkMenuItem"
import { screenStore } from "../../ui/ScreenStore"
import { deckStore } from "../DeckStore"
import { DeckFilters } from "./DeckFilters"
import { FancyDeckMenuItem } from "./FancyDeckMenuItem"

const useStyles = makeStyles(() =>
    createStyles({
        input: {
            color: "white"
        }
    }),
)

class SearchDeckNameStore {
    @observable
    searchValue = ""

    quietPeriodTimeoutId?: number

    inputRef: React.RefObject<HTMLDivElement> = React.createRef<HTMLDivElement>()

    handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (Number.isFinite(event.target.value)) {
            this.searchValue = event.target.value.toString()
        } else {
            this.searchValue = event.target.value
        }

        if (this.quietPeriodTimeoutId != null) {
            window.clearTimeout(this.quietPeriodTimeoutId)
        }

        const trimmed = this.searchValue.trim()
        if (trimmed.length > 2) {
            this.quietPeriodTimeoutId = window.setTimeout(() => {
                log.debug(`Delayed search with ${trimmed}`)
                if (trimmed.length > 3) {
                    deckStore.findDecksByName(trimmed)
                }
                cardStore.findCardsByName(trimmed)
            }, 500)
        } else {
            deckStore.deckNameSearchResults = []
        }
    }

    reset = () => {
        if (this.quietPeriodTimeoutId != null) {
            window.clearTimeout(this.quietPeriodTimeoutId)
        }
        this.searchValue = ""
        deckStore.deckNameSearchResults = []
        cardStore.cardNameSearchResults = []
        if (deckStore.deckNameSearchCancel != null) {
            deckStore.deckNameSearchCancel.cancel()
        }
    }

    constructor() {
        makeObservable(this)
    }
}

const searchDeckNameStore = new SearchDeckNameStore()

interface DeckSearchSuggestProps {
    placement: PopperPlacementType
}

export const DeckOrCardSearchSuggest = withRouter(observer((props: DeckSearchSuggestProps & RouteComponentProps) => {
    const classes = useStyles()
    const deckNameSuggestions = deckStore.deckNameSearchResults.map(deckNameId => ({label: deckNameId.name, value: deckNameId.id}))
    const menuOpen = deckNameSuggestions.length > 0 || cardStore.cardNameSearchResults.length > 0
    // log.debug(`Rendering deck name suggestions: ${prettyJson(deckNameSuggestions)} menuOpen = ${menuOpen}`)
    const filters = new DeckFilters()
    filters.title = searchDeckNameStore.searchValue
    const search = Routes.deckSearch(filters)
    const cardFilters = new CardFilters()
    cardFilters.title = searchDeckNameStore.searchValue
    let goToCards = false
    if (cardFilters.title.length > 0) {
        goToCards = cardStore.searchAndReturnCards(cardFilters).length > 0

    }
    let backgroundColor
    if (themeStore.altColors) {
        backgroundColor = themeStore.darkMode ? deepPurple[700] : deepPurple[500]
    } else {
        backgroundColor = themeStore.darkMode ? blue[700] : blue[400]
    }
    return (
        <>
            <div
                ref={searchDeckNameStore.inputRef}
                style={{
                    display: "flex",
                    borderRadius: 4,
                    backgroundColor,
                    marginLeft: spacing(screenStore.screenSizeXs() ? 1 : 2),
                    marginRight: spacing(screenStore.screenSizeXs() ? 1 : 2),
                    alignItems: "center",
                    width: screenStore.screenSizeXs() ? 104 : 224
                }}
            >
                <Search style={{color: "#FFFFFF", marginLeft: spacing(1), marginRight: spacing(1)}}/>
                <InputBase
                    onChange={searchDeckNameStore.handleSearchChange}
                    style={{caretColor: "#FFFFFF"}}
                    placeholder={screenStore.screenSizeXs() ? "Search" : "Search decks or cards..."}
                    className={classes.input}
                    value={searchDeckNameStore.searchValue}
                    onKeyPress={(event) => {
                        if (event.key === "Enter") {
                            if (goToCards) {
                                // go to card search
                                props.history.push(Routes.cardSearch(cardFilters))
                            } else {
                                // Go to deck search
                                props.history.push(search)
                            }
                            searchDeckNameStore.reset()
                        }
                    }}
                />
                {searchDeckNameStore.searchValue.trim().length > 0 ? (
                    <>
                        <div style={{flexGrow: 1}}/>
                        <IconButton size={"small"} style={{marginLeft: spacing(1)}} onClick={searchDeckNameStore.reset}>
                            <Clear style={{color: "white"}}/>
                        </IconButton>
                    </>
                ) : null}
            </div>
            <SuggestPopper menuOpen={menuOpen} placement={props.placement} search={search} ref={searchDeckNameStore.inputRef}/>
        </>
    )
}))

// eslint-disable-next-line
const SuggestPopper = observer(React.forwardRef((props: { menuOpen: boolean, placement: PopperPlacementType, search: string }, ref: React.Ref<HTMLDivElement>) => {
    return (
        <ClickAwayListener onClickAway={searchDeckNameStore.reset}>
            <Popper
                open={props.menuOpen}
                anchorEl={
                    // eslint-disable-next-line @typescript-eslint/no-explicit-any
                    (ref as any).current
                }
                transition
                style={{zIndex: screenStore.zindexes.menuPops}}
                placement={props.placement}
            >
                {({TransitionProps}) => (
                    <Fade {...TransitionProps} timeout={350}>
                        <Paper>
                            <List
                                style={{
                                    maxHeight: screenStore.screenHeight - 80,
                                    overflowY: "auto"
                                }}
                            >
                                {cardStore.cardNameSearchResults.length > 0 ? (
                                    <>
                                        <ListSubheader disableSticky={true}>
                                            Cards
                                        </ListSubheader>
                                        {cardStore.cardNameSearchResults.map((card) => (
                                            <FancyCardMenuItem card={card} onClick={searchDeckNameStore.reset} key={card.id}/>
                                        ))}
                                        <LinkMenuItem
                                            to={Routes.cards}
                                        >
                                            Search Cards...
                                        </LinkMenuItem>
                                    </>
                                ) : null}
                                {deckStore.deckNameSearchResults.length > 0 && cardStore.cardNameSearchResults.length > 0 ? <Divider/> : null}
                                {deckStore.deckNameSearchResults.length > 0 ? (
                                    <>
                                        <ListSubheader disableSticky={true}>
                                            Decks
                                        </ListSubheader>
                                        {deckStore.deckNameSearchResults.map((deckSearchResult) => (
                                            <FancyDeckMenuItem deck={deckSearchResult} onClick={searchDeckNameStore.reset} key={deckSearchResult.id}/>
                                        ))}
                                        <LinkMenuItem
                                            to={props.search}
                                            onClick={searchDeckNameStore.reset}
                                        >
                                            All results...
                                        </LinkMenuItem>
                                    </>
                                ) : null}
                            </List>
                        </Paper>
                    </Fade>
                )}
            </Popper>
        </ClickAwayListener>
    )
}))
