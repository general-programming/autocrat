package gq.genprog.autocrat.modules

import gq.genprog.autocrat.modules.data.MiscStorage
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameType
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.items.CapabilityItemHandler
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class AdminModule: EventListener {
    val modlog = LogManager.getFactory().getContext(AdminModule::class.qualifiedName!!,
            javaClass.classLoader, null, true,
            javaClass.classLoader.getResource("modlog.xml").toURI(), "modlog")
            .getLogger("autocrat")

    fun BlockPos.joinToString(seperator: CharSequence = ", "): String {
        return arrayOf(x, y, z).joinToString(seperator)
    }

    @Command(aliases = ["mod"], description = "Enter mod mode.", permission = "autocrat.mod")
    fun enterModMode(@Sender sender: ServerPlayerEntity) {
        val data = MiscStorage.get(sender.world)

        if (data.modMode.isPlayerActive(sender)) {
            sender.controller().err("You're already in mod-mode!")
            return
        }

        sender.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent { itemHandler ->
            val tag = data.modMode.serializeInventory(itemHandler)

            val adminTag = data.modMode.storePlayerTag(sender, tag)
            sender.inventory.clear()
            if (adminTag != null) {
                data.modMode.deserializeInto(adminTag, itemHandler)
            }

            sender.setGameType(GameType.CREATIVE)
            sender.controller().success("Entered mod mode. Use /done to quit.")
            modlog.info("{} ({}) entered modmode at ({})", sender.name.unformattedComponentText,
                    sender.uniqueID, sender.position.joinToString())
            data.markDirty()
        }
    }

    @Command(aliases = ["done"], description = "Exit mod mode.", permission = "autocrat.mod")
    fun exitModMode(@Sender sender: ServerPlayerEntity) {
        val data = MiscStorage.get(sender.world)

        if (!data.modMode.isPlayerActive(sender)) {
            sender.controller().err("You're not currently in mod-mode!")
            return
        }

        sender.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent { itemHandler ->
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
            modlog.info("{} exited modmode.", sender.name.unformattedComponentText)
            data.markDirty()
        }
    }

    @SubscribeEvent fun onCommand(ev: CommandEvent) {
        val ctx = ev.parseResults.context
        val sender = ctx.source
        val player = sender.entity
        val data = MiscStorage.get(sender.world)

        if (player !is ServerPlayerEntity) return
        if (!data.modMode.isPlayerActive(player)) return

        modlog.info("{} executed command /{} {}", player.name.unformattedComponentText,
                ctx.rootNode.name, ctx.arguments.keys.joinToString(" "))
    }
}