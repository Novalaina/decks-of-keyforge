import { Box, Card, CardActions, CardContent, CardMedia, Divider, Typography } from "@material-ui/core"
import { startCase } from "lodash"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing, themeStore } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { ContentCreatorDetailsDto } from "../generated-src/ContentCreatorDetailsDto"
import { DiscordIcon } from "../generic/icons/DiscordIcon"
import { KeyLink } from "../mui-restyled/KeyLink"
import { LinkButton, LinkButtonSafe } from "../mui-restyled/LinkButton"
import { WhiteSpaceTypography } from "../mui-restyled/WhiteSpaceTypography"
import { userStore } from "../user/UserStore"
import { contentCreatorStore } from "./ContentCreatorStore"
import { DeleteContentCreator } from "./DeleteContentCreator"

export const contentCreatorTypeName = (type: string) => startCase(type.toLowerCase())

export const ContentCreatorCard = observer((props: {
    contentCreator: ContentCreatorDetailsDto,
    style?: React.CSSProperties
}) => {
    const {contentCreator, style} = props
    const {
        id,
        contentName,
        description,
        link,
        discordServer,
        promoImageKey,
        contentCreatorType,
        username,
    } = contentCreator

    const isOwner = userStore.username === username
    const mediaHeight = 160

    return (
        <Card style={{display: "flex", flexDirection: "column", width: 344, margin: spacing(2), ...style}}>
            {promoImageKey != null && (
                <CardMedia
                    style={{height: mediaHeight}}
                    image={Routes.userContent(promoImageKey)}
                    title={`${contentName} promo image`}
                />
            )}
            <CardContent
                style={{
                    display: "flex",
                    flexDirection: "column",
                    height: promoImageKey == null ? 320 : 240,
                    padding: spacing(2, 2, 0, 2)
                }}
            >
                <Typography variant={"h5"} noWrap={true} style={{flexShrink: 0}}>{contentName}</Typography>
                <Box display={"flex"} justifyContent={"space-between"} mt={1} flexShrink={0}>
                    <Typography variant={"subtitle2"} color={"textSecondary"}>
                        {contentCreatorTypeName(contentCreatorType)}
                    </Typography>
                    <KeyLink to={Routes.userProfilePage(username)} noStyle={true}>
                        <Typography variant={"subtitle2"} color={"textSecondary"}>{username}</Typography>
                    </KeyLink>
                </Box>
                <Divider style={{marginTop: spacing(1), marginBottom: spacing(1), flexShrink: 0}}/>
                <div style={{overflowY: "auto", flexGrow: 1, flexShrink: 1, minHeight: 0}}>
                    <WhiteSpaceTypography variant={"body2"}>{description}</WhiteSpaceTypography>
                </div>
            </CardContent>
            <div style={{flexGrow: 1}}/>
            <CardActions>
                {isOwner && (
                    <>
                        <LinkButton size={"small"} href={Routes.editContentCreator(id)}>
                            Edit
                        </LinkButton>
                        <DeleteContentCreator contentCreator={contentCreator}/>
                    </>
                )}
                <Box style={{marginLeft: "auto"}}/>
                {link != null && (
                    <LinkButtonSafe
                        color={themeStore.darkMode ? "secondary" : "primary"}
                        href={link}
                        onClick={() => contentCreatorStore.recordClick(id)}
                    >
                        Visit
                    </LinkButtonSafe>
                )}
                {discordServer != null && (
                    <LinkButton
                        color={themeStore.darkMode ? "secondary" : "primary"}
                        href={discordServer}
                        onClick={() => link == null && contentCreatorStore.recordClick(id)}
                    >
                        <DiscordIcon height={24} style={{marginRight: spacing(0.5)}}/>
                        Discord
                    </LinkButton>
                )}
            </CardActions>
        </Card>
    )
})
