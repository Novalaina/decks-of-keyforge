import { themeStore } from "../../../config/MuiConfig"
import { EnhancementIconSize } from "../../icons/EnhancementIcon"

export const StarAllianceEnhancedIcon = () => {
    return (
        <svg viewBox="-10 0 876 860" width={EnhancementIconSize} height={EnhancementIconSize}>
            <path fill={themeStore.iconColor}
                  d="M705 368l151 59l-305 120l-120 305l-95 -242q-179 90 -274 102q-55 6 -55 -21q5 -44 140 -157q-20 24 -26 39q-10 27 25 25q49 -3 155 -55l-295 -116l305 -120l120 -305l120 305l64 25q52 -50 46 -71q-6 -24 -80 -4q169 -77 241 -76q34 0 33 20q-6 49 -150 167z"/>
        </svg>
    )
}
