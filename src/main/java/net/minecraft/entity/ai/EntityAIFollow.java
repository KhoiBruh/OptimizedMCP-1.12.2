package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;

public class EntityAIFollow extends EntityAIBase
{
    private final EntityLiving entity;
    private final Predicate<EntityLiving> followPredicate;
    private EntityLiving followingEntity;
    private final double speedModifier;
    private final PathNavigate navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;

    public EntityAIFollow(final EntityLiving p_i47417_1_, double p_i47417_2_, float p_i47417_4_, float p_i47417_5_)
    {
        entity = p_i47417_1_;
        followPredicate = new Predicate<EntityLiving>()
        {
            public boolean apply(@Nullable EntityLiving p_apply_1_)
            {
                return p_apply_1_ != null && p_i47417_1_.getClass() != p_apply_1_.getClass();
            }
        };
        speedModifier = p_i47417_2_;
        navigation = p_i47417_1_.getNavigator();
        stopDistance = p_i47417_4_;
        areaSize = p_i47417_5_;
        setMutexBits(3);

        if (!(p_i47417_1_.getNavigator() instanceof PathNavigateGround) && !(p_i47417_1_.getNavigator() instanceof PathNavigateFlying))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        List<EntityLiving> list = entity.world.<EntityLiving>getEntitiesWithinAABB(EntityLiving.class, entity.getEntityBoundingBox().grow((double) areaSize), followPredicate);

        if (!list.isEmpty())
        {
            for (EntityLiving entityliving : list)
            {
                if (!entityliving.isInvisible())
                {
                    followingEntity = entityliving;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return followingEntity != null && !navigation.noPath() && entity.getDistanceSq(followingEntity) > (double)(stopDistance * stopDistance);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        timeToRecalcPath = 0;
        oldWaterCost = entity.getPathPriority(PathNodeType.WATER);
        entity.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        followingEntity = null;
        navigation.clearPath();
        entity.setPathPriority(PathNodeType.WATER, oldWaterCost);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        if (followingEntity != null && !entity.getLeashed())
        {
            entity.getLookHelper().setLookPositionWithEntity(followingEntity, 10.0F, (float) entity.getVerticalFaceSpeed());

            if (--timeToRecalcPath <= 0)
            {
                timeToRecalcPath = 10;
                double d0 = entity.posX - followingEntity.posX;
                double d1 = entity.posY - followingEntity.posY;
                double d2 = entity.posZ - followingEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > (double)(stopDistance * stopDistance))
                {
                    navigation.tryMoveToEntityLiving(followingEntity, speedModifier);
                }
                else
                {
                    navigation.clearPath();
                    EntityLookHelper entitylookhelper = followingEntity.getLookHelper();

                    if (d3 <= (double) stopDistance || entitylookhelper.getLookPosX() == entity.posX && entitylookhelper.getLookPosY() == entity.posY && entitylookhelper.getLookPosZ() == entity.posZ)
                    {
                        double d4 = followingEntity.posX - entity.posX;
                        double d5 = followingEntity.posZ - entity.posZ;
                        navigation.tryMoveToXYZ(entity.posX - d4, entity.posY, entity.posZ - d5, speedModifier);
                    }
                }
            }
        }
    }
}
