package gq.genprog.autocrat.integration

import gq.genprog.autocrat.integration.providers.BackupProvider
import io.github.hedgehog1029.frame.dispatcher.ArgumentTransformer
import io.github.hedgehog1029.frame.util.IBindingProvider

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class CustomBindings: IBindingProvider {
    override fun configure(transformer: ArgumentTransformer) {
        transformer.bind(BackupsHook.Backup::class.java, BackupProvider())
    }
}