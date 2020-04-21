package gq.genprog.autocrat.modules

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.server.controller
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import kotlin.math.roundToInt

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class SleepVoteModule: EventListener {
    val sleepingPlayers = arrayListOf<UUID>()

    @SubscribeEvent fun onEnterBed(event: PlayerSleepInBedEvent) {
        if (event.player.world.isDaytime ||
                !event.player.isAlive ||
                event.player.isSleeping)
            return

        sleepingPlayers.add(event.player.uniqueID)

        this.updateVote(event.player.world as ServerWorld)
    }

    @SubscribeEvent fun onExitBed(event: PlayerWakeUpEvent) {
        if (sleepingPlayers.remove(event.player.uniqueID))
            this.updateVote(event.player.world as ServerWorld)
    }

    fun updateVote(world: ServerWorld) {
        val playerList = world.server.playerList

        val current = this.sleepingPlayers.size
        val total = playerList.currentPlayerCount
        val percent = (current.toFloat() / total.toFloat()) * 100
        val threshold = AutocratConfig.get().getInt("sleep.threshold")

        if (percent >= threshold) {
            world.dayTime = 1000
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