import axios, { AxiosResponse } from "axios"
import { makeObservable, observable } from "mobx"
import { HttpConfig } from "../config/HttpConfig"
import { Utils } from "../config/Utils"
import { ContentCreatorDetailsDto } from "../generated-src/ContentCreatorDetailsDto"
import { ContentCreatorFilters } from "../generated-src/ContentCreatorFilters"
import { SaveContentCreatorDetails } from "../generated-src/SaveContentCreatorDetails"
import { messageStore } from "../ui/MessageStore"

export class ContentCreatorStore {
    static readonly CONTEXT = HttpConfig.API + "/content-creators"
    static readonly PUBLIC_CONTEXT = HttpConfig.API + "/content-creators/public"
    static readonly SECURE_CONTEXT = HttpConfig.API + "/content-creators/secured"

    @observable
    featuredContent?: ContentCreatorDetailsDto[] = undefined

    @observable
    foundContentCreators?: ContentCreatorDetailsDto[] = undefined

    @observable
    searching = false

    @observable
    saving = false

    @observable
    savingPromoImage = false

    @observable
    deleting = false

    @observable
    contentCreatorBeingEdited?: ContentCreatorDetailsDto = undefined

    @observable
    loadingContentCreator = false

    private currentFilters?: ContentCreatorFilters

    findFeaturedContent = () => {
        axios.get(`${ContentCreatorStore.CONTEXT}/featured`)
            .then((response: AxiosResponse<ContentCreatorDetailsDto[]>) => {
                this.featuredContent = response.data
            })
    }

    searchContentCreators = async (filters: ContentCreatorFilters) => {
        this.currentFilters = Utils.jsonCopy(filters)
        this.searching = true
        try {
            const response: AxiosResponse<ContentCreatorDetailsDto[]> =
                await axios.post(`${ContentCreatorStore.PUBLIC_CONTEXT}/search`, filters)
            this.foundContentCreators = response.data
        } finally {
            this.searching = false
        }
    }

    findContentCreator = async (id: string) => {
        this.loadingContentCreator = true
        try {
            const response: AxiosResponse<ContentCreatorDetailsDto> =
                await axios.get(`${ContentCreatorStore.PUBLIC_CONTEXT}/${id}`)
            this.contentCreatorBeingEdited = response.data
            return response.data
        } finally {
            this.loadingContentCreator = false
        }
    }

    saveContentCreator = async (toSave: SaveContentCreatorDetails) => {
        this.saving = true
        try {
            const response: AxiosResponse<ContentCreatorDetailsDto> =
                await axios.post(ContentCreatorStore.SECURE_CONTEXT, toSave)
            this.contentCreatorBeingEdited = response.data
            messageStore.setSuccessMessage(toSave.id == null ? "Created your content!" : "Updated your content.")
            await this.refreshSearch()
            return response.data
        } finally {
            this.saving = false
        }
    }

    deleteContentCreator = async (id: string) => {
        this.deleting = true
        try {
            await axios.delete(`${ContentCreatorStore.SECURE_CONTEXT}/${id}`)
            messageStore.setSuccessMessage("Deleted your content.")
            await this.refreshSearch()
        } finally {
            this.deleting = false
        }
    }

    savePromoImage = async (id: string, promoImage: File | Blob, extension: string) => {
        this.savingPromoImage = true
        try {
            const imageData = new FormData()
            imageData.append("promoImage", promoImage)

            const response: AxiosResponse<ContentCreatorDetailsDto> = await axios.post(
                `${ContentCreatorStore.SECURE_CONTEXT}/${id}/promo-image`,
                imageData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                        "Extension": extension
                    }
                }
            )
            this.contentCreatorBeingEdited = response.data
        } finally {
            this.savingPromoImage = false
        }
    }

    deletePromoImage = async (id: string) => {
        this.savingPromoImage = true
        try {
            const response: AxiosResponse<ContentCreatorDetailsDto> =
                await axios.delete(`${ContentCreatorStore.SECURE_CONTEXT}/${id}/promo-image`)
            this.contentCreatorBeingEdited = response.data
        } finally {
            this.savingPromoImage = false
        }
    }

    /**
     * Records that someone followed a content creator's link. Failures are ignored so they never
     * block navigating to the creator's site.
     */
    recordClick = (id: string) => {
        axios.post(`${ContentCreatorStore.PUBLIC_CONTEXT}/${id}/click`)
            .catch(() => {
                // Click tracking is best effort.
            })
    }

    private refreshSearch = async () => {
        if (this.currentFilters != null) {
            await this.searchContentCreators(this.currentFilters)
        }
    }

    constructor() {
        makeObservable(this)
    }
}

export const contentCreatorStore = new ContentCreatorStore()
