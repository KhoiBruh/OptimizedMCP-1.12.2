package net.minecraft.client.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.game.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import java.util.List;

public class EntityPlayerSP extends AbstractClientPlayer {

	public final NetHandlerPlayClient connection;
	private final StatisticsManager statWriter;
	private final RecipeBook recipeBook;
	public MovementInput movementInput;
	/**
	 * Ticks left before sprinting is disabled.
	 */
	public int sprintingTicksLeft;
	public float renderArmYaw;
	public float renderArmPitch;
	public float prevRenderArmYaw;
	public float prevRenderArmPitch;
	/**
	 * The amount of time an entity has been in a Portal
	 */
	public float timeInPortal;
	/**
	 * The amount of time an entity has been in a Portal the previous tick
	 */
	public float prevTimeInPortal;
	protected Minecraft mc;
	/**
	 * Used to tell if the player pressed forward twice. If this is at 0 and it's pressed (And they are allowed to
	 * sprint, aka enough food on the ground etc) it sets this to 7. If it's pressed and it's greater than 0 enable
	 * sprinting.
	 */
	protected int sprintToggleTimer;
	private int permissionLevel = 0;
	/**
	 * The last X position which was transmitted to the server, used to determine when the X position changes and needs
	 * to be re-trasmitted
	 */
	private double lastReportedPosX;
	/**
	 * The last Y position which was transmitted to the server, used to determine when the Y position changes and needs
	 * to be re-transmitted
	 */
	private double lastReportedPosY;
	/**
	 * The last Z position which was transmitted to the server, used to determine when the Z position changes and needs
	 * to be re-transmitted
	 */
	private double lastReportedPosZ;
	/**
	 * The last yaw value which was transmitted to the server, used to determine when the yaw changes and needs to be
	 * re-transmitted
	 */
	private float lastReportedYaw;
	/**
	 * The last pitch value which was transmitted to the server, used to determine when the pitch changes and needs to
	 * be re-transmitted
	 */
	private float lastReportedPitch;
	private boolean prevOnGround;
	/**
	 * the last sneaking state sent to the server
	 */
	private boolean serverSneakState;
	/**
	 * the last sprinting state sent to the server
	 */
	private boolean serverSprintState;
	/**
	 * Reset to 0 every time position is sent to the server, used to send periodic updates every 20 ticks even when the
	 * player is not moving.
	 */
	private int positionUpdateTicks;
	private boolean hasValidHealth;
	private String serverBrand;
	private int horseJumpPowerCounter;
	private float horseJumpPower;
	private boolean handActive;
	private EnumHand activeHand;
	private boolean rowingBoat;
	private boolean autoJumpEnabled = true;
	private int autoJumpTime;
	private boolean wasFallFlying;

	public EntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {

		super(p_i47378_2_, p_i47378_3_.getGameProfile());
		connection = p_i47378_3_;
		statWriter = p_i47378_4_;
		recipeBook = p_i47378_5_;
		mc = p_i47378_1_;
		dimension = 0;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {

		return false;
	}

	/**
	 * Heal living entity (param: amount of half-hearts)
	 */
	public void heal(float healAmount) {

	}

	public boolean startRiding(Entity entityIn, boolean force) {

		if (!super.startRiding(entityIn, force)) {
			return false;
		} else {
			if (entityIn instanceof EntityMinecart) {
				mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart) entityIn));
			}

			if (entityIn instanceof EntityBoat) {
				prevRotationYaw = entityIn.rotationYaw;
				rotationYaw = entityIn.rotationYaw;
				setRotationYawHead(entityIn.rotationYaw);
			}

			return true;
		}
	}

	/**
	 * Dismounts this entity from the entity it is riding.
	 */
	public void dismountRidingEntity() {

		super.dismountRidingEntity();
		rowingBoat = false;
	}

	/**
	 * interpolated look vector
	 */
	public Vec3d getLook(float partialTicks) {

		return getVectorForRotation(rotationPitch, rotationYaw);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {

		if (world.isBlockLoaded(new BlockPos(posX, 0D, posZ))) {
			super.onUpdate();

			if (isRiding()) {
				connection.sendPacket(new CPacketPlayer.Rotation(rotationYaw, rotationPitch, onGround));
				connection.sendPacket(new CPacketInput(moveStrafing, moveForward, movementInput.jump, movementInput.sneak));
				Entity entity = getLowestRidingEntity();

				if (entity != this && entity.canPassengerSteer()) {
					connection.sendPacket(new CPacketVehicleMove(entity));
				}
			} else {
				onUpdateWalkingPlayer();
			}
		}
	}

	/**
	 * called every tick when the player is on foot. Performs all the things that normally happen during movement.
	 */
	private void onUpdateWalkingPlayer() {

		boolean flag = isSprinting();

		if (flag != serverSprintState) {
			if (flag) {
				connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
			} else {
				connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
			}

			serverSprintState = flag;
		}

		boolean flag1 = isSneaking();

		if (flag1 != serverSneakState) {
			if (flag1) {
				connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
			} else {
				connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
			}

			serverSneakState = flag1;
		}

		if (isCurrentViewEntity()) {
			AxisAlignedBB axisalignedbb = getEntityBoundingBox();
			double d0 = posX - lastReportedPosX;
			double d1 = axisalignedbb.minY - lastReportedPosY;
			double d2 = posZ - lastReportedPosZ;
			double d3 = rotationYaw - lastReportedYaw;
			double d4 = rotationPitch - lastReportedPitch;
			++positionUpdateTicks;
			boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || positionUpdateTicks >= 20;
			boolean flag3 = d3 != 0D || d4 != 0D;

			if (isRiding()) {
				connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999D, motionZ, rotationYaw, rotationPitch, onGround));
				flag2 = false;
			} else if (flag2 && flag3) {
				connection.sendPacket(new CPacketPlayer.PositionRotation(posX, axisalignedbb.minY, posZ, rotationYaw, rotationPitch, onGround));
			} else if (flag2) {
				connection.sendPacket(new CPacketPlayer.Position(posX, axisalignedbb.minY, posZ, onGround));
			} else if (flag3) {
				connection.sendPacket(new CPacketPlayer.Rotation(rotationYaw, rotationPitch, onGround));
			} else if (prevOnGround != onGround) {
				connection.sendPacket(new CPacketPlayer(onGround));
			}

			if (flag2) {
				lastReportedPosX = posX;
				lastReportedPosY = axisalignedbb.minY;
				lastReportedPosZ = posZ;
				positionUpdateTicks = 0;
			}

			if (flag3) {
				lastReportedYaw = rotationYaw;
				lastReportedPitch = rotationPitch;
			}

			prevOnGround = onGround;
			autoJumpEnabled = mc.gameSettings.autoJump;
		}
	}

	

	/**
	 * Drop one item out of the currently selected stack if {@code dropAll} is false. If {@code dropItem} is true the
	 * entire stack is dropped.
	 */
	public EntityItem dropItem(boolean dropAll) {

		CPacketPlayerDigging.Action cpacketplayerdigging$action = dropAll ? CPacketPlayerDigging.Action.DROP_ALL_ITEMS : CPacketPlayerDigging.Action.DROP_ITEM;
		connection.sendPacket(new CPacketPlayerDigging(cpacketplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
		return null;
	}

	protected ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {

		return ItemStack.EMPTY;
	}

	/**
	 * Sends a chat message from the player.
	 */
	public void sendChatMessage(String message) {

		connection.sendPacket(new CPacketChatMessage(message));
	}

	public void swingArm(EnumHand hand) {

		super.swingArm(hand);
		connection.sendPacket(new CPacketAnimation(hand));
	}

	public void respawnPlayer() {

		connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
	}

	/**
	 * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
	 * bar.
	 */
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {

		if (!isEntityInvulnerable(damageSrc)) {
			setHealth(getHealth() - damageAmount);
		}
	}

	/**
	 * set current crafting inventory back to the 2x2 square
	 */
	public void closeScreen() {

		connection.sendPacket(new CPacketCloseWindow(openContainer.windowId));
		closeScreenAndDropStack();
	}

	public void closeScreenAndDropStack() {

		inventory.setItemStack(ItemStack.EMPTY);
		super.closeScreen();
		mc.displayGuiScreen(null);
	}

	/**
	 * Updates health locally.
	 */
	public void setPlayerSPHealth(float health) {

		if (hasValidHealth) {
			float f = getHealth() - health;

			if (f <= 0F) {
				setHealth(health);

				if (f < 0F) {
					hurtResistantTime = maxHurtResistantTime / 2;
				}
			} else {
				lastDamage = f;
				setHealth(getHealth());
				hurtResistantTime = maxHurtResistantTime;
				damageEntity(DamageSource.GENERIC, f);
				maxHurtTime = 10;
				hurtTime = maxHurtTime;
			}
		} else {
			setHealth(health);
			hasValidHealth = true;
		}
	}

	/**
	 * Adds a value to a statistic field.
	 */
	public void addStat(StatBase stat, int amount) {

		if (stat != null) {
			if (stat.isIndependent) {
				super.addStat(stat, amount);
			}
		}
	}

	/**
	 * Sends the player's abilities to the server (if there is one).
	 */
	public void sendPlayerAbilities() {

		connection.sendPacket(new CPacketPlayerAbilities(capabilities));
	}

	/**
	 * returns true if this is an EntityPlayerSP, or the logged in player.
	 */
	public boolean isUser() {

		return true;
	}

	protected void sendHorseJump() {

		connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_RIDING_JUMP, MathHelper.floor(getHorseJumpPower() * 100F)));
	}

	public void sendHorseInventory() {

		connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.OPEN_INVENTORY));
	}

	/**
	 * Gets the brand of the currently connected server. May be null if the server hasn't yet sent brand information.
	 * Server brand information is sent over the {@code MC|Brand} plugin channel, and is used to identify modded servers
	 * in crash reports.
	 */
	public String getServerBrand() {

		return serverBrand;
	}

	/**
	 * Sets the brand of the currently connected server. Server brand information is sent over the {@code MC|Brand}
	 * plugin channel, and is used to identify modded servers in crash reports.
	 */
	public void setServerBrand(String brand) {

		serverBrand = brand;
	}

	public StatisticsManager getStatFileWriter() {

		return statWriter;
	}

	public RecipeBook getRecipeBook() {

		return recipeBook;
	}

	public void removeRecipeHighlight(IRecipe p_193103_1_) {

		if (recipeBook.isNew(p_193103_1_)) {
			recipeBook.markSeen(p_193103_1_);
			connection.sendPacket(new CPacketRecipeInfo(p_193103_1_));
		}
	}

	public int getPermissionLevel() {

		return permissionLevel;
	}

	public void setPermissionLevel(int p_184839_1_) {

		permissionLevel = p_184839_1_;
	}

	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {

		if (actionBar) {
			mc.ingameGUI.setOverlayMessage(chatComponent, false);
		} else {
			mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
		}
	}

	protected boolean pushOutOfBlocks(double x, double y, double z) {

		if (noClip) {
			return false;
		} else {
			BlockPos blockpos = new BlockPos(x, y, z);
			double d0 = x - (double) blockpos.getX();
			double d1 = z - (double) blockpos.getZ();

			if (!isOpenBlockSpace(blockpos)) {
				int i = -1;
				double d2 = 9999D;

				if (isOpenBlockSpace(blockpos.west()) && d0 < d2) {
					d2 = d0;
					i = 0;
				}

				if (isOpenBlockSpace(blockpos.east()) && 1D - d0 < d2) {
					d2 = 1D - d0;
					i = 1;
				}

				if (isOpenBlockSpace(blockpos.north()) && d1 < d2) {
					d2 = d1;
					i = 4;
				}

				if (isOpenBlockSpace(blockpos.south()) && 1D - d1 < d2) {
					i = 5;
				}

				float f = 0.1F;

				if (i == 0) {
					motionX = -0.10000000149011612D;
				}

				if (i == 1) {
					motionX = 0.10000000149011612D;
				}

				if (i == 4) {
					motionZ = -0.10000000149011612D;
				}

				if (i == 5) {
					motionZ = 0.10000000149011612D;
				}
			}

			return false;
		}
	}

	/**
	 * Returns true if the block at the given BlockPos and the block above it are NOT full cubes.
	 */
	private boolean isOpenBlockSpace(BlockPos pos) {

		return !world.getBlockState(pos).isNormalCube() && !world.getBlockState(pos.up()).isNormalCube();
	}

	/**
	 * Set sprinting switch for Entity.
	 */
	public void setSprinting(boolean sprinting) {

		super.setSprinting(sprinting);
		sprintingTicksLeft = 0;
	}

	/**
	 * Sets the current XP, total XP, and level number.
	 */
	public void setXPStats(float currentXP, int maxXP, int level) {

		experience = currentXP;
		experienceTotal = maxXP;
		experienceLevel = level;
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(ITextComponent component) {

		mc.ingameGUI.getChatGUI().printChatMessage(component);
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canUseCommand(int permLevel, String commandName) {

		return permLevel <= getPermissionLevel();
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	public void handleStatusUpdate(byte id) {

		if (id >= 24 && id <= 28) {
			setPermissionLevel(id - 24);
		} else {
			super.handleStatusUpdate(id);
		}
	}

	/**
	 * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
	 * the coordinates 0, 0, 0
	 */
	public BlockPos getPosition() {

		return new BlockPos(posX + 0.5D, posY + 0.5D, posZ + 0.5D);
	}

	public void playSound(SoundEvent soundIn, float volume, float pitch) {

		world.playSound(posX, posY, posZ, soundIn, getSoundCategory(), volume, pitch, false);
	}

	/**
	 * Returns whether the entity is in a server world
	 */
	public boolean isServerWorld() {

		return true;
	}

	public boolean isHandActive() {

		return handActive;
	}

	public void resetActiveHand() {

		super.resetActiveHand();
		handActive = false;
	}

	public EnumHand getActiveHand() {

		return activeHand;
	}

	public void setActiveHand(EnumHand hand) {

		ItemStack itemstack = getHeldItem(hand);

		if (!itemstack.isEmpty() && !isHandActive()) {
			super.setActiveHand(hand);
			handActive = true;
			activeHand = hand;
		}
	}

	public void notifyDataManagerChange(DataParameter<?> key) {

		super.notifyDataManagerChange(key);

		if (HAND_STATES.equals(key)) {
			boolean flag = (dataManager.get(HAND_STATES) & 1) > 0;
			EnumHand enumhand = (dataManager.get(HAND_STATES) & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

			if (flag && !handActive) {
				setActiveHand(enumhand);
			} else if (!flag && handActive) {
				resetActiveHand();
			}
		}

		if (FLAGS.equals(key) && isElytraFlying() && !wasFallFlying) {
			mc.getSoundHandler().playSound(new ElytraSound(this));
		}
	}

	public boolean isRidingHorse() {

		Entity entity = getRidingEntity();
		return isRiding() && entity instanceof IJumpingMount && ((IJumpingMount) entity).canJump();
	}

	public float getHorseJumpPower() {

		return horseJumpPower;
	}

	public void openEditSign(TileEntitySign signTile) {

		mc.displayGuiScreen(new GuiEditSign(signTile));
	}

	public void displayGuiEditCommandCart(CommandBlockBaseLogic commandBlock) {

		mc.displayGuiScreen(new GuiEditCommandBlockMinecart(commandBlock));
	}

	public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {

		mc.displayGuiScreen(new GuiCommandBlock(commandBlock));
	}

	public void openEditStructure(TileEntityStructure structure) {

		mc.displayGuiScreen(new GuiEditStructure(structure));
	}

	public void openBook(ItemStack stack, EnumHand hand) {

		Item item = stack.getItem();

		if (item == Items.WRITABLE_BOOK) {
			mc.displayGuiScreen(new GuiScreenBook(this, stack, true));
		}
	}

	/**
	 * Displays the GUI for interacting with a chest inventory.
	 */
	public void displayGUIChest(IInventory chestInventory) {

		String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject) chestInventory).guiID() : "minecraft:container";

		if ("minecraft:chest".equals(s)) {
			mc.displayGuiScreen(new GuiChest(inventory, chestInventory));
		} else if ("minecraft:hopper".equals(s)) {
			mc.displayGuiScreen(new GuiHopper(inventory, chestInventory));
		} else if ("minecraft:furnace".equals(s)) {
			mc.displayGuiScreen(new GuiFurnace(inventory, chestInventory));
		} else if ("minecraft:brewing_stand".equals(s)) {
			mc.displayGuiScreen(new GuiBrewingStand(inventory, chestInventory));
		} else if ("minecraft:beacon".equals(s)) {
			mc.displayGuiScreen(new GuiBeacon(inventory, chestInventory));
		} else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
			if ("minecraft:shulker_box".equals(s)) {
				mc.displayGuiScreen(new GuiShulkerBox(inventory, chestInventory));
			} else {
				mc.displayGuiScreen(new GuiChest(inventory, chestInventory));
			}
		} else {
			mc.displayGuiScreen(new GuiDispenser(inventory, chestInventory));
		}
	}

	public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {

		mc.displayGuiScreen(new GuiScreenHorseInventory(inventory, inventoryIn, horse));
	}

	public void displayGui(IInteractionObject guiOwner) {

		String s = guiOwner.guiID();

		if ("minecraft:crafting_table".equals(s)) {
			mc.displayGuiScreen(new GuiCrafting(inventory, world));
		} else if ("minecraft:enchanting_table".equals(s)) {
			mc.displayGuiScreen(new GuiEnchantment(inventory, world, guiOwner));
		} else if ("minecraft:anvil".equals(s)) {
			mc.displayGuiScreen(new GuiRepair(inventory, world));
		}
	}

	public void displayVillagerTradeGui(IMerchant villager) {

		mc.displayGuiScreen(new GuiMerchant(inventory, villager, world));
	}

	/**
	 * Called when the entity is dealt a critical hit.
	 */
	public void onCriticalHit(Entity entityHit) {

		mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
	}

	public void onEnchantmentCritical(Entity entityHit) {

		mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
	}

	/**
	 * Returns if this entity is sneaking.
	 */
	public boolean isSneaking() {

		boolean flag = movementInput != null && movementInput.sneak;
		return flag && !sleeping;
	}

	public void updateEntityActionState() {

		super.updateEntityActionState();

		if (isCurrentViewEntity()) {
			moveStrafing = movementInput.moveStrafe;
			moveForward = movementInput.moveForward;
			isJumping = movementInput.jump;
			prevRenderArmYaw = renderArmYaw;
			prevRenderArmPitch = renderArmPitch;
			renderArmPitch = (float) ((double) renderArmPitch + (double) (rotationPitch - renderArmPitch) * 0.5D);
			renderArmYaw = (float) ((double) renderArmYaw + (double) (rotationYaw - renderArmYaw) * 0.5D);
		}
	}

	protected boolean isCurrentViewEntity() {

		return mc.getRenderViewEntity() == this;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate() {

		++sprintingTicksLeft;

		if (sprintToggleTimer > 0) {
			--sprintToggleTimer;
		}

		prevTimeInPortal = timeInPortal;

		if (inPortal) {
			if (mc.currentScreen != null && !mc.currentScreen.doesGuiPauseGame()) {
				if (mc.currentScreen instanceof GuiContainer) {
					closeScreen();
				}

				mc.displayGuiScreen(null);
			}

			if (timeInPortal == 0F) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRIGGER, rand.nextFloat() * 0.4F + 0.8F));
			}

			timeInPortal += 0.0125F;

			if (timeInPortal >= 1F) {
				timeInPortal = 1F;
			}

			inPortal = false;
		} else if (isPotionActive(MobEffects.NAUSEA) && getActivePotionEffect(MobEffects.NAUSEA).getDuration() > 60) {
			timeInPortal += 0.006666667F;

			if (timeInPortal > 1F) {
				timeInPortal = 1F;
			}
		} else {
			if (timeInPortal > 0F) {
				timeInPortal -= 0.05F;
			}

			if (timeInPortal < 0F) {
				timeInPortal = 0F;
			}
		}

		if (timeUntilPortal > 0) {
			--timeUntilPortal;
		}

		boolean flag = movementInput.jump;
		boolean flag1 = movementInput.sneak;
		float f = 0.8F;
		boolean flag2 = movementInput.moveForward >= 0.8F;
		movementInput.updatePlayerMoveState();
		mc.getTutorial().handleMovement(movementInput);

		if (isHandActive() && !isRiding()) {
			movementInput.moveStrafe *= 0.2F;
			movementInput.moveForward *= 0.2F;
			sprintToggleTimer = 0;
		}

		boolean flag3 = false;

		if (autoJumpTime > 0) {
			--autoJumpTime;
			flag3 = true;
			movementInput.jump = true;
		}

		AxisAlignedBB axisalignedbb = getEntityBoundingBox();
		pushOutOfBlocks(posX - (double) width * 0.35D, axisalignedbb.minY + 0.5D, posZ + (double) width * 0.35D);
		pushOutOfBlocks(posX - (double) width * 0.35D, axisalignedbb.minY + 0.5D, posZ - (double) width * 0.35D);
		pushOutOfBlocks(posX + (double) width * 0.35D, axisalignedbb.minY + 0.5D, posZ - (double) width * 0.35D);
		pushOutOfBlocks(posX + (double) width * 0.35D, axisalignedbb.minY + 0.5D, posZ + (double) width * 0.35D);
		boolean flag4 = (float) getFoodStats().getFoodLevel() > 6F || capabilities.allowFlying;

		if (onGround && !flag1 && !flag2 && movementInput.moveForward >= 0.8F && !isSprinting() && flag4 && !isHandActive() && !isPotionActive(MobEffects.BLINDNESS)) {
			if (sprintToggleTimer <= 0 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
				sprintToggleTimer = 7;
			} else {
				setSprinting(true);
			}
		}

		if (!isSprinting() && movementInput.moveForward >= 0.8F && flag4 && !isHandActive() && !isPotionActive(MobEffects.BLINDNESS) && mc.gameSettings.keyBindSprint.isKeyDown()) {
			setSprinting(true);
		}

		if (isSprinting() && (movementInput.moveForward < 0.8F || collidedHorizontally || !flag4)) {
			setSprinting(false);
		}

		if (capabilities.allowFlying) {
			if (mc.playerController.isSpectatorMode()) {
				if (!capabilities.isFlying) {
					capabilities.isFlying = true;
					sendPlayerAbilities();
				}
			} else if (!flag && movementInput.jump && !flag3) {
				if (flyToggleTimer == 0) {
					flyToggleTimer = 7;
				} else {
					capabilities.isFlying = !capabilities.isFlying;
					sendPlayerAbilities();
					flyToggleTimer = 0;
				}
			}
		}

		if (movementInput.jump && !flag && !onGround && motionY < 0D && !isElytraFlying() && !capabilities.isFlying) {
			ItemStack itemstack = getItemStackFromSlot(EntityEquipmentSlot.CHEST);

			if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
				connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
			}
		}

		wasFallFlying = isElytraFlying();

		if (capabilities.isFlying && isCurrentViewEntity()) {
			if (movementInput.sneak) {
				movementInput.moveStrafe = (float) ((double) movementInput.moveStrafe / 0.3D);
				movementInput.moveForward = (float) ((double) movementInput.moveForward / 0.3D);
				motionY -= capabilities.getFlySpeed() * 3F;
			}

			if (movementInput.jump) {
				motionY += capabilities.getFlySpeed() * 3F;
			}
		}

		if (isRidingHorse()) {
			IJumpingMount ijumpingmount = (IJumpingMount) getRidingEntity();

			if (horseJumpPowerCounter < 0) {
				++horseJumpPowerCounter;

				if (horseJumpPowerCounter == 0) {
					horseJumpPower = 0F;
				}
			}

			if (flag && !movementInput.jump) {
				horseJumpPowerCounter = -10;
				ijumpingmount.setJumpPower(MathHelper.floor(getHorseJumpPower() * 100F));
				sendHorseJump();
			} else if (!flag && movementInput.jump) {
				horseJumpPowerCounter = 0;
				horseJumpPower = 0F;
			} else if (flag) {
				++horseJumpPowerCounter;

				if (horseJumpPowerCounter < 10) {
					horseJumpPower = (float) horseJumpPowerCounter * 0.1F;
				} else {
					horseJumpPower = 0.8F + 2F / (float) (horseJumpPowerCounter - 9) * 0.1F;
				}
			}
		} else {
			horseJumpPower = 0F;
		}

		super.onLivingUpdate();

		if (onGround && capabilities.isFlying && !mc.playerController.isSpectatorMode()) {
			capabilities.isFlying = false;
			sendPlayerAbilities();
		}
	}

	/**
	 * Handles updating while riding another entity
	 */
	public void updateRidden() {

		super.updateRidden();
		rowingBoat = false;

		if (getRidingEntity() instanceof EntityBoat entityboat) {
			entityboat.updateInputs(movementInput.leftKeyDown, movementInput.rightKeyDown, movementInput.forwardKeyDown, movementInput.backKeyDown);
			rowingBoat |= movementInput.leftKeyDown || movementInput.rightKeyDown || movementInput.forwardKeyDown || movementInput.backKeyDown;
		}
	}

	public boolean isRowingBoat() {

		return rowingBoat;
	}

	

	/**
	 * Removes the given potion effect from the active potion map and returns it. Does not call cleanup callbacks for
	 * the end of the potion effect.
	 */
	public PotionEffect removeActivePotionEffect(Potion potioneffectin) {

		if (potioneffectin == MobEffects.NAUSEA) {
			prevTimeInPortal = 0F;
			timeInPortal = 0F;
		}

		return super.removeActivePotionEffect(potioneffectin);
	}

	/**
	 * Tries to move the entity towards the specified location.
	 */
	public void move(MoverType type, double x, double y, double z) {

		double d0 = posX;
		double d1 = posZ;
		super.move(type, x, y, z);
		updateAutoJump((float) (posX - d0), (float) (posZ - d1));
	}

	public boolean isAutoJumpEnabled() {

		return autoJumpEnabled;
	}

	protected void updateAutoJump(float p_189810_1_, float p_189810_2_) {

		if (isAutoJumpEnabled()) {
			if (autoJumpTime <= 0 && onGround && !isSneaking() && !isRiding()) {
				Vec2f vec2f = movementInput.getMoveVector();

				if (vec2f.x() != 0F || vec2f.y() != 0F) {
					Vec3d vec3d = new Vec3d(posX, getEntityBoundingBox().minY, posZ);
					double d0 = posX + (double) p_189810_1_;
					double d1 = posZ + (double) p_189810_2_;
					Vec3d vec3d1 = new Vec3d(d0, getEntityBoundingBox().minY, d1);
					Vec3d vec3d2 = new Vec3d(p_189810_1_, 0D, p_189810_2_);
					float f = getAIMoveSpeed();
					float f1 = (float) vec3d2.lengthSquared();

					if (f1 <= 0.001F) {
						float f2 = f * vec2f.x();
						float f3 = f * vec2f.y();
						float f4 = MathHelper.sin(rotationYaw * 0.017453292F);
						float f5 = MathHelper.cos(rotationYaw * 0.017453292F);
						vec3d2 = new Vec3d(f2 * f5 - f3 * f4, vec3d2.y(), f3 * f5 + f2 * f4);
						f1 = (float) vec3d2.lengthSquared();

						if (f1 <= 0.001F) {
							return;
						}
					}

					float f12 = (float) MathHelper.fastInvSqrt(f1);
					Vec3d vec3d12 = vec3d2.scale(f12);
					Vec3d vec3d13 = getForward();
					float f13 = (float) (vec3d13.x() * vec3d12.x() + vec3d13.z() * vec3d12.z());

					if (f13 >= -0.15F) {
						BlockPos blockpos = new BlockPos(posX, getEntityBoundingBox().maxY, posZ);
						IBlockState iblockstate = world.getBlockState(blockpos);

						if (iblockstate.getCollisionBoundingBox(world, blockpos) == null) {
							blockpos = blockpos.up();
							IBlockState iblockstate1 = world.getBlockState(blockpos);

							if (iblockstate1.getCollisionBoundingBox(world, blockpos) == null) {
								float f6 = 7F;
								float f7 = 1.2F;

								if (isPotionActive(MobEffects.JUMP_BOOST)) {
									f7 += (float) (getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
								}

								float f8 = Math.max(f * 7F, 1F / f12);
								Vec3d vec3d4 = vec3d1.add(vec3d12.scale(f8));
								float f9 = width;
								float f10 = height;
								AxisAlignedBB axisalignedbb = (new AxisAlignedBB(vec3d, vec3d4.addVector(0D, f10, 0D))).grow(f9, 0D, f9);
								Vec3d lvt_19_1_ = vec3d.addVector(0D, 0.5099999904632568D, 0D);
								vec3d4 = vec3d4.addVector(0D, 0.5099999904632568D, 0D);
								Vec3d vec3d5 = vec3d12.crossProduct(new Vec3d(0D, 1D, 0D));
								Vec3d vec3d6 = vec3d5.scale(f9 * 0.5F);
								Vec3d vec3d7 = lvt_19_1_.subtract(vec3d6);
								Vec3d vec3d8 = vec3d4.subtract(vec3d6);
								Vec3d vec3d9 = lvt_19_1_.add(vec3d6);
								Vec3d vec3d10 = vec3d4.add(vec3d6);
								List<AxisAlignedBB> list = world.getCollisionBoxes(this, axisalignedbb);

								if (!list.isEmpty()) {
								}

								float f11 = Float.MIN_VALUE;
								label86:

								for (AxisAlignedBB axisalignedbb2 : list) {
									if (axisalignedbb2.intersects(vec3d7, vec3d8) || axisalignedbb2.intersects(vec3d9, vec3d10)) {
										f11 = (float) axisalignedbb2.maxY;
										Vec3d vec3d11 = axisalignedbb2.getCenter();
										BlockPos blockpos1 = new BlockPos(vec3d11);
										int i = 1;

										while (true) {
											if ((float) i >= f7) {
												break label86;
											}

											BlockPos blockpos2 = blockpos1.up(i);
											IBlockState iblockstate2 = world.getBlockState(blockpos2);
											AxisAlignedBB axisalignedbb1;

											if ((axisalignedbb1 = iblockstate2.getCollisionBoundingBox(world, blockpos2)) != null) {
												f11 = (float) axisalignedbb1.maxY + (float) blockpos2.getY();

												if ((double) f11 - getEntityBoundingBox().minY > (double) f7) {
													return;
												}
											}

											if (i > 1) {
												blockpos = blockpos.up();
												IBlockState iblockstate3 = world.getBlockState(blockpos);

												if (iblockstate3.getCollisionBoundingBox(world, blockpos) != null) {
													return;
												}
											}

											++i;
										}
									}
								}

								if (f11 != Float.MIN_VALUE) {
									float f14 = (float) ((double) f11 - getEntityBoundingBox().minY);

									if (f14 > 0.5F && f14 <= f7) {
										autoJumpTime = 1;
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
