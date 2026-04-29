package net.minecraft.entity.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Hand;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;

public class EntityMinecartEmpty extends EntityMinecart {

	public EntityMinecartEmpty(World worldIn) {

		super(worldIn);
	}

	public EntityMinecartEmpty(World worldIn, double x, double y, double z) {

		super(worldIn, x, y, z);
	}

	public static void registerFixesMinecartEmpty(DataFixer fixer) {

		EntityMinecart.registerFixesMinecart(fixer, EntityMinecartEmpty.class);
	}

	public boolean processInitialInteract(EntityPlayer player, Hand hand) {

		if (player.isSneaking()) {
			return false;
		} else if (isBeingRidden()) {
			return true;
		} else {
			if (!world.isRemote) {
				player.startRiding(this);
			}

			return true;
		}
	}

	/**
	 * Called every tick the minecart is on an activator rail.
	 */
	public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {

		if (receivingPower) {
			if (isBeingRidden()) {
				removePassengers();
			}

			if (getRollingAmplitude() == 0) {
				setRollingDirection(-getRollingDirection());
				setRollingAmplitude(10);
				setDamage(50F);
				markVelocityChanged();
			}
		}
	}

	public EntityMinecart.Type getType() {

		return EntityMinecart.Type.RIDEABLE;
	}

}
