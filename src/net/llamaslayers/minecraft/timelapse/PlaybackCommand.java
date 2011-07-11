package net.llamaslayers.minecraft.timelapse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaybackCommand implements CommandExecutor {
	private final HashMap<Player, Projector> playing = new HashMap<Player, Projector>();

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}
		Player player = (Player) sender;

		if (args.length == 0)
			return false;

		if (args[0].equalsIgnoreCase("start")) {
			if (playing.containsKey(player)) {
				player.sendMessage("You are already playing back a recording.");
			}

			if (args.length != 2)
				return false;
			File recording = new File(new File(
					TimeLapse.instance.getDataFolder(), player.getWorld()
							.getName()), args[1] + ".dat");

			if (recording.exists()) {
				try {
					playing.put(player, new Projector(player, recording, this));
					player.sendMessage("Starting playback...");
					return true;
				} catch (IOException ex) {
				}
			}
			player.sendMessage("Invalid recording ID. Are you on the wrong world?");
			return false;
		}
		if (args[0].equalsIgnoreCase("speed")) {
			if (!playing.containsKey(player)) {
				player.sendMessage("You are not playing back a recording.");
				return true;
			}

			if (args.length != 2)
				return false;

			try {
				playing.get(player).setSpeed(
						Double.parseDouble(args[1]) / 100.0);
			} catch (NumberFormatException ex) {
				return false;
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}
		if (args[0].equalsIgnoreCase("stop")) {
			if (!playing.containsKey(player)) {
				player.sendMessage("You are not playing back a recording.");
				return true;
			}

			if (args.length != 1)
				return false;

			try {
				playing.remove(player).stopPlayback();
			} catch (NumberFormatException ex) {
				return false;
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}

		return false;
	}

	protected void finishedPlayback(Player player) {
		playing.remove(player);
	}

	public void stopAllPlayback() {
		for (Projector projector : playing.values()) {
			projector.stopPlayback();
		}
		playing.clear();
	}
}
