import { observer } from "mobx-react"
import * as React from "react"
import { spacing } from "../config/MuiConfig"
import { landingPageDrawerWidth, LandingPageTitle } from "../landing/LandingPage"
import { Loader } from "../mui-restyled/Loader"
import { screenStore } from "../ui/ScreenStore"
import { ContentCreatorCard } from "./ContentCreatorCard"
import { contentCreatorStore } from "./ContentCreatorStore"

export const FeaturedContentView = observer(() => {

    const featured = contentCreatorStore.featuredContent

    if (featured == null) {
        return <Loader/>
    } else if (featured.length === 0) {
        return null
    }

    const screenWidth = screenStore.screenWidth
    const availableWidth = screenWidth - (screenStore.screenSizeSm() ? 24 : (landingPageDrawerWidth + 24))

    return (
        <>
            <div style={{marginLeft: spacing(4)}}>
                <LandingPageTitle>
                    Featured Content
                </LandingPageTitle>
            </div>
            <div style={{display: "flex", overflowX: "auto", maxWidth: availableWidth}}>
                <div style={{marginLeft: spacing(2)}}/>
                {featured.map(contentCreator => (
                    <ContentCreatorCard
                        key={contentCreator.id}
                        contentCreator={contentCreator}
                        style={{flex: "0 0 auto"}}
                    />
                ))}
                <div style={{paddingLeft: spacing(2)}}/>
            </div>
        </>
    )
})
