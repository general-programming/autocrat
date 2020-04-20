package gq.genprog.autocrat.modules.data

import gq.genprog.autocrat.MOD_ID
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
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
            if (world !is ServerWorld) throw RuntimeException("Cannot access saved data clientside!");
            // TODO: Better way of doing this. Runtime exceptions suck.

            val storage = world.savedData
            return storage.getOrCreate({ MiscStorage() }, IDENTIFIER)
        }
    }

    val nicknames: HashMap<UUID, String> = hashMapOf()
    val modMode = ModModeData()

    fun hasNick(player: PlayerEntity): Boolean {
        return nicknames.containsKey(player.uniqueID)
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        val nickStorage = CompoundNBT()
        for (entry in nicknames) {
            nickStorage.putString(entry.key.toString(), entry.value)
        }

        compound.put("nicks", nickStorage)
        compound.put("admin", modMode.serializeNBT())
        return compound
    }

    override fun read(nbt: CompoundNBT) {
        val nickStorage = nbt.getCompound("nicks")
        for (key in nickStorage.keySet()) {
            val uid = UUID.fromString(key)

            nicknames[uid] = nickStorage.getString(key)
        }

        this.modMode.deserializeNBT(nbt.getCompound("admin"))
    }
}