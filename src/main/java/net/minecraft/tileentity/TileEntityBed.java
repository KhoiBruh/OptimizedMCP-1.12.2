package net.minecraft.tileentity;

import net.minecraft.block.BlockBed;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public class TileEntityBed extends TileEntity {

	private EnumDyeColor color = EnumDyeColor.RED;

	public void setItemValues(ItemStack p_193051_1_) {

		setColor(EnumDyeColor.byMetadata(p_193051_1_.getMetadata()));
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);

		if (compound.hasKey("color")) {
			color = EnumDyeColor.byMetadata(compound.getInteger("color"));
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

	public EnumDyeColor getColor() {

		return color;
	}

	public void setColor(EnumDyeColor color) {

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
