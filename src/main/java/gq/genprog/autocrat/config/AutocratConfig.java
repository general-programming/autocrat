package gq.genprog.autocrat.config;

import net.minecraftforge.common.config.Config;

import static gq.genprog.autocrat.MetaKt.MOD_ID;

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Config(modid = MOD_ID)
public class AutocratConfig {
	public static ClaimsModule claims = new ClaimsModule();
	public static SleepModule sleepVote = new SleepModule();
	public static BackupsModule backups = new BackupsModule();

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

	@Config.Comment("Value in seconds of the keep-alive timeout.")
	@Config.RangeInt(min = 15, max = 300)
	public static int keepAliveTimeout = 30;
}
