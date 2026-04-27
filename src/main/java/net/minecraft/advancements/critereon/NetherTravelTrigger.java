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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class NetherTravelTrigger implements ICriterionTrigger<NetherTravelTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation("nether_travel");
    private final Map<PlayerAdvancements, NetherTravelTrigger.Listeners> listeners = Maps.<PlayerAdvancements, NetherTravelTrigger.Listeners>newHashMap();

    public ResourceLocation getId()
    {
        return ID;
    }

    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener)
    {
        NetherTravelTrigger.Listeners nethertraveltrigger$listeners = listeners.get(playerAdvancementsIn);

        if (nethertraveltrigger$listeners == null)
        {
            nethertraveltrigger$listeners = new NetherTravelTrigger.Listeners(playerAdvancementsIn);
            listeners.put(playerAdvancementsIn, nethertraveltrigger$listeners);
        }

        nethertraveltrigger$listeners.add(listener);
    }

    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener)
    {
        NetherTravelTrigger.Listeners nethertraveltrigger$listeners = listeners.get(playerAdvancementsIn);

        if (nethertraveltrigger$listeners != null)
        {
            nethertraveltrigger$listeners.remove(listener);

            if (nethertraveltrigger$listeners.isEmpty())
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
    public NetherTravelTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        LocationPredicate locationpredicate = LocationPredicate.deserialize(json.get("entered"));
        LocationPredicate locationpredicate1 = LocationPredicate.deserialize(json.get("exited"));
        DistancePredicate distancepredicate = DistancePredicate.deserialize(json.get("distance"));
        return new NetherTravelTrigger.Instance(locationpredicate, locationpredicate1, distancepredicate);
    }

    public void trigger(EntityPlayerMP player, Vec3d enteredNetherPosition)
    {
        NetherTravelTrigger.Listeners nethertraveltrigger$listeners = listeners.get(player.getAdvancements());

        if (nethertraveltrigger$listeners != null)
        {
            nethertraveltrigger$listeners.trigger(player.getServerWorld(), enteredNetherPosition, player.posX, player.posY, player.posZ);
        }
    }

    public static class Instance extends AbstractCriterionInstance
    {
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public Instance(LocationPredicate enteredIn, LocationPredicate exitedIn, DistancePredicate distanceIn)
        {
            super(NetherTravelTrigger.ID);
            entered = enteredIn;
            exited = exitedIn;
            distance = distanceIn;
        }

        public boolean test(WorldServer world, Vec3d enteredNetherPosition, double x, double y, double z)
        {
            if (!entered.test(world, enteredNetherPosition.x, enteredNetherPosition.y, enteredNetherPosition.z))
            {
                return false;
            }
            else if (!exited.test(world, x, y, z))
            {
                return false;
            }
            else
            {
                return distance.test(enteredNetherPosition.x, enteredNetherPosition.y, enteredNetherPosition.z, x, y, z);
            }
        }
    }

    static class Listeners
    {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<NetherTravelTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<NetherTravelTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn)
        {
            playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty()
        {
            return listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener)
        {
            listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener)
        {
            listeners.remove(listener);
        }

        public void trigger(WorldServer world, Vec3d enteredNetherPosition, double x, double y, double z)
        {
            List<ICriterionTrigger.Listener<NetherTravelTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener : listeners)
            {
                if (((NetherTravelTrigger.Instance)listener.getCriterionInstance()).test(world, enteredNetherPosition, x, y, z))
                {
                    if (list == null)
                    {
                        list = Lists.<ICriterionTrigger.Listener<NetherTravelTrigger.Instance>>newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null)
            {
                for (ICriterionTrigger.Listener<NetherTravelTrigger.Instance> listener1 : list)
                {
                    listener1.grantCriterion(playerAdvancements);
                }
            }
        }
    }
}
