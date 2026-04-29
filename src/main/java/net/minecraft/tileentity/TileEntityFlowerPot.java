package net.minecraft.tileentity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;

public class TileEntityFlowerPot extends TileEntity {

	private Item flowerPotItem;
	private int flowerPotData;

	public TileEntityFlowerPot() {

	}

	public TileEntityFlowerPot(Item potItem, int potData) {

		flowerPotItem = potItem;
		flowerPotData = potData;
	}

	public static void registerFixesFlowerPot(DataFixer fixer) {

	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		super.writeToNBT(compound);
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(flowerPotItem);
		compound.setString("Item", resourcelocation == null ? "" : resourcelocation.toString());
		compound.setInteger("Data", flowerPotData);
		return compound;
	}

	public void readFromNBT(NBTTagCompound compound) {

		super.readFromNBT(compound);

		if (compound.hasKey("Item", 8)) {
			flowerPotItem = Item.getByNameOrId(compound.getString("Item"));
		} else {
			flowerPotItem = Item.getItemById(compound.getInteger("Item"));
		}

		flowerPotData = compound.getInteger("Data");
	}

	
	public SPacketUpdateTileEntity getUpdatePacket() {

		return new SPacketUpdateTileEntity(pos, 5, getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {

		return writeToNBT(new NBTTagCompound());
	}

	public void setItemStack(ItemStack stack) {

		flowerPotItem = stack.getItem();
		flowerPotData = stack.getMetadata();
	}

	public ItemStack getFlowerItemStack() {

		return flowerPotItem == null ? ItemStack.EMPTY : new ItemStack(flowerPotItem, 1, flowerPotData);
	}

	
	public Item getFlowerPotItem() {

		return flowerPotItem;
	}

	public int getFlowerPotData() {

		return flowerPotData;
	}

}
