package gq.genprog.autocrat.modules.claims.groups

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
enum class AccessMode(val id: Byte) {
    PUBLIC(1),
    RESTRICTED(2),
    PRIVATE(3);

    fun canForeignBreakBlocks(): Boolean {
        return this == PUBLIC
    }

    fun canForeignRightClick(): Boolean {
        return this == PUBLIC || this == RESTRICTED
    }

    companion object {
        fun fromId(id: Byte): AccessMode {
            return when (id.toInt()) {
                1 -> PUBLIC
                2 -> RESTRICTED
                3 -> PRIVATE
                else -> PRIVATE
            }
        }
    }
}