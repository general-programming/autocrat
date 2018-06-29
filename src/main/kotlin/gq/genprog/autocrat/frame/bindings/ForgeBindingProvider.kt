package gq.genprog.autocrat.frame.bindings

import io.github.hedgehog1029.frame.dispatcher.ArgumentTransformer
import io.github.hedgehog1029.frame.util.IBindingProvider
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeBindingProvider: IBindingProvider {
    override fun configure(transformer: ArgumentTransformer) {
        transformer.bind(ICommandSender::class.java, SenderProvider())
        transformer.bind(EntityPlayerMP::class.java, SenderProvider())
    }
}