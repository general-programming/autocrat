package gq.genprog.autocrat.integration

import gq.genprog.autocrat.modules.claims.PlayerSelection
import io.netty.buffer.Unpooled
import net.minecraft.entity.ai.attributes.RangedAttribute
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.NetworkRegistry

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class WorldEditCUIHook {
    val autocratSelectionUid = "bbdd0c77-aa3b-452c-9feb-52af99f9da89"
    val hasCuiMarker = RangedAttribute( "wecui.marker", 0.0, 0.0, 4.0)

    val cuiChannel = NetworkRegistry.newEventChannel(
            ResourceLocation("worldedit:cui"), { "1" }, { _ -> true }, { _ -> true })

    init {
        cuiChannel.registerObject(this)
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
        return true
    }

    fun ServerPlayerEntity.sendCuiMsg(text: String) {
//        if (isCuiEnabled(this))
//            cuiChannel.send(createPacket(text), this)
    }

    fun createPacket(text: String) {
        val byteArray = text.toByteArray(Charsets.UTF_8)
        val byteBuf = Unpooled.wrappedBuffer(byteArray)
        val packetBuf = PacketBuffer(byteBuf)

//        return FMLProxyPacket(packetBuf, channelName)
    }

    @SubscribeEvent fun onCustomPacket(ev: NetworkEvent.ServerCustomPayloadEvent) {
        val source = ev.source.get()
        val player = source.sender!!
        val text = ev.payload.toString(Charsets.UTF_8)

        if (isCuiEnabled(player)) return

        val (type, value) = text.split('|')

//        player.attributeMap.registerAttribute(hasCuiMarker).also { it.baseValue = value.toInt().toDouble() }
    }
}
