package net.minecraft.entity.ai;

import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;

public class EntityAILookAtVillager extends EntityAIBase
{
    private final EntityIronGolem ironGolem;
    private EntityVillager villager;
    private int lookTime;

    public EntityAILookAtVillager(EntityIronGolem ironGolemIn)
    {
        ironGolem = ironGolemIn;
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!ironGolem.world.isDaytime())
        {
            return false;
        }
        else if (ironGolem.getRNG().nextInt(8000) != 0)
        {
            return false;
        }
        else
        {
            villager = (EntityVillager) ironGolem.world.findNearestEntityWithinAABB(EntityVillager.class, ironGolem.getEntityBoundingBox().grow(6.0D, 2.0D, 6.0D), ironGolem);
            return villager != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return lookTime > 0;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        lookTime = 400;
        ironGolem.setHoldingRose(true);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        ironGolem.setHoldingRose(false);
        villager = null;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        ironGolem.getLookHelper().setLookPositionWithEntity(villager, 30.0F, 30.0F);
        --lookTime;
    }
}
