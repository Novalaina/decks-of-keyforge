import { Box, Link, Typography } from "@material-ui/core"
import imageCompression from "browser-image-compression"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing, themeStore } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { Utils } from "../config/Utils"
import { ContentCreatorDetailsDto } from "../generated-src/ContentCreatorDetailsDto"
import { PatreonRewardsTier } from "../generated-src/PatreonRewardsTier"
import { HelperText } from "../generic/CustomTypographies"
import { FileUploadButton, FileUploadType } from "../mui-restyled/FileUploadButton"
import { KeyButton } from "../mui-restyled/KeyButton"
import { Loader, LoaderSize } from "../mui-restyled/Loader"
import { PatreonRequired } from "../thirdpartysites/patreon/PatreonRequired"
import { messageStore } from "../ui/MessageStore"
import { contentCreatorStore } from "./ContentCreatorStore"

/**
 * Promo images may only be uploaded and displayed for active patrons of this tier or higher.
 */
export const promoImagePatreonTier = PatreonRewardsTier.SUPPORT_SOPHISTICATION

export const UploadPromoImage = observer((props: { contentCreator: ContentCreatorDetailsDto }) => {

    const {contentCreator} = props
    const promoImageKey = contentCreator.promoImageKey
    const loading = contentCreatorStore.savingPromoImage

    return (
        <Box display={"flex"} flexDirection={"column"} maxWidth={480}>
            <Typography variant={"h6"}>Promo Image</Typography>
            <HelperText style={{marginTop: spacing(2)}}>
                Your promo image is displayed at the top of your content's card, and adds to your content creator
                score.
            </HelperText>
            <HelperText style={{marginTop: spacing(2), marginBottom: spacing(2)}}>
                A wide image works best, 688 x 320 pixels is a good size. Max width or height is 2000px.
            </HelperText>
            <PatreonRequired
                requiredLevel={promoImagePatreonTier}
                message={"Please become a second tier Patron of the site to add a promo image to your content!"}
                style={{marginBottom: spacing(2)}}
            >
                {promoImageKey != null && (
                    <Box mb={2}>
                        <Link href={Routes.userContent(promoImageKey)} target={"_blank"}>
                            <img
                                src={Routes.userContent(promoImageKey)}
                                style={{maxWidth: 400}}
                                alt={"Promo image"}
                            />
                        </Link>
                    </Box>
                )}
                <Box display={"flex"}>
                    <FileUploadButton
                        id={"promo-image"}
                        variant={"outlined"}
                        color={themeStore.darkMode ? "secondary" : "primary"}
                        fileType={FileUploadType.IMAGE}
                        disabled={loading}
                        handleUpload={async (event) => {
                            if (event.target.files == null || event.target.files.length === 0) {
                                messageStore.setWarningMessage("No file added to upload.")
                                return
                            }
                            const imgFile = event.target.files[0]
                            try {
                                const compressedImg = await imageCompression(imgFile, {
                                    maxSizeMB: 1.0,
                                    maxWidthOrHeight: 2000,
                                    useWebWorker: true
                                })
                                await contentCreatorStore.savePromoImage(
                                    contentCreator.id,
                                    compressedImg,
                                    Utils.filenameExtension(imgFile)
                                )
                            } catch (e) {
                                messageStore.setWarningMessage("Couldn't upload image.")
                            }
                        }}
                    >
                        {promoImageKey == null ? "Add Promo Image" : "Update Promo Image"}
                        {loading && <Loader size={LoaderSize.SMALL} style={{marginLeft: spacing(2)}}/>}
                    </FileUploadButton>
                    {promoImageKey != null && (
                        <KeyButton
                            disabled={loading}
                            onClick={() => contentCreatorStore.deletePromoImage(contentCreator.id)}
                            variant={"outlined"}
                            style={{marginLeft: spacing(2)}}
                            color={themeStore.darkMode ? "secondary" : "primary"}
                        >
                            Remove
                        </KeyButton>
                    )}
                </Box>
            </PatreonRequired>
        </Box>
    )
})
