import {
    Box,
    Card,
    CardActions,
    FormControl,
    Grid,
    IconButton,
    InputLabel,
    MenuItem,
    Select,
    Switch,
    TextField,
    Typography
} from "@material-ui/core"
import {ChevronLeft, ChevronRight} from "@material-ui/icons"
import {makeObservable, observable} from "mobx"
import {observer} from "mobx-react"
import React, {useEffect} from "react"
import {useParams} from "react-router-dom"
import {cardStore} from "../cards/CardStore"
import {spacing} from "../config/MuiConfig"
import {Routes} from "../config/Routes"
import {log, prettyJson, Utils} from "../config/Utils"
import {CardType} from "../generated-src/CardType"
import {SynTraitValue} from "../generated-src/SynTraitValue"
import {EventValue} from "../generic/EventValue"
import {KeyButton} from "../mui-restyled/KeyButton"
import {LinkButton} from "../mui-restyled/LinkButton"
import {Loader} from "../mui-restyled/Loader"
import {uiStore} from "../ui/UiStore"
import {extraCardInfoStore} from "./ExtraCardInfoStore"
import {CardTraitsViewAndEdit} from "./traitsynbuilder/CardTraitsViewAndEdit"
import {ExtraCardInfo} from "../generated-src/ExtraCardInfo"
import {AercBlameView} from "./AercBlameView"
import {AercBlame} from "../generated-src/AercBlame"
import {CardView} from "../cards/views/CardView"
import {FrontendCard} from "../generated-src/FrontendCard"
import {ExpansionLabel, expansionsWithCards} from "../expansions/Expansions"
import {keyLocalStorage} from "../config/KeyLocalStorage"
import {Expansion} from "../generated-src/Expansion"
import {screenStore} from "../ui/ScreenStore"
import FormControlLabel from "@material-ui/core/FormControlLabel"

export const UpdateExtraCardInfoPage = observer(() => {

    const {infoId} = useParams<{ infoId: string }>()

    const expansion = keyLocalStorage.genericStorage.cardScrollExpansion

    useEffect(() => {
        extraCardInfoStore.findExtraCardInfo(infoId)
        extraCardInfoStore.findAERCBlame(infoId)
    }, [infoId])

    useEffect(() => {
        extraCardInfoStore.findNextAndPreviousExtraCardInfos(infoId, expansion)
    }, [infoId, expansion])

    const extraCardInfo = extraCardInfoStore.extraCardInfo
    if (extraCardInfo == null || !cardStore.cardsLoaded) {
        return <Loader/>
    }
    const card = cardStore.fullCardFromCardName(extraCardInfo.cardName)!
    return <UpdateExtraCardInfo extraCardInfo={extraCardInfo} card={card}
                                aercBlame={extraCardInfoStore.aercBlame ?? []}/>
})

interface UpdateExtraCardInfoProps {
    extraCardInfo: ExtraCardInfo
    card: FrontendCard
    aercBlame: AercBlame[]
}

@observer
export class UpdateExtraCardInfo extends React.Component<UpdateExtraCardInfoProps> {

    @observable
    amberControl = "0"
    @observable
    expectedAmber = "0"
    @observable
    artifactControl = "0"
    @observable
    creatureControl = "0"
    @observable
    aercScore = "0"
    @observable
    efficiency = "0"
    @observable
    recursion = "0"
    @observable
    effectivePower = "0"
    @observable
    creatureProtection = "0"
    @observable
    disruption = "0"
    @observable
    other = "0"

    @observable
    amberControlMax = "0"
    @observable
    expectedAmberMax = "0"
    @observable
    artifactControlMax = "0"
    @observable
    creatureControlMax = "0"
    @observable
    aercScoreMax = "0"
    @observable
    efficiencyMax = "0"
    @observable
    recursionMax = "0"
    @observable
    effectivePowerMax = "0"
    @observable
    creatureProtectionMax = "0"
    @observable
    disruptionMax = "0"
    @observable
    otherMax = "0"

    @observable
    baseSynPercent = ""

    @observable
    enhancementAmber = "0"
    @observable
    enhancementCapture = "0"
    @observable
    enhancementDraw = "0"
    @observable
    enhancementDiscard = "0"
    @observable
    enhancementDamage = "0"

    @observable
    traits: SynTraitValue[] = []
    @observable
    synergies: SynTraitValue[] = []

    @observable
    extraCardTypes: CardType[] = []

    // eslint-disable-next-line
    // @ts-ignore
    infoId: string

    constructor(props: UpdateExtraCardInfoProps) {
        super(props)
        makeObservable(this)
        this.reset(props.extraCardInfo)
    }

    componentDidUpdate(prevProps: Readonly<UpdateExtraCardInfoProps>) {
        if (prevProps.extraCardInfo !== this.props.extraCardInfo) {
            this.reset(this.props.extraCardInfo)
        }
    }

    reset = (resetTo: ExtraCardInfo) => {
        const extraCardInfo = resetTo == null ? this.props.extraCardInfo : resetTo
        this.infoId = extraCardInfo.id

        this.amberControl = extraCardInfo.amberControl.toString()
        this.expectedAmber = extraCardInfo.expectedAmber.toString()
        this.artifactControl = extraCardInfo.artifactControl.toString()
        this.creatureControl = extraCardInfo.creatureControl.toString()
        this.efficiency = extraCardInfo.efficiency.toString()
        this.recursion = extraCardInfo.recursion.toString()
        this.effectivePower = extraCardInfo.effectivePower.toString()
        this.creatureProtection = extraCardInfo.creatureProtection.toString()
        this.disruption = extraCardInfo.disruption.toString()
        this.other = extraCardInfo.other.toString()

        this.amberControlMax = extraCardInfo.amberControlMax == null ? "0" : extraCardInfo.amberControlMax.toString()
        this.expectedAmberMax = extraCardInfo.expectedAmberMax == null ? "0" : extraCardInfo.expectedAmberMax.toString()
        this.artifactControlMax = extraCardInfo.artifactControlMax == null ? "0" : extraCardInfo.artifactControlMax.toString()
        this.creatureControlMax = extraCardInfo.creatureControlMax == null ? "0" : extraCardInfo.creatureControlMax.toString()
        this.efficiencyMax = extraCardInfo.efficiencyMax == null ? "0" : extraCardInfo.efficiencyMax.toString()
        this.recursionMax = extraCardInfo.recursionMax == null ? "0" : extraCardInfo.recursionMax.toString()
        this.effectivePowerMax = extraCardInfo.effectivePowerMax == null ? "0" : extraCardInfo.effectivePowerMax.toString()
        this.creatureProtectionMax = extraCardInfo.creatureProtectionMax == null ? "0" : extraCardInfo.creatureProtectionMax.toString()
        this.disruptionMax = extraCardInfo.disruptionMax == null ? "0" : extraCardInfo.disruptionMax.toString()
        this.otherMax = extraCardInfo.otherMax == null ? "0" : extraCardInfo.otherMax.toString()

        this.baseSynPercent = extraCardInfo.baseSynPercent?.toString() ?? ""

        this.enhancementAmber = extraCardInfo.enhancementAmber.toString()
        this.enhancementCapture = extraCardInfo.enhancementCapture.toString()
        this.enhancementDraw = extraCardInfo.enhancementDraw.toString()
        this.enhancementDiscard = extraCardInfo.enhancementDiscard.toString()
        this.enhancementDamage = extraCardInfo.enhancementDamage.toString()

        this.extraCardTypes = extraCardInfo.extraCardTypes ?? []
        this.traits = extraCardInfo.traits
        this.synergies = extraCardInfo.synergies

        uiStore.setTopbarValues("Edit " + this.props.extraCardInfo.cardName, "Edit", "")
    }

    save = async () => {

        const extraCardInfo: ExtraCardInfo = this.createExtraInfo()

        log.info("In save base syn percent is " + this.baseSynPercent + " saving: " + prettyJson(extraCardInfo))

        await extraCardInfoStore.saveExtraCardInfo(extraCardInfo)
        const saved = await extraCardInfoStore.findExtraCardInfo(this.infoId)
        this.reset(saved)
    }

    private createExtraInfo = (): ExtraCardInfo => {
        return {
            ...this.props.extraCardInfo,

            amberControl: Number(this.amberControl),
            expectedAmber: Number(this.expectedAmber),
            artifactControl: Number(this.artifactControl),
            creatureControl: Number(this.creatureControl),
            efficiency: Number(this.efficiency),
            recursion: Number(this.recursion),
            effectivePower: Number(this.effectivePower),
            creatureProtection: Number(this.creatureProtection),
            disruption: Number(this.disruption),
            other: Number(this.other),

            // @ts-ignore
            amberControlMax: Utils.toNumberOrUndefined(this.amberControlMax),
            // @ts-ignore
            expectedAmberMax: Utils.toNumberOrUndefined(this.expectedAmberMax),
            // @ts-ignore
            artifactControlMax: Utils.toNumberOrUndefined(this.artifactControlMax),
            // @ts-ignore
            creatureControlMax: Utils.toNumberOrUndefined(this.creatureControlMax),
            // @ts-ignore
            efficiencyMax: Utils.toNumberOrUndefined(this.efficiencyMax),
            // @ts-ignore
            recursionMax: Utils.toNumberOrUndefined(this.recursionMax),
            // @ts-ignore
            effectivePowerMax: Utils.toNumberOrUndefined(this.effectivePowerMax),
            // @ts-ignore
            creatureProtectionMax: Utils.toNumberOrUndefined(this.creatureProtectionMax),
            // @ts-ignore
            disruptionMax: Utils.toNumberOrUndefined(this.disruptionMax),
            // @ts-ignore
            otherMax: Utils.toNumberOrUndefined(this.otherMax),

            // @ts-ignore
            baseSynPercent: this.baseSynPercent === "" ? undefined : Number(this.baseSynPercent),

            enhancementAmber: Number(this.enhancementAmber),
            enhancementCapture: Number(this.enhancementCapture),
            enhancementDraw: Number(this.enhancementDraw),
            enhancementDiscard: Number(this.enhancementDiscard),
            enhancementDamage: Number(this.enhancementDamage),

            extraCardTypes: this.extraCardTypes.length === 0 ? undefined : this.extraCardTypes,
            traits: this.traits,
            synergies: this.synergies
        }
    }

    render() {
        const {card, aercBlame} = this.props
        const nextId = extraCardInfoStore.nextAndPrevInfoIds?.nextInfo
        const prevId = extraCardInfoStore.nextAndPrevInfoIds?.previousInfo
        return (
            <Box
                display={"flex"}
                flexWrap={"wrap"}
                justifyContent={"center"}
            >
                <Box>
                    <CardView card={card}/>
                    <Box m={2} maxWidth={624}>
                        <AercBlameView blame={aercBlame}/>
                    </Box>
                </Box>
                <div>
                    <Card style={{maxWidth: 800, margin: spacing(2), padding: spacing(2)}}>
                        <Box display={"flex"} alignItems={"center"} mb={2}
                             flexDirection={screenStore.screenSizeSm() ? "column" : undefined}>
                            <Typography variant={"h4"}>
                                {card.cardTitle}'s AERC
                            </Typography>
                            <div style={{flexGrow: 1}}/>
                            <Box display={"flex"} alignItems={"center"}>
                                <Box minWidth={120}>
                                    <FormControl>
                                        <Select
                                            displayEmpty={true}
                                            value={keyLocalStorage.genericStorage.cardScrollExpansion ?? ""}
                                            label="Card Nav Expansion"
                                            onChange={(event) => {
                                                const value = event.target.value as (Expansion | "")
                                                keyLocalStorage.updateGenericStorage({
                                                    cardScrollExpansion: value === "" ? undefined : value
                                                })
                                            }}
                                        >
                                            <MenuItem value={""}>Card Scroll Expansion</MenuItem>
                                            {expansionsWithCards.map(expansion => (
                                                <MenuItem key={expansion} value={expansion}>
                                                    <ExpansionLabel expansion={expansion} iconSize={16}/>
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Box>
                                <IconButton
                                    disabled={prevId == null}
                                    href={Routes.editExtraCardInfo(prevId)}
                                >
                                    <ChevronLeft/>
                                </IconButton>
                                <IconButton
                                    disabled={nextId == null}
                                    href={Routes.editExtraCardInfo(nextId)}
                                >
                                    <ChevronRight/>
                                </IconButton>
                            </Box>
                        </Box>
                        <Grid
                            container={true}
                            spacing={2}
                            style={{marginTop: spacing(2)}}
                        >
                            <InfoInput
                                name={"E"}
                                value={this.expectedAmber}
                                update={(event: EventValue) => this.expectedAmber = event.target.value}
                            />
                            <InfoInput
                                name={"E max"}
                                value={this.expectedAmberMax}
                                update={(event: EventValue) => this.expectedAmberMax = event.target.value}
                            />
                            <InfoInput
                                name={"A"}
                                value={this.amberControl}
                                update={(event: EventValue) => this.amberControl = event.target.value}
                            />
                            <InfoInput
                                name={"A max"}
                                value={this.amberControlMax}
                                update={(event: EventValue) => this.amberControlMax = event.target.value}
                            />
                            <InfoInput
                                name={"R"}
                                value={this.artifactControl}
                                update={(event: EventValue) => this.artifactControl = event.target.value}
                            />
                            <InfoInput
                                name={"R max"}
                                value={this.artifactControlMax}
                                update={(event: EventValue) => this.artifactControlMax = event.target.value}
                            />
                            <InfoInput
                                name={"C"}
                                value={this.creatureControl}
                                update={(event: EventValue) => this.creatureControl = event.target.value}
                            />
                            <InfoInput
                                name={"C max"}
                                value={this.creatureControlMax}
                                update={(event: EventValue) => this.creatureControlMax = event.target.value}
                            />
                            <InfoInput
                                name={"efficiency"}
                                value={this.efficiency}
                                update={(event: EventValue) => this.efficiency = event.target.value}
                            />
                            <InfoInput
                                name={"efficiency max"}
                                value={this.efficiencyMax}
                                update={(event: EventValue) => this.efficiencyMax = event.target.value}
                            />
                            <InfoInput
                                name={"recursion"}
                                value={this.recursion}
                                update={(event: EventValue) => this.recursion = event.target.value}
                            />
                            <InfoInput
                                name={"recursion max"}
                                value={this.recursionMax}
                                update={(event: EventValue) => this.recursionMax = event.target.value}
                            />
                            <InfoInput
                                name={"disruption"}
                                value={this.disruption}
                                update={(event: EventValue) => this.disruption = event.target.value}
                            />
                            <InfoInput
                                name={"disruption max"}
                                value={this.disruptionMax}
                                update={(event: EventValue) => this.disruptionMax = event.target.value}
                            />
                            <InfoInput
                                name={"effective power"}
                                value={this.effectivePower}
                                update={(event: EventValue) => this.effectivePower = event.target.value}
                            />
                            <InfoInput
                                name={"P max"}
                                value={this.effectivePowerMax}
                                update={(event: EventValue) => this.effectivePowerMax = event.target.value}
                            />
                            <InfoInput
                                name={"creature prot"}
                                value={this.creatureProtection}
                                update={(event: EventValue) => this.creatureProtection = event.target.value}
                            />
                            <InfoInput
                                name={"CP max"}
                                value={this.creatureProtectionMax}
                                update={(event: EventValue) => this.creatureProtectionMax = event.target.value}
                            />
                            <InfoInput
                                name={"other"}
                                value={this.other}
                                update={(event: EventValue) => this.other = event.target.value}
                            />
                            <InfoInput
                                name={"other max"}
                                value={this.otherMax}
                                update={(event: EventValue) => this.otherMax = event.target.value}
                            />
                            <InfoInput
                                name={"syn start %"}
                                value={this.baseSynPercent}
                                update={(event: EventValue) => this.baseSynPercent = event.target.value}
                            />
                            {keyLocalStorage.addBonusIcons && (
                                <>
                                    <InfoInput
                                        name={"bonus amber"}
                                        value={this.enhancementAmber}
                                        update={(event: EventValue) => this.enhancementAmber = event.target.value}
                                    />
                                    <InfoInput
                                        name={"bonus capture"}
                                        value={this.enhancementCapture}
                                        update={(event: EventValue) => this.enhancementCapture = event.target.value}
                                    />
                                    <InfoInput
                                        name={"bonus draw"}
                                        value={this.enhancementDraw}
                                        update={(event: EventValue) => this.enhancementDraw = event.target.value}
                                    />
                                    <InfoInput
                                        name={"bonus discard"}
                                        value={this.enhancementDiscard}
                                        update={(event: EventValue) => this.enhancementDiscard = event.target.value}
                                    />
                                    <InfoInput
                                        name={"bonus damage"}
                                        value={this.enhancementDamage}
                                        update={(event: EventValue) => this.enhancementDamage = event.target.value}
                                    />
                                </>
                            )}
                            <Grid item={true} xs={6} sm={2}>
                                <FormControl fullWidth={true}>
                                    <InputLabel>Card Types</InputLabel>
                                    <Select
                                        multiple={true}
                                        value={this.extraCardTypes}
                                        onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                                            this.extraCardTypes = event.target.value as CardType[]
                                        }}
                                    >
                                        {(Utils.enumValues(CardType) as CardType[]).map(type => (
                                            <MenuItem key={type} value={type}>{type}</MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item={true} xs={12}>
                                <CardTraitsViewAndEdit traits={this.traits} synergies={this.synergies}/>
                            </Grid>
                        </Grid>
                        <CardActions>
                            <LinkButton
                                href={Routes.cards}
                                style={{marginRight: spacing(2)}}
                            >
                                Cards
                            </LinkButton>
                            <FormControlLabel
                                label={"Add Bonus Icons"}
                                control={<Switch checked={keyLocalStorage.addBonusIcons}
                                                 onChange={keyLocalStorage.toggleAddBonusIcons}/>}
                            />
                            <div style={{flexGrow: 1}}/>
                            <KeyButton
                                variant={"contained"}
                                color={"primary"}
                                loading={extraCardInfoStore.savingExtraCardInfo}
                                onClick={this.save}
                            >
                                Save
                            </KeyButton>
                        </CardActions>
                    </Card>
                </div>
            </Box>
        )
    }
}

const InfoInput = (props: { name: string, value: string, update: (event: EventValue) => void, small?: boolean }) => {
    const {name, value, update} = props
    return (
        <Grid item={true} xs={6} sm={2}>
            <TextField
                label={name}
                value={value}
                onChange={update}
                fullWidth={true}
                variant={"outlined"}
                type={"number"}
            />
        </Grid>
    )
}
