package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;

public class TileEntityShulkerBox extends TileEntityLockableLoot implements ITickable, ISidedInventory {

	private static final int[] SLOTS = new int[27];

	static {
		for (int i = 0; i < SLOTS.length; SLOTS[i] = i++) {
		}
	}

	private NonNullList<ItemStack> items;
	private boolean hasBeenCleared;
	private int openCount;
	private TileEntityShulkerBox.AnimationStatus animationStatus;
	private float progress;
	private float progressOld;
	private EnumDyeColor color;
	private boolean destroyedByCreativePlayer;

	public TileEntityShulkerBox() {

		this(null);
	}

	public TileEntityShulkerBox(EnumDyeColor colorIn) {

		items = NonNullList.withSize(27, ItemStack.EMPTY);
		animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSED;
		color = colorIn;
	}

	public static void registerFixesShulkerBox(DataFixer fixer) {

		fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityShulkerBox.class, "Items"));
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	public void update() {

		updateAnimation();

		if (animationStatus == TileEntityShulkerBox.AnimationStatus.OPENING || animationStatus == TileEntityShulkerBox.AnimationStatus.CLOSING) {
			moveCollidedEntities();
		}
	}

	protected void updateAnimation() {

		progressOld = progress;

		switch (animationStatus) {
			case CLOSED:
				progress = 0.0F;
				break;

			case OPENING:
				progress += 0.1F;

				if (progress >= 1.0F) {
					moveCollidedEntities();
					animationStatus = TileEntityShulkerBox.AnimationStatus.OPENED;
					progress = 1.0F;
				}

				break;

			case CLOSING:
				progress -= 0.1F;

				if (progress <= 0.0F) {
					animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSED;
					progress = 0.0F;
				}

				break;

			case OPENED:
				progress = 1.0F;
		}
	}

	public TileEntityShulkerBox.AnimationStatus getAnimationStatus() {

		return animationStatus;
	}

	public AxisAlignedBB getBoundingBox(IBlockState p_190584_1_) {

		return getBoundingBox(p_190584_1_.getValue(BlockShulkerBox.FACING));
	}

	public AxisAlignedBB getBoundingBox(EnumFacing p_190587_1_) {

		return Block.FULL_BLOCK_AABB.expand(0.5F * getProgress(1.0F) * (float) p_190587_1_.getFrontOffsetX(), 0.5F * getProgress(1.0F) * (float) p_190587_1_.getFrontOffsetY(), 0.5F * getProgress(1.0F) * (float) p_190587_1_.getFrontOffsetZ());
	}

	private AxisAlignedBB getTopBoundingBox(EnumFacing p_190588_1_) {

		EnumFacing enumfacing = p_190588_1_.getOpposite();
		return getBoundingBox(p_190588_1_).contract(enumfacing.getFrontOffsetX(), enumfacing.getFrontOffsetY(), enumfacing.getFrontOffsetZ());
	}

	private void moveCollidedEntities() {

		IBlockState iblockstate = world.getBlockState(getPos());

		if (iblockstate.getBlock() instanceof BlockShulkerBox) {
			EnumFacing enumfacing = iblockstate.getValue(BlockShulkerBox.FACING);
			AxisAlignedBB axisalignedbb = getTopBoundingBox(enumfacing).offset(pos);
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

			if (!list.isEmpty()) {
				for (Entity entity : list) {
					if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
						double d0 = 0.0D;
						double d1 = 0.0D;
						double d2 = 0.0D;
						AxisAlignedBB axisalignedbb1 = entity.getEntityBoundingBox();

						switch (enumfacing.getAxis()) {
							case X:
								if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
									d0 = axisalignedbb.maxX - axisalignedbb1.minX;
								} else {
									d0 = axisalignedbb1.maxX - axisalignedbb.minX;
								}

								d0 = d0 + 0.01D;
								break;

							case Y:
								if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
									d1 = axisalignedbb.maxY - axisalignedbb1.minY;
								} else {
									d1 = axisalignedbb1.maxY - axisalignedbb.minY;
								}

								d1 = d1 + 0.01D;
								break;

							case Z:
								if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
									d2 = axisalignedbb.maxZ - axisalignedbb1.minZ;
								} else {
									d2 = axisalignedbb1.maxZ - axisalignedbb.minZ;
								}

								d2 = d2 + 0.01D;
						}

						entity.move(MoverType.SHULKER_BOX, d0 * (double) enumfacing.getFrontOffsetX(), d1 * (double) enumfacing.getFrontOffsetY(), d2 * (double) enumfacing.getFrontOffsetZ());
					}
				}
			}
		}
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory() {

		return items.size();
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	public int getInventoryStackLimit() {

		return 64;
	}

	public boolean receiveClientEvent(int id, int type) {

		if (id == 1) {
			openCount = type;

			if (type == 0) {
				animationStatus = TileEntityShulkerBox.AnimationStatus.CLOSING;
			}

			if (type == 1) {
				animationStatus = TileEntityShulkerBox.AnimationStatus.OPENING;
			}

			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	public void openInventory(EntityPlayer player) {

		if (!player.isSpectator()) {
			if (openCount < 0) {
				openCount = 0;
			}

			++openCount;
			world.addBlockEvent(pos, getBlockType(), 1, openCount);

			if (openCount == 1) {
				world.playSound(null, pos, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	public void closeInventory(EntityPlayer player) {

		if (!player.isSpectator()) {
			--openCount;
			world.addBlockEvent(pos, getBlockType(), 1, openCount);

			if (openCount <= 0) {
				world.playSound(null, pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {

		return new ContainerShulkerBox(playerInventory, this, playerIn);
	}

	public String guiID() {

		return "minecraft:shulker_box";
	}

	/**
	 * Get the name of this object. For players this returns their username
	 */
	public String getName() {

		return hasCustomName() ? customName : "container.shulkerBox";
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);
		loadFromNbt(compound);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		return saveToNbt(compound);
	}

	public void loadFromNbt(NBTTagCompound compound) {

		items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

		if (!checkLootAndRead(compound) && compound.hasKey("Items", 9)) {
			ItemStackHelper.loadAllItems(compound, items);
		}

		if (compound.hasKey("CustomName", 8)) {
			customName = compound.getString("CustomName");
		}
	}

	public NBTTagCompound saveToNbt(NBTTagCompound compound) {

		if (!checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, items, false);
		}

		if (hasCustomName()) {
			compound.setString("CustomName", customName);
		}

		if (!compound.hasKey("Lock") && isLocked()) {
			getLockCode().toNBT(compound);
		}

		return compound;
	}

	protected NonNullList<ItemStack> getItems() {

		return items;
	}

	public boolean isEmpty() {

		for (ItemStack itemstack : items) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public int[] getSlotsForFace(EnumFacing side) {

		return SLOTS;
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from the given side.
	 */
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {

		return !(Block.getBlockFromItem(itemStackIn.getItem()) instanceof BlockShulkerBox);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from the given side.
	 */
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {

		return true;
	}

	public void clear() {

		hasBeenCleared = true;
		super.clear();
	}

	public boolean isCleared() {

		return hasBeenCleared;
	}

	public float getProgress(float p_190585_1_) {

		return progressOld + (progress - progressOld) * p_190585_1_;
	}

	public EnumDyeColor getColor() {

		if (color == null) {
			color = BlockShulkerBox.getColorFromBlock(getBlockType());
		}

		return color;
	}

	
	public SPacketUpdateTileEntity getUpdatePacket() {

		return new SPacketUpdateTileEntity(pos, 10, getUpdateTag());
	}

	public boolean isDestroyedByCreativePlayer() {

		return destroyedByCreativePlayer;
	}

	public void setDestroyedByCreativePlayer(boolean p_190579_1_) {

		destroyedByCreativePlayer = p_190579_1_;
	}

	public boolean shouldDrop() {

		return !isDestroyedByCreativePlayer() || !isEmpty() || hasCustomName() || lootTable != null;
	}

	public enum AnimationStatus {
		CLOSED,
		OPENING,
		OPENED,
		CLOSING
	}

}
