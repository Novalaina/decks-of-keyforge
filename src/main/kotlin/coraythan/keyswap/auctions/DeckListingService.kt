package coraythan.keyswap.auctions

import coraythan.keyswap.*
import coraythan.keyswap.auctions.purchases.CreatePurchase
import coraythan.keyswap.auctions.purchases.CreatePurchaseResult
import coraythan.keyswap.auctions.purchases.PurchaseRepo
import coraythan.keyswap.auctions.purchases.PurchaseService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.SchedulingConfig
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.ownership.DeckOwnershipRepo
import coraythan.keyswap.decks.salenotifications.ForSaleNotificationsService
import coraythan.keyswap.emails.EmailService
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.patreon.levelAtLeast
import coraythan.keyswap.tags.CreateTag
import coraythan.keyswap.tags.PublicityType
import coraythan.keyswap.tags.TagService
import coraythan.keyswap.userdeck.ListingInfo
import coraythan.keyswap.userdeck.OwnedDeckRepo
import coraythan.keyswap.userdeck.UserDeckService
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.KeyUser
import coraythan.keyswap.users.KeyUserRepo
import coraythan.keyswap.users.search.UserSearchService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@Service
@Transactional
class DeckListingService(
    private val deckListingRepo: DeckListingRepo,
    private val currentUserService: CurrentUserService,
    private val deckRepo: DeckRepo,
    private val emailService: EmailService,
    private val forSaleNotificationsService: ForSaleNotificationsService,
    private val userRepo: KeyUserRepo,
    private val userSearchService: UserSearchService,
    private val purchaseService: PurchaseService,
    private val purchaseRepo: PurchaseRepo,
    private val userDeckService: UserDeckService,
    private val deckOwnershipRepo: DeckOwnershipRepo,
    private val tagService: TagService,
    private val ownedDeckRepo: OwnedDeckRepo,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun fixBadDeckListings() {
        var count = 0
        log.info("Begin fix bad deck listings.")
        deckRepo.listedDeckIds().forEach {  deckId ->
            val deck = deckRepo.findByIdOrNull(deckId) ?: error("No deck for id $deckId")
            var updated = false
            if (deck.forSale) {
                if (deck.auctions.none { it.status == DeckListingStatus.SALE }) {
                    log.info("Remove for sale from ${deck.name}")
                    deckRepo.save(deck.copy(forSale = false))
                    updated = true
                }
            }
            if (deck.forTrade) {
                if (deck.auctions.none { it.status == DeckListingStatus.SALE && it.forTrade }) {
                    log.info("Remove for trade from ${deck.name}")
                    deckRepo.save(deck.copy(forTrade = false))
                    updated = true
                }
            }
            if (updated) count++
        }
        log.info("Done fix bad deck listings fixed $count")
    }

    @Scheduled(fixedDelayString = "PT6H", initialDelayString = SchedulingConfig.unexpiredDecksInitialDelay)
    fun unlistExpiredDecks() {
        try {
            log.info("$scheduledStart unlisting expired for sale decks.")

            val buyItNowsToComplete =
                deckListingRepo.findAllByStatusEqualsAndEndDateTimeLessThanEqual(DeckListingStatus.SALE, now())

            buyItNowsToComplete.forEach {

                if (it.seller.autoRenewListings && it.seller.realPatreonTier() != null) {
                    deckListingRepo.save(it.copy(endDateTime = ZonedDateTime.now().plusYears(1)))
                } else {
                    removeDeckListingStatus(it, it.deck.id, it.seller)
                    deckListingRepo.delete(it)
                }
            }

            log.info("$scheduledStop unlisting expired for sale decks.")
        } catch (e: Throwable) {
            log.error("$scheduledException Couldn't unlist expired decks", e)
        }
    }

    fun bulkList(bulkListing: BulkListing, offsetMinutes: Int): Long? {
        val currentUser = currentUserService.loggedInUserOrUnauthorized()
        val tag = if (bulkListing.bulkListingTagName != null && currentUser.realPatreonTier()
                ?.levelAtLeast(PatreonRewardsTier.NOTICE_BARGAINS) == true
        ) {
            tagService.createTag(CreateTag(bulkListing.bulkListingTagName, PublicityType.BULK_SALE, true))
        } else {
            null
        }
        bulkListing.decks.forEach {
            val deck = deckRepo.findByIdOrNull(it)!!
            val listingsForUser =
                deckListingRepo.findBySellerIdAndDeckIdAndStatusNot(currentUser.id, it, DeckListingStatus.COMPLETE)
            if (listingsForUser.size > 1) {
                throw IllegalStateException("More than one listing for user for deck ${deck.name}")
            }
            val listingForUser = listingsForUser.singleOrNull()
            if (listingForUser?.status == DeckListingStatus.AUCTION) {
                throw BadRequestException("Cannot perform a bulk listing on decks for auction. Deck being auctioned: ${deck.name}")
            }
            if (tag != null) {
                tagService.tagDeck(it, tag.id)
            }

            list(
                bulkListing.listingInfo.copy(
                    deckId = it,
                    editAuctionId = listingForUser?.id,
                    tagId = tag?.id,
                ), offsetMinutes
            )
        }
        return tag?.id
    }

    fun list(listingInfo: ListingInfo, offsetMinutes: Int) {

        if (listingInfo.auction) throw BadRequestException("Auctions are now longer available on DoK.")

        val currentUser = currentUserService.loggedInUserOrUnauthorized()

        listingInfo.expireInDays ?: throw BadRequestException("Must include expires in days for auctions.")

        val deckId = listingInfo.deckId ?: throw BadRequestException("Must include a deck id for listings.")
        val deck = deckRepo.findByIdOrNull(deckId)
            ?: throw IllegalStateException("No deck with id ${listingInfo.deckId}")

        if (!ownedDeckRepo.existsByDeckIdAndOwnerId(
                deck.id,
                currentUser.id
            )
        ) throw BadRequestException("You must own ${deck.name} to list it for sale.")

        val listingDate = now()
        val endTime = listingInfo.endTimeLocalTime?.withOffsetMinutes(offsetMinutes) ?: LocalTime.now()
        val endDateTime = listingDate.plusDays(listingInfo.expireInDays.toLong())
            .withHour(endTime.hour)
            .withMinute(endTime.minute)
        val endMinutesMod = endDateTime.minute % 15
        val endMinutes = endDateTime.minute - endMinutesMod
        val realEnd = endDateTime.withMinute(endMinutes)
//        log.info("End minutes: ${endDateTime.minute} end minutes mod: ${endMinutesMod} end minutes mod rounded: ${endMinutes}")

        val hasOwnershipVerification = deckOwnershipRepo.existsByDeckIdAndUserId(deck.id, currentUser.id)
        val newListing = listingInfo.editAuctionId == null

        val tag = if (listingInfo.tagId == null) null else tagService.findTag(listingInfo.tagId)

        val auction = if (newListing) {
            val preexistingListing = deckListingRepo.findBySellerIdAndDeckIdAndStatusNot(
                currentUser.id,
                listingInfo.deckId!!,
                DeckListingStatus.COMPLETE
            )
            if (preexistingListing.isNotEmpty()) throw BadRequestException("You've already listed this deck for sale.")
            DeckListing(
                durationDays = listingInfo.expireInDays,
                endDateTime = realEnd,
                bidIncrement = listingInfo.bidIncrement,
                startingBid = listingInfo.startingBid,
                buyItNow = listingInfo.buyItNow,
                deck = deck,
                seller = currentUser,
                language = listingInfo.language,
                condition = listingInfo.condition,
                externalLink = listingInfo.externalLink,
                listingInfo = listingInfo.listingInfo,
                forSaleInCountry = currentUser.country
                    ?: throw BadRequestException("You must have selected a country to list decks for sale."),
                currencySymbol = currentUser.currencySymbol,
                status = DeckListingStatus.SALE,
                forTrade = currentUser.allowsTrades,
                shippingCost = currentUser.shippingCost,
                acceptingOffers = listingInfo.acceptingOffers,
                hasOwnershipVerification = hasOwnershipVerification,
                tag = tag,
                relistAtPrice = listingInfo.relistAtPrice,
            )
        } else {
            deckListingRepo.findByIdOrNull(listingInfo.editAuctionId!!)!!
                .copy(
                    durationDays = listingInfo.expireInDays,
                    endDateTime = realEnd,
                    bidIncrement = listingInfo.bidIncrement,
                    startingBid = listingInfo.startingBid,
                    buyItNow = listingInfo.buyItNow,
                    deck = deck,
                    seller = currentUser,
                    language = listingInfo.language,
                    condition = listingInfo.condition,
                    externalLink = listingInfo.externalLink,
                    listingInfo = listingInfo.listingInfo,
                    forSaleInCountry = currentUser.country
                        ?: throw BadRequestException("You must have selected a country to list decks for sale."),
                    currencySymbol = currentUser.currencySymbol,
                    status = DeckListingStatus.SALE,
                    forTrade = currentUser.allowsTrades,
                    shippingCost = currentUser.shippingCost,
                    acceptingOffers = listingInfo.acceptingOffers,
                    hasOwnershipVerification = hasOwnershipVerification,
                    tag = tag,
                    relistAtPrice = listingInfo.relistAtPrice,
                )
        }
        deckListingRepo.save(auction)
        deckRepo.save(deck.copy(forSale = true, forTrade = currentUser.allowsTrades))
        userRepo.save(currentUser.copy(mostRecentDeckListing = listingDate, updateStats = true))
        if (newListing) forSaleNotificationsService.sendNotifications(listingInfo, currentUser.username)
    }

    fun offerAccepted(deckId: Long) {
        cancelListing(deckId)
    }

    fun buyItNow(auctionId: UUID): BidPlacementResult {
        val user = currentUserService.loggedInUserOrUnauthorized()
        val auction =
            deckListingRepo.findByIdOrNull(auctionId) ?: throw BadRequestException("No sale listing for id $auctionId")
        if (auction.buyItNow == null) throw BadRequestException("Can't buy it now when the auction doesn't have buy it now.")
        if (user.id == auction.seller.id) throw UnauthorizedException("You can't buy your own deck.")
        if (auction.status == DeckListingStatus.COMPLETE) throw BadRequestException("Can't buy it now because it has already sold.")

        removeDeckListingStatus(auction, auction.deck.id, auction.seller)

        deckListingRepo.delete(auction)

        emailService.sendBoughtNowEmail(
            user,
            auction.seller,
            auction.deck,
            auction.buyItNow
        )
        purchaseService.savePurchase(
            saleType = auction.saleType,
            saleAmount = auction.buyItNow,
            deck = auction.deck,
            seller = auction.seller,
            buyer = user
        )

        return BidPlacementResult(
            successful = true,
            youAreHighBidder = true,
            message = "You have purchased the deck with your buy it now."
        )
    }

    fun deckListingInfo(auctionId: UUID, offsetMinutes: Int): DeckListingDto {
        val auction =
            deckListingRepo.findByIdOrNull(auctionId) ?: throw BadRequestException("No auction with id $auctionId")
        val fakeUsernames = auction.bids.sortedBy { it.bidTime }.map { it.bidder.username }
        val dto = auction.toDto(offsetMinutes)
        if (auction.endDateTime.plusDays(14) < now()) {
            // auction is two weeks old, show real usernames
            return dto
        }
        val currentUser = currentUserService.loggedInUser()
        return dto.copy(bids = dto.bids.map {
            it.copy(
                bidderUsername = if (currentUser?.username == it.bidderUsername) {
                    it.bidderUsername
                } else {
                    "User ${fakeUsernames.indexOf(it.bidderUsername) + 1}"
                }
            )
        })
    }

    fun cancelListing(deckId: Long): Boolean {
        val user = currentUserService.loggedInUserOrUnauthorized()
        val auctions = deckListingRepo.findBySellerIdAndDeckIdAndStatusNot(user.id, deckId, DeckListingStatus.COMPLETE)
        if (auctions.size > 1) {
            log.error("Seller shouldn't have more than one active auction for a single deck ${user.username} deckId: $deckId")
        }
        if (auctions.isEmpty()) return true
        val auction = auctions[0]
        if (auction.status == DeckListingStatus.AUCTION) {
            if (auction.bids.isNotEmpty()) return false
        }
        removeDeckListingStatus(auction, deckId, user)
        deckListingRepo.delete(auction)
        return true
    }

    fun cancelAndRemoveListing(deckId: Long): Boolean {
        this.cancelListing(deckId)
        userDeckService.markAsOwned(deckId, false)
        return true
    }

    fun findActiveListingsForUser(): List<UserDeckListingInfo> {
        val currentUser = currentUserService.loggedInUserOrUnauthorized()
        return deckListingRepo.findAllBySellerIdAndStatusNot(currentUser.id, DeckListingStatus.COMPLETE)
            .map { it.toUserDeckListingInfo() }
    }

    // This being in Purchase Service creates a dependency cycle
    fun createPurchase(
        createPurchase: CreatePurchase,
        verifyNoMatchingPurchases: Boolean = false
    ): CreatePurchaseResult {
        val loggedInUser = currentUserService.loggedInUserOrUnauthorized()
        val deck = deckRepo.findByIdOrNull(createPurchase.deckId)
            ?: throw BadRequestException("No deck for id ${createPurchase.deckId}")
        val seller = if (createPurchase.sellerId == null) null else userRepo.findByIdOrNull(createPurchase.sellerId)
        val buyer = if (createPurchase.buyerId == null) null else userRepo.findByIdOrNull(createPurchase.buyerId)
        if (seller == null && buyer == null) throw BadRequestException("One of buyer or seller must exist. Seller id: ${createPurchase.sellerId} buyer id: ${createPurchase.buyerId}")
        if (seller?.id != loggedInUser.id && buyer?.id != loggedInUser.id) throw BadRequestException("You must be the buyer or seller! Seller id: ${createPurchase.sellerId} buyer id: ${createPurchase.buyerId}")

        if (verifyNoMatchingPurchases && buyer != null && purchaseRepo.existsByDeckIdAndBuyerId(
                createPurchase.deckId,
                buyer.id
            )
        ) {
            return CreatePurchaseResult(
                createdOrUpdated = false,
                message = "You already have a purchase recorded for ${deck.name}."
            )
        }

        if (seller == null) {
            // reporting a purchase
            val preexisting = purchaseRepo.findByDeckId(createPurchase.deckId)
                .filter {
                    it.buyer == null && it.purchasedOn.isAfter(
                        LocalDateTime.now().minusDays(7)
                    ) && it.saleAmount == createPurchase.amount
                }
                .minByOrNull { it.purchasedOn }
            if (preexisting != null) {
                purchaseRepo.save(preexisting.copy(buyer = buyer, buyerCountry = buyer?.country))
                return CreatePurchaseResult(
                    createdOrUpdated = true,
                    message = "Added you as the buyer of ${deck.name}."
                )
            }
        } else if (buyer == null) {
            // reporting a sale
            this.cancelListing(deck.id)
            userDeckService.markAsOwned(deck.id, false)
            val preexisting = purchaseRepo.findByDeckId(createPurchase.deckId)
                .filter {
                    it.seller == null && it.purchasedOn.isAfter(
                        LocalDateTime.now().minusDays(7)
                    ) && it.saleAmount == createPurchase.amount
                }
                .minByOrNull { it.purchasedOn }
            if (preexisting != null) {
                purchaseRepo.save(
                    preexisting.copy(
                        seller = seller,
                        sellerCountry = seller.country,
                        currencySymbol = seller.currencySymbol
                    )
                )
                return CreatePurchaseResult(
                    createdOrUpdated = true,
                    message = "Created sale record and removed ${deck.name} from your decks."
                )
            }
        }

        purchaseService.savePurchase(createPurchase.saleType, createPurchase.amount, deck, seller, buyer)
        return CreatePurchaseResult(
            createdOrUpdated = true,
            message = if (buyer == null) {
                "Created sale record and removed ${deck.name} from your decks."
            } else {
                "Created purchase record for ${deck.name}."
            }
        )
    }

    fun removeAllDecks(password: String) {
        currentUserService.passwordMatches(password)
        val loggedInUser = currentUserService.loggedInUserOrUnauthorized()
        val hasAuction = deckListingRepo.existsBySellerIdAndStatus(loggedInUser.id, DeckListingStatus.AUCTION)
        if (hasAuction) throw BadRequestException("All auctions must be complete or canceled to remove all decks.")

        log.info("Start removing all for sale decks for ${loggedInUser.username}")
        val activeListings = deckListingRepo.findAllBySellerIdAndStatus(loggedInUser.id, DeckListingStatus.SALE)

        var count = 0
        activeListings.forEach {
            count++
            this.cancelListing(it.deck.id)
            if (count % 100 == 0) log.info("Removed $count decks for sale for ${loggedInUser.username}")
        }
        log.info("Done removing all for sale decks for ${loggedInUser.username}")

        userDeckService.removeAllOwned()
    }

    private fun removeDeckListingStatus(listing: DeckListing, deckId: Long, seller: KeyUser) {

        val listingsForDeck = listing.deck.auctions
        val forSale = listingsForDeck.any { it.seller.id != seller.id && it.status == DeckListingStatus.SALE }
        val forTrade = listingsForDeck.any { it.seller.id != seller.id && it.status == DeckListingStatus.SALE && it.forTrade }

        deckRepo.updateForSaleAndTrade(deckId, forSale, forTrade)
        userSearchService.scheduleUserForUpdate(seller)
    }

}
