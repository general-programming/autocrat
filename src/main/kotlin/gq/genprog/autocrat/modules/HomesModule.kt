package gq.genprog.autocrat.modules

import gq.genprog.autocrat.joinToString
import gq.genprog.autocrat.modules.data.PlayerHomes
import gq.genprog.autocrat.modules.data.capability.CapabilityHomeStorage
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Optional
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class HomesModule: EventListener {
    @SubscribeEvent fun onCapabilityAttach(ev: AttachCapabilitiesEvent<Entity>) {
        val ent = ev.`object`

        if (ent is PlayerEntity) {
            ev.addCapability(ResourceLocation("autocrat:homes"), HomeCapabilityProvider())
        }
    }

    @SubscribeEvent fun onClone(ev: PlayerEvent.Clone) {
        if (!ev.isWasDeath) return

        ev.original.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!).ifPresent { oldStorage ->
            ev.player.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!).ifPresent { newStorage ->
                val homes = newStorage.getAllHomes()
                (homes as MutableMap).putAll(oldStorage.getAllHomes())
            }
        }
    }

    @Command(aliases = ["home"], description = "Return to a home.")
    fun home(@Sender sender: ServerPlayerEntity, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!).ifPresent { homes ->
            val home = homes.getHome(homeName)

            if (home != null) {
                sender.setPositionAndUpdate(home.x.toDouble() + 0.5, home.y.toDouble(), home.z.toDouble() + 0.5)
                sender.controller().success("Teleported to $homeName.")
            } else {
                sender.controller().err("No home named '$homeName'.")
            }
        }
    }

    @Command(aliases = ["sethome"], description = "Set a home.")
    fun sethome(@Sender sender: ServerPlayerEntity, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!).ifPresent { homes ->
            homes.setHome(homeName, sender.position)
            sender.controller().success("Set home '$homeName' to (${sender.position.joinToString()}).")
        }
    }

    @Command(aliases = ["delhome"], description = "Delete a home.")
    fun delhome(@Sender sender: ServerPlayerEntity, @Optional optHomeName: String?) {
        val homeName = optHomeName ?: "home"

        sender.getCapability(CapabilityHomeStorage.HOME_STORAGE_CAPABILITY!!).ifPresent { homes ->
            if (homes.delHome(homeName))
                sender.controller().success("Deleted home '$homeName'.")
            else
                sender.controller().warn("You don't currently have a home with the name '$homeName'.")
        }
    }

    class HomeCapabilityProvider: ICapabilitySerializable<CompoundNBT> {
        val homes = PlayerHomes()

        override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
            return CapabilityHomeStorage.get().orEmpty<T>(capability, LazyOptional.of { homes })
        }

        override fun serializeNBT(): CompoundNBT {
            return CapabilityHomeStorage.get().writeNBT(homes, null) as CompoundNBT
        }

        override fun deserializeNBT(nbt: CompoundNBT?) {
            if (nbt == null) return

            CapabilityHomeStorage.get().readNBT(homes, null, nbt)
        }
    }
}
