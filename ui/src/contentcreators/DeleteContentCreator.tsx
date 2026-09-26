import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@material-ui/core"
import { observer } from "mobx-react"
import React, { useState } from "react"
import { ContentCreatorDetailsDto } from "../generated-src/ContentCreatorDetailsDto"
import { KeyButton } from "../mui-restyled/KeyButton"
import { contentCreatorStore } from "./ContentCreatorStore"

export const DeleteContentCreator = observer((props: {
    contentCreator: ContentCreatorDetailsDto,
    onDeleted?: () => void
}) => {
    const {contentCreator, onDeleted} = props

    const [open, setOpen] = useState(false)

    return (
        <>
            <Button size={"small"} onClick={() => setOpen(true)}>
                Delete
            </Button>
            <Dialog open={open}>
                <DialogTitle>Delete {contentCreator.contentName}?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete this content?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <KeyButton
                        onClick={() => setOpen(false)}
                        disabled={contentCreatorStore.deleting}
                    >
                        Cancel
                    </KeyButton>
                    <KeyButton
                        color={"primary"}
                        loading={contentCreatorStore.deleting}
                        onClick={async () => {
                            await contentCreatorStore.deleteContentCreator(contentCreator.id)
                            setOpen(false)
                            if (onDeleted != null) {
                                onDeleted()
                            }
                        }}
                    >
                        Delete
                    </KeyButton>
                </DialogActions>
            </Dialog>
        </>
    )
})
