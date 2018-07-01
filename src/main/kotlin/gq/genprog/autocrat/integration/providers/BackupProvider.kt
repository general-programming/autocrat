package gq.genprog.autocrat.integration.providers

import gq.genprog.autocrat.integration.BackupsHook
import gq.genprog.autocrat.integration.BackupsHookImpl
import io.github.hedgehog1029.frame.dispatcher.arguments.ICommandArguments
import io.github.hedgehog1029.frame.dispatcher.provider.Provider
import net.minecraftforge.fml.common.Loader
import java.lang.reflect.Parameter

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class BackupProvider: Provider<BackupsHook.Backup> {
    override fun provide(args: ICommandArguments, param: Parameter): BackupsHook.Backup? {
        if (!args.hasNext()) return null

        return BackupsHook.Backup(args.next())
    }

    override fun getSuggestions(partial: String): MutableList<String> {
        return if (Loader.isModLoaded("backups"))
            BackupsHookImpl().completeBackupString(partial)
        else mutableListOf()
    }
}