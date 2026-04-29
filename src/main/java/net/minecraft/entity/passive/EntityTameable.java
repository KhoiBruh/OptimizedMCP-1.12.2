package net.minecraft.entity.passive;

import com.google.common.base.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import java.util.UUID;

public abstract class EntityTameable extends EntityAnimal implements IEntityOwnable {

	protected static final DataParameter<Byte> TAMED = EntityDataManager.createKey(EntityTameable.class, DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected EntityAISit aiSit;

	public EntityTameable(World worldIn) {

		super(worldIn);
		setupTamedAI();
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(TAMED, (byte) 0);
		dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);

		if (getOwnerId() == null) {
			compound.setString("OwnerUUID", "");
		} else {
			compound.setString("OwnerUUID", getOwnerId().toString());
		}

		compound.setBoolean("Sitting", isSitting());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		String s;

		if (compound.hasKey("OwnerUUID", 8)) {
			s = compound.getString("OwnerUUID");
		} else {
			String s1 = compound.getString("Owner");
			s = PreYggdrasilConverter.convertMobOwnerIfNeeded(getServer(), s1);
		}

		if (!s.isEmpty()) {
			try {
				setOwnerId(UUID.fromString(s));
				setTamed(true);
			} catch (Throwable var4) {
				setTamed(false);
			}
		}

		if (aiSit != null) {
			aiSit.setSitting(compound.getBoolean("Sitting"));
		}

		setSitting(compound.getBoolean("Sitting"));
	}

	public boolean canBeLeashedTo(EntityPlayer player) {

		return !getLeashed();
	}

	/**
	 * Play the taming effect, will either be hearts or smoke depending on status
	 */
	protected void playTameEffect(boolean play) {

		EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;

		if (!play) {
			enumparticletypes = EnumParticleTypes.SMOKE_NORMAL;
		}

		for (int i = 0; i < 7; ++i) {
			double d0 = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			world.spawnParticle(enumparticletypes, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
		}
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id == 7) {
			playTameEffect(true);
		} else if (id == 6) {
			playTameEffect(false);
		} else {
			super.handleStatusUpdate(id);
		}
	}

	public boolean isTamed() {

		return (dataManager.get(TAMED) & 4) != 0;
	}

	public void setTamed(boolean tamed) {

		byte b0 = dataManager.get(TAMED);

		if (tamed) {
			dataManager.set(TAMED, (byte) (b0 | 4));
		} else {
			dataManager.set(TAMED, (byte) (b0 & -5));
		}

		setupTamedAI();
	}

	protected void setupTamedAI() {

	}

	public boolean isSitting() {

		return (dataManager.get(TAMED) & 1) != 0;
	}

	public void setSitting(boolean sitting) {

		byte b0 = dataManager.get(TAMED);

		if (sitting) {
			dataManager.set(TAMED, (byte) (b0 | 1));
		} else {
			dataManager.set(TAMED, (byte) (b0 & -2));
		}
	}

	
	public UUID getOwnerId() {

		return dataManager.get(OWNER_UNIQUE_ID).orNull();
	}

	public void setOwnerId(UUID p_184754_1_) {

		dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}

	public void setTamedBy(EntityPlayer player) {

		setTamed(true);
		setOwnerId(player.getUniqueID());

		if (player instanceof EntityPlayerMP) {
			CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) player, this);
		}
	}

	
	public EntityLivingBase getOwner() {

		try {
			UUID uuid = getOwnerId();
			return uuid == null ? null : world.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean isOwner(EntityLivingBase entityIn) {

		return entityIn == getOwner();
	}

	/**
	 * Returns the AITask responsible of the sit logic
	 */
	public EntityAISit getAISit() {

		return aiSit;
	}

	public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {

		return true;
	}

	public Team getTeam() {

		if (isTamed()) {
			EntityLivingBase entitylivingbase = getOwner();

			if (entitylivingbase != null) {
				return entitylivingbase.getTeam();
			}
		}

		return super.getTeam();
	}

	/**
	 * Returns whether this Entity is on the same team as the given Entity.
	 */
	public boolean isOnSameTeam(Entity entityIn) {

		if (isTamed()) {
			EntityLivingBase entitylivingbase = getOwner();

			if (entityIn == entitylivingbase) {
				return true;
			}

			if (entitylivingbase != null) {
				return entitylivingbase.isOnSameTeam(entityIn);
			}
		}

		return super.isOnSameTeam(entityIn);
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	public void onDeath(DamageSource cause) {

		if (!world.isRemote && world.getGameRules().getBoolean("showDeathMessages") && getOwner() instanceof EntityPlayerMP) {
			getOwner().sendMessage(getCombatTracker().getDeathMessage());
		}

		super.onDeath(cause);
	}

}
