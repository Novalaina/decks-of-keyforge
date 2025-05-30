import { Tooltip } from "@material-ui/core"
import IconButton from "@material-ui/core/IconButton/IconButton"
import Typography from "@material-ui/core/Typography"
import TagFacesIcon from "@material-ui/icons/TagFaces"
import { makeObservable, observable } from "mobx"
import { observer } from "mobx-react"
import * as React from "react"
import { userDeckStore } from "../../userdeck/UserDeckStore"
import { themeStore } from "../../config/MuiConfig"

interface FunnyDeckProps {
    funnyCount: number
    deckId: number
    deckName: string
}

@observer
export class FunnyDeck extends React.Component<FunnyDeckProps> {
    @observable
    funnyCount = 0

    constructor(props: FunnyDeckProps) {
        super(props)
        makeObservable(this)
    }

    componentDidMount(): void {
        this.funnyCount = this.props.funnyCount
    }

    componentDidUpdate(): void {
        this.funnyCount = this.props.funnyCount
    }

    render() {
        const {deckId, deckName} = this.props
        let title
        let funny = false
        if (userDeckStore.funnyDecks != null) {
            funny = userDeckStore.funnyDecks.includes(deckId)
            title = (funny ? "Remove as" : "Mark as") + " a funny deck"
        } else {
            title = "Login to mark decks as funny"
        }
        let color: "primary" | "secondary" | undefined
        if (funny) {
            if (themeStore.darkMode) {
                color = "secondary"
            } else {
                color = "primary"
            }
        }
        return (
            <div style={{display: "flex", alignItems: "center"}}>
                <Tooltip title={title}>
                    <div>
                        <IconButton
                            onClick={() => {
                                funny = !funny
                                this.funnyCount += (funny ? 1 : -1)
                                userDeckStore.funny(deckName, deckId, funny)
                            }}
                            disabled={!userDeckStore.userDecksLoaded()}
                            size={"small"}
                        >
                            <TagFacesIcon color={color}/>
                        </IconButton>
                    </div>
                </Tooltip>
                <Tooltip title={"Times marked funny"}>
                    <Typography variant={"body1"}>{this.funnyCount}</Typography>
                </Tooltip>
            </div>
        )
    }
}
