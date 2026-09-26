import { Box, Divider, Grid, MenuItem, Paper, TextField, Typography } from "@material-ui/core"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import * as React from "react"
import { useEffect, useState } from "react"
import { useHistory, useParams } from "react-router-dom"
import { Routes } from "../config/Routes"
import { ContentCreatorType, ContentCreatorTypeUtils } from "../generated-src/ContentCreatorType"
import { SaveContentCreatorDetails } from "../generated-src/SaveContentCreatorDetails"
import { KeyButton } from "../mui-restyled/KeyButton"
import { Loader } from "../mui-restyled/Loader"
import { uiStore } from "../ui/UiStore"
import { userStore } from "../user/UserStore"
import { contentCreatorTypeName } from "./ContentCreatorCard"
import { contentCreatorStore } from "./ContentCreatorStore"
import { UploadPromoImage } from "./UploadPromoImage"

const maxContentNameLength = 255
const maxDescriptionLength = 2000
const maxLinkLength = 500

class CreateContentCreatorStore {
    @observable
    contentName = ""

    @observable
    description = ""

    @observable
    link = ""

    @observable
    discordServer = ""

    @observable
    contentCreatorType: ContentCreatorType = ContentCreatorType.OTHER

    @observable
    saveAttempted = false

    constructor() {
        makeObservable(this)
    }

    contentNameValid = () => {
        const trimmed = this.contentName.trim()
        return trimmed.length > 0 && trimmed.length <= maxContentNameLength
    }

    descriptionValid = () => {
        const trimmed = this.description.trim()
        return trimmed.length > 0 && trimmed.length <= maxDescriptionLength
    }

    linkValid = () => {
        const trimmed = this.link.trim()
        return trimmed.length === 0 || CreateContentCreatorStore.urlValid(trimmed)
    }

    discordServerValid = () => {
        const trimmed = this.discordServer.trim()
        return trimmed.length === 0 || CreateContentCreatorStore.urlValid(trimmed)
    }

    hasALink = () => this.link.trim().length > 0 || this.discordServer.trim().length > 0

    valid = () => this.contentNameValid() && this.descriptionValid() && this.linkValid() && this.discordServerValid()
        && this.hasALink()

    toSave = (id?: string): SaveContentCreatorDetails => ({
        id,
        contentName: this.contentName.trim(),
        description: this.description.trim(),
        link: this.link.trim().length === 0 ? undefined : this.link.trim(),
        discordServer: this.discordServer.trim().length === 0 ? undefined : this.discordServer.trim(),
        contentCreatorType: this.contentCreatorType,
    })

    private static urlValid = (url: string) =>
        url.length > 0 && url.length <= maxLinkLength && (url.startsWith("http://") || url.startsWith("https://"))
}

export const CreateContentCreatorPage = observer(() => {

    const {id} = useParams<{ id?: string }>()
    const history = useHistory()
    const [store] = useState(new CreateContentCreatorStore())
    const [loaded, setLoaded] = useState(id == null)

    const editing = contentCreatorStore.contentCreatorBeingEdited

    useEffect(() => {
        uiStore.setTopbarValues("Featured Content", "Content", id == null ? "Add your content" : "Update your content")
    }, [id])

    useEffect(() => {
        if (id == null) {
            contentCreatorStore.contentCreatorBeingEdited = undefined
            setLoaded(true)
            return
        }
        setLoaded(false)
        contentCreatorStore.findContentCreator(id)
            .then((found) => {
                store.contentName = found.contentName
                store.description = found.description
                store.link = found.link ?? ""
                store.discordServer = found.discordServer ?? ""
                store.contentCreatorType = found.contentCreatorType
                setLoaded(true)
            })
            .catch(() => setLoaded(true))
    }, [id, store])

    if (!userStore.loggedIn()) {
        return (
            <Box m={4}>
                <Typography>Please login to add your content.</Typography>
            </Box>
        )
    }

    if (!loaded) {
        return <Loader/>
    }

    const isOwner = id == null || (editing != null && editing.username === userStore.username)

    if (!isOwner) {
        return (
            <Box m={4}>
                <Typography>You can only edit your own content.</Typography>
            </Box>
        )
    }

    return (
        <Box m={2} display={"flex"} justifyContent={"center"}>
            <Paper style={{maxWidth: 800, width: "100%"}}>
                <Box p={4}>
                    <Typography variant={"h4"} color={"primary"} gutterBottom={true}>
                        {id == null ? "Add your Content" : "Update your Content"}
                    </Typography>
                    <Grid container={true} spacing={2}>
                        <Grid item={true} xs={12} sm={8}>
                            <TextField
                                label={"Content Name"}
                                variant={"outlined"}
                                value={store.contentName}
                                onChange={(event) => store.contentName = event.target.value}
                                required={true}
                                fullWidth={true}
                                error={!store.contentNameValid() && store.saveAttempted}
                                helperText={store.contentName.trim().length > maxContentNameLength
                                    ? `Name must be ${maxContentNameLength} characters or less.`
                                    : undefined}
                            />
                        </Grid>
                        <Grid item={true} xs={12} sm={4}>
                            <TextField
                                select={true}
                                variant={"outlined"}
                                label={"Type"}
                                value={store.contentCreatorType}
                                onChange={(event) => store.contentCreatorType = event.target.value as ContentCreatorType}
                                fullWidth={true}
                            >
                                {ContentCreatorTypeUtils.values.map(type => (
                                    <MenuItem key={type} value={type}>{contentCreatorTypeName(type)}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                        <Grid item={true} xs={12}>
                            <TextField
                                label={"Link"}
                                value={store.link}
                                onChange={(event) => store.link = event.target.value}
                                fullWidth={true}
                                error={store.saveAttempted && (!store.linkValid() || !store.hasALink())}
                                helperText={"Link to your content. URL, e.g. https://my-keyforge-podcast.com"}
                            />
                        </Grid>
                        <Grid item={true} xs={12}>
                            <TextField
                                label={"Discord Server Link"}
                                value={store.discordServer}
                                onChange={(event) => store.discordServer = event.target.value}
                                fullWidth={true}
                                error={store.saveAttempted && (!store.discordServerValid() || !store.hasALink())}
                                helperText={"An invite to your Discord server, e.g. https://discord.gg/sNkHD7k. A Link or Discord Server Link is required."}
                            />
                        </Grid>
                        <Grid item={true} xs={12}>
                            <TextField
                                label={"Description"}
                                variant={"filled"}
                                value={store.description}
                                onChange={(event) => store.description = event.target.value}
                                required={true}
                                multiline={true}
                                rows={4}
                                fullWidth={true}
                                error={!store.descriptionValid() && store.saveAttempted}
                                helperText={store.description.trim().length > maxDescriptionLength
                                    ? `Description must be ${maxDescriptionLength} characters or less. Current length: ${store.description.trim().length}`
                                    : undefined}
                            />
                        </Grid>
                    </Grid>
                    <Box display={"flex"} mt={2}>
                        <KeyButton onClick={() => history.push(Routes.contentCreators)}>
                            Cancel
                        </KeyButton>
                        <Box flexGrow={1}/>
                        <KeyButton
                            variant={"contained"}
                            color={"primary"}
                            loading={contentCreatorStore.saving}
                            onClick={async () => {
                                store.saveAttempted = true
                                if (!store.valid()) {
                                    return
                                }
                                const saved = await contentCreatorStore.saveContentCreator(store.toSave(id))
                                if (id == null) {
                                    history.push(Routes.editContentCreator(saved.id))
                                }
                            }}
                        >
                            Save
                        </KeyButton>
                    </Box>
                    {editing != null && (
                        <>
                            <Box my={4}>
                                <Divider/>
                            </Box>
                            <UploadPromoImage contentCreator={editing}/>
                        </>
                    )}
                    {editing == null && (
                        <Box mt={2}>
                            <Typography variant={"body2"} color={"textSecondary"}>
                                After saving your content, you may add a promo image if you are a Patron subscriber.
                            </Typography>
                        </Box>
                    )}
                </Box>
            </Paper>
        </Box>
    )
})
