package net.minecraft.item;

import com.google.common.collect.Maps;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;

public class ItemRecord extends Item {

	private static final Map<SoundEvent, ItemRecord> RECORDS = Maps.newHashMap();
	private final SoundEvent sound;
	private final String displayName;

	protected ItemRecord(String p_i46742_1_, SoundEvent soundIn) {

		displayName = "item.record." + p_i46742_1_ + ".desc";
		sound = soundIn;
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.MISC);
		RECORDS.put(sound, this);
	}

	
	public static ItemRecord getBySound(SoundEvent soundIn) {

		return RECORDS.get(soundIn);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() == Blocks.JUKEBOX && !iblockstate.getValue(BlockJukebox.HAS_RECORD)) {
			if (!worldIn.isRemote) {
				ItemStack itemstack = player.getHeldItem(hand);
				((BlockJukebox) Blocks.JUKEBOX).insertRecord(worldIn, pos, iblockstate, itemstack);
				worldIn.playEvent(null, 1010, pos, Item.getIdFromItem(this));
				itemstack.shrink(1);
				player.addStat(StatList.RECORD_PLAYED);
			}

			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		tooltip.add(getRecordNameLocal());
	}

	public String getRecordNameLocal() {

		return I18n.translateToLocal(displayName);
	}

	/**
	 * Return an item rarity from Rarity
	 */
	public Rarity getRarity(ItemStack stack) {

		return Rarity.RARE;
	}

	public SoundEvent getSound() {

		return sound;
	}

}
