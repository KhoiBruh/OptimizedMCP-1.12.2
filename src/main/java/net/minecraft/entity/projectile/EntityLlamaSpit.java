package net.minecraft.entity.projectile;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityLlamaSpit extends Entity implements IProjectile
{
    public EntityLlama owner;
    private NBTTagCompound ownerNbt;

    public EntityLlamaSpit(World worldIn)
    {
        super(worldIn);
    }

    public EntityLlamaSpit(World worldIn, EntityLlama p_i47273_2_)
    {
        super(worldIn);
        owner = p_i47273_2_;
        setPosition(p_i47273_2_.posX - (double)(p_i47273_2_.width + 1.0F) * 0.5D * (double)MathHelper.sin(p_i47273_2_.renderYawOffset * 0.017453292F), p_i47273_2_.posY + (double)p_i47273_2_.getEyeHeight() - 0.10000000149011612D, p_i47273_2_.posZ + (double)(p_i47273_2_.width + 1.0F) * 0.5D * (double)MathHelper.cos(p_i47273_2_.renderYawOffset * 0.017453292F));
        setSize(0.25F, 0.25F);
    }

    public EntityLlamaSpit(World worldIn, double x, double y, double z, double p_i47274_8_, double p_i47274_10_, double p_i47274_12_)
    {
        super(worldIn);
        setPosition(x, y, z);

        for (int i = 0; i < 7; ++i)
        {
            double d0 = 0.4D + 0.1D * (double)i;
            worldIn.spawnParticle(EnumParticleTypes.SPIT, x, y, z, p_i47274_8_ * d0, p_i47274_10_, p_i47274_12_ * d0);
        }

        motionX = p_i47274_8_;
        motionY = p_i47274_10_;
        motionZ = p_i47274_12_;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (ownerNbt != null)
        {
            restoreOwnerFromSave();
        }

        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        Vec3d vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
        RayTraceResult raytraceresult = world.rayTraceBlocks(vec3d, vec3d1);
        vec3d = new Vec3d(posX, posY, posZ);
        vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

        if (raytraceresult != null)
        {
            vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
        }

        Entity entity = getHitEntity(vec3d, vec3d1);

        if (entity != null)
        {
            raytraceresult = new RayTraceResult(entity);
        }

        if (raytraceresult != null)
        {
            onHit(raytraceresult);
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

        for (rotationPitch = (float)(MathHelper.atan2(motionY, (double)f) * (180D / Math.PI)); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (rotationPitch - prevRotationPitch >= 180.0F)
        {
            prevRotationPitch += 360.0F;
        }

        while (rotationYaw - prevRotationYaw < -180.0F)
        {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F)
        {
            prevRotationYaw += 360.0F;
        }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f1 = 0.99F;
        float f2 = 0.06F;

        if (!world.isMaterialInBB(getEntityBoundingBox(), Material.AIR))
        {
            setDead();
        }
        else if (isInWater())
        {
            setDead();
        }
        else
        {
            motionX *= 0.9900000095367432D;
            motionY *= 0.9900000095367432D;
            motionZ *= 0.9900000095367432D;

            if (!hasNoGravity())
            {
                motionY -= 0.05999999865889549D;
            }

            setPosition(posX, posY, posZ);
        }
    }

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    public void setVelocity(double x, double y, double z)
    {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(x * x + z * z);
            rotationPitch = (float)(MathHelper.atan2(y, (double)f) * (180D / Math.PI));
            rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
            prevRotationPitch = rotationPitch;
            prevRotationYaw = rotationYaw;
            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    @Nullable
    private Entity getHitEntity(Vec3d p_190538_1_, Vec3d p_190538_2_)
    {
        Entity entity = null;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0D));
        double d0 = 0.0D;

        for (Entity entity1 : list)
        {
            if (entity1 != owner)
            {
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(p_190538_1_, p_190538_2_);

                if (raytraceresult != null)
                {
                    double d1 = p_190538_1_.squareDistanceTo(raytraceresult.hitVec);

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity;
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void shoot(double x, double y, double z, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x + rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        y = y + rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        z = z + rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        motionX = x;
        motionY = y;
        motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
    }

    public void onHit(RayTraceResult p_190536_1_)
    {
        if (p_190536_1_.entityHit != null && owner != null)
        {
            p_190536_1_.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, owner).setProjectile(), 1.0F);
        }

        if (!world.isRemote)
        {
            setDead();
        }
    }

    protected void entityInit()
    {
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("Owner", 10))
        {
            ownerNbt = compound.getCompoundTag("Owner");
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        if (owner != null)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            UUID uuid = owner.getUniqueID();
            nbttagcompound.setUniqueId("OwnerUUID", uuid);
            compound.setTag("Owner", nbttagcompound);
        }
    }

    private void restoreOwnerFromSave()
    {
        if (ownerNbt != null && ownerNbt.hasUniqueId("OwnerUUID"))
        {
            UUID uuid = ownerNbt.getUniqueId("OwnerUUID");

            for (EntityLlama entityllama : world.getEntitiesWithinAABB(EntityLlama.class, getEntityBoundingBox().grow(15.0D)))
            {
                if (entityllama.getUniqueID().equals(uuid))
                {
                    owner = entityllama;
                    break;
                }
            }
        }

        ownerNbt = null;
    }
}
