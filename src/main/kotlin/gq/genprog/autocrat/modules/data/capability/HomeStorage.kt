package gq.genprog.autocrat.modules.data.capability

import gq.genprog.autocrat.modules.data.IHomeCapability
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class HomeStorage: Capability.IStorage<IHomeCapability> {
    override fun readNBT(capability: Capability<IHomeCapability>, instance: IHomeCapability, side: Direction?, nbt: INBT) {
        nbt as CompoundNBT

        for (name in nbt.keySet()) {
            val pos = BlockPos.fromLong(nbt.getLong(name))

            instance.setHome(name, pos)
        }
    }

    override fun writeNBT(capability: Capability<IHomeCapability>, instance: IHomeCapability, side: Direction?): INBT? {
        val compound = CompoundNBT()

        for ((name, pos) in instance.getAllHomes()) {
            compound.putLong(name, pos.toLong())
        }

        return compound
    }
}