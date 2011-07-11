package net.llamaslayers.minecraft.timelapse;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecordCommand implements CommandExecutor {
	private final HashMap<Player, Camera> cameras = new HashMap<Player, Camera>();

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("This command can only be used by OPs.");
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}
		Player player = (Player) sender;

		if (args.length == 0)
			return false;
		if (args[0].equalsIgnoreCase("stop")) {
			if (cameras.containsKey(player)) {
				Camera camera = cameras.remove(player);
				camera.stop();
				player.sendMessage("Recording " + camera.getRecordingId()
						+ " stopped with " + camera.getTicks() + " frames.");
			} else {
				player.sendMessage("You were not recording.");
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("start")) {
			int radius = 1;
			if (args.length > 1) {
				try {
					radius = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					return false;
				}
				if (radius < 1)
					return false;
			}
			int ticks = 5;
			if (args.length > 2) {
				try {
					ticks = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex) {
					return false;
				}
				if (ticks < 1)
					return false;
			}
			int keyframeEvery = 240;
			if (args.length > 3) {
				try {
					keyframeEvery = Integer.parseInt(args[3]);
				} catch (NumberFormatException ex) {
					return false;
				}
				if (keyframeEvery < 1)
					return false;
			}

			int playerchunkX = player.getLocation().getBlockX() / 16;
			int playerchunkZ = player.getLocation().getBlockZ() / 16;
			Camera camera = new Camera(player.getWorld(), playerchunkX - radius
					+ 1, playerchunkZ - radius + 1, radius * 2 - 1,
					radius * 2 - 1, ticks, keyframeEvery);
			cameras.put(player, camera);
			player.sendMessage("Recording started with ID "
					+ camera.getRecordingId() + ".");
			return true;
		}

		return false;
	}

	public void stopAllRecordings() {
		for (Camera camera : cameras.values()) {
			camera.stop();
		}
		cameras.clear();
	}
}
