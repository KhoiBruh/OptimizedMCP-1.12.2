package net.minecraft.world.chunk.storage;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;

public class ExtendedBlockStorage
{
    /**
     * Contains the bottom-most Y block represented by this ExtendedBlockStorage. Typically a multiple of 16.
     */
    private final int yBase;

    /**
     * A total count of the number of non-air blocks in this block storage's Chunk.
     */
    private int blockRefCount;

    /**
     * Contains the number of blocks in this block storage's parent chunk that require random ticking. Used to cull the
     * Chunk from random tick updates for performance reasons.
     */
    private int tickRefCount;
    private final BlockStateContainer data;

    /** The NibbleArray containing a block of Block-light data. */
    private NibbleArray blockLight;

    /**
     * The NibbleArray containing skylight data.
     *  
     * Will be null if the provider for the world the chunk containing this block storage does not {@linkplain
     * net.minecraft.world.WorldProvider#hasSkylight have skylight}.
     */
    private NibbleArray skyLight;

    public ExtendedBlockStorage(int y, boolean storeSkylight)
    {
        yBase = y;
        data = new BlockStateContainer();
        blockLight = new NibbleArray();

        if (storeSkylight)
        {
            skyLight = new NibbleArray();
        }
    }

    public IBlockState get(int x, int y, int z)
    {
        return data.get(x, y, z);
    }

    public void set(int x, int y, int z, IBlockState state)
    {
        IBlockState iblockstate = get(x, y, z);
        Block block = iblockstate.getBlock();
        Block block1 = state.getBlock();

        if (block != Blocks.AIR)
        {
            --blockRefCount;

            if (block.getTickRandomly())
            {
                --tickRefCount;
            }
        }

        if (block1 != Blocks.AIR)
        {
            ++blockRefCount;

            if (block1.getTickRandomly())
            {
                ++tickRefCount;
            }
        }

        data.set(x, y, z, state);
    }

    /**
     * Returns whether or not this block storage's Chunk is fully empty, based on its internal reference count.
     */
    public boolean isEmpty()
    {
        return blockRefCount == 0;
    }

    /**
     * Returns whether or not this block storage's Chunk will require random ticking, used to avoid looping through
     * random block ticks when there are no blocks that would randomly tick.
     */
    public boolean needsRandomTick()
    {
        return tickRefCount > 0;
    }

    /**
     * Returns the Y location of this ExtendedBlockStorage.
     */
    public int getYLocation()
    {
        return yBase;
    }

    /**
     * Sets the saved Sky-light value in the extended block storage structure.
     */
    public void setSkyLight(int x, int y, int z, int value)
    {
        skyLight.set(x, y, z, value);
    }

    /**
     * Gets the saved Sky-light value in the extended block storage structure.
     */
    public int getSkyLight(int x, int y, int z)
    {
        return skyLight.get(x, y, z);
    }

    /**
     * Sets the saved Block-light value in the extended block storage structure.
     */
    public void setBlockLight(int x, int y, int z, int value)
    {
        blockLight.set(x, y, z, value);
    }

    /**
     * Gets the saved Block-light value in the extended block storage structure.
     */
    public int getBlockLight(int x, int y, int z)
    {
        return blockLight.get(x, y, z);
    }

    public void recalculateRefCounts()
    {
        blockRefCount = 0;
        tickRefCount = 0;

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                for (int k = 0; k < 16; ++k)
                {
                    Block block = get(i, j, k).getBlock();

                    if (block != Blocks.AIR)
                    {
                        ++blockRefCount;

                        if (block.getTickRandomly())
                        {
                            ++tickRefCount;
                        }
                    }
                }
            }
        }
    }

    public BlockStateContainer getData()
    {
        return data;
    }

    /**
     * Returns the NibbleArray instance containing Block-light data.
     */
    public NibbleArray getBlockLight()
    {
        return blockLight;
    }

    /**
     * Returns the NibbleArray instance containing Sky-light data.
     */
    public NibbleArray getSkyLight()
    {
        return skyLight;
    }

    /**
     * Sets the NibbleArray instance used for Block-light values in this particular storage block.
     */
    public void setBlockLight(NibbleArray newBlocklightArray)
    {
        blockLight = newBlocklightArray;
    }

    /**
     * Sets the NibbleArray instance used for Sky-light values in this particular storage block.
     */
    public void setSkyLight(NibbleArray newSkylightArray)
    {
        skyLight = newSkylightArray;
    }
}
