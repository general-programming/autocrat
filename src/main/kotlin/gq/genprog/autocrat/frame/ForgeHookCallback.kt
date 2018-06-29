package gq.genprog.autocrat.frame

import io.github.hedgehog1029.frame.hooks.IHookCallback
import net.minecraftforge.fml.common.Loader

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ForgeHookCallback: IHookCallback {
    override fun shouldHookLoad(modid: String): Boolean {
        return Loader.isModLoaded(modid)
    }
}