package gq.genprog.autocrat.integration.sel

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MutableBoundingBox

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class MutableSelection {
    var first: BlockPos? = null
    var second: BlockPos? = null

    fun toStructureBox(): MutableBoundingBox? {
        val a = first
        val b = second

        if (a == null || b == null)
            return null

        return MutableBoundingBox.createProper(a.x, a.y, a.z, b.x, b.y, b.z)
    }

    fun isComplete(): Boolean {
        return first != null && second != null
    }

    fun getVolume(): Int {
        if (!this.isComplete()) return -1

        val w = Math.abs(second!!.x - first!!.x)
        val h = Math.abs(second!!.y - first!!.y)
        val d = Math.abs(second!!.z - first!!.z)

        return w * h * d
    }
}