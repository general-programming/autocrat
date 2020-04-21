package gq.genprog.autocrat.integration

import net.minecraftforge.fml.ModList

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
object ConflictChecker {
    fun isAnyLoaded(vararg modids: String): Boolean {
        val list = ModList.get()

        return modids.any { list.isLoaded(it) }
    }

    fun isSleepVoteLoaded(): Boolean {
        return isAnyLoaded("quark", "morpheus")
    }
}