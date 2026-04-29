package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.*;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EntityPlayerMP extends EntityPlayer implements IContainerListener {

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Reference to the MinecraftServer object.
	 */
	public final MinecraftServer mcServer;
	/**
	 * The player interaction manager for this player
	 */
	public final PlayerInteractionManager interactionManager;
	private final List<Integer> entityRemoveQueue = Lists.newLinkedList();
	private final PlayerAdvancements advancements;
	private final StatisticsManagerServer statsFile;
	private final RecipeBookServer recipeBook = new RecipeBookServer();
	/**
	 * The NetServerHandler assigned to this player by the ServerConfigurationManager.
	 */
	public NetHandlerPlayServer connection;
	/**
	 * player X position as seen by PlayerManager
	 */
	public double managedPosX;
	/**
	 * player Z position as seen by PlayerManager
	 */
	public double managedPosZ;
	/**
	 * set to true when player is moving quantity of items from one inventory to another(crafting) but item in either
	 * slot is not changed
	 */
	public boolean isChangingQuantityOnly;
	public int ping;
	/**
	 * True when the player has left the End using an the exit portal, but has not yet been respawned in the overworld
	 */
	public boolean queuedEndExit;
	private String language = "en_US";
	/**
	 * the total health of the player, includes actual health and absorption health. Updated every tick.
	 */
	private float lastHealthScore = Float.MIN_VALUE;
	private int lastFoodScore = Integer.MIN_VALUE;
	private int lastAirScore = Integer.MIN_VALUE;
	private int lastArmorScore = Integer.MIN_VALUE;
	private int lastLevelScore = Integer.MIN_VALUE;
	private int lastExperienceScore = Integer.MIN_VALUE;
	/**
	 * amount of health the client was last set to
	 */
	private float lastHealth = -1.0E8F;
	/**
	 * set to foodStats.GetFoodLevel
	 */
	private int lastFoodLevel = -99999999;
	/**
	 * set to foodStats.getSaturationLevel() == 0.0F each tick
	 */
	private boolean wasHungry = true;
	/**
	 * Amount of experience the client was last set to
	 */
	private int lastExperience = -99999999;
	private int respawnInvulnerabilityTicks = 60;
	private EntityPlayer.EnumChatVisibility chatVisibility;
	private boolean chatColours = true;
	private long playerLastActiveTime = System.currentTimeMillis();
	/**
	 * The entity the player is currently spectating through.
	 */
	private Entity spectatingEntity;
	private boolean invulnerableDimensionChange;
	private boolean seenCredits;
	/**
	 * The position this player started levitating at.
	 */
	private Vec3d levitationStartPos;
	/**
	 * The value of ticksExisted when this player started levitating.
	 */
	private int levitatingSince;
	private boolean disconnected;
	private Vec3d enteredNetherPosition;
	/**
	 * The currently in use window ID. Incremented every time a window is opened.
	 */
	private int currentWindowId;

	public EntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {

		super(worldIn, profile);
		interactionManagerIn.player = this;
		interactionManager = interactionManagerIn;
		BlockPos blockpos = worldIn.getSpawnPoint();

		if (worldIn.provider.hasSkyLight() && worldIn.getWorldInfo().getGameType() != GameType.ADVENTURE) {
			int i = Math.max(0, server.getSpawnRadius(worldIn));
			int j = MathHelper.floor(worldIn.getWorldBorder().getClosestDistance(blockpos.getX(), blockpos.getZ()));

			if (j < i) {
				i = j;
			}

			if (j <= 1) {
				i = 1;
			}

			blockpos = worldIn.getTopSolidOrLiquidBlock(blockpos.add(rand.nextInt(i * 2 + 1) - i, 0, rand.nextInt(i * 2 + 1) - i));
		}

		mcServer = server;
		statsFile = server.getPlayerList().getPlayerStatsFile(this);
		advancements = server.getPlayerList().getPlayerAdvancements(this);
		stepHeight = 1.0F;
		moveToBlockPosAndAngles(blockpos, 0.0F, 0.0F);

		while (!worldIn.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty() && posY < 255.0D) {
			setPosition(posX, posY + 1.0D, posZ);
		}
	}

	public static void registerFixesPlayerMP(DataFixer p_191522_0_) {

		p_191522_0_.registerWalker(FixTypes.PLAYER, (fixer, compound, versionIn) -> {

			if (compound.hasKey("RootVehicle", 10)) {
				NBTTagCompound nbttagcompound = compound.getCompoundTag("RootVehicle");

				if (nbttagcompound.hasKey("Entity", 10)) {
					nbttagcompound.setTag("Entity", fixer.process(FixTypes.ENTITY, nbttagcompound.getCompoundTag("Entity"), versionIn));
				}
			}

			return compound;
		});
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {

		super.readEntityFromNBT(compound);

		if (compound.hasKey("playerGameType", 99)) {
			if (getServer().getForceGamemode()) {
				interactionManager.setGameType(getServer().getGameType());
			} else {
				interactionManager.setGameType(GameType.getByID(compound.getInteger("playerGameType")));
			}
		}

		if (compound.hasKey("enteredNetherPosition", 10)) {
			NBTTagCompound nbttagcompound = compound.getCompoundTag("enteredNetherPosition");
			enteredNetherPosition = new Vec3d(nbttagcompound.getDouble("x"), nbttagcompound.getDouble("y"), nbttagcompound.getDouble("z"));
		}

		seenCredits = compound.getBoolean("seenCredits");

		if (compound.hasKey("recipeBook", 10)) {
			recipeBook.read(compound.getCompoundTag("recipeBook"));
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {

		super.writeEntityToNBT(compound);
		compound.setInteger("playerGameType", interactionManager.getGameType().getID());
		compound.setBoolean("seenCredits", seenCredits);

		if (enteredNetherPosition != null) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setDouble("x", enteredNetherPosition.x());
			nbttagcompound.setDouble("y", enteredNetherPosition.y());
			nbttagcompound.setDouble("z", enteredNetherPosition.z());
			compound.setTag("enteredNetherPosition", nbttagcompound);
		}

		Entity entity1 = getLowestRidingEntity();
		Entity entity = getRidingEntity();

		if (entity != null && entity1 != this && entity1.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			NBTTagCompound nbttagcompound2 = new NBTTagCompound();
			entity1.writeToNBTOptional(nbttagcompound2);
			nbttagcompound1.setUniqueId("Attach", entity.getUniqueID());
			nbttagcompound1.setTag("Entity", nbttagcompound2);
			compound.setTag("RootVehicle", nbttagcompound1);
		}

		compound.setTag("recipeBook", recipeBook.write());
	}

	/**
	 * Add experience levels to this player.
	 */
	public void addExperienceLevel(int levels) {

		super.addExperienceLevel(levels);
		lastExperience = -1;
	}

	public void onEnchant(ItemStack enchantedItem, int cost) {

		super.onEnchant(enchantedItem, cost);
		lastExperience = -1;
	}

	public void addSelfToInternalCraftingInventory() {

		openContainer.addListener(this);
	}

	/**
	 * Sends an ENTER_COMBAT packet to the client
	 */
	public void sendEnterCombat() {

		super.sendEnterCombat();
		connection.sendPacket(new SPacketCombatEvent(getCombatTracker(), SPacketCombatEvent.Event.ENTER_COMBAT));
	}

	/**
	 * Sends an END_COMBAT packet to the client
	 */
	public void sendEndCombat() {

		super.sendEndCombat();
		connection.sendPacket(new SPacketCombatEvent(getCombatTracker(), SPacketCombatEvent.Event.END_COMBAT));
	}

	protected void onInsideBlock(IBlockState p_191955_1_) {

		CriteriaTriggers.ENTER_BLOCK.trigger(this, p_191955_1_);
	}

	protected CooldownTracker createCooldownTracker() {

		return new CooldownTrackerServer(this);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		interactionManager.updateBlockRemoving();
		--respawnInvulnerabilityTicks;

		if (hurtResistantTime > 0) {
			--hurtResistantTime;
		}

		openContainer.detectAndSendChanges();

		if (!world.isRemote && !openContainer.canInteractWith(this)) {
			closeScreen();
			openContainer = inventoryContainer;
		}

		while (!entityRemoveQueue.isEmpty()) {
			int i = Math.min(entityRemoveQueue.size(), Integer.MAX_VALUE);
			int[] aint = new int[i];
			Iterator<Integer> iterator = entityRemoveQueue.iterator();
			int j = 0;

			while (iterator.hasNext() && j < i) {
				aint[j++] = iterator.next();
				iterator.remove();
			}

			connection.sendPacket(new SPacketDestroyEntities(aint));
		}

		Entity entity = getSpectatingEntity();

		if (entity != this) {
			if (entity.isEntityAlive()) {
				setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				mcServer.getPlayerList().serverUpdateMovingPlayer(this);

				if (isSneaking()) {
					setSpectatingEntity(this);
				}
			} else {
				setSpectatingEntity(this);
			}
		}

		CriteriaTriggers.TICK.trigger(this);

		if (levitationStartPos != null) {
			CriteriaTriggers.LEVITATION.trigger(this, levitationStartPos, ticksExisted - levitatingSince);
		}

		advancements.flushDirty(this);
	}

	public void onUpdateEntity() {

		try {
			super.onUpdate();

			for (int i = 0; i < inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = inventory.getStackInSlot(i);

				if (!itemstack.isEmpty() && itemstack.getItem().isMap()) {
					Packet<?> packet = ((ItemMapBase) itemstack.getItem()).createMapDataPacket(itemstack, world, this);

					if (packet != null) {
						connection.sendPacket(packet);
					}
				}
			}

			if (getHealth() != lastHealth || lastFoodLevel != foodStats.getFoodLevel() || foodStats.getSaturationLevel() == 0.0F != wasHungry) {
				connection.sendPacket(new SPacketUpdateHealth(getHealth(), foodStats.getFoodLevel(), foodStats.getSaturationLevel()));
				lastHealth = getHealth();
				lastFoodLevel = foodStats.getFoodLevel();
				wasHungry = foodStats.getSaturationLevel() == 0.0F;
			}

			if (getHealth() + getAbsorptionAmount() != lastHealthScore) {
				lastHealthScore = getHealth() + getAbsorptionAmount();
				updateScorePoints(IScoreCriteria.HEALTH, MathHelper.ceil(lastHealthScore));
			}

			if (foodStats.getFoodLevel() != lastFoodScore) {
				lastFoodScore = foodStats.getFoodLevel();
				updateScorePoints(IScoreCriteria.FOOD, MathHelper.ceil((float) lastFoodScore));
			}

			if (getAir() != lastAirScore) {
				lastAirScore = getAir();
				updateScorePoints(IScoreCriteria.AIR, MathHelper.ceil((float) lastAirScore));
			}

			if (getTotalArmorValue() != lastArmorScore) {
				lastArmorScore = getTotalArmorValue();
				updateScorePoints(IScoreCriteria.ARMOR, MathHelper.ceil((float) lastArmorScore));
			}

			if (experienceTotal != lastExperienceScore) {
				lastExperienceScore = experienceTotal;
				updateScorePoints(IScoreCriteria.XP, MathHelper.ceil((float) lastExperienceScore));
			}

			if (experienceLevel != lastLevelScore) {
				lastLevelScore = experienceLevel;
				updateScorePoints(IScoreCriteria.LEVEL, MathHelper.ceil((float) lastLevelScore));
			}

			if (experienceTotal != lastExperience) {
				lastExperience = experienceTotal;
				connection.sendPacket(new SPacketSetExperience(experience, experienceTotal, experienceLevel));
			}

			if (ticksExisted % 20 == 0) {
				CriteriaTriggers.LOCATION.trigger(this);
			}
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
			addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}
	}

	private void updateScorePoints(IScoreCriteria criteria, int points) {

		for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(criteria)) {
			Score score = getWorldScoreboard().getOrCreateScore(getName(), scoreobjective);
			score.setScorePoints(points);
		}
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	public void onDeath(DamageSource cause) {

		boolean flag = world.getGameRules().getBoolean("showDeathMessages");
		connection.sendPacket(new SPacketCombatEvent(getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

		if (flag) {
			Team team = getTeam();

			if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
				if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
					mcServer.getPlayerList().sendMessageToAllTeamMembers(this, getCombatTracker().getDeathMessage());
				} else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
					mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(this, getCombatTracker().getDeathMessage());
				}
			} else {
				mcServer.getPlayerList().sendMessage(getCombatTracker().getDeathMessage());
			}
		}

		spawnShoulderEntities();

		if (!world.getGameRules().getBoolean("keepInventory") && !isSpectator()) {
			destroyVanishingCursedItems();
			inventory.dropAllItems();
		}

		for (ScoreObjective scoreobjective : world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
			Score score = getWorldScoreboard().getOrCreateScore(getName(), scoreobjective);
			score.incrementScore();
		}

		EntityLivingBase entitylivingbase = getAttackingEntity();

		if (entitylivingbase != null) {
			EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

			if (entitylist$entityegginfo != null) {
				addStat(entitylist$entityegginfo.entityKilledByStat);
			}

			entitylivingbase.awardKillScore(this, scoreValue, cause);
		}

		addStat(StatList.DEATHS);
		takeStat(StatList.TIME_SINCE_DEATH);
		extinguish();
		setFlag(0, false);
		getCombatTracker().reset();
	}

	public void awardKillScore(Entity p_191956_1_, int p_191956_2_, DamageSource p_191956_3_) {

		if (p_191956_1_ != this) {
			super.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
			addScore(p_191956_2_);
			Collection<ScoreObjective> collection = getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TOTAL_KILL_COUNT);

			if (p_191956_1_ instanceof EntityPlayer) {
				addStat(StatList.PLAYER_KILLS);
				collection.addAll(getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.PLAYER_KILL_COUNT));
			} else {
				addStat(StatList.MOB_KILLS);
			}

			collection.addAll(awardTeamKillScores(p_191956_1_));

			for (ScoreObjective scoreobjective : collection) {
				getWorldScoreboard().getOrCreateScore(getName(), scoreobjective).incrementScore();
			}

			CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, p_191956_1_, p_191956_3_);
		}
	}

	private Collection<ScoreObjective> awardTeamKillScores(Entity p_192038_1_) {

		String s = p_192038_1_ instanceof EntityPlayer ? p_192038_1_.getName() : p_192038_1_.getCachedUniqueIdString();
		ScorePlayerTeam scoreplayerteam = getWorldScoreboard().getPlayersTeam(getName());

		if (scoreplayerteam != null) {
			int i = scoreplayerteam.getColor().getColorIndex();

			if (i >= 0 && i < IScoreCriteria.KILLED_BY_TEAM.length) {
				for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.KILLED_BY_TEAM[i])) {
					Score score = getWorldScoreboard().getOrCreateScore(s, scoreobjective);
					score.incrementScore();
				}
			}
		}

		ScorePlayerTeam scoreplayerteam1 = getWorldScoreboard().getPlayersTeam(s);

		if (scoreplayerteam1 != null) {
			int j = scoreplayerteam1.getColor().getColorIndex();

			if (j >= 0 && j < IScoreCriteria.TEAM_KILL.length) {
				return getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TEAM_KILL[j]);
			}
		}

		return Lists.newArrayList();
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if (isEntityInvulnerable(source)) {
			return false;
		} else {
			boolean flag = mcServer.isDedicatedServer() && canPlayersAttack() && "fall".equals(source.damageType);

			if (!flag && respawnInvulnerabilityTicks > 0 && source != DamageSource.OUT_OF_WORLD) {
				return false;
			} else {
				if (source instanceof EntityDamageSource) {
					Entity entity = source.getTrueSource();

					if (entity instanceof EntityPlayer && !canAttackPlayer((EntityPlayer) entity)) {
						return false;
					}

					if (entity instanceof EntityArrow entityarrow) {

						if (entityarrow.shootingEntity instanceof EntityPlayer && !canAttackPlayer((EntityPlayer) entityarrow.shootingEntity)) {
							return false;
						}
					}
				}

				return super.attackEntityFrom(source, amount);
			}
		}
	}

	public boolean canAttackPlayer(EntityPlayer other) {

		return canPlayersAttack() && super.canAttackPlayer(other);
	}

	/**
	 * Returns if other players can attack this player
	 */
	private boolean canPlayersAttack() {

		return mcServer.isPVPEnabled();
	}

	
	public Entity changeDimension(int dimensionIn) {

		invulnerableDimensionChange = true;

		if (dimension == 0 && dimensionIn == -1) {
			enteredNetherPosition = new Vec3d(posX, posY, posZ);
		} else if (dimension != -1 && dimensionIn != 0) {
			enteredNetherPosition = null;
		}

		if (dimension == 1 && dimensionIn == 1) {
			world.removeEntity(this);

			if (!queuedEndExit) {
				queuedEndExit = true;
				connection.sendPacket(new SPacketChangeGameState(4, seenCredits ? 0.0F : 1.0F));
				seenCredits = true;
			}

			return this;
		} else {
			if (dimension == 0 && dimensionIn == 1) {
				dimensionIn = 1;
			}

			mcServer.getPlayerList().changePlayerDimension(this, dimensionIn);
			connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
			lastExperience = -1;
			lastHealth = -1.0F;
			lastFoodLevel = -1;
			return this;
		}
	}

	public boolean isSpectatedByPlayer(EntityPlayerMP player) {

		if (player.isSpectator()) {
			return getSpectatingEntity() == this;
		} else {
			return !isSpectator() && super.isSpectatedByPlayer(player);
		}
	}

	private void sendTileEntityUpdate(TileEntity p_147097_1_) {

		if (p_147097_1_ != null) {
			SPacketUpdateTileEntity spacketupdatetileentity = p_147097_1_.getUpdatePacket();

			if (spacketupdatetileentity != null) {
				connection.sendPacket(spacketupdatetileentity);
			}
		}
	}

	/**
	 * Called when the entity picks up an item.
	 */
	public void onItemPickup(Entity entityIn, int quantity) {

		super.onItemPickup(entityIn, quantity);
		openContainer.detectAndSendChanges();
	}

	public EntityPlayer.SleepResult trySleep(BlockPos bedLocation) {

		EntityPlayer.SleepResult entityplayer$sleepresult = super.trySleep(bedLocation);

		if (entityplayer$sleepresult == EntityPlayer.SleepResult.OK) {
			addStat(StatList.SLEEP_IN_BED);
			Packet<?> packet = new SPacketUseBed(this, bedLocation);
			getServerWorld().getEntityTracker().sendToTracking(this, packet);
			connection.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
			connection.sendPacket(packet);
			CriteriaTriggers.SLEPT_IN_BED.trigger(this);
		}

		return entityplayer$sleepresult;
	}

	/**
	 * Wake up the player if they're sleeping.
	 */
	public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {

		if (isPlayerSleeping()) {
			getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(this, 2));
		}

		super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

		if (connection != null) {
			connection.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
		}
	}

	public boolean startRiding(Entity entityIn, boolean force) {

		Entity entity = getRidingEntity();

		if (!super.startRiding(entityIn, force)) {
			return false;
		} else {
			Entity entity1 = getRidingEntity();

			if (entity1 != entity && connection != null) {
				connection.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
			}

			return true;
		}
	}

	/**
	 * Dismounts this entity from the entity it is riding.
	 */
	public void dismountRidingEntity() {

		Entity entity = getRidingEntity();
		super.dismountRidingEntity();
		Entity entity1 = getRidingEntity();

		if (entity1 != entity && connection != null) {
			connection.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
		}
	}

	/**
	 * Returns whether this Entity is invulnerable to the given DamageSource.
	 */
	public boolean isEntityInvulnerable(DamageSource source) {

		return super.isEntityInvulnerable(source) || isInvulnerableDimensionChange();
	}

	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {

	}

	protected void frostWalk(BlockPos pos) {

		if (!isSpectator()) {
			super.frostWalk(pos);
		}
	}

	/**
	 * process player falling based on movement packet
	 */
	public void handleFalling(double y, boolean onGroundIn) {

		int i = MathHelper.floor(posX);
		int j = MathHelper.floor(posY - 0.20000000298023224D);
		int k = MathHelper.floor(posZ);
		BlockPos blockpos = new BlockPos(i, j, k);
		IBlockState iblockstate = world.getBlockState(blockpos);

		if (iblockstate.getMaterial() == Material.AIR) {
			BlockPos blockpos1 = blockpos.down();
			IBlockState iblockstate1 = world.getBlockState(blockpos1);
			Block block = iblockstate1.getBlock();

			if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
				blockpos = blockpos1;
				iblockstate = iblockstate1;
			}
		}

		super.updateFallState(y, onGroundIn, iblockstate, blockpos);
	}

	public void openEditSign(TileEntitySign signTile) {

		signTile.setPlayer(this);
		connection.sendPacket(new SPacketSignEditorOpen(signTile.getPos()));
	}

	/**
	 * get the next window id to use
	 */
	private void getNextWindowId() {

		currentWindowId = currentWindowId % 100 + 1;
	}

	public void displayGui(IInteractionObject guiOwner) {

		if (guiOwner instanceof ILootContainer && ((ILootContainer) guiOwner).getLootTable() != null && isSpectator()) {
			sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen")).setStyle((new Style()).setColor(TextFormatting.RED)), true);
		} else {
			getNextWindowId();
			connection.sendPacket(new SPacketOpenWindow(currentWindowId, guiOwner.guiID(), guiOwner.displayName()));
			openContainer = guiOwner.createContainer(inventory, this);
			openContainer.windowId = currentWindowId;
			openContainer.addListener(this);
		}
	}

	/**
	 * Displays the GUI for interacting with a chest inventory.
	 */
	public void displayGUIChest(IInventory chestInventory) {

		if (chestInventory instanceof ILootContainer && ((ILootContainer) chestInventory).getLootTable() != null && isSpectator()) {
			sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen")).setStyle((new Style()).setColor(TextFormatting.RED)), true);
		} else {
			if (openContainer != inventoryContainer) {
				closeScreen();
			}

			if (chestInventory instanceof ILockableContainer ilockablecontainer) {

				if (ilockablecontainer.isLocked() && !canOpen(ilockablecontainer.getLockCode()) && !isSpectator()) {
					connection.sendPacket(new SPacketChat(new TextComponentTranslation("container.isLocked", chestInventory.displayName()), ChatType.GAME_INFO));
					connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, posX, posY, posZ, 1.0F, 1.0F));
					return;
				}
			}

			getNextWindowId();

			if (chestInventory instanceof IInteractionObject) {
				connection.sendPacket(new SPacketOpenWindow(currentWindowId, ((IInteractionObject) chestInventory).guiID(), chestInventory.displayName(), chestInventory.getSizeInventory()));
				openContainer = ((IInteractionObject) chestInventory).createContainer(inventory, this);
			} else {
				connection.sendPacket(new SPacketOpenWindow(currentWindowId, "minecraft:container", chestInventory.displayName(), chestInventory.getSizeInventory()));
				openContainer = new ContainerChest(inventory, chestInventory, this);
			}

			openContainer.windowId = currentWindowId;
			openContainer.addListener(this);
		}
	}

	public void displayVillagerTradeGui(IMerchant villager) {

		getNextWindowId();
		openContainer = new ContainerMerchant(inventory, villager, world);
		openContainer.windowId = currentWindowId;
		openContainer.addListener(this);
		IInventory iinventory = ((ContainerMerchant) openContainer).getMerchantInventory();
		ITextComponent itextcomponent = villager.getDisplayName();
		connection.sendPacket(new SPacketOpenWindow(currentWindowId, "minecraft:villager", itextcomponent, iinventory.getSizeInventory()));
		MerchantRecipeList merchantrecipelist = villager.getRecipes(this);

		if (merchantrecipelist != null) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeInt(currentWindowId);
			merchantrecipelist.writeToBuf(packetbuffer);
			connection.sendPacket(new SPacketCustomPayload("MC|TrList", packetbuffer));
		}
	}

	public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {

		if (openContainer != inventoryContainer) {
			closeScreen();
		}

		getNextWindowId();
		connection.sendPacket(new SPacketOpenWindow(currentWindowId, "EntityHorse", inventoryIn.displayName(), inventoryIn.getSizeInventory(), horse.getEntityId()));
		openContainer = new ContainerHorseInventory(inventory, inventoryIn, horse, this);
		openContainer.windowId = currentWindowId;
		openContainer.addListener(this);
	}

	public void openBook(ItemStack stack, EnumHand hand) {

		Item item = stack.getItem();

		if (item == Items.WRITTEN_BOOK) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeEnumValue(hand);
			connection.sendPacket(new SPacketCustomPayload("MC|BOpen", packetbuffer));
		}
	}

	public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {

		commandBlock.setSendToClient(true);
		sendTileEntityUpdate(commandBlock);
	}

	/**
	 * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
	 * contents of that slot.
	 */
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {

		if (!(containerToSend.getSlot(slotInd) instanceof SlotCrafting)) {
			if (containerToSend == inventoryContainer) {
				CriteriaTriggers.INVENTORY_CHANGED.trigger(this, inventory);
			}

			if (!isChangingQuantityOnly) {
				connection.sendPacket(new SPacketSetSlot(containerToSend.windowId, slotInd, stack));
			}
		}
	}

	public void sendContainerToPlayer(Container containerIn) {

		sendAllContents(containerIn, containerIn.getInventory());
	}

	/**
	 * update the crafting window inventory with the items in the list
	 */
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {

		connection.sendPacket(new SPacketWindowItems(containerToSend.windowId, itemsList));
		connection.sendPacket(new SPacketSetSlot(-1, -1, inventory.getItemStack()));
	}

	/**
	 * Sends two ints to the client-side Container. Used for furnace burning time, smelting progress, brewing progress,
	 * and enchanting level. Normally the first int identifies which variable to update, and the second contains the new
	 * value. Both are truncated to shorts in non-local SMP.
	 */
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {

		connection.sendPacket(new SPacketWindowProperty(containerIn.windowId, varToUpdate, newValue));
	}

	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {

		for (int i = 0; i < inventory.getFieldCount(); ++i) {
			connection.sendPacket(new SPacketWindowProperty(containerIn.windowId, i, inventory.getField(i)));
		}
	}

	/**
	 * set current crafting inventory back to the 2x2 square
	 */
	public void closeScreen() {

		connection.sendPacket(new SPacketCloseWindow(openContainer.windowId));
		closeContainer();
	}

	/**
	 * updates item held by mouse
	 */
	public void updateHeldItem() {

		if (!isChangingQuantityOnly) {
			connection.sendPacket(new SPacketSetSlot(-1, -1, inventory.getItemStack()));
		}
	}

	/**
	 * Closes the container the player currently has open.
	 */
	public void closeContainer() {

		openContainer.onContainerClosed(this);
		openContainer = inventoryContainer;
	}

	public void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking) {

		if (isRiding()) {
			if (strafe >= -1.0F && strafe <= 1.0F) {
				moveStrafing = strafe;
			}

			if (forward >= -1.0F && forward <= 1.0F) {
				moveForward = forward;
			}

			isJumping = jumping;
			setSneaking(sneaking);
		}
	}

	/**
	 * Adds a value to a statistic field.
	 */
	public void addStat(StatBase stat, int amount) {

		if (stat != null) {
			statsFile.increaseStat(this, stat, amount);

			for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria())) {
				getWorldScoreboard().getOrCreateScore(getName(), scoreobjective).increaseScore(amount);
			}
		}
	}

	public void takeStat(StatBase stat) {

		if (stat != null) {
			statsFile.unlockAchievement(this, stat, 0);

			for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria())) {
				getWorldScoreboard().getOrCreateScore(getName(), scoreobjective).setScorePoints(0);
			}
		}
	}

	public void unlockRecipes(List<IRecipe> p_192021_1_) {

		recipeBook.add(p_192021_1_, this);
	}

	public void unlockRecipes(ResourceLocation[] p_193102_1_) {

		List<IRecipe> list = Lists.newArrayList();

		for (ResourceLocation resourcelocation : p_193102_1_) {
			list.add(CraftingManager.getRecipe(resourcelocation));
		}

		unlockRecipes(list);
	}

	public void resetRecipes(List<IRecipe> p_192022_1_) {

		recipeBook.remove(p_192022_1_, this);
	}

	public void mountEntityAndWakeUp() {

		disconnected = true;
		removePassengers();

		if (sleeping) {
			wakeUpPlayer(true, false, false);
		}
	}

	public boolean hasDisconnected() {

		return disconnected;
	}

	/**
	 * this function is called when a players inventory is sent to him, lastHealth is updated on any dimension
	 * transitions, then reset.
	 */
	public void setPlayerHealthUpdated() {

		lastHealth = -1.0E8F;
	}

	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {

		connection.sendPacket(new SPacketChat(chatComponent, actionBar ? ChatType.GAME_INFO : ChatType.CHAT));
	}

	/**
	 * Used for when item use count runs out, ie: eating completed
	 */
	protected void onItemUseFinish() {

		if (!activeItemStack.isEmpty() && isHandActive()) {
			connection.sendPacket(new SPacketEntityStatus(this, (byte) 9));
			super.onItemUseFinish();
		}
	}

	public void copyFrom(EntityPlayerMP that, boolean keepEverything) {

		if (keepEverything) {
			inventory.copyInventory(that.inventory);
			setHealth(that.getHealth());
			foodStats = that.foodStats;
			experienceLevel = that.experienceLevel;
			experienceTotal = that.experienceTotal;
			experience = that.experience;
			setScore(that.getScore());
			lastPortalPos = that.lastPortalPos;
			lastPortalVec = that.lastPortalVec;
			teleportDirection = that.teleportDirection;
		} else if (world.getGameRules().getBoolean("keepInventory") || that.isSpectator()) {
			inventory.copyInventory(that.inventory);
			experienceLevel = that.experienceLevel;
			experienceTotal = that.experienceTotal;
			experience = that.experience;
			setScore(that.getScore());
		}

		xpSeed = that.xpSeed;
		enderChest = that.enderChest;
		getDataManager().set(PLAYER_MODEL_FLAG, that.getDataManager().get(PLAYER_MODEL_FLAG));
		lastExperience = -1;
		lastHealth = -1.0F;
		lastFoodLevel = -1;
		recipeBook.copyFrom(that.recipeBook);
		entityRemoveQueue.addAll(that.entityRemoveQueue);
		seenCredits = that.seenCredits;
		enteredNetherPosition = that.enteredNetherPosition;
		setLeftShoulderEntity(that.getLeftShoulderEntity());
		setRightShoulderEntity(that.getRightShoulderEntity());
	}

	protected void onNewPotionEffect(PotionEffect id) {

		super.onNewPotionEffect(id);
		connection.sendPacket(new SPacketEntityEffect(getEntityId(), id));

		if (id.getPotion() == MobEffects.LEVITATION) {
			levitatingSince = ticksExisted;
			levitationStartPos = new Vec3d(posX, posY, posZ);
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	protected void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_) {

		super.onChangedPotionEffect(id, p_70695_2_);
		connection.sendPacket(new SPacketEntityEffect(getEntityId(), id));
		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	protected void onFinishedPotionEffect(PotionEffect effect) {

		super.onFinishedPotionEffect(effect);
		connection.sendPacket(new SPacketRemoveEntityEffect(getEntityId(), effect.getPotion()));

		if (effect.getPotion() == MobEffects.LEVITATION) {
			levitationStartPos = null;
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
	}

	/**
	 * Sets the position of the entity and updates the 'last' variables
	 */
	public void setPositionAndUpdate(double x, double y, double z) {

		connection.setPlayerLocation(x, y, z, rotationYaw, rotationPitch);
	}

	/**
	 * Called when the entity is dealt a critical hit.
	 */
	public void onCriticalHit(Entity entityHit) {

		getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entityHit, 4));
	}

	public void onEnchantmentCritical(Entity entityHit) {

		getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entityHit, 5));
	}

	/**
	 * Sends the player's abilities to the server (if there is one).
	 */
	public void sendPlayerAbilities() {

		if (connection != null) {
			connection.sendPacket(new SPacketPlayerAbilities(capabilities));
			updatePotionMetadata();
		}
	}

	public WorldServer getServerWorld() {

		return (WorldServer) world;
	}

	/**
	 * Sets the player's game mode and sends it to them.
	 */
	public void setGameType(GameType gameType) {

		interactionManager.setGameType(gameType);
		connection.sendPacket(new SPacketChangeGameState(3, (float) gameType.getID()));

		if (gameType == GameType.SPECTATOR) {
			spawnShoulderEntities();
			dismountRidingEntity();
		} else {
			setSpectatingEntity(this);
		}

		sendPlayerAbilities();
		markPotionsDirty();
	}

	/**
	 * Returns true if the player is in spectator mode.
	 */
	public boolean isSpectator() {

		return interactionManager.getGameType() == GameType.SPECTATOR;
	}

	public boolean isCreative() {

		return interactionManager.getGameType() == GameType.CREATIVE;
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(ITextComponent component) {

		connection.sendPacket(new SPacketChat(component));
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canUseCommand(int permLevel, String commandName) {

		if ("seed".equals(commandName) && !mcServer.isDedicatedServer()) {
			return true;
		} else if (!"tell".equals(commandName) && !"help".equals(commandName) && !"me".equals(commandName) && !"trigger".equals(commandName)) {
			if (mcServer.getPlayerList().canSendCommands(getGameProfile())) {
				UserListOpsEntry userlistopsentry = mcServer.getPlayerList().getOppedPlayers().getEntry(getGameProfile());

				if (userlistopsentry != null) {
					return userlistopsentry.getPermissionLevel() >= permLevel;
				} else {
					return mcServer.getOpPermissionLevel() >= permLevel;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Gets the player's IP address. Used in /banip.
	 */
	public String getPlayerIP() {

		String s = connection.netManager.getRemoteAddress().toString();
		s = s.substring(s.indexOf("/") + 1);
		s = s.substring(0, s.indexOf(":"));
		return s;
	}

	public void handleClientSettings(CPacketClientSettings packetIn) {

		language = packetIn.getLang();
		chatVisibility = packetIn.getChatVisibility();
		chatColours = packetIn.isColorsEnabled();
		getDataManager().set(PLAYER_MODEL_FLAG, (byte) packetIn.getModelPartFlags());
		getDataManager().set(MAIN_HAND, (byte) (packetIn.getMainHand() == EnumHandSide.LEFT ? 0 : 1));
	}

	public EntityPlayer.EnumChatVisibility getChatVisibility() {

		return chatVisibility;
	}

	public void loadResourcePack(String url, String hash) {

		connection.sendPacket(new SPacketResourcePackSend(url, hash));
	}

	public void markPlayerActive() {

		playerLastActiveTime = MinecraftServer.getCurrentTimeMillis();
	}

	/**
	 * Gets the stats file for reading achievements
	 */
	public StatisticsManagerServer getStatFile() {

		return statsFile;
	}

	public RecipeBookServer getRecipeBook() {

		return recipeBook;
	}

	/**
	 * Sends a packet to the player to remove an entity.
	 */
	public void removeEntity(Entity entityIn) {

		if (entityIn instanceof EntityPlayer) {
			connection.sendPacket(new SPacketDestroyEntities(entityIn.getEntityId()));
		} else {
			entityRemoveQueue.add(entityIn.getEntityId());
		}
	}

	public void addEntity(Entity entityIn) {

		entityRemoveQueue.remove(Integer.valueOf(entityIn.getEntityId()));
	}

	/**
	 * Clears potion metadata values if the entity has no potion effects. Otherwise, updates potion effect color,
	 * ambience, and invisibility metadata values
	 */
	protected void updatePotionMetadata() {

		if (isSpectator()) {
			resetPotionEffectMetadata();
			setInvisible(true);
		} else {
			super.updatePotionMetadata();
		}

		getServerWorld().getEntityTracker().updateVisibility(this);
	}

	public Entity getSpectatingEntity() {

		return spectatingEntity == null ? this : spectatingEntity;
	}

	public void setSpectatingEntity(Entity entityToSpectate) {

		Entity entity = getSpectatingEntity();
		spectatingEntity = entityToSpectate == null ? this : entityToSpectate;

		if (entity != spectatingEntity) {
			connection.sendPacket(new SPacketCamera(spectatingEntity));
			setPositionAndUpdate(spectatingEntity.posX, spectatingEntity.posY, spectatingEntity.posZ);
		}
	}

	/**
	 * Decrements the counter for the remaining time until the entity may use a portal again.
	 */
	protected void decrementTimeUntilPortal() {

		if (timeUntilPortal > 0 && !invulnerableDimensionChange) {
			--timeUntilPortal;
		}
	}

	/**
	 * Attacks for the player the targeted entity with the currently equipped item.  The equipped item has hitEntity
	 * called on it. Args: targetEntity
	 */
	public void attackTargetEntityWithCurrentItem(Entity targetEntity) {

		if (interactionManager.getGameType() == GameType.SPECTATOR) {
			setSpectatingEntity(targetEntity);
		} else {
			super.attackTargetEntityWithCurrentItem(targetEntity);
		}
	}

	public long getLastActiveTime() {

		return playerLastActiveTime;
	}

	

	/**
	 * Returns null which indicates the tab list should just display the player's name, return a different value to
	 * display the specified text instead of the player's name
	 */
	public ITextComponent getTabListDisplayName() {

		return null;
	}

	public void swingArm(EnumHand hand) {

		super.swingArm(hand);
		resetCooldown();
	}

	public boolean isInvulnerableDimensionChange() {

		return invulnerableDimensionChange;
	}

	public void clearInvulnerableDimensionChange() {

		invulnerableDimensionChange = false;
	}

	public void setElytraFlying() {

		setFlag(7, true);
	}

	public void clearElytraFlying() {

		setFlag(7, true);
		setFlag(7, false);
	}

	public PlayerAdvancements getAdvancements() {

		return advancements;
	}

	
	public Vec3d getEnteredNetherPosition() {

		return enteredNetherPosition;
	}

}
