package gq.genprog.autocrat.modules.claims

import gq.genprog.autocrat.MOD_ID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
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

        fun get(world: World): ClaimWorldStorage {
            val storage = world.perWorldStorage
            var inst = storage.getOrLoadData(ClaimWorldStorage::class.java, IDENTIFIER) as ClaimWorldStorage?

            if (inst == null) {
                inst = ClaimWorldStorage()
                storage.setData(IDENTIFIER, inst)
            }

            return inst
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

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        val groups = NBTTagCompound()
        for (group in this.groups) {
            groups.setTag(group.key, group.value.serializeNBT())
        }

        val chunkList = NBTTagList()
        for (entry in chunks) {
            val serial = NBTTagCompound()

            serial.setInteger("x", entry.key.x)
            serial.setInteger("z", entry.key.z)
            serial.setString("group", entry.value)

            chunkList.appendTag(serial)
        }

        compound.setTag("groups", groups)
        compound.setTag("chunks", chunkList)

        return compound
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        val groups = nbt.getCompoundTag("groups")
        for (id in groups.keySet) {
            this.groups[id] = Faction.Builder().also {
                it.id = id
                it.deserializeNBT(groups.getCompoundTag(id))
            }.build()
        }

        val chunkList = nbt.getTagList("chunks", Constants.NBT.TAG_COMPOUND)
        for (entry in chunkList) {
            val data = entry as NBTTagCompound
            val pos = ChunkPos(data.getInteger("x"), data.getInteger("z"))

            chunks[pos] = data.getString("group")
        }
    }
}