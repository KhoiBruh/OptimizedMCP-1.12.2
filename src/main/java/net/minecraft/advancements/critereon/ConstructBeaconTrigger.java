package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;

public class ConstructBeaconTrigger implements ICriterionTrigger<ConstructBeaconTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation("construct_beacon");
    private final Map<PlayerAdvancements, ConstructBeaconTrigger.Listeners> listeners = Maps.<PlayerAdvancements, ConstructBeaconTrigger.Listeners>newHashMap();

    public ResourceLocation getId()
    {
        return ID;
    }

    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener)
    {
        ConstructBeaconTrigger.Listeners constructbeacontrigger$listeners = listeners.get(playerAdvancementsIn);

        if (constructbeacontrigger$listeners == null)
        {
            constructbeacontrigger$listeners = new ConstructBeaconTrigger.Listeners(playerAdvancementsIn);
            listeners.put(playerAdvancementsIn, constructbeacontrigger$listeners);
        }

        constructbeacontrigger$listeners.add(listener);
    }

    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener)
    {
        ConstructBeaconTrigger.Listeners constructbeacontrigger$listeners = listeners.get(playerAdvancementsIn);

        if (constructbeacontrigger$listeners != null)
        {
            constructbeacontrigger$listeners.remove(listener);

            if (constructbeacontrigger$listeners.isEmpty())
            {
                listeners.remove(playerAdvancementsIn);
            }
        }
    }

    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        listeners.remove(playerAdvancementsIn);
    }

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the JSON.
     */
    public ConstructBeaconTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        MinMaxBounds minmaxbounds = MinMaxBounds.deserialize(json.get("level"));
        return new ConstructBeaconTrigger.Instance(minmaxbounds);
    }

    public void trigger(EntityPlayerMP player, TileEntityBeacon beacon)
    {
        ConstructBeaconTrigger.Listeners constructbeacontrigger$listeners = listeners.get(player.getAdvancements());

        if (constructbeacontrigger$listeners != null)
        {
            constructbeacontrigger$listeners.trigger(beacon);
        }
    }

    public static class Instance extends AbstractCriterionInstance
    {
        private final MinMaxBounds level;

        public Instance(MinMaxBounds level)
        {
            super(ConstructBeaconTrigger.ID);
            this.level = level;
        }

        public boolean test(TileEntityBeacon beacon)
        {
            return level.test((float)beacon.getLevels());
        }
    }

    static class Listeners
    {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener)
        {
            listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener)
        {
            listeners.remove(listener);
        }

        public void trigger(TileEntityBeacon beacon)
        {
            List<ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener : listeners)
            {
                if (((ConstructBeaconTrigger.Instance)listener.getCriterionInstance()).test(beacon))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance>>newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null)
            {
                for (ICriterionTrigger.Listener<ConstructBeaconTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(playerAdvancements);
                }
            }
        }
    }
}
