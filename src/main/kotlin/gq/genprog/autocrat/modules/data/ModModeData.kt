package gq.genprog.autocrat.modules.data

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.nbt.StringNBT
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ModModeData: INBTSerializable<CompoundNBT> {
    val adminInventories: HashMap<UUID, ListNBT> = hashMapOf()
    val normalInventories: HashMap<UUID, ListNBT> = hashMapOf()
    val lastLocation: HashMap<UUID, BlockPos> = hashMapOf()
    val active: HashSet<UUID> = hashSetOf()

    fun isPlayerActive(player: PlayerEntity): Boolean {
        return active.contains(player.uniqueID)
    }

    fun storePlayerTag(player: PlayerEntity, tag: ListNBT): ListNBT? {
        normalInventories[player.uniqueID] = tag
        active.add(player.uniqueID)
        lastLocation[player.uniqueID] = player.position
        return adminInventories.remove(player.uniqueID)
    }

    fun storeAdminTag(player: PlayerEntity, tag: ListNBT): ListNBT? {
        adminInventories[player.uniqueID] = tag
        active.remove(player.uniqueID)
        return normalInventories.remove(player.uniqueID)
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        val adminTag = nbt.getCompound("adminInv")
        for (key in adminTag.keySet()) {
            adminInventories[UUID.fromString(key)] = adminTag.getList(key, Constants.NBT.TAG_COMPOUND)
        }

        val normalTag = nbt.getCompound("normalInv")
        for (key in normalTag.keySet()) {
            normalInventories[UUID.fromString(key)] = normalTag.getList(key, Constants.NBT.TAG_COMPOUND)
        }

        val lastLocTag = nbt.getCompound("lastLoc")
        for (key in lastLocTag.keySet()) {
            lastLocation[UUID.fromString(key)] = BlockPos.fromLong(lastLocTag.getLong(key))
        }

        val activeTag = nbt.getList("active", Constants.NBT.TAG_STRING)
        for (tag in activeTag) {
            val value = (tag as StringNBT).string

            active.add(UUID.fromString(value))
        }
    }

    override fun serializeNBT(): CompoundNBT {
        val compound = CompoundNBT()

        val adminTag = CompoundNBT()
        adminInventories.forEach {
            adminTag.put(it.key.toString(), it.value)
        }

        val normalTag = CompoundNBT()
        normalInventories.forEach {
            normalTag.put(it.key.toString(), it.value)
        }

        val lastLocTag = CompoundNBT()
        lastLocation.forEach {
            lastLocTag.putLong(it.key.toString(), it.value.toLong())
        }

        val activeTag = ListNBT()
        active.forEach {
            activeTag.add(StringNBT(it.toString()))
        }

        compound.put("adminInv", adminTag)
        compound.put("normalInv", normalTag)
        compound.put("lastLoc", lastLocTag)
        compound.put("active", activeTag)

        return compound
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