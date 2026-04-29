package net.minecraft.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class PlayerInteractionManager {

	/**
	 * The world object that this object is connected to.
	 */
	public World world;

	/**
	 * The EntityPlayerMP object that this object is connected to.
	 */
	public EntityPlayerMP player;
	private GameType gameType = GameType.NOT_SET;

	/**
	 * True if the player is destroying a block
	 */
	private boolean isDestroyingBlock;
	private int initialDamage;
	private BlockPos destroyPos = BlockPos.ORIGIN;
	private int curblockDamage;

	/**
	 * Set to true when the "finished destroying block" packet is received but the block wasn't fully damaged yet. The
	 * block will not be destroyed while this is false.
	 */
	private boolean receivedFinishDiggingPacket;
	private BlockPos delayedDestroyPos = BlockPos.ORIGIN;
	private int initialBlockDamage;
	private int durabilityRemainingOnBlock = -1;

	public PlayerInteractionManager(World worldIn) {

		world = worldIn;
	}

	public GameType getGameType() {

		return gameType;
	}

	public void setGameType(GameType type) {

		gameType = type;
		type.configurePlayerCapabilities(player.capabilities);
		player.sendPlayerAbilities();
		player.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, player));
		world.updateAllPlayersSleepingFlag();
	}

	public boolean survivalOrAdventure() {

		return gameType.isSurvivalOrAdventure();
	}

	/**
	 * Get if we are in creative game mode.
	 */
	public boolean isCreative() {

		return gameType.isCreative();
	}

	/**
	 * if the gameType is currently NOT_SET then change it to par1
	 */
	public void initializeGameType(GameType type) {

		if (gameType == GameType.NOT_SET) {
			gameType = type;
		}

		setGameType(gameType);
	}

	public void updateBlockRemoving() {

		++curblockDamage;

		if (receivedFinishDiggingPacket) {
			int i = curblockDamage - initialBlockDamage;
			IBlockState iblockstate = world.getBlockState(delayedDestroyPos);

			if (iblockstate.getMaterial() == Material.AIR) {
				receivedFinishDiggingPacket = false;
			} else {
				float f = iblockstate.getPlayerRelativeBlockHardness(player, player.world, delayedDestroyPos) * (float) (i + 1);
				int j = (int) (f * 10F);

				if (j != durabilityRemainingOnBlock) {
					world.sendBlockBreakProgress(player.getEntityId(), delayedDestroyPos, j);
					durabilityRemainingOnBlock = j;
				}

				if (f >= 1F) {
					receivedFinishDiggingPacket = false;
					tryHarvestBlock(delayedDestroyPos);
				}
			}
		} else if (isDestroyingBlock) {
			IBlockState iblockstate1 = world.getBlockState(destroyPos);

			if (iblockstate1.getMaterial() == Material.AIR) {
				world.sendBlockBreakProgress(player.getEntityId(), destroyPos, -1);
				durabilityRemainingOnBlock = -1;
				isDestroyingBlock = false;
			} else {
				int k = curblockDamage - initialDamage;
				float f1 = iblockstate1.getPlayerRelativeBlockHardness(player, player.world, delayedDestroyPos) * (float) (k + 1);
				int l = (int) (f1 * 10F);

				if (l != durabilityRemainingOnBlock) {
					world.sendBlockBreakProgress(player.getEntityId(), destroyPos, l);
					durabilityRemainingOnBlock = l;
				}
			}
		}
	}

	/**
	 * If not creative, it calls sendBlockBreakProgress until the block is broken first. tryHarvestBlock can also be the
	 * result of this call.
	 */
	public void onBlockClicked(BlockPos pos, Facing side) {

		if (isCreative()) {
			if (!world.extinguishFire(null, pos, side)) {
				tryHarvestBlock(pos);
			}
		} else {
			IBlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if (gameType.hasLimitedInteractions()) {
				if (gameType == GameType.SPECTATOR) {
					return;
				}

				if (!player.isAllowEdit()) {
					ItemStack itemstack = player.getHeldItemMainhand();

					if (itemstack.isEmpty()) {
						return;
					}

					if (!itemstack.canDestroy(block)) {
						return;
					}
				}
			}

			world.extinguishFire(null, pos, side);
			initialDamage = curblockDamage;
			float f = 1F;

			if (iblockstate.getMaterial() != Material.AIR) {
				block.onBlockClicked(world, pos, player);
				f = iblockstate.getPlayerRelativeBlockHardness(player, player.world, pos);
			}

			if (iblockstate.getMaterial() != Material.AIR && f >= 1F) {
				tryHarvestBlock(pos);
			} else {
				isDestroyingBlock = true;
				destroyPos = pos;
				int i = (int) (f * 10F);
				world.sendBlockBreakProgress(player.getEntityId(), pos, i);
				durabilityRemainingOnBlock = i;
			}
		}
	}

	public void blockRemoving(BlockPos pos) {

		if (pos.equals(destroyPos)) {
			int i = curblockDamage - initialDamage;
			IBlockState iblockstate = world.getBlockState(pos);

			if (iblockstate.getMaterial() != Material.AIR) {
				float f = iblockstate.getPlayerRelativeBlockHardness(player, player.world, pos) * (float) (i + 1);

				if (f >= 0.7F) {
					isDestroyingBlock = false;
					world.sendBlockBreakProgress(player.getEntityId(), pos, -1);
					tryHarvestBlock(pos);
				} else if (!receivedFinishDiggingPacket) {
					isDestroyingBlock = false;
					receivedFinishDiggingPacket = true;
					delayedDestroyPos = pos;
					initialBlockDamage = initialDamage;
				}
			}
		}
	}

	/**
	 * Stops the block breaking process
	 */
	public void cancelDestroyingBlock() {

		isDestroyingBlock = false;
		world.sendBlockBreakProgress(player.getEntityId(), destroyPos, -1);
	}

	/**
	 * Removes a block and triggers the appropriate events
	 */
	private boolean removeBlock(BlockPos pos) {

		IBlockState iblockstate = world.getBlockState(pos);
		iblockstate.getBlock().onBlockHarvested(world, pos, iblockstate, player);
		boolean flag = world.setBlockToAir(pos);

		if (flag) {
			iblockstate.getBlock().onBlockDestroyedByPlayer(world, pos, iblockstate);
		}

		return flag;
	}

	/**
	 * Attempts to harvest a block
	 */
	public boolean tryHarvestBlock(BlockPos pos) {

		if (gameType.isCreative() && !player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemSword) {
			return false;
		} else {
			IBlockState iblockstate = world.getBlockState(pos);
			TileEntity tileentity = world.getTileEntity(pos);
			Block block = iblockstate.getBlock();

			if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock()) {
				world.notifyBlockUpdate(pos, iblockstate, iblockstate, 3);
				return false;
			} else {
				if (gameType.hasLimitedInteractions()) {
					if (gameType == GameType.SPECTATOR) {
						return false;
					}

					if (!player.isAllowEdit()) {
						ItemStack itemstack = player.getHeldItemMainhand();

						if (itemstack.isEmpty()) {
							return false;
						}

						if (!itemstack.canDestroy(block)) {
							return false;
						}
					}
				}

				world.playEvent(player, 2001, pos, Block.getStateId(iblockstate));
				boolean flag1 = removeBlock(pos);

				if (isCreative()) {
					player.connection.sendPacket(new SPacketBlockChange(world, pos));
				} else {
					ItemStack itemstack1 = player.getHeldItemMainhand();
					ItemStack itemstack2 = itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy();
					boolean flag = player.canHarvestBlock(iblockstate);

					if (!itemstack1.isEmpty()) {
						itemstack1.onBlockDestroyed(world, iblockstate, pos, player);
					}

					if (flag1 && flag) {
						iblockstate.getBlock().harvestBlock(world, player, pos, iblockstate, tileentity, itemstack2);
					}
				}

				return flag1;
			}
		}
	}

	public ActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, Hand hand) {

		if (gameType == GameType.SPECTATOR) {
			return ActionResult.PASS;
		} else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
			return ActionResult.PASS;
		} else {
			int i = stack.getCount();
			int j = stack.getMetadata();
			TypedActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
			ItemStack itemstack = actionresult.result();

			if (itemstack == stack && itemstack.getCount() == i && itemstack.getMaxItemUseDuration() <= 0 && itemstack.getMetadata() == j) {
				return actionresult.type();
			} else if (actionresult.type() == ActionResult.FAIL && itemstack.getMaxItemUseDuration() > 0 && !player.isHandActive()) {
				return actionresult.type();
			} else {
				player.setHeldItem(hand, itemstack);

				if (isCreative()) {
					itemstack.setCount(i);

					if (itemstack.isItemStackDamageable()) {
						itemstack.setItemDamage(j);
					}
				}

				if (itemstack.isEmpty()) {
					player.setHeldItem(hand, ItemStack.EMPTY);
				}

				if (!player.isHandActive()) {
					((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
				}

				return actionresult.type();
			}
		}
	}

	public ActionResult processRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, Hand hand, BlockPos pos, Facing facing, float hitX, float hitY, float hitZ) {

		if (gameType == GameType.SPECTATOR) {
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof ILockableContainer ilockablecontainer) {
				Block block1 = worldIn.getBlockState(pos).getBlock();

				if (ilockablecontainer instanceof TileEntityChest && block1 instanceof BlockChest) {
					ilockablecontainer = ((BlockChest) block1).getLockableContainer(worldIn, pos);
				}

				if (ilockablecontainer != null) {
					player.displayGUIChest(ilockablecontainer);
					return ActionResult.SUCCESS;
				}
			} else if (tileentity instanceof IInventory) {
				player.displayGUIChest((IInventory) tileentity);
				return ActionResult.SUCCESS;
			}

			return ActionResult.PASS;
		} else {
			if (!player.isSneaking() || player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) {
				IBlockState iblockstate = worldIn.getBlockState(pos);

				if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
					return ActionResult.SUCCESS;
				}
			}

			if (stack.isEmpty()) {
				return ActionResult.PASS;
			} else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
				return ActionResult.PASS;
			} else {
				if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
					Block block = ((ItemBlock) stack.getItem()).getBlock();

					if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
						return ActionResult.FAIL;
					}
				}

				if (isCreative()) {
					int j = stack.getMetadata();
					int i = stack.getCount();
					ActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
					stack.setItemDamage(j);
					stack.setCount(i);
					return enumactionresult;
				} else {
					return stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
				}
			}
		}
	}

	/**
	 * Sets the world instance.
	 */
	public void setWorld(WorldServer serverWorld) {

		world = serverWorld;
	}

}
