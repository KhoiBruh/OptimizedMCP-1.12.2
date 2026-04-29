package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Facing;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemHangingEntity extends Item {

	private final Class<? extends EntityHanging> hangingEntityClass;

	public ItemHangingEntity(Class<? extends EntityHanging> entityClass) {

		hangingEntityClass = entityClass;
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public ActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, Hand hand, Facing facing, float hitX, float hitY, float hitZ) {

		ItemStack itemstack = player.getHeldItem(hand);
		BlockPos blockpos = pos.offset(facing);

		if (facing != Facing.DOWN && facing != Facing.UP && player.canPlayerEdit(blockpos, facing, itemstack)) {
			EntityHanging entityhanging = createEntity(worldIn, blockpos, facing);

			if (entityhanging != null && entityhanging.onValidSurface()) {
				if (!worldIn.isRemote) {
					entityhanging.playPlaceSound();
					worldIn.spawnEntity(entityhanging);
				}

				itemstack.shrink(1);
			}

			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

	
	private EntityHanging createEntity(World worldIn, BlockPos pos, Facing clickedSide) {

		if (hangingEntityClass == EntityPainting.class) {
			return new EntityPainting(worldIn, pos, clickedSide);
		} else {
			return hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(worldIn, pos, clickedSide) : null;
		}
	}

}
