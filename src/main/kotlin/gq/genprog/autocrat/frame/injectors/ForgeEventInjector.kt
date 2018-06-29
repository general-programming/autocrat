package gq.genprog.autocrat.frame.injectors

import io.github.hedgehog1029.frame.inject.Injector
import io.github.hedgehog1029.frame.module.LoadedModule
import net.minecraftforge.common.MinecraftForge
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeEventInjector: Injector {
    override fun inject(module: LoadedModule<*>) {
        if (module.instance is EventListener) {
            MinecraftForge.EVENT_BUS.register(module.instance)
        }
    }
}