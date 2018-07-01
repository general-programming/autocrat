package gq.genprog.autocrat.integration

import gq.genprog.autocrat.integration.sel.MutableSelection
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
open class BackupsHook {
    open fun isLoaded(): Boolean {
        return false
    }

    open fun restoreSelection(world: World, selection: MutableSelection, backupName: String): HookResult {
        return HookResult(Status.NO_HOOK, 0)
    }

    open fun restorePlayerData(world: World, player: EntityPlayerMP, backupName: String): HookResult {
        return HookResult(Status.NO_HOOK, 0)
    }

    open fun listBackups(world: World): List<String> {
        return emptyList()
    }

    open fun completeBackupString(partial: String): List<String> {
        return emptyList()
    }

    open class Backup(val name: String)
    open class HookResult(val status: Status, val affected: Int)

    enum class Status(val message: String) {
        SUCCESS("Success"),
        UNKNOWN_BACKUP("Couldn't find that backup!"),
        INVALID_AREA("You need to select an area first!"),
        AREA_NOT_LOADED("The selected area isn't loaded."),
        AREA_TOO_BIG("That area is too large! Select a smaller area."),
        MISSING_DATA("Player file is missing!"),
        NO_HOOK("This module is inactive."),
        UNKNOWN("An unknown error occurred");

        fun wasSuccessful(): Boolean {
            return this == SUCCESS
        }
    }
}