package gq.genprog.autocrat.modules

import gq.genprog.autocrat.randomFrom
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
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

    @SubscribeEvent fun onNameFormat(event: PlayerEvent.NameFormat) {
        val prefix = randomFrom(validColors).toString()
        val suffix = TextFormatting.RESET.toString()

        event.displayname = prefix + event.username + suffix
    }
}