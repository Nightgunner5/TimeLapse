package net.llamaslayers.minecraft.timelapse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class Camera implements Runnable {
	private final int keyframeEvery;
	public final int taskID;
	private int ticks = 0;
	private final boolean isRecording;
	private final Snapshot snapshot;
	private final ObjectOutputStream output;
	private String recordingId;

	public Camera(World world, int chunkX, int chunkZ, int chunkW, int chunkL,
			int ticks, int keyframeEvery) {
		this.keyframeEvery = keyframeEvery;
		isRecording = true;
		Chunk[] chunks = new Chunk[chunkW * chunkL];
		int i = 0;
		for (int x = 0; x < chunkW; x++) {
			for (int z = 0; z < chunkL; z++) {
				chunks[i++] = world.getChunkAt(x + chunkX, z + chunkZ);
			}
		}
		snapshot = new Snapshot(chunks);

		output = Camera.getFilm(world, this);

		taskID = Bukkit.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(TimeLapse.instance, this, 0, ticks);

		try {
			output.writeInt(1); // Version number
			output.writeLong(System.currentTimeMillis());
			output.writeObject(world.getName());
			output.writeInt(chunkX);
			output.writeInt(chunkZ);
			output.writeInt(chunkW);
			output.writeInt(chunkL);
			output.writeInt(ticks);
			output.writeInt(keyframeEvery);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			snapshot.update();
			try {
				if (ticks++ % keyframeEvery == 0) {
					output.writeObject(snapshot.getKeyframe());
				} else {
					output.writeObject(snapshot.getUpdate());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized void stop() {
		Bukkit.getServer().getScheduler().cancelTask(taskID);
		try {
			output.flush();
			output.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean isRecording() {
		return isRecording;
	}

	public int getTicks() {
		return ticks;
	}

	public String getRecordingId() {
		return recordingId;
	}

	private static ObjectOutputStream getFilm(World world, Camera self) {
		File filmFolder = new File(TimeLapse.instance.getDataFolder(),
				world.getName());
		filmFolder.mkdirs();
		Calendar today = Calendar.getInstance();
		String filename = today.get(Calendar.YEAR) + "-"
				+ today.get(Calendar.MONTH) + "-" + today.get(Calendar.DATE);
		File film = new File(filmFolder, filename + ".dat");
		int identifier = 0;
		while (film.exists()) {
			identifier++;
			film = new File(filmFolder, filename + "-" + identifier + ".dat");
		}

		self.recordingId = film.getName().replace(".dat", "");

		try {
			return new ObjectOutputStream(new FileOutputStream(film));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
