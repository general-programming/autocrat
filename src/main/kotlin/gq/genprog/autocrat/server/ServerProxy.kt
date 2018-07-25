package gq.genprog.autocrat.server

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.frame.ForgeCommandFactory
import gq.genprog.autocrat.frame.ForgeHookCallback
import gq.genprog.autocrat.frame.bindings.ForgeBindingProvider
import gq.genprog.autocrat.frame.injectors.ForgeEventInjector
import gq.genprog.autocrat.integration.CustomBindings
import gq.genprog.autocrat.integration.ReloadableTricks
import gq.genprog.autocrat.modules.*
import io.github.hedgehog1029.frame.Frame
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
open class ServerProxy: Proxy() {
    val frame: Frame = Frame.Builder().also {
        it.commandFactory = ForgeCommandFactory()
        it.hookCallback = ForgeHookCallback()
    }.build()

    override fun onPreInit(ev: FMLPreInitializationEvent) {
        frame.addInjector(ForgeEventInjector())

        frame.loadBindings(ForgeBindingProvider())
        frame.loadBindings(CustomBindings())

        frame.loadModule(ChoicesModule())
//        frame.loadModule(TricksModule())

        if (AutocratConfig.modules.claims) {
            frame.loadModule(GroupModule())
            frame.loadModule(ClaimsModule())
        }

        if (AutocratConfig.modules.sleepVote)
            frame.loadModule(SleepVoteModule())

        if (AutocratConfig.modules.fancyNames)
            frame.loadModule(FancyName())

        if (AutocratConfig.modules.admin)
            frame.loadModule(AdminModule())

        frame.loadModule(BackupsModule())
    }

    override fun onServerStart(ev: FMLServerStartingEvent) {
        frame.go()
    }

    override fun onServerStarted(ev: FMLServerStartedEvent) {
        ReloadableTricks.reload()
    }
}