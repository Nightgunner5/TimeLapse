package net.llamaslayers.minecraft.timelapse;

import java.io.Serializable;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.craftbukkit.CraftChunk;

public class Keyframe implements Serializable {
	private static final long serialVersionUID = 1L;

	private final byte[] blocks;
	private final byte[][] secondary;

	public Keyframe(int chunks) {
		blocks = new byte[chunks * 32768];
		secondary = new byte[chunks][49152];
	}

	public Update update(Chunk[] chunks) {
		Update update = new Update();
		int i = 0;
		byte[] buffer = new byte[81920];
		for (Chunk chunk : chunks) {
			ChunkSnapshot snapshot = chunk.getChunkSnapshot();
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int y = 0; y < 128; y++) {
						byte block = (byte) snapshot.getBlockTypeId(x, y, z);
						if (blocks[i] != block) {
							blocks[i] = block;
							update.addBlockChange(i, block,
									(byte) snapshot.getBlockData(x, y, z));
						}
						i++;
					}
				}
			}

			CraftChunk internalChunk = (CraftChunk) chunk;
			internalChunk.getHandle().getData(buffer, 0, 0, 0, 16, 128, 16, 0);

			System.arraycopy(buffer, 32768, secondary[i / 32768 - 1], 0, 49152);
		}
		return update;
	}

	public byte[] getBlockData() {
		return blocks;
	}

	public byte[][] getSecondaryData() {
		return secondary;
	}
}
