package net.minecraft.server.management;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class DemoPlayerInteractionManager extends PlayerInteractionManager {

	private boolean displayedIntro;
	private boolean demoTimeExpired;
	private int demoEndedReminder;
	private int gameModeTicks;

	public DemoPlayerInteractionManager(World worldIn) {

		super(worldIn);
	}

	public void updateBlockRemoving() {

		super.updateBlockRemoving();
		++gameModeTicks;
		long i = world.getTotalWorldTime();
		long j = i / 24000L + 1L;

		if (!displayedIntro && gameModeTicks > 20) {
			displayedIntro = true;
			player.connection.sendPacket(new SPacketChangeGameState(5, 0.0F));
		}

		demoTimeExpired = i > 120500L;

		if (demoTimeExpired) {
			++demoEndedReminder;
		}

		if (i % 24000L == 500L) {
			if (j <= 6L) {
				player.sendMessage(new TextComponentTranslation("demo.day." + j));
			}
		} else if (j == 1L) {
			if (i == 100L) {
				player.connection.sendPacket(new SPacketChangeGameState(5, 101.0F));
			} else if (i == 175L) {
				player.connection.sendPacket(new SPacketChangeGameState(5, 102.0F));
			} else if (i == 250L) {
				player.connection.sendPacket(new SPacketChangeGameState(5, 103.0F));
			}
		} else if (j == 5L && i % 24000L == 22000L) {
			player.sendMessage(new TextComponentTranslation("demo.day.warning"));
		}
	}

	/**
	 * Sends a message to the player reminding them that this is the demo version
	 */
	private void sendDemoReminder() {

		if (demoEndedReminder > 100) {
			player.sendMessage(new TextComponentTranslation("demo.reminder"));
			demoEndedReminder = 0;
		}
	}

	/**
	 * If not creative, it calls sendBlockBreakProgress until the block is broken first. tryHarvestBlock can also be the
	 * result of this call.
	 */
	public void onBlockClicked(BlockPos pos, EnumFacing side) {

		if (demoTimeExpired) {
			sendDemoReminder();
		} else {
			super.onBlockClicked(pos, side);
		}
	}

	public void blockRemoving(BlockPos pos) {

		if (!demoTimeExpired) {
			super.blockRemoving(pos);
		}
	}

	/**
	 * Attempts to harvest a block
	 */
	public boolean tryHarvestBlock(BlockPos pos) {

		return !demoTimeExpired && super.tryHarvestBlock(pos);
	}

	public EnumActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand) {

		if (demoTimeExpired) {
			sendDemoReminder();
			return EnumActionResult.PASS;
		} else {
			return super.processRightClick(player, worldIn, stack, hand);
		}
	}

	public EnumActionResult processRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (demoTimeExpired) {
			sendDemoReminder();
			return EnumActionResult.PASS;
		} else {
			return super.processRightClickBlock(player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
		}
	}

}
