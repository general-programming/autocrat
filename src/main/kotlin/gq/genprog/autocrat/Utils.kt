package gq.genprog.autocrat

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.IContainerListener
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.concurrent.CompletableFuture

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
            container.inventory[i] = copy

            for (receiver in getContainerListeners(container)) {
                receiver.sendSlotContents(container, i, copy)
            }
        }
    }

    fun sendHeldContents(player: PlayerEntity) {
        val i = player.inventory.currentItem
        val stack = player.container.getSlot(i).stack

        for (receiver in getContainerListeners(player.container)) {
            receiver.sendSlotContents(player.container, i, stack)
        }
    }
}

class CompletableTask(val runnable: () -> Unit) {
    val future = CompletableFuture<Unit>()

    fun run() {
        try {
            future.complete(runnable())
        } catch (ex: Throwable) {
            future.completeExceptionally(ex)
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
