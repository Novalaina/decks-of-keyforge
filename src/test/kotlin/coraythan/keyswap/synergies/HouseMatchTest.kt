package coraythan.keyswap.synergies

import coraythan.keyswap.House
import coraythan.keyswap.cards.CardType
import coraythan.keyswap.cards.dokcards.DokCardInDeck
import coraythan.keyswap.cards.extrainfo.ExtraCardInfo
import coraythan.keyswap.synergy.SynTraitHouse
import coraythan.keyswap.synergy.SynTraitValue
import coraythan.keyswap.synergy.SynergyTrait
import coraythan.keyswap.synergy.synergysystem.DeckSynergyService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slf4j.LoggerFactory

class HouseMatchTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    val pixieCards = listOf<DokCardInDeck>()
            .plus(
                    testCard(
                            name = "dust pixie",
                            house = House.Untamed,
                            extraCardInfo = ExtraCardInfo(
                                    expectedAmber = 2.0,
                                    expectedAmberMax = 4.0,
                                    synergies = listOf(
                                            SynTraitValue(SynergyTrait.replays, rating = 3, house = SynTraitHouse.house),
                                            SynTraitValue(SynergyTrait.replays, rating = 2, house = SynTraitHouse.outOfHouse)
                                    )
                            )
                    )
            )
            .plus(
                    testCard(
                            name = "Hysteria",
                            house = House.Dis,
                            extraCardInfo = ExtraCardInfo(
                                    traits = listOf(
                                            SynTraitValue(SynergyTrait.replays, rating = 3, house = SynTraitHouse.continuous)
                                    )
                            )
                    )
            )

    val housePipCards = listOf<DokCardInDeck>()
        .plus(
            testCard(
                name = "Crystal Hive",
                house = House.Mars,
                extraCardInfo = ExtraCardInfo(
                    expectedAmber = 0.0,
                    expectedAmberMax = 10.0,
                    synergies = listOf(
                        SynTraitValue(SynergyTrait.creatureCount, rating = 3, house = SynTraitHouse.house),
                    )
                )
            )
        )
        .plus(
            (0..5).map {
                testCard(
                    name = "Ace Jonavan",
                    house = House.Ekwidon,
                    bonusMars = true,
                    cardType = CardType.Creature,
                ).copy(bonusHouses = setOf(House.Mars))
            }
        )

    @Test
    fun testPixieHysteria() {
        val pixieResults = DeckSynergyService.fromDeckWithCards(boringDeck, pixieCards)
        assertEquals(2.8, pixieResults.expectedAmber, 0.001)
    }

    @Test
    fun testHousePips() {
        val housePipResults = DeckSynergyService.fromDeckWithCards(aemberSkiesDeck, housePipCards)
        assertEquals(5.8, housePipResults.expectedAmber, 0.001)
    }

}
