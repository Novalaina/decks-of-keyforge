import { DeckSearchResult } from "../models/DeckSearchResult"
import { Box } from "@material-ui/core"
import { spacing } from "../../config/MuiConfig"
import { ExpansionIcon } from "../../expansions/ExpansionIcon"
import { DeckScoreView } from "../DeckScoreView"
import { OrganizedPlayStats } from "../OrganizedPlayStats"
import * as React from "react"
import { DeckViewDeckName } from "./DeckViewDeckName"
import { DeckTokenView, deckTopTokenWidth } from "./DeckTokenView"
import { DeckSaleIcons } from "./DeckSaleIcons"
import { Expansion } from "../../generated-src/Expansion"
import { DeckAllianceText } from "./DeckAllianceText"
import { CardAsLine } from "../../cards/views/CardAsLine"

interface BannerProps {
    deck: DeckSearchResult
    fullVersion: boolean
    fake: boolean
}

export const DeckTopBanner = (props: BannerProps & { compact: boolean }) => {
    if (props.compact) {
        return <DeckTopBannerCompact  {...props} />
    }

    return <DeckTopBannerFull {...props} />
}

const DeckTopBannerFull = (props: BannerProps) => {
    const {deck, fullVersion, fake} = props
    const token = deck.tokenInfo
    const archonPower = deck.archonPower

    let specialCard
    if (token != null) {
        specialCard = (
            <CardAsLine
                card={{cardTitle: token.name, cardTitleUrl: token.nameUrl}}
                deck={deck}
                width={deckTopTokenWidth}
                img={true}
                cardActualHouse={token.house}
                copies={deck.tokenCreationValues?.tokensPerGame}
            />
        )
    } else if (deck.expansion === Expansion.DARK_TIDINGS) {
        specialCard = (
            <DeckTokenView
                tokenNameUrl={"https://keyforge-card-images.s3.us-west-2.amazonaws.com/card-images-houses/tide-card.png"}
            />
        )
    } else if (archonPower != null) {
        specialCard = (
            <CardAsLine
                card={archonPower}
                deck={deck}
                width={deckTopTokenWidth}
                img={true}
            />
        )
    }

    return (
        <>
            {specialCard != null && (
                <Box>
                    {specialCard}
                </Box>
            )}
            <Box flexGrow={1} display={"flex"} flexDirection={"column"} ml={2}>
                <Box flexGrow={1}/>
                <Box display={"flex"} mb={2} alignItems={"flexEnd"} flexGrow={1}>
                    <ExpansionIcon expansion={deck.expansion} size={40} white={true} style={{marginRight: spacing(1)}}/>
                    <DeckSaleIcons deck={deck}/>
                    <Box flexGrow={1}/>
                    <Box ml={1}>
                        <OrganizedPlayStats deck={deck}/>
                    </Box>
                </Box>
                <DeckAllianceText deck={deck}/>
                <Box mb={0.5}>
                    <DeckViewDeckName deck={deck} fake={fake} fullVersion={fullVersion}/>
                </Box>
            </Box>
            <Box ml={2}>
                <DeckScoreView deck={deck}/>
            </Box>
        </>
    )
}

const DeckTopBannerCompact = (props: BannerProps) => {
    const {deck, fake, fullVersion} = props
    const token = deck.tokenInfo
    const archonPower = deck.archonPower

    let specialCard
    const compactSpecialCardWidth = 80
    if (token != null) {
        specialCard = (
            <CardAsLine
                card={{cardTitle: token.name, cardTitleUrl: token.nameUrl}}
                deck={deck}
                width={compactSpecialCardWidth}
                img={true}
                cardActualHouse={token.house}
                copies={deck.tokenCreationValues?.tokensPerGame}
            />
        )
    } else if (archonPower != null) {
        specialCard = (
            <CardAsLine
                card={archonPower}
                deck={deck}
                width={compactSpecialCardWidth}
                img={true}
            />
        )
    }

    return (
        <Box width={"100%"}>
            <DeckAllianceText deck={deck}/>
            <Box display={"flex"} flexGrow={"Grow"}>
                <DeckViewDeckName deck={deck} fake={fake} fullVersion={fullVersion}/>
                <Box flexGrow={1}/>
                <Box ml={1}>
                    <ExpansionIcon expansion={deck.expansion} white={true} size={32}/>
                </Box>
            </Box>
            <Box display={"flex"} alignItems={"flex-end"} mt={1}>
                <Box>
                    <DeckSaleIcons deck={deck}/>
                    <Box mt={1}>
                        {specialCard}
                    </Box>
                </Box>
                <Box flexGrow={1}/>
                <DeckScoreView deck={deck} style={{marginLeft: spacing(4)}}/>
            </Box>
        </Box>
    )
}
