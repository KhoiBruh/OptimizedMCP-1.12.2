package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class DebugRendererCollisionBox implements DebugRenderer.IDebugRenderer {

	private final Minecraft minecraft;

	public DebugRendererCollisionBox(Minecraft minecraftIn) {
		minecraft = minecraftIn;
	}

	public void render(float partialTicks, long finishTimeNano) {
		EntityPlayer player = minecraft.player;
		double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
		World world = minecraft.player.world;
		List<AxisAlignedBB> list = world.getCollisionBoxes(player, player.getEntityBoundingBox().grow(6D));
		GLS.enableBlend();
		GLS.blendFunc(GLS.SourceFactor.SRC_ALPHA, GLS.DestFactor.ONE_MINUS_SRC_ALPHA, GLS.SourceFactor.ONE, GLS.DestFactor.ZERO);
		GLS.lineWidth(2F);
		GLS.disableTexture2D();
		GLS.depthMask(false);

		for (AxisAlignedBB axisalignedbb : list) {
			RenderGlobal.drawSelectionBoundingBox(axisalignedbb.grow(0.002D)
			                                                   .offset(-renderPosX, -renderPosY, -renderPosZ), 1F, 1F, 1F, 1F);
		}

		GLS.depthMask(true);
		GLS.enableTexture2D();
		GLS.disableBlend();
	}

}
