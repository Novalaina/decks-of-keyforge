package coraythan.keyswap.config

object SchedulingConfig {
    const val updateUserStatsInitialDelay = "PT10S"
    const val countDecksIntialDelay = "PT30S"
    const val rateDecksInitialDelay = "PT45S"
    const val importNewDecksInitialDelay = "PT1M"
    const val purchasesInitialDelay = "PT1M"
    const val postProcessDecksDelay = "PT3S"
    const val unexpiredDecksInitialDelay = "PT5M"
    const val expireOffersInitialDelay = "PT6M"
    const val publishNewSasInitialDelay = "PT10M"
    const val newDeckStatsInitialDelay = "PT11M"
    const val updatePatronsInitialDelay = "PT30M"
    const val updateAllianceDecksInitialDelay = "PT45M"
    const val removeManualPatronsInitialDelay = "PT1H"
    const val correctCountsInitialDelay = "PT6H"
}
