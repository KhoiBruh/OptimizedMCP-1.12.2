package net.minecraft.client.gui.advancements;

import com.google.common.collect.Lists;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiAdvancement extends Gui {

	private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/advancements/widgets.png");
	private static final Pattern PATTERN = Pattern.compile("(.+) \\S+");
	private final GuiAdvancementTab guiAdvancementTab;
	private final Advancement advancement;
	private final DisplayInfo displayInfo;
	private final String title;
	private final int width;
	private final List<String> description;
	private final Minecraft minecraft;
	private final List<GuiAdvancement> children = Lists.newArrayList();
	private final int x;
	private final int y;
	private GuiAdvancement parent;
	private AdvancementProgress advancementProgress;

	public GuiAdvancement(GuiAdvancementTab p_i47385_1_, Minecraft p_i47385_2_, Advancement p_i47385_3_, DisplayInfo p_i47385_4_) {

		guiAdvancementTab = p_i47385_1_;
		advancement = p_i47385_3_;
		displayInfo = p_i47385_4_;
		minecraft = p_i47385_2_;
		title = p_i47385_2_.fontRenderer.trimStringToWidth(p_i47385_4_.getTitle().getFormattedText(), 163);
		x = MathHelper.floor(p_i47385_4_.getX() * 28F);
		y = MathHelper.floor(p_i47385_4_.getY() * 27F);
		int i = p_i47385_3_.getRequirementCount();
		int j = String.valueOf(i).length();
		int k = i > 1 ? p_i47385_2_.fontRenderer.getStringWidth("  ") + p_i47385_2_.fontRenderer.getStringWidth("0") * j * 2 + p_i47385_2_.fontRenderer.getStringWidth("/") : 0;
		int l = 29 + p_i47385_2_.fontRenderer.getStringWidth(title) + k;
		String s = p_i47385_4_.getDescription().getFormattedText();
		description = findOptimalLines(s, l);

		for (String s1 : description) {
			l = Math.max(l, p_i47385_2_.fontRenderer.getStringWidth(s1));
		}

		width = l + 3 + 5;
	}

	private List<String> findOptimalLines(String p_192995_1_, int p_192995_2_) {

		if (p_192995_1_.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<String> list = minecraft.fontRenderer.listFormattedStringToWidth(p_192995_1_, p_192995_2_);

			if (list.size() < 2) {
				return list;
			} else {
				String s = list.get(0);
				String s1 = list.get(1);
				int i = minecraft.fontRenderer.getStringWidth(s + ' ' + s1.split(" ")[0]);

				if (i - p_192995_2_ <= 10) {
					return minecraft.fontRenderer.listFormattedStringToWidth(p_192995_1_, i);
				} else {
					Matcher matcher = PATTERN.matcher(s);

					if (matcher.matches()) {
						int j = minecraft.fontRenderer.getStringWidth(matcher.group(1));

						if (p_192995_2_ - j <= 10) {
							return minecraft.fontRenderer.listFormattedStringToWidth(p_192995_1_, j);
						}
					}

					return list;
				}
			}
		}
	}

	
	private GuiAdvancement getFirstVisibleParent(Advancement advancementIn) {

		while (true) {
			advancementIn = advancementIn.getParent();

			if (advancementIn == null || advancementIn.getDisplay() != null) {
				break;
			}
		}

		if (advancementIn != null && advancementIn.getDisplay() != null) {
			return guiAdvancementTab.getAdvancementGui(advancementIn);
		} else {
			return null;
		}
	}

	public void drawConnectivity(int p_191819_1_, int p_191819_2_, boolean p_191819_3_) {

		if (parent != null) {
			int i = p_191819_1_ + parent.x + 13;
			int j = p_191819_1_ + parent.x + 26 + 4;
			int k = p_191819_2_ + parent.y + 13;
			int l = p_191819_1_ + x + 13;
			int i1 = p_191819_2_ + y + 13;
			int j1 = p_191819_3_ ? -16777216 : -1;

			if (p_191819_3_) {
				drawHorizontalLine(j, i, k - 1, j1);
				drawHorizontalLine(j + 1, i, k, j1);
				drawHorizontalLine(j, i, k + 1, j1);
				drawHorizontalLine(l, j - 1, i1 - 1, j1);
				drawHorizontalLine(l, j - 1, i1, j1);
				drawHorizontalLine(l, j - 1, i1 + 1, j1);
				drawVerticalLine(j - 1, i1, k, j1);
				drawVerticalLine(j + 1, i1, k, j1);
			} else {
				drawHorizontalLine(j, i, k, j1);
				drawHorizontalLine(l, j, i1, j1);
				drawVerticalLine(j, i1, k, j1);
			}
		}

		for (GuiAdvancement guiadvancement : children) {
			guiadvancement.drawConnectivity(p_191819_1_, p_191819_2_, p_191819_3_);
		}
	}

	public void draw(int p_191817_1_, int p_191817_2_) {

		if (!displayInfo.isHidden() || advancementProgress != null && advancementProgress.isDone()) {
			float f = advancementProgress == null ? 0F : advancementProgress.getPercent();
			AdvancementState advancementstate;

			if (f >= 1F) {
				advancementstate = AdvancementState.OBTAINED;
			} else {
				advancementstate = AdvancementState.UNOBTAINED;
			}

			minecraft.getTextureManager().bindTexture(WIDGETS);
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.enableBlend();
			drawTexturedModalRect(p_191817_1_ + x + 3, p_191817_2_ + y, displayInfo.getFrame().getIcon(), 128 + advancementstate.getId() * 26, 26, 26);
			RenderHelper.enableGUIStandardItemLighting();
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, displayInfo.getIcon(), p_191817_1_ + x + 8, p_191817_2_ + y + 5);
		}

		for (GuiAdvancement guiadvancement : children) {
			guiadvancement.draw(p_191817_1_, p_191817_2_);
		}
	}

	public void getAdvancementProgress(AdvancementProgress advancementProgressIn) {

		advancementProgress = advancementProgressIn;
	}

	public void addGuiAdvancement(GuiAdvancement guiAdvancementIn) {

		children.add(guiAdvancementIn);
	}

	public void drawHover(int p_191821_1_, int p_191821_2_, float p_191821_3_, int p_191821_4_, int p_191821_5_) {

		boolean flag = p_191821_4_ + p_191821_1_ + x + width + 26 >= guiAdvancementTab.getScreen().width;
		String s = advancementProgress == null ? null : advancementProgress.getProgressText();
		int i = s == null ? 0 : minecraft.fontRenderer.getStringWidth(s);
		boolean flag1 = 113 - p_191821_2_ - y - 26 <= 6 + description.size() * minecraft.fontRenderer.FONT_HEIGHT;
		float f = advancementProgress == null ? 0F : advancementProgress.getPercent();
		int j = MathHelper.floor(f * (float) width);
		AdvancementState advancementstate;
		AdvancementState advancementstate1;
		AdvancementState advancementstate2;

		if (f >= 1F) {
			j = width / 2;
			advancementstate = AdvancementState.OBTAINED;
			advancementstate1 = AdvancementState.OBTAINED;
			advancementstate2 = AdvancementState.OBTAINED;
		} else if (j < 2) {
			j = width / 2;
			advancementstate = AdvancementState.UNOBTAINED;
			advancementstate1 = AdvancementState.UNOBTAINED;
			advancementstate2 = AdvancementState.UNOBTAINED;
		} else if (j > width - 2) {
			j = width / 2;
			advancementstate = AdvancementState.OBTAINED;
			advancementstate1 = AdvancementState.OBTAINED;
			advancementstate2 = AdvancementState.UNOBTAINED;
		} else {
			advancementstate = AdvancementState.OBTAINED;
			advancementstate1 = AdvancementState.UNOBTAINED;
			advancementstate2 = AdvancementState.UNOBTAINED;
		}

		int k = width - j;
		minecraft.getTextureManager().bindTexture(WIDGETS);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableBlend();
		int l = p_191821_2_ + y;
		int i1;

		if (flag) {
			i1 = p_191821_1_ + x - width + 26 + 6;
		} else {
			i1 = p_191821_1_ + x;
		}

		int j1 = 32 + description.size() * minecraft.fontRenderer.FONT_HEIGHT;

		if (!description.isEmpty()) {
			if (flag1) {
				render9Sprite(i1, l + 26 - j1, width, j1, 10, 200, 26, 0, 52);
			} else {
				render9Sprite(i1, l, width, j1, 10, 200, 26, 0, 52);
			}
		}

		drawTexturedModalRect(i1, l, 0, advancementstate.getId() * 26, j, 26);
		drawTexturedModalRect(i1 + j, l, 200 - k, advancementstate1.getId() * 26, k, 26);
		drawTexturedModalRect(p_191821_1_ + x + 3, p_191821_2_ + y, displayInfo.getFrame().getIcon(), 128 + advancementstate2.getId() * 26, 26, 26);

		if (flag) {
			minecraft.fontRenderer.drawString(title, (float) (i1 + 5), (float) (p_191821_2_ + y + 9), -1, true);

			if (s != null) {
				minecraft.fontRenderer.drawString(s, (float) (p_191821_1_ + x - i), (float) (p_191821_2_ + y + 9), -1, true);
			}
		} else {
			minecraft.fontRenderer.drawString(title, (float) (p_191821_1_ + x + 32), (float) (p_191821_2_ + y + 9), -1, true);

			if (s != null) {
				minecraft.fontRenderer.drawString(s, (float) (p_191821_1_ + x + width - i - 5), (float) (p_191821_2_ + y + 9), -1, true);
			}
		}

		if (flag1) {
			for (int k1 = 0; k1 < description.size(); ++k1) {
				minecraft.fontRenderer.drawString(description.get(k1), (float) (i1 + 5), (float) (l + 26 - j1 + 7 + k1 * minecraft.fontRenderer.FONT_HEIGHT), -5592406, false);
			}
		} else {
			for (int l1 = 0; l1 < description.size(); ++l1) {
				minecraft.fontRenderer.drawString(description.get(l1), (float) (i1 + 5), (float) (p_191821_2_ + y + 9 + 17 + l1 * minecraft.fontRenderer.FONT_HEIGHT), -5592406, false);
			}
		}

		RenderHelper.enableGUIStandardItemLighting();
		minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, displayInfo.getIcon(), p_191821_1_ + x + 8, p_191821_2_ + y + 5);
	}

	protected void render9Sprite(int p_192994_1_, int p_192994_2_, int p_192994_3_, int p_192994_4_, int p_192994_5_, int p_192994_6_, int p_192994_7_, int p_192994_8_, int p_192994_9_) {

		drawTexturedModalRect(p_192994_1_, p_192994_2_, p_192994_8_, p_192994_9_, p_192994_5_, p_192994_5_);
		renderRepeating(p_192994_1_ + p_192994_5_, p_192994_2_, p_192994_3_ - p_192994_5_ - p_192994_5_, p_192994_5_, p_192994_8_ + p_192994_5_, p_192994_9_, p_192994_6_ - p_192994_5_ - p_192994_5_, p_192994_7_);
		drawTexturedModalRect(p_192994_1_ + p_192994_3_ - p_192994_5_, p_192994_2_, p_192994_8_ + p_192994_6_ - p_192994_5_, p_192994_9_, p_192994_5_, p_192994_5_);
		drawTexturedModalRect(p_192994_1_, p_192994_2_ + p_192994_4_ - p_192994_5_, p_192994_8_, p_192994_9_ + p_192994_7_ - p_192994_5_, p_192994_5_, p_192994_5_);
		renderRepeating(p_192994_1_ + p_192994_5_, p_192994_2_ + p_192994_4_ - p_192994_5_, p_192994_3_ - p_192994_5_ - p_192994_5_, p_192994_5_, p_192994_8_ + p_192994_5_, p_192994_9_ + p_192994_7_ - p_192994_5_, p_192994_6_ - p_192994_5_ - p_192994_5_, p_192994_7_);
		drawTexturedModalRect(p_192994_1_ + p_192994_3_ - p_192994_5_, p_192994_2_ + p_192994_4_ - p_192994_5_, p_192994_8_ + p_192994_6_ - p_192994_5_, p_192994_9_ + p_192994_7_ - p_192994_5_, p_192994_5_, p_192994_5_);
		renderRepeating(p_192994_1_, p_192994_2_ + p_192994_5_, p_192994_5_, p_192994_4_ - p_192994_5_ - p_192994_5_, p_192994_8_, p_192994_9_ + p_192994_5_, p_192994_6_, p_192994_7_ - p_192994_5_ - p_192994_5_);
		renderRepeating(p_192994_1_ + p_192994_5_, p_192994_2_ + p_192994_5_, p_192994_3_ - p_192994_5_ - p_192994_5_, p_192994_4_ - p_192994_5_ - p_192994_5_, p_192994_8_ + p_192994_5_, p_192994_9_ + p_192994_5_, p_192994_6_ - p_192994_5_ - p_192994_5_, p_192994_7_ - p_192994_5_ - p_192994_5_);
		renderRepeating(p_192994_1_ + p_192994_3_ - p_192994_5_, p_192994_2_ + p_192994_5_, p_192994_5_, p_192994_4_ - p_192994_5_ - p_192994_5_, p_192994_8_ + p_192994_6_ - p_192994_5_, p_192994_9_ + p_192994_5_, p_192994_6_, p_192994_7_ - p_192994_5_ - p_192994_5_);
	}

	protected void renderRepeating(int p_192993_1_, int p_192993_2_, int p_192993_3_, int p_192993_4_, int p_192993_5_, int p_192993_6_, int p_192993_7_, int p_192993_8_) {

		for (int i = 0; i < p_192993_3_; i += p_192993_7_) {
			int j = p_192993_1_ + i;
			int k = Math.min(p_192993_7_, p_192993_3_ - i);

			for (int l = 0; l < p_192993_4_; l += p_192993_8_) {
				int i1 = p_192993_2_ + l;
				int j1 = Math.min(p_192993_8_, p_192993_4_ - l);
				drawTexturedModalRect(j, i1, p_192993_5_, p_192993_6_, k, j1);
			}
		}
	}

	public boolean isMouseOver(int p_191816_1_, int p_191816_2_, int p_191816_3_, int p_191816_4_) {

		if (!displayInfo.isHidden() || advancementProgress != null && advancementProgress.isDone()) {
			int i = p_191816_1_ + x;
			int j = i + 26;
			int k = p_191816_2_ + y;
			int l = k + 26;
			return p_191816_3_ >= i && p_191816_3_ <= j && p_191816_4_ >= k && p_191816_4_ <= l;
		} else {
			return false;
		}
	}

	public void attachToParent() {

		if (parent == null && advancement.getParent() != null) {
			parent = getFirstVisibleParent(advancement);

			if (parent != null) {
				parent.addGuiAdvancement(this);
			}
		}
	}

	public int getY() {

		return y;
	}

	public int getX() {

		return x;
	}

}
