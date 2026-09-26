import { Box, Divider, List, ListSubheader, Typography } from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { KeyDrawer } from "../components/KeyDrawer"
import { spacing, themeStore } from "../config/MuiConfig"
import { AboutSubPaths, Routes, StatsSubPaths } from "../config/Routes"
import { DeckFilters } from "../decks/search/DeckFilters"
import { ExpansionIcon } from "../expansions/ExpansionIcon"
import { displayMyDecksLinksFor, expansionInfoMap, expansionInfos } from "../expansions/Expansions"
import { UnstyledLink } from "../generic/UnstyledLink"
import { LinkButton } from "../mui-restyled/LinkButton"
import { Loader } from "../mui-restyled/Loader"
import { FeaturedSellersView } from "../sellers/FeaturedSellersView"
import { FeaturedContentView } from "../contentcreators/FeaturedContentView"
import { statsStore } from "../stats/StatsStore"
import { WinRateBar } from "../stats/WinRateStatsView"
import { DiscordButton } from "../thirdpartysites/discord/DiscordButton"
import { GithubTicketsButton } from "../thirdpartysites/github/GithubTicketsButton"
import { PatronButton } from "../thirdpartysites/patreon/PatronButton"
import { screenStore } from "../ui/ScreenStore"
import { uiStore } from "../ui/UiStore"
import { AlliancesSearchLink, DeckSearchLink, LandingPageLink } from "./DeckSearchLink"
import { AllianceDeckFiltersUtils } from "../alliancedecks/AllianceDeckFiltersUtils"
import { SortDirection } from "../generated-src/SortDirection"
import { DokLink } from "../generic/DokLink"
import { AnnouncementPaper } from "../generic/AnnouncementPaper"

const topSas = new DeckFilters()

const tournamentLegal = new DeckFilters()
tournamentLegal.expansions = expansionInfos
    .filter(expansionInfo => expansionInfo.tournamentIllegal != true)
    .map(expansionInfo => expansionInfo.expansionNumber)

const forSale = DeckFilters.forSale()
const forTrade = new DeckFilters()
forTrade.forTrade = true

const worstSas = new DeckFilters()
worstSas.sortDirection = SortDirection.ASC

const validAlliances = AllianceDeckFiltersUtils.createEmpty()
validAlliances.validOnly = true
const allAlliances = AllianceDeckFiltersUtils.createEmpty()

export const landingPageDrawerWidth = 280

@observer
export class LandingPage extends React.Component<{}> {

    componentDidMount(): void {
        uiStore.setTopbarValues("DoK", "DoK", "Search, evaluate and buy KeyForge decks")
    }

    render() {

        const stats = statsStore.stats

        return (
            <Box>
                <Box display={"flex"}>
                    <KeyDrawer width={landingPageDrawerWidth} hamburgerMenu={true}>
                        <List>
                            <ListSubheader>
                                Decks
                            </ListSubheader>
                            <div style={{
                                display: "flex",
                                flexWrap: "wrap",
                                paddingRight: spacing(1)
                            }}>
                                <DeckSearchLink
                                    name={"Tourny Legal"}
                                    filters={tournamentLegal}
                                    style={{marginBottom: spacing(2)}}
                                />
                                <DeckSearchLink
                                    name={"Search"}
                                    filters={topSas}
                                    style={{marginBottom: spacing(2)}}
                                />
                                {displayMyDecksLinksFor.map(expansion => {
                                    const expansionFilters = new DeckFilters()
                                    expansionFilters.expansions = [expansionInfoMap.get(expansion)!.expansionNumber]
                                    return (
                                        <DeckSearchLink
                                            key={expansion}
                                            name={<ExpansionIcon expansion={expansion} white={true}/>}
                                            filters={expansionFilters}
                                            style={{marginBottom: spacing(2)}}
                                        />
                                    )
                                })}
                            </div>
                            <Divider/>
                            <ListSubheader>
                                Alliance Decks
                            </ListSubheader>
                            <div style={{display: "flex", flexWrap: "wrap", paddingRight: spacing(1)}}>
                                <AlliancesSearchLink
                                    name={"Valid"}
                                    filters={validAlliances}
                                    style={{marginBottom: spacing(2)}}
                                />
                                <AlliancesSearchLink
                                    name={"All"}
                                    filters={allAlliances}
                                    style={{marginBottom: spacing(2)}}
                                />
                            </div>
                            <Divider/>
                            <ListSubheader>
                                Community
                            </ListSubheader>
                            <div style={{display: "flex", flexWrap: "wrap", paddingRight: spacing(1)}}>
                                <LandingPageLink
                                    name={"Tourneys"}
                                    to={Routes.tournaments}
                                    color={"secondary"}
                                    style={{marginBottom: spacing(2)}}

                                />
                                <LandingPageLink
                                    name={"Users"}
                                    to={Routes.users}
                                    color={"secondary"}
                                    style={{marginBottom: spacing(2)}}
                                />
                                <LandingPageLink
                                    name={"Content"}
                                    to={Routes.contentCreators}
                                    color={"secondary"}
                                    style={{marginBottom: spacing(2)}}
                                />
                            </div>
                            <Divider/>
                            <ListSubheader>
                                Decks for Sale
                            </ListSubheader>
                            <Box display={"flex"} flexWrap={"wrap"}>
                                <DeckSearchLink name={"For Sale"} filters={forSale} style={{marginBottom: spacing(2)}}/>
                                <DeckSearchLink
                                    name={"Trades"}
                                    filters={forTrade}
                                    style={{marginBottom: spacing(2)}}
                                />
                            </Box>

                            <Divider/>
                            <ListSubheader>
                                Cards
                            </ListSubheader>
                            <div style={{display: "flex", flexWrap: "wrap", paddingRight: spacing(1)}}>
                                <LandingPageLink
                                    name={"Search Cards"}
                                    to={Routes.cards}
                                    color={"secondary"}
                                />
                            </div>
                        </List>
                    </KeyDrawer>
                    <Box style={{flexGrow: 1}}>

                        <AnnouncementPaper maxWidth={800} style={{margin: spacing(4), marginBottom: spacing(2)}}>
                            <Typography variant={"h5"} gutterBottom={true}>
                                Calling all Creators!
                            </Typography>
                            <Typography variant={"body1"} style={{marginBottom: spacing(1)}}>
                                We've added a new feature to Decks of KeyForge so that community content creators can
                                list links to their content on the app!
                            </Typography>
                            <LinkButton variant={"outlined"} href={Routes.contentCreators} color={"primary"} style={{marginBottom: spacing(1)}}>
                                Check it out!
                            </LinkButton>
                            <Typography variant={"body1"} style={{marginBottom: spacing(1)}}>
                                Anyone can create a content creator listing, and we will feature both popular content
                                creators, as well as random new creators, on the landing page of the site. If you are a
                                patron, you may also upload an splash image for your content.
                            </Typography>
                        </AnnouncementPaper>
                        {screenStore.screenSizeSm() ? (
                            <div style={{display: "flex", flexWrap: "wrap", justifyContent: "center"}}>
                                <DeckSearchLink name={"Search"} filters={topSas} style={{marginTop: spacing(2)}}/>
                                <DeckSearchLink name={"For Sale"} filters={forSale} style={{marginTop: spacing(2)}}/>
                            </div>
                        ) : null}
                        <FeaturedSellersView/>
                        <FeaturedContentView/>
                        <div style={{marginLeft: spacing(2)}}>
                            <div style={{marginLeft: spacing(2)}}>
                                <UnstyledLink to={StatsSubPaths.winRates}>
                                    <LandingPageTitle>
                                        Stats
                                    </LandingPageTitle>
                                </UnstyledLink>
                            </div>
                            {stats == null ? <Loader/> : (
                                <div style={{display: "flex", flexWrap: "wrap"}}>
                                    <WinRateBar name={"SAS Win Rate"} data={stats.sasWinRate}/>
                                    <WinRateBar name={"AERC Win Rate"} data={stats.aercWinRate} secondary={true}/>
                                </div>
                            )}
                        </div>
                        <div style={{margin: spacing(4)}}>
                            <UnstyledLink to={AboutSubPaths.contact}>
                                <LandingPageTitle>
                                    Join in!
                                </LandingPageTitle>
                            </UnstyledLink>
                            <Box display={"flex"} flexWrap={"wrap"}>
                                <PatronButton size={"large"}
                                              style={{marginRight: spacing(2), marginBottom: spacing(2)}}/>
                                <DiscordButton style={{marginRight: spacing(2), marginBottom: spacing(2)}}/>
                                <GithubTicketsButton style={{marginBottom: spacing(2)}}/>
                            </Box>
                            <LandingPageTitle marginTop={2}>
                                Disclaimers
                            </LandingPageTitle>
                            <Typography style={{marginBottom: spacing(1)}} color={"textPrimary"}>
                                DoK (a.k.a. decksofkeyforge.com) is not associated with or endorsed by Ghost Galaxy,
                                the producers of KeyForge, in any way.
                            </Typography>
                            <Typography style={{marginBottom: spacing(1)}} color={"textPrimary"}>
                                DoK is owned and operated by Graylake LLC. For questions or comments check out
                                the <DokLink href={AboutSubPaths.contact}>contact me page</DokLink>!
                            </Typography>
                            <Typography style={{marginBottom: spacing(1)}} color={"textPrimary"}>
                                When using DoK you buy and sell decks entirely at your own risk. We make no guarantees
                                about the safety of
                                any transactions.
                            </Typography>
                            <LinkButton size={"small"} href={Routes.codeOfConduct} newWindow={true}
                                        style={{marginRight: spacing(2)}}>
                                Code of Conduct
                            </LinkButton>
                            <LinkButton size={"small"} href={Routes.termsOfUse} newWindow={true}
                                        style={{marginRight: spacing(2)}}>
                                Terms of Use
                            </LinkButton>
                            <LinkButton size={"small"} href={Routes.privacyPolicy} newWindow={true}>
                                Privacy Policy
                            </LinkButton>
                        </div>
                    </Box>
                </Box>
            </Box>
        )
    }
}

export const LandingPageTitle = observer((props: {
    children: string,
    marginTop?: number,
    linkName?: string,
    link?: string
}) => {
    return (
        <Box display={"flex"} alignItems={"flex-start"} mb={2} mt={props.marginTop ?? 4}>
            <Typography
                variant={"h4"}
                color={themeStore.darkMode ? "textPrimary" : "primary"}
            >
                {props.children}
            </Typography>
            {props.link != null && (
                <LinkButton
                    variant={"outlined"}
                    href={props.link}
                    color={"primary"}
                    style={{marginLeft: spacing(4)}}
                >
                    {props.linkName}
                </LinkButton>
            )}
        </Box>
    )
})
