package coraythan.keyswap

import coraythan.keyswap.cards.TokenService
import coraythan.keyswap.cards.dokcards.DokCardCacheService
import coraythan.keyswap.deckimports.DeckImporterService
import coraythan.keyswap.synergy.FixSynergies
import coraythan.keyswap.users.search.UserSearchService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

var startupComplete = false

@Component
class RunOnStart(
    private val cardCache: DokCardCacheService,
    private val fixSynergies: FixSynergies,
    private val userSearchService: UserSearchService,
    private val tokenService: TokenService,
    private val deckImporterService: DeckImporterService,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun run(vararg args: String?) {

        deckImporterService.findMenagerieIdsToFix()

        tokenService.loadTokens()
        cardCache.loadCards()

        fixSynergies.fix()

        startupComplete = true

        log.info(
            "\n" + """
            .-./`) ,---------.         .--.      .--.    ,-----.    .-------.    .--.   .--.      .-''-.   ______     .---.  
            \ .-.')\          \        |  |_     |  |  .'  .-,  '.  |  _ _   \   |  | _/  /     .'_ _   \ |    _ `''. \   /  
            / `-' \ `--.  ,---'        | _( )_   |  | / ,-.|  \ _ \ | ( ' )  |   | (`' ) /     / ( ` )   '| _ | ) _  \|   |  
             `-'`"`    |   \           |(_ o _)  |  |;  \  '_ /  | :|(_ o _) /   |(_ ()_)     . (_ o _)  ||( ''_'  ) | \ /   
             .---.     :_ _:           | (_,_) \ |  ||  _`,/ \ _/  || (_,_).' __ | (_,_)   __ |  (_,_)___|| . (_) `. |  v    
             |   |     (_I_)           |  |/    \|  |: (  '\_/ \   ;|  |\ \  |  ||  |\ \  |  |'  \   .---.|(_    ._) ' _ _   
             |   |    (_(=)_)          |  '  /\  `  | \ `"/  \  ) / |  | \ `'   /|  | \ `'   / \  `-'    /|  (_.\.' / (_I_)  
             |   |     (_I_)           |    /  \    |  '. \_/``".'  |  |  \    / |  |  \    /   \       / |       .' (_(=)_) 
             '---'     '---'           `---'    `---`    '-----'    ''-'   `'-'  `--'   `'-'     `'-..-'  '-----'`    (_I_)  
        """.trimIndent() + "\n"
        )

        userSearchService.updateSearchResults()
        // allianceDeckService.updateAllianceDeckValidity()
//        deckListingService.fixBadDeckListings()
    }

}
