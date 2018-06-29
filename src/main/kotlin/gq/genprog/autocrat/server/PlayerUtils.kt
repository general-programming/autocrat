package gq.genprog.autocrat.server

import gq.genprog.autocrat.modules.ChoicesModule
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.management.PlayerList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
interface IController {
    fun chat(msg: String, color: TextFormatting = TextFormatting.GOLD): IController
    fun chat(block: MessageBuilder.() -> Unit)
}

abstract class AbstractController: IController {
    override fun chat(msg: String, color: TextFormatting): IController {
        val text = TextComponentString(msg).also {
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

class CommandSenderController(val sender: ICommandSender): AbstractController() {
    override fun sendMessage(component: ITextComponent) {
        sender.sendMessage(component)
    }
}

class MessageBuilder {
    val parent = TextComponentString("")

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
        val component = TextComponentString(text + '\n')
        component.style.color = color

        parent.appendSibling(component)
    }

    fun last(text: String) {
        parent.appendText(text)
    }
}

fun EntityPlayer.controller(): CommandSenderController {
    return CommandSenderController(this)
}

fun PlayerEvent.controller(): CommandSenderController {
    return CommandSenderController(this.entityPlayer)
}

fun PlayerList.controller(): IController {
    return object : AbstractController() {
        override fun sendMessage(component: ITextComponent) {
            this@controller.sendMessage(component, false)
        }
    }
}

fun EntityPlayer.choice(cb: (choice: Boolean) -> Unit) {
    ChoicesModule.awaitChoice(this.uniqueID, cb)
}