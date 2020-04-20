package gq.genprog.autocrat.modules.claims

import gq.genprog.autocrat.modules.claims.groups.AccessMode
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.nbt.StringNBT
import net.minecraftforge.common.util.Constants
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class Faction(val id: String, var name: String, val owner: UUID, var access: AccessMode, val members: ArrayList<UUID>) {
    fun isForeign(user: UUID): Boolean {
        return user != owner && !members.contains(user)
    }

    fun isForeign(user: PlayerEntity): Boolean {
        return isForeign(user.uniqueID)
    }

    fun isOwner(user: UUID): Boolean {
        return user == owner
    }

    fun isOwner(user: PlayerEntity): Boolean {
        return isOwner(user.uniqueID)
    }

    fun serializeNBT(): CompoundNBT {
        val nbt = CompoundNBT()

        nbt.setString("name", name)
        nbt.setUniqueId("owner", owner)
        nbt.setByte("accessMode", access.id)

        val memberTag = ListNBT()
        for (member in members) {
            memberTag.appendTag(StringNBT(member.toString()))
        }

        nbt.setTag("members", memberTag)

        return nbt
    }

    class Builder {
        var id: String? = null
        var name: String = "Unnamed Faction"
        var owner: UUID? = null
        var access: AccessMode? = null
        val members: ArrayList<UUID> = arrayListOf()

        fun deserializeNBT(nbt: CompoundNBT) {
            this.name = nbt.getString("name")
            this.owner = nbt.getUniqueId("owner")
            this.access = AccessMode.fromId(nbt.getByte("accessMode"))

            val memberTags = nbt.getList("members", Constants.NBT.TAG_STRING)
            for (tag in memberTags) {
                this.members.add(UUID.fromString((tag as StringNBT).string))
            }
        }

        fun build(): Faction {
            return Faction(id!!, name, owner!!, access!!, members)
        }
    }
}