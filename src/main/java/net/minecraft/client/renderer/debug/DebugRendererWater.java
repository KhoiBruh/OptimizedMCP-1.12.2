package net.minecraft.client.renderer.debug;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugRendererWater implements DebugRenderer.IDebugRenderer {

	private final Minecraft minecraft;

	public DebugRendererWater(Minecraft minecraftIn) {
		minecraft = minecraftIn;
	}

	public void render(float partialTicks, long finishTimeNano) {
		EntityPlayer player = minecraft.player;
		double xo = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double yo = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double zo = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
		BlockPos blockpos = minecraft.player.getPosition();
		World world = minecraft.player.world;
		GLS.enableBlend();
		GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA, GLS.SourceFactor.ONE, GLS.DestFactor.ZERO);
		GLS.color(0F, 1F, 0F, 0.75F);
		GLS.disableTexture2D();
		GLS.lineWidth(6F);

		for (BlockPos blockpos1 : BlockPos.getAllInBox(blockpos.add(-10, -10, -10), blockpos.add(10, 10, 10))) {
			IBlockState iblockstate = world.getBlockState(blockpos1);

			if (iblockstate.getBlock() == Blocks.WATER || iblockstate.getBlock() == Blocks.FLOWING_WATER) {
				double d0 = BlockLiquid.getLiquidHeight(iblockstate, world, blockpos1);
				RenderGlobal.renderFilledBox((new AxisAlignedBB((float) blockpos1.getX() + 0.01F, (float) blockpos1.getY() + 0.01F, (float) blockpos1.getZ() + 0.01F, (float) blockpos1.getX() + 0.99F, d0, (float) blockpos1.getZ() + 0.99F)).offset(-xo, -yo, -zo), 1F, 1F, 1F, 0.2F);
			}
		}

		for (BlockPos blockpos2 : BlockPos.getAllInBox(blockpos.add(-10, -10, -10), blockpos.add(10, 10, 10))) {
			IBlockState iblockstate1 = world.getBlockState(blockpos2);

			if (iblockstate1.getBlock() == Blocks.WATER || iblockstate1.getBlock() == Blocks.FLOWING_WATER) {
				Integer integer = iblockstate1.getValue(BlockLiquid.LEVEL);
				double d1 = integer > 7 ? 0.9D : 1D - 0.11D * (double) integer;
				String s = iblockstate1.getBlock() == Blocks.FLOWING_WATER ? "f" : "s";
				DebugRenderer.renderDebugText(s + " " + integer, (double) blockpos2.getX() + 0.5D, (double) blockpos2.getY() + d1, (double) blockpos2.getZ() + 0.5D, partialTicks, -16777216);
			}
		}

		GLS.enableTexture2D();
		GLS.disableBlend();
	}

}
