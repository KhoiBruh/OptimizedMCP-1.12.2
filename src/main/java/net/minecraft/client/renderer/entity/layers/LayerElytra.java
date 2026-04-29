package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerElytra implements LayerRenderer<EntityLivingBase> {

	/**
	 * The basic Elytra texture.
	 */
	private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
	protected final RenderLivingBase<?> renderPlayer;

	/**
	 * The model used by the Elytra.
	 */
	private final ModelElytra modelElytra = new ModelElytra();

	public LayerElytra(RenderLivingBase<?> p_i47185_1_) {

		renderPlayer = p_i47185_1_;
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

		if (itemstack.getItem() == Items.ELYTRA) {
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			if (entitylivingbaseIn instanceof AbstractClientPlayer abstractclientplayer) {

				if (abstractclientplayer.isPlayerInfoSet() && abstractclientplayer.getLocationElytra() != null) {
					renderPlayer.bindTexture(abstractclientplayer.getLocationElytra());
				} else if (abstractclientplayer.hasPlayerInfo() && abstractclientplayer.getLocationCape() != null && abstractclientplayer.isWearing(PlayerModelParts.CAPE)) {
					renderPlayer.bindTexture(abstractclientplayer.getLocationCape());
				} else {
					renderPlayer.bindTexture(TEXTURE_ELYTRA);
				}
			} else {
				renderPlayer.bindTexture(TEXTURE_ELYTRA);
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, 0F, 0.125F);
			modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
			modelElytra.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			if (itemstack.isItemEnchanted()) {
				LayerArmorBase.renderEnchantedGlint(renderPlayer, entitylivingbaseIn, modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	public boolean shouldCombineTextures() {

		return false;
	}

}
