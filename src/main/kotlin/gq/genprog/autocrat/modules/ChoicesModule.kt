package gq.genprog.autocrat.modules

import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TextFormatting
import java.util.*
import kotlin.collections.HashMap

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ChoicesModule {
    companion object {
        val activeChoices: HashMap<UUID, (choice: Boolean) -> Unit> = HashMap()

        fun awaitChoice(uuid: UUID, cb: (choice: Boolean) -> Unit) {
            activeChoices[uuid] = cb
        }

        fun resolve(uuid: UUID, result: Boolean) {
            activeChoices[uuid]?.invoke(result)

            activeChoices.remove(uuid)
        }
    }

    @Command(aliases = ["yes", "confirm"], description = "Confirm a choice.") fun confirm(@Sender sender: ServerPlayerEntity) {
        if (activeChoices.containsKey(sender.uniqueID)) {
            resolve(sender.uniqueID, true)
        } else {
            sender.controller().chat("You don't have a choice to make!", TextFormatting.RED)
        }
    }

    @Command(aliases = ["no", "cancel"], description = "Cancel a choice.") fun cancel(@Sender sender: ServerPlayerEntity) {
        if (activeChoices.containsKey(sender.uniqueID)) {
            resolve(sender.uniqueID, false)
        } else {
            sender.controller().chat("You don't have a choice to make!", TextFormatting.RED)
        }
    }
}