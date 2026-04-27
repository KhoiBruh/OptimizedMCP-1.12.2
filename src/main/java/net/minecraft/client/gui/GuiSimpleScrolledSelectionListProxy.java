package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.util.math.MathHelper;

public class GuiSimpleScrolledSelectionListProxy extends GuiSlot {

	private final RealmsSimpleScrolledSelectionList realmsScrolledSelectionList;

	public GuiSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList realmsScrolledSelectionListIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {

		super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
		realmsScrolledSelectionList = realmsScrolledSelectionListIn;
	}

	protected int getSize() {

		return realmsScrolledSelectionList.getItemCount();
	}

	/**
	 * The element in the slot that was clicked, boolean for whether it was double clicked or not
	 */
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {

		realmsScrolledSelectionList.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slotIndex) {

		return realmsScrolledSelectionList.isSelectedItem(slotIndex);
	}

	protected void drawBackground() {

		realmsScrolledSelectionList.renderBackground();
	}

	protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {

		realmsScrolledSelectionList.renderItem(slotIndex, xPos, yPos, heightIn, mouseXIn, mouseYIn);
	}

	public int getWidth() {

		return width;
	}

	public int getMouseY() {

		return mouseY;
	}

	public int getMouseX() {

		return mouseX;
	}

	/**
	 * Return the height of the content being scrolled
	 */
	protected int getContentHeight() {

		return realmsScrolledSelectionList.getMaxPosition();
	}

	protected int getScrollBarX() {

		return realmsScrolledSelectionList.getScrollbarPosition();
	}

	public void handleMouseInput() {

		super.handleMouseInput();
	}

	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {

		if (visible) {
			mouseX = mouseXIn;
			mouseY = mouseYIn;
			drawBackground();
			int i = getScrollBarX();
			int j = i + 6;
			bindAmountScrolled();
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			int k = left + width / 2 - getListWidth() / 2 + 2;
			int l = top + 4 - (int) amountScrolled;

			if (hasListHeader) {
				drawListHeader(k, l, tessellator);
			}

			drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);
			GlStateManager.disableDepth();
			overlayBackground(0, top, 255, 255);
			overlayBackground(bottom, height, 255, 255);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
			GlStateManager.disableAlpha();
			GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture2D();
			int i1 = getMaxScroll();

			if (i1 > 0) {
				int j1 = (bottom - top) * (bottom - top) / getContentHeight();
				j1 = MathHelper.clamp(j1, 32, bottom - top - 8);
				int k1 = (int) amountScrolled * (bottom - top - j1) / i1 + top;

				if (k1 < top) {
					k1 = top;
				}

				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(i, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(j, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(j, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(i, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(i, k1 + j1, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(j, k1 + j1, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(j, k1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(i, k1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				tessellator.draw();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(i, k1 + j1 - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				bufferbuilder.pos(j - 1, k1 + j1 - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				bufferbuilder.pos(j - 1, k1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				bufferbuilder.pos(i, k1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				tessellator.draw();
			}

			renderDecorations(mouseXIn, mouseYIn);
			GlStateManager.enableTexture2D();
			GlStateManager.shadeModel(7424);
			GlStateManager.enableAlpha();
			GlStateManager.disableBlend();
		}
	}

}
