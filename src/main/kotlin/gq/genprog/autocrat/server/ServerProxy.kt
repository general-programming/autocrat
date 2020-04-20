package gq.genprog.autocrat.server

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.frame.ForgeCommandFactory
import gq.genprog.autocrat.frame.ForgeHookCallback
import gq.genprog.autocrat.frame.bindings.ForgeBindingProvider
import gq.genprog.autocrat.frame.injectors.ForgeEventInjector
import gq.genprog.autocrat.integration.ConflictChecker
import gq.genprog.autocrat.integration.CustomBindings
import gq.genprog.autocrat.integration.ReloadableTricks
import gq.genprog.autocrat.modules.*
import gq.genprog.autocrat.modules.data.IHomeCapability
import gq.genprog.autocrat.modules.data.PlayerHomes
import gq.genprog.autocrat.modules.data.capability.HomeStorage
import io.github.hedgehog1029.frame.Frame
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.event.server.FMLServerStartedEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ServerProxy {
    val frame: Frame = Frame.Builder().also {
        it.commandFactory = ForgeCommandFactory()
        it.hookCallback = ForgeHookCallback()
    }.build()

    @SubscribeEvent
    fun onServerInit(ev: FMLDedicatedServerSetupEvent) {
        CapabilityManager.INSTANCE.register(IHomeCapability::class.java, HomeStorage()) { PlayerHomes() }

        frame.addInjector(ForgeEventInjector())

        frame.loadBindings(ForgeBindingProvider())
        frame.loadBindings(CustomBindings())

        frame.loadModule(ChoicesModule())
//        frame.loadModule(TricksModule())

        if (AutocratConfig.modules.claims) {
            frame.loadModule(GroupModule())
            frame.loadModule(ClaimsModule())
        }

        if (AutocratConfig.modules.sleepVote && !ConflictChecker.isSleepVoteLoaded())
            frame.loadModule(SleepVoteModule())

        if (AutocratConfig.modules.fancyNames)
            frame.loadModule(FancyName())

        if (AutocratConfig.modules.admin)
            frame.loadModule(AdminModule())

        if (AutocratConfig.modules.simpleHome)
            frame.loadModule(SimpleHomesModule())

        if (AutocratConfig.modules.commandHome)
            frame.loadModule(HomesModule())
    }

    @SubscribeEvent
    fun onServerStart(ev: FMLServerStartingEvent) {
        frame.go()
    }

    @SubscribeEvent
    fun onServerStarted(ev: FMLServerStartedEvent) {
        ReloadableTricks.reload()
    }
}