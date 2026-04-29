package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.BlockRenderType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.storage.MapData;

import java.util.Objects;

public class ItemRenderer {

	private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
	private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

	/**
	 * A reference to the Minecraft object.
	 */
	private final Minecraft mc;
	private final RenderManager renderManager;
	private final RenderItem itemRenderer;
	private ItemStack itemStackMainHand = ItemStack.EMPTY;
	private ItemStack itemStackOffHand = ItemStack.EMPTY;
	private float equippedProgressMainHand;
	private float prevEquippedProgressMainHand;
	private float equippedProgressOffHand;
	private float prevEquippedProgressOffHand;

	public ItemRenderer(Minecraft mcIn) {

		mc = mcIn;
		renderManager = mcIn.getRenderManager();
		itemRenderer = mcIn.getRenderItem();
	}

	public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {

		renderItemSide(entityIn, heldStack, transform, false);
	}

	public void renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {

		if (!heldStack.isEmpty()) {
			Item item = heldStack.getItem();
			Block block = Block.getBlockFromItem(item);
			GlStateManager.pushMatrix();
			boolean flag = itemRenderer.shouldRenderItemIn3D(heldStack) && block.getBlockLayer() == BlockRenderLayer.TRANSLUCENT;

			if (flag) {
				GlStateManager.depthMask(false);
			}

			itemRenderer.renderItem(heldStack, entitylivingbaseIn, transform, leftHanded);

			if (flag) {
				GlStateManager.depthMask(true);
			}

			GlStateManager.popMatrix();
		}
	}

	/**
	 * Rotate the render around X and Y
	 */
	private void rotateArroundXAndY(float angle, float angleY) {

		GlStateManager.pushMatrix();
		GlStateManager.rotate(angle, 1F, 0F, 0F);
		GlStateManager.rotate(angleY, 0F, 1F, 0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	private void setLightmap() {

		AbstractClientPlayer abstractclientplayer = mc.player;
		int i = mc.world.getCombinedLight(new BlockPos(abstractclientplayer.posX, abstractclientplayer.posY + (double) abstractclientplayer.getEyeHeight(), abstractclientplayer.posZ), 0);
		float f = (float) (i & 65535);
		float f1 = (float) (i >> 16);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
	}

	private void rotateArm(float p_187458_1_) {

		EntityPlayerSP entityplayersp = mc.player;
		float f = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * p_187458_1_;
		float f1 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * p_187458_1_;
		GlStateManager.rotate((entityplayersp.rotationPitch - f) * 0.1F, 1F, 0F, 0F);
		GlStateManager.rotate((entityplayersp.rotationYaw - f1) * 0.1F, 0F, 1F, 0F);
	}

	/**
	 * Return the angle to render the Map
	 */
	private float getMapAngleFromPitch(float pitch) {

		float f = 1F - pitch / 45F + 0.1F;
		f = MathHelper.clamp(f, 0F, 1F);
		f = -MathHelper.cos(f * (float) Math.PI) * 0.5F + 0.5F;
		return f;
	}

	private void renderArms() {

		if (!mc.player.isInvisible()) {
			GlStateManager.disableCull();
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90F, 0F, 1F, 0F);
			renderArm(HandSide.RIGHT);
			renderArm(HandSide.LEFT);
			GlStateManager.popMatrix();
			GlStateManager.enableCull();
		}
	}

	private void renderArm(HandSide p_187455_1_) {

		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		Render<AbstractClientPlayer> render = renderManager.getEntityRenderObject(mc.player);
		RenderPlayer renderplayer = (RenderPlayer) render;
		GlStateManager.pushMatrix();
		float f = p_187455_1_ == HandSide.RIGHT ? 1F : -1F;
		GlStateManager.rotate(92F, 0F, 1F, 0F);
		GlStateManager.rotate(45F, 1F, 0F, 0F);
		GlStateManager.rotate(f * -41F, 0F, 0F, 1F);
		GlStateManager.translate(f * 0.3F, -1.1F, 0.45F);

		if (p_187455_1_ == HandSide.RIGHT) {
			renderplayer.renderRightArm(mc.player);
		} else {
			renderplayer.renderLeftArm(mc.player);
		}

		GlStateManager.popMatrix();
	}

	private void renderMapFirstPersonSide(float p_187465_1_, HandSide hand, float p_187465_3_, ItemStack stack) {

		float f = hand == HandSide.RIGHT ? 1F : -1F;
		GlStateManager.translate(f * 0.125F, -0.125F, 0F);

		if (!mc.player.isInvisible()) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(f * 10F, 0F, 0F, 1F);
			renderArmFirstPerson(p_187465_1_, p_187465_3_, hand);
			GlStateManager.popMatrix();
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(f * 0.51F, -0.08F + p_187465_1_ * -1.2F, -0.75F);
		float f1 = MathHelper.sqrt(p_187465_3_);
		float f2 = MathHelper.sin(f1 * (float) Math.PI);
		float f3 = -0.5F * f2;
		float f4 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
		float f5 = -0.3F * MathHelper.sin(p_187465_3_ * (float) Math.PI);
		GlStateManager.translate(f * f3, f4 - 0.3F * f2, f5);
		GlStateManager.rotate(f2 * -45F, 1F, 0F, 0F);
		GlStateManager.rotate(f * f2 * -30F, 0F, 1F, 0F);
		renderMapFirstPerson(stack);
		GlStateManager.popMatrix();
	}

	private void renderMapFirstPerson(float p_187463_1_, float p_187463_2_, float p_187463_3_) {

		float f = MathHelper.sqrt(p_187463_3_);
		float f1 = -0.2F * MathHelper.sin(p_187463_3_ * (float) Math.PI);
		float f2 = -0.4F * MathHelper.sin(f * (float) Math.PI);
		GlStateManager.translate(0F, -f1 / 2F, f2);
		float f3 = getMapAngleFromPitch(p_187463_1_);
		GlStateManager.translate(0F, 0.04F + p_187463_2_ * -1.2F + f3 * -0.5F, -0.72F);
		GlStateManager.rotate(f3 * -85F, 1F, 0F, 0F);
		renderArms();
		float f4 = MathHelper.sin(f * (float) Math.PI);
		GlStateManager.rotate(f4 * 20F, 1F, 0F, 0F);
		GlStateManager.scale(2F, 2F, 2F);
		renderMapFirstPerson(itemStackMainHand);
	}

	private void renderMapFirstPerson(ItemStack stack) {

		GlStateManager.rotate(180F, 0F, 1F, 0F);
		GlStateManager.rotate(180F, 0F, 0F, 1F);
		GlStateManager.scale(0.38F, 0.38F, 0.38F);
		GlStateManager.disableLighting();
		mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.translate(-0.5F, -0.5F, 0F);
		GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(-7D, 135D, 0D).tex(0D, 1D).endVertex();
		bufferbuilder.pos(135D, 135D, 0D).tex(1D, 1D).endVertex();
		bufferbuilder.pos(135D, -7D, 0D).tex(1D, 0D).endVertex();
		bufferbuilder.pos(-7D, -7D, 0D).tex(0D, 0D).endVertex();
		tessellator.draw();
		MapData mapdata = Items.FILLED_MAP.getMapData(stack, mc.world);

		if (mapdata != null) {
			mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
		}

		GlStateManager.enableLighting();
	}

	private void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, HandSide p_187456_3_) {

		boolean flag = p_187456_3_ != HandSide.LEFT;
		float f = flag ? 1F : -1F;
		float f1 = MathHelper.sqrt(p_187456_2_);
		float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
		float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
		float f4 = -0.4F * MathHelper.sin(p_187456_2_ * (float) Math.PI);
		GlStateManager.translate(f * (f2 + 0.64000005F), f3 - 0.6F + p_187456_1_ * -0.6F, f4 - 0.71999997F);
		GlStateManager.rotate(f * 45F, 0F, 1F, 0F);
		float f5 = MathHelper.sin(p_187456_2_ * p_187456_2_ * (float) Math.PI);
		float f6 = MathHelper.sin(f1 * (float) Math.PI);
		GlStateManager.rotate(f * f6 * 70F, 0F, 1F, 0F);
		GlStateManager.rotate(f * f5 * -20F, 0F, 0F, 1F);
		AbstractClientPlayer abstractclientplayer = mc.player;
		mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
		GlStateManager.translate(f * -1F, 3.6F, 3.5F);
		GlStateManager.rotate(f * 120F, 0F, 0F, 1F);
		GlStateManager.rotate(200F, 1F, 0F, 0F);
		GlStateManager.rotate(f * -135F, 0F, 1F, 0F);
		GlStateManager.translate(f * 5.6F, 0F, 0F);
		RenderPlayer renderplayer = (RenderPlayer) renderManager.<AbstractClientPlayer>getEntityRenderObject(abstractclientplayer);
		GlStateManager.disableCull();

		if (flag) {
			renderplayer.renderRightArm(abstractclientplayer);
		} else {
			renderplayer.renderLeftArm(abstractclientplayer);
		}

		GlStateManager.enableCull();
	}

	private void transformEatFirstPerson(float p_187454_1_, HandSide hand, ItemStack stack) {

		float f = (float) mc.player.getItemInUseCount() - p_187454_1_ + 1F;
		float f1 = f / (float) stack.getMaxItemUseDuration();

		if (f1 < 0.8F) {
			float f2 = MathHelper.abs(MathHelper.cos(f / 4F * (float) Math.PI) * 0.1F);
			GlStateManager.translate(0F, f2, 0F);
		}

		float f3 = 1F - (float) Math.pow(f1, 27D);
		int i = hand == HandSide.RIGHT ? 1 : -1;
		GlStateManager.translate(f3 * 0.6F * (float) i, f3 * -0.5F, f3 * 0F);
		GlStateManager.rotate((float) i * f3 * 90F, 0F, 1F, 0F);
		GlStateManager.rotate(f3 * 10F, 1F, 0F, 0F);
		GlStateManager.rotate((float) i * f3 * 30F, 0F, 0F, 1F);
	}

	private void transformFirstPerson(HandSide hand, float p_187453_2_) {

		int i = hand == HandSide.RIGHT ? 1 : -1;
		float f = MathHelper.sin(p_187453_2_ * p_187453_2_ * (float) Math.PI);
		GlStateManager.rotate((float) i * (45F + f * -20F), 0F, 1F, 0F);
		float f1 = MathHelper.sin(MathHelper.sqrt(p_187453_2_) * (float) Math.PI);
		GlStateManager.rotate((float) i * f1 * -20F, 0F, 0F, 1F);
		GlStateManager.rotate(f1 * -80F, 1F, 0F, 0F);
		GlStateManager.rotate((float) i * -45F, 0F, 1F, 0F);
	}

	private void transformSideFirstPerson(HandSide hand, float p_187459_2_) {

		int i = hand == HandSide.RIGHT ? 1 : -1;
		GlStateManager.translate((float) i * 0.56F, -0.52F + p_187459_2_ * -0.6F, -0.72F);
	}

	/**
	 * Renders the active item in the player's hand when in first person mode.
	 */
	public void renderItemInFirstPerson(float partialTicks) {

		AbstractClientPlayer abstractclientplayer = mc.player;
		float f = abstractclientplayer.getSwingProgress(partialTicks);
		Hand enumhand = MoreObjects.firstNonNull(abstractclientplayer.swingingHand, Hand.MAIN_HAND);
		float f1 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
		float f2 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
		boolean flag = true;
		boolean flag1 = true;

		if (abstractclientplayer.isHandActive()) {
			ItemStack itemstack = abstractclientplayer.getActiveItemStack();

			if (itemstack.getItem() == Items.BOW) {
				Hand enumhand1 = abstractclientplayer.getActiveHand();
				flag = enumhand1 == Hand.MAIN_HAND;
				flag1 = !flag;
			}
		}

		rotateArroundXAndY(f1, f2);
		setLightmap();
		rotateArm(partialTicks);
		GlStateManager.enableRescaleNormal();

		if (flag) {
			float f3 = enumhand == Hand.MAIN_HAND ? f : 0F;
			float f5 = 1F - (prevEquippedProgressMainHand + (equippedProgressMainHand - prevEquippedProgressMainHand) * partialTicks);
			renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, Hand.MAIN_HAND, f3, itemStackMainHand, f5);
		}

		if (flag1) {
			float f4 = enumhand == Hand.OFF_HAND ? f : 0F;
			float f6 = 1F - (prevEquippedProgressOffHand + (equippedProgressOffHand - prevEquippedProgressOffHand) * partialTicks);
			renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, Hand.OFF_HAND, f4, itemStackOffHand, f6);
		}

		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
	}

	public void renderItemInFirstPerson(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, Hand hand, float p_187457_5_, ItemStack stack, float p_187457_7_) {

		boolean flag = hand == Hand.MAIN_HAND;
		HandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
		GlStateManager.pushMatrix();

		if (stack.isEmpty()) {
			if (flag && !player.isInvisible()) {
				renderArmFirstPerson(p_187457_7_, p_187457_5_, enumhandside);
			}
		} else if (stack.getItem() == Items.FILLED_MAP) {
			if (flag && itemStackOffHand.isEmpty()) {
				renderMapFirstPerson(p_187457_3_, p_187457_7_, p_187457_5_);
			} else {
				renderMapFirstPersonSide(p_187457_7_, enumhandside, p_187457_5_, stack);
			}
		} else {
			boolean flag1 = enumhandside == HandSide.RIGHT;

			if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
				int j = flag1 ? 1 : -1;

				switch (stack.getItemUseAction()) {
					case NONE, BLOCK:
						transformSideFirstPerson(enumhandside, p_187457_7_);
						break;

					case EAT:
					case DRINK:
						transformEatFirstPerson(p_187457_2_, enumhandside, stack);
						transformSideFirstPerson(enumhandside, p_187457_7_);
						break;

					case BOW:
						transformSideFirstPerson(enumhandside, p_187457_7_);
						GlStateManager.translate((float) j * -0.2785682F, 0.18344387F, 0.15731531F);
						GlStateManager.rotate(-13.935F, 1F, 0F, 0F);
						GlStateManager.rotate((float) j * 35.3F, 0F, 1F, 0F);
						GlStateManager.rotate((float) j * -9.785F, 0F, 0F, 1F);
						float f5 = (float) stack.getMaxItemUseDuration() - ((float) mc.player.getItemInUseCount() - p_187457_2_ + 1F);
						float f6 = f5 / 20F;
						f6 = (f6 * f6 + f6 * 2F) / 3F;

						if (f6 > 1F) {
							f6 = 1F;
						}

						if (f6 > 0.1F) {
							float f7 = MathHelper.sin((f5 - 0.1F) * 1.3F);
							float f3 = f6 - 0.1F;
							float f4 = f7 * f3;
							GlStateManager.translate(f4 * 0F, f4 * 0.004F, f4 * 0F);
						}

						GlStateManager.translate(f6 * 0F, f6 * 0F, f6 * 0.04F);
						GlStateManager.scale(1F, 1F, 1F + f6 * 0.2F);
						GlStateManager.rotate((float) j * 45F, 0F, -1F, 0F);
				}
			} else {
				float f = -0.4F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * (float) Math.PI);
				float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * ((float) Math.PI * 2F));
				float f2 = -0.2F * MathHelper.sin(p_187457_5_ * (float) Math.PI);
				int i = flag1 ? 1 : -1;
				GlStateManager.translate((float) i * f, f1, f2);
				transformSideFirstPerson(enumhandside, p_187457_7_);
				transformFirstPerson(enumhandside, p_187457_5_);
			}

			renderItemSide(player, stack, flag1 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag1);
		}

		GlStateManager.popMatrix();
	}

	/**
	 * Renders the overlays.
	 */
	public void renderOverlays(float partialTicks) {

		GlStateManager.disableAlpha();

		if (mc.player.isEntityInsideOpaqueBlock()) {
			IBlockState iblockstate = mc.world.getBlockState(new BlockPos(mc.player));
			EntityPlayer entityplayer = mc.player;

			for (int i = 0; i < 8; ++i) {
				double d0 = entityplayer.posX + (double) (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
				double d1 = entityplayer.posY + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
				double d2 = entityplayer.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
				BlockPos blockpos = new BlockPos(d0, d1 + (double) entityplayer.getEyeHeight(), d2);
				IBlockState iblockstate1 = mc.world.getBlockState(blockpos);

				if (iblockstate1.causesSuffocation()) {
					iblockstate = iblockstate1;
				}
			}

			if (iblockstate.getRenderType() != BlockRenderType.INVISIBLE) {
				renderBlockInHand(mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
			}
		}

		if (!mc.player.isSpectator()) {
			if (mc.player.isInsideOfMaterial(Material.WATER)) {
				renderWaterOverlayTexture(partialTicks);
			}

			if (mc.player.isBurning()) {
				renderFireInFirstPerson();
			}
		}

		GlStateManager.enableAlpha();
	}

	/**
	 * Render the block in the player's hand
	 */
	private void renderBlockInHand(TextureAtlasSprite sprite) {

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		float f = 0.1F;
		GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
		GlStateManager.pushMatrix();
		float f1 = -1F;
		float f2 = 1F;
		float f3 = -1F;
		float f4 = 1F;
		float f5 = -0.5F;
		float f6 = sprite.getMinU();
		float f7 = sprite.getMaxU();
		float f8 = sprite.getMinV();
		float f9 = sprite.getMaxV();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(-1D, -1D, -0.5D).tex(f7, f9).endVertex();
		bufferbuilder.pos(1D, -1D, -0.5D).tex(f6, f9).endVertex();
		bufferbuilder.pos(1D, 1D, -0.5D).tex(f6, f8).endVertex();
		bufferbuilder.pos(-1D, 1D, -0.5D).tex(f7, f8).endVertex();
		tessellator.draw();
		GlStateManager.popMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	/**
	 * Renders a texture that warps around based on the direction the player is looking. Texture needs to be bound
	 * before being called. Used for the water overlay.
	 */
	private void renderWaterOverlayTexture(float partialTicks) {

		mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		float f = mc.player.getBrightness();
		GlStateManager.color(f, f, f, 0.5F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		float f1 = 4F;
		float f2 = -1F;
		float f3 = 1F;
		float f4 = -1F;
		float f5 = 1F;
		float f6 = -0.5F;
		float f7 = -mc.player.rotationYaw / 64F;
		float f8 = mc.player.rotationPitch / 64F;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(-1D, -1D, -0.5D).tex(4F + f7, 4F + f8).endVertex();
		bufferbuilder.pos(1D, -1D, -0.5D).tex(0F + f7, 4F + f8).endVertex();
		bufferbuilder.pos(1D, 1D, -0.5D).tex(0F + f7, 0F + f8).endVertex();
		bufferbuilder.pos(-1D, 1D, -0.5D).tex(4F + f7, 0F + f8).endVertex();
		tessellator.draw();
		GlStateManager.popMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableBlend();
	}

	/**
	 * Renders the fire on the screen for first person mode. Arg: partialTickTime
	 */
	private void renderFireInFirstPerson() {

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(1F, 1F, 1F, 0.9F);
		GlStateManager.depthFunc(519);
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		float f = 1F;

		for (int i = 0; i < 2; ++i) {
			GlStateManager.pushMatrix();
			TextureAtlasSprite textureatlassprite = mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			float f1 = textureatlassprite.getMinU();
			float f2 = textureatlassprite.getMaxU();
			float f3 = textureatlassprite.getMinV();
			float f4 = textureatlassprite.getMaxV();
			float f5 = -0.5F;
			float f6 = 0.5F;
			float f7 = -0.5F;
			float f8 = 0.5F;
			float f9 = -0.5F;
			GlStateManager.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0F);
			GlStateManager.rotate((float) (i * 2 - 1) * 10F, 0F, 1F, 0F);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(-0.5D, -0.5D, -0.5D).tex(f2, f4).endVertex();
			bufferbuilder.pos(0.5D, -0.5D, -0.5D).tex(f1, f4).endVertex();
			bufferbuilder.pos(0.5D, 0.5D, -0.5D).tex(f1, f3).endVertex();
			bufferbuilder.pos(-0.5D, 0.5D, -0.5D).tex(f2, f3).endVertex();
			tessellator.draw();
			GlStateManager.popMatrix();
		}

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
	}

	public void updateEquippedItem() {

		prevEquippedProgressMainHand = equippedProgressMainHand;
		prevEquippedProgressOffHand = equippedProgressOffHand;
		EntityPlayerSP entityplayersp = mc.player;
		ItemStack itemstack = entityplayersp.getHeldItemMainhand();
		ItemStack itemstack1 = entityplayersp.getHeldItemOffhand();

		if (entityplayersp.isRowingBoat()) {
			equippedProgressMainHand = MathHelper.clamp(equippedProgressMainHand - 0.4F, 0F, 1F);
			equippedProgressOffHand = MathHelper.clamp(equippedProgressOffHand - 0.4F, 0F, 1F);
		} else {
			float f = entityplayersp.getCooledAttackStrength(1F);
			equippedProgressMainHand += MathHelper.clamp((Objects.equals(itemStackMainHand, itemstack) ? f * f * f : 0F) - equippedProgressMainHand, -0.4F, 0.4F);
			equippedProgressOffHand += MathHelper.clamp((float) (Objects.equals(itemStackOffHand, itemstack1) ? 1 : 0) - equippedProgressOffHand, -0.4F, 0.4F);
		}

		if (equippedProgressMainHand < 0.1F) {
			itemStackMainHand = itemstack;
		}

		if (equippedProgressOffHand < 0.1F) {
			itemStackOffHand = itemstack1;
		}
	}

	public void resetEquippedProgress(Hand hand) {

		if (hand == Hand.MAIN_HAND) {
			equippedProgressMainHand = 0F;
		} else {
			equippedProgressOffHand = 0F;
		}
	}

}
