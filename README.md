# autocrat

A modular administration toolset for Minecraft servers.

## Modules

* Claims (user groups, in-world selection, WECUI support)
* Sleep Vote (player threshold sleep)
* Fancy Names (nicknames, random name color on join)
* Admin (mod-mode for administrators)
* Simple homes (throw enderpearl at feet to return to your bed)
* Integration with [silly511/Backups-Mod](https://github.com/Silly511/Backups-Mod)

### Outstanding 1.12 Bugs

This branch (`1.12`) has a few outstanding issues:

* SimpleHome may not work across dimensions (waiting for 1.14 backport)
* Mod-mode's return-to-position may not work across dimensions (waiting for 1.14 backport)
* Command-based homes module is unfinished

## Building

With the ForgeGradle environment:

```
gradle setupCiWorkspace
gradle reobfJar
```

## Module details

### Admin / Mod-mode

"Mod-mode" is a tool which helps server administrators seperate their
survival gameplay profile and their moderation workflow.

By executing `/mod`, an administrator can enter "mod mode". This
command switches to creative, flips their inventory to their last
admin inventory, and starts logging commands. From here the
moderator can freely teleport using standard commands to wherever
requires their attention. When the moderator has finished, they can
simply execute `/done`, which returns them to their original position,
back in survival, and with their previous survival inventory intact.

### Claims

Claims are selected using the golden shovel and are linked to groups.
Groups may be created with `/group create Group Name`; check the other
subcommands of `/group` to invite members. Claims may be established
using the `/claim` command. You can also unclaim the current chunk
by using `/unclaim`.
