package net.minecraft.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityEvokerFangs extends Entity
{
    private int warmupDelayTicks;
    private boolean sentSpikeEvent;
    private int lifeTicks;
    private boolean clientSideAttackStarted;
    private EntityLivingBase caster;
    private UUID casterUuid;

    public EntityEvokerFangs(World worldIn)
    {
        super(worldIn);
        lifeTicks = 22;
        setSize(0.5F, 0.8F);
    }

    public EntityEvokerFangs(World worldIn, double x, double y, double z, float p_i47276_8_, int p_i47276_9_, EntityLivingBase casterIn)
    {
        this(worldIn);
        warmupDelayTicks = p_i47276_9_;
        setCaster(casterIn);
        rotationYaw = p_i47276_8_ * (180F / (float)Math.PI);
        setPosition(x, y, z);
    }

    protected void entityInit()
    {
    }

    public void setCaster(@Nullable EntityLivingBase p_190549_1_)
    {
        caster = p_190549_1_;
        casterUuid = p_190549_1_ == null ? null : p_190549_1_.getUniqueID();
    }

    @Nullable
    public EntityLivingBase getCaster()
    {
        if (caster == null && casterUuid != null && world instanceof WorldServer)
        {
            Entity entity = ((WorldServer) world).getEntityFromUuid(casterUuid);

            if (entity instanceof EntityLivingBase)
            {
                caster = (EntityLivingBase)entity;
            }
        }

        return caster;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        warmupDelayTicks = compound.getInteger("Warmup");
        casterUuid = compound.getUniqueId("OwnerUUID");
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setInteger("Warmup", warmupDelayTicks);

        if (casterUuid != null)
        {
            compound.setUniqueId("OwnerUUID", casterUuid);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (world.isRemote)
        {
            if (clientSideAttackStarted)
            {
                --lifeTicks;

                if (lifeTicks == 14)
                {
                    for (int i = 0; i < 12; ++i)
                    {
                        double d0 = posX + (rand.nextDouble() * 2.0D - 1.0D) * (double) width * 0.5D;
                        double d1 = posY + 0.05D + rand.nextDouble() * 1.0D;
                        double d2 = posZ + (rand.nextDouble() * 2.0D - 1.0D) * (double) width * 0.5D;
                        double d3 = (rand.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        double d4 = 0.3D + rand.nextDouble() * 0.3D;
                        double d5 = (rand.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        world.spawnParticle(EnumParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    }
                }
            }
        }
        else if (--warmupDelayTicks < 0)
        {
            if (warmupDelayTicks == -8)
            {
                for (EntityLivingBase entitylivingbase : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D)))
                {
                    damage(entitylivingbase);
                }
            }

            if (!sentSpikeEvent)
            {
                world.setEntityState(this, (byte)4);
                sentSpikeEvent = true;
            }

            if (--lifeTicks < 0)
            {
                setDead();
            }
        }
    }

    private void damage(EntityLivingBase p_190551_1_)
    {
        EntityLivingBase entitylivingbase = getCaster();

        if (p_190551_1_.isEntityAlive() && !p_190551_1_.getIsInvulnerable() && p_190551_1_ != entitylivingbase)
        {
            if (entitylivingbase == null)
            {
                p_190551_1_.attackEntityFrom(DamageSource.MAGIC, 6.0F);
            }
            else
            {
                if (entitylivingbase.isOnSameTeam(p_190551_1_))
                {
                    return;
                }

                p_190551_1_.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, entitylivingbase), 6.0F);
            }
        }
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    public void handleStatusUpdate(byte id)
    {
        super.handleStatusUpdate(id);

        if (id == 4)
        {
            clientSideAttackStarted = true;

            if (!isSilent())
            {
                world.playSound(posX, posY, posZ, SoundEvents.EVOCATION_FANGS_ATTACK, getSoundCategory(), 1.0F, rand.nextFloat() * 0.2F + 0.85F, false);
            }
        }
    }

    public float getAnimationProgress(float partialTicks)
    {
        if (!clientSideAttackStarted)
        {
            return 0.0F;
        }
        else
        {
            int i = lifeTicks - 2;
            return i <= 0 ? 1.0F : 1.0F - ((float)i - partialTicks) / 20.0F;
        }
    }
}
