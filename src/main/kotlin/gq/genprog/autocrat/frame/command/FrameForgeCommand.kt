package gq.genprog.autocrat.frame.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import gq.genprog.autocrat.server.MessageBuilder
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.server.permission.PermissionAPI

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class FrameForgeCommand(val pipeline: IPipeline) {
    val usage get() = "/${pipeline.primaryAlias} ${pipeline.usage}"
    val plans = pipeline.executionPlans.sortedBy { plan -> plan.arity }

    fun checkPermission(source: CommandSource): Boolean {
        if (pipeline.permission.isBlank() || source.entity !is PlayerEntity) {
            return true
        }

        val player = source.asPlayer()
        return PermissionAPI.hasPermission(player, pipeline.permission)
    }

    fun defaultExecutor(ctx: CommandContext<CommandSource>): Int {
        ctx.source.sendErrorMessage(MessageBuilder().apply {
            color(TextFormatting.RED)
            last("Usage: $usage")
        }.parent)

        return -1
    }

    fun createNode(): LiteralArgumentBuilder<CommandSource>? {
        val builder = literal<CommandSource>(pipeline.primaryAlias)

        plans.forEach {
            val mappedArgs = it.parameterNames.map { name ->
                argument<CommandSource, String>(name, FrameArgumentType(this))
            }

            val last = mappedArgs.lastOrNull() ?: builder
            last.requires(this::checkPermission)
            last.executes(FrameCommandExecutor(this, it.parameterNames))

            if (mappedArgs.isNotEmpty()) {
                val branch = mappedArgs.reduceRight { arg, acc -> arg.then(acc) }
                builder.then(branch)
            }
        }

        if (builder.command == null) {
            builder.executes(this::defaultExecutor)
        }

        return builder
    }
}