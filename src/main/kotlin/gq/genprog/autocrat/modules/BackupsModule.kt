package gq.genprog.autocrat.modules

import gq.genprog.autocrat.integration.BackupsHook
import gq.genprog.autocrat.integration.BackupsHookImpl
import gq.genprog.autocrat.integration.sel.MutableSelection
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Group
import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.hooks.Hook
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Group("bk", "backups")
class BackupsModule: EventListener {
    @Hook("backups", target = BackupsHookImpl::class)
    val backupsHook: BackupsHook? = null

    val selections: HashMap<UUID, MutableSelection> = hashMapOf()
    val lastRestored: HashMap<UUID, UUID> = hashMapOf()

    fun getSelection(player: EntityPlayer): MutableSelection {
        if (!selections.containsKey(player.uniqueID)) {
            selections[player.uniqueID] = MutableSelection()
        }

        return selections[player.uniqueID]!!
    }

    @SubscribeEvent
    fun onRightClick(event: PlayerInteractEvent.RightClickBlock) {
        if (event.itemStack.item == Items.GOLDEN_AXE) {
            val selection = this.getSelection(event.entityPlayer)
            val pos = event.pos

            selection.second = pos

            event.controller().chat("Set second position to (${pos.x}, ${pos.y}, ${pos.z}).")
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onLeftClick(event: PlayerInteractEvent.LeftClickBlock) {
        if (event.itemStack.item == Items.GOLDEN_AXE) {
            val selection = this.getSelection(event.entityPlayer)
            val pos = event.pos

            selection.first = pos

            event.controller().chat("Set first position to (${pos.x}, ${pos.y}, ${pos.z}).")
            event.isCanceled = true
        }
    }

    @Command(aliases = ["restore"], description = "Restore a backup within a selection.")
    fun restoreBackup(@Sender sender: EntityPlayerMP, backup: BackupsHook.Backup) {
        if (!backupsHook!!.isLoaded()) {
            sender.controller().err("Backups module isn't enabled!")
            return
        }

        val sel = selections[sender.uniqueID]
        if (sel == null || !sel.isComplete()) {
            sender.controller().err("You need to make a selection first!")
            return
        }

        val size = sel.getVolume()
        sender.controller().chat {
            color(TextFormatting.YELLOW)
            append("This operation will modify $size blocks.")
            last("Are you sure you want to do this? Use /yes or /no to decide.")
        }

        ChoicesModule.awaitChoice(sender.uniqueID) { yes ->
            if (yes) {
                val result = backupsHook.restoreSelection(sender.world, sel, backup.name)

                if (result.status.wasSuccessful())
                    sender.controller().chat("Successfully restored region; ${result.affected} blocks changed.")
                else
                    sender.controller().chat {
                        color(TextFormatting.RED)
                        append("There was a problem restoring that backup!")
                        last(result.status.message)
                    }
            } else {
                sender.controller().chat("Cancelled restore.")
            }
        }
    }

    @Command(aliases = ["restore_inv"], description = "Restore a player's inventory from backup.")
    fun restorePlayerInv(@Sender sender: EntityPlayerMP, target: EntityPlayerMP, backup: BackupsHook.Backup) {
        if (!backupsHook!!.isLoaded()) {
            sender.controller().err("Backups module isn't enabled!")
            return
        }

        sender.controller().chat {
            color(TextFormatting.YELLOW)
            append("This operation will overwrite the current player data of ${target.name}.")
            append("A backup will be taken, and may be restored with /bk undo.")
            last("Are you sure you want to do this? Use /yes or /no to decide.")
        }

        ChoicesModule.awaitChoice(sender.uniqueID) { yes ->
            if (yes) {
                this.backupInventory(target)
                lastRestored[sender.uniqueID] = target.uniqueID

                val result = backupsHook.restorePlayerData(sender.world, target, backup.name)

                if (result.status.wasSuccessful()) {
                    sender.controller().success("Successfully restored ${target.name}'s player data from backup.")
                } else sender.controller().err(result.status.message)
            } else sender.controller().chat("Cancelled operation.")
        }
    }

    @Command(aliases = ["undo"], description = "Undo a player data restore.")
    fun undoPlayerRestore(@Sender sender: EntityPlayerMP) {
        val last = lastRestored[sender.uniqueID]
        if (last == null) {
            sender.controller().err("You haven't restored anyone's player data recently.")
            return
        }

        val player = sender.server!!.playerList.getPlayerByUUID(last)
        val success = this.restoreInventory(player)

        if (success) {
            sender.controller().success("Rolled back last operation.")
        } else {
            sender.controller().err("Could not roll back last operation.")
        }
    }

    fun backupInventory(player: EntityPlayerMP) {
        val world = player.serverWorld
        val flag = world.disableLevelSaving
        world.disableLevelSaving = true

        val loadFile = File(world.provider.saveFolder, "playerdata")
        val playerFile = File(loadFile, "${player.cachedUniqueIdString}.dat")
        val target = File(loadFile, "${player.cachedUniqueIdString}.dat.backup")

        playerFile.copyTo(target)

        world.disableLevelSaving = flag
    }

    fun restoreInventory(player: EntityPlayerMP): Boolean {
        val world = player.serverWorld
        val loadFile = File(world.provider.saveFolder, "playerdata")
        val backupFile = File(loadFile, "${player.cachedUniqueIdString}.dat.backup")

        if (!backupFile.exists() || !backupFile.isFile) return false

        val nbt = CompressedStreamTools.readCompressed(FileInputStream(backupFile))
        player.readFromNBT(nbt)

        return true
    }
}