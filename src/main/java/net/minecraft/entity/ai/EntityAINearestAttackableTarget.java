package net.minecraft.entity.ai;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.entity.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.Comparator;
import java.util.List;

public class EntityAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAITarget {

	protected final Class<T> targetClass;
	/**
	 * Instance of EntityAINearestAttackableTargetSorter.
	 */
	protected final EntityAINearestAttackableTarget.Sorter sorter;
	protected final Predicate<? super T> targetEntitySelector;
	private final int targetChance;
	protected T targetEntity;

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {

		this(creature, classTarget, checkSight, false);
	}

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {

		this(creature, classTarget, 10, checkSight, onlyNearby, null);
	}

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, final Predicate<? super T> targetSelector) {

		super(creature, checkSight, onlyNearby);
		targetClass = classTarget;
		targetChance = chance;
		sorter = new EntityAINearestAttackableTarget.Sorter(creature);
		setMutexBits(1);
		targetEntitySelector = (Predicate<T>) p_apply_1_ -> {

			if (p_apply_1_ == null) {
				return false;
			} else if (targetSelector != null && !targetSelector.apply(p_apply_1_)) {
				return false;
			} else {
				return EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) && isSuitableTarget(p_apply_1_, false);
			}
		};
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {

		if (targetChance > 0 && taskOwner.getRNG().nextInt(targetChance) != 0) {
			return false;
		} else if (targetClass != EntityPlayer.class && targetClass != EntityPlayerMP.class) {
			List<T> list = taskOwner.world.getEntitiesWithinAABB(targetClass, getTargetableArea(getTargetDistance()), targetEntitySelector);

			if (list.isEmpty()) {
				return false;
			} else {
				list.sort(sorter);
				targetEntity = list.getFirst();
				return true;
			}
		} else {
			targetEntity = (T) taskOwner.world.getNearestAttackablePlayer(taskOwner.posX, taskOwner.posY + (double) taskOwner.getEyeHeight(), taskOwner.posZ, getTargetDistance(), getTargetDistance(), new Function<>() {
				
				public Double apply(EntityPlayer p_apply_1_) {

					ItemStack itemstack = p_apply_1_.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

					if (itemstack.getItem() == Items.SKULL) {
						int i = itemstack.getItemDamage();
						boolean flag = taskOwner instanceof EntitySkeleton && i == 0;
						boolean flag1 = taskOwner instanceof EntityZombie && i == 2;
						boolean flag2 = taskOwner instanceof EntityCreeper && i == 4;

						if (flag || flag1 || flag2) {
							return 0.5D;
						}
					}

					return 1D;
				}
			}, (Predicate<EntityPlayer>) targetEntitySelector);
			return targetEntity != null;
		}
	}

	protected AxisAlignedBB getTargetableArea(double targetDistance) {

		return taskOwner.getEntityBoundingBox().grow(targetDistance, 4D, targetDistance);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {

		taskOwner.setAttackTarget(targetEntity);
		super.startExecuting();
	}

	public static class Sorter implements Comparator<Entity> {

		private final Entity entity;

		public Sorter(Entity entityIn) {

			entity = entityIn;
		}

		public int compare(Entity p_compare_1_, Entity p_compare_2_) {

			double d0 = entity.getDistanceSq(p_compare_1_);
			double d1 = entity.getDistanceSq(p_compare_2_);

			if (d0 < d1) {
				return -1;
			} else {
				return d0 > d1 ? 1 : 0;
			}
		}

	}

}
