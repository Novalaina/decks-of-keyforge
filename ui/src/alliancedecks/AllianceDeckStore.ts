import axios, { AxiosResponse } from "axios"
import { clone } from "lodash"
import { computed, makeObservable, observable } from "mobx"
import { HttpConfig } from "../config/HttpConfig"
import { keyLocalStorage } from "../config/KeyLocalStorage"
import { messageStore } from "../ui/MessageStore"
import { DeckCount, DeckPage, DeckSearchResult, DeckWithSynergyInfo } from "../decks/models/DeckSearchResult";
import { AllianceDeckFilters } from "../generated-src/AllianceDeckFilters";
import { DeckStoreInterface } from "./DeckStoreInterface";
import { AllianceDeckHouses } from "../generated-src/AllianceDeckHouses";
import { log } from "../config/Utils";
import { userDeckStore } from "../userdeck/UserDeckStore";

export class AllianceDeckStore implements DeckStoreInterface {
    static readonly DECK_PAGE_SIZE = 20
    static readonly CONTEXT = HttpConfig.API + "/alliance-decks"
    static readonly SECURE_CONTEXT = HttpConfig.API + "/alliance-decks/secured"

    @observable
    simpleDecks: Map<string, DeckSearchResult> = new Map()

    @observable
    currentDeckPage = 0

    @observable
    decksToDisplay?: string[]

    deckIdToDeck?: Map<string, DeckSearchResult>

    deckPage?: DeckPage
    nextDeckPage?: DeckPage

    @observable
    decksCount?: DeckCount

    @observable
    currentFilters?: AllianceDeckFilters

    @observable
    searchingForDecks = false

    @observable
    countingDecks = false

    @observable
    addingMoreDecks = false

    @observable
    deck?: DeckWithSynergyInfo

    @observable
    savingDeck = false

    @observable
    savedDeckId?: string

    reset = () => {
        this.currentDeckPage = 0
        this.deckIdToDeck = undefined
        this.deckPage = undefined
        this.nextDeckPage = undefined
        this.decksCount = undefined
        this.currentFilters = undefined
    }

    saveAllianceDeck = async () => {
        this.savingDeck = true
        const deck = keyLocalStorage.allianceDeckSaveInfo
        const [deckOne, deckTwo, deckThree] = deck.houses
        const allianceDeckSaveInfo: AllianceDeckHouses = {
            houseOne: deckOne!.house,
            houseOneDeckId: deckOne!.deckId,
            houseTwo: deckTwo!.house,
            houseTwoDeckId: deckTwo!.deckId,
            houseThree: deckThree!.house,
            houseThreeDeckId: deckThree!.deckId,
            owned: keyLocalStorage.genericStorage.addAllianceToMyAlliances !== false,
            tokenName: deck.tokenName,
            propheciesDeckId: deck.propheciesDeckId,
        }
        const response: AxiosResponse<string> = await axios.post(`${AllianceDeckStore.SECURE_CONTEXT}`, allianceDeckSaveInfo)

        this.savedDeckId = response.data
        this.savingDeck = false
        return response.data
    }

    markOwned = async (id: string, name: string) => {
        await axios.post(`${AllianceDeckStore.SECURE_CONTEXT}/${id}/owned`)
        userDeckStore.addToOwnedAllianceDecks(id)
        messageStore.setSuccessMessage(`Added ${name} to your alliances.`)
    }

    markNotOwned = async (id: string, name: string) => {
        await axios.post(`${AllianceDeckStore.SECURE_CONTEXT}/${id}/unowned`)
        userDeckStore.removeFromOwnedAllianceDecks(id)
        messageStore.setSuccessMessage(`Removed ${name} from your alliances.`)
    }

    findDeck = (keyforgeId: string) => {
        axios.get(`${AllianceDeckStore.CONTEXT}/with-synergies/${keyforgeId}`)
            .then((response: AxiosResponse) => {
                const deck: DeckWithSynergyInfo = response.data
                if (!deck || !deck.deck) {
                    messageStore.setWarningMessage(`We couldn't find the alliance deck with the id: ${keyforgeId}`)
                } else {
                    this.deck = deck
                }
            })
    }

    findDeckWithCards = (keyforgeId: string) => {
        if (this.simpleDecks.get(keyforgeId) == null) {
            axios.get(`${AllianceDeckStore.CONTEXT}/search-result-with-cards/${keyforgeId}`)
                .then((response: AxiosResponse) => {
                    const deck: DeckSearchResult = response.data
                    this.simpleDecks.set(keyforgeId, deck)
                })
        }
    }

    refreshDeckSearch = () => {
        if (this.currentFilters) {
            return this.searchDecks(this.currentFilters)
        }
    }

    searchDecks = async (filters: AllianceDeckFilters) => {
        this.searchingForDecks = true
        filters.pageSize = keyLocalStorage.deckPageSize
        this.currentFilters = clone(filters)
        // log.debug(`Searching for first deck page with ${prettyJson(this.currentFilters)}`)
        this.nextDeckPage = undefined
        const decksPromise = this.findDecks(filters)
        this.findDecksCount(filters)
        const decks = await decksPromise
        if (decks) {
            log.debug(`Replacing decks page with decks:  ${decks.decks.map((deck, idx) => `\n${idx + 1}. ${deck.name}`)}`)
            this.decksToDisplay = undefined
            this.deckIdToDeck = undefined
            this.addNewDecksToDecks(decks)
        }
        this.searchingForDecks = false
        this.findNextDecks()
    }

    findNextDecks = async () => {
        if (this.currentFilters && this.moreDecksAvailable()) {
            this.addingMoreDecks = true
            this.currentFilters.page++
            // log.debug(`Searching for next deck page with ${prettyJson(this.currentFilters)}`)
            const decks = await this.findDecks(this.currentFilters)
            if (decks) {
                this.addingMoreDecks = false
                this.nextDeckPage = decks

            }
        }
    }

    showMoreDecks = async () => {
        if (this.deckPage && this.nextDeckPage && this.decksCount) {
            // log.debug(`Current decks page ${this.deckPage.page}. Total pages ${this.decksCount.pages}.`)
            this.addNewDecksToDecks(this.nextDeckPage)
            this.nextDeckPage = undefined
            this.findNextDecks()
            // log.debug(`Done finding decks. Current decks page ${this.deckPage.page}. Total pages ${this.decksCount.pages}.`)
        }
    }

    displayDecks = () => {
        if (this.decksToDisplay == null) {
            return []
        }
        return this.decksToDisplay
            .map(deckId => this.deckIdToDeck?.get(deckId))
            .filter(deck => deck != null) as DeckSearchResult[]
    }

    moreDecksAvailable = () => (this.deckPage && this.decksCount && this.deckPage.page + 1 < this.decksCount.pages)
        || (this.deckPage && !this.decksCount && this.deckPage.decks.length % AllianceDeckStore.DECK_PAGE_SIZE === 0)


    constructor() {
        makeObservable(this)
    }

    @computed
    get searchingOrLoaded(): boolean {
        return this.searchingForDecks || this.decksToDisplay != null
    }

    private addNewDecksToDecks = (decks: DeckPage) => {
        this.deckPage = decks
        if (this.deckIdToDeck == null) {
            this.deckIdToDeck = new Map()
        }
        decks.decks.forEach(deck => {
            this.deckIdToDeck!.set(deck.keyforgeId, deck)
        })
        const decksToAdd = decks.decks.map(deck => deck.keyforgeId)
        if (this.decksToDisplay == null) {
            this.decksToDisplay = []
        }
        this.decksToDisplay = this.decksToDisplay.concat(decksToAdd)
        this.currentDeckPage = decks.page
    }

    private findDecks = async (filters: AllianceDeckFilters) => {
        const response: AxiosResponse<DeckPage> = await axios.post(`${AllianceDeckStore.CONTEXT}/filter`, filters)
        return response.data
    }

    private findDecksCount = (filters: AllianceDeckFilters) => {
        this.decksCount = undefined
        this.countingDecks = true
        axios.post(`${AllianceDeckStore.CONTEXT}/filter-count`, filters)
            .then((response: AxiosResponse) => {
                this.countingDecks = false
                this.decksCount = response.data
            })
    }
}

export const allianceDeckStore = new AllianceDeckStore()
