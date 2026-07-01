package net.minecraft.client.gui.menu;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.component.Button;
import net.minecraft.client.gui.component.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;

import java.io.IOException;

public class CreateFlatWorldScreen extends Screen {

	private final CreateWorldScreen createWorldGui;
	private FlatGeneratorInfo generatorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();

	/**
	 * The title given to the flat world currently in creation
	 */
	private String flatWorldTitle;

	/**
	 * The text used to identify the material for a layer
	 */
	private String materialText;

	/**
	 * The text used to identify the height of a layer
	 */
	private String heightText;
	private CreateFlatWorldScreen.Details createFlatWorldListSlotGui;

	/**
	 * The (unused and permenantly hidden) add layer button
	 */
	private Button addLayerButton;

	/**
	 * The (unused and permenantly hidden) edit layer button
	 */
	private Button editLayerButton;

	/**
	 * The remove layer button
	 */
	private Button removeLayerButton;

	public CreateFlatWorldScreen(CreateWorldScreen createWorldGuiIn, String preset) {

		createWorldGui = createWorldGuiIn;
		setPreset(preset);
	}

	/**
	 * Gets the superflat preset in the text format described on the Superflat article on the Minecraft Wiki
	 */
	public String getPreset() {

		return generatorInfo.toString();
	}

	/**
	 * Sets the superflat preset. Invalid or null values will result in the default superflat preset being used.
	 */
	public void setPreset(String preset) {

		generatorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(preset);
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void init() {

		buttons.clear();
		flatWorldTitle = I18n.format("createWorld.customize.flat.title");
		materialText = I18n.format("createWorld.customize.flat.tile");
		heightText = I18n.format("createWorld.customize.flat.height");
		createFlatWorldListSlotGui = new CreateFlatWorldScreen.Details();
		addLayerButton = addButton(new Button(2, width / 2 - 154, height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
		editLayerButton = addButton(new Button(3, width / 2 - 50, height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
		removeLayerButton = addButton(new Button(4, width / 2 - 155, height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
		buttons.add(new Button(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		buttons.add(new Button(5, width / 2 + 5, height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
		buttons.add(new Button(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		addLayerButton.visible = false;
		editLayerButton.visible = false;
		generatorInfo.updateLayers();
		onLayersChanged();
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouse() throws IOException {

		super.handleMouse();
		createFlatWorldListSlotGui.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void action(Button button) {

		int i = generatorInfo.getFlatLayers().size() - createFlatWorldListSlotGui.selectedLayer - 1;

		if (button.id == 1) {
			mc.displayScreen(createWorldGui);
		} else if (button.id == 0) {
			createWorldGui.chunkProviderSettingsJson = getPreset();
			mc.displayScreen(createWorldGui);
		} else if (button.id == 5) {
			mc.displayScreen(new FlatPresetsScreen(this));
		} else if (button.id == 4 && hasSelectedLayer()) {
			generatorInfo.getFlatLayers().remove(i);
			createFlatWorldListSlotGui.selectedLayer = Math.min(createFlatWorldListSlotGui.selectedLayer, generatorInfo.getFlatLayers().size() - 1);
		}

		generatorInfo.updateLayers();
		onLayersChanged();
	}

	/**
	 * Would update whether or not the edit and remove buttons are enabled, but is currently disabled and always
	 * disables the buttons (which are invisible anyways)
	 */
	public void onLayersChanged() {

		boolean flag = hasSelectedLayer();
		removeLayerButton.enabled = flag;
		editLayerButton.enabled = flag;
		editLayerButton.enabled = false;
		addLayerButton.enabled = false;
	}

	/**
	 * Returns whether there is a valid layer selection
	 */
	private boolean hasSelectedLayer() {

		return createFlatWorldListSlotGui.selectedLayer > -1 && createFlatWorldListSlotGui.selectedLayer < generatorInfo.getFlatLayers().size();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void draw(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, flatWorldTitle, width / 2, 8, 16777215);
		int i = width / 2 - 92 - 16;
		drawString(fontRenderer, materialText, i, 32, 16777215);
		drawString(fontRenderer, heightText, i + 2 + 213 - fontRenderer.getStringWidth(heightText), 32, 16777215);
		super.draw(mouseX, mouseY, partialTicks);
	}

	class Details extends GuiSlot {

		public int selectedLayer = -1;

		public Details() {

			super(CreateFlatWorldScreen.this.mc, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24);
		}

		private void drawItem(int x, int z, ItemStack itemToDraw) {

			drawItemBackground(x + 1, z + 1);
			GLS.enableRescaleNormal();

			if (!itemToDraw.isEmpty()) {
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemIntoGUI(itemToDraw, x + 2, z + 2);
				RenderHelper.disableStandardItemLighting();
			}

			GLS.disableRescaleNormal();
		}

		private void drawItemBackground(int x, int y) {

			drawItemBackground(x, y, 0, 0);
		}

		private void drawItemBackground(int x, int z, int textureX, int textureY) {

			GLS.color(1F, 1F, 1F, 1F);
			mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
			float f = 0.0078125F;
			float f1 = 0.0078125F;
			int i = 18;
			int j = 18;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(x, z + 18, zLevel).tex((float) (textureX) * 0.0078125F, (float) (textureY + 18) * 0.0078125F).endVertex();
			bufferbuilder.pos(x + 18, z + 18, zLevel).tex((float) (textureX + 18) * 0.0078125F, (float) (textureY + 18) * 0.0078125F).endVertex();
			bufferbuilder.pos(x + 18, z, zLevel).tex((float) (textureX + 18) * 0.0078125F, (float) (textureY) * 0.0078125F).endVertex();
			bufferbuilder.pos(x, z, zLevel).tex((float) (textureX) * 0.0078125F, (float) (textureY) * 0.0078125F).endVertex();
			tessellator.draw();
		}

		protected int getSize() {

			return generatorInfo.getFlatLayers().size();
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {

			selectedLayer = slotIndex;
			onLayersChanged();
		}

		protected boolean isSelected(int slotIndex) {

			return slotIndex == selectedLayer;
		}

		protected void drawBackground() {

		}

		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {

			FlatLayerInfo flatlayerinfo = generatorInfo.getFlatLayers().get(generatorInfo.getFlatLayers().size() - slotIndex - 1);
			IBlockState iblockstate = flatlayerinfo.getLayerMaterial();
			Block block = iblockstate.getBlock();
			Item item = Item.getItemFromBlock(block);

			if (item == Items.AIR) {
				if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
					if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
						item = Items.LAVA_BUCKET;
					}
				} else {
					item = Items.WATER_BUCKET;
				}
			}

			ItemStack itemstack = new ItemStack(item, 1, item.getHasSubtypes() ? block.getMetaFromState(iblockstate) : 0);
			String s = item.getItemStackDisplayName(itemstack);
			drawItem(xPos, yPos, itemstack);
			fontRenderer.drawString(s, xPos + 18 + 5, yPos + 3, 16777215);
			String s1;

			if (slotIndex == 0) {
				s1 = I18n.format("createWorld.customize.flat.layer.top", flatlayerinfo.getLayerCount());
			} else if (slotIndex == generatorInfo.getFlatLayers().size() - 1) {
				s1 = I18n.format("createWorld.customize.flat.layer.bottom", flatlayerinfo.getLayerCount());
			} else {
				s1 = I18n.format("createWorld.customize.flat.layer", flatlayerinfo.getLayerCount());
			}

			fontRenderer.drawString(s1, xPos + 2 + 213 - fontRenderer.getStringWidth(s1), yPos + 3, 16777215);
		}

		protected int getScrollBarX() {

			return width - 70;
		}

	}

}
