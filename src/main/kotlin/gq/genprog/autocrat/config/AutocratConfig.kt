package gq.genprog.autocrat.config

import com.electronwill.nightconfig.core.UnmodifiableConfig
import net.minecraftforge.common.ForgeConfigSpec

object AutocratConfig {
    val spec = ForgeConfigSpec.Builder().apply {
        comment("Controls which Autocrat modules are enabled.",
                "Certain modules may force-disable themselves if they detect a mod with conflicting functionality.")
        push("modules").apply {
            comment("Enable the claims modules (factions, claims)")
            define("claims", true)
            
            comment("Enable the fancy names module (auto colour names, nicknames)")
            define("fancyNames", true)

            comment("Enable the sleep-vote module. Disables itself if Quark or Morpheus are installed.")
            define("sleepVote", true)

            comment("Enable the admin module (mod-mode)")
            define("admin", true)

            comment("Enables the simple home module (ender pearl at feet)")
            define("simpleHome", true)

            comment("Enables the command-based homes module (/home, /sethome). [WIP]")
            define("commandHome", false)
        }.pop()

        push("general").apply {
            comment("List of disabled commands by primary alias.")
            defineList("disabledCommands", listOf<String>()) { _ -> true }
        }.pop()

        push("claims").apply {
            comment("Maximum number of chunks a group can claim.")
            defineInRange("maxClaimedChunks", 50, 0, 1024)
        }.pop()

        push("sleep").apply {
            comment("Controls the percentage of sleeping players required to skip to day.")
            defineInRange("threshold", 40, 0, 100)
        }.pop()
    }.build()

    fun get() = ConfigAccess(spec.values)

    class ConfigAccess(val cfg: UnmodifiableConfig) {
        operator fun <T> get(path: String): T = cfg.get<ForgeConfigSpec.ConfigValue<T>>(path).get()
        fun getInt(path: String): Int = cfg.getInt(path)
    }

    fun <T> get(path: String): T = get()[path]
}