package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.List;

public class EntityFishHook extends Entity {

	private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.createKey(EntityFishHook.class, DataSerializers.VARINT);
	public Entity caughtEntity;
	private boolean inGround;
	private int ticksInGround;
	private EntityPlayer angler;
	private int ticksInAir;
	private int ticksCatchable;
	private int ticksCaughtDelay;
	private int ticksCatchableDelay;
	private float fishApproachAngle;
	private EntityFishHook.State currentState = EntityFishHook.State.FLYING;
	private int luck;
	private int lureSpeed;

	public EntityFishHook(World worldIn, EntityPlayer p_i47290_2_, double x, double y, double z) {

		super(worldIn);
		init(p_i47290_2_);
		setPosition(x, y, z);
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
	}

	public EntityFishHook(World worldIn, EntityPlayer fishingPlayer) {

		super(worldIn);
		init(fishingPlayer);
		shoot();
	}

	private void init(EntityPlayer p_190626_1_) {

		setSize(0.25F, 0.25F);
		ignoreFrustumCheck = true;
		angler = p_190626_1_;
		angler.fishEntity = this;
	}

	public void setLureSpeed(int p_191516_1_) {

		lureSpeed = p_191516_1_;
	}

	public void setLuck(int p_191517_1_) {

		luck = p_191517_1_;
	}

	private void shoot() {

		float f = angler.prevRotationPitch + (angler.rotationPitch - angler.prevRotationPitch);
		float f1 = angler.prevRotationYaw + (angler.rotationYaw - angler.prevRotationYaw);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		double d0 = angler.prevPosX + (angler.posX - angler.prevPosX) - (double) f3 * 0.3D;
		double d1 = angler.prevPosY + (angler.posY - angler.prevPosY) + (double) angler.getEyeHeight();
		double d2 = angler.prevPosZ + (angler.posZ - angler.prevPosZ) - (double) f2 * 0.3D;
		setLocationAndAngles(d0, d1, d2, f1, f);
		motionX = -f3;
		motionY = MathHelper.clamp(-(f5 / f4), -5F, 5F);
		motionZ = -f2;
		float f6 = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		motionX *= 0.6D / (double) f6 + 0.5D + rand.nextGaussian() * 0.0045D;
		motionY *= 0.6D / (double) f6 + 0.5D + rand.nextGaussian() * 0.0045D;
		motionZ *= 0.6D / (double) f6 + 0.5D + rand.nextGaussian() * 0.0045D;
		float f7 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));
		rotationPitch = (float) (MathHelper.atan2(motionY, f7) * (180D / Math.PI));
		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
	}

	protected void entityInit() {

		getDataManager().register(DATA_HOOKED_ENTITY, 0);
	}

	public void notifyDataManagerChange(DataParameter<?> key) {

		if (DATA_HOOKED_ENTITY.equals(key)) {
			int i = getDataManager().get(DATA_HOOKED_ENTITY);
			caughtEntity = i > 0 ? world.getEntityByID(i - 1) : null;
		}

		super.notifyDataManagerChange(key);
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	public boolean isInRangeToRenderDist(double distance) {

		double d0 = 64D;
		return distance < 4096D;
	}

	/**
	 * Set the position and rotation values directly without any clamping.
	 */
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {

	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		super.onUpdate();

		if (angler == null) {
			setDead();
		} else if (world.isRemote || !shouldStopFishing()) {
			if (inGround) {
				++ticksInGround;

				if (ticksInGround >= 1200) {
					setDead();
					return;
				}
			}

			float f = 0F;
			BlockPos blockpos = new BlockPos(this);
			IBlockState iblockstate = world.getBlockState(blockpos);

			if (iblockstate.getMaterial() == Material.WATER) {
				f = BlockLiquid.getBlockLiquidHeight(iblockstate, world, blockpos);
			}

			if (currentState == EntityFishHook.State.FLYING) {
				if (caughtEntity != null) {
					motionX = 0D;
					motionY = 0D;
					motionZ = 0D;
					currentState = EntityFishHook.State.HOOKED_IN_ENTITY;
					return;
				}

				if (f > 0F) {
					motionX *= 0.3D;
					motionY *= 0.2D;
					motionZ *= 0.3D;
					currentState = EntityFishHook.State.BOBBING;
					return;
				}

				if (!world.isRemote) {
					checkCollision();
				}

				if (!inGround && !onGround && !collidedHorizontally) {
					++ticksInAir;
				} else {
					ticksInAir = 0;
					motionX = 0D;
					motionY = 0D;
					motionZ = 0D;
				}
			} else {
				if (currentState == EntityFishHook.State.HOOKED_IN_ENTITY) {
					if (caughtEntity != null) {
						if (caughtEntity.isDead) {
							caughtEntity = null;
							currentState = EntityFishHook.State.FLYING;
						} else {
							posX = caughtEntity.posX;
							double d2 = caughtEntity.height;
							posY = caughtEntity.getEntityBoundingBox().minY + d2 * 0.8D;
							posZ = caughtEntity.posZ;
							setPosition(posX, posY, posZ);
						}
					}

					return;
				}

				if (currentState == EntityFishHook.State.BOBBING) {
					motionX *= 0.9D;
					motionZ *= 0.9D;
					double d0 = posY + motionY - (double) blockpos.getY() - (double) f;

					if (Math.abs(d0) < 0.01D) {
						d0 += Math.signum(d0) * 0.1D;
					}

					motionY -= d0 * (double) rand.nextFloat() * 0.2D;

					if (!world.isRemote && f > 0F) {
						catchingFish(blockpos);
					}
				}
			}

			if (iblockstate.getMaterial() != Material.WATER) {
				motionY -= 0.03D;
			}

			move(MoverType.SELF, motionX, motionY, motionZ);
			updateRotation();
			double d1 = 0.92D;
			motionX *= 0.92D;
			motionY *= 0.92D;
			motionZ *= 0.92D;
			setPosition(posX, posY, posZ);
		}
	}

	private boolean shouldStopFishing() {

		ItemStack itemstack = angler.getHeldItemMainhand();
		ItemStack itemstack1 = angler.getHeldItemOffhand();
		boolean flag = itemstack.getItem() == Items.FISHING_ROD;
		boolean flag1 = itemstack1.getItem() == Items.FISHING_ROD;

		if (!angler.isDead && angler.isEntityAlive() && (flag || flag1) && getDistanceSq(angler) <= 1024D) {
			return false;
		} else {
			setDead();
			return true;
		}
	}

	private void updateRotation() {

		float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

		for (rotationPitch = (float) (MathHelper.atan2(motionY, f) * (180D / Math.PI)); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) {
		}

		while (rotationPitch - prevRotationPitch >= 180F) {
			prevRotationPitch += 360F;
		}

		while (rotationYaw - prevRotationYaw < -180F) {
			prevRotationYaw -= 360F;
		}

		while (rotationYaw - prevRotationYaw >= 180F) {
			prevRotationYaw += 360F;
		}

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
	}

	private void checkCollision() {

		Vec3d vec3d = new Vec3d(posX, posY, posZ);
		Vec3d vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
		RayTraceResult raytraceresult = world.rayTraceBlocks(vec3d, vec3d1, false, true, false);
		vec3d = new Vec3d(posX, posY, posZ);
		vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

		if (raytraceresult != null) {
			vec3d1 = new Vec3d(raytraceresult.hitVec.x(), raytraceresult.hitVec.y(), raytraceresult.hitVec.z());
		}

		Entity entity = null;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1D));
		double d0 = 0D;

		for (Entity entity1 : list) {
			if (canBeHooked(entity1) && (entity1 != angler || ticksInAir >= 5)) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
				RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

				if (raytraceresult1 != null) {
					double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);

					if (d1 < d0 || d0 == 0D) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		if (entity != null) {
			raytraceresult = new RayTraceResult(entity);
		}

		if (raytraceresult != null && raytraceresult.typeOfHit != RayTraceResult.Type.MISS) {
			if (raytraceresult.typeOfHit == RayTraceResult.Type.ENTITY) {
				caughtEntity = raytraceresult.entityHit;
				setHookedEntity();
			} else {
				inGround = true;
			}
		}
	}

	private void setHookedEntity() {

		getDataManager().set(DATA_HOOKED_ENTITY, caughtEntity.getEntityId() + 1);
	}

	private void catchingFish(BlockPos p_190621_1_) {

		WorldServer worldserver = (WorldServer) world;
		int i = 1;
		BlockPos blockpos = p_190621_1_.up();

		if (rand.nextFloat() < 0.25F && world.isRainingAt(blockpos)) {
			++i;
		}

		if (rand.nextFloat() < 0.5F && !world.canSeeSky(blockpos)) {
			--i;
		}

		if (ticksCatchable > 0) {
			--ticksCatchable;

			if (ticksCatchable <= 0) {
				ticksCaughtDelay = 0;
				ticksCatchableDelay = 0;
			} else {
				motionY -= 0.2D * (double) rand.nextFloat() * (double) rand.nextFloat();
			}
		} else if (ticksCatchableDelay > 0) {
			ticksCatchableDelay -= i;

			if (ticksCatchableDelay > 0) {
				fishApproachAngle = (float) ((double) fishApproachAngle + rand.nextGaussian() * 4D);
				float f = fishApproachAngle * 0.017453292F;
				float f1 = MathHelper.sin(f);
				float f2 = MathHelper.cos(f);
				double d0 = posX + (double) (f1 * (float) ticksCatchableDelay * 0.1F);
				double d1 = (float) MathHelper.floor(getEntityBoundingBox().minY) + 1F;
				double d2 = posZ + (double) (f2 * (float) ticksCatchableDelay * 0.1F);
				Block block = worldserver.getBlockState(new BlockPos(d0, d1 - 1D, d2)).getBlock();

				if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
					if (rand.nextFloat() < 0.15F) {
						worldserver.spawnParticle(ParticleTypes.WATER_BUBBLE, d0, d1 - 0.10000000149011612D, d2, 1, f1, 0.1D, f2, 0D);
					}

					float f3 = f1 * 0.04F;
					float f4 = f2 * 0.04F;
					worldserver.spawnParticle(ParticleTypes.WATER_WAKE, d0, d1, d2, 0, f4, 0.01D, -f3, 1D);
					worldserver.spawnParticle(ParticleTypes.WATER_WAKE, d0, d1, d2, 0, -f4, 0.01D, f3, 1D);
				}
			} else {
				motionY = -0.4F * MathHelper.nextFloat(rand, 0.6F, 1F);
				playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
				double d3 = getEntityBoundingBox().minY + 0.5D;
				worldserver.spawnParticle(ParticleTypes.WATER_BUBBLE, posX, d3, posZ, (int) (1F + width * 20F), width, 0D, width, 0.20000000298023224D);
				worldserver.spawnParticle(ParticleTypes.WATER_WAKE, posX, d3, posZ, (int) (1F + width * 20F), width, 0D, width, 0.20000000298023224D);
				ticksCatchable = MathHelper.getInt(rand, 20, 40);
			}
		} else if (ticksCaughtDelay > 0) {
			ticksCaughtDelay -= i;
			float f5 = 0.15F;

			if (ticksCaughtDelay < 20) {
				f5 = (float) ((double) f5 + (double) (20 - ticksCaughtDelay) * 0.05D);
			} else if (ticksCaughtDelay < 40) {
				f5 = (float) ((double) f5 + (double) (40 - ticksCaughtDelay) * 0.02D);
			} else if (ticksCaughtDelay < 60) {
				f5 = (float) ((double) f5 + (double) (60 - ticksCaughtDelay) * 0.01D);
			}

			if (rand.nextFloat() < f5) {
				float f6 = MathHelper.nextFloat(rand, 0F, 360F) * 0.017453292F;
				float f7 = MathHelper.nextFloat(rand, 25F, 60F);
				double d4 = posX + (double) (MathHelper.sin(f6) * f7 * 0.1F);
				double d5 = (float) MathHelper.floor(getEntityBoundingBox().minY) + 1F;
				double d6 = posZ + (double) (MathHelper.cos(f6) * f7 * 0.1F);
				Block block1 = worldserver.getBlockState(new BlockPos((int) d4, (int) d5 - 1, (int) d6)).getBlock();

				if (block1 == Blocks.WATER || block1 == Blocks.FLOWING_WATER) {
					worldserver.spawnParticle(ParticleTypes.WATER_SPLASH, d4, d5, d6, 2 + rand.nextInt(2), 0.10000000149011612D, 0D, 0.10000000149011612D, 0D);
				}
			}

			if (ticksCaughtDelay <= 0) {
				fishApproachAngle = MathHelper.nextFloat(rand, 0F, 360F);
				ticksCatchableDelay = MathHelper.getInt(rand, 20, 80);
			}
		} else {
			ticksCaughtDelay = MathHelper.getInt(rand, 100, 600);
			ticksCaughtDelay -= lureSpeed * 20 * 5;
		}
	}

	protected boolean canBeHooked(Entity p_189739_1_) {

		return p_189739_1_.canBeCollidedWith() || p_189739_1_ instanceof EntityItem;
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

	}

	public int handleHookRetraction() {

		if (!world.isRemote && angler != null) {
			int i = 0;

			if (caughtEntity != null) {
				bringInHookedEntity();
				world.setEntityState(this, (byte) 31);
				i = caughtEntity instanceof EntityItem ? 3 : 5;
			} else if (ticksCatchable > 0) {
				LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) world);
				lootcontext$builder.withLuck((float) luck + angler.getLuck());

				for (ItemStack itemstack : world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(rand, lootcontext$builder.build())) {
					EntityItem entityitem = new EntityItem(world, posX, posY, posZ, itemstack);
					double d0 = angler.posX - posX;
					double d1 = angler.posY - posY;
					double d2 = angler.posZ - posZ;
					double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
					double d4 = 0.1D;
					entityitem.motionX = d0 * 0.1D;
					entityitem.motionY = d1 * 0.1D + (double) MathHelper.sqrt(d3) * 0.08D;
					entityitem.motionZ = d2 * 0.1D;
					world.spawnEntity(entityitem);
					angler.world.spawnEntity(new EntityXPOrb(angler.world, angler.posX, angler.posY + 0.5D, angler.posZ + 0.5D, rand.nextInt(6) + 1));
					Item item = itemstack.getItem();

					if (item == Items.FISH || item == Items.COOKED_FISH) {
						angler.addStat(StatList.FISH_CAUGHT, 1);
					}
				}

				i = 1;
			}

			if (inGround) {
				i = 2;
			}

			setDead();
			return i;
		} else {
			return 0;
		}
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id == 31 && world.isRemote && caughtEntity instanceof EntityPlayer && ((EntityPlayer) caughtEntity).isUser()) {
			bringInHookedEntity();
		}

		super.handleStatusUpdate(id);
	}

	protected void bringInHookedEntity() {

		if (angler != null) {
			double d0 = angler.posX - posX;
			double d1 = angler.posY - posY;
			double d2 = angler.posZ - posZ;
			double d3 = 0.1D;
			caughtEntity.motionX += d0 * 0.1D;
			caughtEntity.motionY += d1 * 0.1D;
			caughtEntity.motionZ += d2 * 0.1D;
		}
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {

		return false;
	}

	/**
	 * Will get destroyed next tick.
	 */
	public void setDead() {

		super.setDead();

		if (angler != null) {
			angler.fishEntity = null;
		}
	}

	public EntityPlayer getAngler() {

		return angler;
	}

	enum State {
		FLYING,
		HOOKED_IN_ENTITY,
		BOBBING
	}

}
