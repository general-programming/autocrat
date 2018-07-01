package gq.genprog.autocrat.modules

import gq.genprog.autocrat.modules.data.MiscStorage
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.GameType
import net.minecraftforge.items.CapabilityItemHandler

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class AdminModule {
    @Command(aliases = ["mod"], description = "Enter mod mode.")
    fun enterModMode(@Sender sender: EntityPlayerMP) {
        val data = MiscStorage.get(sender.world)

        if (data.modMode.isPlayerActive(sender)) {
            sender.controller().err("You're already in mod-mode!")

        }

        if (sender.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            val itemHandler = sender.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)!!
            val tag = data.modMode.serializeInventory(itemHandler)

            val adminTag = data.modMode.storePlayerTag(sender, tag)
            sender.inventory.clear()
            if (adminTag != null) {
                data.modMode.deserializeInto(adminTag, itemHandler)
            }

            sender.setGameType(GameType.CREATIVE)
            sender.controller().success("Entered mod mode. Use /done to quit.")
            data.markDirty()
        }
    }

    @Command(aliases = ["done"], description = "Exit mod mode.")
    fun exitModMode(@Sender sender: EntityPlayerMP) {
        val data = MiscStorage.get(sender.world)

        if (!data.modMode.isPlayerActive(sender)) {
            sender.controller().err("You're not currently in mod-mode!")
        }

        if (sender.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            val itemHandler = sender.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)!!
            val tag = data.modMode.serializeInventory(itemHandler)

            val playerTag = data.modMode.storeAdminTag(sender, tag)
            sender.inventory.clear()
            if (playerTag != null) {
                data.modMode.deserializeInto(playerTag, itemHandler)
            }

            val pos = data.modMode.lastLocation.remove(sender.uniqueID)
            if (pos != null) {
                sender.setPositionAndUpdate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            }

            sender.setGameType(GameType.SURVIVAL)
            sender.controller().success("Exited mod mode.")
            data.markDirty()
        }
    }
}