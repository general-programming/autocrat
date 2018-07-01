package gq.genprog.autocrat.modules

import gq.genprog.autocrat.integration.BackupsHook
import gq.genprog.autocrat.integration.BackupsHookImpl
import gq.genprog.autocrat.integration.sel.MutableSelection
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.hooks.Hook
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class BackupsModule: EventListener {
    @Hook("backups", target = BackupsHookImpl::class)
    val backupsHook: BackupsHook? = null

    val selections: HashMap<UUID, MutableSelection> = hashMapOf()
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
                val success = backupsHook.restoreSelection(sender.world, sel, backup.name)

                if (success)
                    sender.controller().chat("Successfully restored region.")
                else
                    sender.controller().chat("There was a problem restoring that backup!")
            } else {
                sender.controller().chat("Cancelled restore.")
            }
        }
    }
}