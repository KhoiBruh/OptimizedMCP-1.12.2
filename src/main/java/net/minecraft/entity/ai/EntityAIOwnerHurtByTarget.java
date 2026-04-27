package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtByTarget extends EntityAITarget
{
    EntityTameable tameable;
    EntityLivingBase attacker;
    private int timestamp;

    public EntityAIOwnerHurtByTarget(EntityTameable theDefendingTameableIn)
    {
        super(theDefendingTameableIn, false);
        tameable = theDefendingTameableIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!tameable.isTamed())
        {
            return false;
        }
        else
        {
            EntityLivingBase entitylivingbase = tameable.getOwner();

            if (entitylivingbase == null)
            {
                return false;
            }
            else
            {
                attacker = entitylivingbase.getRevengeTarget();
                int i = entitylivingbase.getRevengeTimer();
                return i != timestamp && isSuitableTarget(attacker, false) && tameable.shouldAttackEntity(attacker, entitylivingbase);
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        taskOwner.setAttackTarget(attacker);
        EntityLivingBase entitylivingbase = tameable.getOwner();

        if (entitylivingbase != null)
        {
            timestamp = entitylivingbase.getRevengeTimer();
        }

        super.startExecuting();
    }
}
