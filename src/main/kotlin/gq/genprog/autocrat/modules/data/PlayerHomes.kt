package gq.genprog.autocrat.modules.data

import net.minecraft.util.math.BlockPos

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class PlayerHomes(homes: Map<String, BlockPos>) : IHomeCapability {
    val homes: HashMap<String, BlockPos> = hashMapOf()

    init {
        this.homes.putAll(homes)
    }

    constructor(): this(hashMapOf())

    override fun getHome(name: String): BlockPos? {
        return homes[name]
    }

    override fun setHome(name: String, pos: BlockPos) {
        homes[name] = pos
    }

    override fun delHome(name: String): Boolean {
        return homes.remove(name) != null
    }

    override fun getAllHomes(): Map<String, BlockPos> {
        return homes
    }
}