import { Box, Card, Collapse, Tooltip } from "@material-ui/core"
import CardActions from "@material-ui/core/CardActions/CardActions"
import CardContent from "@material-ui/core/CardContent/CardContent"
import Divider from "@material-ui/core/Divider/Divider"
import List from "@material-ui/core/List/List"
import Typography from "@material-ui/core/Typography/Typography"
import { observer } from "mobx-react"
import * as React from "react"
import { AercForCombos } from "../aerc/AercForCombos"
import { AercViewForDeck, AercViewType } from "../aerc/views/AercViews"
import { deckListingStore } from "../auctions/DeckListingStore"
import { cardStore } from "../cards/CardStore"
import { EnhancementType } from "../cards/EnhancementType"
import { CardAsLine } from "../cards/views/CardAsLine"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { spacing } from "../config/MuiConfig"
import { Routes } from "../config/Routes"
import { ExpansionIcon } from "../expansions/ExpansionIcon"
import { displaySas, expansionInfoMap } from "../expansions/Expansions"
import { DeckListingStatus } from "../generated-src/DeckListingStatus"
import { DeckSaleInfo } from "../generated-src/DeckSaleInfo"
import { House } from "../generated-src/House"
import { SimpleCard } from "../generated-src/SimpleCard"
import { AuctionDeckIcon } from "../generic/icons/AuctionDeckIcon"
import { SellDeckIcon } from "../generic/icons/SellDeckIcon"
import { TradeDeckIcon } from "../generic/icons/TradeDeckIcon"
import { KeyCard } from "../generic/KeyCard"
import { HouseBanner } from "../houses/HouseBanner"
import { HouseLabel } from "../houses/HouseUtils"
import { KeyLink } from "../mui-restyled/KeyLink"
import { InlineDeckNote } from "../notes/DeckNote"
import { DeckTagsView } from "../tags/DeckTagsView"
import { screenStore } from "../ui/ScreenStore"
import { userStore } from "../user/UserStore"
import { OwnersList } from "../userdeck/OwnersList"
import { userDeckStore } from "../userdeck/UserDeckStore"
import { CompareDeckButton } from "./buttons/CompareDeckButton"
import { EvilTwinButton } from "./buttons/EvilTwinButton"
import { FavoriteDeck } from "./buttons/FavoriteDeck"
import { FunnyDeck } from "./buttons/FunnyDeck"
import { MoreDeckActions } from "./buttons/MoreDeckActions"
import { MyDecksButton } from "./buttons/MyDecksButton"
import { DeckScoreView } from "./DeckScoreView"
import { EnhancementsInDeck } from "./EnhancementsInDeck"
import { DeckSearchResult, DeckUtils } from "./models/DeckSearchResult"
import { OrganizedPlayStats } from "./OrganizedPlayStats"
import { DeckOwnershipButton } from "./ownership/DeckOwnershipButton"
import { ForSaleView } from "./sales/ForSaleView"

interface DeckViewSmallProps {
    deck: DeckSearchResult
    saleInfo?: DeckSaleInfo[]
    fullVersion?: boolean
    hideActions?: boolean
    style?: React.CSSProperties
    fake?: boolean
    margin?: number
}

@observer
export class DeckViewSmall extends React.Component<DeckViewSmallProps> {
    render() {

        if (!cardStore.cardsLoaded) {
            return null
        }

        const {deck, saleInfo, fullVersion, hideActions, style, fake, margin} = this.props
        const {id, keyforgeId, name, wishlistCount, funnyCount, owners, twinId} = deck

        const compact = screenStore.smallDeckView()

        const width = screenStore.deckWidth(!!saleInfo)
        const height = screenStore.deckHeight()
        const displaySalesSeparately = screenStore.displayDeckSaleInfoSeparately()

        let saleInfoView
        if (saleInfo) {
            saleInfoView =
                <ForSaleView deckId={id} saleInfo={saleInfo} deckName={name} keyforgeId={keyforgeId} height={displaySalesSeparately ? undefined : height}/>
        }

        const viewNotes = !hideActions && keyLocalStorage.genericStorage.viewNotes
        const viewTags = !hideActions && keyLocalStorage.genericStorage.viewTags
        const link = fake ? Routes.theoreticalDeckPage(keyforgeId) : Routes.deckPage(keyforgeId)

        let ownersFiltered = owners
        if (fullVersion) {
            ownersFiltered = owners?.filter(owner => owner != userStore.username)
        }

        return (
            <div>
                <KeyCard
                    style={{
                        width,
                        margin: margin != null ? margin : spacing(2),
                        ...style
                    }}
                    topContents={<DeckViewTopContents deck={deck} compact={compact}/>}
                    rightContents={!displaySalesSeparately && saleInfoView}
                    id={deck.keyforgeId}
                >
                    {compact && displaySas(deck.expansion) && (
                        <AercViewForDeck deck={deck} type={AercViewType.MOBILE_DECK}/>
                    )}
                    <div style={{display: "flex"}}>
                        <div style={{flexGrow: 1}}>
                            <CardContent style={{paddingBottom: 0, width: compact ? undefined : 544}}>
                                <KeyLink
                                    to={link}
                                    disabled={fullVersion}
                                    noStyle={true}
                                >
                                    <Box style={{maxWidth: width - spacing(6)}}>
                                        <Typography variant={"h5"} style={{fontSize: name.length > 48 ? 18 : undefined}}>{name}</Typography>
                                    </Box>
                                </KeyLink>
                                <DisplayAllCardsByHouse deck={deck} compact={compact}/>
                                <OwnersList owners={ownersFiltered}/>
                                <Collapse in={viewTags}>
                                    <DeckTagsView deckId={deck.id}/>
                                </Collapse>
                                <Collapse in={viewNotes}>
                                    <InlineDeckNote id={deck.id}/>
                                </Collapse>
                            </CardContent>
                            {!hideActions && !fake && (
                                <CardActions style={{flexWrap: "wrap", padding: spacing(1)}}>
                                    {compact ? null : (<CompareDeckButton deck={deck}/>)}
                                    {compact ? null : (<MyDecksButton deck={deck}/>)}
                                    <div style={{flexGrow: 1, margin: 0}}/>
                                    <div>
                                        <FavoriteDeck deckName={name} deckId={id} favoriteCount={wishlistCount ?? 0}/>
                                    </div>
                                    <div>
                                        <FunnyDeck deckName={name} deckId={id} funnyCount={funnyCount ?? 0}/>
                                    </div>
                                    <DeckOwnershipButton deckName={name} deckId={id} hasVerification={deck.hasOwnershipVerification}/>
                                    <EvilTwinButton twinId={twinId}/>
                                    <MoreDeckActions deck={deck} compact={compact}/>
                                </CardActions>
                            )}
                        </div>
                        {!compact && <AercViewForDeck deck={deck} type={AercViewType.DECK}/>}
                    </div>
                </KeyCard>
                {displaySalesSeparately && saleInfo && (
                    <Card
                        style={{
                            width: width > 400 ? 400 : width,
                            margin: spacing(2),
                        }}
                    >
                        {saleInfoView}
                    </Card>
                )}
            </div>
        )
    }
}

const deckTopClass = "deck-top-contents"

const DeckViewTopContents = observer((props: { deck: DeckSearchResult, compact: boolean }) => {
    const {deck, compact} = props
    const {housesAndCards, id, forAuction, forSale, forTrade, expansion} = deck
    const houses = housesAndCards.map(house => house.house)

    let displayForAuction = false
    let displayForSale = false
    let displayForTrade = false

    if (userDeckStore.ownedByMe(id)) {
        const saleInfo = deckListingStore.listingInfoForDeck(id)
        if (saleInfo != null) {
            displayForAuction = saleInfo.status === DeckListingStatus.AUCTION
            if (!displayForAuction) {
                displayForSale = true
                displayForTrade = saleInfo.forTrade
            }
        }
    } else {
        displayForAuction = forAuction == true
        if (!displayForAuction) {
            displayForSale = forSale == true
            displayForTrade = forTrade == true
        }
    }
    const displaySaleIcons = (displayForAuction || displayForSale || displayForTrade)
    let saleIcons
    if (displaySaleIcons) {
        saleIcons = (
            <>
                {displayForAuction && (
                    <Tooltip title={"On auction"}>
                        <div style={{display: "flex", justifyContent: "center"}}><AuctionDeckIcon height={36}/></div>
                    </Tooltip>
                )}
                {displayForSale && (
                    <Tooltip title={"For sale"}>
                        <div style={{display: "flex", justifyContent: "center"}}><SellDeckIcon height={36}/></div>
                    </Tooltip>
                )}
                {displayForTrade && (
                    <Tooltip title={"For trade"}>
                        <div style={{display: "flex", justifyContent: "center"}}><TradeDeckIcon height={36}/></div>
                    </Tooltip>
                )}
            </>
        )
    }
    if (compact) {
        return (
            <Box
                display={"grid"}
                gridGap={spacing(1)}
                flexGrow={1}
                alignItems={"center"}
            >
                <Box
                    display={"flex"}
                    justifyContent={"space-between"}
                    alignItems={"center"}
                    className={deckTopClass}
                >
                    <Box
                        display={"grid"}
                        gridGap={spacing(2)}
                    >
                        {saleIcons && (
                            <Box
                                display={"grid"}
                                gridGap={spacing(2)}
                                gridAutoFlow={"column"}
                            >
                                {saleIcons}
                            </Box>
                        )}
                        <Tooltip title={expansionInfoMap.get(expansion)!.name}>
                            <div>
                                <ExpansionIcon expansion={expansion} size={40} white={true}/>
                            </div>
                        </Tooltip>
                    </Box>
                    <DeckScoreView deck={deck} style={{marginLeft: spacing(4)}}/>
                </Box>
                <OrganizedPlayStats deck={deck}/>
                <EnhancementsInDeck deck={deck}/>
            </Box>
        )
    } else {
        return (
            <Box
                display={"flex"}
                alignItems={"center"}
                className={deckTopClass}
            >
                <Box
                    display={"grid"}
                    gridGap={spacing(1)}
                    flexGrow={1}
                    alignItems={"center"}
                >
                    <HouseBanner houses={houses} expansion={deck.expansion} extras={saleIcons}/>
                    <OrganizedPlayStats deck={deck}/>
                    <Box display={"flex"} justifyContent={"center"}>
                        <EnhancementsInDeck deck={deck} style={{marginLeft: spacing(4)}}/>
                    </Box>
                </Box>
                <DeckScoreView deck={deck}/>
            </Box>
        )
    }
})

const DisplayAllCardsByHouse = observer((props: { deck: DeckSearchResult, compact: boolean }) => {
    const {deck, compact} = props

    const bonusIcons = DeckUtils.calculateBonusIcons(deck)

    let showEnhancements = false
    let enhancementCount = 0
    let enhancementType = EnhancementType.AEMBER

    if (bonusIcons != null) {

        const cards = deck.housesAndCards.flatMap(houseCards => houseCards.cards)

        const enhancedAmber = bonusIcons.get(EnhancementType.AEMBER)!
        const enhancedCapture = bonusIcons.get(EnhancementType.CAPTURE)!
        const enhancedDamage = bonusIcons.get(EnhancementType.DAMAGE)!
        const enhancedDraw = bonusIcons.get(EnhancementType.DRAW)!

        const typesCount = (enhancedAmber > 0 ? 1 : 0) + (enhancedCapture > 0 ? 1 : 0) + (enhancedDamage > 0 ? 1 : 0) +
            (enhancedDraw > 0 ? 1 : 0)

        if (typesCount === 1) {
            const enhancedCardsCount = cards.filter(card => card.enhanced).length

            const totalBonuses = enhancedAmber + enhancedCapture + enhancedDamage + enhancedDraw

            if (enhancedCardsCount === 1 || enhancedCardsCount === totalBonuses) {
                showEnhancements = true
                enhancementCount = totalBonuses / enhancedCardsCount

                if (enhancedCapture > 0) {
                    enhancementType = EnhancementType.CAPTURE
                } else if (enhancedDamage > 0) {
                    enhancementType = EnhancementType.DAMAGE
                } else if (enhancedDraw > 0) {
                    enhancementType = EnhancementType.DRAW
                }
            }
        }
    }

    if (compact) {
        return <DisplayAllCardsByHouseCompact
            deck={deck}
            showEnhancements={showEnhancements}
            enhancementCount={enhancementCount}
            enhancementType={enhancementType}
        />
    }

    return (
        <div style={{display: "flex", justifyContent: "space-between", width: "100%"}}>
            {deck.housesAndCards.map((cardsForHouse) => (
                <DisplayCardsInHouse
                    key={cardsForHouse.house}
                    {...cardsForHouse}
                    deck={props.deck}
                    showEnhancements={showEnhancements}
                    enhancementCount={enhancementCount}
                    enhancementType={enhancementType}
                />
            ))}
        </div>
    )
})

const DisplayAllCardsByHouseCompact = observer((props: { deck: DeckSearchResult, showEnhancements: boolean, enhancementCount: number, enhancementType: EnhancementType }) => {
    return (
        <div style={{display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center"}}>
            {props.deck.housesAndCards.map((cardsForHouse) => (
                <DisplayCardsInHouse
                    key={cardsForHouse.house} {...cardsForHouse}
                    compact={true}
                    deck={props.deck}
                    showEnhancements={props.showEnhancements}
                    enhancementCount={props.enhancementCount}
                    enhancementType={props.enhancementType}
                />
            ))}
        </div>
    )
})

const smallDeckViewCardLineWidth = 144

const DisplayCardsInHouse = observer((props: { house: House, cards: SimpleCard[], compact?: boolean, deck: DeckSearchResult, showEnhancements: boolean, enhancementCount: number, enhancementType: EnhancementType }) => {
    const {house, deck, cards, compact, showEnhancements, enhancementCount, enhancementType} = props
    const deckExpansion = deck.expansion

    const enhancementsForCard = (card: SimpleCard) => {
        if (card.enhanced && showEnhancements) {
            const enhancements = new Map<EnhancementType, number>()
            enhancements.set(enhancementType, enhancementCount)
            return enhancements
        }
        return undefined
    }

    return (
        <List>
            <AercForCombos combos={deck.synergyDetails?.filter(combo => combo.house === house)}>
                <HouseLabel house={house} title={true}/>
            </AercForCombos>
            <Divider style={{marginTop: 4}}/>
            {compact ?
                (
                    <div style={{display: "flex"}}>
                        <div style={{marginRight: spacing(1)}}>
                            {cards.slice(0, 6).map((card, idx) => (
                                <CardAsLine
                                    key={idx}
                                    card={card}
                                    cardActualHouse={house}
                                    width={smallDeckViewCardLineWidth}
                                    marginTop={4}
                                    deckExpansion={deckExpansion}
                                    deck={deck}
                                    enhancements={enhancementsForCard(card)}
                                />
                            ))}
                        </div>
                        <div>
                            {cards.slice(6).map((card, idx) => (
                                <CardAsLine
                                    key={idx}
                                    card={card}
                                    cardActualHouse={house}
                                    width={smallDeckViewCardLineWidth}
                                    marginTop={4}
                                    deckExpansion={deckExpansion}
                                    deck={deck}
                                    enhancements={enhancementsForCard(card)}
                                />
                            ))}
                        </div>
                    </div>
                )
                :
                cards.map((card, idx) => (
                    <CardAsLine
                        key={idx}
                        card={card}
                        cardActualHouse={house}
                        width={160}
                        marginTop={4}
                        deckExpansion={deckExpansion}
                        deck={deck}
                        enhancements={enhancementsForCard(card)}
                    />
                ))
            }
        </List>
    )
})
