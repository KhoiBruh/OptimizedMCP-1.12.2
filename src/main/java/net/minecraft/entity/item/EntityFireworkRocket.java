package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityFireworkRocket extends Entity {

	private static final DataParameter<ItemStack> FIREWORK_ITEM = EntityDataManager.createKey(EntityFireworkRocket.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<Integer> BOOSTED_ENTITY_ID = EntityDataManager.createKey(EntityFireworkRocket.class, DataSerializers.VARINT);

	/**
	 * The age of the firework in ticks.
	 */
	private int fireworkAge;

	/**
	 * The lifetime of the firework in ticks. When the age reaches the lifetime the firework explodes.
	 */
	private int lifetime;
	private EntityLivingBase boostedEntity;

	public EntityFireworkRocket(World worldIn) {

		super(worldIn);
		setSize(0.25F, 0.25F);
	}

	protected void entityInit() {

		dataManager.register(FIREWORK_ITEM, ItemStack.EMPTY);
		dataManager.register(BOOSTED_ENTITY_ID, Integer.valueOf(0));
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	public boolean isInRangeToRenderDist(double distance) {

		return distance < 4096.0D && !isAttachedToEntity();
	}

	public boolean isInRangeToRender3d(double x, double y, double z) {

		return super.isInRangeToRender3d(x, y, z) && !isAttachedToEntity();
	}

	public EntityFireworkRocket(World worldIn, double x, double y, double z, ItemStack givenItem) {

		super(worldIn);
		fireworkAge = 0;
		setSize(0.25F, 0.25F);
		setPosition(x, y, z);
		int i = 1;

		if (!givenItem.isEmpty() && givenItem.hasTagCompound()) {
			dataManager.set(FIREWORK_ITEM, givenItem.copy());
			NBTTagCompound nbttagcompound = givenItem.getTagCompound();
			NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Fireworks");
			i += nbttagcompound1.getByte("Flight");
		}

		motionX = rand.nextGaussian() * 0.001D;
		motionZ = rand.nextGaussian() * 0.001D;
		motionY = 0.05D;
		lifetime = 10 * i + rand.nextInt(6) + rand.nextInt(7);
	}

	public EntityFireworkRocket(World p_i47367_1_, ItemStack p_i47367_2_, EntityLivingBase p_i47367_3_) {

		this(p_i47367_1_, p_i47367_3_.posX, p_i47367_3_.posY, p_i47367_3_.posZ, p_i47367_2_);
		dataManager.set(BOOSTED_ENTITY_ID, Integer.valueOf(p_i47367_3_.getEntityId()));
		boostedEntity = p_i47367_3_;
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
	public void setVelocity(double x, double y, double z) {

		motionX = x;
		motionY = y;
		motionZ = z;

		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(x * x + z * z);
			rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
			rotationPitch = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
			prevRotationYaw = rotationYaw;
			prevRotationPitch = rotationPitch;
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		super.onUpdate();

		if (isAttachedToEntity()) {
			if (boostedEntity == null) {
				Entity entity = world.getEntityByID(dataManager.get(BOOSTED_ENTITY_ID).intValue());

				if (entity instanceof EntityLivingBase) {
					boostedEntity = (EntityLivingBase) entity;
				}
			}

			if (boostedEntity != null) {
				if (boostedEntity.isElytraFlying()) {
					Vec3d vec3d = boostedEntity.getLookVec();
					double d0 = 1.5D;
					double d1 = 0.1D;
					boostedEntity.motionX += vec3d.x() * 0.1D + (vec3d.x() * 1.5D - boostedEntity.motionX) * 0.5D;
					boostedEntity.motionY += vec3d.y() * 0.1D + (vec3d.y() * 1.5D - boostedEntity.motionY) * 0.5D;
					boostedEntity.motionZ += vec3d.z() * 0.1D + (vec3d.z() * 1.5D - boostedEntity.motionZ) * 0.5D;
				}

				setPosition(boostedEntity.posX, boostedEntity.posY, boostedEntity.posZ);
				motionX = boostedEntity.motionX;
				motionY = boostedEntity.motionY;
				motionZ = boostedEntity.motionZ;
			}
		} else {
			motionX *= 1.15D;
			motionZ *= 1.15D;
			motionY += 0.04D;
			move(MoverType.SELF, motionX, motionY, motionZ);
		}

		float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

		for (rotationPitch = (float) (MathHelper.atan2(motionY, f) * (180D / Math.PI)); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F) {
		}

		while (rotationPitch - prevRotationPitch >= 180.0F) {
			prevRotationPitch += 360.0F;
		}

		while (rotationYaw - prevRotationYaw < -180.0F) {
			prevRotationYaw -= 360.0F;
		}

		while (rotationYaw - prevRotationYaw >= 180.0F) {
			prevRotationYaw += 360.0F;
		}

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

		if (fireworkAge == 0 && !isSilent()) {
			world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
		}

		++fireworkAge;

		if (world.isRemote && fireworkAge % 2 < 2) {
			world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, posX, posY - 0.3D, posZ, rand.nextGaussian() * 0.05D, -motionY * 0.5D, rand.nextGaussian() * 0.05D);
		}

		if (!world.isRemote && fireworkAge > lifetime) {
			world.setEntityState(this, (byte) 17);
			dealExplosionDamage();
			setDead();
		}
	}

	private void dealExplosionDamage() {

		float f = 0.0F;
		ItemStack itemstack = dataManager.get(FIREWORK_ITEM);
		NBTTagCompound nbttagcompound = itemstack.isEmpty() ? null : itemstack.getSubCompound("Fireworks");
		NBTTagList nbttaglist = nbttagcompound != null ? nbttagcompound.getTagList("Explosions", 10) : null;

		if (nbttaglist != null && !nbttaglist.hasNoTags()) {
			f = (float) (5 + nbttaglist.tagCount() * 2);
		}

		if (f > 0.0F) {
			if (boostedEntity != null) {
				boostedEntity.attackEntityFrom(DamageSource.FIREWORKS, (float) (5 + nbttaglist.tagCount() * 2));
			}

			double d0 = 5.0D;
			Vec3d vec3d = new Vec3d(posX, posY, posZ);

			for (EntityLivingBase entitylivingbase : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(5.0D))) {
				if (entitylivingbase != boostedEntity && getDistanceSq(entitylivingbase) <= 25.0D) {
					boolean flag = false;

					for (int i = 0; i < 2; ++i) {
						RayTraceResult raytraceresult = world.rayTraceBlocks(vec3d, new Vec3d(entitylivingbase.posX, entitylivingbase.posY + (double) entitylivingbase.height * 0.5D * (double) i, entitylivingbase.posZ), false, true, false);

						if (raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS) {
							flag = true;
							break;
						}
					}

					if (flag) {
						float f1 = f * (float) Math.sqrt((5.0D - (double) getDistance(entitylivingbase)) / 5.0D);
						entitylivingbase.attackEntityFrom(DamageSource.FIREWORKS, f1);
					}
				}
			}
		}
	}

	public boolean isAttachedToEntity() {

		return dataManager.get(BOOSTED_ENTITY_ID).intValue() > 0;
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id == 17 && world.isRemote) {
			ItemStack itemstack = dataManager.get(FIREWORK_ITEM);
			NBTTagCompound nbttagcompound = itemstack.isEmpty() ? null : itemstack.getSubCompound("Fireworks");
			world.makeFireworks(posX, posY, posZ, motionX, motionY, motionZ, nbttagcompound);
		}

		super.handleStatusUpdate(id);
	}

	public static void registerFixesFireworkRocket(DataFixer fixer) {

		fixer.registerWalker(FixTypes.ENTITY, new ItemStackData(EntityFireworkRocket.class, "FireworksItem"));
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		compound.setInteger("Life", fireworkAge);
		compound.setInteger("LifeTime", lifetime);
		ItemStack itemstack = dataManager.get(FIREWORK_ITEM);

		if (!itemstack.isEmpty()) {
			compound.setTag("FireworksItem", itemstack.writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		fireworkAge = compound.getInteger("Life");
		lifetime = compound.getInteger("LifeTime");
		NBTTagCompound nbttagcompound = compound.getCompoundTag("FireworksItem");

		if (nbttagcompound != null) {
			ItemStack itemstack = new ItemStack(nbttagcompound);

			if (!itemstack.isEmpty()) {
				dataManager.set(FIREWORK_ITEM, itemstack);
			}
		}
	}

	/**
	 * Returns true if it's possible to attack this entity with an item.
	 */
	public boolean canBeAttackedWithItem() {

		return false;
	}

}
