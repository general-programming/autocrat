package gq.genprog.autocrat.server

import gq.genprog.autocrat.frame.ForgeCommandFactory
import gq.genprog.autocrat.frame.ForgeHookCallback
import gq.genprog.autocrat.frame.bindings.ForgeBindingProvider
import gq.genprog.autocrat.frame.injectors.ForgeEventInjector
import gq.genprog.autocrat.modules.*
import io.github.hedgehog1029.frame.Frame
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
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

        frame.loadModule(ChoicesModule())
        frame.loadModule(GroupModule())
        frame.loadModule(ClaimsModule())
        frame.loadModule(SleepVoteModule())
        frame.loadModule(FancyName())
    }

    override fun onServerStart(ev: FMLServerStartingEvent) {
        frame.go()
    }
}