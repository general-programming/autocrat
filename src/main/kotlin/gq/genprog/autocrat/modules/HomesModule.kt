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
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class HomesModule: EventListener {
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
    fun home(@Sender sender: EntityPlayerMP, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        if (sender.hasCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)) {
            val homes = sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!

            val home = homes.getHome(homeName)

            if (home != null) {
                sender.setPositionAndUpdate(home.x.toDouble() + 0.5, home.y.toDouble(), home.z.toDouble() + 0.5)
                sender.controller().success("Teleported to $homeName.")
            } else {
                sender.controller().err("No home named '$homeName'.")
            }
        } else {
            sender.controller().err("Severe error getting home: no home capability!")
        }
    }

    @Command(aliases = ["sethome"], description = "Set a home.")
    fun sethome(@Sender sender: EntityPlayerMP, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        if (sender.hasCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)) {
            val homes = sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!

            homes.setHome(homeName, sender.position)
            sender.controller().success("Set home '$homeName' to (${sender.position.joinToString()}).")
        } else {
            sender.controller().err("Severe error setting home: no home capability!")
        }
    }

    @Command(aliases = ["delhome"], description = "Delete a home.")
    fun delhome(@Sender sender: EntityPlayerMP, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        if (sender.hasCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)) {
            val homes = sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!, null)!!

            if (homes.delHome(homeName))
                sender.controller().success("Deleted home '$homeName'.")
            else
                sender.controller().warn("You don't currently have a home with the name '$homeName'.")
        } else {
            sender.controller().err("Severe error setting home: no home capability!")
        }
    }

    class HomeCapabilityProvider: ICapabilitySerializable<NBTTagCompound> {
        val homes = PlayerHomes()

        override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            if (capability == CapabilityHomeStorage.HOME_STORAGE_CAPABILITY) {
                return CapabilityHomeStorage.get().cast(homes)
            }

            return null
        }

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            if (capability == CapabilityHomeStorage.HOME_STORAGE_CAPABILITY)
                return true

            return false
        }

        override fun serializeNBT(): NBTTagCompound {
            return CapabilityHomeStorage.get().writeNBT(homes, null) as NBTTagCompound
        }

        override fun deserializeNBT(nbt: NBTTagCompound?) {
            if (nbt == null) return

            CapabilityHomeStorage.get().readNBT(homes, null, nbt)
        }
    }
}