package net.minecraft.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public interface ICriterionTrigger<T extends ICriterionInstance>
{
    ResourceLocation getId();

    void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<T> listener);

    void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<T> listener);

    void removeAllListeners(PlayerAdvancements playerAdvancementsIn);

    /**
     * Deserialize a ICriterionInstance of this trigger from the data in the JSON.
     */
    T deserializeInstance(JsonObject json, JsonDeserializationContext context);

    public static class Listener<T extends ICriterionInstance>
    {
        private final T criterionInstance;
        private final Advancement advancement;
        private final String criterionName;

        public Listener(T criterionInstanceIn, Advancement advancementIn, String criterionNameIn)
        {
            criterionInstance = criterionInstanceIn;
            advancement = advancementIn;
            criterionName = criterionNameIn;
        }

        public T getCriterionInstance()
        {
            return criterionInstance;
        }

        public void grantCriterion(PlayerAdvancements playerAdvancementsIn)
        {
            playerAdvancementsIn.grantCriterion(advancement, criterionName);
        }

        public boolean equals(Object p_equals_1_)
        {
            if (this == p_equals_1_)
            {
                return true;
            }
            else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass())
            {
                ICriterionTrigger.Listener<?> listener = (ICriterionTrigger.Listener)p_equals_1_;

                if (!criterionInstance.equals(listener.criterionInstance))
                {
                    return false;
                }
                else
                {
                    return !advancement.equals(listener.advancement) ? false : criterionName.equals(listener.criterionName);
                }
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            int i = criterionInstance.hashCode();
            i = 31 * i + advancement.hashCode();
            i = 31 * i + criterionName.hashCode();
            return i;
        }
    }
}
