package net.llamaslayers.minecraft.timelapse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import net.llamaslayers.minecraft.timelapse.Update.UpdateData;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class Update implements Serializable, Iterable<UpdateData> {
	public static class UpdateData {
		public final int id;
		public final int block;
		public final byte data;

		public UpdateData(int id, byte block, byte data) {
			this.id = id;
			this.block = block < 0 ? 128 - block : block;
			this.data = data;
		}

		public Location getLocation(Chunk[] chunks) {
			Chunk chunk = chunks[id / 32768];
			int loc = id % 32768;
			return new Location(chunk.getWorld(), chunk.getX() * 16
					+ (loc / 2048), loc % 128, chunk.getZ() * 16 + (loc / 128)
					% 16);
		}
	}

	private static final long serialVersionUID = 1L;

	private final HashMap<Integer, Byte> blockChanges = new HashMap<Integer, Byte>();
	private final HashMap<Integer, Byte> blockData = new HashMap<Integer, Byte>();

	public void addBlockChange(int id, byte block, byte data) {
		blockChanges.put(id, block);
		blockData.put(id, data);
	}

	private static class UpdateDataIterator implements Iterator<UpdateData> {
		private final Iterator<Integer> iterator;
		private final HashMap<Integer, Byte> blockChanges;
		private final HashMap<Integer, Byte> blockData;

		public UpdateDataIterator(Iterator<Integer> iterator,
				HashMap<Integer, Byte> blockChanges,
				HashMap<Integer, Byte> blockData) {
			this.iterator = iterator;
			this.blockChanges = blockChanges;
			this.blockData = blockData;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public UpdateData next() {
			int id = iterator.next();
			return new UpdateData(id, blockChanges.get(id),
					blockData.containsKey(id) ? blockData.get(id) : -1);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<UpdateData> iterator() {
		return new UpdateDataIterator(blockChanges.keySet().iterator(),
				blockChanges, blockData);
	}
}
