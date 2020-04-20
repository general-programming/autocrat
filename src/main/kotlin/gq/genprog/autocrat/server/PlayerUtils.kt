package gq.genprog.autocrat.server

import gq.genprog.autocrat.modules.ChoicesModule
import net.minecraft.command.ICommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.management.PlayerList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
interface IController {
    fun chat(msg: String, color: TextFormatting = TextFormatting.GOLD): IController
    fun chat(block: MessageBuilder.() -> Unit)
    fun err(msg: String) {
        this.chat(msg, TextFormatting.RED)
    }

    fun warn(msg: String) {
        this.chat(msg, TextFormatting.YELLOW)
    }

    fun success(msg: String) {
        this.chat(msg, TextFormatting.GREEN)
    }
}

abstract class AbstractController: IController {
    override fun chat(msg: String, color: TextFormatting): IController {
        val text = StringTextComponent(msg).also {
            it.style = Style().setColor(color)
        }

        this.sendMessage(text)
        return this
    }

    override fun chat(block: MessageBuilder.() -> Unit) {
        val builder = MessageBuilder()

        block.invoke(builder)
        this.sendMessage(builder.parent)
    }

    abstract fun sendMessage(component: ITextComponent)
}

class CommandSenderController(val sender: ICommandSource): AbstractController() {
    override fun sendMessage(component: ITextComponent) {
        sender.sendMessage(component)
    }
}

class MessageBuilder {
    val parent = StringTextComponent("")

    fun color(color: TextFormatting) {
        parent.style.color = color
    }

    fun style(block: Style.() -> Unit) {
        block.invoke(parent.style)
    }

    fun append(text: String) {
        parent.appendText(text + '\n')
    }

    fun append(text: String, color: TextFormatting) {
        val component = StringTextComponent(text + '\n')
        component.style.color = color

        parent.appendSibling(component)
    }

    fun last(text: String) {
        parent.appendText(text)
    }
}

fun PlayerEntity.controller(): CommandSenderController {
    return CommandSenderController(this)
}

fun PlayerEvent.controller(): CommandSenderController {
    return CommandSenderController(this.player)
}

fun PlayerList.controller(): IController {
    return object : AbstractController() {
        override fun sendMessage(component: ITextComponent) {
            this@controller.sendMessage(component, false)
        }
    }
}

fun PlayerEntity.choice(cb: (choice: Boolean) -> Unit) {
    ChoicesModule.awaitChoice(this.uniqueID, cb)
}

fun PlayerEntity.resolveOverworld(): ServerWorld {
    return server!!.getWorld(DimensionType.OVERWORLD)
}