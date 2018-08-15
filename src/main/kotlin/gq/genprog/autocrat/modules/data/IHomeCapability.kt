package gq.genprog.autocrat.modules.data

import net.minecraft.util.math.BlockPos

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
interface IHomeCapability {
    fun getHome(name: String): BlockPos?
    fun setHome(name: String, pos: BlockPos)
    fun delHome(name: String): Boolean
    fun getAllHomes(): Map<String, BlockPos>
}