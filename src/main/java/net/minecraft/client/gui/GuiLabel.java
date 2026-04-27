package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class GuiLabel extends Gui {

	private final List<String> labels;
	private final boolean labelBgEnabled;
	private final int textColor;
	private final int backColor;
	private final int ulColor;
	private final int brColor;
	private final FontRenderer fontRenderer;
	private final int border;
	public int x;
	public int y;
	public int id;
	public boolean visible = true;
	protected int width = 200;
	protected int height = 20;
	private boolean centered;

	public GuiLabel(FontRenderer fontRendererObj, int p_i45540_2_, int p_i45540_3_, int p_i45540_4_, int p_i45540_5_, int p_i45540_6_, int p_i45540_7_) {

		fontRenderer = fontRendererObj;
		id = p_i45540_2_;
		x = p_i45540_3_;
		y = p_i45540_4_;
		width = p_i45540_5_;
		height = p_i45540_6_;
		labels = Lists.newArrayList();
		centered = false;
		labelBgEnabled = false;
		textColor = p_i45540_7_;
		backColor = -1;
		ulColor = -1;
		brColor = -1;
		border = 0;
	}

	public void addLine(String p_175202_1_) {

		labels.add(I18n.format(p_175202_1_));
	}

	/**
	 * Sets the Label to be centered
	 */
	public GuiLabel setCentered() {

		centered = true;
		return this;
	}

	public void drawLabel(Minecraft mc, int mouseX, int mouseY) {

		if (visible) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			drawLabelBackground(mc, mouseX, mouseY);
			int i = y + height / 2 + border / 2;
			int j = i - labels.size() * 10 / 2;

			for (int k = 0; k < labels.size(); ++k) {
				if (centered) {
					drawCenteredString(fontRenderer, labels.get(k), x + width / 2, j + k * 10, textColor);
				} else {
					drawString(fontRenderer, labels.get(k), x, j + k * 10, textColor);
				}
			}
		}
	}

	protected void drawLabelBackground(Minecraft mcIn, int mouseX, int mouseY) {

		if (labelBgEnabled) {
			int i = width + border * 2;
			int j = height + border * 2;
			int k = x - border;
			int l = y - border;
			drawRect(k, l, k + i, l + j, backColor);
			drawHorizontalLine(k, k + i, l, ulColor);
			drawHorizontalLine(k, k + i, l + j, brColor);
			drawVerticalLine(k, l, l + j, ulColor);
			drawVerticalLine(k + i, l, l + j, brColor);
		}
	}

}
