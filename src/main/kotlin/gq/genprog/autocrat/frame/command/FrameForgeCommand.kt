package gq.genprog.autocrat.frame.command

import io.github.hedgehog1029.frame.dispatcher.exception.DispatcherException
import io.github.hedgehog1029.frame.dispatcher.exception.UsageException
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import io.github.hedgehog1029.frame.util.Namespace
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class FrameForgeCommand(val pipeline: IPipeline): ICommand {
    override fun getUsage(sender: ICommandSender?): String {
        return "/${pipeline.primaryAlias} ${pipeline.usage}"
    }

    override fun getName(): String {
        return pipeline.primaryAlias
    }

    override fun getTabCompletions(server: MinecraftServer?, sender: ICommandSender?, args: Array<out String>?, targetPos: BlockPos?): MutableList<String> {
        return listOf<String>().toMutableList() // TODO: this
    }

    override fun compareTo(other: ICommand?): Int {
        if (other == null) return -1

        return this.name.compareTo(other.name)
    }

    override fun checkPermission(server: MinecraftServer?, sender: ICommandSender): Boolean {
        return true // TODO: implement this too
    }

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean {
        return false
    }

    override fun getAliases(): MutableList<String> {
        return pipeline.aliases.toMutableList()
    }

    fun ICommandSender.reply(text: String, color: TextFormatting = TextFormatting.GOLD) {
        val component = TextComponentString(text).also {
            it.style.color = color
        }

        this.sendMessage(component)
    }

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        val namespace = Namespace()

        namespace.set("server", server)
        namespace.set("sender", sender)

        try {
            pipeline.call(ArrayDeque(args.asList()), namespace)
        } catch (ex: UsageException) {
            sender.reply("Usage: " + this.getUsage(sender), TextFormatting.RED)
        } catch (ex: DispatcherException) {
            sender.reply(ex.message ?: "An unknown error occurred!", TextFormatting.RED)
        }
    }
}