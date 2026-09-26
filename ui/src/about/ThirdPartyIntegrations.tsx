import { Box, Grid, Typography } from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing } from "../config/MuiConfig"
import { HelperText } from "../generic/CustomTypographies"
import { InfoListCard } from "../generic/InfoListCard"
import { LinkButtonSafe } from "../mui-restyled/LinkButton"
import { DiscordUser } from "../thirdpartysites/discord/DiscordUser"
import KeyForgeBurgerInsertsIcon from "./about-images/burger-inserts-icon.png"
import { AboutGridItem } from "./AboutPage"
import { RouteComponentProps } from "react-router"
import { uiStore } from "../ui/UiStore"

@observer
export class ThirdPartyIntegrations extends React.Component {

    constructor(props: RouteComponentProps<{}>) {
        super(props)
        uiStore.setTopbarValues("3rd Party Tools", "Tools", "Third party tools")
    }

    render() {
        return (
            <Box display={"flex"} justifyContent={"center"} flexDirection={"column"}>
                <Box display={"flex"} justifyContent={"center"} m={4}>
                    <Box display={"flex"} flexDirection={"column"} maxWidth={800}>
                        <Typography variant={"h4"} style={{marginBottom: spacing(2)}}>Third Party Integrations</Typography>
                        <Typography variant={"subtitle1"} style={{marginBottom: spacing(2)}}>
                            Third party tools and sites that enhance DoK or use its data and APIs.
                        </Typography>
                        <HelperText color={"error"} style={{marginBottom: spacing(1)}}>Use these at your own risk!</HelperText>
                        <HelperText>
                            Decks of KeyForge does not verify the safety of these tools.
                            If you experience an issue, please contact the listed project maintainer. If you find that one of these projects is unmaintained
                            or abusive, please contact <a href="mailto:decksofkeyforge@gmail.com">decksofkeyforge@gmail.com</a>. Also, send me a message
                            if you have an awesome project you want included in this list! I will be selective in projects I accept, but feel
                            free to let me know about it.
                        </HelperText>
                    </Box>
                </Box>
                <Grid container={true} spacing={4} justify={"center"}>
                    <DisplayIntegration
                        name={"Ammasser"}
                        description={"An unofficial browser extension that collects KeyForge decks from your Master Vault account and syncs them to Decks of KeyForge or The Crucible Online."}
                        url={"https://amasser.keyforge.cards/"}
                        urlName={"Amasser · keyforge.cards"}
                        discord={"dmikalova"}
                    />
                    <DisplayIntegration
                        name={"keyforj"}
                        description={"Printable boxes for your KeyForge decks and Adventure Decks."}
                        url={"https://kf.sirjorj.com/"}
                        urlName={"Deck Boxes & Adventure Boxes Webapp"}
                        discord={"sirjorj"}
                    />
                    <DisplayIntegration
                        name={"iOS: Import to DoK from MV App"}
                        description={
                            [
                                <Typography key={"first"}>
                                    <b>How to install:</b>
                                </Typography>,
                                <ol key={"second"}>
                                    <li>Open iPhone Settings</li>
                                    <li>Search for "Shortcuts"</li>
                                    <li>Enable "Allow Unstrusted Shortcuts" (this will make it so you can install the shortcut)</li>
                                    <li>Click the link below</li>
                                    <li>Scroll down and press the "Add Untrusted Shortcut" button</li>
                                </ol>,
                                <Typography key={"third"}>
                                    <b>How to use:</b>
                                </Typography>,
                                <ol key={"fourth"}>
                                    <li>Scan a deck using the Master Vault application</li>
                                    <li>Press the "Share" button</li>
                                    <li>Click on the "Open in DOK" action</li>
                                    <li>Watch as the deck is imported, and Safari is opened on the DOK page</li>
                                    <li>Click "My Deck" in DOK to add to your collection (optional)</li>
                                </ol>,
                            ]
                        }
                        url={"https://www.icloud.com/shortcuts/5a505eae78fb4b70b2d177c59c2460ae"}
                        urlName={"Install Shortcut"}
                        discord={"RyanCH#4711"}
                    />
                    <DisplayIntegration
                        name={"KeyForge Burger Inserts"}
                        description={"Web application for printing paper inserts with deck information. This is based off the original Excel project."}
                        url={"https://keyforge-burger-inserts.herokuapp.com"}
                        urlName={"Burger Token Inserts App"}
                        discord={"Drallieiv#4274"}
                        contact={"github.com/drallieiv"}
                        icon={<img src={KeyForgeBurgerInsertsIcon}/>}
                    />
                    <DisplayIntegration
                        name={"KeyForge Deck Info Inserts"}
                        description={"Dynamic Excel Based template to print out decksofkeyforge.com stats in an insert formatted to fit BurgerTokens.com cases"}
                        url={"https://www.dropbox.com/s/vdh8c33tcc7pi58/Keyforge_Deck_Info_Inserts%20-%20FINAL.xlsx?dl=0"}
                        urlName={"Dropbox link to Excel sheet"}
                        discord={"GavinForge (Steve)#7647"}
                    />
                    <DisplayIntegration
                        name={"Redirect MV to DoK"}
                        description={"A Chrome plugin that adds a button to go from a deck page on Master Vault to Decks of KeyForge"}
                        url={"https://chrome.google.com/webstore/detail/decksofkeyforgecom-redire/hoppohnelffeollapmhmgckjmhkhgnhn?hl=en"}
                        urlName={"Decks Of KeyForge redirect"}
                        discord={"Aurore#3266"}
                    />
                </Grid>
            </Box>
        )
    }
}

const DisplayIntegration = (props: { name: string, description: React.ReactNode[] | string, icon?: React.ReactNode, url?: string, urlName?: string, discord?: string, contact?: string }) => {
    let infos: React.ReactNode[] = []
    if (typeof props.description === "string") {
        infos.push(props.description)
    } else {
        infos = props.description
    }
    if (props.url) {
        infos.push(
            <LinkButtonSafe color={"primary"} variant={"outlined"} href={props.url} key={"url"}>{props.urlName ? props.urlName : props.url}</LinkButtonSafe>
        )
    }
    if (props.discord) {
        infos.push(<DiscordUser discord={props.discord}/>)
    }
    if (props.contact) {
        infos.push(`Contact: ${props.contact}`)
    }
    return (
        <AboutGridItem>
            <InfoListCard
                title={props.name}
                infos={infos}
                icon={props.icon}
            />
        </AboutGridItem>
    )
}
