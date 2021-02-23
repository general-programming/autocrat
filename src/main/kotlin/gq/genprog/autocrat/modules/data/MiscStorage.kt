package gq.genprog.autocrat.modules.data

import gq.genprog.autocrat.MOD_ID
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.server.MinecraftServer
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

        fun get(server: MinecraftServer): MiscStorage {
            val world = server.getWorld(World.OVERWORLD)!!
            val storage = world.savedData
            return storage.getOrCreate({ MiscStorage() }, IDENTIFIER)
        }
    }

    val nicknames: HashMap<UUID, String> = hashMapOf()
    val modModeData: HashMap<UUID, ModModeData> = hashMapOf()

    fun hasNick(player: PlayerEntity): Boolean {
        return nicknames.containsKey(player.uniqueID)
    }

    fun fetchModModeData(player: ServerPlayerEntity): ModModeData {
        return modModeData.getOrPut(player.uniqueID) {
            ModModeData().apply {
                lastLocation = player.position
            }
        }
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        val nickStorage = CompoundNBT()
        for (entry in nicknames) {
            nickStorage.putString(entry.key.toString(), entry.value)
        }

        val modModeStorage = CompoundNBT()
        for (entry in modModeData) {
            modModeStorage.put(entry.key.toString(), entry.value.serializeNBT())
        }

        compound.put("nicks", nickStorage)
        compound.put("admin", modModeStorage)
        return compound
    }

    override fun read(nbt: CompoundNBT) {
        val nickStorage = nbt.getCompound("nicks")
        for (key in nickStorage.keySet()) {
            val uid = UUID.fromString(key)

            nicknames[uid] = nickStorage.getString(key)
        }

        val modModeStorage = nbt.getCompound("admin")
        for (key in modModeStorage.keySet()) {
            val uid = UUID.fromString(key)

            modModeData[uid] = ModModeData().apply {
                deserializeNBT(modModeStorage.getCompound(key))
            }
        }
    }
}
