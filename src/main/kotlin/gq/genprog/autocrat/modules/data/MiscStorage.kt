package gq.genprog.autocrat.modules.data

import gq.genprog.autocrat.MOD_ID
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class MiscStorage(name: String): WorldSavedData(name) {
    constructor(): this(IDENTIFIER)

    companion object {
        val IDENTIFIER = "${MOD_ID}_misc"

        fun get(world: World): MiscStorage {
            val storage = world.mapStorage!!
            var inst = storage.getOrLoadData(MiscStorage::class.java, IDENTIFIER) as MiscStorage?

            if (inst == null) {
                inst = MiscStorage()
                storage.setData(IDENTIFIER, inst)
            }

            return inst
        }
    }

    val nicknames: HashMap<UUID, String> = hashMapOf()
    val modMode = ModModeData()

    fun hasNick(player: EntityPlayer): Boolean {
        return nicknames.containsKey(player.uniqueID)
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        val nickStorage = NBTTagCompound()
        for (entry in nicknames) {
            nickStorage.setString(entry.key.toString(), entry.value)
        }

        compound.setTag("nicks", nickStorage)
        compound.setTag("admin", modMode.serializeNBT())
        return compound
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        val nickStorage = nbt.getCompoundTag("nicks")
        for (key in nickStorage.keySet) {
            val uid = UUID.fromString(key)

            nicknames[uid] = nickStorage.getString(key)
        }

        this.modMode.deserializeNBT(nbt.getCompoundTag("admin"))
    }
}