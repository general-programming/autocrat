package gq.genprog.autocrat.integration

import gq.genprog.autocrat.integration.sel.MutableSelection
import net.minecraft.world.World

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
open class BackupsHook {
    open fun isLoaded(): Boolean {
        return false
    }

    open fun restoreSelection(world: World, selection: MutableSelection, backupName: String): Boolean {
        return false
    }

    open fun listBackups(world: World): List<String> {
        return emptyList()
    }

    open fun completeBackupString(partial: String): List<String> {
        return emptyList()
    }

    open class Backup(val name: String)
}