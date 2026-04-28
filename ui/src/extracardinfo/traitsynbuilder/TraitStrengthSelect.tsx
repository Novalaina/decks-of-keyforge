import { TraitBuilderStore } from "./TraitBuilderStore"
import { Box, Button, DialogContent, DialogTitle } from "@material-ui/core"
import { Utils } from "../../config/Utils"
import { TraitBubble } from "../../synergy/TraitBubble"
import { spacing } from "../../config/MuiConfig"
import Typography from "@material-ui/core/Typography/Typography"
import { SynTraitRatingValues } from "../../synergy/SynTraitValue"
import React from "react"
import { HelperText } from "../../generic/CustomTypographies"
import { SynergyTrait } from "../../generated-src/SynergyTrait"
import { synBuilderDialogStyle } from "./SelectTraitOrSyn"

export const TraitStrengthSelect = (props: { store: TraitBuilderStore }) => {
    const store = props.store
    const postiveName = store.traitOrSynergy === "synergy" ? "Synergy" : "Trait"
    return (
        <>
            <DialogTitle
                id="form-dialog-title">Select {Utils.camelCaseToTitleCase(store.traitOrSynergy)} Strength</DialogTitle>
            <DialogContent style={synBuilderDialogStyle()}>
                <Box display={"flex"}>
                    <TraitBubble
                        traitValue={store.synTraitValue()}
                        trait={store.traitOrSynergy === "trait"}
                    />
                </Box>

                <Box display={"grid"} gridTemplateColumns={"1fr 1fr 1fr 1fr 1fr"} gridGap={spacing(1)} my={2}>
                    {store.traitOrSynergy === "synergy" && (
                        <>
                            <TraitStrengthButton name={"Extra Weak"} strength={-1} store={store}
                                                 color={"secondary"}/>
                            <TraitStrengthButton name={"Weak Antisyn"} strength={-2} store={store}
                                                 color={"secondary"}/>
                            <TraitStrengthButton name={"Normal Antisyn"} strength={-3} store={store}
                                                 color={"secondary"}/>
                            <TraitStrengthButton name={"Strong Antisyn"} strength={-4} store={store}
                                                 color={"secondary"}/>
                            <TraitStrengthButton name={"Extra Strong"} strength={-6} store={store}
                                                 color={"secondary"}/>
                        </>
                    )}

                    <>
                        <TraitStrengthButton name={"Extra Weak"} strength={1} store={store}/>
                        <TraitStrengthButton name={"Weak " + postiveName} strength={2} store={store}/>
                        <TraitStrengthButton name={"Normal " + postiveName} strength={3} store={store}/>
                        <TraitStrengthButton name={"Strong " + postiveName} strength={4} store={store}/>
                        <TraitStrengthButton name={"Extra Strong"} strength={6} store={store}/>
                    </>
                </Box>

                <TraitStrengthDescription trait={store.trait}/>

            </DialogContent>
        </>
    )
}

const TraitStrengthDescription = (props: { trait?: SynergyTrait }) => {
    const trait = props.trait

    if (trait != null && [SynergyTrait.dangerousRandomPlay, SynergyTrait.replaysSelf].includes(trait)) {
        return (
            <>
                <HelperText color={"textPrimary"} style={{marginBottom: spacing(1)}}>
                    Dangerous Random Play will increase the value of bonus pips on a card.<br/>
                    Replays Self will increase the value of bonus pips on a card.
                </HelperText>
                <Box
                    display={"grid"}
                    gridTemplateColumns={"1fr 4fr"}
                    gridColumnGap={spacing(2)}
                    gridRowGap={spacing(0.5)}
                >
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Dangerous Play</Typography>
                    <Typography>Strong: 0%, Normal: 25%, Weak: 50%, Extra Weak: 75%</Typography>
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Replays Self</Typography>
                    <Typography>Strong: 2 replays, Normal: 1, Weak: 0.5, Extra Weak: 0.25</Typography>
                </Box>
            </>
        )
    }

    if (trait != null && [
        SynergyTrait.creatureCount, SynergyTrait.tokenCount, SynergyTrait.bonusAmber, SynergyTrait.bonusCapture,
        SynergyTrait.bonusDraw, SynergyTrait.bonusDamage, SynergyTrait.bonusDiscard, SynergyTrait.bonusPower, SynergyTrait.totalCreaturePower, SynergyTrait.totalArmor,
        SynergyTrait.expectedAember, SynergyTrait.capturedAmber, SynergyTrait.targettedCapturedAmber, SynergyTrait.haunted
    ].includes(trait)) {
        return (
            <>
                <HelperText color={"textPrimary"} style={{marginBottom: spacing(1)}}>
                    These traits go from 0 to 100% synergy with a normal strength synergy.<br/>
                    They can go below 0% or above 100%.<br/>
                    Synergy strength multiplies the value: Strong x2, Normal x1, Weak x0.5, Extra Weak x0.25.<br/>
                    Out of house range is double in-house range.
                </HelperText>
                <Box
                    display={"grid"}
                    gridTemplateColumns={"1fr 2fr"}
                    gridColumnGap={spacing(2)}
                    gridRowGap={spacing(0.5)}
                >
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Creature Count</Typography>
                    <Typography>Deck: 15-25, House: 3-10</Typography>
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Token Count</Typography>
                    <Typography>Deck or House: 0-25</Typography>
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Enhanced Pips</Typography>
                    <Typography>Deck: 0-30, House: 0-10</Typography>
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Total Creature Power</Typography>
                    <Typography>Deck: 45-90, House: 15-30</Typography>
                    <Typography variant={"body2"} style={{fontWeight: "bold"}}>Total Armor</Typography>
                    <Typography>Deck: 0-10, House: 0-5</Typography>
                </Box>
            </>
        )
    }

    return (
        <>
            <HelperText color={"textPrimary"} style={{marginBottom: spacing(1)}}>
                Remember the necessary matches to max out a Synergy + Trait pair.
            </HelperText>
            <Box display={"grid"} gridTemplateColumns={"1fr 1fr"} gridColumnGap={spacing(2)}
                 gridRowGap={spacing(0.5)}>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>Strong + Extra Strong</Typography>
                <Typography>1 match</Typography>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>Strong + Strong / Weak + Extra Strong</Typography>
                <Typography>2 matches</Typography>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>Normal + Strong / Extra Weak + Extra Strong</Typography>
                <Typography>3 matches</Typography>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>
                    Normal + Normal / Weak + Strong
                </Typography>
                <Typography>4 matches</Typography>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>
                    Weak + Normal / Extra weak + Strong
                </Typography>
                <Typography>6.7 matches</Typography>
                <Typography variant={"body2"} style={{fontWeight: "bold"}}>
                    Extra weak + Normal / Weak + Weak
                </Typography>
                <Typography>10 matches</Typography>
            </Box>
        </>
    )
}

export const TraitStrengthButton = (props: {
    name: string,
    strength: SynTraitRatingValues,
    store: TraitBuilderStore,
    color?: "secondary"
}) => {
    return (
        <Button
            variant={"contained"}
            color={props.color ?? "primary"}
            size={"small"}
            onClick={() => {
                props.store.rating = props.strength
                props.store.navigateToNextPage()
            }}
        >
            {props.name}
        </Button>
    )
}
