package net.minecraft.world.biome;

public class BiomeVoid extends Biome
{
    public BiomeVoid(Biome.BiomeProperties properties)
    {
        super(properties);
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
        decorator = new BiomeVoidDecorator();
    }

    public boolean ignorePlayerSpawnSuitability()
    {
        return true;
    }
}
