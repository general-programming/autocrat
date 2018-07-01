package gq.genprog.autocrat.modules.data

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ModModeData: INBTSerializable<NBTTagCompound> {
    val adminInventories: HashMap<UUID, NBTTagList> = hashMapOf()
    val normalInventories: HashMap<UUID, NBTTagList> = hashMapOf()
    val active: HashSet<UUID> = hashSetOf()

    fun isPlayerActive(player: EntityPlayer): Boolean {
        return active.contains(player.uniqueID)
    }

    fun storePlayerTag(player: EntityPlayer, tag: NBTTagList): NBTTagList? {
        normalInventories[player.uniqueID] = tag
        active.add(player.uniqueID)
        return adminInventories.remove(player.uniqueID)
    }

    fun storeAdminTag(player: EntityPlayer, tag: NBTTagList): NBTTagList? {
        adminInventories[player.uniqueID] = tag
        active.remove(player.uniqueID)
        return normalInventories.remove(player.uniqueID)
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        val adminTag = nbt.getCompoundTag("adminInv")
        for (key in adminTag.keySet) {
            adminInventories[UUID.fromString(key)] = adminTag.getTagList(key, Constants.NBT.TAG_COMPOUND)
        }

        val normalTag = nbt.getCompoundTag("normalInv")
        for (key in normalTag.keySet) {
            normalInventories[UUID.fromString(key)] = normalTag.getTagList(key, Constants.NBT.TAG_COMPOUND)
        }
    }

    override fun serializeNBT(): NBTTagCompound {
        val compound = NBTTagCompound()

        val adminTag = NBTTagCompound()
        adminInventories.forEach {
            adminTag.setTag(it.key.toString(), it.value)
        }

        val normalTag = NBTTagCompound()
        normalInventories.forEach {
            normalTag.setTag(it.key.toString(), it.value)
        }

        return compound
    }

    fun serializeInventory(inv: IItemHandler): NBTTagList {
        val stacks = NBTTagList()

        for(i in 0..inv.slots) {
            val nbt = inv.getStackInSlot(i).serializeNBT()
            stacks.appendTag(nbt)
        }

        return stacks
    }

    fun deserializeInto(nbt: NBTTagList, inv: IItemHandler) {
        for ((i, entry) in nbt.withIndex()) {
            val stack = ItemStack(entry as NBTTagCompound)
            inv.insertItem(i, stack, false)
        }
    }
}