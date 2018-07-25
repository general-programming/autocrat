package gq.genprog.autocrat

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
object InventoryUtils {
    fun getContainerListeners(container: Container): List<IContainerListener> {
        val field = Container::class.java.getDeclaredField("listeners")
        field.isAccessible = true

        return field.get(container) as List<IContainerListener>
    }

    fun sendAllContents(container: Container) {
        for (i in 0 until container.inventorySlots.size) {
            val stack = container.inventorySlots.get(i).stack
            val copy = stack.copy()
            container.inventoryItemStacks[i] = copy

            for (receiver in getContainerListeners(container)) {
                receiver.sendSlotContents(container, i, copy)
            }
        }
    }

    fun sendHeldContents(player: EntityPlayer) {
        val i = player.inventory.currentItem
        val stack = player.inventoryContainer.getSlot(i).stack

        for (receiver in getContainerListeners(player.inventoryContainer)) {
            receiver.sendSlotContents(player.inventoryContainer, i, stack)
        }
    }
}

val random = Random()
fun <T> randomFrom(array: Array<T>): T {
    return array[random.nextInt(array.size)]
}

fun BlockPos.joinToString(seperator: CharSequence = ", "): String {
    return arrayOf(x, y, z).joinToString(seperator)
}

fun BlockPos.toDoubleVec(): Vec3d {
    return Vec3d(this)
}
