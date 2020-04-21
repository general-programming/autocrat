package gq.genprog.autocrat.modules.data

import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.math.BlockPos
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
    var lastLocation: BlockPos = BlockPos.ZERO
    var active = false

    override fun deserializeNBT(nbt: CompoundNBT) {
        adminInventory = nbt.getList("adminInv", Constants.NBT.TAG_COMPOUND)
        normalInventory = nbt.getList("normalInv", Constants.NBT.TAG_COMPOUND)
        lastLocation = BlockPos.fromLong(nbt.getLong("lastLoc"))
        active = nbt.getBoolean("active")
    }

    override fun serializeNBT(): CompoundNBT {
        val tag = CompoundNBT()

        tag.put("adminInv", adminInventory)
        tag.put("normalInv", normalInventory)
        tag.putLong("lastLoc", lastLocation.toLong())
        tag.putBoolean("active", active)

        return tag
    }

    inner class Handlers(val player: ServerPlayerEntity) {
        fun transitionToAdmin() {
            player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent {
                normalInventory = serializeInventory(it)
                lastLocation = player.position
                active = true

                player.inventory.clear()
                deserializeInto(adminInventory, it)
//              adminInventory.clear()
            }
        }

        fun transitionToUser() {
            player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent {
                adminInventory = serializeInventory(it)
                active = false

                player.inventory.clear()
                deserializeInto(normalInventory, it)
                player.setPositionAndUpdate(lastLocation.x.toDouble(), lastLocation.y.toDouble(), lastLocation.z.toDouble())
//              normalInventory.clear()
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