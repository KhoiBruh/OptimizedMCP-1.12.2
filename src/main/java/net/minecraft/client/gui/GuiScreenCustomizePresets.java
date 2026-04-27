package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiScreenCustomizePresets extends GuiScreen {

	private static final List<GuiScreenCustomizePresets.Info> PRESETS = Lists.newArrayList();
	private GuiScreenCustomizePresets.ListPreset list;
	private GuiButton select;
	private GuiTextField export;
	private final GuiCustomizeWorldScreen parent;
	protected String title = "Customize World Presets";
	private String shareText;
	private String listText;

	public GuiScreenCustomizePresets(GuiCustomizeWorldScreen parentIn) {

		parent = parentIn;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {

		buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		title = I18n.format("createWorld.customize.custom.presets.title");
		shareText = I18n.format("createWorld.customize.presets.share");
		listText = I18n.format("createWorld.customize.presets.list");
		export = new GuiTextField(2, fontRenderer, 50, 40, width - 100, 20);
		list = new GuiScreenCustomizePresets.ListPreset();
		export.setMaxStringLength(2000);
		export.setText(parent.saveValues());
		select = addButton(new GuiButton(0, width / 2 - 102, height - 27, 100, 20, I18n.format("createWorld.customize.presets.select")));
		buttonList.add(new GuiButton(1, width / 2 + 3, height - 27, 100, 20, I18n.format("gui.cancel")));
		updateButtonValidity();
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		list.handleMouseInput();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {

		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		export.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if (!export.textboxKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {

		switch (button.id) {
			case 0:
				parent.loadValues(export.getText());
				mc.displayGuiScreen(parent);
				break;

			case 1:
				mc.displayGuiScreen(parent);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawDefaultBackground();
		list.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, title, width / 2, 8, 16777215);
		drawString(fontRenderer, shareText, 50, 30, 10526880);
		drawString(fontRenderer, listText, 50, 70, 10526880);
		export.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		export.updateCursorCounter();
		super.updateScreen();
	}

	public void updateButtonValidity() {

		select.enabled = hasValidSelection();
	}

	private boolean hasValidSelection() {

		return list.selected > -1 && list.selected < PRESETS.size() || export.getText().length() > 1;
	}

	static {
		ChunkGeneratorSettings.Factory chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{ \"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":8.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":0.5, \"biomeScaleWeight\":2.0, \"biomeScaleOffset\":0.375, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":255 }");
		ResourceLocation resourcelocation = new ResourceLocation("textures/gui/presets/water.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.waterWorld"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":3000.0, \"heightScale\":6000.0, \"upperLimitScale\":250.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/isles.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.isleLand"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":5.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":1.0, \"biomeScaleWeight\":4.0, \"biomeScaleOffset\":1.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/delight.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.caveDelight"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":738.41864, \"heightScale\":157.69133, \"upperLimitScale\":801.4267, \"lowerLimitScale\":1254.1643, \"depthNoiseScaleX\":374.93652, \"depthNoiseScaleZ\":288.65228, \"depthNoiseScaleExponent\":1.2092624, \"mainNoiseScaleX\":1355.9908, \"mainNoiseScaleY\":745.5343, \"mainNoiseScaleZ\":1183.464, \"baseSize\":1.8758626, \"stretchY\":1.7137525, \"biomeDepthWeight\":1.7553768, \"biomeDepthOffset\":3.4701107, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":2.535211, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/madness.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.mountains"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":1000.0, \"mainNoiseScaleY\":3000.0, \"mainNoiseScaleZ\":1000.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":20 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/drought.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.drought"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":2.0, \"lowerLimitScale\":64.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":6 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/chaos.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.caveChaos"), resourcelocation, chunkgeneratorsettings$factory));
		chunkgeneratorsettings$factory = ChunkGeneratorSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":true, \"seaLevel\":40 }");
		resourcelocation = new ResourceLocation("textures/gui/presets/luck.png");
		PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.goodLuck"), resourcelocation, chunkgeneratorsettings$factory));
	}

	static class Info {

		public String name;
		public ResourceLocation texture;
		public ChunkGeneratorSettings.Factory settings;

		public Info(String nameIn, ResourceLocation textureIn, ChunkGeneratorSettings.Factory settingsIn) {

			name = nameIn;
			texture = textureIn;
			settings = settingsIn;
		}

	}

	class ListPreset extends GuiSlot {

		public int selected = -1;

		public ListPreset() {

			super(GuiScreenCustomizePresets.this.mc, GuiScreenCustomizePresets.this.width, GuiScreenCustomizePresets.this.height, 80, GuiScreenCustomizePresets.this.height - 32, 38);
		}

		protected int getSize() {

			return GuiScreenCustomizePresets.PRESETS.size();
		}

		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {

			selected = slotIndex;
			updateButtonValidity();
			export.setText((GuiScreenCustomizePresets.PRESETS.get(list.selected)).settings.toString());
		}

		protected boolean isSelected(int slotIndex) {

			return slotIndex == selected;
		}

		protected void drawBackground() {

		}

		private void blitIcon(int p_178051_1_, int p_178051_2_, ResourceLocation texture) {

			int i = p_178051_1_ + 5;
			drawHorizontalLine(i - 1, i + 32, p_178051_2_ - 1, -2039584);
			drawHorizontalLine(i - 1, i + 32, p_178051_2_ + 32, -6250336);
			drawVerticalLine(i - 1, p_178051_2_ - 1, p_178051_2_ + 32, -2039584);
			drawVerticalLine(i + 32, p_178051_2_ - 1, p_178051_2_ + 32, -6250336);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(texture);
			int j = 32;
			int k = 32;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(i, p_178051_2_ + 32, 0.0D).tex(0.0D, 1.0D).endVertex();
			bufferbuilder.pos(i + 32, p_178051_2_ + 32, 0.0D).tex(1.0D, 1.0D).endVertex();
			bufferbuilder.pos(i + 32, p_178051_2_, 0.0D).tex(1.0D, 0.0D).endVertex();
			bufferbuilder.pos(i, p_178051_2_, 0.0D).tex(0.0D, 0.0D).endVertex();
			tessellator.draw();
		}

		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {

			GuiScreenCustomizePresets.Info guiscreencustomizepresets$info = GuiScreenCustomizePresets.PRESETS.get(slotIndex);
			blitIcon(xPos, yPos, guiscreencustomizepresets$info.texture);
			fontRenderer.drawString(guiscreencustomizepresets$info.name, xPos + 32 + 10, yPos + 14, 16777215);
		}

	}

}
