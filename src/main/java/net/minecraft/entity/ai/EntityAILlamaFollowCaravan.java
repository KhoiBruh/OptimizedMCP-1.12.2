package net.minecraft.entity.ai;

import java.util.List;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.util.math.Vec3d;

public class EntityAILlamaFollowCaravan extends EntityAIBase
{
    public EntityLlama llama;
    private double speedModifier;
    private int distCheckCounter;

    public EntityAILlamaFollowCaravan(EntityLlama llamaIn, double speedModifierIn)
    {
        llama = llamaIn;
        speedModifier = speedModifierIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!llama.getLeashed() && !llama.inCaravan())
        {
            List<EntityLlama> list = llama.world.<EntityLlama>getEntitiesWithinAABB(llama.getClass(), llama.getEntityBoundingBox().grow(9.0D, 4.0D, 9.0D));
            EntityLlama entityllama = null;
            double d0 = Double.MAX_VALUE;

            for (EntityLlama entityllama1 : list)
            {
                if (entityllama1.inCaravan() && !entityllama1.hasCaravanTrail())
                {
                    double d1 = llama.getDistanceSq(entityllama1);

                    if (d1 <= d0)
                    {
                        d0 = d1;
                        entityllama = entityllama1;
                    }
                }
            }

            if (entityllama == null)
            {
                for (EntityLlama entityllama2 : list)
                {
                    if (entityllama2.getLeashed() && !entityllama2.hasCaravanTrail())
                    {
                        double d2 = llama.getDistanceSq(entityllama2);

                        if (d2 <= d0)
                        {
                            d0 = d2;
                            entityllama = entityllama2;
                        }
                    }
                }
            }

            if (entityllama == null)
            {
                return false;
            }
            else if (d0 < 4.0D)
            {
                return false;
            }
            else if (!entityllama.getLeashed() && !firstIsLeashed(entityllama, 1))
            {
                return false;
            }
            else
            {
                llama.joinCaravan(entityllama);
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        if (llama.inCaravan() && llama.getCaravanHead().isEntityAlive() && firstIsLeashed(llama, 0))
        {
            double d0 = llama.getDistanceSq(llama.getCaravanHead());

            if (d0 > 676.0D)
            {
                if (speedModifier <= 3.0D)
                {
                    speedModifier *= 1.2D;
                    distCheckCounter = 40;
                    return true;
                }

                if (distCheckCounter == 0)
                {
                    return false;
                }
            }

            if (distCheckCounter > 0)
            {
                --distCheckCounter;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        llama.leaveCaravan();
        speedModifier = 2.1D;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        if (llama.inCaravan())
        {
            EntityLlama entityllama = llama.getCaravanHead();
            double d0 = (double) llama.getDistance(entityllama);
            float f = 2.0F;
            Vec3d vec3d = (new Vec3d(entityllama.posX - llama.posX, entityllama.posY - llama.posY, entityllama.posZ - llama.posZ)).normalize().scale(Math.max(d0 - 2.0D, 0.0D));
            llama.getNavigator().tryMoveToXYZ(llama.posX + vec3d.x, llama.posY + vec3d.y, llama.posZ + vec3d.z, speedModifier);
        }
    }

    private boolean firstIsLeashed(EntityLlama p_190858_1_, int p_190858_2_)
    {
        if (p_190858_2_ > 8)
        {
            return false;
        }
        else if (p_190858_1_.inCaravan())
        {
            if (p_190858_1_.getCaravanHead().getLeashed())
            {
                return true;
            }
            else
            {
                EntityLlama entityllama = p_190858_1_.getCaravanHead();
                ++p_190858_2_;
                return firstIsLeashed(entityllama, p_190858_2_);
            }
        }
        else
        {
            return false;
        }
    }
}
