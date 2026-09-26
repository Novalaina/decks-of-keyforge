import { configure } from "mobx"
import * as React from "react"
import * as ReactDOM from "react-dom"
import { App } from "./App"
import { cardStore } from "./cards/CardStore"
import { HttpConfig } from "./config/HttpConfig"
import { IdbUtils } from "./config/IdbUtils"
import { monitoring } from "./config/Monitoring"
import { serverStatusStore } from "./config/ServerStatusStore"
import { TextConfig } from "./config/TextConfig"
import { userMessageStore } from "./messages/UserMessageStore"
import { sellerRatingsStore } from "./sellerratings/SellerRatingsStore"
import { sellerStore } from "./sellers/SellerStore"
import { contentCreatorStore } from "./contentcreators/ContentCreatorStore"
import { statsStore } from "./stats/StatsStore"
import { teamStore } from "./teams/TeamStore"
import { userStore } from "./user/UserStore"

configure({enforceActions: "never"})

const load = async () => {
    monitoring.initialize()
    TextConfig.loadFonts()
    HttpConfig.setupAxios()
    serverStatusStore.checkIfUpdating()
    sellerRatingsStore.findSellerRatings()
    sellerStore.findFeaturedSellers()
    contentCreatorStore.findFeaturedContent()
    userStore.loadUserInfo()
    teamStore.findAllTeamNames()
    userMessageStore.checkUnreadMessages()
    await IdbUtils.canUseIdb()
    cardStore.loadAllCards()
    statsStore.findGlobalStats()
}

load()

ReactDOM.render(<App/>, document.getElementById("root"))
