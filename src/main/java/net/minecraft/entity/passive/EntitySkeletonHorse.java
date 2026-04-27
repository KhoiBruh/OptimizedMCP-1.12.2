package net.minecraft.entity.passive;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISkeletonRiders;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class EntitySkeletonHorse extends AbstractHorse {

	private final EntityAISkeletonRiders skeletonTrapAI = new EntityAISkeletonRiders(this);
	private boolean skeletonTrap;
	private int skeletonTrapTime;

	public EntitySkeletonHorse(World worldIn) {

		super(worldIn);
	}

	public static void registerFixesSkeletonHorse(DataFixer fixer) {

		AbstractHorse.registerFixesAbstractHorse(fixer, EntitySkeletonHorse.class);
	}

	protected void applyEntityAttributes() {

		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
		getEntityAttribute(JUMP_STRENGTH).setBaseValue(getModifiedJumpStrength());
	}

	protected SoundEvent getAmbientSound() {

		super.getAmbientSound();
		return SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT;
	}

	protected SoundEvent getDeathSound() {

		super.getDeathSound();
		return SoundEvents.ENTITY_SKELETON_HORSE_DEATH;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

		super.getHurtSound(damageSourceIn);
		return SoundEvents.ENTITY_SKELETON_HORSE_HURT;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	public EnumCreatureAttribute getCreatureAttribute() {

		return EnumCreatureAttribute.UNDEAD;
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	public double getMountedYOffset() {

		return super.getMountedYOffset() - 0.1875D;
	}

	@Nullable
	protected ResourceLocation getLootTable() {

		return LootTableList.ENTITIES_SKELETON_HORSE;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate() {

		super.onLivingUpdate();

		if (isTrap() && skeletonTrapTime++ >= 18000) {
			setDead();
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setBoolean("SkeletonTrap", isTrap());
		compound.setInteger("SkeletonTrapTime", skeletonTrapTime);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		setTrap(compound.getBoolean("SkeletonTrap"));
		skeletonTrapTime = compound.getInteger("SkeletonTrapTime");
	}

	public boolean isTrap() {

		return skeletonTrap;
	}

	public void setTrap(boolean trap) {

		if (trap != skeletonTrap) {
			skeletonTrap = trap;

			if (trap) {
				tasks.addTask(1, skeletonTrapAI);
			} else {
				tasks.removeTask(skeletonTrapAI);
			}
		}
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand) {

		ItemStack itemstack = player.getHeldItem(hand);
		boolean flag = !itemstack.isEmpty();

		if (flag && itemstack.getItem() == Items.SPAWN_EGG) {
			return super.processInteract(player, hand);
		} else if (!isTame()) {
			return false;
		} else if (isChild()) {
			return super.processInteract(player, hand);
		} else if (player.isSneaking()) {
			openGUI(player);
			return true;
		} else if (isBeingRidden()) {
			return super.processInteract(player, hand);
		} else {
			if (flag) {
				if (itemstack.getItem() == Items.SADDLE && !isHorseSaddled()) {
					openGUI(player);
					return true;
				}

				if (itemstack.interactWithEntity(player, this, hand)) {
					return true;
				}
			}

			mountTo(player);
			return true;
		}
	}

}
