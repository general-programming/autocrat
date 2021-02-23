package gq.genprog.autocrat.modules

import gq.genprog.autocrat.CompletableTask
import gq.genprog.autocrat.server.controller
import gq.genprog.autocrat.toDoubleVec
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.item.EnderPearlEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.particles.ParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SimpleHomesModule: EventListener {
    val waitingTasks = ArrayDeque<CompletableTask>()

    fun schedule(cb: () -> Unit): CompletableFuture<Unit> {
        val task = CompletableTask(cb)
        waitingTasks.add(task)

        return task.future
    }

    @SubscribeEvent fun onPlayerPearl(ev: ProjectileImpactEvent.Throwable) {
        if (ev.throwable !is EnderPearlEntity) return
        val thrower = ev.throwable.func_234616_v_() as? ServerPlayerEntity ?: return

        if (thrower.rotationPitch >= 80.0F) {
            val overworld = thrower.server.getWorld(World.OVERWORLD)!!

            val posOpt = if (thrower.world.getBlockState(thrower.position).block == Blocks.WATER) {
                val pos = overworld.spawnPoint
                val adjustedPos = overworld.getHeight(Heightmap.Type.WORLD_SURFACE, pos)

                Optional.of(adjustedPos.toDoubleVec())
            } else {
                thrower.bedPosition.flatMap { pos ->
                    val state = overworld.getBlockState(pos)
                    if (state.isBed(overworld, pos, thrower)) {
                        state.getBedSpawnPosition(EntityType.PLAYER, overworld, pos, 0.0f, thrower)
                    } else {
                        Optional.of(pos.toDoubleVec())
                    }
                }
            }

            if (!posOpt.isPresent) {
                return thrower.controller().err("Your bed is missing or obstructed.")
            }

            val pos = posOpt.get()

            ev.throwable.remove()
            ev.isCanceled = true

            if (thrower.world.dimensionKey != World.OVERWORLD) {
                schedule {
                    thrower.teleport(overworld, pos.x, pos.y, pos.z, thrower.rotationYaw, 0F)
                }
            } else {
                thrower.setPositionAndUpdate(pos.x, pos.y, pos.z)
                CompletableFuture.completedFuture(false)
            }.thenRun {
                thrower.serverWorld.spawnParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 50, 0.2, 0.01, 0.2, 0.1)
                thrower.serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F)
            }
        }
    }

    @SubscribeEvent fun onPostTick(ev: TickEvent.WorldTickEvent) {
        if (ev.phase != TickEvent.Phase.END) return

        while (waitingTasks.isNotEmpty()) {
            waitingTasks.pop().run()
        }
    }

    @SubscribeEvent fun onPlayerSleep(ev: PlayerSleepInBedEvent) {
        val player = ev.player as ServerPlayerEntity

        if (player.bedPosition == ev.optionalPos) return

        player.setBedPosition(ev.pos)
        player.controller().chat("You have set your home location to this bed. When you die, you will respawn here.")
    }

    // WIP tpa system

//import com.mojang.authlib.GameProfile
//import net.minecraft.block.material.Material
//import net.minecraft.item.Items
//import net.minecraft.inventory.IContainerListener
//import net.minecraft.inventory.InventoryBasic
//import net.minecraft.item.ItemStack
//import net.minecraft.nbt.CompoundNBT
//import net.minecraft.nbt.NBTUtil
//import net.minecraft.util.EnumActionResult
//import net.minecraftforge.event.entity.player.PlayerInteractEvent

//    @SubscribeEvent fun onSneakClick(ev: PlayerInteractEvent.RightClickItem) {
//        if (!ev.entity.isSneaking || ev.itemStack.item != Items.ENDER_PEARL) return
//
//        ev.isCanceled = true
//        ev.cancellationResult = EnumActionResult.FAIL
//
//        val player = ev.entityPlayer as ServerPlayerEntity
//
//        this.openSkullDisplay(player)
//    }
//
//    fun openSkullDisplay(player: ServerPlayerEntity) {
//        val playerList = player.mcServer.playerList
//        val count = (playerList.currentPlayerCount / 9 + 1) * 9
//        val inv = InventoryBasic("Players", true, count)
//        val skulls = playerList.players.mapNotNull { createSkullOf(player) }
//
//        skulls.forEachIndexed { i, pl -> inv.setInventorySlotContents(i, pl.stack) }
//
//        inv.addInventoryChangeListener {
//            skulls.forEachIndexed { i, pl ->
//                if (it.getStackInSlot(i).count == 0) {
//                    player.inventory.itemStack = ItemStack.EMPTY
//                    player.closeScreen()
//
//                    val target = playerList.getPlayerByUUID(pl.profile.id)
//                    player.setPositionAndUpdate(target.posX, target.posY, target.posZ)
//                }
//            }
//        }
//
//        player.displayGUIChest(inv)
//    }
//
//    fun createSkullOf(player: PlayerEntity): SkullWrapper {
//        val profile = player.gameProfile
//        val stack = ItemStack(Items.SKULL, 1, 3).apply {
//            val owner = NBTUtil.writeGameProfile(CompoundNBT(), profile)
//            val tag = CompoundNBT().also { it.setTag("SkullOwner", owner) }
//
//            this.tagCompound = tag
//            this.item.updateItemStackNBT(tag)
//        }
//
//        return SkullWrapper(profile, stack)
//    }
//
//    data class SkullWrapper(val profile: GameProfile, val stack: ItemStack)
}
