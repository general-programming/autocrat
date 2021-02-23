package gq.genprog.autocrat.modules

import gq.genprog.autocrat.modules.data.MiscStorage
import gq.genprog.autocrat.randomFrom
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Optional
import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.annotation.Text
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.event.ServerChatEvent
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

    val colorCache = hashMapOf<UUID, TextFormatting>()

    fun getUserColor(player: ServerPlayerEntity): TextFormatting {
        return colorCache.getOrPut(player.uniqueID) { randomFrom(validColors) }
    }

    @SubscribeEvent
    fun onChat(event: ServerChatEvent) {
        val storage = MiscStorage.get(event.player.server!!)
        val pickedColor = getUserColor(event.player)

        // awful cast hacks
        val component = event.component as TranslationTextComponent
        val oldDisplayName = component.formatArgs[0] as ITextComponent

        val displayName = if (storage.hasNick(event.player)) {
            StringTextComponent(storage.nicknames[event.player.uniqueID]!!).mergeStyle(pickedColor)
        } else {
            oldDisplayName.deepCopy().mergeStyle(pickedColor)
        }

        component.formatArgs[0] = displayName
    }

    @Command(aliases = ["nickname", "nick"], description = "Change your nickname.")
    fun changeNick(@Sender sender: ServerPlayerEntity, @Optional @Text nick: String?) {
        val storage = MiscStorage.get(sender.server)

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
        sender.serverWorld.savedData.save()
    }
}
