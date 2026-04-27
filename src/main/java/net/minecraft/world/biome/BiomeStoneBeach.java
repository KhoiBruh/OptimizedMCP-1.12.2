package net.minecraft.world.biome;

import net.minecraft.init.Blocks;

public class BiomeStoneBeach extends Biome
{
    public BiomeStoneBeach(Biome.BiomeProperties properties)
    {
        super(properties);
        spawnableCreatureList.clear();
        topBlock = Blocks.STONE.getDefaultState();
        fillerBlock = Blocks.STONE.getDefaultState();
        decorator.treesPerChunk = -999;
        decorator.deadBushPerChunk = 0;
        decorator.reedsPerChunk = 0;
        decorator.cactiPerChunk = 0;
    }
}
