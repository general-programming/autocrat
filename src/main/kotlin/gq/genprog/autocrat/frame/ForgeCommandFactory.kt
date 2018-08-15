package gq.genprog.autocrat.frame

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.frame.command.FrameForgeCommand
import io.github.hedgehog1029.frame.dispatcher.mapping.ICommandFactory
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import net.minecraft.command.CommandHandler
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.server.permission.DefaultPermissionLevel
import net.minecraftforge.server.permission.PermissionAPI

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeCommandFactory: ICommandFactory {
    override fun registerCommand(pipe: IPipeline) {
        if (AutocratConfig.general.disabledCommands.contains(pipe.primaryAlias)) {
            return
        }

        val handler = FMLCommonHandler.instance().minecraftServerInstance.commandManager as CommandHandler

        handler.registerCommand(FrameForgeCommand(pipe))

        if (!pipe.permission.isBlank()) {
            PermissionAPI.registerNode(pipe.permission, DefaultPermissionLevel.OP, "Use command ${pipe.primaryAlias}")
        }
    }
}