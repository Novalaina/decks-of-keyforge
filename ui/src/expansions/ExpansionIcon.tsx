import {observer} from "mobx-react"
import * as React from "react"
import anomalyDark from "../cards/imgs/anomaly-dark.svg"
import anomaly from "../cards/imgs/anomaly.svg"
import {themeStore} from "../config/MuiConfig"
import {Expansion} from "../generated-src/Expansion"
import {expansionInfoMap} from "./Expansions"
import aoaDark from "./imgs/aoa-dark.svg"
import aoa from "./imgs/aoa.svg"
import cotaDark from "./imgs/cota-dark.svg"
import cota from "./imgs/cota.svg"
import dtDark from "./imgs/dt-dark.svg"
import dt from "./imgs/dt.svg"
import mmDark from "./imgs/mm-dark.svg"
import mm from "./imgs/mm.svg"
import wcDark from "./imgs/wc-dark.svg"
import wc from "./imgs/wc.svg"
import woe from "./imgs/woe.svg"
import woeDark from "./imgs/woe-dark.svg"
import uc from "./imgs/uc.svg"
import ucDark from "./imgs/uc-dark.svg"
import vm from "./imgs/vm23.svg"
import vmDark from "./imgs/vm23-dark.svg"
import gr from "./imgs/gr.svg"
import grDark from "./imgs/gr-dark.svg"
import menagerie from "./imgs/menagerie.svg"
import menagerieDark from "./imgs/menagerie-dark.svg"
import mcw from "./imgs/mcw.svg"
import mcwDark from "./imgs/mcw-dark.svg"
import as from "./imgs/as.svg"
import asDark from "./imgs/as-dark.svg"
import toc from "./imgs/toc.svg"
import tocDark from "./imgs/toc-dark.svg"
import mom from "./imgs/mom.svg"
import momDark from "./imgs/mom-dark.svg"
import d from "./imgs/d.svg"
import dDark from "./imgs/d-dark.svg"

export const ExpansionIcon = observer((props: {
    expansion: Expansion,
    size?: number,
    white?: boolean,
    style?: React.CSSProperties
}) => {

    let lightSrc
    let darkSrc
    switch (props.expansion) {
        case Expansion.WORLDS_COLLIDE:
            lightSrc = wc
            darkSrc = wcDark
            break
        case Expansion.CALL_OF_THE_ARCHONS:
            lightSrc = cota
            darkSrc = cotaDark
            break
        case Expansion.AGE_OF_ASCENSION:
            lightSrc = aoa
            darkSrc = aoaDark
            break
        case Expansion.MASS_MUTATION:
            lightSrc = mm
            darkSrc = mmDark
            break
        case Expansion.DARK_TIDINGS:
            lightSrc = dt
            darkSrc = dtDark
            break
        case Expansion.WINDS_OF_EXCHANGE:
            lightSrc = woe
            darkSrc = woeDark
            break
        case Expansion.GRIM_REMINDERS:
            lightSrc = gr
            darkSrc = grDark
            break
        case Expansion.MENAGERIE_2024:
            lightSrc = menagerie
            darkSrc = menagerieDark
            break
        case Expansion.UNCHAINED_2022:
            lightSrc = uc
            darkSrc = ucDark
            break
        case Expansion.VAULT_MASTERS_2023:
        case Expansion.VAULT_MASTERS_2024:
        case Expansion.VAULT_MASTERS_2025:
            lightSrc = vm
            darkSrc = vmDark
            break
        case Expansion.ANOMALY_EXPANSION:
            lightSrc = anomaly
            darkSrc = anomalyDark
            break
        case Expansion.MARTIAN_CIVIL_WAR:
            lightSrc = mcw
            darkSrc = mcwDark
            break
        case Expansion.AEMBER_SKIES:
            lightSrc = as
            darkSrc = asDark
            break
        case Expansion.TOKENS_OF_CHANGE:
            lightSrc = toc
            darkSrc = tocDark
            break
        case Expansion.MORE_MUTATION:
            lightSrc = mom
            darkSrc = momDark
            break
        case Expansion.DISCOVERY:
            lightSrc = d
            darkSrc = dDark
            break
    }
    let src
    if (props.white != null) {
        src = props.white ? darkSrc : lightSrc
    } else {
        src = themeStore.darkMode ? darkSrc : lightSrc
    }
    if (src == null) {
        return null
    }
    const size = props.size == null ? 24 : props.size

    return (
        <img
            alt={expansionInfoMap.get(props.expansion)!.name}
            src={src}
            style={{width: size, height: size, ...props.style}}
        />
    )
})
