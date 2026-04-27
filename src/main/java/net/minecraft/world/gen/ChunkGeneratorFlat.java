package net.minecraft.world.gen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkGeneratorFlat implements IChunkGenerator
{
    private final World world;
    private final Random random;
    private final IBlockState[] cachedBlockIDs = new IBlockState[256];
    private final FlatGeneratorInfo flatWorldGenInfo;
    private final Map<String, MapGenStructure> structureGenerators = new HashMap<String, MapGenStructure>();
    private final boolean hasDecoration;
    private final boolean hasDungeons;
    private WorldGenLakes waterLakeGenerator;
    private WorldGenLakes lavaLakeGenerator;

    public ChunkGeneratorFlat(World worldIn, long seed, boolean generateStructures, String flatGeneratorSettings)
    {
        world = worldIn;
        random = new Random(seed);
        flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(flatGeneratorSettings);

        if (generateStructures)
        {
            Map<String, Map<String, String>> map = flatWorldGenInfo.getWorldFeatures();

            if (map.containsKey("village"))
            {
                Map<String, String> map1 = (Map)map.get("village");

                if (!map1.containsKey("size"))
                {
                    map1.put("size", "1");
                }

                structureGenerators.put("Village", new MapGenVillage(map1));
            }

            if (map.containsKey("biome_1"))
            {
                structureGenerators.put("Temple", new MapGenScatteredFeature(map.get("biome_1")));
            }

            if (map.containsKey("mineshaft"))
            {
                structureGenerators.put("Mineshaft", new MapGenMineshaft(map.get("mineshaft")));
            }

            if (map.containsKey("stronghold"))
            {
                structureGenerators.put("Stronghold", new MapGenStronghold(map.get("stronghold")));
            }

            if (map.containsKey("oceanmonument"))
            {
                structureGenerators.put("Monument", new StructureOceanMonument(map.get("oceanmonument")));
            }
        }

        if (flatWorldGenInfo.getWorldFeatures().containsKey("lake"))
        {
            waterLakeGenerator = new WorldGenLakes(Blocks.WATER);
        }

        if (flatWorldGenInfo.getWorldFeatures().containsKey("lava_lake"))
        {
            lavaLakeGenerator = new WorldGenLakes(Blocks.LAVA);
        }

        hasDungeons = flatWorldGenInfo.getWorldFeatures().containsKey("dungeon");
        int j = 0;
        int k = 0;
        boolean flag = true;

        for (FlatLayerInfo flatlayerinfo : flatWorldGenInfo.getFlatLayers())
        {
            for (int i = flatlayerinfo.getMinY(); i < flatlayerinfo.getMinY() + flatlayerinfo.getLayerCount(); ++i)
            {
                IBlockState iblockstate = flatlayerinfo.getLayerMaterial();

                if (iblockstate.getBlock() != Blocks.AIR)
                {
                    flag = false;
                    cachedBlockIDs[i] = iblockstate;
                }
            }

            if (flatlayerinfo.getLayerMaterial().getBlock() == Blocks.AIR)
            {
                k += flatlayerinfo.getLayerCount();
            }
            else
            {
                j += flatlayerinfo.getLayerCount() + k;
                k = 0;
            }
        }

        worldIn.setSeaLevel(j);
        hasDecoration = flag && flatWorldGenInfo.getBiome() != Biome.getIdForBiome(Biomes.VOID) ? false : flatWorldGenInfo.getWorldFeatures().containsKey("decoration");
    }

    /**
     * Generates the chunk at the specified position, from scratch
     */
    public Chunk generateChunk(int x, int z)
    {
        ChunkPrimer chunkprimer = new ChunkPrimer();

        for (int i = 0; i < cachedBlockIDs.length; ++i)
        {
            IBlockState iblockstate = cachedBlockIDs[i];

            if (iblockstate != null)
            {
                for (int j = 0; j < 16; ++j)
                {
                    for (int k = 0; k < 16; ++k)
                    {
                        chunkprimer.setBlockState(j, i, k, iblockstate);
                    }
                }
            }
        }

        for (MapGenBase mapgenbase : structureGenerators.values())
        {
            mapgenbase.generate(world, x, z, chunkprimer);
        }

        Chunk chunk = new Chunk(world, chunkprimer, x, z);
        Biome[] abiome = world.getBiomeProvider().getBiomes((Biome[])null, x * 16, z * 16, 16, 16);
        byte[] abyte = chunk.getBiomeArray();

        for (int l = 0; l < abyte.length; ++l)
        {
            abyte[l] = (byte)Biome.getIdForBiome(abiome[l]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    /**
     * Generate initial structures in this chunk, e.g. mineshafts, temples, lakes, and dungeons
     *  
     * @param x Chunk x coordinate
     * @param z Chunk z coordinate
     */
    public void populate(int x, int z)
    {
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = world.getBiome(new BlockPos(i + 16, 0, j + 16));
        boolean flag = false;
        random.setSeed(world.getSeed());
        long k = random.nextLong() / 2L * 2L + 1L;
        long l = random.nextLong() / 2L * 2L + 1L;
        random.setSeed((long)x * k + (long)z * l ^ world.getSeed());
        ChunkPos chunkpos = new ChunkPos(x, z);

        for (MapGenStructure mapgenstructure : structureGenerators.values())
        {
            boolean flag1 = mapgenstructure.generateStructure(world, random, chunkpos);

            if (mapgenstructure instanceof MapGenVillage)
            {
                flag |= flag1;
            }
        }

        if (waterLakeGenerator != null && !flag && random.nextInt(4) == 0)
        {
            waterLakeGenerator.generate(world, random, blockpos.add(random.nextInt(16) + 8, random.nextInt(256), random.nextInt(16) + 8));
        }

        if (lavaLakeGenerator != null && !flag && random.nextInt(8) == 0)
        {
            BlockPos blockpos1 = blockpos.add(random.nextInt(16) + 8, random.nextInt(random.nextInt(248) + 8), random.nextInt(16) + 8);

            if (blockpos1.getY() < world.getSeaLevel() || random.nextInt(10) == 0)
            {
                lavaLakeGenerator.generate(world, random, blockpos1);
            }
        }

        if (hasDungeons)
        {
            for (int i1 = 0; i1 < 8; ++i1)
            {
                (new WorldGenDungeons()).generate(world, random, blockpos.add(random.nextInt(16) + 8, random.nextInt(256), random.nextInt(16) + 8));
            }
        }

        if (hasDecoration)
        {
            biome.decorate(world, random, blockpos);
        }
    }

    /**
     * Called to generate additional structures after initial worldgen, used by ocean monuments
     */
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        Biome biome = world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        MapGenStructure mapgenstructure = structureGenerators.get(structureName);
        return mapgenstructure != null ? mapgenstructure.getNearestStructurePos(worldIn, position, findUnexplored) : null;
    }

    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        MapGenStructure mapgenstructure = structureGenerators.get(structureName);
        return mapgenstructure != null ? mapgenstructure.isInsideStructure(pos) : false;
    }

    /**
     * Recreates data about structures intersecting given chunk (used for example by getPossibleCreatures), without
     * placing any blocks. When called for the first time before any chunk is generated - also initializes the internal
     * state needed by getPossibleCreatures.
     */
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        for (MapGenStructure mapgenstructure : structureGenerators.values())
        {
            mapgenstructure.generate(world, x, z, (ChunkPrimer)null);
        }
    }
}
