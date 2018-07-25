package gq.genprog.autocrat.integration

import net.minecraftforge.fml.common.Loader

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
object ConflictChecker {
    fun isAnyLoaded(vararg modids: String): Boolean {
        return modids.any { Loader.isModLoaded(it) }
    }

    fun isSleepVoteLoaded(): Boolean {
        return isAnyLoaded("quark", "morpheus")
    }
}