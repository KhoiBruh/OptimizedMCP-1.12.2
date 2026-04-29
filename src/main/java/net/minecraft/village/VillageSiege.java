package net.minecraft.village;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import java.util.Iterator;
import java.util.List;

public class VillageSiege {

	private final World world;
	private boolean hasSetupSiege;
	private int siegeState = -1;
	private int siegeCount;
	private int nextSpawnTime;

	/**
	 * Instance of Village.
	 */
	private Village village;
	private int spawnX;
	private int spawnY;
	private int spawnZ;

	public VillageSiege(World worldIn) {

		world = worldIn;
	}

	/**
	 * Runs a single tick for the village siege
	 */
	public void tick() {

		if (world.isDaytime()) {
			siegeState = 0;
		} else if (siegeState != 2) {
			if (siegeState == 0) {
				float f = world.getCelestialAngle(0F);

				if ((double) f < 0.5D || (double) f > 0.501D) {
					return;
				}

				siegeState = world.rand.nextInt(10) == 0 ? 1 : 2;
				hasSetupSiege = false;

				if (siegeState == 2) {
					return;
				}
			}

			if (siegeState != -1) {
				if (!hasSetupSiege) {
					if (!trySetupSiege()) {
						return;
					}

					hasSetupSiege = true;
				}

				if (nextSpawnTime > 0) {
					--nextSpawnTime;
				} else {
					nextSpawnTime = 2;

					if (siegeCount > 0) {
						spawnZombie();
						--siegeCount;
					} else {
						siegeState = 2;
					}
				}
			}
		}
	}

	private boolean trySetupSiege() {

		List<EntityPlayer> list = world.playerEntities;
		Iterator<EntityPlayer> iterator = list.iterator();

		while (true) {
			if (!iterator.hasNext()) {
				return false;
			}

			EntityPlayer entityplayer = (EntityPlayer) iterator.next();

			if (!entityplayer.isSpectator()) {
				village = world.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);

				if (village != null && village.getNumVillageDoors() >= 10 && village.getTicksSinceLastDoorAdding() >= 20 && village.getNumVillagers() >= 20) {
					BlockPos blockpos = village.getCenter();
					float f = (float) village.getVillageRadius();
					boolean flag = false;

					for (int i = 0; i < 10; ++i) {
						float f1 = world.rand.nextFloat() * ((float) Math.PI * 2F);
						spawnX = blockpos.getX() + (int) ((double) (MathHelper.cos(f1) * f) * 0.9D);
						spawnY = blockpos.getY();
						spawnZ = blockpos.getZ() + (int) ((double) (MathHelper.sin(f1) * f) * 0.9D);
						flag = false;

						for (Village village : world.getVillageCollection().getVillageList()) {
							if (village != this.village && village.isBlockPosWithinSqVillageRadius(new BlockPos(spawnX, spawnY, spawnZ))) {
								flag = true;
								break;
							}
						}

						if (!flag) {
							break;
						}
					}

					if (flag) {
						return false;
					}

					Vec3d vec3d = findRandomSpawnPos(new BlockPos(spawnX, spawnY, spawnZ));

					if (vec3d != null) {
						break;
					}
				}
			}
		}

		nextSpawnTime = 0;
		siegeCount = 20;
		return true;
	}

	private boolean spawnZombie() {

		Vec3d vec3d = findRandomSpawnPos(new BlockPos(spawnX, spawnY, spawnZ));

		if (vec3d == null) {
			return false;
		} else {
			EntityZombie entityzombie;

			try {
				entityzombie = new EntityZombie(world);
				entityzombie.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityzombie)), null);
			} catch (Exception exception) {
				exception.printStackTrace();
				return false;
			}

			entityzombie.setLocationAndAngles(vec3d.x(), vec3d.y(), vec3d.z(), world.rand.nextFloat() * 360F, 0F);
			world.spawnEntity(entityzombie);
			BlockPos blockpos = village.getCenter();
			entityzombie.setHomePosAndDistance(blockpos, village.getVillageRadius());
			return true;
		}
	}

	
	private Vec3d findRandomSpawnPos(BlockPos pos) {

		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos = pos.add(world.rand.nextInt(16) - 8, world.rand.nextInt(6) - 3, world.rand.nextInt(16) - 8);

			if (village.isBlockPosWithinSqVillageRadius(blockpos) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, blockpos)) {
				return new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
			}
		}

		return null;
	}

}
