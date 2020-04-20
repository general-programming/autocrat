package gq.genprog.autocrat.modules

import gq.genprog.autocrat.modules.claims.ClaimWorldStorage
import gq.genprog.autocrat.modules.claims.Faction
import gq.genprog.autocrat.modules.claims.groups.AccessMode
import gq.genprog.autocrat.server.controller
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Group
import io.github.hedgehog1029.frame.annotation.Sender
import io.github.hedgehog1029.frame.annotation.Text
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TextFormatting

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Group("groups", "group")
class GroupModule {
    val slugRegex = Regex("[^a-z0-9\\-]")

    @Command(aliases = ["create"], description = "Create a new group.")
    fun createGroup(@Sender sender: ServerPlayerEntity, @Text name: String) {
        val storage = ClaimWorldStorage.get(sender.serverWorld)

        if (storage.getGroupOfPlayer(sender.uniqueID) != null) {
            sender.controller().chat("You're already in a group!", TextFormatting.RED)
            return
        }

        val id = name.toLowerCase().replace(' ', '-').replace(slugRegex, "")
        if (storage.groups.containsKey(id)) {
            sender.controller().chat("A group with that name already exists!", TextFormatting.RED)
            return
        }

        val group = Faction.Builder().also {
            it.id = id
            it.name = name
            it.owner = sender.uniqueID
            it.access = AccessMode.PRIVATE
        }.build()

        storage.groups[id] = group
        sender.controller().chat("Created a new group called $name.", TextFormatting.GREEN)
        storage.markDirty()
    }

    @Command(aliases = ["invite"], description = "Invite a new player to your group.")
    fun invitePlayer(@Sender sender: ServerPlayerEntity, target: ServerPlayerEntity) {
        val storage = ClaimWorldStorage.get(sender.serverWorld)
        val group = storage.getGroupOfPlayer(sender.uniqueID)

        if (group == null) {
            sender.controller().chat("You're not in a group! Use /group create <name> first.", TextFormatting.RED)
            return
        }

        if (!group.isOwner(sender)) {
            sender.controller().chat("You don't own your group!", TextFormatting.RED)
            return
        }

        group.members.add(target.uniqueID)
        sender.controller().chat("Added user ${target.name} to your group.")
        storage.markDirty()
    }

    @Command(aliases = ["uninvite"], description = "Remove a player from your group.")
    fun removePlayer(@Sender sender: ServerPlayerEntity, target: ServerPlayerEntity) {
        val storage = ClaimWorldStorage.get(sender.serverWorld)
        val group = storage.getGroupOfPlayer(sender.uniqueID)

        if (group == null) {
            sender.controller().chat("You're not in a group!", TextFormatting.RED)
            return
        }

        if (!group.isOwner(sender)) {
            sender.controller().chat("You don't own your group!", TextFormatting.RED)
            return
        }

        group.members.remove(target.uniqueID)
        sender.controller().chat("Removed user ${target.name} from your group.")
        storage.markDirty()
    }

    @Command(aliases = ["rename"], description = "Rename your group.")
    fun renameGroup(@Sender sender: ServerPlayerEntity, @Text name: String) {
        val storage = ClaimWorldStorage.get(sender.serverWorld)
        val group = storage.getGroupOfPlayer(sender.uniqueID)

        if (group == null) {
            sender.controller().chat("You're not in a group! Use /group create <name> first.", TextFormatting.RED)
            return
        }

        if (!group.isOwner(sender)) {
            sender.controller().chat("You don't own your group!", TextFormatting.RED)
            return
        }

        group.name = name
        sender.controller().chat("Renamed your group to $name.")
        storage.markDirty()
    }

    @Command(aliases = ["info"], description = "Get information about your group.")
    fun groupInfo(@Sender sender: ServerPlayerEntity) {
        val storage = ClaimWorldStorage.get(sender.serverWorld)
        val group = storage.getGroupOfPlayer(sender.uniqueID)

        if (group == null) {
            sender.controller().chat("You're not in a group! Use /group create <name> first.", TextFormatting.RED)
            return
        }

        val owner = sender.mcServer.playerList.getPlayerByUUID(group.owner)
        sender.controller().chat {
            color(TextFormatting.GREEN)
            append("Group ${group.id} info:", TextFormatting.GOLD)
            append("Name: ${group.name} / Owner: ${owner.name}")
            last("${group.members.size} members / in ${group.access} mode")
        }
    }
}