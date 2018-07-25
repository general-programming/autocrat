package gq.genprog.autocrat.integration

import gq.genprog.autocrat.modules.claims.PlayerSelection
import io.netty.buffer.Unpooled
import net.minecraft.entity.ai.attributes.RangedAttribute
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class WorldEditCUIHook {
    val channelName = "WECUI"
    val hasCuiMarker = RangedAttribute(null, "wecui.marker", 0.0, 0.0, 4.0)

    val cuiChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName)

    init {
        cuiChannel.register(this)
    }

    fun startCuboidSelection(player: EntityPlayerMP) {
        player.sendCuiMsg("s|cuboid")
    }

    fun clearSelection(player: EntityPlayerMP) {
        player.sendCuiMsg("s")
    }

    fun sendPoint(player: EntityPlayerMP, selection: PlayerSelection) {
        if (!isCuiEnabled(player)) return

        val area = selection.currentArea()

        selection.first?.apply {
            player.sendCuiMsg("p|0|$x|$y|$z|$area")
        }

        selection.second?.apply {
            player.sendCuiMsg("p|1|$x|$y|$z|$area")
        }

        player.sendCuiMsg("grid|4.0")
        player.sendCuiMsg("col|#|#00CED1|#FF8C00|#")
    }

    fun isCuiEnabled(player: EntityPlayerMP): Boolean {
        return player.attributeMap.getAttributeInstanceByName(hasCuiMarker.name) != null
    }

    fun EntityPlayerMP.sendCuiMsg(text: String) {
        if (isCuiEnabled(this))
            cuiChannel.sendTo(createPacket(text), this)
    }

    fun createPacket(text: String): FMLProxyPacket {
        val byteArray = text.toByteArray(Charsets.UTF_8)
        val byteBuf = Unpooled.wrappedBuffer(byteArray)
        val packetBuf = PacketBuffer(byteBuf)

        return FMLProxyPacket(packetBuf, channelName)
    }

    @SubscribeEvent fun onCustomPacket(ev: FMLNetworkEvent.ServerCustomPacketEvent) {
        if (ev.packet.channel() == channelName) {
            val player = (ev.handler as NetHandlerPlayServer).player
            val text = ev.packet.payload().toString(Charsets.UTF_8)

            if (isCuiEnabled(player)) return

            val (type, value) = text.split('|')

            println("WECUI version $value")
            player.attributeMap.registerAttribute(hasCuiMarker).also { it.baseValue = value.toInt().toDouble() }
        }
    }
}