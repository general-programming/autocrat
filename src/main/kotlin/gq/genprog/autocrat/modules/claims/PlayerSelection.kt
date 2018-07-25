package gq.genprog.autocrat.modules.claims

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class PlayerSelection {
    var first: BlockPos? = null
    var second: BlockPos? = null

    val firstChunk: ChunkPos? get() {
        if (first == null)
            return null
        return ChunkPos(first!!)
    }

    val secondChunk: ChunkPos? get() {
        if (second == null)
            return null
        return ChunkPos(second!!)
    }

    fun isComplete(): Boolean {
        return first != null && second != null
    }

    fun currentArea(): Int {
        if (!isComplete()) return 0

        val (x1, y1, z1) = first!!.run { arrayOf(x, y, z) }
        val (x2, y2, z2) = second!!.run { arrayOf(x, y, z) }

        val w = Math.abs(x1 - x2)
        val h = Math.abs(y1 - y2)
        val d = Math.abs(z1 - z2)

        return w * h * d
    }

    fun toImmutable(): CompletedSelection? {
        if (!isComplete())
            return null

        return CompletedSelection(firstChunk!!, secondChunk!!)
    }
}

class CompletedSelection(val first: ChunkPos, val second: ChunkPos) {
    fun countChunks(): Int {
        val w = Math.abs(first.x - second.x) + 1
        val h = Math.abs(first.z - second.z) + 1

        return w * h
    }

    fun getAllChunks(): List<ChunkPos> {
        val xMin = Math.min(first.x, second.x)
        val xMax = Math.max(first.x, second.x)
        val zMin = Math.min(first.z, second.z)
        val zMax = Math.max(first.z, second.z)

        val chunks = ArrayList<ChunkPos>(this.countChunks())

        for (x in xMin..xMax) {
            for (z in zMin..zMax) {
                chunks.add(ChunkPos(x, z))
            }
        }

        return chunks
    }
}
