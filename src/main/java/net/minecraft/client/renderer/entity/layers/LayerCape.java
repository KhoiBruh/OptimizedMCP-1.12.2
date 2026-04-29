package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class LayerCape implements LayerRenderer<AbstractClientPlayer> {

	private final RenderPlayer playerRenderer;

	public LayerCape(RenderPlayer playerRendererIn) {

		playerRenderer = playerRendererIn;
	}

	public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) && entitylivingbaseIn.getLocationCape() != null) {
			ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

			if (itemstack.getItem() != Items.ELYTRA) {
				GlStateManager.color(1F, 1F, 1F, 1F);
				playerRenderer.bindTexture(entitylivingbaseIn.getLocationCape());
				GlStateManager.pushMatrix();
				GlStateManager.translate(0F, 0F, 0.125F);
				double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double) partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double) partialTicks);
				double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double) partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double) partialTicks);
				double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double) partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double) partialTicks);
				float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
				double d3 = MathHelper.sin(f * 0.017453292F);
				double d4 = -MathHelper.cos(f * 0.017453292F);
				float f1 = (float) d1 * 10F;
				f1 = MathHelper.clamp(f1, -6F, 32F);
				float f2 = (float) (d0 * d3 + d2 * d4) * 100F;
				float f3 = (float) (d0 * d4 - d2 * d3) * 100F;

				if (f2 < 0F) {
					f2 = 0F;
				}

				float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
				f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6F) * 32F * f4;

				if (entitylivingbaseIn.isSneaking()) {
					f1 += 25F;
				}

				GlStateManager.rotate(6F + f2 / 2F + f1, 1F, 0F, 0F);
				GlStateManager.rotate(f3 / 2F, 0F, 0F, 1F);
				GlStateManager.rotate(-f3 / 2F, 0F, 1F, 0F);
				GlStateManager.rotate(180F, 0F, 1F, 0F);
				playerRenderer.getMainModel().renderCape(0.0625F);
				GlStateManager.popMatrix();
			}
		}
	}

	public boolean shouldCombineTextures() {

		return false;
	}

}
