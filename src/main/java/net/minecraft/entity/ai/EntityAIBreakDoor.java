package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;

public class EntityAIBreakDoor extends EntityAIDoorInteract
{
    private int breakingTime;
    private int previousBreakProgress = -1;

    public EntityAIBreakDoor(EntityLiving entityIn)
    {
        super(entityIn);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!super.shouldExecute())
        {
            return false;
        }
        else if (!entity.world.getGameRules().getBoolean("mobGriefing"))
        {
            return false;
        }
        else
        {
            BlockDoor blockdoor = doorBlock;
            return !BlockDoor.isOpen(entity.world, doorPosition);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        super.startExecuting();
        breakingTime = 0;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        double d0 = entity.getDistanceSq(doorPosition);
        boolean flag;

        if (breakingTime <= 240)
        {
            BlockDoor blockdoor = doorBlock;

            if (!BlockDoor.isOpen(entity.world, doorPosition) && d0 < 4.0D)
            {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        super.resetTask();
        entity.world.sendBlockBreakProgress(entity.getEntityId(), doorPosition, -1);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        super.updateTask();

        if (entity.getRNG().nextInt(20) == 0)
        {
            entity.world.playEvent(1019, doorPosition, 0);
        }

        ++breakingTime;
        int i = (int)((float) breakingTime / 240.0F * 10.0F);

        if (i != previousBreakProgress)
        {
            entity.world.sendBlockBreakProgress(entity.getEntityId(), doorPosition, i);
            previousBreakProgress = i;
        }

        if (breakingTime == 240 && entity.world.getDifficulty() == EnumDifficulty.HARD)
        {
            entity.world.setBlockToAir(doorPosition);
            entity.world.playEvent(1021, doorPosition, 0);
            entity.world.playEvent(2001, doorPosition, Block.getIdFromBlock(doorBlock));
        }
    }
}
