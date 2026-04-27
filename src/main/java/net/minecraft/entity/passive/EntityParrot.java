package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollow;
import net.minecraft.entity.ai.EntityAIFollowOwnerFlying;
import net.minecraft.entity.ai.EntityAILandOnOwnersShoulder;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityParrot extends EntityShoulderRiding implements EntityFlying
{
    private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityParrot.class, DataSerializers.VARINT);
    private static final Predicate<EntityLiving> CAN_MIMIC = new Predicate<EntityLiving>()
    {
        public boolean apply(@Nullable EntityLiving p_apply_1_)
        {
            return p_apply_1_ != null && EntityParrot.IMITATION_SOUND_EVENTS.containsKey(EntityList.REGISTRY.getIDForObject(p_apply_1_.getClass()));
        }
    };
    private static final Item DEADLY_ITEM = Items.COOKIE;
    private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    private static final Int2ObjectMap<SoundEvent> IMITATION_SOUND_EVENTS = new Int2ObjectOpenHashMap<SoundEvent>(32);
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    private boolean partyParrot;
    private BlockPos jukeboxPosition;

    public EntityParrot(World worldIn)
    {
        super(worldIn);
        setSize(0.5F, 0.9F);
        moveHelper = new EntityFlyHelper(this);
    }

    @Nullable

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory.
     *  
     * The livingdata parameter is used to pass data between all instances during a pack spawn. It will be null on the
     * first call. Subclasses may check if it's null, and then create a new one and return it if so, initializing all
     * entities in the pack with the contained data.
     *  
     * @return The IEntityLivingData to pass to this method for other instances of this entity class within the same
     * pack
     *  
     * @param difficulty The current local difficulty
     * @param livingdata Shared spawn data. Will usually be null. (See return value for more information)
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        setVariant(rand.nextInt(5));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    protected void initEntityAI()
    {
        aiSit = new EntityAISit(this);
        tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(2, aiSit);
        tasks.addTask(2, new EntityAIFollowOwnerFlying(this, 1.0D, 5.0F, 1.0F));
        tasks.addTask(2, new EntityAIWanderAvoidWaterFlying(this, 1.0D));
        tasks.addTask(3, new EntityAILandOnOwnersShoulder(this));
        tasks.addTask(3, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4000000059604645D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
    }

    /**
     * Returns new PathNavigateGround instance
     */
    protected PathNavigate createNavigator(World worldIn)
    {
        PathNavigateFlying pathnavigateflying = new PathNavigateFlying(this, worldIn);
        pathnavigateflying.setCanOpenDoors(false);
        pathnavigateflying.setCanFloat(true);
        pathnavigateflying.setCanEnterDoors(true);
        return pathnavigateflying;
    }

    public float getEyeHeight()
    {
        return height * 0.6F;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        playMimicSound(world, this);

        if (jukeboxPosition == null || jukeboxPosition.distanceSq(posX, posY, posZ) > 12.0D || world.getBlockState(jukeboxPosition).getBlock() != Blocks.JUKEBOX)
        {
            partyParrot = false;
            jukeboxPosition = null;
        }

        super.onLivingUpdate();
        calculateFlapping();
    }

    public void setPartying(BlockPos pos, boolean p_191987_2_)
    {
        jukeboxPosition = pos;
        partyParrot = p_191987_2_;
    }

    public boolean isPartying()
    {
        return partyParrot;
    }

    private void calculateFlapping()
    {
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed = (float)((double) flapSpeed + (double)(onGround ? -1 : 4) * 0.3D);
        flapSpeed = MathHelper.clamp(flapSpeed, 0.0F, 1.0F);

        if (!onGround && flapping < 1.0F)
        {
            flapping = 1.0F;
        }

        flapping = (float)((double) flapping * 0.9D);

        if (!onGround && motionY < 0.0D)
        {
            motionY *= 0.6D;
        }

        flap += flapping * 2.0F;
    }

    private static boolean playMimicSound(World worldIn, Entity p_192006_1_)
    {
        if (!p_192006_1_.isSilent() && worldIn.rand.nextInt(50) == 0)
        {
            List<EntityLiving> list = worldIn.<EntityLiving>getEntitiesWithinAABB(EntityLiving.class, p_192006_1_.getEntityBoundingBox().grow(20.0D), CAN_MIMIC);

            if (!list.isEmpty())
            {
                EntityLiving entityliving = list.get(worldIn.rand.nextInt(list.size()));

                if (!entityliving.isSilent())
                {
                    SoundEvent soundevent = getImitatedSound(EntityList.REGISTRY.getIDForObject(entityliving.getClass()));
                    worldIn.playSound((EntityPlayer)null, p_192006_1_.posX, p_192006_1_.posY, p_192006_1_.posZ, soundevent, p_192006_1_.getSoundCategory(), 0.7F, getPitch(worldIn.rand));
                    return true;
                }
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!isTamed() && TAME_ITEMS.contains(itemstack.getItem()))
        {
            if (!player.capabilities.isCreativeMode)
            {
                itemstack.shrink(1);
            }

            if (!isSilent())
            {
                world.playSound((EntityPlayer)null, posX, posY, posZ, SoundEvents.ENTITY_PARROT_EAT, getSoundCategory(), 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            }

            if (!world.isRemote)
            {
                if (rand.nextInt(10) == 0)
                {
                    setTamedBy(player);
                    playTameEffect(true);
                    world.setEntityState(this, (byte)7);
                }
                else
                {
                    playTameEffect(false);
                    world.setEntityState(this, (byte)6);
                }
            }

            return true;
        }
        else if (itemstack.getItem() == DEADLY_ITEM)
        {
            if (!player.capabilities.isCreativeMode)
            {
                itemstack.shrink(1);
            }

            addPotionEffect(new PotionEffect(MobEffects.POISON, 900));

            if (player.isCreative() || !getIsInvulnerable())
            {
                attackEntityFrom(DamageSource.causePlayerDamage(player), Float.MAX_VALUE);
            }

            return true;
        }
        else
        {
            if (!world.isRemote && !isFlying() && isTamed() && isOwner(player))
            {
                aiSit.setSitting(!isSitting());
            }

            return super.processInteract(player, hand);
        }
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(ItemStack stack)
    {
        return false;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor(posX);
        int j = MathHelper.floor(getEntityBoundingBox().minY);
        int k = MathHelper.floor(posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        Block block = world.getBlockState(blockpos.down()).getBlock();
        return block instanceof BlockLeaves || block == Blocks.GRASS || block instanceof BlockLog || block == Blocks.AIR && world.getLight(blockpos) > 8 && super.getCanSpawnHere();
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
    {
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        return false;
    }

    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable)
    {
        return null;
    }

    public static void playAmbientSound(World worldIn, Entity p_192005_1_)
    {
        if (!p_192005_1_.isSilent() && !playMimicSound(worldIn, p_192005_1_) && worldIn.rand.nextInt(200) == 0)
        {
            worldIn.playSound((EntityPlayer)null, p_192005_1_.posX, p_192005_1_.posY, p_192005_1_.posZ, getAmbientSound(worldIn.rand), p_192005_1_.getSoundCategory(), 1.0F, getPitch(worldIn.rand));
        }
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
    }

    @Nullable
    public SoundEvent getAmbientSound()
    {
        return getAmbientSound(rand);
    }

    private static SoundEvent getAmbientSound(Random random)
    {
        if (random.nextInt(1000) == 0)
        {
            List<Integer> list = new ArrayList<Integer>(IMITATION_SOUND_EVENTS.keySet());
            return getImitatedSound(((Integer)list.get(random.nextInt(list.size()))).intValue());
        }
        else
        {
            return SoundEvents.ENTITY_PARROT_AMBIENT;
        }
    }

    public static SoundEvent getImitatedSound(int p_191999_0_)
    {
        return IMITATION_SOUND_EVENTS.containsKey(p_191999_0_) ? (SoundEvent)IMITATION_SOUND_EVENTS.get(p_191999_0_) : SoundEvents.ENTITY_PARROT_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_PARROT_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_PARROT_DEATH;
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15F, 1.0F);
    }

    protected float playFlySound(float p_191954_1_)
    {
        playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15F, 1.0F);
        return p_191954_1_ + flapSpeed / 2.0F;
    }

    protected boolean makeFlySound()
    {
        return true;
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    protected float getSoundPitch()
    {
        return getPitch(rand);
    }

    private static float getPitch(Random random)
    {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    public SoundCategory getSoundCategory()
    {
        return SoundCategory.NEUTRAL;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return true;
    }

    protected void collideWithEntity(Entity entityIn)
    {
        if (!(entityIn instanceof EntityPlayer))
        {
            super.collideWithEntity(entityIn);
        }
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (aiSit != null)
            {
                aiSit.setSitting(false);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    public int getVariant()
    {
        return MathHelper.clamp(((Integer) dataManager.get(VARIANT)).intValue(), 0, 4);
    }

    public void setVariant(int p_191997_1_)
    {
        dataManager.set(VARIANT, Integer.valueOf(p_191997_1_));
    }

    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(VARIANT, Integer.valueOf(0));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", getVariant());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        setVariant(compound.getInteger("Variant"));
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_PARROT;
    }

    public boolean isFlying()
    {
        return !onGround;
    }

    static
    {
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityBlaze.class), SoundEvents.E_PARROT_IM_BLAZE);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityCaveSpider.class), SoundEvents.E_PARROT_IM_SPIDER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityCreeper.class), SoundEvents.E_PARROT_IM_CREEPER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityElderGuardian.class), SoundEvents.E_PARROT_IM_ELDER_GUARDIAN);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityDragon.class), SoundEvents.E_PARROT_IM_ENDERDRAGON);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityEnderman.class), SoundEvents.E_PARROT_IM_ENDERMAN);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityEndermite.class), SoundEvents.E_PARROT_IM_ENDERMITE);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityEvoker.class), SoundEvents.E_PARROT_IM_EVOCATION_ILLAGER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityGhast.class), SoundEvents.E_PARROT_IM_GHAST);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityHusk.class), SoundEvents.E_PARROT_IM_HUSK);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityIllusionIllager.class), SoundEvents.E_PARROT_IM_ILLUSION_ILLAGER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityMagmaCube.class), SoundEvents.E_PARROT_IM_MAGMACUBE);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityPigZombie.class), SoundEvents.E_PARROT_IM_ZOMBIE_PIGMAN);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityPolarBear.class), SoundEvents.E_PARROT_IM_POLAR_BEAR);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityShulker.class), SoundEvents.E_PARROT_IM_SHULKER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntitySilverfish.class), SoundEvents.E_PARROT_IM_SILVERFISH);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntitySkeleton.class), SoundEvents.E_PARROT_IM_SKELETON);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntitySlime.class), SoundEvents.E_PARROT_IM_SLIME);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntitySpider.class), SoundEvents.E_PARROT_IM_SPIDER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityStray.class), SoundEvents.E_PARROT_IM_STRAY);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityVex.class), SoundEvents.E_PARROT_IM_VEX);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityVindicator.class), SoundEvents.E_PARROT_IM_VINDICATION_ILLAGER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityWitch.class), SoundEvents.E_PARROT_IM_WITCH);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityWither.class), SoundEvents.E_PARROT_IM_WITHER);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityWitherSkeleton.class), SoundEvents.E_PARROT_IM_WITHER_SKELETON);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityWolf.class), SoundEvents.E_PARROT_IM_WOLF);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityZombie.class), SoundEvents.E_PARROT_IM_ZOMBIE);
        IMITATION_SOUND_EVENTS.put(EntityList.REGISTRY.getIDForObject(EntityZombieVillager.class), SoundEvents.E_PARROT_IM_ZOMBIE_VILLAGER);
    }
}
