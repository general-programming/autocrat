package gq.genprog.autocrat.frame.bindings

import io.github.hedgehog1029.frame.dispatcher.ArgumentTransformer
import io.github.hedgehog1029.frame.util.IBindingProvider
import net.minecraft.command.ICommandSource
import net.minecraft.entity.player.ServerPlayerEntity

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeBindingProvider: IBindingProvider {
    override fun configure(transformer: ArgumentTransformer) {
        transformer.bind(ICommandSource::class.java, SenderProvider())
        transformer.bind(ServerPlayerEntity::class.java, SenderProvider())
    }
}