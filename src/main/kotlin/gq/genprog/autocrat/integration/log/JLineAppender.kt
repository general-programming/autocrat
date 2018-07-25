package gq.genprog.autocrat.integration.log

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import java.io.Serializable

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Plugin(name = "JLine", category = "Core", elementType = "appender", printObject = false)
class JLineAppender(name: String, filter: Filter?, layout: Layout<out Serializable>?, subName: String) : AbstractAppender(name, filter, layout) {
    val wrapped = LogManager.getLogger(subName)

    override fun append(event: LogEvent) {
        wrapped.log(event.level, event.marker, event.message, event.thrown)
    }

    companion object {
        @JvmStatic
        @PluginFactory fun createAppender(@PluginAttribute("name") name: String?,
                                          @PluginAttribute("logName") subName: String = "autocrat",
                                          @PluginElement("Layout") layout: Layout<out Serializable>?): JLineAppender? {
            if (name == null)
                return null

            var actualLayout = layout
            if (layout == null)
                actualLayout = PatternLayout.createDefaultLayout()

            return JLineAppender(name, null, actualLayout, subName)
        }
    }
}