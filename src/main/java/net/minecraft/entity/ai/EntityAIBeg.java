package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityAIBeg extends EntityAIBase
{
    private final EntityWolf wolf;
    private EntityPlayer player;
    private final World world;
    private final float minPlayerDistance;
    private int timeoutCounter;

    public EntityAIBeg(EntityWolf wolf, float minDistance)
    {
        this.wolf = wolf;
        world = wolf.world;
        minPlayerDistance = minDistance;
        setMutexBits(2);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        player = world.getClosestPlayerToEntity(wolf, (double) minPlayerDistance);
        return player == null ? false : hasTemptationItemInHand(player);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        if (!player.isEntityAlive())
        {
            return false;
        }
        else if (wolf.getDistanceSq(player) > (double)(minPlayerDistance * minPlayerDistance))
        {
            return false;
        }
        else
        {
            return timeoutCounter > 0 && hasTemptationItemInHand(player);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        wolf.setBegging(true);
        timeoutCounter = 40 + wolf.getRNG().nextInt(40);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        wolf.setBegging(false);
        player = null;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        wolf.getLookHelper().setLookPosition(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ, 10.0F, (float) wolf.getVerticalFaceSpeed());
        --timeoutCounter;
    }

    /**
     * Gets if the Player has the Bone in the hand.
     */
    private boolean hasTemptationItemInHand(EntityPlayer player)
    {
        for (EnumHand enumhand : EnumHand.values())
        {
            ItemStack itemstack = player.getHeldItem(enumhand);

            if (wolf.isTamed() && itemstack.getItem() == Items.BONE)
            {
                return true;
            }

            if (wolf.isBreedingItem(itemstack))
            {
                return true;
            }
        }

        return false;
    }
}
