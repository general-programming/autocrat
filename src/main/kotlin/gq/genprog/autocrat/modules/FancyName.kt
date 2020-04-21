package gq.genprog.autocrat.modules

import gq.genprog.autocrat.modules.data.MiscStorage
import gq.genprog.autocrat.randomFrom
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Optional
import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.annotation.Text
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class FancyName: EventListener {
    val validColors = arrayOf(
            TextFormatting.GOLD,
            TextFormatting.RED,
            TextFormatting.BLUE,
            TextFormatting.LIGHT_PURPLE,
            TextFormatting.DARK_PURPLE,
            TextFormatting.AQUA,
            TextFormatting.DARK_GREEN
    )

    @SubscribeEvent
    fun onNameFormat(event: PlayerEvent.NameFormat) {
        val storage = MiscStorage.get(event.player.world as ServerWorld)
        val prefix = randomFrom(validColors).toString()
        val suffix = TextFormatting.RESET.toString()

        if (storage.hasNick(event.player)) {
            event.displayname = prefix + storage.nicknames[event.player.uniqueID]

            return
        }

        event.displayname = prefix + event.username + suffix
    }

    @Command(aliases = ["nickname", "nick"], description = "Change your nickname.")
    fun changeNick(@Sender sender: ServerPlayerEntity, @Optional @Text nick: String?) {
        val storage = MiscStorage.get(sender.serverWorld)

        if (nick.isNullOrBlank()) {
            storage.nicknames.remove(sender.uniqueID)

            sender.controller().chat("Cleared your nickname.", TextFormatting.GREEN)
        } else {
            val formatted = nick.replace('&', '\u00A7') + TextFormatting.RESET.toString()

            storage.nicknames[sender.uniqueID] = formatted
            sender.controller().chat {
                color(TextFormatting.GREEN)
                last("Set your nickname to '")
                last(formatted)
                last("'.")
            }
        }

        storage.markDirty()
//        sender.refreshDisplayName()
    }
}