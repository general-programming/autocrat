package gq.genprog.autocrat.modules

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.server.controller
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.WorldServer
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.roundToInt

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SleepVoteModule: EventListener {
    val sleepingPlayers = arrayListOf<UUID>()

    @SubscribeEvent fun onEnterBed(event: PlayerSleepInBedEvent) {
        if (event.entityPlayer.world.isDaytime ||
                !event.entityPlayer.isEntityAlive ||
                event.entityPlayer.isPlayerSleeping)
            return

        sleepingPlayers.add(event.entityPlayer.uniqueID)

        this.updateVote(event.entityPlayer.world as WorldServer)
    }

    @SubscribeEvent fun onExitBed(event: PlayerWakeUpEvent) {
        if (sleepingPlayers.remove(event.entityPlayer.uniqueID))
            this.updateVote(event.entityPlayer.world as WorldServer)
    }

    fun updateVote(world: WorldServer) {
        val playerList = world.minecraftServer!!.playerList

        val current = this.sleepingPlayers.size
        val total = playerList.currentPlayerCount
        val percent = (current.toFloat() / total.toFloat()) * 100
        val threshold = AutocratConfig.sleepVote.threshold

        if (percent >= threshold) {
            world.worldTime = 1000
            playerList.controller().chat {
                color(TextFormatting.GOLD)
                last("Wakey, wakey, rise and shine!")
            }

            sleepingPlayers.clear()
            return
        }

        val percentStr = percent.roundToInt()
        playerList.controller().chat("$current/$total players are sleeping ($percentStr% / $threshold%)", TextFormatting.GOLD)
    }
}