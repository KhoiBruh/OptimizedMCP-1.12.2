package net.minecraft.client.renderer;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.List;

public class RenderItem implements IResourceManagerReloadListener {

	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	/**
	 * False when the renderer is rendering the item's effects into a GUI
	 */
	private final boolean notRenderingEffectsInGUI = true;
	private final ItemModelMesher itemModelMesher;
	private final TextureManager textureManager;
	private final ItemColors itemColors;
	/**
	 * Defines the zLevel of rendering of item on GUI.
	 */
	public float zLevel;

	public RenderItem(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_) {

		textureManager = p_i46552_1_;
		itemModelMesher = new ItemModelMesher(p_i46552_2_);
		registerItems();
		itemColors = p_i46552_3_;
	}

	public ItemModelMesher getItemModelMesher() {

		return itemModelMesher;
	}

	protected void registerItem(Item itm, int subType, String identifier) {

		itemModelMesher.register(itm, subType, new ModelResourceLocation(identifier, "inventory"));
	}

	protected void registerBlock(Block blk, int subType, String identifier) {

		registerItem(Item.getItemFromBlock(blk), subType, identifier);
	}

	private void registerBlock(Block blk, String identifier) {

		registerBlock(blk, 0, identifier);
	}

	private void registerItem(Item itm, String identifier) {

		registerItem(itm, 0, identifier);
	}

	private void renderModel(IBakedModel model, ItemStack stack) {

		renderModel(model, -1, stack);
	}

	private void renderModel(IBakedModel model, int color) {

		renderModel(model, color, ItemStack.EMPTY);
	}

	private void renderModel(IBakedModel model, int color, ItemStack stack) {

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.values()) {
			renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), color, stack);
		}

		renderQuads(bufferbuilder, model.getQuads(null, null, 0L), color, stack);
		tessellator.draw();
	}

	public void renderItem(ItemStack stack, IBakedModel model) {

		if (!stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			if (model.isBuiltInRenderer()) {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableRescaleNormal();
				TileEntityItemStackRenderer.instance.renderByItem(stack);
			} else {
				renderModel(model, stack);

				if (stack.hasEffect()) {
					renderEffect(model);
				}
			}

			GlStateManager.popMatrix();
		}
	}

	private void renderEffect(IBakedModel model) {

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);
		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		renderModel(model, -8372020);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		renderModel(model, -8372020);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableLighting();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	private void putQuadNormal(BufferBuilder renderer, BakedQuad quad) {

		Vec3i vec3i = quad.getFace().getDirectionVec();
		renderer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
	}

	private void renderQuad(BufferBuilder renderer, BakedQuad quad, int color) {

		renderer.addVertexData(quad.getVertexData());
		renderer.putColor4(color);
		putQuadNormal(renderer, quad);
	}

	private void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack) {

		boolean flag = color == -1 && !stack.isEmpty();
		int i = 0;

		for (int j = quads.size(); i < j; ++i) {
			BakedQuad bakedquad = quads.get(i);
			int k = color;

			if (flag && bakedquad.hasTintIndex()) {
				k = itemColors.colorMultiplier(stack, bakedquad.getTintIndex());

				if (EntityRenderer.anaglyphEnable) {
					k = TextureUtil.anaglyphColor(k);
				}

				k = k | -16777216;
			}

			renderQuad(renderer, bakedquad, k);
		}
	}

	public boolean shouldRenderItemIn3D(ItemStack stack) {

		IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
		return ibakedmodel != null && ibakedmodel.isGui3d();
	}

	public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType) {

		if (!stack.isEmpty()) {
			IBakedModel ibakedmodel = getItemModelWithOverrides(stack, null, null);
			renderItemModel(stack, ibakedmodel, cameraTransformType, false);
		}
	}

	public IBakedModel getItemModelWithOverrides(ItemStack stack, World worldIn, EntityLivingBase entitylivingbaseIn) {

		IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
		Item item = stack.getItem();

		if (item != null && item.hasCustomProperties()) {
			ResourceLocation resourcelocation = ibakedmodel.getOverrides().applyOverride(stack, worldIn, entitylivingbaseIn);
			return resourcelocation == null ? ibakedmodel : itemModelMesher.getModelManager().getModel(new ModelResourceLocation(resourcelocation, "inventory"));
		} else {
			return ibakedmodel;
		}
	}

	public void renderItem(ItemStack stack, EntityLivingBase entitylivingbaseIn, ItemCameraTransforms.TransformType transform, boolean leftHanded) {

		if (!stack.isEmpty() && entitylivingbaseIn != null) {
			IBakedModel ibakedmodel = getItemModelWithOverrides(stack, entitylivingbaseIn.world, entitylivingbaseIn);
			renderItemModel(stack, ibakedmodel, transform, leftHanded);
		}
	}

	protected void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded) {

		if (!stack.isEmpty()) {
			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.pushMatrix();
			ItemCameraTransforms itemcameratransforms = bakedmodel.getItemCameraTransforms();
			ItemCameraTransforms.applyTransformSide(itemcameratransforms.getTransform(transform), leftHanded);

			if (isThereOneNegativeScale(itemcameratransforms.getTransform(transform))) {
				GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
			}

			renderItem(stack, bakedmodel);
			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	/**
	 * Return true if only one scale is negative
	 */
	private boolean isThereOneNegativeScale(ItemTransformVec3f itemTranformVec) {

		return itemTranformVec.scale().x < 0.0F ^ itemTranformVec.scale().y < 0.0F ^ itemTranformVec.scale().z < 0.0F;
	}

	public void renderItemIntoGUI(ItemStack stack, int x, int y) {

		renderItemModelIntoGUI(stack, x, y, getItemModelWithOverrides(stack, null, null));
	}

	protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel) {

		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		setupGuiTransform(x, y, bakedmodel.isGui3d());
		bakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
		renderItem(stack, bakedmodel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d) {

		GlStateManager.translate((float) xPosition, (float) yPosition, 100.0F + zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, -1.0F, 1.0F);
		GlStateManager.scale(16.0F, 16.0F, 16.0F);

		if (isGui3d) {
			GlStateManager.enableLighting();
		} else {
			GlStateManager.disableLighting();
		}
	}

	public void renderItemAndEffectIntoGUI(ItemStack stack, int xPosition, int yPosition) {

		renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, stack, xPosition, yPosition);
	}

	public void renderItemAndEffectIntoGUI(EntityLivingBase p_184391_1_, final ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_) {

		if (!p_184391_2_.isEmpty()) {
			zLevel += 50.0F;

			try {
				renderItemModelIntoGUI(p_184391_2_, p_184391_3_, p_184391_4_, getItemModelWithOverrides(p_184391_2_, null, p_184391_1_));
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
				crashreportcategory.addDetail("Item Type", () -> String.valueOf(p_184391_2_.getItem()));
				crashreportcategory.addDetail("Item Aux", () -> String.valueOf(p_184391_2_.getMetadata()));
				crashreportcategory.addDetail("Item NBT", () -> String.valueOf(p_184391_2_.getTagCompound()));
				crashreportcategory.addDetail("Item Foil", () -> String.valueOf(p_184391_2_.hasEffect()));
				throw new ReportedException(crashreport);
			}

			zLevel -= 50.0F;
		}
	}

	public void renderItemOverlays(FontRenderer fr, ItemStack stack, int xPosition, int yPosition) {

		renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, null);
	}

	/**
	 * Renders the stack size and/or damage bar for the given ItemStack.
	 */
	public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {

		if (!stack.isEmpty()) {
			if (stack.getCount() != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.getCount()) : text;
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}

			if (stack.isItemDamaged()) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				float f = (float) stack.getItemDamage();
				float f1 = (float) stack.getMaxDamage();
				float f2 = Math.max(0.0F, (f1 - f) / f1);
				int i = Math.round(13.0F - f * 13.0F / f1);
				int j = MathHelper.hsvToRGB(f2 / 3.0F, 1.0F, 1.0F);
				draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
				draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}

			EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
			float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

			if (f3 > 0.0F) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				Tessellator tessellator1 = Tessellator.getInstance();
				BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
				draw(bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		}
	}

	/**
	 * Draw with the WorldRenderer
	 */
	private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {

		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

	private void registerItems() {

		registerBlock(Blocks.ANVIL, "anvil_intact");
		registerBlock(Blocks.ANVIL, 1, "anvil_slightly_damaged");
		registerBlock(Blocks.ANVIL, 2, "anvil_very_damaged");
		registerBlock(Blocks.CARPET, EnumDyeColor.BLACK.getMetadata(), "black_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.BLUE.getMetadata(), "blue_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.BROWN.getMetadata(), "brown_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.CYAN.getMetadata(), "cyan_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.GRAY.getMetadata(), "gray_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.GREEN.getMetadata(), "green_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.LIME.getMetadata(), "lime_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.MAGENTA.getMetadata(), "magenta_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.ORANGE.getMetadata(), "orange_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.PINK.getMetadata(), "pink_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.PURPLE.getMetadata(), "purple_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.RED.getMetadata(), "red_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.SILVER.getMetadata(), "silver_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.WHITE.getMetadata(), "white_carpet");
		registerBlock(Blocks.CARPET, EnumDyeColor.YELLOW.getMetadata(), "yellow_carpet");
		registerBlock(Blocks.COBBLESTONE_WALL, BlockWall.EnumType.MOSSY.getMetadata(), "mossy_cobblestone_wall");
		registerBlock(Blocks.COBBLESTONE_WALL, BlockWall.EnumType.NORMAL.getMetadata(), "cobblestone_wall");
		registerBlock(Blocks.DIRT, BlockDirt.DirtType.COARSE_DIRT.getMetadata(), "coarse_dirt");
		registerBlock(Blocks.DIRT, BlockDirt.DirtType.DIRT.getMetadata(), "dirt");
		registerBlock(Blocks.DIRT, BlockDirt.DirtType.PODZOL.getMetadata(), "podzol");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.FERN.getMeta(), "double_fern");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.GRASS.getMeta(), "double_grass");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta(), "paeonia");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.ROSE.getMeta(), "double_rose");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta(), "sunflower");
		registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta(), "syringa");
		registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_leaves");
		registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_leaves");
		registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.OAK.getMetadata(), "oak_leaves");
		registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_leaves");
		registerBlock(Blocks.LEAVES2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_leaves");
		registerBlock(Blocks.LEAVES2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_leaves");
		registerBlock(Blocks.LOG, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_log");
		registerBlock(Blocks.LOG, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_log");
		registerBlock(Blocks.LOG, BlockPlanks.EnumType.OAK.getMetadata(), "oak_log");
		registerBlock(Blocks.LOG, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_log");
		registerBlock(Blocks.LOG2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_log");
		registerBlock(Blocks.LOG2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_log");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.CHISELED_STONEBRICK.getMetadata(), "chiseled_brick_monster_egg");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.COBBLESTONE.getMetadata(), "cobblestone_monster_egg");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.CRACKED_STONEBRICK.getMetadata(), "cracked_brick_monster_egg");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.MOSSY_STONEBRICK.getMetadata(), "mossy_brick_monster_egg");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.STONE.getMetadata(), "stone_monster_egg");
		registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.STONEBRICK.getMetadata(), "stone_brick_monster_egg");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_planks");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_planks");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_planks");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_planks");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.OAK.getMetadata(), "oak_planks");
		registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_planks");
		registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.BRICKS.getMetadata(), "prismarine_bricks");
		registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.DARK.getMetadata(), "dark_prismarine");
		registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.ROUGH.getMetadata(), "prismarine");
		registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.CHISELED.getMetadata(), "chiseled_quartz_block");
		registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.DEFAULT.getMetadata(), "quartz_block");
		registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.LINES_Y.getMetadata(), "quartz_column");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.ALLIUM.getMeta(), "allium");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta(), "blue_orchid");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta(), "houstonia");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta(), "orange_tulip");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta(), "oxeye_daisy");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta(), "pink_tulip");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY.getMeta(), "poppy");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.RED_TULIP.getMeta(), "red_tulip");
		registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta(), "white_tulip");
		registerBlock(Blocks.SAND, BlockSand.EnumType.RED_SAND.getMetadata(), "red_sand");
		registerBlock(Blocks.SAND, BlockSand.EnumType.SAND.getMetadata(), "sand");
		registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.CHISELED.getMetadata(), "chiseled_sandstone");
		registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.DEFAULT.getMetadata(), "sandstone");
		registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.SMOOTH.getMetadata(), "smooth_sandstone");
		registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.CHISELED.getMetadata(), "chiseled_red_sandstone");
		registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.DEFAULT.getMetadata(), "red_sandstone");
		registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.SMOOTH.getMetadata(), "smooth_red_sandstone");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_sapling");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_sapling");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_sapling");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_sapling");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.OAK.getMetadata(), "oak_sapling");
		registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_sapling");
		registerBlock(Blocks.SPONGE, 0, "sponge");
		registerBlock(Blocks.SPONGE, 1, "sponge_wet");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.RED.getMetadata(), "red_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass");
		registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.RED.getMetadata(), "red_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass_pane");
		registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass_pane");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BLACK.getMetadata(), "black_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BLUE.getMetadata(), "blue_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BROWN.getMetadata(), "brown_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.GRAY.getMetadata(), "gray_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.GREEN.getMetadata(), "green_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.LIME.getMetadata(), "lime_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.PINK.getMetadata(), "pink_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.RED.getMetadata(), "red_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.SILVER.getMetadata(), "silver_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.WHITE.getMetadata(), "white_stained_hardened_clay");
		registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_hardened_clay");
		registerBlock(Blocks.STONE, BlockStone.EnumType.ANDESITE.getMetadata(), "andesite");
		registerBlock(Blocks.STONE, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata(), "andesite_smooth");
		registerBlock(Blocks.STONE, BlockStone.EnumType.DIORITE.getMetadata(), "diorite");
		registerBlock(Blocks.STONE, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata(), "diorite_smooth");
		registerBlock(Blocks.STONE, BlockStone.EnumType.GRANITE.getMetadata(), "granite");
		registerBlock(Blocks.STONE, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata(), "granite_smooth");
		registerBlock(Blocks.STONE, BlockStone.EnumType.STONE.getMetadata(), "stone");
		registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.CRACKED.getMetadata(), "cracked_stonebrick");
		registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.DEFAULT.getMetadata(), "stonebrick");
		registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.CHISELED.getMetadata(), "chiseled_stonebrick");
		registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.MOSSY.getMetadata(), "mossy_stonebrick");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.BRICK.getMetadata(), "brick_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata(), "cobblestone_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.WOOD.getMetadata(), "old_wood_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.NETHERBRICK.getMetadata(), "nether_brick_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.QUARTZ.getMetadata(), "quartz_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.SAND.getMetadata(), "sandstone_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata(), "stone_brick_slab");
		registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.STONE.getMetadata(), "stone_slab");
		registerBlock(Blocks.STONE_SLAB2, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata(), "red_sandstone_slab");
		registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.DEAD_BUSH.getMeta(), "dead_bush");
		registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.FERN.getMeta(), "fern");
		registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.GRASS.getMeta(), "tall_grass");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_slab");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_slab");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_slab");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_slab");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.OAK.getMetadata(), "oak_slab");
		registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_slab");
		registerBlock(Blocks.WOOL, EnumDyeColor.BLACK.getMetadata(), "black_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.BLUE.getMetadata(), "blue_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.BROWN.getMetadata(), "brown_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.CYAN.getMetadata(), "cyan_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.GRAY.getMetadata(), "gray_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.GREEN.getMetadata(), "green_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.LIME.getMetadata(), "lime_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.MAGENTA.getMetadata(), "magenta_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.ORANGE.getMetadata(), "orange_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.PINK.getMetadata(), "pink_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.PURPLE.getMetadata(), "purple_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.RED.getMetadata(), "red_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.SILVER.getMetadata(), "silver_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.WHITE.getMetadata(), "white_wool");
		registerBlock(Blocks.WOOL, EnumDyeColor.YELLOW.getMetadata(), "yellow_wool");
		registerBlock(Blocks.FARMLAND, "farmland");
		registerBlock(Blocks.ACACIA_STAIRS, "acacia_stairs");
		registerBlock(Blocks.ACTIVATOR_RAIL, "activator_rail");
		registerBlock(Blocks.BEACON, "beacon");
		registerBlock(Blocks.BEDROCK, "bedrock");
		registerBlock(Blocks.BIRCH_STAIRS, "birch_stairs");
		registerBlock(Blocks.BOOKSHELF, "bookshelf");
		registerBlock(Blocks.BRICK_BLOCK, "brick_block");
		registerBlock(Blocks.BRICK_BLOCK, "brick_block");
		registerBlock(Blocks.BRICK_STAIRS, "brick_stairs");
		registerBlock(Blocks.BROWN_MUSHROOM, "brown_mushroom");
		registerBlock(Blocks.CACTUS, "cactus");
		registerBlock(Blocks.CLAY, "clay");
		registerBlock(Blocks.COAL_BLOCK, "coal_block");
		registerBlock(Blocks.COAL_ORE, "coal_ore");
		registerBlock(Blocks.COBBLESTONE, "cobblestone");
		registerBlock(Blocks.CRAFTING_TABLE, "crafting_table");
		registerBlock(Blocks.DARK_OAK_STAIRS, "dark_oak_stairs");
		registerBlock(Blocks.DAYLIGHT_DETECTOR, "daylight_detector");
		registerBlock(Blocks.DEADBUSH, "dead_bush");
		registerBlock(Blocks.DETECTOR_RAIL, "detector_rail");
		registerBlock(Blocks.DIAMOND_BLOCK, "diamond_block");
		registerBlock(Blocks.DIAMOND_ORE, "diamond_ore");
		registerBlock(Blocks.DISPENSER, "dispenser");
		registerBlock(Blocks.DROPPER, "dropper");
		registerBlock(Blocks.EMERALD_BLOCK, "emerald_block");
		registerBlock(Blocks.EMERALD_ORE, "emerald_ore");
		registerBlock(Blocks.ENCHANTING_TABLE, "enchanting_table");
		registerBlock(Blocks.END_PORTAL_FRAME, "end_portal_frame");
		registerBlock(Blocks.END_STONE, "end_stone");
		registerBlock(Blocks.OAK_FENCE, "oak_fence");
		registerBlock(Blocks.SPRUCE_FENCE, "spruce_fence");
		registerBlock(Blocks.BIRCH_FENCE, "birch_fence");
		registerBlock(Blocks.JUNGLE_FENCE, "jungle_fence");
		registerBlock(Blocks.DARK_OAK_FENCE, "dark_oak_fence");
		registerBlock(Blocks.ACACIA_FENCE, "acacia_fence");
		registerBlock(Blocks.OAK_FENCE_GATE, "oak_fence_gate");
		registerBlock(Blocks.SPRUCE_FENCE_GATE, "spruce_fence_gate");
		registerBlock(Blocks.BIRCH_FENCE_GATE, "birch_fence_gate");
		registerBlock(Blocks.JUNGLE_FENCE_GATE, "jungle_fence_gate");
		registerBlock(Blocks.DARK_OAK_FENCE_GATE, "dark_oak_fence_gate");
		registerBlock(Blocks.ACACIA_FENCE_GATE, "acacia_fence_gate");
		registerBlock(Blocks.FURNACE, "furnace");
		registerBlock(Blocks.GLASS, "glass");
		registerBlock(Blocks.GLASS_PANE, "glass_pane");
		registerBlock(Blocks.GLOWSTONE, "glowstone");
		registerBlock(Blocks.GOLDEN_RAIL, "golden_rail");
		registerBlock(Blocks.GOLD_BLOCK, "gold_block");
		registerBlock(Blocks.GOLD_ORE, "gold_ore");
		registerBlock(Blocks.GRASS, "grass");
		registerBlock(Blocks.GRASS_PATH, "grass_path");
		registerBlock(Blocks.GRAVEL, "gravel");
		registerBlock(Blocks.HARDENED_CLAY, "hardened_clay");
		registerBlock(Blocks.HAY_BLOCK, "hay_block");
		registerBlock(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, "heavy_weighted_pressure_plate");
		registerBlock(Blocks.HOPPER, "hopper");
		registerBlock(Blocks.ICE, "ice");
		registerBlock(Blocks.IRON_BARS, "iron_bars");
		registerBlock(Blocks.IRON_BLOCK, "iron_block");
		registerBlock(Blocks.IRON_ORE, "iron_ore");
		registerBlock(Blocks.IRON_TRAPDOOR, "iron_trapdoor");
		registerBlock(Blocks.JUKEBOX, "jukebox");
		registerBlock(Blocks.JUNGLE_STAIRS, "jungle_stairs");
		registerBlock(Blocks.LADDER, "ladder");
		registerBlock(Blocks.LAPIS_BLOCK, "lapis_block");
		registerBlock(Blocks.LAPIS_ORE, "lapis_ore");
		registerBlock(Blocks.LEVER, "lever");
		registerBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, "light_weighted_pressure_plate");
		registerBlock(Blocks.LIT_PUMPKIN, "lit_pumpkin");
		registerBlock(Blocks.MELON_BLOCK, "melon_block");
		registerBlock(Blocks.MOSSY_COBBLESTONE, "mossy_cobblestone");
		registerBlock(Blocks.MYCELIUM, "mycelium");
		registerBlock(Blocks.NETHERRACK, "netherrack");
		registerBlock(Blocks.NETHER_BRICK, "nether_brick");
		registerBlock(Blocks.NETHER_BRICK_FENCE, "nether_brick_fence");
		registerBlock(Blocks.NETHER_BRICK_STAIRS, "nether_brick_stairs");
		registerBlock(Blocks.NOTEBLOCK, "noteblock");
		registerBlock(Blocks.OAK_STAIRS, "oak_stairs");
		registerBlock(Blocks.OBSIDIAN, "obsidian");
		registerBlock(Blocks.PACKED_ICE, "packed_ice");
		registerBlock(Blocks.PISTON, "piston");
		registerBlock(Blocks.PUMPKIN, "pumpkin");
		registerBlock(Blocks.QUARTZ_ORE, "quartz_ore");
		registerBlock(Blocks.QUARTZ_STAIRS, "quartz_stairs");
		registerBlock(Blocks.RAIL, "rail");
		registerBlock(Blocks.REDSTONE_BLOCK, "redstone_block");
		registerBlock(Blocks.REDSTONE_LAMP, "redstone_lamp");
		registerBlock(Blocks.REDSTONE_ORE, "redstone_ore");
		registerBlock(Blocks.REDSTONE_TORCH, "redstone_torch");
		registerBlock(Blocks.RED_MUSHROOM, "red_mushroom");
		registerBlock(Blocks.SANDSTONE_STAIRS, "sandstone_stairs");
		registerBlock(Blocks.RED_SANDSTONE_STAIRS, "red_sandstone_stairs");
		registerBlock(Blocks.SEA_LANTERN, "sea_lantern");
		registerBlock(Blocks.SLIME_BLOCK, "slime");
		registerBlock(Blocks.SNOW, "snow");
		registerBlock(Blocks.SNOW_LAYER, "snow_layer");
		registerBlock(Blocks.SOUL_SAND, "soul_sand");
		registerBlock(Blocks.SPRUCE_STAIRS, "spruce_stairs");
		registerBlock(Blocks.STICKY_PISTON, "sticky_piston");
		registerBlock(Blocks.STONE_BRICK_STAIRS, "stone_brick_stairs");
		registerBlock(Blocks.STONE_BUTTON, "stone_button");
		registerBlock(Blocks.STONE_PRESSURE_PLATE, "stone_pressure_plate");
		registerBlock(Blocks.STONE_STAIRS, "stone_stairs");
		registerBlock(Blocks.TNT, "tnt");
		registerBlock(Blocks.TORCH, "torch");
		registerBlock(Blocks.TRAPDOOR, "trapdoor");
		registerBlock(Blocks.TRIPWIRE_HOOK, "tripwire_hook");
		registerBlock(Blocks.VINE, "vine");
		registerBlock(Blocks.WATERLILY, "waterlily");
		registerBlock(Blocks.WEB, "web");
		registerBlock(Blocks.WOODEN_BUTTON, "wooden_button");
		registerBlock(Blocks.WOODEN_PRESSURE_PLATE, "wooden_pressure_plate");
		registerBlock(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION.getMeta(), "dandelion");
		registerBlock(Blocks.END_ROD, "end_rod");
		registerBlock(Blocks.CHORUS_PLANT, "chorus_plant");
		registerBlock(Blocks.CHORUS_FLOWER, "chorus_flower");
		registerBlock(Blocks.PURPUR_BLOCK, "purpur_block");
		registerBlock(Blocks.PURPUR_PILLAR, "purpur_pillar");
		registerBlock(Blocks.PURPUR_STAIRS, "purpur_stairs");
		registerBlock(Blocks.PURPUR_SLAB, "purpur_slab");
		registerBlock(Blocks.PURPUR_DOUBLE_SLAB, "purpur_double_slab");
		registerBlock(Blocks.END_BRICKS, "end_bricks");
		registerBlock(Blocks.MAGMA, "magma");
		registerBlock(Blocks.NETHER_WART_BLOCK, "nether_wart_block");
		registerBlock(Blocks.RED_NETHER_BRICK, "red_nether_brick");
		registerBlock(Blocks.BONE_BLOCK, "bone_block");
		registerBlock(Blocks.STRUCTURE_VOID, "structure_void");
		registerBlock(Blocks.OBSERVER, "observer");
		registerBlock(Blocks.WHITE_SHULKER_BOX, "white_shulker_box");
		registerBlock(Blocks.ORANGE_SHULKER_BOX, "orange_shulker_box");
		registerBlock(Blocks.MAGENTA_SHULKER_BOX, "magenta_shulker_box");
		registerBlock(Blocks.LIGHT_BLUE_SHULKER_BOX, "light_blue_shulker_box");
		registerBlock(Blocks.YELLOW_SHULKER_BOX, "yellow_shulker_box");
		registerBlock(Blocks.LIME_SHULKER_BOX, "lime_shulker_box");
		registerBlock(Blocks.PINK_SHULKER_BOX, "pink_shulker_box");
		registerBlock(Blocks.GRAY_SHULKER_BOX, "gray_shulker_box");
		registerBlock(Blocks.SILVER_SHULKER_BOX, "silver_shulker_box");
		registerBlock(Blocks.CYAN_SHULKER_BOX, "cyan_shulker_box");
		registerBlock(Blocks.PURPLE_SHULKER_BOX, "purple_shulker_box");
		registerBlock(Blocks.BLUE_SHULKER_BOX, "blue_shulker_box");
		registerBlock(Blocks.BROWN_SHULKER_BOX, "brown_shulker_box");
		registerBlock(Blocks.GREEN_SHULKER_BOX, "green_shulker_box");
		registerBlock(Blocks.RED_SHULKER_BOX, "red_shulker_box");
		registerBlock(Blocks.BLACK_SHULKER_BOX, "black_shulker_box");
		registerBlock(Blocks.WHITE_GLAZED_TERRACOTTA, "white_glazed_terracotta");
		registerBlock(Blocks.ORANGE_GLAZED_TERRACOTTA, "orange_glazed_terracotta");
		registerBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA, "magenta_glazed_terracotta");
		registerBlock(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, "light_blue_glazed_terracotta");
		registerBlock(Blocks.YELLOW_GLAZED_TERRACOTTA, "yellow_glazed_terracotta");
		registerBlock(Blocks.LIME_GLAZED_TERRACOTTA, "lime_glazed_terracotta");
		registerBlock(Blocks.PINK_GLAZED_TERRACOTTA, "pink_glazed_terracotta");
		registerBlock(Blocks.GRAY_GLAZED_TERRACOTTA, "gray_glazed_terracotta");
		registerBlock(Blocks.SILVER_GLAZED_TERRACOTTA, "silver_glazed_terracotta");
		registerBlock(Blocks.CYAN_GLAZED_TERRACOTTA, "cyan_glazed_terracotta");
		registerBlock(Blocks.PURPLE_GLAZED_TERRACOTTA, "purple_glazed_terracotta");
		registerBlock(Blocks.BLUE_GLAZED_TERRACOTTA, "blue_glazed_terracotta");
		registerBlock(Blocks.BROWN_GLAZED_TERRACOTTA, "brown_glazed_terracotta");
		registerBlock(Blocks.GREEN_GLAZED_TERRACOTTA, "green_glazed_terracotta");
		registerBlock(Blocks.RED_GLAZED_TERRACOTTA, "red_glazed_terracotta");
		registerBlock(Blocks.BLACK_GLAZED_TERRACOTTA, "black_glazed_terracotta");

		for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
			registerBlock(Blocks.CONCRETE, enumdyecolor.getMetadata(), enumdyecolor.getDyeColorName() + "_concrete");
			registerBlock(Blocks.CONCRETE_POWDER, enumdyecolor.getMetadata(), enumdyecolor.getDyeColorName() + "_concrete_powder");
		}

		registerBlock(Blocks.CHEST, "chest");
		registerBlock(Blocks.TRAPPED_CHEST, "trapped_chest");
		registerBlock(Blocks.ENDER_CHEST, "ender_chest");
		registerItem(Items.IRON_SHOVEL, "iron_shovel");
		registerItem(Items.IRON_PICKAXE, "iron_pickaxe");
		registerItem(Items.IRON_AXE, "iron_axe");
		registerItem(Items.FLINT_AND_STEEL, "flint_and_steel");
		registerItem(Items.APPLE, "apple");
		registerItem(Items.BOW, "bow");
		registerItem(Items.ARROW, "arrow");
		registerItem(Items.SPECTRAL_ARROW, "spectral_arrow");
		registerItem(Items.TIPPED_ARROW, "tipped_arrow");
		registerItem(Items.COAL, 0, "coal");
		registerItem(Items.COAL, 1, "charcoal");
		registerItem(Items.DIAMOND, "diamond");
		registerItem(Items.IRON_INGOT, "iron_ingot");
		registerItem(Items.GOLD_INGOT, "gold_ingot");
		registerItem(Items.IRON_SWORD, "iron_sword");
		registerItem(Items.WOODEN_SWORD, "wooden_sword");
		registerItem(Items.WOODEN_SHOVEL, "wooden_shovel");
		registerItem(Items.WOODEN_PICKAXE, "wooden_pickaxe");
		registerItem(Items.WOODEN_AXE, "wooden_axe");
		registerItem(Items.STONE_SWORD, "stone_sword");
		registerItem(Items.STONE_SHOVEL, "stone_shovel");
		registerItem(Items.STONE_PICKAXE, "stone_pickaxe");
		registerItem(Items.STONE_AXE, "stone_axe");
		registerItem(Items.DIAMOND_SWORD, "diamond_sword");
		registerItem(Items.DIAMOND_SHOVEL, "diamond_shovel");
		registerItem(Items.DIAMOND_PICKAXE, "diamond_pickaxe");
		registerItem(Items.DIAMOND_AXE, "diamond_axe");
		registerItem(Items.STICK, "stick");
		registerItem(Items.BOWL, "bowl");
		registerItem(Items.MUSHROOM_STEW, "mushroom_stew");
		registerItem(Items.GOLDEN_SWORD, "golden_sword");
		registerItem(Items.GOLDEN_SHOVEL, "golden_shovel");
		registerItem(Items.GOLDEN_PICKAXE, "golden_pickaxe");
		registerItem(Items.GOLDEN_AXE, "golden_axe");
		registerItem(Items.STRING, "string");
		registerItem(Items.FEATHER, "feather");
		registerItem(Items.GUNPOWDER, "gunpowder");
		registerItem(Items.WOODEN_HOE, "wooden_hoe");
		registerItem(Items.STONE_HOE, "stone_hoe");
		registerItem(Items.IRON_HOE, "iron_hoe");
		registerItem(Items.DIAMOND_HOE, "diamond_hoe");
		registerItem(Items.GOLDEN_HOE, "golden_hoe");
		registerItem(Items.WHEAT_SEEDS, "wheat_seeds");
		registerItem(Items.WHEAT, "wheat");
		registerItem(Items.BREAD, "bread");
		registerItem(Items.LEATHER_HELMET, "leather_helmet");
		registerItem(Items.LEATHER_CHESTPLATE, "leather_chestplate");
		registerItem(Items.LEATHER_LEGGINGS, "leather_leggings");
		registerItem(Items.LEATHER_BOOTS, "leather_boots");
		registerItem(Items.CHAINMAIL_HELMET, "chainmail_helmet");
		registerItem(Items.CHAINMAIL_CHESTPLATE, "chainmail_chestplate");
		registerItem(Items.CHAINMAIL_LEGGINGS, "chainmail_leggings");
		registerItem(Items.CHAINMAIL_BOOTS, "chainmail_boots");
		registerItem(Items.IRON_HELMET, "iron_helmet");
		registerItem(Items.IRON_CHESTPLATE, "iron_chestplate");
		registerItem(Items.IRON_LEGGINGS, "iron_leggings");
		registerItem(Items.IRON_BOOTS, "iron_boots");
		registerItem(Items.DIAMOND_HELMET, "diamond_helmet");
		registerItem(Items.DIAMOND_CHESTPLATE, "diamond_chestplate");
		registerItem(Items.DIAMOND_LEGGINGS, "diamond_leggings");
		registerItem(Items.DIAMOND_BOOTS, "diamond_boots");
		registerItem(Items.GOLDEN_HELMET, "golden_helmet");
		registerItem(Items.GOLDEN_CHESTPLATE, "golden_chestplate");
		registerItem(Items.GOLDEN_LEGGINGS, "golden_leggings");
		registerItem(Items.GOLDEN_BOOTS, "golden_boots");
		registerItem(Items.FLINT, "flint");
		registerItem(Items.PORKCHOP, "porkchop");
		registerItem(Items.COOKED_PORKCHOP, "cooked_porkchop");
		registerItem(Items.PAINTING, "painting");
		registerItem(Items.GOLDEN_APPLE, "golden_apple");
		registerItem(Items.GOLDEN_APPLE, 1, "golden_apple");
		registerItem(Items.SIGN, "sign");
		registerItem(Items.OAK_DOOR, "oak_door");
		registerItem(Items.SPRUCE_DOOR, "spruce_door");
		registerItem(Items.BIRCH_DOOR, "birch_door");
		registerItem(Items.JUNGLE_DOOR, "jungle_door");
		registerItem(Items.ACACIA_DOOR, "acacia_door");
		registerItem(Items.DARK_OAK_DOOR, "dark_oak_door");
		registerItem(Items.BUCKET, "bucket");
		registerItem(Items.WATER_BUCKET, "water_bucket");
		registerItem(Items.LAVA_BUCKET, "lava_bucket");
		registerItem(Items.MINECART, "minecart");
		registerItem(Items.SADDLE, "saddle");
		registerItem(Items.IRON_DOOR, "iron_door");
		registerItem(Items.REDSTONE, "redstone");
		registerItem(Items.SNOWBALL, "snowball");
		registerItem(Items.BOAT, "oak_boat");
		registerItem(Items.SPRUCE_BOAT, "spruce_boat");
		registerItem(Items.BIRCH_BOAT, "birch_boat");
		registerItem(Items.JUNGLE_BOAT, "jungle_boat");
		registerItem(Items.ACACIA_BOAT, "acacia_boat");
		registerItem(Items.DARK_OAK_BOAT, "dark_oak_boat");
		registerItem(Items.LEATHER, "leather");
		registerItem(Items.MILK_BUCKET, "milk_bucket");
		registerItem(Items.BRICK, "brick");
		registerItem(Items.CLAY_BALL, "clay_ball");
		registerItem(Items.REEDS, "reeds");
		registerItem(Items.PAPER, "paper");
		registerItem(Items.BOOK, "book");
		registerItem(Items.SLIME_BALL, "slime_ball");
		registerItem(Items.CHEST_MINECART, "chest_minecart");
		registerItem(Items.FURNACE_MINECART, "furnace_minecart");
		registerItem(Items.EGG, "egg");
		registerItem(Items.COMPASS, "compass");
		registerItem(Items.FISHING_ROD, "fishing_rod");
		registerItem(Items.CLOCK, "clock");
		registerItem(Items.GLOWSTONE_DUST, "glowstone_dust");
		registerItem(Items.FISH, ItemFishFood.FishType.COD.getMetadata(), "cod");
		registerItem(Items.FISH, ItemFishFood.FishType.SALMON.getMetadata(), "salmon");
		registerItem(Items.FISH, ItemFishFood.FishType.CLOWNFISH.getMetadata(), "clownfish");
		registerItem(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata(), "pufferfish");
		registerItem(Items.COOKED_FISH, ItemFishFood.FishType.COD.getMetadata(), "cooked_cod");
		registerItem(Items.COOKED_FISH, ItemFishFood.FishType.SALMON.getMetadata(), "cooked_salmon");
		registerItem(Items.DYE, EnumDyeColor.BLACK.getDyeDamage(), "dye_black");
		registerItem(Items.DYE, EnumDyeColor.RED.getDyeDamage(), "dye_red");
		registerItem(Items.DYE, EnumDyeColor.GREEN.getDyeDamage(), "dye_green");
		registerItem(Items.DYE, EnumDyeColor.BROWN.getDyeDamage(), "dye_brown");
		registerItem(Items.DYE, EnumDyeColor.BLUE.getDyeDamage(), "dye_blue");
		registerItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage(), "dye_purple");
		registerItem(Items.DYE, EnumDyeColor.CYAN.getDyeDamage(), "dye_cyan");
		registerItem(Items.DYE, EnumDyeColor.SILVER.getDyeDamage(), "dye_silver");
		registerItem(Items.DYE, EnumDyeColor.GRAY.getDyeDamage(), "dye_gray");
		registerItem(Items.DYE, EnumDyeColor.PINK.getDyeDamage(), "dye_pink");
		registerItem(Items.DYE, EnumDyeColor.LIME.getDyeDamage(), "dye_lime");
		registerItem(Items.DYE, EnumDyeColor.YELLOW.getDyeDamage(), "dye_yellow");
		registerItem(Items.DYE, EnumDyeColor.LIGHT_BLUE.getDyeDamage(), "dye_light_blue");
		registerItem(Items.DYE, EnumDyeColor.MAGENTA.getDyeDamage(), "dye_magenta");
		registerItem(Items.DYE, EnumDyeColor.ORANGE.getDyeDamage(), "dye_orange");
		registerItem(Items.DYE, EnumDyeColor.WHITE.getDyeDamage(), "dye_white");
		registerItem(Items.BONE, "bone");
		registerItem(Items.SUGAR, "sugar");
		registerItem(Items.CAKE, "cake");
		registerItem(Items.REPEATER, "repeater");
		registerItem(Items.COOKIE, "cookie");
		registerItem(Items.SHEARS, "shears");
		registerItem(Items.MELON, "melon");
		registerItem(Items.PUMPKIN_SEEDS, "pumpkin_seeds");
		registerItem(Items.MELON_SEEDS, "melon_seeds");
		registerItem(Items.BEEF, "beef");
		registerItem(Items.COOKED_BEEF, "cooked_beef");
		registerItem(Items.CHICKEN, "chicken");
		registerItem(Items.COOKED_CHICKEN, "cooked_chicken");
		registerItem(Items.RABBIT, "rabbit");
		registerItem(Items.COOKED_RABBIT, "cooked_rabbit");
		registerItem(Items.MUTTON, "mutton");
		registerItem(Items.COOKED_MUTTON, "cooked_mutton");
		registerItem(Items.RABBIT_FOOT, "rabbit_foot");
		registerItem(Items.RABBIT_HIDE, "rabbit_hide");
		registerItem(Items.RABBIT_STEW, "rabbit_stew");
		registerItem(Items.ROTTEN_FLESH, "rotten_flesh");
		registerItem(Items.ENDER_PEARL, "ender_pearl");
		registerItem(Items.BLAZE_ROD, "blaze_rod");
		registerItem(Items.GHAST_TEAR, "ghast_tear");
		registerItem(Items.GOLD_NUGGET, "gold_nugget");
		registerItem(Items.NETHER_WART, "nether_wart");
		registerItem(Items.BEETROOT, "beetroot");
		registerItem(Items.BEETROOT_SEEDS, "beetroot_seeds");
		registerItem(Items.BEETROOT_SOUP, "beetroot_soup");
		registerItem(Items.TOTEM_OF_UNDYING, "totem");
		registerItem(Items.POTIONITEM, "bottle_drinkable");
		registerItem(Items.SPLASH_POTION, "bottle_splash");
		registerItem(Items.LINGERING_POTION, "bottle_lingering");
		registerItem(Items.GLASS_BOTTLE, "glass_bottle");
		registerItem(Items.DRAGON_BREATH, "dragon_breath");
		registerItem(Items.SPIDER_EYE, "spider_eye");
		registerItem(Items.FERMENTED_SPIDER_EYE, "fermented_spider_eye");
		registerItem(Items.BLAZE_POWDER, "blaze_powder");
		registerItem(Items.MAGMA_CREAM, "magma_cream");
		registerItem(Items.BREWING_STAND, "brewing_stand");
		registerItem(Items.CAULDRON, "cauldron");
		registerItem(Items.ENDER_EYE, "ender_eye");
		registerItem(Items.SPECKLED_MELON, "speckled_melon");
		itemModelMesher.register(Items.SPAWN_EGG, stack -> new ModelResourceLocation("spawn_egg", "inventory"));
		registerItem(Items.EXPERIENCE_BOTTLE, "experience_bottle");
		registerItem(Items.FIRE_CHARGE, "fire_charge");
		registerItem(Items.WRITABLE_BOOK, "writable_book");
		registerItem(Items.EMERALD, "emerald");
		registerItem(Items.ITEM_FRAME, "item_frame");
		registerItem(Items.FLOWER_POT, "flower_pot");
		registerItem(Items.CARROT, "carrot");
		registerItem(Items.POTATO, "potato");
		registerItem(Items.BAKED_POTATO, "baked_potato");
		registerItem(Items.POISONOUS_POTATO, "poisonous_potato");
		registerItem(Items.MAP, "map");
		registerItem(Items.GOLDEN_CARROT, "golden_carrot");
		registerItem(Items.SKULL, 0, "skull_skeleton");
		registerItem(Items.SKULL, 1, "skull_wither");
		registerItem(Items.SKULL, 2, "skull_zombie");
		registerItem(Items.SKULL, 3, "skull_char");
		registerItem(Items.SKULL, 4, "skull_creeper");
		registerItem(Items.SKULL, 5, "skull_dragon");
		registerItem(Items.CARROT_ON_A_STICK, "carrot_on_a_stick");
		registerItem(Items.NETHER_STAR, "nether_star");
		registerItem(Items.END_CRYSTAL, "end_crystal");
		registerItem(Items.PUMPKIN_PIE, "pumpkin_pie");
		registerItem(Items.FIREWORK_CHARGE, "firework_charge");
		registerItem(Items.COMPARATOR, "comparator");
		registerItem(Items.NETHERBRICK, "netherbrick");
		registerItem(Items.QUARTZ, "quartz");
		registerItem(Items.TNT_MINECART, "tnt_minecart");
		registerItem(Items.HOPPER_MINECART, "hopper_minecart");
		registerItem(Items.ARMOR_STAND, "armor_stand");
		registerItem(Items.IRON_HORSE_ARMOR, "iron_horse_armor");
		registerItem(Items.GOLDEN_HORSE_ARMOR, "golden_horse_armor");
		registerItem(Items.DIAMOND_HORSE_ARMOR, "diamond_horse_armor");
		registerItem(Items.LEAD, "lead");
		registerItem(Items.NAME_TAG, "name_tag");
		itemModelMesher.register(Items.BANNER, stack -> new ModelResourceLocation("banner", "inventory"));
		itemModelMesher.register(Items.BED, stack -> new ModelResourceLocation("bed", "inventory"));
		itemModelMesher.register(Items.SHIELD, stack -> new ModelResourceLocation("shield", "inventory"));
		registerItem(Items.ELYTRA, "elytra");
		registerItem(Items.CHORUS_FRUIT, "chorus_fruit");
		registerItem(Items.CHORUS_FRUIT_POPPED, "chorus_fruit_popped");
		registerItem(Items.SHULKER_SHELL, "shulker_shell");
		registerItem(Items.IRON_NUGGET, "iron_nugget");
		registerItem(Items.RECORD_13, "record_13");
		registerItem(Items.RECORD_CAT, "record_cat");
		registerItem(Items.RECORD_BLOCKS, "record_blocks");
		registerItem(Items.RECORD_CHIRP, "record_chirp");
		registerItem(Items.RECORD_FAR, "record_far");
		registerItem(Items.RECORD_MALL, "record_mall");
		registerItem(Items.RECORD_MELLOHI, "record_mellohi");
		registerItem(Items.RECORD_STAL, "record_stal");
		registerItem(Items.RECORD_STRAD, "record_strad");
		registerItem(Items.RECORD_WARD, "record_ward");
		registerItem(Items.RECORD_11, "record_11");
		registerItem(Items.RECORD_WAIT, "record_wait");
		registerItem(Items.PRISMARINE_SHARD, "prismarine_shard");
		registerItem(Items.PRISMARINE_CRYSTALS, "prismarine_crystals");
		registerItem(Items.KNOWLEDGE_BOOK, "knowledge_book");
		itemModelMesher.register(Items.ENCHANTED_BOOK, stack -> new ModelResourceLocation("enchanted_book", "inventory"));
		itemModelMesher.register(Items.FILLED_MAP, stack -> new ModelResourceLocation("filled_map", "inventory"));
		registerBlock(Blocks.COMMAND_BLOCK, "command_block");
		registerItem(Items.FIREWORKS, "fireworks");
		registerItem(Items.COMMAND_BLOCK_MINECART, "command_block_minecart");
		registerBlock(Blocks.BARRIER, "barrier");
		registerBlock(Blocks.MOB_SPAWNER, "mob_spawner");
		registerItem(Items.WRITTEN_BOOK, "written_book");
		registerBlock(Blocks.BROWN_MUSHROOM_BLOCK, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "brown_mushroom_block");
		registerBlock(Blocks.RED_MUSHROOM_BLOCK, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "red_mushroom_block");
		registerBlock(Blocks.DRAGON_EGG, "dragon_egg");
		registerBlock(Blocks.REPEATING_COMMAND_BLOCK, "repeating_command_block");
		registerBlock(Blocks.CHAIN_COMMAND_BLOCK, "chain_command_block");
		registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.SAVE.getModeId(), "structure_block");
		registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.LOAD.getModeId(), "structure_block");
		registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.CORNER.getModeId(), "structure_block");
		registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.DATA.getModeId(), "structure_block");
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {

		itemModelMesher.rebuildCache();
	}

}
