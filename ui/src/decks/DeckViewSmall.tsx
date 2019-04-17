import { Tooltip } from "@material-ui/core"
import CardActions from "@material-ui/core/CardActions/CardActions"
import CardContent from "@material-ui/core/CardContent/CardContent"
import Divider from "@material-ui/core/Divider/Divider"
import List from "@material-ui/core/List/List"
import Typography from "@material-ui/core/Typography/Typography"
import { observer } from "mobx-react"
import * as React from "react"
import { CardsForDeck } from "../cards/CardsForDeck"
import { CardAsLine } from "../cards/CardSimpleView"
import { KCard } from "../cards/KCard"
import { spacing } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { AuctionDeckIcon } from "../generic/icons/AuctionDeckIcon"
import { ChainsView } from "../generic/icons/ChainsView"
import { SellDeckIcon } from "../generic/icons/SellDeckIcon"
import { TradeDeckIcon } from "../generic/icons/TradeDeckIcon"
import { UnregisteredDeckIcon } from "../generic/icons/UnregisteredDeckIcon"
import { KeyCard } from "../generic/KeyCard"
import { House, houseValues } from "../houses/House"
import { HouseBanner } from "../houses/HouseBanner"
import { KeyButton } from "../mui-restyled/KeyButton"
import { KeyLink } from "../mui-restyled/KeyLink"
import { AercScoreView } from "../stats/AercScoreView"
import { screenStore } from "../ui/ScreenStore"
import { userDeckStore } from "../userdeck/UserDeckStore"
import { FunnyDeck } from "./buttons/FunnyDeck"
import { MyDecksButton } from "./buttons/MyDecksButton"
import { WishlistDeck } from "./buttons/WishlistDeck"
import { Deck, DeckUtils } from "./Deck"
import { DeckScoreView } from "./DeckScoreView"

interface DeckViewSmallProps {
    deck: Deck
    fullVersion?: boolean
    hideActions?: boolean
    style?: React.CSSProperties
}

@observer
export class DeckViewSmall extends React.Component<DeckViewSmallProps> {
    render() {
        const {deck, fullVersion, hideActions, style} = this.props
        const {
            id, keyforgeId, name,
            wishlistCount, funnyCount,
            forSale, forTrade, forAuction, chains,
            registered
        } = deck
        const userDeck = userDeckStore.userDeckByDeckId(id)
        let userDeckForSale = false
        let userDeckForTrade = false
        let userDeckForAuction = false
        if (userDeck) {
            userDeckForSale = userDeck.forSale
            userDeckForTrade = userDeck.forTrade
            userDeckForAuction = userDeck.forAuction
        }
        const compact = screenStore.screenSizeXs()
        return (
            <KeyCard
                style={{
                    margin: spacing(2),
                    width: compact ? 328 : 544,
                    ...style
                }}
                topContents={<DeckViewTopContents deck={deck} compact={compact}/>}
                topContentsStyle={{
                    padding: 0,
                    paddingTop: spacing(1),
                    paddingBottom: spacing(1)
                }}
            >
                <CardContent style={{paddingBottom: 0}}>
                    <div style={{display: "flex", alignItems: "center"}}>
                        {registered ? null : (
                            <Tooltip title={"Unregistered Deck"}>
                                <div>
                                    <UnregisteredDeckIcon style={{marginRight: spacing(1), marginTop: 3}}/>
                                </div>
                            </Tooltip>
                        )}
                        <div style={{flexGrow: 1}}>
                            <KeyLink
                                to={Routes.deckPage(keyforgeId)}
                                disabled={fullVersion}
                                noStyle={true}
                            >
                                <Typography variant={"h5"}>{name}</Typography>
                            </KeyLink>
                        </div>
                        <ChainsView chains={chains} style={{marginLeft: spacing(1)}}/>
                        {forSale || userDeckForSale ? (
                            <Tooltip title={"For sale"}>
                                <div style={{marginLeft: spacing(1)}}><SellDeckIcon/></div>
                            </Tooltip>
                        ) : null}
                        {forTrade || userDeckForTrade ? (
                            <Tooltip title={"For trade"}>
                                <div style={{marginLeft: spacing(1)}}><TradeDeckIcon/></div>
                            </Tooltip>
                        ) : null}
                        {forAuction || userDeckForAuction ? (
                            <Tooltip title={"On auction"}>
                                <div style={{marginLeft: spacing(1)}}><AuctionDeckIcon/></div>
                            </Tooltip>
                        ) : null}
                    </div>
                    <DisplayAllCardsByHouse deck={this.props.deck}/>
                </CardContent>
                {hideActions ? null : (
                    <CardActions style={{flexWrap: "wrap", padding: spacing(1)}}>
                        {fullVersion && deck.registered ? (
                            <KeyButton
                                href={"https://www.keyforgegame.com/deck-details/" + keyforgeId}
                                color={"primary"}
                            >
                                Master Vault
                            </KeyButton>
                        ) : null}
                        {!fullVersion ? (
                            <KeyLink
                                to={Routes.deckPage(keyforgeId)}
                                noStyle={true}
                            >
                                <KeyButton color={"primary"}>View Deck</KeyButton>
                            </KeyLink>
                        ) : null}
                        <CardsForDeck style={{marginRight: spacing(1)}} cards={deck.searchResultCards} deckName={deck.name}/>
                        <MyDecksButton deck={deck}/>
                        <div style={{flexGrow: 1}}/>
                        <div style={{marginRight: spacing(1)}}>
                            <WishlistDeck deckName={name} deckId={id} wishlistCount={wishlistCount}/>
                        </div>
                        <div style={{marginRight: spacing(1)}}>
                            <FunnyDeck deckName={name} deckId={id} funnyCount={funnyCount}/>
                        </div>
                    </CardActions>
                )}
            </KeyCard>
        )
    }
}

const DeckViewTopContents = (props: { deck: Deck, compact: boolean }) => {
    const {deck, compact} = props
    const {houses} = deck
    if (compact) {
        return (
            <div style={{
                display: "flex",
                alignItems: "center",
                flexDirection: "column",
            }}>
                <div style={{display: "flex", alignItems: "center"}}>
                    <HouseBanner houses={houses} size={48} vertical={true}/>
                    <DeckScoreView deck={deck} style={{marginLeft: spacing(6)}}/>
                </div>
                <AercScoreView hasAerc={deck} style={{marginTop: spacing(2)}} includeTotal={true}/>
            </div>
        )
    } else {
        return (
            <div style={{
                display: "flex",
                flexWrap: "wrap",
                alignItems: "center",
                justifyContent: "space-between",
                paddingRight: spacing(2),
            }}>
                <div style={{flexGrow: 1}}>
                    <HouseBanner houses={houses}/>
                    <div style={{display: "flex", justifyContent: "center"}}>
                        <AercScoreView hasAerc={deck} style={{marginTop: spacing(1)}} includeTotal={true}/>
                    </div>
                </div>
                <DeckScoreView deck={deck} style={compact ? {alignItems: "flex-end"} : undefined}/>
            </div>
        )
    }
}

const DisplayAllCardsByHouse = (props: { deck: Deck }) => {
    const cardsByHouse = DeckUtils.cardsInHouses(props.deck)

    if (screenStore.screenSizeXs()) {
        return <DisplayAllCardsByHouseCompact {...props}/>
    }

    return (
        <div style={{display: "flex", justifyContent: "space-between", width: "100%"}}>
            {cardsByHouse.map((cardsForHouse) => (<DisplayCardsInHouse key={cardsForHouse.house} {...cardsForHouse}/>))}
        </div>
    )
}

const DisplayAllCardsByHouseCompact = (props: { deck: Deck }) => {
    const cardsByHouse = DeckUtils.cardsInHouses(props.deck)

    return (
        <div style={{display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center"}}>
            {cardsByHouse.map((cardsForHouse) => (
                <DisplayCardsInHouse key={cardsForHouse.house} {...cardsForHouse}/>
            ))}
        </div>
    )
}

const DisplayCardsInHouse = (props: { house: House, cards: KCard[], disableTextSelection?: boolean }) => (
    <List>
        {houseValues.get(props.house)!.title}
        <Divider style={{marginTop: 4}}/>
        {props.cards.map((card, idx) => (<CardAsLine key={idx} card={card} width={160} marginTop={4}/>))}
    </List>
)
