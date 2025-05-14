import Typography from "@material-ui/core/Typography/Typography"
import * as React from "react"
import { House } from "../generated-src/House"
import brobnarImg from "./imgs/brobnar.png"
import disImg from "./imgs/dis.png"
import keyrakenImg from "./imgs/keyraken.png"
import geistoidImg from "./imgs/geistoid.png"
import ekwidonImg from "./imgs/ekwidon.png"
import logosImg from "./imgs/logos.png"
import marsImg from "./imgs/mars.png"
import eldersImg from "./imgs/elders.png"
import rebelsImg from "./imgs/rebels.png"
import sanctumImg from "./imgs/sanctum.png"
import saurianRepublicImg from "./imgs/saurian.png"
import shadowsImg from "./imgs/shadows.png"
import starAllianceImg from "./imgs/star-alliance.png"
import unfathomableImg from "./imgs/unfathomable.png"
import untamedImg from "./imgs/untamed.png"
import redemptionImg from "./imgs/redemption.png"
import prophecyImg from "./imgs/prophecy.svg"
import skybornImg from "./imgs/skyborn.png"
import { SynergyCombo } from "../generated-src/SynergyCombo"
import { AercForCombos } from "../aerc/AercForCombos"
import { Box } from "@material-ui/core"

export interface HouseValue {
    house: House
    img: string
    displayName?: string
    cardsOnly?: boolean
}

export const houseValuesArray: HouseValue[] = [
    {
        house: House.Brobnar,
        img: brobnarImg,
    },
    {
        house: House.Dis,
        img: disImg
    },
    {
        house: House.Ekwidon,
        img: ekwidonImg
    },
    {
        house: House.Geistoid,
        img: geistoidImg
    },
    {
        house: House.Logos,
        img: logosImg
    },
    {
        house: House.Mars,
        img: marsImg
    },
    {
        house: House.Sanctum,
        img: sanctumImg
    },
    {
        house: House.Saurian,
        img: saurianRepublicImg,
        displayName: "Saurian"
    },
    {
        house: House.Shadows,
        img: shadowsImg
    },
    {
        house: House.StarAlliance,
        img: starAllianceImg,
        displayName: "Star",
    },
    {
        house: House.Unfathomable,
        img: unfathomableImg,
        displayName: "Unfathom",
    },
    {
        house: House.Untamed,
        img: untamedImg
    },
    {
        house: House.Keyraken,
        img: keyrakenImg,
    },
    {
        house: House.Elders,
        img: eldersImg
    },
    {
        house: House.IronyxRebels,
        img: rebelsImg,
        displayName: "Rebels"
    },
    {
        house: House.Redemption,
        img: redemptionImg,
        displayName: "Redemption"
    },
    {
        house: House.Skyborn,
        img: skybornImg,
        displayName: "Skyborn"
    },
    {
        house: House.Prophecy,
        img: prophecyImg,
        cardsOnly: true,
    },
]
    .map(value => {
        if (value.displayName == null) {
            // @ts-ignore
            value.displayName = value.house
        }
        return value
    })

export const deckHouses = houseValuesArray.filter(houseValue => !houseValue.cardsOnly)

export const mmHouses = [...houseValuesArray]
mmHouses.splice(0, 1)
mmHouses.splice(2, 1)

export const houseValues: Map<House, HouseValue> = new Map(houseValuesArray.map(houseValue => (
    [houseValue.house, houseValue] as [House, HouseValue]
)))

export const HouseLabel = (props: {
    house: House,
    title?: boolean,
    width?: number,
    iconSize?: number,
    synergyDetails?: SynergyCombo[],
}) => {
    const {house, title, width, iconSize, synergyDetails} = props
    const value = houseValues.get(props.house)!
    const houseSize = iconSize ?? 32

    return (
        <AercForCombos combos={synergyDetails?.filter(combo => combo.house === house)}>
            <Box display={"flex"} alignItems={"center"} height={32}>
                <img alt={house} src={value.img} style={{width: houseSize, height: houseSize, marginRight: 8}}/>
                <Typography
                    noWrap={false}
                    variant={title ? "h5" : "body2"}
                    style={{width, fontSize: title ? "1.25rem" : undefined}}
                >
                    {value.displayName == null ? house : value.displayName}
                </Typography>
            </Box>
        </AercForCombos>
    )
}
