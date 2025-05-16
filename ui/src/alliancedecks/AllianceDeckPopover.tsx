import {
    Box,
    Button,
    FormControlLabel,
    IconButton,
    List,
    ListItem,
    MenuItem,
    Paper,
    Switch,
    TextField,
    Typography
} from "@material-ui/core"
import { Clear } from "@material-ui/icons"
import { observer } from "mobx-react"
import React from "react"
import { useHistory } from "react-router-dom"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { userStore } from "../user/UserStore"
import { theme, themeStore } from "../config/MuiConfig"
import { PatronButton } from "../thirdpartysites/patreon/PatronButton"
import { HouseImage } from "../houses/HouseBanner"
import { screenStore } from "../ui/ScreenStore"
import { KeyButton } from "../mui-restyled/KeyButton"
import { allianceDeckStore } from "./AllianceDeckStore"
import { Routes } from "../config/Routes"
import { House } from "../generated-src/House"
import { Expansion } from "../generated-src/Expansion"
import { ExpansionIcon } from "../expansions/ExpansionIcon"
import { userDeckStore } from "../userdeck/UserDeckStore"
import { uniqBy } from "lodash"

export const AllianceDeckPopover = observer(() => {

    const history = useHistory()

    const decks = keyLocalStorage.allianceDeckSaveInfo.houses
    const createAllianceDeck = keyLocalStorage.genericStorage.buildAllianceDeck
    if (!createAllianceDeck || decks.length === 0) {
        return null
    }

    const patron = userStore.patron
    const tokenNames = decks
        .filter(deck => deck.tokenName != null)
        .map(deck => deck.tokenName!)

    return (
        <Box style={{
            position: "fixed",
            right: theme.spacing(2),
            bottom: theme.spacing(2),
            zIndex: screenStore.zindexes.keyDrawer
        }}>
            <Paper style={{background: themeStore.lightAmberBackground}}>
                {!patron && (
                    <Box p={2}>
                        <Typography
                            style={{marginBottom: theme.spacing(2)}}
                        >
                            Please become a patron to build alliance decks!
                        </Typography>
                        <PatronButton/>
                    </Box>
                )}
                <List>
                    {decks.map(deck => (
                        <ListItem key={deck.house}>
                            <Box width={screenStore.screenSizeXs() ? 144 : 280} display={"flex"} alignItems={"center"}>
                                <HouseImage house={deck.house} size={24}/>
                                <Typography
                                    variant={"h5"}
                                    noWrap={true}
                                    style={{fontSize: 16, marginLeft: theme.spacing(1)}}
                                >
                                    {deck.deckName}
                                </Typography>
                            </Box>
                            <IconButton
                                style={{marginLeft: theme.spacing(2)}}
                                size={"small"}
                                onClick={() => keyLocalStorage.removeAllianceHouse(deck.deckId)}
                            >
                                <Clear fontSize={"small"}/>
                            </IconButton>
                        </ListItem>
                    ))}
                </List>
                {tokenNames.length > 0 && (
                    <Box mx={2} mb={2}>
                        <TextField
                            select={true}
                            label="Token"
                            value={keyLocalStorage.allianceDeckSaveInfo.tokenName}
                            onChange={(event) => keyLocalStorage.setAllianceToken(event.target.value)}
                        >
                            {tokenNames.map((tokenName) => (
                                <MenuItem key={tokenName} value={tokenName}>
                                    {tokenName}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Box>
                )}
                {decks[0].expansion === Expansion.PROPHETIC_VISIONS && (
                    <Box mx={2} mb={2}>
                        <TextField
                            select={true}
                            label={"Prophecies Deck"}
                            value={keyLocalStorage.allianceDeckSaveInfo.propheciesDeckId}
                            onChange={(event) => keyLocalStorage.setPropheciesDeck(event.target.value)}
                            fullWidth={true}
                        >
                            {uniqBy(decks, "deckId").map((prophecyDeck) => (
                                <MenuItem key={prophecyDeck.deckId} value={prophecyDeck.deckId}>
                                    {prophecyDeck.deckName}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Box>
                )}
                <Box>
                    <Box ml={2} display={"flex"} alignItems={"flex-start"}>
                        <Box mr={2}><ExpansionIcon expansion={decks[0].expansion} size={32}/></Box>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={keyLocalStorage.genericStorage.addAllianceToMyAlliances !== false}
                                    onChange={(event) => {
                                        keyLocalStorage.updateGenericStorage({
                                            addAllianceToMyAlliances: event.target.checked
                                        })
                                    }}
                                    color="secondary"
                                />
                            }
                            label="Add to my alliances"
                        />
                    </Box>
                </Box>
                <Button
                    onClick={() => {
                        keyLocalStorage.updateGenericStorage({buildAllianceDeck: false})
                        keyLocalStorage.clearAllianceDeck()
                    }}
                    variant={"outlined"}
                    style={{margin: theme.spacing(2), marginRight: 0}}
                >
                    Clear
                </Button>
                <KeyButton
                    variant={"contained"}
                    color={"primary"}
                    style={{margin: theme.spacing(2)}}
                    disabled={!patron || decks.length !== 3}
                    onClick={async () => {
                        const newDeckId = await allianceDeckStore.saveAllianceDeck()
                        if (keyLocalStorage.genericStorage.addAllianceToMyAlliances !== false) {
                            userDeckStore.addToOwnedAllianceDecks(newDeckId)
                        }
                        keyLocalStorage.updateGenericStorage({buildAllianceDeck: false})
                        keyLocalStorage.clearAllianceDeck()
                        history.push(Routes.allianceDeckPage(newDeckId))
                    }}
                    loading={allianceDeckStore.savingDeck}
                >
                    {screenStore.screenSizeXs() ? "Alliance" : "Forge Alliance"}
                </KeyButton>
            </Paper>
        </Box>
    )
})

export interface AllianceDeckSaveInfo {
    houses: AllianceDeckNameId[]
    tokenName?: string
    propheciesDeckId?: string
}

export interface AllianceDeckNameId {
    deckId: string
    deckName: string
    house: House
    expansion: Expansion
    tokenName?: string
}
