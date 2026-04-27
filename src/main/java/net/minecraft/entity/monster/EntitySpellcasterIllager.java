package net.minecraft.entity.monster;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class EntitySpellcasterIllager extends AbstractIllager {

	private static final DataParameter<Byte> SPELL = EntityDataManager.createKey(EntitySpellcasterIllager.class, DataSerializers.BYTE);
	protected int spellTicks;
	private EntitySpellcasterIllager.SpellType activeSpell = EntitySpellcasterIllager.SpellType.NONE;

	public EntitySpellcasterIllager(World p_i47506_1_) {

		super(p_i47506_1_);
	}

	protected void entityInit() {

		super.entityInit();
		dataManager.register(SPELL, Byte.valueOf((byte) 0));
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);
		spellTicks = compound.getInteger("SpellTicks");
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setInteger("SpellTicks", spellTicks);
	}

	public AbstractIllager.IllagerArmPose getArmPose() {

		return isSpellcasting() ? AbstractIllager.IllagerArmPose.SPELLCASTING : AbstractIllager.IllagerArmPose.CROSSED;
	}

	public boolean isSpellcasting() {

		if (world.isRemote) {
			return dataManager.get(SPELL).byteValue() > 0;
		} else {
			return spellTicks > 0;
		}
	}

	public void setSpellType(EntitySpellcasterIllager.SpellType spellType) {

		activeSpell = spellType;
		dataManager.set(SPELL, Byte.valueOf((byte) spellType.id));
	}

	protected EntitySpellcasterIllager.SpellType getSpellType() {

		return !world.isRemote ? activeSpell : EntitySpellcasterIllager.SpellType.getFromId(dataManager.get(SPELL).byteValue());
	}

	protected void updateAITasks() {

		super.updateAITasks();

		if (spellTicks > 0) {
			--spellTicks;
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		super.onUpdate();

		if (world.isRemote && isSpellcasting()) {
			EntitySpellcasterIllager.SpellType entityspellcasterillager$spelltype = getSpellType();
			double d0 = entityspellcasterillager$spelltype.particleSpeed[0];
			double d1 = entityspellcasterillager$spelltype.particleSpeed[1];
			double d2 = entityspellcasterillager$spelltype.particleSpeed[2];
			float f = renderYawOffset * 0.017453292F + MathHelper.cos((float) ticksExisted * 0.6662F) * 0.25F;
			float f1 = MathHelper.cos(f);
			float f2 = MathHelper.sin(f);
			world.spawnParticle(EnumParticleTypes.SPELL_MOB, posX + (double) f1 * 0.6D, posY + 1.8D, posZ + (double) f2 * 0.6D, d0, d1, d2);
			world.spawnParticle(EnumParticleTypes.SPELL_MOB, posX - (double) f1 * 0.6D, posY + 1.8D, posZ - (double) f2 * 0.6D, d0, d1, d2);
		}
	}

	protected int getSpellTicks() {

		return spellTicks;
	}

	protected abstract SoundEvent getSpellSound();

	public class AICastingApell extends EntityAIBase {

		public AICastingApell() {

			setMutexBits(3);
		}

		public boolean shouldExecute() {

			return getSpellTicks() > 0;
		}

		public void startExecuting() {

			super.startExecuting();
			navigator.clearPath();
		}

		public void resetTask() {

			super.resetTask();
			setSpellType(EntitySpellcasterIllager.SpellType.NONE);
		}

		public void updateTask() {

			if (getAttackTarget() != null) {
				getLookHelper().setLookPositionWithEntity(getAttackTarget(), (float) getHorizontalFaceSpeed(), (float) getVerticalFaceSpeed());
			}
		}

	}

	public abstract class AIUseSpell extends EntityAIBase {

		protected int spellWarmup;
		protected int spellCooldown;

		public boolean shouldExecute() {

			if (getAttackTarget() == null) {
				return false;
			} else if (isSpellcasting()) {
				return false;
			} else {
				return ticksExisted >= spellCooldown;
			}
		}

		public boolean shouldContinueExecuting() {

			return getAttackTarget() != null && spellWarmup > 0;
		}

		public void startExecuting() {

			spellWarmup = getCastWarmupTime();
			spellTicks = getCastingTime();
			spellCooldown = ticksExisted + getCastingInterval();
			SoundEvent soundevent = getSpellPrepareSound();

			if (soundevent != null) {
				playSound(soundevent, 1.0F, 1.0F);
			}

			setSpellType(getSpellType());
		}

		public void updateTask() {

			--spellWarmup;

			if (spellWarmup == 0) {
				castSpell();
				playSound(getSpellSound(), 1.0F, 1.0F);
			}
		}

		protected abstract void castSpell();

		protected int getCastWarmupTime() {

			return 20;
		}

		protected abstract int getCastingTime();

		protected abstract int getCastingInterval();

		@Nullable
		protected abstract SoundEvent getSpellPrepareSound();

		protected abstract EntitySpellcasterIllager.SpellType getSpellType();

	}

	public enum SpellType {
		NONE(0, 0.0D, 0.0D, 0.0D),
		SUMMON_VEX(1, 0.7D, 0.7D, 0.8D),
		FANGS(2, 0.4D, 0.3D, 0.35D),
		WOLOLO(3, 0.7D, 0.5D, 0.2D),
		DISAPPEAR(4, 0.3D, 0.3D, 0.8D),
		BLINDNESS(5, 0.1D, 0.1D, 0.2D);

		private final int id;
		private final double[] particleSpeed;

		SpellType(int idIn, double xParticleSpeed, double yParticleSpeed, double zParticleSpeed) {

			id = idIn;
			particleSpeed = new double[]{xParticleSpeed, yParticleSpeed, zParticleSpeed};
		}

		public static EntitySpellcasterIllager.SpellType getFromId(int idIn) {

			for (EntitySpellcasterIllager.SpellType entityspellcasterillager$spelltype : values()) {
				if (idIn == entityspellcasterillager$spelltype.id) {
					return entityspellcasterillager$spelltype;
				}
			}

			return NONE;
		}
	}

}
