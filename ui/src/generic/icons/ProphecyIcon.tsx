import * as React from "react"
import Prophecy from "../imgs/prophecy-icon.svg"

export const ProphecyIcon = (props: { width?: number, style?: React.CSSProperties }) => {
    return (
        <img src={Prophecy} style={{height: props.width ? props.width : 24, ...props.style}} alt={"Prophecy"}/>
    )
}
