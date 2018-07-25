package gq.genprog.autocrat.modules

import gq.genprog.autocrat.server.controller
import gq.genprog.autocrat.toDoubleVec
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SimpleHomesModule: EventListener {
    @SubscribeEvent fun onPlayerPearl(ev: ProjectileImpactEvent.Throwable) {
        if (ev.throwable !is EntityEnderPearl) return
        val thrower = ev.throwable.thrower as? EntityPlayerMP ?: return

        if (thrower.rotationPitch >= 80.0F) {
            val pos = if (thrower.world.getBlockState(thrower.position).block == Blocks.WATER) {
                thrower.world.getTopSolidOrLiquidBlock(thrower.world.spawnPoint).add(0, 1, 0)
            } else {
                EntityPlayer.getBedSpawnLocation(thrower.world, thrower.getBedLocation(), false) ?:
                        return thrower.controller().err("Your bed is missing or obstructed.")
            }.toDoubleVec()

            thrower.setPositionAndUpdate(pos.x, pos.y, pos.z)
            thrower.serverWorld.spawnParticle(EnumParticleTypes.PORTAL, pos.x, pos.y, pos.z, 50, 0.2, 0.01, 0.2, 0.1)
            thrower.serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F)

            ev.isCanceled = true
            ev.throwable.world.removeEntity(ev.throwable)
        }
    }

    @SubscribeEvent fun onPlayerSleep(ev: PlayerSleepInBedEvent) {
        val player = ev.entityPlayer as EntityPlayerMP

        if (player.getBedLocation() == ev.pos) return

        player.setSpawnPoint(ev.pos, false)
        player.controller().chat("You have set your home location to this bed. When you die, you will respawn here.")
    }

    // WIP tpa system

//import com.mojang.authlib.GameProfile
//import net.minecraft.block.material.Material
//import net.minecraft.init.Items
//import net.minecraft.inventory.IContainerListener
//import net.minecraft.inventory.InventoryBasic
//import net.minecraft.item.ItemStack
//import net.minecraft.nbt.NBTTagCompound
//import net.minecraft.nbt.NBTUtil
//import net.minecraft.util.EnumActionResult
//import net.minecraftforge.event.entity.player.PlayerInteractEvent

//    @SubscribeEvent fun onSneakClick(ev: PlayerInteractEvent.RightClickItem) {
//        if (!ev.entity.isSneaking || ev.itemStack.item != Items.ENDER_PEARL) return
//
//        ev.isCanceled = true
//        ev.cancellationResult = EnumActionResult.FAIL
//
//        val player = ev.entityPlayer as EntityPlayerMP
//
//        this.openSkullDisplay(player)
//    }
//
//    fun openSkullDisplay(player: EntityPlayerMP) {
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
//    fun createSkullOf(player: EntityPlayer): SkullWrapper {
//        val profile = player.gameProfile
//        val stack = ItemStack(Items.SKULL, 1, 3).apply {
//            val owner = NBTUtil.writeGameProfile(NBTTagCompound(), profile)
//            val tag = NBTTagCompound().also { it.setTag("SkullOwner", owner) }
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