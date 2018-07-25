package gq.genprog.autocrat.modules

import gq.genprog.autocrat.InventoryUtils
import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.integration.WorldEditCUIHook
import gq.genprog.autocrat.modules.claims.ClaimWorldStorage
import gq.genprog.autocrat.modules.claims.PlayerSelection
import gq.genprog.autocrat.server.choice
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Sender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.collections.HashMap

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class ClaimsModule: EventListener {
    val selections: HashMap<UUID, PlayerSelection> = HashMap()
    val weHook = WorldEditCUIHook()

    fun getPlayerSelection(player: EntityPlayer): PlayerSelection {
        if (!selections.containsKey(player.uniqueID)) {
            selections[player.uniqueID] = PlayerSelection()
            weHook.startCuboidSelection(player as EntityPlayerMP)
        }

        return selections[player.uniqueID]!!
    }

    @SubscribeEvent fun onLeftClick(event: PlayerInteractEvent.LeftClickBlock) {
        val claims = ClaimWorldStorage.get(event.world)
        val pos = ChunkPos(event.pos)

        if (claims.isClaimed(pos)) {
            val group = claims.getClaimGroup(pos)!!

            if (group.isForeign(event.entityPlayer)) {
                event.isCanceled = !group.access.canForeignBreakBlocks()
                return // foreign users can't claim inside someone else's claim
            }
        }

        if (event.itemStack.item == Items.GOLDEN_SHOVEL) {
            val sel = getPlayerSelection(event.entityPlayer)
            sel.first = event.pos

            event.controller().chat("Set first position to (${event.pos.x}, ${event.pos.y}, ${event.pos.z})")
            event.isCanceled = true
            weHook.sendPoint(event.entityPlayer as EntityPlayerMP, sel)
        }
    }

    @SubscribeEvent fun onRightClick(event: PlayerInteractEvent.RightClickBlock) {
        val claims = ClaimWorldStorage.get(event.world)
        val pos = ChunkPos(event.pos)

        if (event.itemStack.item == Items.STICK) {
            val group = claims.getClaimGroup(pos)

            if (group == null)
                event.controller().chat("This area isn't claimed.", TextFormatting.RED)
            else
                event.controller().chat("This area is claimed by ${group.name}.", TextFormatting.GREEN)
        }

        if (claims.isClaimed(pos)) {
            val group = claims.getClaimGroup(pos)!!

            if (group.isForeign(event.entityPlayer)) {
                event.isCanceled = !group.access.canForeignRightClick()

                if (event.isCanceled)
                    InventoryUtils.sendAllContents(event.entityPlayer.inventoryContainer)
                return
            }
        }

        if (event.itemStack.item == Items.GOLDEN_SHOVEL) {
            val sel = getPlayerSelection(event.entityPlayer)
            sel.second = event.pos

            event.controller().chat("Set second position to (${event.pos.x}, ${event.pos.y}, ${event.pos.z})")
            event.isCanceled = true
            weHook.sendPoint(event.entityPlayer as EntityPlayerMP, sel)
        }
    }

    @SubscribeEvent fun onBlockPlace(event: BlockEvent.PlaceEvent) {
        val claims = ClaimWorldStorage.get(event.world)
        val pos = ChunkPos(event.pos)

        if (claims.isClaimed(pos)) {
            val group = claims.getClaimGroup(pos)!!

            if (group.isForeign(event.player)) {
                event.isCanceled = !group.access.canForeignBreakBlocks()

                if (event.isCanceled)
                    InventoryUtils.sendAllContents(event.player.inventoryContainer)
            }
        }
    }

    @Command(aliases = ["claim"], description = "Claim a selected area.") fun claimArea(@Sender sender: EntityPlayerMP) {
        val ctx = sender.controller()
        val claims = ClaimWorldStorage.get(sender.world)
        val sel = getPlayerSelection(sender).toImmutable()

        if (sel == null) {
            ctx.chat("You need to make a selection first!", TextFormatting.RED)
            return
        }

        val group = claims.getGroupOfPlayer(sender.uniqueID)
        if (group == null) {
            ctx.chat("You need to create a group first! Use /groups create <name> to create one.", TextFormatting.RED)
            return
        }

        val numToClaim = sel.countChunks()
        val totalClaimed = numToClaim + claims.countClaimsOf(group)
        if (totalClaimed > AutocratConfig.claims.maxClaimedChunks) {
            ctx.chat("You can't claim more than 50 total chunks!", TextFormatting.RED)
            return
        }

        ctx.chat {
            color(TextFormatting.YELLOW)
            append("You're claiming $numToClaim chunks.")
            last("Are you sure you want to do this? Use /yes or /no to decide.")
        }

        sender.choice { yes ->
            if (yes) {
                claims.claimChunks(sender.uniqueID, sel.getAllChunks())

                ctx.chat("Claimed $numToClaim chunks.", TextFormatting.GREEN)
                selections.remove(sender.uniqueID)
                weHook.clearSelection(sender)
            } else {
                ctx.chat("Cancelled claim request.")
            }
        }
    }

    @Command(aliases = ["unclaimchunk"], description = "Unclaim the chunk you are standing in.")
    fun unclaimChunk(@Sender sender: EntityPlayerMP) {
        val ctx = sender.controller()
        val claims = ClaimWorldStorage.get(sender.world)
        val pos = ChunkPos(sender.position)

        if (claims.isClaimed(pos)) {
            if (claims.getClaimGroup(pos)!!.isOwner(sender)) {
                claims.unclaimChunksUnchecked(listOf(pos))

                ctx.chat("Unclaimed this chunk.", TextFormatting.GREEN)
            } else {
                ctx.chat("You're not the owner of this claimed chunk!", TextFormatting.RED)
            }
        } else {
            ctx.chat("This chunk isn't claimed!", TextFormatting.RED)
        }
    }
}