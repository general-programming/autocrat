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
        val data = MiscStorage.get(sender.serverWorld)
        val modMode = data.fetchModModeData(sender)

        if (modMode.active) {
            sender.controller().err("You're already in mod-mode!")
            return
        }

        modMode.Handlers(sender).transitionToAdmin()

        sender.setGameType(GameType.CREATIVE)
        modlog.info("{} ({}) entered modmode at ({})", sender.name.unformattedComponentText,
                sender.uniqueID, sender.position.joinToString())
        data.markDirty()
    }

    @Command(aliases = ["done"], description = "Exit mod mode.", permission = "autocrat.mod")
    fun exitModMode(@Sender sender: ServerPlayerEntity) {
        val data = MiscStorage.get(sender.serverWorld)
        val modMode = data.fetchModModeData(sender)

        if (!modMode.active) {
            sender.controller().err("You're not currently in mod-mode!")
            return
        }

        modMode.Handlers(sender).transitionToAdmin()

        sender.setGameType(GameType.SURVIVAL)
        sender.controller().success("Exited mod mode.")
        modlog.info("{} exited modmode.", sender.name.unformattedComponentText)
        data.markDirty()
    }

    @SubscribeEvent fun onCommand(ev: CommandEvent) {
        val ctx = ev.parseResults.context
        val sender = ctx.source
        val player = sender.entity
        val data = MiscStorage.get(sender.world)
        val fullCommand = ev.parseResults.reader.string

        if (player !is ServerPlayerEntity) return
        if (!data.fetchModModeData(player).active) return

        modlog.info("{} executed command {}", player.name.unformattedComponentText, fullCommand)
    }
}