package net.minecraft.tileentity;

import net.minecraft.block.BlockBed;
import net.minecraft.init.Items;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public class TileEntityBed extends TileEntity {

	private DyeColor color = DyeColor.RED;

	public void setItemValues(ItemStack p_193051_1_) {

		setColor(DyeColor.byMetadata(p_193051_1_.getMetadata()));
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);

		if (compound.hasKey("color")) {
			color = DyeColor.byMetadata(compound.getInteger("color"));
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		compound.setInteger("color", color.getMetadata());
		return compound;
	}

	public NBTTagCompound getUpdateTag() {

		return writeToNBT(new NBTTagCompound());
	}

	public SPacketUpdateTileEntity getUpdatePacket() {

		return new SPacketUpdateTileEntity(pos, 11, getUpdateTag());
	}

	public DyeColor getColor() {

		return color;
	}

	public void setColor(DyeColor color) {

		this.color = color;
		markDirty();
	}

	public boolean isHeadPiece() {

		return BlockBed.isHeadPiece(getBlockMetadata());
	}

	public ItemStack getItemStack() {

		return new ItemStack(Items.BED, 1, color.getMetadata());
	}

}
