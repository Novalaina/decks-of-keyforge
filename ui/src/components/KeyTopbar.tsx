import { Button, Divider, IconButton, List, ListItem, ListItemText, Tabs } from "@material-ui/core"
import AppBar from "@material-ui/core/AppBar/AppBar"
import Collapse from "@material-ui/core/Collapse"
import { blue, deepPurple } from "@material-ui/core/colors"
import Drawer from "@material-ui/core/Drawer"
import Toolbar from "@material-ui/core/Toolbar/Toolbar"
import Typography from "@material-ui/core/Typography/Typography"
import { ArrowBack, ExpandLess, ExpandMore } from "@material-ui/icons"
import MenuIcon from "@material-ui/icons/Menu"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import * as React from "react"
import { RouteComponentProps, withRouter } from "react-router"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { spacing, themeStore } from "../config/MuiConfig"
import { AboutSubPaths, MyDokSubPaths, Routes, StatsSubPaths } from "../config/Routes"
import { randomDeckMenuItem } from "../decks/RandomDeckFinder"
import { DeckFilters } from "../decks/search/DeckFilters"
import { DeckOrCardSearchSuggest } from "../decks/search/DeckOrCardSearchSuggest"
import {
    activeCardLinksExpansions,
    displayMyDecksLinksFor,
    expansionInfoMap,
    recentExpansions,
} from "../expansions/Expansions"
import { DokIcon } from "../generic/icons/DokIcon"
import { PatreonIcon } from "../generic/icons/PatreonIcon"
import { LinkMenu, LinkMenuStore } from "../generic/LinkMenu"
import { LinkTab } from "../generic/LinkTab"
import { UnstyledLink } from "../generic/UnstyledLink"
import { LinkButton, ListItemLink } from "../mui-restyled/LinkButton"
import { Loader } from "../mui-restyled/Loader"
import { ToolbarSpacer } from "../mui-restyled/ToolbarSpacer"
import { screenStore } from "../ui/ScreenStore"
import { uiStore } from "../ui/UiStore"
import { LoginPop } from "../user/LoginPop"
import { userStore } from "../user/UserStore"
import { ShareButton } from "./ShareButton"

interface KeyTopbarProps extends RouteComponentProps<{}> {
}

const myDeckLinks = () => {
    const links = [
        {to: Routes.usersDecks(), text: "My Decks", mobileActive: true},
        ...displayMyDecksLinksFor.map(expansion => ({
            to: Routes.decksForExpansions([expansionInfoMap.get(expansion)!.expansionNumber], true),
            text: `My ${expansionInfoMap.get(expansion)!.abbreviation}`,
            mobileActive: false
        })),
        {to: Routes.userDecksForSale(userStore.username!), text: "For Sale"},
        {to: Routes.sellersView(), text: "Sellers View", onClick: () => keyLocalStorage.setDeckListViewType("table")},
    ]
    if (userStore.hasTeam) {
        links.push(
            {to: Routes.teamDecks(), text: "My Team"}
        )
    }
    if (userStore.patron) {
        links.push(
            {to: Routes.analyzeUsersDecks(), text: "Analyze"}
        )
        links.push(
            {to: Routes.myAllianceDecks(), text: "Alliance Decks"}
        )
    }
    return links
}

export enum MenuStoreName {
    DECKS,
    MY_DECKS,
    CARDS,
    STATS,
    ABOUT,
    MY_DOK,
    COMMUNITY
}

const decksMenuStore = new LinkMenuStore(MenuStoreName.DECKS)
const myDecksMenuStore = new LinkMenuStore(MenuStoreName.MY_DECKS)
const cardsMenuStore = new LinkMenuStore(MenuStoreName.CARDS)
const communityMenuStore = new LinkMenuStore(MenuStoreName.COMMUNITY)
const statsMenuStore = new LinkMenuStore(MenuStoreName.STATS)
const aboutMenuStore = new LinkMenuStore(MenuStoreName.ABOUT)
const myDokMenuStore = new LinkMenuStore(MenuStoreName.MY_DOK)

export const closeAllMenuStoresExcept = (except?: MenuStoreName) => {
    const closeThese: LinkMenuStore[] = []
    if (except !== MenuStoreName.DECKS) {
        closeThese.push(decksMenuStore)
    }
    if (except !== MenuStoreName.MY_DECKS) {
        closeThese.push(myDecksMenuStore)
    }
    if (except !== MenuStoreName.CARDS) {
        closeThese.push(cardsMenuStore)
    }
    if (except !== MenuStoreName.STATS) {
        closeThese.push(statsMenuStore)
    }
    if (except !== MenuStoreName.COMMUNITY) {
        closeThese.push(communityMenuStore)
    }
    if (except !== MenuStoreName.ABOUT) {
        closeThese.push(aboutMenuStore)
    }
    if (except !== MenuStoreName.MY_DOK) {
        closeThese.push(myDokMenuStore)
    }
    closeThese.forEach(toClose => toClose.handleClose())
}

@observer
class KeyTopbarPlain extends React.Component<KeyTopbarProps> {

    render() {
        const {topbarName, topbarShortName, topbarSubheader} = uiStore

        if (this.props.location.pathname.includes("/printables")) {
            return null
        }

        let subheaderNode
        if (topbarSubheader) {
            subheaderNode = (
                <Typography
                    variant={"subtitle1"}
                    style={{marginLeft: spacing(2), marginTop: 12}}
                    color={"inherit"}>
                    {topbarSubheader}
                </Typography>
            )
        }

        const topbarNameToDisplay = screenStore.topbarNameShortened(topbarName, topbarShortName)

        let menuContents
        if (screenStore.smallScreenTopBar()) {
            menuContents = (
                <>
                    {uiStore.displayBack && (
                        <IconButton onClick={this.props.history.goBack} style={{marginRight: spacing(1)}}
                                    color={"inherit"}>
                            <ArrowBack color={"inherit"}/>
                        </IconButton>
                    )}
                    <div style={{marginRight: spacing(2)}}>
                        <UnstyledLink to={Routes.landing}><DokIcon/></UnstyledLink>
                    </div>
                    <Typography
                        variant={screenStore.screenSizeXs() ? "h5" : "h4"}
                        color={"inherit"}
                        noWrap={true}
                    >
                        {topbarNameToDisplay}
                    </Typography>
                    <div style={{flexGrow: 1}}/>
                    <DeckOrCardSearchSuggest placement={"bottom-end"}/>
                    {screenStore.screenWidth > 1000 && (
                        <>
                            <LinkMenu
                                genericOnClick={rightMenuStore.close}
                                links={[
                                    {to: Routes.decks, text: "Decks", mobileActive: true},
                                    {to: Routes.validAlliances(), text: "Alliances", mobileActive: true},
                                    {to: Routes.deckSearch(DeckFilters.forSale()), text: "For Sale"},
                                    randomDeckMenuItem,
                                ]}
                                dropdownOnly={true}
                                linkMenuStore={decksMenuStore}
                            />
                            {userStore.loggedIn() && (
                                <>
                                    <LinkMenu
                                        genericOnClick={rightMenuStore.close}
                                        links={myDeckLinks()}
                                        dropdownOnly={true}
                                        linkMenuStore={myDecksMenuStore}
                                    />
                                    <MyDokDropdown/>
                                </>
                            )}
                        </>
                    )}
                    {screenStore.smallDeckView() && (
                        <ShareButton
                            url={window.location.href}
                            title={topbarName}
                        />
                    )}
                    <RightMenu/>
                </>
            )
        } else {
            menuContents = (
                <>
                    {uiStore.displayBack && (
                        <IconButton
                            onClick={this.props.history.goBack}
                            style={{marginRight: spacing(1)}}
                            color={"inherit"}
                        >
                            <ArrowBack color={"inherit"}/>
                        </IconButton>
                    )}
                    <UnstyledLink to={Routes.landing}><DokIcon/></UnstyledLink>
                    <Typography
                        variant={"h4"}
                        style={{marginLeft: spacing(2)}}
                        color={"inherit"}
                        noWrap={true}
                    >
                        {topbarNameToDisplay}
                    </Typography>
                    {screenStore.screenWidth < 2000 ? null : subheaderNode}
                    <div style={{flexGrow: 1}}/>
                    <DeckOrCardSearchSuggest placement={"bottom-start"}/>
                    <RightMenu/>
                </>
            )
        }

        const onMyDok = this.props.location.pathname.includes(Routes.myDok)
        const onAboutPage = this.props.location.pathname.includes(Routes.about)
        const onStatsPage = this.props.location.pathname.includes(Routes.stats)
        let background
        if (themeStore.altColors) {
            background = themeStore.darkMode ? deepPurple["800"] : undefined
        } else {
            background = themeStore.darkMode ? blue["800"] : undefined
        }
        return (
            <div>
                <AppBar
                    position={"fixed"}
                    style={{
                        zIndex: screenStore.zindexes.keyTopBar,
                        background
                    }}
                >
                    <Toolbar
                        style={{
                            paddingLeft: screenStore.smallDeckView() ? spacing(2) : undefined,
                            paddingRight: screenStore.smallDeckView() ? spacing(2) : undefined
                        }}
                    >
                        {menuContents}
                    </Toolbar>
                    {(onMyDok || onAboutPage || onStatsPage) && (
                        <>
                            <Divider style={{backgroundColor: "rgb(255, 255, 255, 0.25)"}}/>
                            <Toolbar variant={"dense"}>
                                {onMyDok && (
                                    <Tabs
                                        value={this.props.location.pathname}
                                        variant={"scrollable"}
                                    >
                                        <LinkTab label="Messages" to={MyDokSubPaths.messages}
                                                 value={MyDokSubPaths.messages}/>
                                        <LinkTab label="Profile" to={MyDokSubPaths.profile}
                                                 value={MyDokSubPaths.profile}/>
                                        <LinkTab label="Offers" to={MyDokSubPaths.offers} value={MyDokSubPaths.offers}/>
                                        <LinkTab label="Bought / Sold" to={MyDokSubPaths.purchases}
                                                 value={MyDokSubPaths.purchases}/>
                                        <LinkTab label="Notifications" to={MyDokSubPaths.notifications}
                                                 value={MyDokSubPaths.notifications}/>
                                        <LinkTab label="My Team" to={MyDokSubPaths.team} value={MyDokSubPaths.team}/>
                                    </Tabs>
                                )}
                                {onAboutPage && (
                                    <Tabs
                                        value={this.props.location.pathname}
                                        variant={"scrollable"}
                                    >
                                        <LinkTab label="SAS and AERC" to={AboutSubPaths.sas} value={AboutSubPaths.sas}/>
                                        <LinkTab label="Patron Rewards" to={AboutSubPaths.patreon}
                                                 value={AboutSubPaths.patreon}/>
                                        <LinkTab label="Contact Me" to={AboutSubPaths.contact}
                                                 value={AboutSubPaths.contact}/>
                                        <LinkTab label="Release Notes" to={AboutSubPaths.releaseNotes}
                                                 value={AboutSubPaths.releaseNotes}/>
                                        <LinkTab label="API" to={AboutSubPaths.sellersAndDevs}
                                                 value={AboutSubPaths.sellersAndDevs}/>
                                    </Tabs>
                                )}
                                {onStatsPage && (
                                    <Tabs
                                        value={this.props.location.pathname}
                                        variant={"scrollable"}
                                    >
                                        <LinkTab label="Win Rates" to={StatsSubPaths.winRates}
                                                 value={StatsSubPaths.winRates}/>
                                        <LinkTab label="Decks" to={StatsSubPaths.deckStats}
                                                 value={StatsSubPaths.deckStats}/>
                                        <LinkTab label="AERC" to={StatsSubPaths.aercStats}
                                                 value={StatsSubPaths.aercStats}/>
                                        <LinkTab label="Purchases" to={StatsSubPaths.purchaseStats}
                                                 value={StatsSubPaths.purchaseStats}/>
                                    </Tabs>
                                )}
                            </Toolbar>
                        </>
                    )}
                </AppBar>
                <ToolbarSpacer/>
            </div>
        )
    }
}

class RightMenuStore {
    @observable
    open = false

    @observable
    communityOpen = false

    @observable
    aboutOpen = false

    close = () => {
        this.open = false
        this.aboutOpen = false
        this.communityOpen = false
    }

    constructor() {
        makeObservable(this)
    }
}

export const rightMenuStore = new RightMenuStore()

@observer
class RightMenu extends React.Component {

    render() {
        if (screenStore.smallScreenTopBar()) {
            return (
                <>
                    <IconButton
                        onClick={() => rightMenuStore.open = !rightMenuStore.open}
                        color={"inherit"}
                    >
                        <MenuIcon/>
                    </IconButton>

                    <Drawer
                        open={rightMenuStore.open}
                        onClose={rightMenuStore.close}
                        anchor={"right"}
                        style={{zIndex: screenStore.zindexes.rightMenu}}
                    >
                        <div style={{display: "flex", flexDirection: "column", minWidth: 160}}>
                            <AppLinks/>
                            <Divider style={{margin: spacing(2)}}/>
                            <UserLinks/>
                        </div>
                    </Drawer>

                </>
            )
        }

        return (
            <>
                <AppLinks/>
                <div
                    style={{
                        display: "flex",
                        borderLeft: "1px solid rgb(255, 255, 255, 0.25)",
                        marginLeft: spacing(2),
                        paddingLeft: spacing(2)
                    }}
                >
                    <UserLinksDesktop/>
                </div>
            </>
        )
    }
}

const AppLinks = observer(() => (
    <>
        <LinkMenu
            genericOnClick={rightMenuStore.close}
            links={[
                {to: Routes.decks, text: "Decks", mobileActive: true},
                ...recentExpansions.map(expansion => ({
                    to: Routes.decksForExpansions([expansionInfoMap.get(expansion)!.expansionNumber]),
                    text: `${expansionInfoMap.get(expansion)!.abbreviation}`,
                    mobileActive: false
                })),
                {to: Routes.validAlliances(), text: "Alliances", mobileActive: true},
                {to: Routes.importDecks, text: "Import Deck", mobileActive: true},
                {to: Routes.deckSearch(DeckFilters.forSale()), text: "For Sale"},
                randomDeckMenuItem,
            ]}
            linkMenuStore={decksMenuStore}
        />
        <LinkMenu
            genericOnClick={rightMenuStore.close}
            links={[
                {to: Routes.cards, text: "Cards", mobileActive: true},
                ...activeCardLinksExpansions.map(expansion => ({
                    to: Routes.cardsForExpansion(expansionInfoMap.get(expansion)!.expansionNumber),
                    text: `${expansionInfoMap.get(expansion)!.abbreviation} Cards`,
                    mobileActive: false
                }))
            ]}
            linkMenuStore={cardsMenuStore}
        />
        <LinkMenu
            genericOnClick={rightMenuStore.close}
            links={[
                {to: StatsSubPaths.winRates, text: "Stats", mobileActive: true},
                {to: StatsSubPaths.winRates, text: "Win Rates", mobileActive: false},
                {to: StatsSubPaths.deckStats, text: "Deck Stats", mobileActive: false},
                {to: StatsSubPaths.aercStats, text: "AERC Stats", mobileActive: false},
                {to: StatsSubPaths.purchaseStats, text: "Sale Stats", mobileActive: false},
            ]}
            linkMenuStore={statsMenuStore}
        />
        {screenStore.smallScreenTopBar() ? (
            <>
                <ListItem
                    button={true}
                    onClick={() => rightMenuStore.communityOpen = !rightMenuStore.communityOpen}
                >
                    <ListItemText primary={"Community"}/>
                    {rightMenuStore.communityOpen ? <ExpandLess/> : <ExpandMore/>}
                </ListItem>
                <Collapse in={rightMenuStore.communityOpen} timeout="auto" unmountOnExit>
                    <List component="div" disablePadding>
                        <ListItemLink
                            to={Routes.users}
                            onClick={rightMenuStore.close}
                            primary={"Users"}
                        />
                        <ListItemLink
                            to={Routes.tags}
                            onClick={rightMenuStore.close}
                            primary={"Tagged Decks"}
                        />
                        <ListItemLink
                            to={Routes.tournaments}
                            onClick={rightMenuStore.close}
                            primary={"Tournaments"}
                        />
                        <ListItemLink
                            to={Routes.contentCreators}
                            onClick={rightMenuStore.close}
                            primary={"Content Creators"}
                        />
                        <ListItemLink
                            to={Routes.thirdPartyTools}
                            onClick={rightMenuStore.close}
                            primary={"3rd Party Tools"}
                        />
                    </List>
                </Collapse>
            </>
        ) : (
            <LinkMenu
                genericOnClick={rightMenuStore.close}
                links={[
                    {to: Routes.community, text: "Community", mobileActive: false},
                    {to: Routes.users, text: "Users", mobileActive: true},
                    {to: Routes.tags, text: "Tagged Decks", mobileActive: true},
                    {to: Routes.tournaments, text: "Tournaments", mobileActive: true},
                    {to: Routes.contentCreators, text: "Content Creators", mobileActive: true},
                    {to: Routes.thirdPartyTools, text: "3rd Party Tools", mobileActive: false},
                ]}
                linkMenuStore={communityMenuStore}
            />
        )}
        {screenStore.smallScreenTopBar() ? (
            <>
                <ListItem
                    button={true}
                    onClick={() => rightMenuStore.aboutOpen = !rightMenuStore.aboutOpen}
                >
                    <ListItemText primary={"About"}/>
                    {rightMenuStore.aboutOpen ? <ExpandLess/> : <ExpandMore/>}
                </ListItem>
                <Collapse in={rightMenuStore.aboutOpen} timeout="auto" unmountOnExit>
                    <List component="div" disablePadding>
                        <ListItemLink
                            to={AboutSubPaths.sas}
                            onClick={rightMenuStore.close}
                            primary={"SAS and AERC"}
                        />
                        <ListItemLink
                            to={AboutSubPaths.contact}
                            onClick={rightMenuStore.close}
                            primary={"Contact Me"}
                        />
                        <ListItemLink
                            to={AboutSubPaths.releaseNotes}
                            onClick={rightMenuStore.close}
                            primary={"Release Notes"}
                        />
                        <ListItemLink
                            to={AboutSubPaths.sellersAndDevs}
                            onClick={rightMenuStore.close}
                            primary={"APIs"}
                        />
                    </List>
                </Collapse>
            </>
        ) : (
            <LinkMenu
                genericOnClick={rightMenuStore.close}
                links={[
                    {to: AboutSubPaths.sas, text: "About", mobileActive: true},
                    {to: AboutSubPaths.sas, text: "SAS and AERC"},
                    {to: AboutSubPaths.patreon, text: "Patron Rewards"},
                    {to: AboutSubPaths.contact, text: "Contact Me"},
                    {to: AboutSubPaths.releaseNotes, text: "Release Notes"},
                    {to: AboutSubPaths.sellersAndDevs, text: "APIs"},
                ]}
                linkMenuStore={aboutMenuStore}
            />
        )}
    </>
))

@observer
class UserLinks extends React.Component {
    @observable
    buttonAnchor?: HTMLElement

    constructor(props: {}) {
        super(props)
        makeObservable(this)
    }

    render() {
        if (!userStore.loggedIn() && userStore.loginInProgress) {
            return <Loader/>
        } else if (userStore.loggedIn()) {
            return (
                <>
                    <LinkMenu
                        genericOnClick={rightMenuStore.close}
                        links={myDeckLinks()}
                        linkMenuStore={myDecksMenuStore}
                    />
                    <ListItemLink
                        to={MyDokSubPaths.profile}
                        onClick={rightMenuStore.close}
                        primary={"My DoK"}
                    />
                    <ListItemLink
                        to={MyDokSubPaths.messages}
                        onClick={rightMenuStore.close}
                        primary={"Messages"}
                    />
                    <ListItemLink
                        to={AboutSubPaths.patreon}
                        onClick={rightMenuStore.close}
                        primary={"Patreon"}
                        icon={<PatreonIcon primary={true}/>}
                    />
                    <ListItem
                        button={true}
                        onClick={() => {
                            userStore.logout()
                            rightMenuStore.close()
                        }}
                    >
                        <ListItemText primary={"Logout"}/>
                    </ListItem>
                </>
            )
        } else {
            return (
                <>
                    <LoginPop
                        style={{margin: spacing(1)}}
                    />
                    <LinkButton
                        color={"secondary"}
                        variant={"contained"}
                        href={Routes.registration}
                        style={{margin: spacing(1)}}
                        onClick={rightMenuStore.close}
                    >
                        Sign Up
                    </LinkButton>
                </>
            )
        }
    }
}

@observer
class UserLinksDesktop extends React.Component {
    @observable
    buttonAnchor?: HTMLElement

    constructor(props: {}) {
        super(props)
        makeObservable(this)
    }

    render() {
        if (!userStore.loggedIn() && userStore.loginInProgress) {
            return <Loader/>
        } else if (userStore.loggedIn()) {
            return (
                <>
                    <LinkMenu
                        genericOnClick={rightMenuStore.close}
                        links={myDeckLinks()}
                        linkMenuStore={myDecksMenuStore}
                    />
                    <MyDokDropdown/>
                    <LinkButton
                        color={"inherit"}
                        href={AboutSubPaths.patreon}
                        onClick={rightMenuStore.close}
                    >
                        <PatreonIcon style={{marginRight: spacing(1)}} primary={screenStore.smallScreenTopBar()}/>
                        Patron Rewards
                    </LinkButton>
                    <Button
                        variant={"outlined"}
                        color={"inherit"}
                        onClick={userStore.logout}
                        style={{marginLeft: spacing(1)}}
                    >
                        Logout
                    </Button>
                </>
            )
        } else {
            return (
                <>
                    <LoginPop
                        style={{display: "flex", justifyContent: "center"}}
                    />
                    <LinkButton
                        color={"secondary"}
                        variant={"contained"}
                        href={Routes.registration}
                        onClick={rightMenuStore.close}
                        style={{marginLeft: spacing(2)}}
                    >
                        Sign Up
                    </LinkButton>
                </>
            )
        }
    }
}

const MyDokDropdown = () => {

    const links = [
        {to: MyDokSubPaths.profile, text: "My DoK", mobileActive: true},
        {to: MyDokSubPaths.messages, text: "Messages", mobileActive: true},
        {to: MyDokSubPaths.profile, text: "Profile", mobileActive: false},
        {to: MyDokSubPaths.offers, text: "Offers", mobileActive: false},
        {to: MyDokSubPaths.purchases, text: "Bought / Sold", mobileActive: false},
        {to: MyDokSubPaths.notifications, text: "Notifications", mobileActive: false},
        {to: MyDokSubPaths.team, text: "My Team", mobileActive: false},
    ]

    if (userStore.isTeamSas) {
        links.push({to: Routes.searchGames, text: "Search Games", mobileActive: true})
    }
    if (userStore.isAdmin) {
        links.push({to: Routes.adminPanel, text: "Admin Panel", mobileActive: true})
    }

    return (
        <LinkMenu
            genericOnClick={rightMenuStore.close}
            links={links}
            dropdownOnly={true}
            linkMenuStore={myDokMenuStore}
        />
    )
}

export const KeyTopbar = withRouter(KeyTopbarPlain)
