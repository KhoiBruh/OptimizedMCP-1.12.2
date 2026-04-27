package net.minecraft.entity.ai;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.Vec3d;

public class EntityAIPlay extends EntityAIBase
{
    private final EntityVillager villager;
    private EntityLivingBase targetVillager;
    private final double speed;
    private int playTime;

    public EntityAIPlay(EntityVillager villagerIn, double speedIn)
    {
        villager = villagerIn;
        speed = speedIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (villager.getGrowingAge() >= 0)
        {
            return false;
        }
        else if (villager.getRNG().nextInt(400) != 0)
        {
            return false;
        }
        else
        {
            List<EntityVillager> list = villager.world.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, villager.getEntityBoundingBox().grow(6.0D, 3.0D, 6.0D));
            double d0 = Double.MAX_VALUE;

            for (EntityVillager entityvillager : list)
            {
                if (entityvillager != villager && !entityvillager.isPlaying() && entityvillager.getGrowingAge() < 0)
                {
                    double d1 = entityvillager.getDistanceSq(villager);

                    if (d1 <= d0)
                    {
                        d0 = d1;
                        targetVillager = entityvillager;
                    }
                }
            }

            if (targetVillager == null)
            {
                Vec3d vec3d = RandomPositionGenerator.findRandomTarget(villager, 16, 3);

                if (vec3d == null)
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return playTime > 0;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        if (targetVillager != null)
        {
            villager.setPlaying(true);
        }

        playTime = 1000;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        villager.setPlaying(false);
        targetVillager = null;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        --playTime;

        if (targetVillager != null)
        {
            if (villager.getDistanceSq(targetVillager) > 4.0D)
            {
                villager.getNavigator().tryMoveToEntityLiving(targetVillager, speed);
            }
        }
        else if (villager.getNavigator().noPath())
        {
            Vec3d vec3d = RandomPositionGenerator.findRandomTarget(villager, 16, 3);

            if (vec3d == null)
            {
                return;
            }

            villager.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, speed);
        }
    }
}
