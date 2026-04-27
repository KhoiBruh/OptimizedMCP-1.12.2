package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;

public class SoundRegistry extends RegistrySimple<ResourceLocation, SoundEventAccessor>
{
    private Map<ResourceLocation, SoundEventAccessor> soundRegistry;

    protected Map<ResourceLocation, SoundEventAccessor> createUnderlyingMap()
    {
        soundRegistry = Maps.<ResourceLocation, SoundEventAccessor>newHashMap();
        return soundRegistry;
    }

    public void add(SoundEventAccessor accessor)
    {
        putObject(accessor.getLocation(), accessor);
    }

    /**
     * Reset the underlying sound map (Called on resource manager reload)
     */
    public void clearMap()
    {
        soundRegistry.clear();
    }
}
