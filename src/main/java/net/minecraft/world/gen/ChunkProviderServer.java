package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderServer implements IChunkProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<Long> droppedChunksSet = Sets.<Long>newHashSet();
    private final IChunkGenerator chunkGenerator;
    private final IChunkLoader chunkLoader;
    private final Long2ObjectMap<Chunk> id2ChunkMap = new Long2ObjectOpenHashMap<Chunk>(8192);
    private final WorldServer world;

    public ChunkProviderServer(WorldServer worldObjIn, IChunkLoader chunkLoaderIn, IChunkGenerator chunkGeneratorIn)
    {
        world = worldObjIn;
        chunkLoader = chunkLoaderIn;
        chunkGenerator = chunkGeneratorIn;
    }

    public Collection<Chunk> getLoadedChunks()
    {
        return id2ChunkMap.values();
    }

    /**
     * Marks the chunk for unload if the {@link WorldProvider} allows it.
     *  
     * Queueing a chunk for unload does <b>not</b> guarantee that it will be unloaded, as any request for the chunk will
     * unqueue the chunk.
     */
    public void queueUnload(Chunk chunkIn)
    {
        if (world.provider.canDropChunk(chunkIn.x, chunkIn.z))
        {
            droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(chunkIn.x, chunkIn.z)));
            chunkIn.unloadQueued = true;
        }
    }

    /**
     * Marks all chunks for unload
     *  
     * @see #queueUnload(Chunk)
     */
    public void queueUnloadAll()
    {
        ObjectIterator objectiterator = id2ChunkMap.values().iterator();

        while (objectiterator.hasNext())
        {
            Chunk chunk = (Chunk)objectiterator.next();
            queueUnload(chunk);
        }
    }

    @Nullable
    public Chunk getLoadedChunk(int x, int z)
    {
        long i = ChunkPos.asLong(x, z);
        Chunk chunk = (Chunk) id2ChunkMap.get(i);

        if (chunk != null)
        {
            chunk.unloadQueued = false;
        }

        return chunk;
    }

    @Nullable
    public Chunk loadChunk(int x, int z)
    {
        Chunk chunk = getLoadedChunk(x, z);

        if (chunk == null)
        {
            chunk = loadChunkFromFile(x, z);

            if (chunk != null)
            {
                id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
                chunk.onLoad();
                chunk.populate(this, chunkGenerator);
            }
        }

        return chunk;
    }

    public Chunk provideChunk(int x, int z)
    {
        Chunk chunk = loadChunk(x, z);

        if (chunk == null)
        {
            long i = ChunkPos.asLong(x, z);

            try
            {
                chunk = chunkGenerator.generateChunk(x, z);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                crashreportcategory.addCrashSection("Location", String.format("%d,%d", x, z));
                crashreportcategory.addCrashSection("Position hash", Long.valueOf(i));
                crashreportcategory.addCrashSection("Generator", chunkGenerator);
                throw new ReportedException(crashreport);
            }

            id2ChunkMap.put(i, chunk);
            chunk.onLoad();
            chunk.populate(this, chunkGenerator);
        }

        return chunk;
    }

    @Nullable
    private Chunk loadChunkFromFile(int x, int z)
    {
        try
        {
            Chunk chunk = chunkLoader.loadChunk(world, x, z);

            if (chunk != null)
            {
                chunk.setLastSaveTime(world.getTotalWorldTime());
                chunkGenerator.recreateStructures(chunk, x, z);
            }

            return chunk;
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't load chunk", (Throwable)exception);
            return null;
        }
    }

    private void saveChunkExtraData(Chunk chunkIn)
    {
        try
        {
            chunkLoader.saveExtraChunkData(world, chunkIn);
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't save entities", (Throwable)exception);
        }
    }

    private void saveChunkData(Chunk chunkIn)
    {
        try
        {
            chunkIn.setLastSaveTime(world.getTotalWorldTime());
            chunkLoader.saveChunk(world, chunkIn);
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Couldn't save chunk", (Throwable)ioexception);
        }
        catch (MinecraftException minecraftexception)
        {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", (Throwable)minecraftexception);
        }
    }

    public boolean saveChunks(boolean all)
    {
        int i = 0;
        List<Chunk> list = Lists.newArrayList(id2ChunkMap.values());

        for (int j = 0; j < list.size(); ++j)
        {
            Chunk chunk = list.get(j);

            if (all)
            {
                saveChunkExtraData(chunk);
            }

            if (chunk.needsSaving(all))
            {
                saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !all)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Flushes all pending chunks fully back to disk
     */
    public void flushToDisk()
    {
        chunkLoader.flush();
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean tick()
    {
        if (!world.disableLevelSaving)
        {
            if (!droppedChunksSet.isEmpty())
            {
                Iterator<Long> iterator = droppedChunksSet.iterator();

                for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove())
                {
                    Long olong = iterator.next();
                    Chunk chunk = (Chunk) id2ChunkMap.get(olong);

                    if (chunk != null && chunk.unloadQueued)
                    {
                        chunk.onUnload();
                        saveChunkData(chunk);
                        saveChunkExtraData(chunk);
                        id2ChunkMap.remove(olong);
                        ++i;
                    }
                }
            }

            chunkLoader.chunkTick();
        }

        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return !world.disableLevelSaving;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "ServerChunkCache: " + id2ChunkMap.size() + " Drop: " + droppedChunksSet.size();
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return chunkGenerator.getPossibleCreatures(creatureType, pos);
    }

    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        return chunkGenerator.getNearestStructurePos(worldIn, structureName, position, findUnexplored);
    }

    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        return chunkGenerator.isInsideStructure(worldIn, structureName, pos);
    }

    public int getLoadedChunkCount()
    {
        return id2ChunkMap.size();
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean chunkExists(int x, int z)
    {
        return id2ChunkMap.containsKey(ChunkPos.asLong(x, z));
    }

    public boolean isChunkGeneratedAt(int x, int z)
    {
        return id2ChunkMap.containsKey(ChunkPos.asLong(x, z)) || chunkLoader.isChunkGeneratedAt(x, z);
    }
}
