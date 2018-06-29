package gq.genprog.autocrat.config

import gq.genprog.autocrat.MOD_ID
import net.minecraftforge.common.config.Config

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Config(modid = MOD_ID)
class AutocratConfig {
    class ClaimsModule {
        @Config.Comment("Maximum number of chunks a group can claim.")
        public val maxClaimedChunks: Int = 50
    }

    class SleepModule {
        @Config.Comment("Controls the percentage of sleeping players required to skip to day.")
        public val threshold: Int = 45
    }

    companion object {
        @JvmStatic public val claims = ClaimsModule()
        @JvmStatic public val sleepVote = SleepModule()
    }
}