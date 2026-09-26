import { Box, Divider, FormControl, Input, InputLabel, MenuItem, Paper, Select, TextField, Typography } from "@material-ui/core"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import * as React from "react"
import { useEffect, useState } from "react"
import { spacing } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { ContentCreatorSort } from "../generated-src/ContentCreatorSort"
import { ContentCreatorType, ContentCreatorTypeUtils } from "../generated-src/ContentCreatorType"
import { SortDirection } from "../generated-src/SortDirection"
import { KeyButton } from "../mui-restyled/KeyButton"
import { LinkButton } from "../mui-restyled/LinkButton"
import { Loader } from "../mui-restyled/Loader"
import { uiStore } from "../ui/UiStore"
import { userStore } from "../user/UserStore"
import { ContentCreatorCard, contentCreatorTypeName } from "./ContentCreatorCard"
import { contentCreatorStore } from "./ContentCreatorStore"

class ContentCreatorSearchStore {
    @observable
    contentName = ""

    @observable
    username = ""

    @observable
    types: ContentCreatorType[] = []

    @observable
    sort: ContentCreatorSort = ContentCreatorSort.UPDATED

    @observable
    sortDirection: SortDirection = SortDirection.DESC

    performSearch = async () => {
        await contentCreatorStore.searchContentCreators({
            contentName: this.contentName,
            username: this.username,
            types: this.types,
            sort: this.sort,
            sortDirection: this.sortDirection,
        })
    }

    constructor() {
        makeObservable(this)
    }
}

export const ContentCreatorSearchPage = observer(() => {

    const [store] = useState(new ContentCreatorSearchStore())

    useEffect(() => {
        uiStore.setTopbarValues("Content Creators of KeyForge", "Creators", "Find KeyForge content")
        store.performSearch()
    }, [store])

    const found = contentCreatorStore.foundContentCreators

    return (
        <Box m={2}>
            <Paper>
                <Box p={2}>
                    <Box display={"flex"} flexWrap={"wrap"} alignItems={"flex-end"}  style={{gap: 16}}>
                        <Typography variant={"h4"} color={"primary"}>
                            Content Creators
                        </Typography>
                        <Box flexGrow={1}/>
                        <Box display={"flex"} flexWrap={"wrap"} style={{gap: 16}}>
                            <TextField
                                label={"Content Name"}
                                value={store.contentName}
                                onChange={(event) => store.contentName = event.target.value}
                                onKeyPress={(event) => {
                                    if (event.key === "Enter") store.performSearch()
                                }}
                                style={{width: 160}}
                            />
                            <TextField
                                label={"Username"}
                                value={store.username}
                                onChange={(event) => store.username = event.target.value}
                                onKeyPress={(event) => {
                                    if (event.key === "Enter") store.performSearch()
                                }}
                                style={{width: 160}}
                            />
                            <FormControl style={{minWidth: 120}}>
                                <InputLabel>Type</InputLabel>
                                <Select
                                    input={<Input/>}
                                    multiple={true}
                                    value={store.types}
                                    onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                                        store.types = event.target.value as ContentCreatorType[]
                                    }}
                                >
                                    {ContentCreatorTypeUtils.values.map(type => (
                                        <MenuItem key={type} value={type}>{contentCreatorTypeName(type)}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            <FormControl style={{minWidth: 120}}>
                                <InputLabel>Sort By</InputLabel>
                                <Select
                                    value={store.sort}
                                    onChange={(event) => {
                                        store.sort = event.target.value as ContentCreatorSort
                                    }}
                                >
                                    <MenuItem value={ContentCreatorSort.UPDATED}>Last Updated</MenuItem>
                                    <MenuItem value={ContentCreatorSort.CONTENT_NAME}>Content Name</MenuItem>
                                </Select>
                            </FormControl>
                            <FormControl style={{minWidth: 100}}>
                                <InputLabel>Order</InputLabel>
                                <Select
                                    value={store.sortDirection}
                                    onChange={(event) => {
                                        store.sortDirection = event.target.value as SortDirection
                                    }}
                                >
                                    <MenuItem value={SortDirection.DESC}>Descending</MenuItem>
                                    <MenuItem value={SortDirection.ASC}>Ascending</MenuItem>
                                </Select>
                            </FormControl>
                        </Box>
                            <Box display={"flex"}>
                                <KeyButton
                                    variant={"contained"}
                                    color={"primary"}
                                    loading={contentCreatorStore.searching}
                                    onClick={store.performSearch}
                                    style={{marginRight: spacing(2)}}
                                >
                                    Search
                                </KeyButton>
                                {userStore.loggedIn() && (
                                    <LinkButton variant={"outlined"} href={Routes.createContentCreator}>
                                        Create
                                    </LinkButton>
                                )}
                            </Box>
                    </Box>
                </Box>
                <Divider/>
                <Box p={1}>
                    {contentCreatorStore.searching && found == null ? (
                        <Loader/>
                    ) : found != null && found.length === 0 ? (
                        <Box p={2}>
                            <Typography>No content creators found.</Typography>
                        </Box>
                    ) : (
                        <Box display={"flex"} flexWrap={"wrap"}>
                            {found?.map(contentCreator => (
                                <ContentCreatorCard key={contentCreator.id} contentCreator={contentCreator}/>
                            ))}
                        </Box>
                    )}
                </Box>
            </Paper>
        </Box>
    )
})
