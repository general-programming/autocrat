package gq.genprog.autocrat.frame

import gq.genprog.autocrat.frame.command.FrameForgeCommand
import io.github.hedgehog1029.frame.dispatcher.mapping.ICommandFactory
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import net.minecraft.command.CommandHandler
import net.minecraftforge.fml.server.FMLServerHandler

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeCommandFactory: ICommandFactory {
    override fun registerCommand(pipe: IPipeline) {
        val handler = FMLServerHandler.instance().server.commandManager as CommandHandler

        handler.registerCommand(FrameForgeCommand(pipe))
    }
}