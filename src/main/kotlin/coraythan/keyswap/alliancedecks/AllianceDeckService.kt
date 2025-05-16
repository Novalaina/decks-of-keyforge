package coraythan.keyswap.alliancedecks

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import coraythan.keyswap.House
import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.config.BadRequestException
import coraythan.keyswap.config.UnauthorizedException
import coraythan.keyswap.deckimports.DeckBuildingData
import coraythan.keyswap.deckimports.DeckCreationService
import coraythan.keyswap.deckimports.TheoryCard
import coraythan.keyswap.decks.DeckRepo
import coraythan.keyswap.decks.SortDirection
import coraythan.keyswap.decks.UserHolder
import coraythan.keyswap.decks.models.DeckCount
import coraythan.keyswap.decks.models.DeckSearchResult
import coraythan.keyswap.decks.models.DeckWithSynergyInfo
import coraythan.keyswap.decks.models.DecksPage
import coraythan.keyswap.expansions.Expansion
import coraythan.keyswap.patreon.PatreonRewardsTier
import coraythan.keyswap.sasupdate.SasVersionService
import coraythan.keyswap.stats.StatsService
import coraythan.keyswap.synergy.synergysystem.DeckSynergyService
import coraythan.keyswap.tokenize
import coraythan.keyswap.users.CurrentUserService
import coraythan.keyswap.users.KeyUser
import coraythan.keyswap.users.KeyUserService
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AllianceDeckService(
    private val cardCache: DokCardCacheService,
    private val userService: KeyUserService,
    private val currentUserService: CurrentUserService,
    private val statsService: StatsService,
    private val ownedAllianceDeckRepo: OwnedAllianceDeckRepo,
    private val allianceDeckRepo: AllianceDeckRepo,
    private val deckRepo: DeckRepo,
    private val deckCreationService: DeckCreationService,
    private val allianceHouseRepo: AllianceHouseRepo,
    private val sasVersionService: SasVersionService,
    private val entityManager: EntityManager,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val query = JPAQueryFactory(entityManager)
    private val matchFirstWord = "^\\S+".toRegex()
    private val matchLastWord = "\\S+$".toRegex()

    fun updateAllianceDeckValidity() {
        log.info("Update alliance deck validity")
        var continueUpdating = true
        var page = 0
        while (continueUpdating) {
            var updated = 0
            val decks = allianceDeckRepo.findAll(PageRequest.of(page, 100))
            if (decks.isEmpty) {
                continueUpdating = false
                log.info("Done updating alliance deck validity")
            } else {
                decks.forEach {
                    val valid = AllianceDeck.determineIfValid(it, cardCache.cardsForDeck(it))
                    if (it.validAlliance != valid) {
                        allianceDeckRepo.save(it.copy(validAlliance = valid))
                        updated++
                    }
                }
                log.info("Updating alliances validity on page $page updated $updated alliances.")
                page++
            }
        }
    }

    @Transactional
    fun saveAllianceDeck(toSave: AllianceDeckHouses): UUID {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)
        return saveAllianceDeckWithUser(toSave, user)
    }

    @Transactional
    fun saveAllianceDeckWithUser(toSave: AllianceDeckHouses, user: KeyUser): UUID {

        val deckOne = deckRepo.findByKeyforgeId(toSave.houseOneDeckId)
            ?: throw BadRequestException("No deck for ${toSave.houseOneDeckId}")
        val deckTwo = deckRepo.findByKeyforgeId(toSave.houseTwoDeckId)
            ?: throw BadRequestException("No deck for ${toSave.houseTwoDeckId}")
        val deckThree = deckRepo.findByKeyforgeId(toSave.houseThreeDeckId)
            ?: throw BadRequestException("No deck for ${toSave.houseThreeDeckId}")

        if (toSave.tokenName == null) {
            if (deckOne.tokenNumber != null || deckTwo.tokenNumber != null || deckThree.tokenNumber != null) {
                throw BadRequestException("No token provided when a deck includes a token.")
            }
        }

        val allianceName = combineDeckNames(
            listOf(
                Pair(toSave.houseOne, deckOne.name),
                Pair(toSave.houseTwo, deckTwo.name),
                Pair(toSave.houseThree, deckThree.name),
            )
        )

        val allianceDeckUniqueKey = AllianceDeck.uniqueHousesId(
            listOf(
                Pair(toSave.houseOne, toSave.houseOneDeckId),
                Pair(toSave.houseTwo, toSave.houseTwoDeckId),
                Pair(toSave.houseThree, toSave.houseThreeDeckId),
            ),
            toSave.tokenName,
            toSave.propheciesDeckId
        )

        var allianceDeck = allianceDeckRepo.findFirst1ByHousesUniqueId(allianceDeckUniqueKey).firstOrNull()
        log.info("Alliance deck houses decks id: $allianceDeckUniqueKey exists already? ${allianceDeck != null}")

        if (allianceDeck == null) {
            val deckHousePairs = listOf(
                Pair(deckOne, toSave.houseOne),
                Pair(deckTwo, toSave.houseTwo),
                Pair(deckThree, toSave.houseThree),
            )
                .sortedBy { it.second.ordinal }

            val expansion = deckOne.expansionEnum
            if (expansion == Expansion.MARTIAN_CIVIL_WAR) {
                if (deckTwo.expansionEnum != deckThree.expansionEnum) {
                    throw BadRequestException(
                        "When building a Martian Civil War deck the other two houses must be " +
                                "from the same expansion."
                    )
                }
            } else if (deckHousePairs.any { it.first.expansionEnum != expansion }) {
                throw BadRequestException("All decks in alliance must be same expansion.")
            }

            val allianceDeckInfo = DeckBuildingData(
                name = allianceName,
                cards = deckHousePairs.associate { deckHousePair ->
                    deckHousePair.second to cardCache.cardsForDeck(deckHousePair.first)
                        .filter { it.house == deckHousePair.second }
                        .map {
                            TheoryCard(
                                it.card.cardTitle,
                                enhanced = false,
                                bonusAember = it.bonusAember,
                                bonusCapture = it.bonusCapture,
                                bonusDamage = it.bonusDamage,
                                bonusDraw = it.bonusDraw,
                                bonusDiscard = it.bonusDiscard,
                                bonusHouses = it.bonusHouses,
                            )
                        }
                }.let { cardsByHouse ->
                    if (expansion == Expansion.PROPHETIC_VISIONS) {
                        val propheciesDeck = if (toSave.propheciesDeckId == toSave.houseOneDeckId) {
                            deckOne
                        } else if (toSave.propheciesDeckId == toSave.houseTwoDeckId) {
                            deckTwo
                        } else if (toSave.propheciesDeckId == toSave.houseThreeDeckId) {
                            deckThree
                        } else {
                            throw BadRequestException("No prophecies deck provided.")
                        }
                        val prophecies = cardCache.cardsForDeck(propheciesDeck)
                            .filter { it.house == House.Prophecy }
                        cardsByHouse.plus(Pair(House.Prophecy, prophecies.map { TheoryCard(it.card.cardTitle) }))
                    } else {
                        cardsByHouse
                    }
                },
                expansion = expansion,
                tokenTitle = toSave.tokenName,
                alliance = true,
            )

            val bonusIcons = allianceDeckInfo.bonusIcons
            val deck = deckCreationService.viewTheoreticalDeck(allianceDeckInfo)
            val cards = cardCache.cardsForDeck(deck)

            val tempAllianceDeck = AllianceDeck.fromDeck(deck, cards, user)
                .copy(
                    housesUniqueId = allianceDeckUniqueKey,
                    sasVersion = sasVersionService.findSasVersion()
                )
                .withBonusIcons(bonusIcons)

            allianceDeck = allianceDeckRepo.save(tempAllianceDeck)

            val allianceHouses = deckHousePairs.map {
                AllianceHouse(
                    house = it.second,
                    deckId = it.first.id,
                    allianceDeck = allianceDeck,
                    keyforgeId = it.first.keyforgeId,
                    name = it.first.name,
                )
            }

            allianceHouseRepo.saveAll(allianceHouses)
        }

        if (toSave.owned && !ownedAllianceDeckRepo.existsByDeckIdAndOwnerId(allianceDeck.id, user.id)) {
            ownedAllianceDeckRepo.save(
                OwnedAllianceDeck(
                    owner = user,
                    deck = allianceDeck,
                    teamId = user.teamId,
                )
            )
        }

        return allianceDeck.id
    }

    @Transactional
    fun addOwnedAllianceDeck(allianceDeckId: UUID) {
        val user = currentUserService.hasPatronLevelOrUnauthorized(PatreonRewardsTier.NOTICE_BARGAINS)

        ownedAllianceDeckRepo.save(
            OwnedAllianceDeck(
                owner = user,
                deck = allianceDeckRepo.findByIdOrNull(allianceDeckId)
                    ?: throw BadRequestException("No alliance deck for $allianceDeckId"),
                teamId = user.teamId,
            )
        )
    }

    @Transactional
    fun removeAllianceDeckOwnership(allianceDeckId: UUID) {
        val user = currentUserService.loggedInUserOrUnauthorized()
        ownedAllianceDeckRepo.deleteByDeckIdAndOwnerId(allianceDeckId, user.id)
    }

    fun findAllianceDeckWithSynergies(id: UUID): DeckWithSynergyInfo? {
        val deck = allianceDeckRepo.findByIdOrNull(id) ?: return null
        return allianceDeckToAllianceDeckWithSynergies(deck)
    }

    fun findAllianceDeckSearchResultWithCards(id: UUID): DeckSearchResult {
        val deck = allianceDeckRepo.findByIdOrNull(id) ?: throw BadRequestException("No alliance deck with id $id")
        val cards = cardCache.cardsForDeck(deck)
        val token = cardCache.tokenForDeck(deck)
        return deck.toDeckSearchResult(
            cardCache.deckToHouseAndCards(deck),
            cards,
            stats = statsService.findCurrentStats(),
            token = token,
            synergies = DeckSynergyService.fromDeckWithCards(deck, cards, token),
        )
    }

    fun countFilters(filters: AllianceDeckFilters): DeckCount {
        val userHolder = UserHolder(null, currentUserService, userService)
        val predicate = deckFilterPredicate(filters, userHolder)

        val deckQ = QAllianceDeck.allianceDeck
        val count = query
            .select(deckQ.id)
            .from(deckQ)
            .where(predicate)
            .limit(10000)
            .fetch()
            .count()
            .toLong()

        return DeckCount(
            pages = (count + filters.pageSize - 1) / filters.pageSize,
            count = count
        )
    }

    fun findOwned(): List<UUID> {
        val currentUser = currentUserService.loggedInUser() ?: return listOf()
        val ownedDeckQ = QOwnedAllianceDeck.ownedAllianceDeck
        return query
            .select(Projections.constructor(AllianceDeckIdDto::class.java, ownedDeckQ.deck.id))
            .from(ownedDeckQ)
            .where(ownedDeckQ.owner.id.eq(currentUser.id))
            .fetch()
            .map { it.deckId }
    }

    fun filterDecks(filters: AllianceDeckFilters, timezoneOffsetMinutes: Int): DecksPage {

        val userHolder = UserHolder(null, currentUserService, userService)
        val predicate = deckFilterPredicate(filters, userHolder)
        val deckQ = QAllianceDeck.allianceDeck

        val initialSort = when (filters.sort) {
            AllianceDeckSortOptions.ADDED_DATE -> if (filters.sortDirection == SortDirection.DESC) deckQ.createdDateTime.desc() else deckQ.createdDateTime.asc()
            AllianceDeckSortOptions.SAS_RATING -> if (filters.sortDirection == SortDirection.DESC) deckQ.sasRating.desc() else deckQ.sasRating.asc()
            AllianceDeckSortOptions.NAME -> if (filters.sortDirection == SortDirection.DESC) deckQ.name.asc() else deckQ.name.desc()
        }

        val deckResults = query.selectFrom(deckQ)
            .where(predicate)
            .limit(filters.pageSize)
            .offset(filters.page * filters.pageSize)
            .orderBy(initialSort, deckQ.id.asc())
            .fetch()

        val decks = deckResults.mapNotNull {
            val cardsAndToken = cardCache.cardsAndTokenFutureProof(it, userHolder.user)
            val cards = cardsAndToken.cards
            val token = cardsAndToken.token

            var searchResult = it.toDeckSearchResult(
                cardCache.deckToHouseAndCards(it),
                cards,
                stats = statsService.findCurrentStats(),
                synergies = DeckSynergyService.fromDeckWithCards(it, cards, token),
                token = token,
            )

            if (filters.teamDecks) {
                val user = userHolder.user
                val teamId = user?.teamId ?: throw BadRequestException("You aren't on a team.")
                if (user.realPatreonTier() == null) throw BadRequestException("You do not have permission to view team decks.")
                val owners = ownedAllianceDeckRepo.findByDeckIdAndTeamId(it.id, teamId).map { userDeck ->
                    userDeck.owner.username
                }
                if (owners.isNotEmpty()) {
                    searchResult = searchResult.copy(owners = owners)
                }
            }
            if (filters.owners.isNotEmpty()) {
                val visibleUsers =
                    filters.owners.filter { owner ->
                        val foundUser = userService.findUserByUsername(owner)
                        when {
                            foundUser == null -> false
                            foundUser.teamId != null && foundUser.teamId == userHolder.user?.teamId -> true
                            else -> foundUser.allowUsersToSeeDeckOwnership
                        }
                    }

                val owners = ownedAllianceDeckRepo.findByDeckIdAndOwnedByIn(it.id, visibleUsers).map { userDeck ->
                    userDeck.owner.username
                }
                if (owners.isNotEmpty()) {
                    searchResult = searchResult.copy(owners = owners)
                }
            }

            searchResult

        }

        return DecksPage(
            decks,
            filters.page
        )
    }

    private fun deckFilterPredicate(
        filters: AllianceDeckFilters,
        userHolder: UserHolder
    ): BooleanBuilder {
        val deckQ = QAllianceDeck.allianceDeck
        val predicate = BooleanBuilder()

        if (filters.expansions.isNotEmpty()) {
            predicate.andAnyOf(*filters.expansions.map { deckQ.expansion.eq(it) }.toTypedArray())
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
        if (excludeHouses.isNotEmpty()) {
            excludeHouses.forEach { predicate.and(deckQ.houseNamesString.notLike("%$it%")) }
        }

        if (filters.title.isNotBlank()) {
            filters.title.tokenize().forEach { predicate.and(deckQ.name.likeIgnoreCase("%$it%")) }
        }

        if (filters.validOnly) {
            predicate.and(deckQ.validAlliance.isTrue)
        }
        if (filters.invalidOnly) {
            predicate.and(deckQ.validAlliance.isFalse)
        }

        if (filters.teamDecks) {
            val teamId = userHolder.user?.teamId
            if (teamId != null) {
                predicate.and(deckQ.ownedDecks.any().teamId.eq(teamId))
            }
        }

        if (filters.owners.isNotEmpty()) {
            val allOwners = filters.owners.toSet()
                .filter { it.isNotBlank() }
                .mapNotNull { userService.findIdAndDeckVisibilityByUsername(it) }

            val visibleUsers =
                allOwners.filter {
                    it.allowUsersToSeeDeckOwnership
                }

            if (allOwners.size == 1 && allOwners[0].id == userHolder.user?.id) {
                // it's me
                predicate.and(deckQ.ownedDecks.any().owner.id.eq(userHolder.user?.id))
            } else if (filters.teamDecks) {
                // team decks
                val myTeamId = userHolder.user?.teamId
                if (myTeamId == null || allOwners.any { it.teamId != myTeamId }) {
                    throw UnauthorizedException("You must be logged in and share teams with the searched user to see their decks.")
                }
                predicate.and(deckQ.ownedDecks.any().owner.id.`in`(allOwners.map { it.id }))

            } else {
                // just find the publicly owned ones
                predicate.and(deckQ.ownedDecks.any().owner.id.`in`(visibleUsers.map { it.id }))
            }
        }

        return predicate
    }

    private fun allianceDeckToAllianceDeckWithSynergies(deck: AllianceDeck): DeckWithSynergyInfo {
        val user = currentUserService.loggedInUser()
        val stats = statsService.findCurrentStats()
        val cardsAndToken = cardCache.cardsAndTokenFutureProof(deck, user)
        val cards = cardsAndToken.cards
        val token = cardsAndToken.token
        val synergies = DeckSynergyService.fromDeckWithCards(deck, cards, token)

        val searchResult = deck.toDeckSearchResult(
            cardCache.deckToHouseAndCards(deck),
            cards,
            stats = stats,
            synergies = synergies,
            includeDetails = true,
            token,
        )

        return DeckWithSynergyInfo(
            deck = searchResult,
            synergyPercentile = stats?.synergyStats?.percentileForValue?.get(synergies.synergyRating) ?: -1.0,
            antisynergyPercentile = stats?.antisynergyStats?.percentileForValue?.get(synergies.antisynergyRating)
                ?: -1.0,
        )
    }

    private fun combineDeckNames(names: List<Pair<House, String>>): String {
        val namesSorted = names.sortedBy { it.first }
        val firstWord = matchFirstWord.find(namesSorted[0].second)?.value ?: "The Confusing"
        val lastWord = matchLastWord.find(namesSorted[2].second)?.value ?: " of Illusions"
        val middleName = namesSorted[1].second
        val middleSplit = middleName.split(" ")
        val possibleName = if (middleSplit.size < 3) {
            "$firstWord ${middleSplit.random()} $lastWord"
        } else {
            middleName
                .replace(matchFirstWord, firstWord)
                .replace(matchLastWord, lastWord)
        }

        if (possibleName.length < 37) return possibleName
        val shortMiddle = middleSplit.random()
        return "$firstWord $shortMiddle $lastWord"
    }
}

data class AllianceDeckIdDto(
    val deckId: UUID
)
