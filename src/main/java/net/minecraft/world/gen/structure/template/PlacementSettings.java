package net.minecraft.world.gen.structure.template;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class PlacementSettings
{
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private boolean ignoreEntities;
    @Nullable

    /**
     * the type of block in the world that will get replaced by the structure
     */
    private Block replacedBlock;
    @Nullable

    /** the chunk the structure is within */
    private ChunkPos chunk;
    @Nullable

    /** the bounds the structure is contained within */
    private StructureBoundingBox boundingBox;
    private boolean ignoreStructureBlock = true;
    private float integrity = 1.0F;
    @Nullable
    private Random random;
    @Nullable
    private Long setSeed;

    public PlacementSettings copy()
    {
        PlacementSettings placementsettings = new PlacementSettings();
        placementsettings.mirror = mirror;
        placementsettings.rotation = rotation;
        placementsettings.ignoreEntities = ignoreEntities;
        placementsettings.replacedBlock = replacedBlock;
        placementsettings.chunk = chunk;
        placementsettings.boundingBox = boundingBox;
        placementsettings.ignoreStructureBlock = ignoreStructureBlock;
        placementsettings.integrity = integrity;
        placementsettings.random = random;
        placementsettings.setSeed = setSeed;
        return placementsettings;
    }

    public PlacementSettings setMirror(Mirror mirrorIn)
    {
        mirror = mirrorIn;
        return this;
    }

    public PlacementSettings setRotation(Rotation rotationIn)
    {
        rotation = rotationIn;
        return this;
    }

    public PlacementSettings setIgnoreEntities(boolean ignoreEntitiesIn)
    {
        ignoreEntities = ignoreEntitiesIn;
        return this;
    }

    public PlacementSettings setReplacedBlock(Block replacedBlockIn)
    {
        replacedBlock = replacedBlockIn;
        return this;
    }

    public PlacementSettings setChunk(ChunkPos chunkPosIn)
    {
        chunk = chunkPosIn;
        return this;
    }

    public PlacementSettings setBoundingBox(StructureBoundingBox boundingBoxIn)
    {
        boundingBox = boundingBoxIn;
        return this;
    }

    public PlacementSettings setSeed(@Nullable Long seedIn)
    {
        setSeed = seedIn;
        return this;
    }

    public PlacementSettings setRandom(@Nullable Random randomIn)
    {
        random = randomIn;
        return this;
    }

    public PlacementSettings setIntegrity(float integrityIn)
    {
        integrity = integrityIn;
        return this;
    }

    public Mirror getMirror()
    {
        return mirror;
    }

    public PlacementSettings setIgnoreStructureBlock(boolean ignoreStructureBlockIn)
    {
        ignoreStructureBlock = ignoreStructureBlockIn;
        return this;
    }

    public Rotation getRotation()
    {
        return rotation;
    }

    public Random getRandom(@Nullable BlockPos seed)
    {
        if (random != null)
        {
            return random;
        }
        else if (setSeed != null)
        {
            return setSeed.longValue() == 0L ? new Random(System.currentTimeMillis()) : new Random(setSeed.longValue());
        }
        else if (seed == null)
        {
            return new Random(System.currentTimeMillis());
        }
        else
        {
            int i = seed.getX();
            int j = seed.getZ();
            return new Random((long)(i * i * 4987142 + i * 5947611) + (long)(j * j) * 4392871L + (long)(j * 389711) ^ 987234911L);
        }
    }

    public float getIntegrity()
    {
        return integrity;
    }

    public boolean getIgnoreEntities()
    {
        return ignoreEntities;
    }

    @Nullable
    public Block getReplacedBlock()
    {
        return replacedBlock;
    }

    @Nullable
    public StructureBoundingBox getBoundingBox()
    {
        if (boundingBox == null && chunk != null)
        {
            setBoundingBoxFromChunk();
        }

        return boundingBox;
    }

    public boolean getIgnoreStructureBlock()
    {
        return ignoreStructureBlock;
    }

    void setBoundingBoxFromChunk()
    {
        boundingBox = getBoundingBoxFromChunk(chunk);
    }

    @Nullable
    private StructureBoundingBox getBoundingBoxFromChunk(@Nullable ChunkPos pos)
    {
        if (pos == null)
        {
            return null;
        }
        else
        {
            int i = pos.x * 16;
            int j = pos.z * 16;
            return new StructureBoundingBox(i, 0, j, i + 16 - 1, 255, j + 16 - 1);
        }
    }
}
