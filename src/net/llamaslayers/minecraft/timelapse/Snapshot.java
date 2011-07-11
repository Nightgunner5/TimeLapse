package net.llamaslayers.minecraft.timelapse;

import org.bukkit.Chunk;

public class Snapshot {
	private Update lastUpdate;
	private final Keyframe currentState;
	private final Chunk[] chunks;

	public Snapshot(Chunk[] chunks) {
		this.chunks = chunks;
		currentState = new Keyframe(chunks.length);
	}

	public void update() {
		lastUpdate = currentState.update(chunks);
	}

	public Keyframe getKeyframe() {
		return currentState;
	}

	public Update getUpdate() {
		return lastUpdate;
	}
}
