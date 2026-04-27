package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ChunkProviderClient implements IChunkProvider {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The completely empty chunk used by ChunkProviderClient when chunkMapping doesn't contain the requested
	 * coordinates.
	 */
	private final Chunk blankChunk;
	private final Long2ObjectMap<Chunk> chunkMapping = new Long2ObjectOpenHashMap<Chunk>(8192) {
		protected void rehash(int p_rehash_1_) {

			if (p_rehash_1_ > key.length) {
				super.rehash(p_rehash_1_);
			}
		}
	};

	/**
	 * Reference to the World object.
	 */
	private final World world;

	public ChunkProviderClient(World worldIn) {

		blankChunk = new EmptyChunk(worldIn, 0, 0);
		world = worldIn;
	}

	/**
	 * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
	 * to false
	 */
	public void unloadChunk(int x, int z) {

		Chunk chunk = provideChunk(x, z);

		if (!chunk.isEmpty()) {
			chunk.onUnload();
		}

		chunkMapping.remove(ChunkPos.asLong(x, z));
	}

	@Nullable
	public Chunk getLoadedChunk(int x, int z) {

		return chunkMapping.get(ChunkPos.asLong(x, z));
	}

	/**
	 * loads or generates the chunk at the chunk location specified
	 */
	public Chunk loadChunk(int chunkX, int chunkZ) {

		Chunk chunk = new Chunk(world, chunkX, chunkZ);
		chunkMapping.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
		chunk.markLoaded(true);
		return chunk;
	}

	public Chunk provideChunk(int x, int z) {

		return MoreObjects.firstNonNull(getLoadedChunk(x, z), blankChunk);
	}

	/**
	 * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
	 */
	public boolean tick() {

		long i = System.currentTimeMillis();
		ObjectIterator objectiterator = chunkMapping.values().iterator();

		while (objectiterator.hasNext()) {
			Chunk chunk = (Chunk) objectiterator.next();
			chunk.onTick(System.currentTimeMillis() - i > 5L);
		}

		if (System.currentTimeMillis() - i > 100L) {
			LOGGER.info("Warning: Clientside chunk ticking took {} ms", System.currentTimeMillis() - i);
		}

		return false;
	}

	/**
	 * Converts the instance data to a readable string.
	 */
	public String makeString() {

		return "MultiplayerChunkCache: " + chunkMapping.size() + ", " + chunkMapping.size();
	}

	public boolean isChunkGeneratedAt(int x, int z) {

		return chunkMapping.containsKey(ChunkPos.asLong(x, z));
	}

}
