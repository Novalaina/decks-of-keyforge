import { Box, Button, Card, Collapse } from "@material-ui/core"
import CardActions from "@material-ui/core/CardActions/CardActions"
import CardContent from "@material-ui/core/CardContent/CardContent"
import Divider from "@material-ui/core/Divider/Divider"
import List from "@material-ui/core/List/List"
import { observer } from "mobx-react"
import * as React from "react"
import { AercViewForDeck, AercViewType } from "../aerc/views/AercViews"
import { cardStore } from "../cards/CardStore"
import { CardAsLine } from "../cards/views/CardAsLine"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { spacing } from "../config/MuiConfig"
import { displaySas } from "../expansions/Expansions"
import { DeckSaleInfo } from "../generated-src/DeckSaleInfo"
import { House } from "../generated-src/House"
import { SimpleCard } from "../generated-src/SimpleCard"
import { KeyCard } from "../generic/KeyCard"
import { HouseLabel } from "../houses/HouseUtils"
import { InlineDeckNote } from "../notes/DeckNote"
import { DeckTagsView } from "../tags/DeckTagsView"
import { screenStore } from "../ui/ScreenStore"
import { userStore } from "../user/UserStore"
import { OwnersList } from "../userdeck/OwnersList"
import { CompareDeckButton } from "./buttons/CompareDeckButton"
import { EvilTwinButton } from "./buttons/EvilTwinButton"
import { FavoriteDeck } from "./buttons/FavoriteDeck"
import { FunnyDeck } from "./buttons/FunnyDeck"
import { MoreDeckActions } from "./buttons/MoreDeckActions"
import { MyDecksButton } from "./buttons/MyDecksButton"
import { DeckSearchResult } from "./models/DeckSearchResult"
import { DeckOwnershipButton } from "./ownership/DeckOwnershipButton"
import { ForSaleView } from "./sales/ForSaleView"
import { DeleteTheoreticalDeckButton } from "../importdeck/theoretical/DeleteTheoreticalDeckButton"
import { Expansion } from "../generated-src/Expansion"
import { DeckType } from "../generated-src/DeckType"
import { MiniDeckLink } from "./buttons/MiniDeckLink"
import { DeckTopBanner } from "./deckview/DeckTopBanner"

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
        const alliance = deck.deckType === DeckType.ALLIANCE

        const compact = screenStore.smallDeckView()

        const width = screenStore.deckWidth(!!saleInfo)
        const height = screenStore.deckHeight()
        const displaySalesSeparately = screenStore.displayDeckSaleInfoSeparately()

        let saleInfoView
        if (saleInfo) {
            saleInfoView =
                <ForSaleView deckId={id} saleInfo={saleInfo} deckName={name} keyforgeId={keyforgeId}
                             height={displaySalesSeparately ? undefined : height}/>
        }

        const viewNotes = !hideActions && keyLocalStorage.genericStorage.viewNotes && !alliance
        const viewTags = !hideActions && keyLocalStorage.genericStorage.viewTags && !alliance

        let ownersFiltered = owners
        if (fullVersion) {
            ownersFiltered = owners?.filter(owner => owner != userStore.username)
        }

        return (
            <Box>
                <KeyCard
                    style={{
                        width,
                        margin: margin != null ? margin : spacing(2),
                        ...style
                    }}
                    topContents={<DeckTopBanner deck={deck} compact={compact} fake={!!fake}
                                                fullVersion={!!fullVersion}/>}
                    rightContents={!displaySalesSeparately && saleInfoView}
                    id={deck.keyforgeId}
                >
                    {compact && displaySas(deck.expansion) && (
                        <AercViewForDeck deck={deck} type={AercViewType.MOBILE_DECK}/>
                    )}
                    <div style={{display: "flex"}}>
                        <Box display={"flex"} flexDirection={"column"} flexGrow={1}>
                            <CardContent style={{paddingBottom: 0, width: compact ? undefined : 544}}>
                                <DisplayProphecies deck={deck} compact={compact}/>
                                <DisplayAllCardsByHouse deck={deck} compact={compact} fake={!!fake}/>
                                <Box display={"flex"} flexDirection={"column"} mt={1}>
                                    <OwnersList owners={ownersFiltered}/>
                                    <Box mt={viewTags ? 1 : 0}>
                                        <Collapse in={viewTags}>
                                            <DeckTagsView deckId={deck.id}/>
                                        </Collapse>
                                    </Box>
                                </Box>
                                <Collapse in={viewNotes}>
                                    <InlineDeckNote id={deck.id}/>
                                </Collapse>
                            </CardContent>
                            <Box flexGrow={1}/>
                            {!hideActions && !fake && (
                                <CardActions style={{flexWrap: "wrap", padding: spacing(1)}}>
                                    {compact ? null : (<CompareDeckButton deck={deck}/>)}
                                    {compact ? null : (<MyDecksButton deck={deck}/>)}
                                    <div style={{flexGrow: 1, margin: 0}}/>
                                    {!alliance && (
                                        <>
                                            <div>
                                                <FavoriteDeck deckName={name} deckId={id}
                                                              favoriteCount={wishlistCount ?? 0}/>
                                            </div>
                                            <div>
                                                <FunnyDeck deckName={name} deckId={id} funnyCount={funnyCount ?? 0}/>
                                            </div>
                                            <DeckOwnershipButton
                                                deck={deck}
                                                hasVerification={deck.hasOwnershipVerification}
                                            />
                                            <EvilTwinButton twinId={twinId}/>
                                        </>
                                    )}
                                    <MoreDeckActions deck={deck} compact={compact}/>
                                </CardActions>
                            )}
                            {fake && (
                                <CardActions style={{flexWrap: "wrap", padding: spacing(1)}}>
                                    {!compact && !fullVersion && (
                                        <DeleteTheoreticalDeckButton deckId={deck.keyforgeId} deckName={deck.name}/>)}
                                </CardActions>
                            )}
                        </Box>
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
            </Box>
        )
    }
}

const DisplayProphecies = observer((props: { deck: DeckSearchResult, compact: boolean }) => {
        const {deck, compact} = props
        const prophecies = deck.prophecies
        if (prophecies == null) {
            return null
        }
        if (!compact) {
            const prophecyWidth = 160
            return (
                <Box mb={1} display={"grid"} gridTemplateColumns={"1fr 1fr 1fr"} gridGap={spacing(1)}>
                    <HouseLabel
                        house={House.Prophecy}
                        title={true}
                        synergyDetails={deck.synergyDetails}
                        iconSize={44}
                    />

                    <Box>
                        {prophecies.slice(0, 2).map(prophecy => (
                            <CardAsLine
                                key={prophecy.cardTitle}
                                card={prophecy}
                                cardActualHouse={House.Prophecy}
                                deck={deck}
                                width={prophecyWidth}
                            />
                        ))}
                    </Box>
                    <Box>
                        {prophecies.slice(2, 4).map(prophecy => (
                            <CardAsLine
                                key={prophecy.cardTitle}
                                card={prophecy}
                                cardActualHouse={House.Prophecy}
                                deck={deck}
                                width={prophecyWidth}
                            />
                        ))}
                    </Box>
                </Box>

            )
        }
        return (
            <Box mb={1}>
                <Box display={"flex"} mb={0.5}>
                    <HouseLabel
                        house={House.Prophecy}
                        title={true}
                        synergyDetails={deck.synergyDetails}
                    />
                </Box>
                <Divider/>
                <Box
                    display={"grid"}
                    gridColumnGap={spacing(1)}
                    gridTemplateColumns={"1fr 1fr"}
                    mt={0.5}
                >
                    {prophecies.map(prophecy => (
                        <CardAsLine
                            key={prophecy.cardTitle}
                            card={prophecy}
                            cardActualHouse={House.Prophecy}
                            deck={deck}
                            width={smallDeckViewCardLineWidth}
                        />
                    ))}
                </Box>
            </Box>
        )
    }
)

const DisplayAllCardsByHouse = observer((props: { deck: DeckSearchResult, compact: boolean, fake: boolean }) => {
    const {deck, compact, fake} = props

    if (compact) {
        return <DisplayAllCardsByHouseCompact
            deck={deck}
            fake={fake}
        />
    }

    return (
        <Box display={"grid"} gridTemplateColumns={"1fr 1fr 1fr"} gridGap={spacing(1)}>
            {deck.housesAndCards.map((cardsForHouse) => (
                <DisplayCardsInHouse
                    key={cardsForHouse.house}
                    {...cardsForHouse}
                    deck={props.deck}
                    fake={fake}
                />
            ))}
        </Box>
    )
})

const DisplayAllCardsByHouseCompact = observer((props: { deck: DeckSearchResult, fake: boolean }) => {
    return (
        <div style={{display: "flex", flexDirection: "column"}}>
            {props.deck.housesAndCards.map((cardsForHouse) => (
                <DisplayCardsInHouse
                    key={cardsForHouse.house} {...cardsForHouse}
                    compact={true}
                    deck={props.deck}
                    fake={props.fake}
                />
            ))}
        </div>
    )
})

const smallDeckViewCardLineWidth = 144

const DisplayCardsInHouse = observer((props: {
    house: House,
    cards: SimpleCard[],
    compact?: boolean,
    deck: DeckSearchResult,
    fake: boolean
}) => {
    const {
        house,
        deck,
        cards,
        compact,
        fake,
    } = props
    const alliance = deck.deckType === DeckType.ALLIANCE
    const deckExpansion = deck.expansion

    let allianceHouse
    if (deck.allianceHouses != null) {
        allianceHouse = deck.allianceHouses.find(houseInfo => houseInfo.house === house)
    }

    return (
        <List>
            <Box display={"flex"} alignItems={compact ? "center" : undefined}
                 flexDirection={compact ? undefined : "column"}>
                <Box flexGrow={1} mb={compact ? undefined : 1}>
                    <HouseLabel
                        house={house}
                        title={true}
                        synergyDetails={deck.synergyDetails}
                        iconSize={compact ? undefined : 44}
                    />
                </Box>
                {allianceHouse && (
                    <MiniDeckLink deck={allianceHouse} maxHeight={24}/>
                )}
            </Box>
            <Divider style={{marginTop: 4}}/>
            {compact ?
                (
                    <Box display={"grid"} gridTemplateColumns={"1fr 1fr"} gridColumnGap={spacing(1)}>
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
                                />
                            ))}
                        </div>
                    </Box>
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
                    />
                ))
            }
            {keyLocalStorage.genericStorage.buildAllianceDeck && !fake && !alliance && (
                <>
                    <Divider style={{marginTop: spacing(1), marginBottom: spacing(0.5)}}/>
                    <AddHouseToAlliance
                        deckId={deck.keyforgeId}
                        deckName={deck.name}
                        house={house}
                        expansion={deck.expansion}
                        tokenName={deck.tokenInfo?.name}
                    />
                </>
            )}
        </List>
    )
})

const AddHouseToAlliance = observer((props: {
    deckId: string,
    deckName: string,
    house: House,
    expansion: Expansion,
    tokenName?: string,
}) => {
    const {deckId, deckName, house, expansion, tokenName} = props

    let disabled = keyLocalStorage.allianceDeckSaveInfo.houses.length > 2
        || keyLocalStorage.allianceDeckSaveInfo.houses.find(allianceDeck => allianceDeck.house === house) != null
    if (!disabled) {
        if (keyLocalStorage.allianceDeckSaveInfo.houses.find(allianceDeck => allianceDeck.expansion === Expansion.MARTIAN_CIVIL_WAR) != null) {
            disabled = expansion === Expansion.MARTIAN_CIVIL_WAR || (
                keyLocalStorage.allianceDeckSaveInfo.houses[1] != null
                && (keyLocalStorage.allianceDeckSaveInfo.houses[1].house === house || keyLocalStorage.allianceDeckSaveInfo.houses[1].expansion !== expansion)
            )
        } else {
            disabled = keyLocalStorage.allianceDeckSaveInfo.houses.find(allianceDeck => allianceDeck.expansion !== expansion) != null
        }
    }

    return (
        <Button
            onClick={() => {
                keyLocalStorage.addAllianceHouse(deckId, deckName, house, expansion, tokenName)
            }}
            disabled={disabled}
        >
            Add to Alliance
        </Button>
    )
})