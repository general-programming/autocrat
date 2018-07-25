package gq.genprog.autocrat.modules

import gq.genprog.autocrat.joinToString
import gq.genprog.autocrat.modules.data.PlayerHomes
import gq.genprog.autocrat.modules.data.capability.CapabilityHomeStorage
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Optional
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class HomesModule {
    @SubscribeEvent fun onCapabilityAttach(ev: AttachCapabilitiesEvent<Entity>) {
        val ent = ev.`object`

        if (ent is EntityPlayer) {
            ev.addCapability(ResourceLocation("autocrat:homes"), HomeCapabilityProvider())
        }
    }

    @SubscribeEvent fun onClone(ev: PlayerEvent.Clone) {
        if (!ev.isWasDeath) return

        ev.original.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)?.also {
            val homes = ev.entityPlayer.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!.getAllHomes()

            (homes as MutableMap).putAll(it.getAllHomes())
        }
    }

    @Command(aliases = ["home"], description = "Return to a home.")
    fun home(@Sender sender: EntityPlayerMP, @Optional homeName: String = "home") {
        if (sender.hasCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)) {
            val homes = sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!

            val home = homes.getHome(homeName)

            if (home != null) {
                sender.setPositionAndUpdate(home.x.toDouble(), home.y.toDouble(), home.z.toDouble())
                sender.controller().success("Teleported to $homeName.")
            }
        } else {
            sender.controller().err("Severe error getting home: no home capability!")
        }
    }

    @Command(aliases = ["sethome"], description = "Set a home.")
    fun sethome(@Sender sender: EntityPlayerMP, @Optional homeName: String = "home") {
        if (sender.hasCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)) {
            val homes = sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!

            homes.setHome(homeName, sender.position)
            sender.controller().success("Set home '$homeName' to (${sender.position.joinToString()}).")
        } else {
            sender.controller().err("Severe error setting home: no home capability!")
        }
    }

    class HomeCapabilityProvider: ICapabilityProvider {
        override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            if (capability == CapabilityHomeStorage.HOME_STORAGE_CAPABILITY) {
                return CapabilityHomeStorage.HOME_STORAGE_CAPABILITY?.cast(PlayerHomes())
            }

            return null
        }

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            if (capability == CapabilityHomeStorage.HOME_STORAGE_CAPABILITY)
                return true

            return false
        }
    }
}