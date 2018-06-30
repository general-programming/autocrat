package gq.genprog.autocrat.frame.bindings

import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.dispatcher.arguments.ICommandArguments
import io.github.hedgehog1029.frame.dispatcher.provider.Provider
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.server.FMLServerHandler
import java.lang.reflect.Parameter

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SenderProvider<T : ICommandSender>: Provider<T> {
    override fun getSuggestions(partial: String): MutableList<String> {
        return FMLServerHandler.instance().server.playerList.players.map { it.name }.filter {
            it.startsWith(partial)
        }.toMutableList()
    }

    override fun provide(args: ICommandArguments, param: Parameter): T? {
        if (param.isAnnotationPresent(Sender::class.java)) {
            return args.namespace.get("sender") as T
        }

        val server = args.namespace.get("server") as MinecraftServer

        val name = args.next()
        return server.playerList.getPlayerByUsername(name) as T?
    }
}
