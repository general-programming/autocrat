package gq.genprog.autocrat.modules.claims

import gq.genprog.autocrat.MOD_ID
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.common.util.Constants
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ClaimWorldStorage(identifier: String): WorldSavedData(identifier) {
    constructor(): this(IDENTIFIER)

    companion object {
        const val IDENTIFIER = "${MOD_ID}_claims"

        fun get(world: ServerWorld): ClaimWorldStorage {
            val storage = world.savedData
            return storage.getOrCreate({ ClaimWorldStorage() }, IDENTIFIER)
        }
    }

    val chunks: HashMap<ChunkPos, String> = hashMapOf()
    val groups: HashMap<String, Faction> = hashMapOf()

    fun claimChunks(owner: UUID, requested: List<ChunkPos>): Boolean {
        val group = getGroupOfPlayer(owner) ?: return false

        if (requested.any { chunks.containsKey(it) }) {
            throw AlreadyClaimedException()
        }

        for (pos in requested) {
            chunks[pos] = group.id
        }

        this.markDirty()
        return true
    }

    fun unclaimChunksUnchecked(requested: List<ChunkPos>): Boolean {
        for (pos in requested) {
            chunks.remove(pos)
        }

        this.markDirty()
        return true
    }

    fun isClaimed(pos: ChunkPos): Boolean {
        return chunks.containsKey(pos)
    }

    fun getClaimGroup(pos: ChunkPos): Faction? {
        return getGroup(chunks[pos])
    }

    fun getGroup(id: String?): Faction? {
        return groups[id]
    }

    fun getGroupOfPlayer(uuid: UUID): Faction? {
        for (entry in groups) {
            if (!entry.value.isForeign(uuid))
                return entry.value
        }

        return null
    }

    fun countClaimsOf(group: Faction): Int {
        return chunks.count {
            it.value == group.id
        }
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        val groups = CompoundNBT()
        for (group in this.groups) {
            groups.put(group.key, group.value.serializeNBT())
        }

        val chunkList = ListNBT()
        for (entry in chunks) {
            val serial = CompoundNBT()

            serial.putInt("x", entry.key.x)
            serial.putInt("z", entry.key.z)
            serial.putString("group", entry.value)

            chunkList.add(serial)
        }

        compound.put("groups", groups)
        compound.put("chunks", chunkList)

        return compound
    }

    override fun read(nbt: CompoundNBT) {
        val groups = nbt.getCompound("groups")
        for (id in groups.keySet()) {
            this.groups[id] = Faction.Builder().also {
                it.id = id
                it.deserializeNBT(groups.getCompound(id))
            }.build()
        }

        val chunkList = nbt.getList("chunks", Constants.NBT.TAG_COMPOUND)
        for (entry in chunkList) {
            val data = entry as CompoundNBT
            val pos = ChunkPos(data.getInt("x"), data.getInt("z"))

            chunks[pos] = data.getString("group")
        }
    }
}