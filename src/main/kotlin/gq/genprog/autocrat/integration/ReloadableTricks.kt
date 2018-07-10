package gq.genprog.autocrat.integration

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.helpers.Values

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
object ReloadableTricks {
    fun reload() {
        Values.keepAliveTimeout = AutocratConfig.keepAliveTimeout.toLong() * 1000L
    }
}