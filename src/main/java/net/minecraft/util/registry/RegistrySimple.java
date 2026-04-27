package net.minecraft.util.registry;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrySimple<K, V> implements IRegistry<K, V>
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Map<K, V> registryObjects = createUnderlyingMap();
    private Object[] values;

    protected Map<K, V> createUnderlyingMap()
    {
        return Maps.<K, V>newHashMap();
    }

    @Nullable
    public V getObject(@Nullable K name)
    {
        return registryObjects.get(name);
    }

    /**
     * Register an object on this registry.
     */
    public void putObject(K key, V value)
    {
        Validate.notNull(key);
        Validate.notNull(value);
        values = null;

        if (registryObjects.containsKey(key))
        {
            LOGGER.debug("Adding duplicate key '{}' to registry", key);
        }

        registryObjects.put(key, value);
    }

    public Set<K> getKeys()
    {
        return Collections.<K>unmodifiableSet(registryObjects.keySet());
    }

    @Nullable
    public V getRandomObject(Random random)
    {
        if (values == null)
        {
            Collection<?> collection = registryObjects.values();

            if (collection.isEmpty())
            {
                return (V)null;
            }

            values = collection.toArray(new Object[collection.size()]);
        }

        return (V) values[random.nextInt(values.length)];
    }

    /**
     * Does this registry contain an entry for the given key?
     */
    public boolean containsKey(K key)
    {
        return registryObjects.containsKey(key);
    }

    public Iterator<V> iterator()
    {
        return registryObjects.values().iterator();
    }
}
