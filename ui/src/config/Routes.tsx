import { Box, Typography } from "@material-ui/core"
import { observer } from "mobx-react"
import * as React from "react"
import { BrowserRouter, Route, Switch } from "react-router-dom"
import { AboutPage } from "../about/AboutPage"
import { CommunityPage } from "../about/CommunityPage"
import { PrivacyPolicy } from "../about/PrivacyPolicy"
import { ThirdPartyIntegrations } from "../about/ThirdPartyIntegrations"
import { AdminPanelView } from "../admin/AdminPanelView"
import { CardFilters, cardFiltersToQueryString } from "../cards/CardFilters"
import { CardSearchPage } from "../cards/CardSearchPage"
import { cardNameToCardNameKey } from "../cards/KCard"
import { CardPage } from "../cards/views/CardPage"
import { ChangePasswordPage } from "../components/ChangePasswordPage"
import { ForgotPasswordPage } from "../components/ForgotPasswordPage"
import { KeyTopbar } from "../components/KeyTopbar"
import { VerifyEmailPage } from "../components/VerifyEmailPage"
import { DeckComparisonView } from "../decks/comparison/DeckComparisonView"
import { DeckViewPage } from "../decks/DeckViewFull"
import { prepareForSaleQueryString } from "../decks/salenotifications/ForSaleNotificationsStore"
import { CollectionStatsSearchPage } from "../decks/search/CollectionStatsSearchPage"
import { DeckFilters, deckFiltersToQueryString } from "../decks/search/DeckFilters"
import { DeckSearchPage } from "../decks/search/DeckSearchPage"
import { ExpansionNumber } from "../expansions/Expansions"
import { UpdateExtraCardInfoPage } from "../extracardinfo/UpdateExtraCardInfoPage"
import { SaleNotificationQueryDto } from "../generated-src/SaleNotificationQueryDto"
import { DokIcon } from "../generic/icons/DokIcon"
import { CreateTheoreticalDeck } from "../importdeck/theoretical/CreateTheoreticalDeck"
import { ViewMyTheoreticalDecks } from "../importdeck/theoretical/ViewMyTheoreticalDecks"
import { ViewTheoreticalDeck } from "../importdeck/theoretical/ViewTheoreticalDeck"
import { TournamentPage } from "../keyforgeevents/tournaments/TournamentPage"
import { TournamentPrintables } from "../keyforgeevents/tournaments/TournamentPrintables"
import { TournamentSearchPage } from "../keyforgeevents/tournaments/TournamentSearchPage"
import { LandingPage } from "../landing/LandingPage"
import { ViewMessagePage } from "../messages/ViewMessage"
import { MyDokPage } from "../my-dok/MyDokPage"
import { StatsPage } from "../stats/StatsPage"
import { TagSearchPage } from "../tags/TagSearchPage"
import { SnackMessage } from "../ui/MessageStore"
import { CodeOfConduct } from "../user/CodeOfConduct"
import { ProfilePage } from "../user/ProfilePage"
import { RegistrationPage } from "../user/RegistrationPage"
import { UserSearchPage } from "../user/search/UserSearchPage"
import { AgreeToTerms, TermsOfUse } from "../user/TermsOfUse"
import { userStore } from "../user/UserStore"
import { KeyLoaderBar } from "./KeyLoaderBar"
import { LoggedInRoute } from "./LoggedInRoute"
import { spacing, themeStore } from "./MuiConfig"
import { serverStatusStore } from "./ServerStatusStore"
import { ViewMyOldAllianceDecks } from "../importdeck/theoretical/ViewMyOldAllianceDecks"
import { GamesSearchPage } from "../gamestracker/GamesSearchPage"
import { AllianceDeckFilters } from "../generated-src/AllianceDeckFilters"
import { AllianceDeckViewPage } from "../alliancedecks/AllianceDeckViewPage"
import { AllianceDeckSearchPage } from "../alliancedecks/AllianceDeckSearchPage"
import { AllianceDeckFiltersUtils } from "../alliancedecks/AllianceDeckFiltersUtils"
import { DeckNameId } from "../decks/comparison/CompareDecks"
import { DeckType } from "../generated-src/DeckType"
import { CardHistoryPage } from "../cards/views/CardHistoryPage"
import { DeckImportPage } from "../importdeck/DeckImportPage"
import { ContentCreatorSearchPage } from "../contentcreators/ContentCreatorSearchPage"
import { CreateContentCreatorPage } from "../contentcreators/CreateContentCreatorPage"

class Routes {

    static landing = ""
    static users = "/users"
    static community = "/community"
    static contentCreators = "/content-creators"
    static myDok = "/my-dok"
    static tournaments = "/tournaments"
    static messages = "/messages"
    static adminPanel = "/admin-panel"
    static cards = "/cards"
    static extraCardInfo = "/extra-card-infos"
    static about = "/about"
    static decks = "/decks"
    static allianceDecks = "/alliance-decks"
    static tags = "/tags"
    static gamesTracker = "/games-tracker"
    static collectionStats = `/analyze-collection`
    static theoreticalDecks = "/theoretical-decks"
    static oldAlliancesRoute = "/alliances"
    static compareDecks = "/compare-decks"
    static stats = "/stats"
    static registration = "/registration"
    static forgotPassword = "/forgot-password"
    static privacyPolicy = "/privacy-policy"
    static termsOfUse = "/terms-of-use"
    static codeOfConduct = "/code-of-conduct"
    static thirdPartyTools = "/third-party-tools"
    static importDecks = "/import-decks"
    static myTournaments = `${Routes.tournaments}?mineOnly=true`
    static myTheoreticalDecks = `${Routes.theoreticalDecks}/mine`
    static oldMyAllianceDecks = `${Routes.oldAlliancesRoute}/mine`
    static createTheoreticalDeck = `${Routes.theoreticalDecks}/create`
    static createContentCreator = `${Routes.contentCreators}/create`
    static editContentCreator = (id?: string) => `${Routes.contentCreators}/edit/${id == null ? ":id" : id}`
    static searchGames = `${Routes.gamesTracker}/search`
    static validAlliances = () => `${Routes.allianceDecks}?validOnly=true`
    static myAllianceDecks = () => `${Routes.allianceDecks}?owners=${userStore.username}&validOnly=true`
    static editExtraCardInfo = (infoId?: string | number) => `${Routes.extraCardInfo}/edit/${infoId == null ? ":infoId" : infoId}`
    static theoreticalDeckPage = (id?: string) => `${Routes.theoreticalDecks}/${id == null ? ":id" : id}`
    static allianceDeckPage = (id?: string) => `${Routes.allianceDecks}/${id == null ? ":id" : id}`
    static messagePage = (id?: number) => `${Routes.messages}/${id == null ? ":id" : id}`
    static deckPage = (keyforgeDeckId?: string) => `${Routes.decks}/${keyforgeDeckId == null ? ":keyforgeDeckId" : keyforgeDeckId}`
    static cardPage = (cardName?: string) => `${Routes.cards}/${cardName == null ? ":cardName" : cardNameToCardNameKey(cardName)}`
    static cardHistory = (cardName?: string) => `${Routes.cards}/history/${cardName ?? ":cardName"}`
    static tournamentPage = (id?: number) => `${Routes.tournaments}/${id == null ? ":id" : id}`
    static tournamentPrintables = (id?: number) => `${Routes.tournaments}/printables/${id == null ? ":id" : id}`
    static changePasswordPage = (resetCode?: string) => `/reset-password/${resetCode == null ? ":resetCode" : resetCode}`
    static verifyEmailPage = (verificationCode?: string) => `/verify-email/${verificationCode == null ? ":verificationCode" : verificationCode}`
    static userProfilePage = (username?: string) => `${Routes.users}/${username == null ? ":username" : username}`
    static usersDecks = () => `/decks?owner=${userStore.username}`
    static analyzeUsersDecks = () => `/analyze-collection?owner=${userStore.username}`
    static teamDecks = () => `/decks?teamDecks=true`
    static userContent = (key: string) => `https://dok-user-content.s3-us-west-2.amazonaws.com/${key}`
    static compareDecksWithIds = (decks: DeckNameId[]) => {
        const normalDeckIds = decks
            .filter(deck => deck.type === DeckType.STANDARD)
            .map(deck => `decks=${deck.keyforgeId}`)
        const allianceDeckIds = decks
            .filter(deck => deck.type === DeckType.ALLIANCE)
            .map(deck => `allianceDecks=${deck.keyforgeId}`)
        const allIds = normalDeckIds.concat(allianceDeckIds).join("&")
        return `${Routes.compareDecks}?${allIds}`
    }

    /**
     * Deck filters should be cleaned.
     * @param filters
     */
    static analyzeDeckSearch = (filters: DeckFilters) => {
        const stringified = deckFiltersToQueryString(filters)
        if (stringified === "") {
            return Routes.collectionStats
        }
        return `${Routes.collectionStats}?${stringified}`
    }

    /**
     * Deck filters should be cleaned.
     * @param filters
     */
    static deckSearch = (filters: DeckFilters) => {
        const stringified = deckFiltersToQueryString(filters)
        if (stringified === "") {
            return Routes.decks
        }
        return `${Routes.decks}?${stringified}`
    }

    /**
     * Deck filters should be cleaned.
     * @param filters
     */
    static allianceDeckSearch = (filters: AllianceDeckFilters) => {
        const stringified = AllianceDeckFiltersUtils.deckFiltersToQueryString(filters)
        if (stringified === "") {
            return Routes.allianceDecks
        }
        return `${Routes.allianceDecks}?${stringified}`
    }

    static tournamentDecksSearch = (tournamentId: number) => {
        const filters = new DeckFilters()
        filters.tournamentIds = [tournamentId]
        return Routes.deckSearch(filters)
    }

    static decksForUser = (username: string) => {
        const filters = new DeckFilters()
        filters.owner = username
        return Routes.deckSearch(filters)
    }

    static decksForUserOnMyTeam = (username: string) => {
        const filters = new DeckFilters()
        filters.owner = username
        filters.teamDecks = true
        return Routes.deckSearch(filters)
    }

    static decksForSale = () => {
        const filters = new DeckFilters()
        filters.forSale = true
        return Routes.deckSearch(filters)
    }

    static cardsForExpansion = (expansion: ExpansionNumber) => {
        const filters = new CardFilters()
        filters.expansions = [expansion]
        return Routes.cardSearch(filters)
    }

    static decksForExpansions = (expansions: ExpansionNumber[], mine?: boolean) => {
        const filters = new DeckFilters()
        filters.expansions = expansions.slice()
        if (mine && userStore.username != null) {
            filters.owner = userStore.username
        }
        return Routes.deckSearch(filters)
    }

    /**
     * Card filters should be cleaned.
     * @param filters
     */
    static cardSearch = (filters: CardFilters) => {
        const stringified = cardFiltersToQueryString(filters)
        if (stringified === "") {
            return Routes.cards
        }
        return `${Routes.cards}?${stringified}`
    }

    static cardsUpdate = (aercHistoryDate: string) => {
        const filters = new CardFilters()
        filters.aercHistory = true
        filters.aercHistoryDate = aercHistoryDate
        return Routes.cardSearch(filters)
    }

    static deckSearchForSaleQuery = (filters: SaleNotificationQueryDto) => {
        return `${Routes.decks}?${prepareForSaleQueryString(filters)}`
    }

    static userDecksForSale = (username: string) => {
        const filters = DeckFilters.forSale()
        filters.owner = username
        return Routes.deckSearch(filters)
    }

    static sellersView = () => {
        const filters = DeckFilters.forSale()
        filters.owner = userStore.username!
        filters.forSale = true
        return Routes.deckSearch(filters)
    }

}

const KeyRouter = observer(() => {

    if (serverStatusStore.siteUpdating) {
        return (
            <Box
                display={"flex"}
                flexDirection={"column"}
                alignItems={"center"}
                justifyContent={"center"}
                mt={4}
            >
                <DokIcon height={80} style={{}}/>
                <Box
                    mt={2}
                    p={2}
                    style={{borderRadius: 8, backgroundColor: themeStore.darkBackgroundColor}}
                >
                    <Typography
                        variant={"h4"}
                        color={"textPrimary"}
                    >
                        Site update in progress
                    </Typography>
                    <Typography
                        style={{marginTop: spacing(2)}}
                        color={"textSecondary"}
                    >
                        Refresh this page to check if the update is complete.
                    </Typography>
                </Box>
            </Box>
        )
    }

    return (
        <BrowserRouter>
            <div>
                <KeyTopbar/>
                <div style={{marginBottom: spacing(2)}}/>
                <Switch>
                    <LoggedInRoute
                        exact={true}
                        path={Routes.adminPanel}
                        component={AdminPanelView}
                    />
                    <Route
                        exact={true}
                        path={Routes.createTheoreticalDeck}
                        component={CreateTheoreticalDeck}
                    />
                    <Route
                        exact={true}
                        path={Routes.myTheoreticalDecks}
                        component={ViewMyTheoreticalDecks}
                    />
                    <Route
                        exact={true}
                        path={Routes.oldMyAllianceDecks}
                        component={ViewMyOldAllianceDecks}
                    />
                    <Route
                        exact={true}
                        path={Routes.searchGames}
                        component={GamesSearchPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.allianceDeckPage()}
                        component={AllianceDeckViewPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.importDecks}
                        component={DeckImportPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.theoreticalDeckPage()}
                        component={ViewTheoreticalDeck}
                    />
                    <Route
                        exact={true}
                        path={Routes.allianceDeckPage()}
                        component={DeckViewPage}
                    />
                    <Route
                        path={Routes.collectionStats}
                        component={CollectionStatsSearchPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.deckPage()}
                        component={DeckViewPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.cardPage()}
                        component={CardPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.cardHistory()}
                        component={CardHistoryPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.tournaments}
                        component={TournamentSearchPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.tournamentPage()}
                        component={TournamentPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.tournamentPrintables()}
                        component={TournamentPrintables}
                    />
                    <Route
                        exact={true}
                        path={Routes.messagePage()}
                        component={ViewMessagePage}
                    />
                    <Route
                        exact={true}
                        path={Routes.userProfilePage()}
                        component={ProfilePage}
                    />
                    <Route
                        path={Routes.about}
                        component={AboutPage}
                    />
                    <LoggedInRoute
                        path={Routes.myDok}
                        component={MyDokPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.cards}
                        component={CardSearchPage}
                    />
                    <Route
                        path={Routes.allianceDecks}
                        component={AllianceDeckSearchPage}
                    />
                    <Route
                        path={Routes.decks}
                        component={DeckSearchPage}
                    />
                    <Route
                        path={Routes.compareDecks}
                        component={DeckComparisonView}
                    />
                    <Route
                        path={Routes.tags}
                        component={TagSearchPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.createContentCreator}
                        component={CreateContentCreatorPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.editContentCreator()}
                        component={CreateContentCreatorPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.contentCreators}
                        component={ContentCreatorSearchPage}
                    />
                    <Route
                        path={Routes.community}
                        component={CommunityPage}
                    />
                    <Route
                        path={Routes.users}
                        component={UserSearchPage}
                    />
                    <Route
                        path={Routes.thirdPartyTools}
                        component={ThirdPartyIntegrations}
                    />
                    <Route
                        path={Routes.stats}
                        component={StatsPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.registration}
                        component={RegistrationPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.forgotPassword}
                        component={ForgotPasswordPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.privacyPolicy}
                        component={PrivacyPolicy}
                    />
                    <Route
                        exact={true}
                        path={Routes.termsOfUse}
                        component={TermsOfUse}
                    />
                    <Route
                        exact={true}
                        path={Routes.codeOfConduct}
                        component={CodeOfConduct}
                    />
                    <Route
                        exact={true}
                        path={Routes.changePasswordPage()}
                        component={ChangePasswordPage}
                    />
                    <Route
                        exact={true}
                        path={Routes.verifyEmailPage()}
                        component={VerifyEmailPage}
                    />
                    <LoggedInRoute
                        exact={true}
                        path={Routes.editExtraCardInfo()}
                        component={UpdateExtraCardInfoPage}
                    />
                    <Route
                        path={Routes.landing}
                        component={LandingPage}
                    />
                </Switch>
                <SnackMessage/>
                <KeyLoaderBar/>
                <AgreeToTerms/>
            </div>
        </BrowserRouter>
    )
})

export class AboutSubPaths {
    static sas = Routes.about + "/sas"
    static patreon = Routes.about + "/patreon"
    static contact = Routes.about + "/contact"
    static releaseNotes = Routes.about + "/release-notes"
    static sellersAndDevs = Routes.about + "/sellers-and-devs"
}

export class StatsSubPaths {
    static winRates = Routes.stats + "/win-rates"
    static deckStats = Routes.stats + "/deck-stats"
    static aercStats = Routes.stats + "/aerc-stats"
    static purchaseStats = Routes.stats + "/purchase-stats"
}

export class MyDokSubPaths {
    static messages = Routes.myDok + "/messages"
    static profile = Routes.myDok + "/my-profile"
    static notifications = Routes.myDok + "/notifications"
    static offers = Routes.myDok + "/offers"
    static purchases = Routes.myDok + "/bought-and-sold"
    static team = Routes.myDok + "/my-team"
}

export {
    Routes,
    KeyRouter,
}
