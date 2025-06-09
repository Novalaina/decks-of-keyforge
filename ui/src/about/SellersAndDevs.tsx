import { Typography } from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { spacing } from "../config/MuiConfig"
import { InfoListCard } from "../generic/InfoListCard"
import { KeyButton } from "../mui-restyled/KeyButton"
import { publicApiStore } from "../sellers/PublicApiStore"
import { DiscordButton } from "../thirdpartysites/discord/DiscordButton"
import { userStore } from "../user/UserStore"
import { AboutGridItem } from "./AboutPage"
import { DokLink } from "../generic/DokLink"

@observer
export class SellersAndDevs extends React.Component {

    componentDidMount(): void {
        publicApiStore.apiKey = undefined
    }

    render() {

        let apiKeyOrButton
        if (!userStore.loggedIn()) {
            apiKeyOrButton = (
                <Typography>Login to generate an API Key.</Typography>
            )
        } else if (publicApiStore.apiKey) {
            apiKeyOrButton = (
                <div>
                    <Typography variant={"h4"} style={{marginTop: spacing(2)}}>
                        Your API key:
                    </Typography>
                    <Typography variant={"h5"} color={"primary"} style={{marginTop: spacing(2)}}>
                        {publicApiStore.apiKey}
                    </Typography>
                    <Typography style={{marginTop: spacing(2), marginBottom: spacing(2)}}>
                        To learn the API send an email to <DokLink
                        href={"mailto:decksofkeyforge@gmail.com"}>decksofkeyforge@gmail.com</DokLink>
                        or join us on the Decks of KeyForge discord:
                    </Typography>
                    <DiscordButton/>
                </div>
            )
        } else {
            apiKeyOrButton = (
                <div style={{display: "flex", justifyContent: "center", marginTop: spacing(4)}}>
                    <KeyButton
                        color={"secondary"}
                        variant={"contained"}
                        loading={publicApiStore.generatingApiKey}
                        disabled={publicApiStore.generatingApiKey}
                        onClick={publicApiStore.generateApiKey}
                    >
                        Generate API Key
                    </KeyButton>
                </div>
            )
        }

        return (
            <>
                <AboutGridItem>
                    <InfoListCard title={"Generate your API Key"} infos={[
                        "To use our public APIs to sell decks or get SAS and AERC stats you'll need to generate an API key.",
                        "Using the button below you can generate your Api Key. " +
                        "This API key can be used to access your private list of decks you own. (If you do not allow anyone to see your decks.) " +
                        "You may provide this to other sites for them to sync your DoK deck list with their site, but you should only give it " +
                        "to sites or tools you trust. " +
                        "You can make a new one at any time, but when " +
                        "you do the previous Api Key immediately becomes invalid. You will only be able to see this API key " +
                        "immediately after it is generated.",
                        apiKeyOrButton
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"File with all SAS and AERC"} infos={[
                        "Please use this instead of the API if it fits your needs to help conserve DoK's server resources.",
                        "I've created csv files (spreadsheets) with the deck info for all decks up to the time they were generated.",
                        <DokLink
                            href={"https://drive.google.com/drive/folders/1Yk4x221rqeIr32WrC5hkMXyBmWC311rS?usp=sharing"}
                            target={"_blank"} rel={"noopener noreferrer"} key={"sas-link4"}>
                            Folder with deck csv files
                        </DokLink>,
                        "Like with the API, if you use this please attribute decksofkeyforge.com on your site / material.",
                        "I plan to create a new file whenever I update SAS or AERC."
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Rules to use the APIs in general"} infos={[
                        "1. Please do not spam requests. Use the all decks CSV above when appropriate. The site has been brought down " +
                        "due to improper use of these APIs.",
                        "2. You can make a maximum of 25 requests per minute. If you need more, please become a patron! Patrons have " +
                        "increased request limits. $6 a month = 50 per min, $12 a month = 100 per min, and $25+ a month = 250 per min.",
                        "3. If you use these endpoints for your project or website please give credit and provide a link to " +
                        "decksofkeyforge.com",
                        "4. Please only use the APIs specified here from this site. If you have an idea for a cool feature of the site " +
                        "you'd like to see as a public API, send me a message on discord or via email!"
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"SAS and AERC API"} infos={[
                        "I've created a simple API you can use to get SAS and AERC ratings for a deck",
                        "1. First off, if you want all the decks, use the Decks CSV above. Don't request every deck that exists from DoK!",
                        "2. I've included the version of SAS used. If possible, please cache or persist SAS scores until a new version is released.",
                        "The url to request deck info is:",
                        "https://decksofkeyforge.com/public-api/v3/decks/{deck-id-from-master-vault}",
                        `You'll need to include the header "Api-Key: {your-api-key}`,
                        "3. You may also make this request for Alliance decks. Perform the request as above, except with the request url:",
                        "https://decksofkeyforge.com/public-api/v1/alliance-decks/{deck-id-from-dok}"
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"My Decks API"} infos={[
                        "With this API you can get a list of all decks you own, or have added to favorites, funny, or notes.",
                        "Third party sites and tools may request their users' DoK API key. With it you can see the decks that user " +
                        "has listed as owned on DoK.",
                        "https://decksofkeyforge.com/public-api/v1/my-decks?page=0",
                        "(The 'page' query parameter is optional, and pages results by 100 decks at a time. This is " +
                        "necessary if you have many decks.)",
                        "Or for alliance decks: ",
                        "https://decksofkeyforge.com/public-api/v1/my-alliances",
                        `You'll need to include the header "Api-Key: {your-api-key}`
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Deck Statistics API"} infos={[
                        "This API provides the data I use to build the graphs and charts you see on the site.",
                        "These statistics are updated once every 3 days. You shouldn't need to request them more often than that.",
                        "The url to request stats is:",
                        "https://decksofkeyforge.com/public-api/v1/stats",
                        `You'll need to include the header "Api-Key: {your-api-key}`
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Cards API"} infos={[
                        "With this API you can get a list of all the cards that exist in KeyForge. It does not contain special variants of those cards, " +
                        "like mavericks or legacies.",
                        "These are only updated after a new set releases. Please do not repeatedly call this endpoint. You can call it a few times after " +
                        "a new set releases as it gradually fills in all the cards.",
                        "The url to request cards is:",
                        "https://decksofkeyforge.com/public-api/v1/cards",
                        `You'll need to include the header "Api-Key: {your-api-key}`
                    ]}/>
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard title={"Tournaments API"} infos={[
                        "Request info about a DoK Tournament by Tournament Id.",
                        "The JSON returned by this endpoint is subject to change. Do not request this frequently or repeatedly.",
                        "The url to request a tournament is:",
                        "https://decksofkeyforge.com/public-api/v1/tournaments/{id}",
                        `You'll need to include the header "Api-Key: {your-api-key}`
                    ]}/>
                </AboutGridItem>
            </>
        )
    }
}
