package gq.genprog.autocrat.frame.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import gq.genprog.autocrat.server.MessageBuilder
import io.github.hedgehog1029.frame.dispatcher.exception.DispatcherException
import io.github.hedgehog1029.frame.dispatcher.exception.UsageException
import io.github.hedgehog1029.frame.util.Namespace
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TextFormatting
import java.util.*

class FrameCommandExecutor(val cmd: FrameForgeCommand, val parameterNames: List<String>): Command<CommandSource> {
    override fun run(context: CommandContext<CommandSource>): Int {
        val namespace = Namespace()

        namespace.set("server", context.source.server)
        namespace.set("source", context.source)
        namespace.set("sender", context.source.entity ?: context.source.server)
        // urgh, CommandSource#source is private

        try {
            val arguments = ArrayDeque<String>()

            for (name in parameterNames) {
                arguments.add(context.getArgument(name, String::class.java))
            }

            cmd.pipeline.call(arguments, namespace)

            return 1
        } catch (ex: UsageException) {
            context.source.sendErrorMessage(MessageBuilder().apply {
                color(TextFormatting.RED)
                last("Usage: ${cmd.usage}")
            }.parent)

            return -1
        } catch (ex: DispatcherException) {
            context.source.sendErrorMessage(MessageBuilder().apply {
                color(TextFormatting.RED)
                last(ex.message ?: "An unknown error occurred!")
            }.parent)

            ex.printStackTrace()

            return -1
        }
    }
}