package gq.genprog.autocrat.config;

import net.minecraftforge.common.config.Config;

import static gq.genprog.autocrat.MetaKt.MOD_ID;

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Config(modid = MOD_ID)
public class AutocratConfig {
	@Config.Comment({"Controls which Autocrat modules are enabled.",
			"Certain modules may force-disable themselves if they detect a mod with conflicting functionality."})
	public static Modules modules = new Modules();
	public static ClaimsModule claims = new ClaimsModule();
	public static SleepModule sleepVote = new SleepModule();
	public static BackupsModule backups = new BackupsModule();

	public static class Modules {
		@Config.Comment("Enable the claims modules (factions, claims)")
		public boolean claims = true;

		@Config.Comment("Enable the fancy names module (auto colour names, nicknames)")
		public boolean fancyNames = true;

		@Config.Comment("Enable the sleep-vote module. Disables itself if Quark or Morpheus are installed.")
		public boolean sleepVote = true;

		@Config.Comment("Enable the admin module (mod-mode)")
		public boolean admin = true;

		@Config.Comment("Enables the simple home module (ender pearl at feet)")
		public boolean simpleHome = true;

		@Config.Comment("Enables the command-based homes module (/home, /sethome). [WIP]")
		public boolean commandHome = false;
	}

	public static class ClaimsModule {
		@Config.Comment("Maximum number of chunks a group can claim.")
		public int maxClaimedChunks = 50;
	}

	public static class SleepModule {
		@Config.Comment("Controls the percentage of sleeping players required to skip to day.")
		public int threshold = 45;
	}

	public static class BackupsModule {
		@Config.Comment("Set the maximum size that can be restored at once via /bk restore.")
		public int maxRestoreSize = 524288;
	}
}
