package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import java.util.List;

public class ItemBanner extends ItemBlock {

	public ItemBanner() {

		super(Blocks.STANDING_BANNER);
		maxStackSize = 16;
		setCreativeTab(CreativeTabs.DECORATIONS);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	public static void appendHoverTextFromTileEntityTag(ItemStack stack, List<String> p_185054_1_) {

		NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag");

		if (nbttagcompound != null && nbttagcompound.hasKey("Patterns")) {
			NBTTagList nbttaglist = nbttagcompound.getTagList("Patterns", 10);

			for (int i = 0; i < nbttaglist.tagCount() && i < 6; ++i) {
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				DyeColor enumdyecolor = DyeColor.byDyeDamage(nbttagcompound1.getInteger("Color"));
				BannerPattern bannerpattern = BannerPattern.byHash(nbttagcompound1.getString("Pattern"));

				if (bannerpattern != null) {
					p_185054_1_.add(I18n.translateToLocal("item.banner." + bannerpattern.getFileName() + "." + enumdyecolor.getUnlocalizedName()));
				}
			}
		}
	}

	public static ItemStack makeBanner(DyeColor color, NBTTagList patterns) {

		ItemStack itemstack = new ItemStack(Items.BANNER, 1, color.getDyeDamage());

		if (patterns != null && !patterns.hasNoTags()) {
			itemstack.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", patterns.copy());
		}

		return itemstack;
	}

	public static DyeColor getBaseColor(ItemStack stack) {

		return DyeColor.byDyeDamage(stack.getMetadata() & 15);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		IBlockState iblockstate = worldIn.getBlockState(pos);
		boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);

		if (facing != Facing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == Facing.UP)) {
			pos = pos.offset(facing);
			ItemStack itemstack = player.getHeldItem(hand);

			if (player.canPlayerEdit(pos, facing, itemstack) && Blocks.STANDING_BANNER.canPlaceBlockAt(worldIn, pos)) {
				if (worldIn.isRemote) {
					return ActionResult.SUCCESS;
				} else {
					pos = flag ? pos.down() : pos;

					if (facing == Facing.UP) {
						int i = MathHelper.floor((double) ((player.rotationYaw + 180F) * 16F / 360F) + 0.5D) & 15;
						worldIn.setBlockState(pos, Blocks.STANDING_BANNER.getDefaultState().withProperty(BlockStandingSign.ROTATION, i), 3);
					} else {
						worldIn.setBlockState(pos, Blocks.WALL_BANNER.getDefaultState().withProperty(BlockWallSign.FACING, facing), 3);
					}

					TileEntity tileentity = worldIn.getTileEntity(pos);

					if (tileentity instanceof TileEntityBanner) {
						((TileEntityBanner) tileentity).setItemValues(itemstack, false);
					}

					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
					}

					itemstack.shrink(1);
					return ActionResult.SUCCESS;
				}
			} else {
				return ActionResult.FAIL;
			}
		} else {
			return ActionResult.FAIL;
		}
	}

	public String getItemStackDisplayName(ItemStack stack) {

		String s = "item.banner.";
		DyeColor enumdyecolor = getBaseColor(stack);
		s = s + enumdyecolor.getUnlocalizedName() + ".name";
		return I18n.translateToLocal(s);
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		appendHoverTextFromTileEntityTag(stack, tooltip);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

		if (isInCreativeTab(tab)) {
			for (DyeColor enumdyecolor : DyeColor.values()) {
				items.add(makeBanner(enumdyecolor, null));
			}
		}
	}

	/**
	 * gets the CreativeTab this item is displayed on
	 */
	public CreativeTabs getCreativeTab() {

		return CreativeTabs.DECORATIONS;
	}

}
