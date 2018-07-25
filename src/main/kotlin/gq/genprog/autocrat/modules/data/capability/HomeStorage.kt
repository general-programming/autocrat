package gq.genprog.autocrat.modules.data.capability

import gq.genprog.autocrat.modules.data.IHomeCapability
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class HomeStorage: Capability.IStorage<IHomeCapability> {
    override fun readNBT(capability: Capability<IHomeCapability>, instance: IHomeCapability, side: EnumFacing?, nbt: NBTBase) {
        nbt as NBTTagCompound

        for (name in nbt.keySet) {
            val pos = BlockPos.fromLong(nbt.getLong(name))

            instance.setHome(name, pos)
        }
    }

    override fun writeNBT(capability: Capability<IHomeCapability>, instance: IHomeCapability, side: EnumFacing?): NBTBase? {
        val compound = NBTTagCompound()

        for ((name, pos) in instance.getAllHomes()) {
            compound.setLong(name, pos.toLong())
        }

        return compound
    }
}