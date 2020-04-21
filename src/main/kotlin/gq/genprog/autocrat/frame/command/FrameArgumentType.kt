package gq.genprog.autocrat.frame.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.hedgehog1029.frame.util.Namespace
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class FrameArgumentType(val cmd: FrameForgeCommand): ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val namespace = Namespace()
        val src = context.source as CommandSource

        namespace["server"] = src.server
        namespace["source"] = src

        cmd.pipeline.getCompletions(builder.input.split(' '), namespace).forEach {
            builder.suggest(it)
        }

        return builder.buildFuture()
    }
}