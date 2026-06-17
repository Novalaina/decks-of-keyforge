import { Box, Card, Link, TextField, Tooltip, Typography } from "@material-ui/core"
import * as React from "react"
import { useEffect, useState } from "react"
import { spacing, themeStore } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { Utils } from "../config/Utils"
import { KeyButton } from "../mui-restyled/KeyButton"
import { LinkButton } from "../mui-restyled/LinkButton"
import { screenStore } from "../ui/ScreenStore"
import { userStore } from "../user/UserStore"
import { uiStore } from "../ui/UiStore"
import { deckStore } from "../decks/DeckStore"
import { observer } from "mobx-react"
import { DeckViewSmall } from "../decks/DeckViewSmall"
import { Alert } from "@material-ui/lab"

export const DeckImportPage = observer(() => {

    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)
    const [loadingMine, setLoadingMine] = useState(false)
    const [deckId, setDeckId] = useState("")

    const importDeck = async () => {
        setError("")
        setLoading(true)
        const importWith = Utils.findUuid(deckId)
        if (importWith.length !== 36) {
            setError("Please include a KeyForge deck UUID to import a deck.")
            setLoading(false)
            return
        }
        const errorMessage = await deckStore.importDeck(importWith)
        if (errorMessage != null && errorMessage.length > 0) {
            setError(errorMessage)
        } else {
            await deckStore.findDeckForImportPage(importWith)
        }
        setLoading(false)
    }

    const importAndAdd = async () => {
        setError("")
        setLoadingMine(true)
        const importWith = Utils.findUuid(deckId)
        if (importWith.length !== 36) {
            setError("Please include a KeyForge deck UUID to import a deck.")
            setLoadingMine(false)
            return
        }
        const errorMessage = await deckStore.importDeckAndAddToMyDecks(importWith)
        if (errorMessage != null && errorMessage.length > 0) {
            setError(errorMessage)
        } else {
            await deckStore.findDeckForImportPage(importWith)
        }
        setLoadingMine(false)
    }

    useEffect(() => {
        uiStore.setTopbarValues("Import Decks", "Import", "Add decks to the site")
        deckStore.importDecksPageDeck = undefined
        return () => {
            deckStore.importDecksPageDeck = undefined
        }
    }, [])

    const smallScreen = screenStore.screenSizeSm()

    return (
        <Box display={"flex"} justifyContent={"center"} alignItems={smallScreen && "center"}
             flexDirection={smallScreen ? "column" : "row"}>
            <Box maxWidth={600} m={2}>
                <Card>
                    <Box m={2}>
                        <Typography
                            variant={"body2"} color={"textSecondary"}
                            noWrap={false}
                            style={{marginBottom: spacing(2)}}
                        >
                            Decks are imported to the site approximately every two minutes, although the automatic
                            process can fall behind if many decks are available at once. You can manually import
                            decks here to see scores for decks earlier, or to add them to your collection.
                        </Typography>
                        <Typography
                            variant={"body2"} color={"textSecondary"}
                            noWrap={false}
                            style={{marginBottom: spacing(2)}}
                        >
                            If this feature doesn't work, please try again later.
                        </Typography>
                        {error.length > 0 && (
                            <Box mb={2}>
                                <Alert severity={"warning"}>
                                    {error === "Not all houses have 12 cards." ? (
                                        <Box display={"flex"} flexDirection={"column"} style={{gap: 16}}>
                                            <Typography>This deck has invalid card counts in Master Vault. Please send
                                                feedback to request that Ghost Galaxy fix this deck on Master
                                                Vault.</Typography>
                                            <Link href={"https://keyforging.com/contact-us/"} target={"_blank"}>KeyForge
                                                Master Vault Feedback Form</Link>
                                        </Box>
                                    ) : error}
                                </Alert>
                            </Box>
                        )}
                        <TextField
                            variant={"outlined"}
                            color={themeStore.darkMode ? "secondary" : undefined}
                            label={"KeyForge Deck Id or URL"}
                            value={deckId}
                            onChange={(event) => setDeckId(event.target.value)}
                            style={{marginBottom: spacing(2)}}
                            autoFocus={true}
                            helperText={"Id or Url from the deck url at keyforgegame.com e.g. 293f366d-af1d-46ea-9c0f-4cc956dae50d"}
                            error={!!error}
                        />
                        <div
                            style={{marginBottom: spacing(2), display: "flex"}}
                        >
                            <Tooltip
                                title={userStore.theoreticalDecksAllowed ? "" : "Become a $3 a month patron to create theoretical decks!"}
                                style={{zIndex: screenStore.zindexes.tooltip}}
                            >
                                <div>
                                    <LinkButton
                                        href={Routes.createTheoreticalDeck}
                                        style={{marginRight: spacing(2)}}
                                        disabled={!userStore.theoreticalDecksAllowed}
                                    >
                                        Create Theoretical Deck
                                    </LinkButton>
                                </div>
                            </Tooltip>
                        </div>
                        <div style={{display: "flex", alignItems: "center"}}>
                            <KeyButton
                                variant={"contained"}
                                onClick={importDeck}
                                loading={loading}
                            >
                                Import
                            </KeyButton>
                            {userStore.loggedIn() ? (
                                <KeyButton
                                    variant={"contained"}
                                    color={"primary"}
                                    onClick={importAndAdd}
                                    loading={loadingMine}
                                    style={{marginLeft: spacing(2)}}
                                >
                                    Import to my Decks
                                </KeyButton>
                            ) : null}
                        </div>
                    </Box>
                </Card>
            </Box>
            {deckStore.importDecksPageDeck != null && (
                <DeckViewSmall deck={deckStore.importDecksPageDeck}/>
            )}
        </Box>
    )
})
