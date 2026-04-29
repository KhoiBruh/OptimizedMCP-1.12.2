package net.minecraft.entity.monster;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import java.util.UUID;

public class EntityZombieVillager extends EntityZombie {

	private static final DataParameter<Boolean> CONVERTING = EntityDataManager.createKey(EntityZombieVillager.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(EntityZombieVillager.class, DataSerializers.VARINT);

	/**
	 * Ticker used to determine the time remaining for this zombie to convert into a villager when cured.
	 */
	private int conversionTime;

	/**
	 * The entity that started the conversion, used for the {@link CriteriaTriggers#CURED_ZOMBIE_VILLAGER} advancement
	 * criteria
	 */
	private UUID converstionStarter;

	public EntityZombieVillager(World worldIn) {

		super(worldIn);
	}

	public static void registerFixesZombieVillager(DataFixer fixer) {

		EntityLiving.registerFixesMob(fixer, EntityZombieVillager.class);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(CONVERTING, false);
		dataManager.register(PROFESSION, 0);
	}

	public int getProfession() {

		return Math.max(dataManager.get(PROFESSION) % 6, 0);
	}

	public void setProfession(int profession) {

		dataManager.set(PROFESSION, profession);
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setInteger("Profession", getProfession());
		compound.setInteger("ConversionTime", isConverting() ? conversionTime : -1);

		if (converstionStarter != null) {
			compound.setUniqueId("ConversionPlayer", converstionStarter);
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		setProfession(compound.getInteger("Profession"));

		if (compound.hasKey("ConversionTime", 99) && compound.getInteger("ConversionTime") > -1) {
			startConverting(compound.hasUniqueId("ConversionPlayer") ? compound.getUniqueId("ConversionPlayer") : null, compound.getInteger("ConversionTime"));
		}
	}

	

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
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {

		setProfession(world.rand.nextInt(6));
		return super.onInitialSpawn(difficulty, livingdata);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		if (!world.isRemote && isConverting()) {
			int i = getConversionProgress();
			conversionTime -= i;

			if (conversionTime <= 0) {
				finishConversion();
			}
		}

		super.onUpdate();
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand) {

		ItemStack itemstack = player.getHeldItem(hand);

		if (itemstack.getItem() == Items.GOLDEN_APPLE && itemstack.getMetadata() == 0 && isPotionActive(MobEffects.WEAKNESS)) {
			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}

			if (!world.isRemote) {
				startConverting(player.getUniqueID(), rand.nextInt(2401) + 3600);
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines if an entity can be despawned, used on idle far away entities
	 */
	protected boolean canDespawn() {

		return !isConverting();
	}

	/**
	 * Returns whether this zombie is in the process of converting to a villager
	 */
	public boolean isConverting() {

		return getDataManager().get(CONVERTING);
	}

	/**
	 * Starts conversion of this zombie villager to a villager
	 *
	 * @param conversionStarterIn The entity that started the conversion's UUID
	 * @param conversionTimeIn    The time that it will take to finish conversion
	 */
	protected void startConverting(UUID conversionStarterIn, int conversionTimeIn) {

		converstionStarter = conversionStarterIn;
		conversionTime = conversionTimeIn;
		getDataManager().set(CONVERTING, true);
		removePotionEffect(MobEffects.WEAKNESS);
		addPotionEffect(new PotionEffect(MobEffects.STRENGTH, conversionTimeIn, Math.min(world.getDifficulty().getDifficultyId() - 1, 0)));
		world.setEntityState(this, (byte) 16);
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id == 16) {
			if (!isSilent()) {
				world.playSound(posX + 0.5D, posY + 0.5D, posZ + 0.5D, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, getSoundCategory(), 1F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}
		} else {
			super.handleStatusUpdate(id);
		}
	}

	protected void finishConversion() {

		EntityVillager entityvillager = new EntityVillager(world);
		entityvillager.copyLocationAndAnglesFrom(this);
		entityvillager.setProfession(getProfession());
		entityvillager.finalizeMobSpawn(world.getDifficultyForLocation(new BlockPos(entityvillager)), null, false);
		entityvillager.setLookingForHome();

		if (isChild()) {
			entityvillager.setGrowingAge(-24000);
		}

		world.removeEntity(this);
		entityvillager.setNoAI(isAIDisabled());

		if (hasCustomName()) {
			entityvillager.setCustomNameTag(getCustomNameTag());
			entityvillager.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
		}

		world.spawnEntity(entityvillager);

		if (converstionStarter != null) {
			EntityPlayer entityplayer = world.getPlayerEntityByUUID(converstionStarter);

			if (entityplayer instanceof EntityPlayerMP) {
				CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((EntityPlayerMP) entityplayer, this, entityvillager);
			}
		}

		entityvillager.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
		world.playEvent(null, 1027, new BlockPos((int) posX, (int) posY, (int) posZ), 0);
	}

	protected int getConversionProgress() {

		int i = 1;

		if (rand.nextFloat() < 0.01F) {
			int j = 0;
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			for (int k = (int) posX - 4; k < (int) posX + 4 && j < 14; ++k) {
				for (int l = (int) posY - 4; l < (int) posY + 4 && j < 14; ++l) {
					for (int i1 = (int) posZ - 4; i1 < (int) posZ + 4 && j < 14; ++i1) {
						Block block = world.getBlockState(blockpos$mutableblockpos.setPos(k, l, i1)).getBlock();

						if (block == Blocks.IRON_BARS || block == Blocks.BED) {
							if (rand.nextFloat() < 0.3F) {
								++i;
							}

							++j;
						}
					}
				}
			}
		}

		return i;
	}

	/**
	 * Gets the pitch of living sounds in living entities.
	 */
	protected float getSoundPitch() {

		return isChild() ? (rand.nextFloat() - rand.nextFloat()) * 0.2F + 2F : (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1F;
	}

	public SoundEvent getAmbientSound() {

		return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
	}

	public SoundEvent getHurtSound(DamageSource damageSourceIn) {

		return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
	}

	public SoundEvent getDeathSound() {

		return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
	}

	public SoundEvent getStepSound() {

		return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
	}

	
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_ZOMBIE_VILLAGER;
	}

	protected ItemStack getSkullDrop() {

		return ItemStack.EMPTY;
	}

}
