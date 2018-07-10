package gq.genprog.autocrat.modules

import gq.genprog.autocrat.integration.ReloadableTricks
import io.github.hedgehog1029.frame.annotation.Command
import io.github.hedgehog1029.frame.annotation.Group

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class TricksModule {
    @Command(aliases = ["reload"], description = "Reload tricks.", permission = "autocrat.reload")
    @Group("autocrat", "auto")
    fun reload() {
        ReloadableTricks.reload()
    }
}