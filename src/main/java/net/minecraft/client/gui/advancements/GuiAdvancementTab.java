package net.minecraft.client.gui.advancements;

import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Map;

public class GuiAdvancementTab extends Gui {

	private final Minecraft minecraft;
	private final GuiScreenAdvancements screen;
	private final AdvancementTabType type;
	private final int index;
	private final Advancement advancement;
	private final DisplayInfo display;
	private final ItemStack icon;
	private final String title;
	private final GuiAdvancement root;
	private final Map<Advancement, GuiAdvancement> guis = Maps.newLinkedHashMap();
	private int scrollX;
	private int scrollY;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private float fade;
	private boolean centered;

	public GuiAdvancementTab(Minecraft p_i47589_1_, GuiScreenAdvancements p_i47589_2_, AdvancementTabType p_i47589_3_, int p_i47589_4_, Advancement p_i47589_5_, DisplayInfo p_i47589_6_) {

		minecraft = p_i47589_1_;
		screen = p_i47589_2_;
		type = p_i47589_3_;
		index = p_i47589_4_;
		advancement = p_i47589_5_;
		display = p_i47589_6_;
		icon = p_i47589_6_.getIcon();
		title = p_i47589_6_.getTitle().getFormattedText();
		root = new GuiAdvancement(this, p_i47589_1_, p_i47589_5_, p_i47589_6_);
		addGuiAdvancement(root, p_i47589_5_);
	}

	@Nullable
	public static GuiAdvancementTab create(Minecraft p_193936_0_, GuiScreenAdvancements p_193936_1_, int p_193936_2_, Advancement p_193936_3_) {

		if (p_193936_3_.getDisplay() == null) {
			return null;
		} else {
			for (AdvancementTabType advancementtabtype : AdvancementTabType.values()) {
				if (p_193936_2_ < advancementtabtype.getMax()) {
					return new GuiAdvancementTab(p_193936_0_, p_193936_1_, advancementtabtype, p_193936_2_, p_193936_3_, p_193936_3_.getDisplay());
				}

				p_193936_2_ -= advancementtabtype.getMax();
			}

			return null;
		}
	}

	public Advancement getAdvancement() {

		return advancement;
	}

	public String getTitle() {

		return title;
	}

	public void drawTab(int p_191798_1_, int p_191798_2_, boolean p_191798_3_) {

		type.draw(this, p_191798_1_, p_191798_2_, p_191798_3_, index);
	}

	public void drawIcon(int p_191796_1_, int p_191796_2_, RenderItem p_191796_3_) {

		type.drawIcon(p_191796_1_, p_191796_2_, index, p_191796_3_, icon);
	}

	public void drawContents() {

		if (!centered) {
			scrollX = 117 - (maxX + minX) / 2;
			scrollY = 56 - (maxY + minY) / 2;
			centered = true;
		}

		GlStateManager.depthFunc(518);
		drawRect(0, 0, 234, 113, -16777216);
		GlStateManager.depthFunc(515);
		ResourceLocation resourcelocation = display.getBackground();

		if (resourcelocation != null) {
			minecraft.getTextureManager().bindTexture(resourcelocation);
		} else {
			minecraft.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		int i = scrollX % 16;
		int j = scrollY % 16;

		for (int k = -1; k <= 15; ++k) {
			for (int l = -1; l <= 8; ++l) {
				drawModalRectWithCustomSizedTexture(i + 16 * k, j + 16 * l, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
			}
		}

		root.drawConnectivity(scrollX, scrollY, true);
		root.drawConnectivity(scrollX, scrollY, false);
		root.draw(scrollX, scrollY);
	}

	public void drawToolTips(int p_192991_1_, int p_192991_2_, int p_192991_3_, int p_192991_4_) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 200.0F);
		drawRect(0, 0, 234, 113, MathHelper.floor(fade * 255.0F) << 24);
		boolean flag = false;

		if (p_192991_1_ > 0 && p_192991_1_ < 234 && p_192991_2_ > 0 && p_192991_2_ < 113) {
			for (GuiAdvancement guiadvancement : guis.values()) {
				if (guiadvancement.isMouseOver(scrollX, scrollY, p_192991_1_, p_192991_2_)) {
					flag = true;
					guiadvancement.drawHover(scrollX, scrollY, fade, p_192991_3_, p_192991_4_);
					break;
				}
			}
		}

		GlStateManager.popMatrix();

		if (flag) {
			fade = MathHelper.clamp(fade + 0.02F, 0.0F, 0.3F);
		} else {
			fade = MathHelper.clamp(fade - 0.04F, 0.0F, 1.0F);
		}
	}

	public boolean isMouseOver(int p_191793_1_, int p_191793_2_, int p_191793_3_, int p_191793_4_) {

		return type.isMouseOver(p_191793_1_, p_191793_2_, index, p_191793_3_, p_191793_4_);
	}

	public void scroll(int p_191797_1_, int p_191797_2_) {

		if (maxX - minX > 234) {
			scrollX = MathHelper.clamp(scrollX + p_191797_1_, -(maxX - 234), 0);
		}

		if (maxY - minY > 113) {
			scrollY = MathHelper.clamp(scrollY + p_191797_2_, -(maxY - 113), 0);
		}
	}

	public void addAdvancement(Advancement p_191800_1_) {

		if (p_191800_1_.getDisplay() != null) {
			GuiAdvancement guiadvancement = new GuiAdvancement(this, minecraft, p_191800_1_, p_191800_1_.getDisplay());
			addGuiAdvancement(guiadvancement, p_191800_1_);
		}
	}

	private void addGuiAdvancement(GuiAdvancement p_193937_1_, Advancement p_193937_2_) {

		guis.put(p_193937_2_, p_193937_1_);
		int i = p_193937_1_.getX();
		int j = i + 28;
		int k = p_193937_1_.getY();
		int l = k + 27;
		minX = Math.min(minX, i);
		maxX = Math.max(maxX, j);
		minY = Math.min(minY, k);
		maxY = Math.max(maxY, l);

		for (GuiAdvancement guiadvancement : guis.values()) {
			guiadvancement.attachToParent();
		}
	}

	@Nullable
	public GuiAdvancement getAdvancementGui(Advancement p_191794_1_) {

		return guis.get(p_191794_1_);
	}

	public GuiScreenAdvancements getScreen() {

		return screen;
	}

}
