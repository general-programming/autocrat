package gq.genprog.autocrat.integration

import gq.genprog.autocrat.modules.claims.PlayerSelection
import io.netty.buffer.Unpooled
import net.minecraft.entity.ai.attributes.RangedAttribute
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class WorldEditCUIHook {
    val channelName = "WECUI"
    val autocratSelectionUid = "bbdd0c77-aa3b-452c-9feb-52af99f9da89"
    val hasCuiMarker = RangedAttribute(null, "wecui.marker", 0.0, 0.0, 4.0)

    val cuiChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName)

    init {
        cuiChannel.register(this)
    }

    fun startCuboidSelection(player: ServerPlayerEntity) {
        player.sendCuiMsg("+s|cuboid|$autocratSelectionUid")
    }

    fun clearSelection(player: ServerPlayerEntity) {
        player.sendCuiMsg("+s|clear|$autocratSelectionUid")
    }

    fun sendPoint(player: ServerPlayerEntity, selection: PlayerSelection) {
        if (!isCuiEnabled(player)) return

        val area = selection.currentArea()

        selection.first?.apply {
            player.sendCuiMsg("+p|0|$x|$y|$z|$area")
        }

        selection.second?.apply {
            player.sendCuiMsg("+p|1|$x|$y|$z|$area")
        }

        player.sendCuiMsg("+grid|4.0|cull")
        player.sendCuiMsg("+col|#008080|#00CED1|#FF8C00|#191970") // color format: border, grid, primary, secondary
    }

    fun sendChunkBorders(player: ServerPlayerEntity, first: ChunkPos, second: ChunkPos) {
        player.sendCuiMsg("+s|cuboid|$autocratSelectionUid")
        player.sendCuiMsg("+p|0|${first.x}|0|${first.z}|0")
        player.sendCuiMsg("+p|1|${second.x}|256|${second.z}|0")
        player.sendCuiMsg("+grid|0.0|cull")
        player.sendCuiMsg("+col|#DAA520|#FF4500|#00000000|#00000000")
    }

    fun isCuiEnabled(player: ServerPlayerEntity): Boolean {
        return player.attributeMap.getAttributeInstanceByName(hasCuiMarker.name) != null
    }

    fun ServerPlayerEntity.sendCuiMsg(text: String) {
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

            player.attributeMap.registerAttribute(hasCuiMarker).also { it.baseValue = value.toInt().toDouble() }
        }
    }
}