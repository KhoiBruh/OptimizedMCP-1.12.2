package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapData;

public class RenderItemFrame extends Render<EntityItemFrame> {

	private static final ResourceLocation MAP_BACKGROUND_TEXTURES = new ResourceLocation("textures/map/map_background.png");
	private final Minecraft mc = Minecraft.getMinecraft();
	private final ModelResourceLocation itemFrameModel = new ModelResourceLocation("item_frame", "normal");
	private final ModelResourceLocation mapModel = new ModelResourceLocation("item_frame", "map");
	private final RenderItem itemRenderer;

	public RenderItemFrame(RenderManager renderManagerIn, RenderItem itemRendererIn) {
		super(renderManagerIn);
		itemRenderer = itemRendererIn;
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	public void doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GLS.pushMatrix();
		BlockPos blockpos = entity.getHangingPosition();
		double d0 = (double) blockpos.getX() - entity.posX + x;
		double d1 = (double) blockpos.getY() - entity.posY + y;
		double d2 = (double) blockpos.getZ() - entity.posZ + z;
		GLS.translate(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D);
		GLS.rotate(180F - entity.rotationYaw, 0F, 1F, 0F);
		renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
		ModelManager modelmanager = blockrendererdispatcher.getBlockModelShapes().getModelManager();
		IBakedModel ibakedmodel;

		if (entity.getDisplayedItem().getItem() == Items.FILLED_MAP) {
			ibakedmodel = modelmanager.getModel(mapModel);
		} else {
			ibakedmodel = modelmanager.getModel(itemFrameModel);
		}

		GLS.pushMatrix();
		GLS.translate(-0.5F, -0.5F, -0.5F);

		if (renderOutlines) {
			GLS.enableColorMaterial();
			GLS.enableOutlineMode(getTeamColor(entity));
		}

		blockrendererdispatcher.getBlockModelRenderer().renderModelBrightnessColor(ibakedmodel, 1F, 1F, 1F, 1F);

		if (renderOutlines) {
			GLS.disableOutlineMode();
			GLS.disableColorMaterial();
		}

		GLS.popMatrix();
		GLS.translate(0F, 0F, 0.4375F);
		renderItem(entity);
		GLS.popMatrix();
		renderName(entity, x + (double) ((float) entity.facingDirection.getFrontOffsetX() * 0.3F), y - 0.25D, z + (double) ((float) entity.facingDirection.getFrontOffsetZ() * 0.3F));
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityItemFrame entity) {
		return null;
	}

	private void renderItem(EntityItemFrame itemFrame) {
		ItemStack itemstack = itemFrame.getDisplayedItem();

		if (!itemstack.isEmpty()) {
			GLS.pushMatrix();
			GLS.disableLighting();
			boolean flag = itemstack.getItem() == Items.FILLED_MAP;
			int i = flag ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			GLS.rotate((float) i * 360F / 8F, 0F, 0F, 1F);

			if (flag) {
				renderManager.renderEngine.bindTexture(MAP_BACKGROUND_TEXTURES);
				GLS.rotate(180F, 0F, 0F, 1F);
				float f = 0.0078125F;
				GLS.scale(0.0078125F, 0.0078125F, 0.0078125F);
				GLS.translate(-64F, -64F, 0F);
				MapData mapdata = Items.FILLED_MAP.getMapData(itemstack, itemFrame.world);
				GLS.translate(0F, 0F, -1F);

				if (mapdata != null) {
					mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, true);
				}
			} else {
				GLS.scale(0.5F, 0.5F, 0.5F);
				GLS.pushAttrib();
				RenderHelper.enableStandardItemLighting();
				itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
				RenderHelper.disableStandardItemLighting();
				GLS.popAttrib();
			}

			GLS.enableLighting();
			GLS.popMatrix();
		}
	}

	protected void renderName(EntityItemFrame entity, double x, double y, double z) {
		if (Minecraft.isGuiEnabled() && !entity.getDisplayedItem().isEmpty() && entity.getDisplayedItem()
		                                                                              .hasDisplayName() && renderManager.pointedEntity == entity) {
			double d0 = entity.getDistanceSq(renderManager.renderViewEntity);
			float f = entity.isSneaking() ? 32F : 64F;

			if (d0 < (double) (f * f)) {
				String s = entity.getDisplayedItem().getDisplayName();
				renderLivingLabel(entity, s, x, y, z, 64);
			}
		}
	}

}
