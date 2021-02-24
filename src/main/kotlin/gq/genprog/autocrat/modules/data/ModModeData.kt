package gq.genprog.autocrat.modules.data

import gq.genprog.autocrat.toDoubleVec
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.RegistryKey
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ModModeData: INBTSerializable<CompoundNBT> {
    var adminInventory = ListNBT()
    var normalInventory = ListNBT()
    var lastWorld: RegistryKey<World>? = null
    var lastLocation: BlockPos = BlockPos.ZERO
    var active = false

    override fun deserializeNBT(nbt: CompoundNBT) {
        adminInventory = nbt.getList("adminInv", Constants.NBT.TAG_COMPOUND)
        normalInventory = nbt.getList("normalInv", Constants.NBT.TAG_COMPOUND)
        lastLocation = BlockPos.fromLong(nbt.getLong("lastLoc"))
        active = nbt.getBoolean("active")

        if (nbt.contains("lastWorld")) {
            val location = ResourceLocation(nbt.getString("lastWorld"))
            lastWorld = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, location)
        }
    }

    override fun serializeNBT(): CompoundNBT {
        val tag = CompoundNBT()

        tag.put("adminInv", adminInventory)
        tag.put("normalInv", normalInventory)
        tag.putLong("lastLoc", lastLocation.toLong())
        tag.putBoolean("active", active)

        lastWorld?.also {
            tag.putString("lastWorld", it.location.toString())
        }

        return tag
    }

    inner class Handlers(val player: ServerPlayerEntity) {
        fun transitionToAdmin() {
            player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent {
                normalInventory = serializeInventory(it)
                lastWorld = player.world.dimensionKey
                lastLocation = player.position
                active = true

                player.inventory.clear()
                deserializeInto(adminInventory, it)
            }
        }

        fun transitionToUser() {
            player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent {
                adminInventory = serializeInventory(it)
                active = false

                player.inventory.clear()
                deserializeInto(normalInventory, it)

                if (lastWorld != null) {
                    player.server.getWorld(lastWorld!!)?.also { world ->
                        val newPos = lastLocation.toDoubleVec().add(0.5, 0.0, 0.5)
                        player.teleport(world, newPos.x, newPos.y, newPos.z, player.rotationYaw, player.rotationPitch)
                    }
                } else {
                    lastLocation.toDoubleVec().add(0.5, 0.0, 0.5).apply {
                        player.setPositionAndUpdate(x, y, z)
                    }
                }
            }
        }

        fun serializeInventory(inv: IItemHandler): ListNBT {
            val stacks = ListNBT()

            for(i in 0..inv.slots) {
                val nbt = inv.getStackInSlot(i).serializeNBT()
                stacks.add(nbt)
            }

            return stacks
        }

        fun deserializeInto(nbt: ListNBT, inv: IItemHandler) {
            for ((i, entry) in nbt.withIndex()) {
                val stack = ItemStack.read(entry as CompoundNBT)
                inv.insertItem(i, stack, false)
            }
        }
    }

    fun getHandlers(player: ServerPlayerEntity) = Handlers(player)
}
