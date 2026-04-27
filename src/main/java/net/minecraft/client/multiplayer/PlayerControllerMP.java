package net.minecraft.client.multiplayer;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public class PlayerControllerMP {

	/**
	 * The Minecraft instance.
	 */
	private final Minecraft mc;
	private final NetHandlerPlayClient connection;
	private BlockPos currentBlock = new BlockPos(-1, -1, -1);

	/**
	 * The Item currently being used to destroy a block
	 */
	private ItemStack currentItemHittingBlock = ItemStack.EMPTY;

	/**
	 * Current block damage (MP)
	 */
	private float curBlockDamageMP;

	/**
	 * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
	 */
	private float stepSoundTickCounter;

	/**
	 * Delays the first damage on the block after the first click on the block
	 */
	private int blockHitDelay;

	/**
	 * Tells if the player is hitting a block
	 */
	private boolean isHittingBlock;

	/**
	 * Current game type for the player
	 */
	private GameType currentGameType = GameType.SURVIVAL;

	/**
	 * Index of the current item held by the player in the inventory hotbar
	 */
	private int currentPlayerItem;

	public PlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {

		mc = mcIn;
		connection = netHandler;
	}

	public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing) {

		if (!mcIn.world.extinguishFire(mcIn.player, pos, facing)) {
			playerController.onPlayerDestroyBlock(pos);
		}
	}

	/**
	 * Sets player capabilities depending on current gametype. params: player
	 */
	public void setPlayerCapabilities(EntityPlayer player) {

		currentGameType.configurePlayerCapabilities(player.capabilities);
	}

	/**
	 * None
	 */
	public boolean isSpectator() {

		return currentGameType == GameType.SPECTATOR;
	}

	/**
	 * Sets the game type for the player.
	 */
	public void setGameType(GameType type) {

		currentGameType = type;
		currentGameType.configurePlayerCapabilities(mc.player.capabilities);
	}

	/**
	 * Flips the player around.
	 */
	public void flipPlayer(EntityPlayer playerIn) {

		playerIn.rotationYaw = -180.0F;
	}

	public boolean shouldDrawHUD() {

		return currentGameType.isSurvivalOrAdventure();
	}

	public boolean onPlayerDestroyBlock(BlockPos pos) {

		if (currentGameType.hasLimitedInteractions()) {
			if (currentGameType == GameType.SPECTATOR) {
				return false;
			}

			if (!mc.player.isAllowEdit()) {
				ItemStack itemstack = mc.player.getHeldItemMainhand();

				if (itemstack.isEmpty()) {
					return false;
				}

				if (!itemstack.canDestroy(mc.world.getBlockState(pos).getBlock())) {
					return false;
				}
			}
		}

		if (currentGameType.isCreative() && !mc.player.getHeldItemMainhand().isEmpty() && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
			return false;
		} else {
			World world = mc.world;
			IBlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !mc.player.canUseCommandBlock()) {
				return false;
			} else if (iblockstate.getMaterial() == Material.AIR) {
				return false;
			} else {
				world.playEvent(2001, pos, Block.getStateId(iblockstate));
				block.onBlockHarvested(world, pos, iblockstate, mc.player);
				boolean flag = world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

				if (flag) {
					block.onBlockDestroyedByPlayer(world, pos, iblockstate);
				}

				currentBlock = new BlockPos(currentBlock.getX(), -1, currentBlock.getZ());

				if (!currentGameType.isCreative()) {
					ItemStack itemstack1 = mc.player.getHeldItemMainhand();

					if (!itemstack1.isEmpty()) {
						itemstack1.onBlockDestroyed(world, iblockstate, pos, mc.player);

						if (itemstack1.isEmpty()) {
							mc.player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
						}
					}
				}

				return flag;
			}
		}
	}

	/**
	 * Called when the player is hitting a block with an item.
	 */
	public boolean clickBlock(BlockPos loc, EnumFacing face) {

		if (currentGameType.hasLimitedInteractions()) {
			if (currentGameType == GameType.SPECTATOR) {
				return false;
			}

			if (!mc.player.isAllowEdit()) {
				ItemStack itemstack = mc.player.getHeldItemMainhand();

				if (itemstack.isEmpty()) {
					return false;
				}

				if (!itemstack.canDestroy(mc.world.getBlockState(loc).getBlock())) {
					return false;
				}
			}
		}

		if (!mc.world.getWorldBorder().contains(loc)) {
			return false;
		} else {
			if (currentGameType.isCreative()) {
				mc.getTutorial().onHitBlock(mc.world, loc, mc.world.getBlockState(loc), 1.0F);
				connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
				clickBlockCreative(mc, this, loc, face);
				blockHitDelay = 5;
			} else if (!isHittingBlock || !isHittingPosition(loc)) {
				if (isHittingBlock) {
					connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, face));
				}

				IBlockState iblockstate = mc.world.getBlockState(loc);
				mc.getTutorial().onHitBlock(mc.world, loc, iblockstate, 0.0F);
				connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
				boolean flag = iblockstate.getMaterial() != Material.AIR;

				if (flag && curBlockDamageMP == 0.0F) {
					iblockstate.getBlock().onBlockClicked(mc.world, loc, mc.player);
				}

				if (flag && iblockstate.getPlayerRelativeBlockHardness(mc.player, mc.player.world, loc) >= 1.0F) {
					onPlayerDestroyBlock(loc);
				} else {
					isHittingBlock = true;
					currentBlock = loc;
					currentItemHittingBlock = mc.player.getHeldItemMainhand();
					curBlockDamageMP = 0.0F;
					stepSoundTickCounter = 0.0F;
					mc.world.sendBlockBreakProgress(mc.player.getEntityId(), currentBlock, (int) (curBlockDamageMP * 10.0F) - 1);
				}
			}

			return true;
		}
	}

	/**
	 * Resets current block damage
	 */
	public void resetBlockRemoving() {

		if (isHittingBlock) {
			mc.getTutorial().onHitBlock(mc.world, currentBlock, mc.world.getBlockState(currentBlock), -1.0F);
			connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
			isHittingBlock = false;
			curBlockDamageMP = 0.0F;
			mc.world.sendBlockBreakProgress(mc.player.getEntityId(), currentBlock, -1);
			mc.player.resetCooldown();
		}
	}

	public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {

		syncCurrentPlayItem();

		if (blockHitDelay > 0) {
			--blockHitDelay;
			return true;
		} else if (currentGameType.isCreative() && mc.world.getWorldBorder().contains(posBlock)) {
			blockHitDelay = 5;
			mc.getTutorial().onHitBlock(mc.world, posBlock, mc.world.getBlockState(posBlock), 1.0F);
			connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
			clickBlockCreative(mc, this, posBlock, directionFacing);
			return true;
		} else if (isHittingPosition(posBlock)) {
			IBlockState iblockstate = mc.world.getBlockState(posBlock);
			Block block = iblockstate.getBlock();

			if (iblockstate.getMaterial() == Material.AIR) {
				isHittingBlock = false;
				return false;
			} else {
				curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(mc.player, mc.player.world, posBlock);

				if (stepSoundTickCounter % 4.0F == 0.0F) {
					SoundType soundtype = block.getSoundType();
					mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.hitSound(), SoundCategory.NEUTRAL, (soundtype.volume() + 1.0F) / 8.0F, soundtype.pitch() * 0.5F, posBlock));
				}

				++stepSoundTickCounter;
				mc.getTutorial().onHitBlock(mc.world, posBlock, iblockstate, MathHelper.clamp(curBlockDamageMP, 0.0F, 1.0F));

				if (curBlockDamageMP >= 1.0F) {
					isHittingBlock = false;
					connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
					onPlayerDestroyBlock(posBlock);
					curBlockDamageMP = 0.0F;
					stepSoundTickCounter = 0.0F;
					blockHitDelay = 5;
				}

				mc.world.sendBlockBreakProgress(mc.player.getEntityId(), currentBlock, (int) (curBlockDamageMP * 10.0F) - 1);
				return true;
			}
		} else {
			return clickBlock(posBlock, directionFacing);
		}
	}

	/**
	 * player reach distance = 4F
	 */
	public float getBlockReachDistance() {

		return currentGameType.isCreative() ? 5.0F : 4.5F;
	}

	public void updateController() {

		syncCurrentPlayItem();

		if (connection.getNetworkManager().isChannelOpen()) {
			connection.getNetworkManager().processReceivedPackets();
		} else {
			connection.getNetworkManager().checkDisconnected();
		}
	}

	private boolean isHittingPosition(BlockPos pos) {

		ItemStack itemstack = mc.player.getHeldItemMainhand();
		boolean flag = currentItemHittingBlock.isEmpty() && itemstack.isEmpty();

		if (!currentItemHittingBlock.isEmpty() && !itemstack.isEmpty()) {
			flag = itemstack.getItem() == currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == currentItemHittingBlock.getMetadata());
		}

		return pos.equals(currentBlock) && flag;
	}

	/**
	 * Syncs the current player item with the server
	 */
	private void syncCurrentPlayItem() {

		int i = mc.player.inventory.currentItem;

		if (i != currentPlayerItem) {
			currentPlayerItem = i;
			connection.sendPacket(new CPacketHeldItemChange(currentPlayerItem));
		}
	}

	public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {

		syncCurrentPlayItem();
		ItemStack itemstack = player.getHeldItem(hand);
		float f = (float) (vec.x() - (double) pos.getX());
		float f1 = (float) (vec.y() - (double) pos.getY());
		float f2 = (float) (vec.z() - (double) pos.getZ());
		boolean flag = false;

		if (!mc.world.getWorldBorder().contains(pos)) {
			return EnumActionResult.FAIL;
		} else {
			if (currentGameType != GameType.SPECTATOR) {
				IBlockState iblockstate = worldIn.getBlockState(pos);

				if ((!player.isSneaking() || player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) && iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, direction, f, f1, f2)) {
					flag = true;
				}

				if (!flag && itemstack.getItem() instanceof ItemBlock itemblock) {

					if (!itemblock.canPlaceBlockOnSide(worldIn, pos, direction, player, itemstack)) {
						return EnumActionResult.FAIL;
					}
				}
			}

			connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));

			if (!flag && currentGameType != GameType.SPECTATOR) {
				if (itemstack.isEmpty()) {
					return EnumActionResult.PASS;
				} else if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
					return EnumActionResult.PASS;
				} else {
					if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
						Block block = ((ItemBlock) itemstack.getItem()).getBlock();

						if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
							return EnumActionResult.FAIL;
						}
					}

					if (currentGameType.isCreative()) {
						int i = itemstack.getMetadata();
						int j = itemstack.getCount();
						EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
						itemstack.setItemDamage(i);
						itemstack.setCount(j);
						return enumactionresult;
					} else {
						return itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
					}
				}
			} else {
				return EnumActionResult.SUCCESS;
			}
		}
	}

	public EnumActionResult processRightClick(EntityPlayer player, World worldIn, EnumHand hand) {

		if (currentGameType == GameType.SPECTATOR) {
			return EnumActionResult.PASS;
		} else {
			syncCurrentPlayItem();
			connection.sendPacket(new CPacketPlayerTryUseItem(hand));
			ItemStack itemstack = player.getHeldItem(hand);

			if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
				return EnumActionResult.PASS;
			} else {
				int i = itemstack.getCount();
				ActionResult<ItemStack> actionresult = itemstack.useItemRightClick(worldIn, player, hand);
				ItemStack itemstack1 = actionresult.result();

				if (itemstack1 != itemstack || itemstack1.getCount() != i) {
					player.setHeldItem(hand, itemstack1);
				}

				return actionresult.type();
			}
		}
	}

	public EntityPlayerSP createPlayer(World p_192830_1_, StatisticsManager p_192830_2_, RecipeBook p_192830_3_) {

		return new EntityPlayerSP(mc, p_192830_1_, connection, p_192830_2_, p_192830_3_);
	}

	/**
	 * Attacks an entity
	 */
	public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {

		syncCurrentPlayItem();
		connection.sendPacket(new CPacketUseEntity(targetEntity));

		if (currentGameType != GameType.SPECTATOR) {
			playerIn.attackTargetEntityWithCurrentItem(targetEntity);
			playerIn.resetCooldown();
		}
	}

	/**
	 * Handles right clicking an entity, sends a packet to the server.
	 */
	public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, EnumHand hand) {

		syncCurrentPlayItem();
		connection.sendPacket(new CPacketUseEntity(target, hand));
		return currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : player.interactOn(target, hand);
	}

	/**
	 * Handles right clicking an entity from the entities side, sends a packet to the server.
	 */
	public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, RayTraceResult ray, EnumHand hand) {

		syncCurrentPlayItem();
		Vec3d vec3d = new Vec3d(ray.hitVec.x() - target.posX, ray.hitVec.y() - target.posY, ray.hitVec.z() - target.posZ);
		connection.sendPacket(new CPacketUseEntity(target, hand, vec3d));
		return currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : target.applyPlayerInteraction(player, vec3d, hand);
	}

	/**
	 * Handles slot clicks, sends a packet to the server.
	 */
	public ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player) {

		short short1 = player.openContainer.getNextTransactionID(player.inventory);
		ItemStack itemstack = player.openContainer.slotClick(slotId, mouseButton, type, player);
		connection.sendPacket(new CPacketClickWindow(windowId, slotId, mouseButton, type, itemstack, short1));
		return itemstack;
	}

	public void func_194338_a(int p_194338_1_, IRecipe p_194338_2_, boolean p_194338_3_, EntityPlayer p_194338_4_) {

		connection.sendPacket(new CPacketPlaceRecipe(p_194338_1_, p_194338_2_, p_194338_3_));
	}

	/**
	 * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
	 * enchantment action the player has taken.
	 */
	public void sendEnchantPacket(int windowID, int button) {

		connection.sendPacket(new CPacketEnchantItem(windowID, button));
	}

	/**
	 * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
	 */
	public void sendSlotPacket(ItemStack itemStackIn, int slotId) {

		if (currentGameType.isCreative()) {
			connection.sendPacket(new CPacketCreativeInventoryAction(slotId, itemStackIn));
		}
	}

	/**
	 * Sends a Packet107 to the server to drop the item on the ground
	 */
	public void sendPacketDropItem(ItemStack itemStackIn) {

		if (currentGameType.isCreative() && !itemStackIn.isEmpty()) {
			connection.sendPacket(new CPacketCreativeInventoryAction(-1, itemStackIn));
		}
	}

	public void onStoppedUsingItem(EntityPlayer playerIn) {

		syncCurrentPlayItem();
		connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
		playerIn.stopActiveHand();
	}

	public boolean gameIsSurvivalOrAdventure() {

		return currentGameType.isSurvivalOrAdventure();
	}

	/**
	 * Checks if the player is not creative, used for checking if it should break a block instantly
	 */
	public boolean isNotCreative() {

		return !currentGameType.isCreative();
	}

	/**
	 * returns true if player is in creative mode
	 */
	public boolean isInCreativeMode() {

		return currentGameType.isCreative();
	}

	/**
	 * true for hitting entities far away.
	 */
	public boolean extendedReach() {

		return currentGameType.isCreative();
	}

	/**
	 * Checks if the player is riding a horse, used to chose the GUI to open
	 */
	public boolean isRidingHorse() {

		return mc.player.isRiding() && mc.player.getRidingEntity() instanceof AbstractHorse;
	}

	public boolean isSpectatorMode() {

		return currentGameType == GameType.SPECTATOR;
	}

	public GameType getCurrentGameType() {

		return currentGameType;
	}

	/**
	 * Return isHittingBlock
	 */
	public boolean getIsHittingBlock() {

		return isHittingBlock;
	}

	public void pickItem(int index) {

		connection.sendPacket(new CPacketCustomPayload("MC|PickItem", (new PacketBuffer(Unpooled.buffer())).writeVarInt(index)));
	}

}
