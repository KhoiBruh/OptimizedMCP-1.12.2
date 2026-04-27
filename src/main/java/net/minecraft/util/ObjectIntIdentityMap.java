package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class ObjectIntIdentityMap<T> implements IObjectIntIterable<T>
{
    private final IdentityHashMap<T, Integer> identityMap;
    private final List<T> objectList;

    public ObjectIntIdentityMap()
    {
        this(512);
    }

    public ObjectIntIdentityMap(int expectedSize)
    {
        objectList = Lists.<T>newArrayListWithExpectedSize(expectedSize);
        identityMap = new IdentityHashMap<T, Integer>(expectedSize);
    }

    public void put(T key, int value)
    {
        identityMap.put(key, Integer.valueOf(value));

        while (objectList.size() <= value)
        {
            objectList.add(null);
        }

        objectList.set(value, key);
    }

    public int get(T key)
    {
        Integer integer = identityMap.get(key);
        return integer == null ? -1 : integer.intValue();
    }

    @Nullable
    public final T getByValue(int value)
    {
        return (T)(value >= 0 && value < objectList.size() ? objectList.get(value) : null);
    }

    public Iterator<T> iterator()
    {
        return Iterators.filter(objectList.iterator(), Predicates.notNull());
    }

    public int size()
    {
        return identityMap.size();
    }
}
