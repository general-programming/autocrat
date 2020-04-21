package gq.genprog.autocrat.frame.bindings

import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.dispatcher.arguments.ICommandArguments
import io.github.hedgehog1029.frame.dispatcher.provider.Provider
import io.github.hedgehog1029.frame.module.wrappers.ParameterWrapper
import io.github.hedgehog1029.frame.util.Namespace
import net.minecraft.command.CommandSource
import net.minecraft.command.ICommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.server.MinecraftServer

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SenderProvider<T : ICommandSource>: Provider<T> {
    override fun getSuggestions(index: Int, partial: String, namespace: Namespace): MutableList<String> {
        return mutableListOf()
    }

    override fun provide(args: ICommandArguments, param: ParameterWrapper): T? {
        if (param.isAnnotationPresent(Sender::class.java)) {
            return args.namespace.get("sender") as T
        }

        val server = args.namespace.get("server") as MinecraftServer

        val name = args.next()
        return server.playerList.getPlayerByUsername(name) as T?
    }

    override fun argsWanted(param: ParameterWrapper): Int {
        return if (param.isAnnotationPresent(Sender::class.java)) {
            0
        } else {
            1
        }
    }
}

class PlayerProvider: Provider<ServerPlayerEntity> {
    override fun provide(args: ICommandArguments, param: ParameterWrapper): ServerPlayerEntity? {
        if (param.isAnnotationPresent(Sender::class.java)) {
            val source = args.namespace.get("source") as CommandSource

            return source.asPlayer()
        }

        val server = args.namespace.get("server") as MinecraftServer

        val name = args.next()
        return server.playerList.getPlayerByUsername(name)
    }

    override fun getSuggestions(index: Int, partial: String, namespace: Namespace): MutableList<String> {
        val server = namespace.get("server") as MinecraftServer


        return server.playerList.players.map { it.gameProfile.name }.toMutableList()
    }

    override fun argsWanted(param: ParameterWrapper): Int {
        return if (param.isAnnotationPresent(Sender::class.java)) {
            0
        } else {
            1
        }
    }
}
