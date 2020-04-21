package gq.genprog.autocrat.frame.command

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import io.github.hedgehog1029.frame.dispatcher.pipeline.IPipeline
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.server.permission.PermissionAPI

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class FrameForgeCommand(val pipeline: IPipeline) {
    val usage get() = "${pipeline.primaryAlias} ${pipeline.usage}"
    val plans = pipeline.executionPlans.sortedBy { plan -> plan.arity }

    fun checkPermission(source: CommandSource): Boolean {
        if (pipeline.permission.isBlank() || source.entity !is PlayerEntity) {
            return true
        }

        val player = source.asPlayer()
        return PermissionAPI.hasPermission(player, pipeline.permission)
    }

    fun createNode(): LiteralArgumentBuilder<CommandSource>? {
        val builder = literal<CommandSource>(pipeline.primaryAlias)

        var lastBranch: ArgumentBuilder<CommandSource, *> = builder
        plans.forEach {
            it.parameterNames.forEach { name ->
                val next = argument<CommandSource, String>(name, FrameArgumentType())
                lastBranch.then(next)
                lastBranch = next
            }

            lastBranch.requires(this::checkPermission)
            lastBranch.executes(FrameCommandExecutor(this, it.parameterNames))
        }

        return builder
    }
}