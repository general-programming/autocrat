package gq.genprog.autocrat.modules.claims

import gq.genprog.autocrat.modules.claims.groups.AccessMode
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
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

    fun isForeign(user: EntityPlayer): Boolean {
        return isForeign(user.uniqueID)
    }

    fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()

        nbt.setString("name", name)
        nbt.setUniqueId("owner", owner)
        nbt.setByte("accessMode", access.id)

        val memberTag = NBTTagList()
        for (member in members) {
            memberTag.appendTag(NBTTagString(member.toString()))
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

        fun deserializeNBT(nbt: NBTTagCompound) {
            this.name = nbt.getString("name")
            this.owner = nbt.getUniqueId("owner")
            this.access = AccessMode.fromId(nbt.getByte("accessMode"))

            val memberTags = nbt.getTagList("members", Constants.NBT.TAG_STRING)
            for (tag in memberTags) {
                this.members.add(UUID.fromString((tag as NBTTagString).string))
            }
        }

        fun build(): Faction {
            return Faction(id!!, name, owner!!, access!!, members)
        }
    }
}