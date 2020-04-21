package gq.genprog.autocrat.frame

import com.mojang.brigadier.CommandDispatcher
import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.frame.command.FrameForgeCommand
import io.github.hedgehog1029.frame.dispatcher.mapping.ICommandFactory
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import net.minecraft.command.CommandSource
import net.minecraftforge.server.permission.DefaultPermissionLevel
import net.minecraftforge.server.permission.PermissionAPI

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeCommandFactory: ICommandFactory {
    lateinit var dispatcher: CommandDispatcher<CommandSource>

    override fun registerCommand(pipe: IPipeline) {
        if (AutocratConfig.get<List<String>>("general.disabledCommands").contains(pipe.primaryAlias)) {
            return
        }

        val command = FrameForgeCommand(pipe)
        val node = dispatcher.register(command.createNode())

        command.yieldAliases(node) {
            dispatcher.register(it)
        }

        if (!pipe.permission.isBlank()) {
            PermissionAPI.registerNode(pipe.permission, DefaultPermissionLevel.OP, "Use command ${pipe.primaryAlias}")
        }
    }
}