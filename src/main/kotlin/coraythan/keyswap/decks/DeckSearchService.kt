package coraythan.keyswap.decks

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Ops
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import coraythan.keyswap.*
import coraythan.keyswap.auctions.DeckListingStatus
import coraythan.keyswap.auctions.QDeckListing
import coraythan.keyswap.cards.TokenService
import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.SchedulingConfig
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.decks.models.*
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.patreon.levelAtLeast
import coraythan.keyswap.stats.StatsService
import coraythan.keyswap.synergy.synergysystem.DeckSynergyService
import coraythan.keyswap.tags.KTagRepo
import coraythan.keyswap.tags.PublicityType
import coraythan.keyswap.tags.QDeckTag
import coraythan.keyswap.userdeck.*
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.KeyUser
import coraythan.keyswap.users.KeyUserService
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val lockCountDecks = "PT4H"

@Transactional
@Service
class DeckSearchService(
    private val deckRepo: DeckRepo,
    private val userService: KeyUserService,
    private val currentUserService: CurrentUserService,
    private val statsService: StatsService,
    private val tagRepo: KTagRepo,
    private val entityManager: EntityManager,
    private val ownedDeckRepo: OwnedDeckRepo,
    private val ownedDeckService: OwnedDeckService,
    private val cardCache: DokCardCacheService,
    private val tokenService: TokenService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val defaultFilters = DeckFilters()
    private val query = JPAQueryFactory(entityManager)

    companion object {
        var deckCount: Long? = null
        var deckCountByExpansion: Map<Expansion, Long>? = null
    }

    @Scheduled(
        fixedDelayString = lockCountDecks,
        initialDelayString = SchedulingConfig.countDecksIntialDelay
    )
    fun countDecks() {
        log.info("$scheduledStart count decks.")

        deckCount = deckRepo.count()
        deckCountByExpansion = Expansion.entries.associateWith { expansion ->
            deckRepo.countByExpansion(expansion.expansionNumber)
        }

        log.info("$scheduledStop count decks.")
    }

    fun countFilters(filters: DeckFilters): DeckCount {

        val count: Long
        val preExistingCount = deckCount
        val preExistingCountMap = deckCountByExpansion
        if (preExistingCount != null && preExistingCountMap != null && filtersAreEqualForCount(filters)) {
            count = if (filters.expansions == defaultFilters.expansions) {
                preExistingCount
            } else {
                filters.expansions.sumOf {
                    preExistingCountMap[Expansion.forExpansionNumber(it)] ?: 0
                }
            }
        } else {

            val userHolder = UserHolder(null, currentUserService, userService)
            val predicate = deckFilterPredicate(filters, userHolder, filters.sort)

            val deckQ = QDeckSasValuesSearchable.deckSasValuesSearchable
            count = query
                .select(deckQ.id)
                .from(deckQ)
                .where(predicate)
                .limit(
                    if (
                        filters.forSale == true || filters.forTrade
                    ) 20000 else 1000
                )
                .fetch()
                .count()
                .toLong()
        }

        return DeckCount(
            pages = (count + filters.pageSize - 1) / filters.pageSize,
            count = count
        )
    }

    fun filterDecks(filters: DeckFilters, timezoneOffsetMinutes: Int): DecksPage {

//        log.info("Filter decks for deck search")

        val userHolder = UserHolder(null, currentUserService, userService)
        val predicate = deckFilterPredicate(filters, userHolder, filters.sort)
        val deckQ = QDeckSasValuesSearchable.deckSasValuesSearchable
        val sortProperty = when (filters.sort) {
            DeckSortOptions.ADDED_DATE -> deckQ.id
            DeckSortOptions.SAS_RATING -> deckQ.sasRating
            DeckSortOptions.NAME -> deckQ.name
        }

        val sort = if (filters.sort == DeckSortOptions.NAME) {
            if (filters.sortDirection == SortDirection.DESC) {
                (sortProperty as ComparableExpressionBase<*>).asc()
            } else {
                (sortProperty as ComparableExpressionBase<*>).desc()
            }
        } else if (filters.sortDirection == SortDirection.DESC) {
            (sortProperty as ComparableExpressionBase<*>).desc()
        } else {
            (sortProperty as ComparableExpressionBase<*>).asc()
        }

//        log.info("Filter decks for deck search 2")

        val dsvResults = query.selectFrom(deckQ)
            .innerJoin(deckQ.deck).fetchJoin()
            .where(predicate)
            .limit(filters.pageSize)
            .offset(filters.page * filters.pageSize)
            .apply {
                if (filters.sort != DeckSortOptions.ADDED_DATE) {
                    orderBy(sort, deckQ.id.asc())
                } else {
                    orderBy(sort)
                }
            }
            .fetch()

        val deckResults = dsvResults.map { it.deck }

//        log.info("Filter decks for deck search 3")

        val decks = deckResults.mapNotNull {

            val cardsAndToken = cardCache.cardsAndTokenFutureProof(it, userHolder.user)

            var searchResult = it.toDeckSearchResult(
                cardCache.deckToHouseAndCards(it),
                cardsAndToken.cards,
                stats = statsService.findCurrentStats(),
                synergies = DeckSynergyService.fromDeckWithCards(it, cardsAndToken.cards, cardsAndToken.token),
                token = cardsAndToken.token,
            )

            if (filters.forSale == true || filters.forTrade) {
                searchResult = searchResult.copy(
                    deckSaleInfo = saleInfoForDeck(
                        searchResult.keyforgeId,
                        timezoneOffsetMinutes,
                        it,
                        userHolder.user
                    )
                )
            }
            if (filters.teamDecks) {
                val user = userHolder.user
                val teamId = user?.teamId ?: throw BadRequestException("You aren't on a team.")
                if (user.realPatreonTier() == null) throw BadRequestException("You do not have permission to view team decks.")
                val owners = ownedDeckRepo.findByDeckIdAndTeamId(it.id, teamId).map { userDeck ->
                    userDeck.owner.username
                }
                if (owners.isNotEmpty()) {
                    searchResult = searchResult.copy(owners = owners)
                }
            }
            if (filters.owners.isNotEmpty()) {
                val visibleUsers =
                    if (filters.forSale == true || filters.forTrade) filters.owners else filters.owners.filter { owner ->
                        val foundUser = userService.findUserByUsername(owner)
                        when {
                            foundUser == null -> false
                            foundUser.teamId != null && foundUser.teamId == userHolder.user?.teamId -> true
                            else -> foundUser.allowUsersToSeeDeckOwnership
                        }
                    }

                val owners = ownedDeckRepo.findByDeckIdAndOwnedByIn(it.id, visibleUsers).map { userDeck ->
                    userDeck.owner.username
                }
                if (owners.isNotEmpty()) {
                    searchResult = searchResult.copy(owners = owners)
                }
            }

            if (filters.withOwners) {
                if (userHolder.user?.realPatreonTier()
                        ?.levelAtLeast(PatreonRewardsTier.SUPPORT_SOPHISTICATION) != true
                ) {
                    throw BadRequestException("Please become a $6+ a month patron to view owners.")
                }
                val deckWithOwners = ownedDeckService.addOwners(searchResult)
                if (deckWithOwners.owners == null) {
                    null
                } else {
                    deckWithOwners
                }
            } else {
                searchResult
            }

        }

        return DecksPage(
            decks,
            filters.page
        )
    }

    private fun filtersAreEqualForCount(filters: DeckFilters) = filters.copy(
        sort = defaultFilters.sort,
        sortDirection = defaultFilters.sortDirection,
        expansions = defaultFilters.expansions,
        page = defaultFilters.page,
        pageSize = defaultFilters.pageSize,
    ) == defaultFilters

    fun deckExistsForFilters(
        filters: DeckQuery,
        userHolder: UserHolder,
        deckId: Long,
    ): Boolean {
        val deckQ = QDeckSasValuesSearchable.deckSasValuesSearchable
        val predicate = deckFilterPredicate(filters, userHolder)
        predicate.and(deckQ.deckId.eq(deckId))

        val count = query
            .select(deckQ.id)
            .from(deckQ)
            .limit(1)
            .where(predicate)
            .fetch()
            .count()

        return count > 0
    }

    fun deckFilterPredicate(
        filters: DeckQuery,
        userHolder: UserHolder,
        sortOptions: DeckSortOptions? = null,
    ): BooleanBuilder {
        val deckQ = QDeckSasValuesSearchable.deckSasValuesSearchable
        val ownedDecksQ = QOwnedDeck.ownedDeck
        val tagQ = QDeckTag.deckTag
        val predicate = BooleanBuilder()

        if (filters.expansions.isNotEmpty()) {
            val expansions = if (filters.tokens.isEmpty()) {
                filters.expansions
            } else {
                val validExpansions = filters.expansions
                    .filter {
                        val expToFilter = Expansion.forExpansionNumber(it)
                        Expansion.expansionsWithTokens().contains(expToFilter)
                    }
                validExpansions
            }
            if (expansions.isEmpty()) {
                // Nothing! No tokens in these expansions
                predicate.and(deckQ.expansion.eq(-100))
            } else {
                predicate.andAnyOf(*expansions.map { deckQ.expansion.eq(it) }.toTypedArray())
            }
        }

        if (filters.houses.isNotEmpty()) {
            if (filters.houses.size < 4) {
                filters.houses.forEach { predicate.and(deckQ.houseNamesString.like("%$it%")) }
            } else {
                val excludeHouses = House.entries.filter { !filters.houses.contains(it) }
                excludeHouses.forEach { predicate.and(deckQ.houseNamesString.notLike("%$it%")) }
            }
        }

        val excludeHouses = filters.excludeHouses
        if (!excludeHouses.isNullOrEmpty()) {
            excludeHouses.forEach { predicate.and(deckQ.houseNamesString.notLike("%$it%")) }
        }

        if (filters.title.isNotBlank()) {
            if (filters.titleQl) {
                predicate.and(deckQ.name.like("%${filters.title}%"))
            } else {
                filters.title.tokenize().forEach { predicate.and(deckQ.name.likeIgnoreCase("%$it%")) }
            }
        }

        if (filters.teamDecks) {
            val teamId = userHolder.user?.teamId
            if (teamId != null) {
                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(ownedDecksQ.deckId)
                            .from(ownedDecksQ)
                            .where(ownedDecksQ.teamId.eq(teamId))
                    )
                )
            }
        }

        filters.tags.forEach {
            canAccessTag(it, userHolder.user?.id)

            predicate.and(
                deckQ.deckId.eqAny(
                    JPAExpressions
                        .select(tagQ.deck.id)
                        .from(tagQ)
                        .where(tagQ.tag.id.eq(it))
                )
            )
        }

        filters.notTags.forEach {
            canAccessTag(it, userHolder.user?.id)

            predicate.andNot(
                deckQ.deckId.eqAny(
                    JPAExpressions
                        .select(tagQ.deck.id)
                        .from(tagQ)
                        .where(tagQ.tag.id.eq(it))
                )
            )
        }

        if (filters.notes.isNotBlank()) {
            val username = filters.notesUser.ifBlank {
                userHolder.user?.username
            } ?: throw IllegalArgumentException("No notes user for notes.")

            val trimmed = filters.notes.lowercase().trim()
            val deckNoteQ = QDeckNote.deckNote
            predicate.and(
                deckQ.deckId.eqAny(
                    JPAExpressions
                        .select(deckNoteQ.deck.id)
                        .from(deckNoteQ)
                        .where(deckNoteQ.user.username.eq(username).and(deckNoteQ.notes.containsIgnoreCase(trimmed)))
                )
            )
        }

        if (filters.previousOwner.isNotBlank()) {
            val user = userHolder.user
            if (userHolder.user?.username == filters.previousOwner && user != null) {
                // it's me
                val previouslyOwnedDeckQ = QPreviouslyOwnedDeck.previouslyOwnedDeck
                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(previouslyOwnedDeckQ.deck.id)
                            .from(previouslyOwnedDeckQ)
                            .where(previouslyOwnedDeckQ.previousOwner.id.eq(userHolder.user?.id))
                    )
                )
            } else {
                throw UnauthorizedException("You cannot view other users' previously owned decks.")
            }
        }

        if (filters.owners.isNotEmpty() || filters.owner.isNotBlank()) {

            val allOwners = filters.owners.toSet().plus(filters.owner)
                .filter { it.isNotBlank() }
                .mapNotNull { userService.findIdAndDeckVisibilityByUsername(it) }

            val visibleUsers =
                if (filters.forSale == true || filters.forTrade) allOwners else allOwners.filter {
                    it.allowUsersToSeeDeckOwnership
                }

            if (allOwners.size == 1 && allOwners[0].id == userHolder.user?.id) {
                // it's me
                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(ownedDecksQ.deckId)
                            .from(ownedDecksQ)
                            .where(ownedDecksQ.owner.id.eq(userHolder.user?.id))
                    )
                )
                if (filters.notForSale) {
                    predicate.andNot(deckQ.deck.forSale)
                }
            } else if (filters.teamDecks) {
                // team decks
                val myTeamId = userHolder.user?.teamId
                if (myTeamId == null || allOwners.any { it.teamId != myTeamId }) {
                    throw UnauthorizedException("You must be logged in and share teams with the searched user to see their decks.")
                }
                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(ownedDecksQ.deckId)
                            .from(ownedDecksQ)
                            .where(ownedDecksQ.owner.id.`in`(allOwners.map { it.id }))
                    )
                )
                if (filters.notForSale) {
                    predicate.andNot(deckQ.deck.forSale)
                }
            } else if (allOwners.size == 1 && !allOwners[0].allowUsersToSeeDeckOwnership) {
                // One user search and not public, show for sale

                val deckListingQ = QDeckListing.deckListing
                predicate.and(
                    deckQ.deck.auctions.any().`in`(
                        JPAExpressions.selectFrom(deckListingQ)
                            .where(
                                deckListingQ.seller.id.eq(allOwners[0].id),
                                deckListingQ.status.ne(DeckListingStatus.COMPLETE)
                            )
                    )
                )
            } else {
                // just find the publicly owned ones

                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(ownedDecksQ.deckId)
                            .from(ownedDecksQ)
                            .where(ownedDecksQ.owner.id.`in`(visibleUsers.map { it.id }))
                    )
                )
            }
        }

        if (filters.myFavorites) {
            val favsUserId = userHolder.user?.id
            if (favsUserId != null) {
                val favoritedDecksQ = QFavoritedDeck.favoritedDeck
                predicate.and(
                    deckQ.deckId.eqAny(
                        JPAExpressions
                            .select(favoritedDecksQ.deck.id)
                            .from(favoritedDecksQ)
                            .where(favoritedDecksQ.user.id.eq(favsUserId))
                    )
                )
            }
        }

        if (filters.forSale == true && filters.forTrade) {
            predicate.and(
                BooleanBuilder().andAnyOf(
                    *listOfNotNull(
                        deckQ.deck.forTrade.isTrue,
                        deckQ.deck.forSale.isTrue
                    ).toTypedArray()
                )
            )
        } else if (filters.forSale == true) {
            predicate.and(deckQ.deck.forSale.isTrue)
        } else if (filters.forTrade) {
            predicate.and(deckQ.deck.forTrade.isTrue)
        }

        val listedWithinDays = filters.listedWithinDays
        if ((filters.forSale == true || filters.forTrade) && listedWithinDays != null) {
            predicate.and(deckQ.deck.auctions.any().dateListed.gt(now().minusDays(listedWithinDays.toLong())))
        }

        if ((filters.forSale == true || filters.forTrade) && filters.forSaleInCountry != null) {
            val preferredCountries = userHolder.user?.preferredCountries
            if (preferredCountries.isNullOrEmpty()) {
                predicate.and(
                    deckQ.deck.auctions.any().forSaleInCountry.eq(filters.forSaleInCountry)
                )
            } else {
                predicate.andAnyOf(*preferredCountries.map {
                    deckQ.deck.auctions.any().forSaleInCountry.eq(it)
                }.toTypedArray())
            }
        }
        if (filters.constraints.isNotEmpty()) {
            filters.constraints.forEach {
                val property = if (it.property == "bonusAmber") {
                    "rawAmber"
                } else {
                    it.property
                }
                if (property == "listedWithinDays") {
                    predicate.and(deckQ.deck.auctions.any().dateListed.gt(now().minusDays(it.value.toLong())))
                } else {
                    val pathToVal = if (property == "buyItNow") {
                        Expressions.path(Double::class.java, deckQ.deck.auctions.any(), property)
                    } else {
                        Expressions.path(Double::class.java, deckQ, property)
                    }
                    val capOpsValue = when (it.cap) {
                        Cap.MIN -> Ops.GOE
                        Cap.MAX -> Ops.LOE
                        Cap.EQUALS -> Ops.EQ
                    }
                    predicate.and(Expressions.predicate(capOpsValue, pathToVal, Expressions.constant(it.value)))
                }
            }
        }

        if (filters.tokens.isNotEmpty()) {
            predicate.and(
                deckQ.tokenNumber.`in`(
                    *filters.tokens.map { tokenService.cardTitleToTokenId(it) }.toTypedArray()
                )
            )
        }

        filters.cards.forEach {
            when {
                it.mav == true -> {
                    predicate.andAnyOf(
                        *it.cardNames.flatMap { cardName ->
                            House.entries.toSet().minus(cardCache.findByCardName(cardName).dokCard.houses.toSet())
                                .map { otherHouse ->
                                    deckQ.cardNames.like("%~$cardName${otherHouse}~%")
                                }

                        }.toTypedArray()
                    )
                }

                it.house != null -> predicate.andAnyOf(
                    *it.cardNames.map { cardName ->
                        deckQ.cardNames.like("%~$cardName${it.house}~%")
                    }.toTypedArray()
                )

                it.quantity == 0 -> it.cardNames.forEach { cardName ->
                    predicate.and(deckQ.cardNames.notLike("%~${cardName}1%"))
                }

                else -> predicate.andAnyOf(
                    *it.cardNames.map { cardName ->
                        deckQ.cardNames.like("%~$cardName${(1..it.quantity).joinToString("")}%")
                    }.toTypedArray()
                )
            }
        }

        if (filters.tournamentIds.isNotEmpty()) {
            predicate.andAnyOf(
                *filters.tournamentIds.map {
                    deckQ.deck.tournamentDecks.any().tourneyId.eq(it)
                }.toTypedArray()
            )
        }

        return predicate
    }

    fun randomDeckId(): String {
        val deckOffset = (0..(deckCount ?: 800000)).random()
        val deckQ = QDeck.deck
        return query
            .select(deckQ.keyforgeId)
            .from(deckQ)
            .where(BooleanBuilder().and(deckQ.id.gt(deckOffset)))
            .limit(1)
            .orderBy(deckQ.id.asc())
            .fetchFirst()
    }

    fun findDeckSearchResultWithCards(keyforgeId: String): DeckSearchResult {
        val deck = deckRepo.findByKeyforgeId(keyforgeId) ?: throw BadRequestException("No deck with id $keyforgeId")
        return deck.toDeckSearchResult(
            cardCache.deckToHouseAndCards(deck),
            cardCache.cardsForDeck(deck),
            stats = statsService.findCurrentStats(),
            token = cardCache.tokenForDeck(deck),
        )
    }

    fun findDeckWithSynergies(keyforgeId: String): DeckWithSynergyInfo? {
        if (keyforgeId == "simple") {
            // quiet down the annoying constant errors
//            log.warn("Still receiving dumb simple deck requests.")
            return null
        }
        if (keyforgeId.length != 36) {
            throw BadRequestException("Request for deck with synergies with bad id: $keyforgeId")
        }
        val deck = deckRepo.findByKeyforgeId(keyforgeId) ?: return null
        return deckToDeckWithSynergies(deck)
    }

    fun findDeckSimple(keyforgeId: String): SimpleDeckSearchResult {
        val deck = deckRepo.findByKeyforgeId(keyforgeId) ?: throw BadRequestException("No deck with id $keyforgeId")
        return deck.toDeckSearchResult().toSimpleResult()
    }

    fun deckToDeckWithSynergies(deck: Deck): DeckWithSynergyInfo {
        val user = currentUserService.loggedInUser()
        val stats = statsService.findCurrentStats()
        val cardsAndToken = cardCache.cardsAndTokenFutureProof(deck, user)
        val cards = cardsAndToken.cards
        val synergies = DeckSynergyService.fromDeckWithCards(deck, cards, cardsAndToken.token)

        val searchResult = deck.toDeckSearchResult(
            cardCache.deckToHouseAndCards(deck),
            cards,
            stats = stats,
            synergies = synergies,
            includeDetails = true,
            token = cardsAndToken.token,
        )

        return DeckWithSynergyInfo(
            deck = if (user?.realPatreonTier()?.levelAtLeast(PatreonRewardsTier.SUPPORT_SOPHISTICATION) == true) {
                ownedDeckService.addOwners(searchResult)
            } else {
                searchResult
            },
            synergyPercentile = stats?.synergyStats?.percentileForValue?.get(synergies.synergyRating) ?: -1.0,
            antisynergyPercentile = stats?.antisynergyStats?.percentileForValue?.get(synergies.antisynergyRating)
                ?: -1.0,
        )
    }

    fun saleInfoForDeck(
        keyforgeId: String,
        offsetMinutes: Int,
        deckParam: Deck? = null,
        userParam: KeyUser? = null
    ): List<DeckSaleInfo> {
        val deck = deckParam ?: deckRepo.findByKeyforgeId(keyforgeId) ?: return listOf()
        val currentUser = userParam ?: currentUserService.loggedInUser()

        return deck.auctions.filter { it.status != DeckListingStatus.COMPLETE }
            .map { DeckSaleInfo.fromDeckListing(offsetMinutes, it, currentUser) }
            .sortedByDescending { it.dateListed }
    }

    fun findDecksByName(name: String): List<DeckSearchResult> {
        val deckQ = QDeckSasValuesSearchable.deckSasValuesSearchable
        val predicate = BooleanBuilder()
        val trimmed = name
            .lowercase()
            .trim()
        val tokenized = trimmed
            .split("\\W+".toRegex())
            .filter { it.length > 2 }

        val toUse = tokenized.ifEmpty { listOf(trimmed) }
        toUse.forEach { predicate.and(deckQ.name.likeIgnoreCase("%$it%")) }

        return query.selectFrom(deckQ)
            .innerJoin(deckQ.deck).fetchJoin()
            .where(predicate)
            .orderBy(deckQ.sasRating.desc())
            .limit(5)
            .fetch()
            .map {
                val deck = it.deck
                val cards = cardCache.cardsForDeck(deck)
                val token = cardCache.tokenForDeck(deck)
                deck.toDeckSearchResult(
                    housesAndCards = deck.houses.map { house -> HouseAndCards(house, listOf()) },
                    stats = statsService.findCurrentStats(),
                    synergies = DeckSynergyService.fromDeckWithCards(deck, cards, token),
                    token = token,
                )
            }
    }

    private fun canAccessTag(tagId: Long, userId: UUID?) {
        val canAccess = if (userId == null) {
            tagRepo.existsByIdAndPublicityTypeNot(tagId, PublicityType.PRIVATE)
        } else {
            tagRepo.existsByIdAndNotPrivateOrCreatorId(tagId, userId)
        }
        if (!canAccess) {
            throw BadRequestException("You do not have permission to view decks with this tag.")
        }
    }

}

data class UserHolder(
    private val id: UUID? = null,
    private val currentUserService: CurrentUserService,
    private val userService: KeyUserService
) {
    val user: KeyUser? by lazy {
        if (id == null) currentUserService.loggedInUser() else userService.findByIdOrNull(id)
    }
}
