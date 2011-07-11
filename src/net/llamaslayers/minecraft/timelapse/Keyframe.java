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

	private Keyframe(byte[] blocks, byte[][] secondary) {
		this.blocks = new byte[blocks.length];
		System.arraycopy(blocks, 0, this.blocks, 0, blocks.length);
		this.secondary = new byte[secondary.length][secondary[0].length];
		for (int i = 0; i < secondary.length; i++) {
			System.arraycopy(secondary[i], 0, this.secondary[i], 0,
					secondary[i].length);
		}
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
						} else if (getNibble(
								secondary[i / 32768][i % 32768 / 2], i % 2) != snapshot
								.getBlockData(x, y, z)) {
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

	private byte getNibble(byte b, int position) {
		if (position == 0)
			return (byte) (b >>> 4);
		return (byte) (b & 0xf);
	}

	public byte[] getBlockData() {
		return blocks;
	}

	public byte[][] getSecondaryData() {
		return secondary;
	}

	@Override
	public Keyframe clone() {
		return new Keyframe(blocks, secondary);
	}
}
