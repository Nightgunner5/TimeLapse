package net.llamaslayers.minecraft.timelapse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import net.llamaslayers.minecraft.timelapse.Update.UpdateData;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class Projector extends Thread {
	private double speed = 1;
	private final ObjectInputStream input;
	private final Player player;
	private boolean playing = true;
	private final PlaybackCommand callback;
	public final long recordingStartedAt;
	public final String world;
	private final Chunk[] chunks;
	public final int tickInterval;
	public final int keyframeInterval;

	public Projector(Player player, File recording, PlaybackCommand callback)
			throws IOException {
		super("TimeLapse Projector for " + player.getName());

		input = new ObjectInputStream(new FileInputStream(recording));
		this.player = player;
		this.callback = callback;

		switch (input.readInt()) {
		case 1: {
			recordingStartedAt = input.readLong();
			try {
				world = (String) input.readObject();
			} catch (ClassNotFoundException ex) {
				throw new IOException(ex);
			}
			int chunkX = input.readInt();
			int chunkZ = input.readInt();
			int chunkW = input.readInt();
			int chunkL = input.readInt();
			tickInterval = input.readInt();
			keyframeInterval = input.readInt();

			chunks = new Chunk[chunkW * chunkL];
			int i = 0;
			for (int x = 0; x < chunkW; x++) {
				for (int z = 0; z < chunkL; z++) {
					chunks[i++] = Bukkit.getServer().getWorld(world)
							.getChunkAt(x + chunkX, z + chunkZ);
				}
			}

			break;
		}
		default:
			throw new IOException("Unknown version ID");
		}
		start();
	}

	public void setSpeed(double multiplier) {
		if (multiplier <= 0)
			throw new IllegalArgumentException();
		speed = multiplier;
	}

	@Override
	public void run() {
		while (playing) {
			try {
				Thread.sleep((long) (tickInterval * 50 / speed));
			} catch (InterruptedException ex) {
			}

			try {
				Object frame = input.readObject();
				if (frame instanceof Keyframe) {
					byte[] blocks = ((Keyframe) frame).getBlockData();
					byte[][] secondary = ((Keyframe) frame).getSecondaryData();
					byte[] chunkData = new byte[81920];
					for (int i = 0; i < chunks.length; i++) {
						System.arraycopy(blocks, 32768 * i, chunkData, 0, 32768);
						System.arraycopy(secondary[i], 0, chunkData, 32768,
								49152);
						player.sendChunkChange(chunks[i].getBlock(0, 0, 0)
								.getLocation(), 16, 128, 16, chunkData);
					}
				} else if (frame instanceof Update) {
					for (UpdateData data : (Update) frame) {
						player.sendBlockChange(data.getLocation(chunks),
								data.block,
								data.data == -1 ? data.getLocation(chunks)
										.getBlock().getData() : data.data);
					}
				} else
					throw new IOException();
			} catch (IOException ex) {
				ex.printStackTrace();
				playing = false;
				callback.finishedPlayback(player);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				playing = false;
				callback.finishedPlayback(player);
			}
		}
	}

	public boolean isPlaying() {
		return playing;
	}

	public void stopPlayback() {
		playing = false;
	}
}
