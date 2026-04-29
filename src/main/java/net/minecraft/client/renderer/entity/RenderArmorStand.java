package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelArmorStand;
import net.minecraft.client.model.ModelArmorStandArmor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderArmorStand extends RenderLivingBase<EntityArmorStand> {

	/**
	 * A constant instance of the armor stand texture, wrapped inside a ResourceLocation wrapper.
	 */
	public static final ResourceLocation TEXTURE_ARMOR_STAND = new ResourceLocation("textures/entity/armorstand/wood.png");

	public RenderArmorStand(RenderManager manager) {

		super(manager, new ModelArmorStand(), 0F);
		LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
			protected void initArmor() {

				modelLeggings = new ModelArmorStandArmor(0.5F);
				modelArmor = new ModelArmorStandArmor(1F);
			}
		};
		addLayer(layerbipedarmor);
		addLayer(new LayerHeldItem(this));
		addLayer(new LayerElytra(this));
		addLayer(new LayerCustomHead(getMainModel().bipedHead));
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityArmorStand entity) {

		return TEXTURE_ARMOR_STAND;
	}

	public ModelArmorStand getMainModel() {

		return (ModelArmorStand) super.getMainModel();
	}

	protected void applyRotations(EntityArmorStand entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {

		GlStateManager.rotate(180F - rotationYaw, 0F, 1F, 0F);
		float f = (float) (entityLiving.world.getTotalWorldTime() - entityLiving.punchCooldown) + partialTicks;

		if (f < 5F) {
			GlStateManager.rotate(MathHelper.sin(f / 1.5F * (float) Math.PI) * 3F, 0F, 1F, 0F);
		}
	}

	protected boolean canRenderName(EntityArmorStand entity) {

		return entity.getAlwaysRenderNameTag();
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityArmorStand entity, double x, double y, double z, float entityYaw, float partialTicks) {

		if (entity.hasMarker()) {
			renderMarker = true;
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		if (entity.hasMarker()) {
			renderMarker = false;
		}
	}

}
