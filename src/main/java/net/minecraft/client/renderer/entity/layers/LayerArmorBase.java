package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public abstract class LayerArmorBase<T extends ModelBase> implements LayerRenderer<EntityLivingBase> {

	protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	protected T modelLeggings;
	protected T modelArmor;
	private final RenderLivingBase<?> renderer;
	private final float alpha = 1.0F;
	private final float colorR = 1.0F;
	private final float colorG = 1.0F;
	private final float colorB = 1.0F;
	private boolean skipRenderGlint;
	private static final Map<String, ResourceLocation> ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();

	public LayerArmorBase(RenderLivingBase<?> rendererIn) {

		renderer = rendererIn;
		initArmor();
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
		renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
	}

	public boolean shouldCombineTextures() {

		return false;
	}

	private void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {

		ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slotIn);

		if (itemstack.getItem() instanceof ItemArmor itemarmor) {

			if (itemarmor.getEquipmentSlot() == slotIn) {
				T t = getModelFromSlot(slotIn);
				t.setModelAttributes(renderer.getMainModel());
				t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
				setModelSlotVisible(t, slotIn);
				boolean flag = isLegSlot(slotIn);
				renderer.bindTexture(getArmorResource(itemarmor, flag));

				switch (itemarmor.getArmorMaterial()) {
					case LEATHER:
						int i = itemarmor.getColor(itemstack);
						float f = (float) (i >> 16 & 255) / 255.0F;
						float f1 = (float) (i >> 8 & 255) / 255.0F;
						float f2 = (float) (i & 255) / 255.0F;
						GlStateManager.color(colorR * f, colorG * f1, colorB * f2, alpha);
						t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
						renderer.bindTexture(getArmorResource(itemarmor, flag, "overlay"));

					case CHAIN:
					case IRON:
					case GOLD:
					case DIAMOND:
						GlStateManager.color(colorR, colorG, colorB, alpha);
						t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

					default:
						if (!skipRenderGlint && itemstack.isItemEnchanted()) {
							renderEnchantedGlint(renderer, entityLivingBaseIn, t, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
						}
				}
			}
		}
	}

	public T getModelFromSlot(EntityEquipmentSlot slotIn) {

		return isLegSlot(slotIn) ? modelLeggings : modelArmor;
	}

	private boolean isLegSlot(EntityEquipmentSlot slotIn) {

		return slotIn == EntityEquipmentSlot.LEGS;
	}

	public static void renderEnchantedGlint(RenderLivingBase<?> p_188364_0_, EntityLivingBase p_188364_1_, ModelBase model, float p_188364_3_, float p_188364_4_, float p_188364_5_, float p_188364_6_, float p_188364_7_, float p_188364_8_, float p_188364_9_) {

		float f = (float) p_188364_1_.ticksExisted + p_188364_5_;
		p_188364_0_.bindTexture(ENCHANTED_ITEM_GLINT_RES);
		Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(false);
		float f1 = 0.5F;
		GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
			float f2 = 0.76F;
			GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			float f3 = 0.33333334F;
			GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
			GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			model.render(p_188364_1_, p_188364_3_, p_188364_4_, p_188364_6_, p_188364_7_, p_188364_8_, p_188364_9_);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}

		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
		Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
	}

	private ResourceLocation getArmorResource(ItemArmor armor, boolean p_177181_2_) {

		return getArmorResource(armor, p_177181_2_, null);
	}

	private ResourceLocation getArmorResource(ItemArmor armor, boolean p_177178_2_, String p_177178_3_) {

		String s = String.format("textures/models/armor/%s_layer_%d%s.png", armor.getArmorMaterial().getName(), p_177178_2_ ? 2 : 1, p_177178_3_ == null ? "" : String.format("_%s", p_177178_3_));
		ResourceLocation resourcelocation = ARMOR_TEXTURE_RES_MAP.get(s);

		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s);
			ARMOR_TEXTURE_RES_MAP.put(s, resourcelocation);
		}

		return resourcelocation;
	}

	protected abstract void initArmor();

	protected abstract void setModelSlotVisible(T p_188359_1_, EntityEquipmentSlot slotIn);

}
