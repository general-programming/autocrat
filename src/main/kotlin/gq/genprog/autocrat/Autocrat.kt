package gq.genprog.autocrat

import gq.genprog.autocrat.server.Proxy
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Mod(modid = MOD_ID, name = MOD_NAME, acceptableRemoteVersions = "*", modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter", dependencies = "required-after:forgelin;")
class Autocrat {
    companion object {
        @SidedProxy(modId = MOD_ID, serverSide = "gq.genprog.autocrat.server.ServerProxy", clientSide = "gq.genprog.autocrat.server.Proxy")
        @JvmStatic var proxy: Proxy? = null
    }

    @Mod.EventHandler fun onPreInit(ev: FMLPreInitializationEvent) {
        proxy?.onPreInit(ev)
    }

    @Mod.EventHandler fun onInit(ev: FMLInitializationEvent) {
        proxy?.onInit(ev)
    }

    @Mod.EventHandler fun onPostInit(ev: FMLPostInitializationEvent) {
        proxy?.onPostInit(ev)
    }

    @Mod.EventHandler fun onServerStarting(ev: FMLServerStartingEvent) {
        proxy?.onServerStart(ev)
    }

    @Mod.EventHandler fun onServerStarted(ev: FMLServerStartedEvent) {
        proxy?.onServerStarted(ev)
    }
}