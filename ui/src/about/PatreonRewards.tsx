import {
    Button,
    Card,
    CardActionArea,
    CardContent,
    CardMedia,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Typography
} from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { useState } from "react"
import { spacing } from "../config/MuiConfig"
import { AboutSubPaths, MyDokSubPaths, Routes } from "../config/Routes"
import CollectionAnalysis from "../generic/imgs/patron-benefits/collection-analysis.png"
import DarkMode from "../generic/imgs/patron-benefits/dark-mode.png"
import DeckComparison from "../generic/imgs/patron-benefits/deck-comparison.png"
import { InfoListCard } from "../generic/InfoListCard"
import { LinkPatreon } from "../thirdpartysites/patreon/LinkPatreon"
import { patronRewardLevelName } from "../thirdpartysites/patreon/PatreonRewardsTier"
import { patreonStore } from "../thirdpartysites/patreon/PatreonStore"
import { PatronButton } from "../thirdpartysites/patreon/PatronButton"
import { screenStore } from "../ui/ScreenStore"
import { userStore } from "../user/UserStore"
import { AboutGridItem } from "./AboutPage"
import { DokLink } from "../generic/DokLink"

@observer
export class PatreonRewards extends React.Component {

    componentDidMount(): void {
        patreonStore.findTopPatrons()
    }

    /* eslint react/jsx-key: 0 */
    render() {
        return (
            <>
                <AboutGridItem>
                    {userStore.patron ? (
                        <InfoListCard
                            title={"Thanks for being a Patron!"}
                            infos={[
                                <Typography style={{marginBottom: spacing(2)}}>Patreon
                                    tier: {patronRewardLevelName(userStore.patronTier)}</Typography>,
                                "All tiers have the benefits of all lower tiers.",
                                <div style={{paddingTop: spacing(1), display: "flex", justifyContent: "center"}}>
                                    <PatronButton/>
                                </div>
                            ]}
                        />
                    ) : (
                        <InfoListCard
                            title={"Become a Patron"}
                            infos={[
                                "Become a patron with Patreon to support the site and gain rewards! After becoming a Patron link your Patreon account to " +
                                "get your benefits.",
                                "All tiers have the benefits of all lower tiers.",
                                <div style={{paddingTop: spacing(1), display: "flex", justifyContent: "center"}}>
                                    <PatronButton style={{marginRight: spacing(2)}}/>
                                    <LinkPatreon redirectPath={AboutSubPaths.patreon}/>
                                </div>
                            ]}
                        />
                    )}
                    <div style={{marginBottom: spacing(4)}}/>
                    <InfoListCard
                        title={"Accessing your rewards"}
                        infos={[
                            <Typography style={{marginRight: spacing(2)}}>
                                After becoming a patron link your account with the above "Link Patreon" button.
                            </Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Deck Name Query Language –
                                $3+</Typography>,
                            <Typography>
                                Use a special query language to query decks. Words match exactly, for example
                                "Ice" will match a deck named "Anteater 'Ice'
                            </Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Dark Mode – $3+</Typography>,
                            <Typography>Go to your <DokLink href={MyDokSubPaths.profile}>profile</DokLink> to toggle on
                                dark mode.</Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Discord Roles –
                                $3+</Typography>,
                            <Typography>Link your Discord account on your <DokLink
                                href={"https://www.patreon.com/settings/apps"}>Patreon Profile</DokLink>.</Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Theoretical Decks –
                                $3+</Typography>,
                            <Typography>Use the toggle on the bottom left of the deck search window.</Typography>,
                            <Typography>Click the button on the "Import Deck" menu.</Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Deck for sale notifications –
                                $6+</Typography>,
                            <Typography>
                                Search <DokLink href={Routes.decksForSale()}>decks for sale</DokLink> and you will see a
                                "Notify" button appear next to
                                the "Search" button. Use that to save a custom search and get notified when someone
                                lists a match.
                            </Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>SAS Preview Mode –
                                $6+</Typography>,
                            <Typography>Head to your <DokLink href={MyDokSubPaths.profile}>profile</DokLink> to toggle
                                on SAS Preview Mode.</Typography>,
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Store listing on the landing
                                page – $12+</Typography>,
                            <Typography>
                                You must have 10 or more decks listed for sale or trade for your store to show. You can
                                change your store name,
                                icon and banner from your <DokLink href={MyDokSubPaths.profile}>profile</DokLink>.
                            </Typography>,
                        ]}
                    />
                </AboutGridItem>
                <AboutGridItem>
                    <InfoListCard
                        title={"It that Fastidiously Notices Bargains"}
                        titleVariant={"h5"}
                        subtitle={"$3 or more per month"}
                        noDivider={true}
                        infos={[
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Rewards</Typography>,
                            "Build alliance decks!",
                            "Use a special query language in your deck name searches",
                            "Dark mode",
                            "Analyze collections of decks up to 1000",
                            "Create tags for decks and search those tags",
                            "Compare decks",
                            "List decks for sale or trade for up to 1 year and auto renew listings (profile setting)",
                            "List up to 250 decks for sale. (Free maximum is 100.)",
                            "Create public offer-based sale listings",
                            "Search team decks",
                            "Create Theoretical Decks",
                            "Add notes to decks",
                            "Add splash images to your content creator listings",
                            "That warm and fuzzy feeling that comes from supporting something cool",
                        ]}
                    />
                    <div style={{marginBottom: spacing(4)}}/>
                    <InfoListCard
                        title={"The Archon that Purchases the Paradigm"}
                        titleVariant={"h5"}
                        subtitle={"$6 or more per month"}
                        noDivider={true}
                        infos={[
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Rewards</Typography>,
                            "Up to 25 deck sale notifications: Set up to be notified whenever decks that match certain search criteria are listed for sale or trade.",
                            "Preview new SAS Scores before they are released!",
                            `See deck ownership. Use the "Extra Options" on search page`,
                            "List up to 1000 decks for sale",
                            "Download deck search result spreadsheets with 1,000 or 5,000 results",
                            "Form a team and manage members",
                            "See your full purchase and sale history",
                            "Analyze collections of decks up to 2,500",
                        ]}
                    />
                    <div style={{marginBottom: spacing(4)}}/>
                    <InfoListCard
                        title={"Merchant Æmbermaker"}
                        titleVariant={"h5"}
                        subtitle={"$12 or more per month"}
                        noDivider={true}
                        infos={[
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Rewards</Typography>,
                            "List any number of decks for sale.",
                            "Create your own store on DoK! Your store will be listed on the landing page, and you can add a name, icon, and banner for it.",
                        ]}
                    />
                    <div style={{marginBottom: spacing(4)}}/>
                    <InfoListCard
                        title={"The Charitable Champion"}
                        titleVariant={"h5"}
                        subtitle={"$25 or more per month"}
                        noDivider={true}
                        infos={[
                            <Typography variant={"subtitle1"} style={{fontWeight: "bold"}}>Rewards</Typography>,
                            "Up to 50 deck sale notifications.",
                            "Credit on the contact me about page as a major contributor to the site.",
                            "Join a special major contributor channel on the discord."
                        ]}
                    />
                </AboutGridItem>
                <AboutGridItem>
                    <BenefitPic name={"Dark Mode"} costPerMonth={3} src={DarkMode} mediaHeight={288}/>
                    <div style={{marginBottom: spacing(4)}}/>
                    <BenefitPic name={"Collection Analysis"} costPerMonth={3} src={CollectionAnalysis}
                                mediaHeight={400}/>
                    <div style={{marginBottom: spacing(4)}}/>
                    <BenefitPic name={"Compare Decks"} costPerMonth={3} src={DeckComparison} mediaHeight={360}/>
                </AboutGridItem>
            </>
        )
    }
}

export const BenefitPic = (props: { name: string, costPerMonth: number, src: string, mediaHeight: number }) => {

    const {name, costPerMonth, src, mediaHeight} = props

    const [open, setOpen] = useState(false)

    return (
        <>
            <Card style={{maxWidth: 624}}>
                <CardActionArea
                    onClick={() => setOpen(true)}
                >
                    <CardMedia
                        style={{height: mediaHeight}}
                        image={src}
                        title={name}
                    />
                </CardActionArea>
                <CardContent>
                    <Typography gutterBottom variant="h5" component="h2">
                        {name}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" component="p">
                        ${costPerMonth}+ per month
                    </Typography>
                </CardContent>
            </Card>
            <Dialog
                open={open}
                onClose={() => setOpen(false)}
                fullScreen={true}
            >
                <DialogTitle>{name}</DialogTitle>
                <DialogContent>
                    <img alt={name} src={src} width={screenStore.screenWidth - 80}/>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpen(false)}>Close</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}
