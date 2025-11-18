import { CardUtils } from "../KCard"
import { SynergyCombo } from "../../generated-src/SynergyCombo"
import { ExtraCardInfo } from "../../generated-src/ExtraCardInfo"
import { observer } from "mobx-react"
import { cardStore } from "../CardStore"
import { userStore } from "../../user/UserStore"
import { screenStore } from "../../ui/ScreenStore"
import { GraySidebar } from "../../generic/GraySidebar"
import { spacing } from "../../config/MuiConfig"
import { Box, Divider } from "@material-ui/core"
import { CardQualityIcon } from "../../generic/icons/CardQualityIcon"
import Typography from "@material-ui/core/Typography/Typography"
import { UnstyledLink } from "../../generic/UnstyledLink"
import { Routes } from "../../config/Routes"
import { LinkButton, LinkIconButton } from "../../mui-restyled/LinkButton"
import HistoryIcon from "@material-ui/icons/History"
import { HouseBanner } from "../../houses/HouseBanner"
import { CardWinsDisplay } from "../cardwins/CardWinsDisplay"
import { TimeUtils } from "../../config/TimeUtils"
import { AercForCard } from "../../aerc/views/AercForCard"
import * as React from "react"
import { CardSimpleView } from "./CardSimpleView"
import { CardSetsFromCard, CardSynergies, CardTraits } from "./CardSupplementalViews"
import { FrontendCard } from "../../generated-src/FrontendCard"
import { CardType } from "../../generated-src/CardType"

interface CardViewProps {
    card: FrontendCard
    cardUrl?: string
    simple?: boolean
    noLink?: boolean
    combo?: SynergyCombo
    extraCombos?: SynergyCombo[]
    displayHistory?: boolean
    copies?: number
    history?: ExtraCardInfo[]
}

export const CardView = observer((props: CardViewProps) => {
    const {card, cardUrl, simple, noLink, combo, displayHistory, copies} = props

    if (simple) {
        return <CardSimpleView card={card}/>
    }
    const {cardTitle, cardType, cardText, amber} = card

    const extraCardInfo = cardStore.findExtraInfoToUse(card)

    const cardAerc = cardStore.hasAercFromCardName(card.cardTitle)!

    let previousCard
    if (displayHistory) {
        previousCard = cardStore.findPrevExtraInfoForCard(card.cardTitle)
    }

    let futureCard
    if (userStore.displayFutureSas) {
        futureCard = cardStore.findNextExtraInfoForCard(card.cardTitle)
    }

    const sidebarProps = screenStore.screenSizeXs() ? {
        vertical: true,
        width: 300,
    } : {
        width: 624,
    }

    return (
        <GraySidebar {...sidebarProps} >
            <Box>
                <img alt={card.cardTitle} src={cardUrl ?? card.cardTitleUrl}/>
            </Box>
            <div style={{padding: spacing(2), width: "100%"}}>
                <Box display={"flex"} alignItems={"center"} maxWidth={sidebarProps.width}>
                    <CardQualityIcon quality={CardUtils.fakeRatingFromAerc(cardAerc)}/>
                    {noLink ? (
                        <Typography
                            color={"textPrimary"} variant={"h6"}
                            style={{marginLeft: spacing(1), flexGrow: 1}}
                        >
                            {cardTitle}
                        </Typography>
                    ) : (
                        <>
                            <UnstyledLink
                                to={Routes.cardPage(card.cardTitle)}
                                style={{marginLeft: spacing(1), marginRight: spacing(1), color: "rgba(0, 0, 0, 0.87)"}}
                            >
                                <Typography
                                    color={"textPrimary"}
                                    variant={"h6"}
                                >
                                    {cardTitle}
                                </Typography>
                            </UnstyledLink>
                            <LinkIconButton
                                size={"small"}
                                href={Routes.cardHistory(card.cardTitle)}
                                newWindow={true}
                            >
                                <HistoryIcon style={{width: 20, height: 20}}/>
                            </LinkIconButton>
                            <Box flexGrow={1}/>
                        </>
                    )}
                    <div style={{flexGrow: 1}}/>
                    <CardSetsFromCard card={card}/>
                </Box>
                {card.houses.length > 1 && (
                    <HouseBanner houses={card.houses} size={24}
                                 style={{marginBottom: spacing(2), marginTop: spacing(1)}}/>
                )}
                <div style={{display: "flex"}}>
                    <Typography color={"textPrimary"} variant={"subtitle2"}>{cardType}</Typography>
                    <div style={{flexGrow: 1}}/>
                    {amber > 0 ?
                        <Typography color={"textPrimary"} variant={"subtitle2"}>{amber} aember</Typography> : null}
                </div>
                <Typography color={"textPrimary"} variant={"body2"}>{cardText}</Typography>
                <CardWinsDisplay card={card}/>
                {card.cardType === CardType.ArchonPower && (
                    <>
                        <Divider style={{marginTop: spacing(1), marginBottom: spacing(1)}}/>
                        <Typography color={"textPrimary"} variant={"subtitle2"} style={{fontStyle: "italic"}}>
                        Archon Powers are not included in SAS Scores
                    </Typography>
                    </>
                )}
                {props.history == null ? (
                    <>
                        {futureCard && (
                            <AercAndSynergies card={futureCard} title={"Future AERC"}/>
                        )}
                        <AercAndSynergies card={card} combo={combo}
                                          title={(previousCard || futureCard) && "Current AERC"} copies={copies}/>
                        {previousCard && (
                            <AercAndSynergies card={previousCard} title={"Previous AERC"}/>
                        )}
                    </>
                ) : (
                    <>
                        {props.history.map(info => (
                            <AercAndSynergies
                                card={{...card, extraCardInfo: info}}
                                title={info.publishedDate == null ? "Future AERC" : `AERC history`}
                            />
                        ))}
                    </>
                )}
                {userStore.contentCreator && !noLink && (
                    <>
                        <Divider style={{marginTop: spacing(1), marginBottom: spacing(1)}}/>
                        <LinkButton
                            href={Routes.editExtraCardInfo(extraCardInfo.id)}
                            newWindow={true}
                        >
                            Edit
                        </LinkButton>
                    </>
                )}
            </div>
        </GraySidebar>
    )
})

const AercAndSynergies = (props: { card: FrontendCard, combo?: SynergyCombo, title?: string, copies?: number }) => {
    const {card, combo, title, copies} = props
    const extraCardInfo = card.extraCardInfo
    const {traits, synergies, publishedDate} = extraCardInfo
    return (
        <>
            <Divider style={{marginTop: spacing(1), marginBottom: spacing(1)}}/>
            <div style={{display: "flex", alignItems: "flex-end"}}>
                <Typography color={"textPrimary"} variant={"h5"} style={{marginRight: spacing(2)}}>{title}</Typography>
                {title && publishedDate && (
                    <Typography color={"textPrimary"}
                                variant={"subtitle2"}>{TimeUtils.formatDate(publishedDate)}</Typography>
                )}
            </div>
            <AercForCard card={card} realValue={combo} copies={copies}/>
            <Divider style={{marginTop: spacing(1), marginBottom: spacing(1)}}/>
            {traits.length !== 0 ? <Typography color={"textPrimary"} variant={"subtitle1"}>Traits</Typography> : null}
            <CardTraits traits={traits}/>
            {synergies.length !== 0 ?
                <Typography color={"textPrimary"} variant={"subtitle1"}>Synergies</Typography> : null}
            <CardSynergies synergies={synergies}/>
        </>
    )
}
