package net.llamaslayers.minecraft.timelapse;

import org.bukkit.plugin.java.JavaPlugin;

public class TimeLapse extends JavaPlugin {
	protected static TimeLapse instance;
	private RecordCommand record;
	private PlaybackCommand playback;

	@Override
	public void onDisable() {
		record.stopAllRecordings();
		playback.stopAllPlayback();
	}

	@Override
	public void onEnable() {
		instance = this;

		getCommand("record").setExecutor(record = new RecordCommand());
		getCommand("playback").setExecutor(playback = new PlaybackCommand());
	}
}
