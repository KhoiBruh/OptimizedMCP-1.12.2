package net.minecraft.client.gui.toasts;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;

public class SystemToast implements IToast {

	private final SystemToast.Type type;
	private String title;
	private String subtitle;
	private long firstDrawTime;
	private boolean newDisplay;

	public SystemToast(SystemToast.Type typeIn, ITextComponent titleComponent, ITextComponent subtitleComponent) {

		type = typeIn;
		title = titleComponent.getUnformattedText();
		subtitle = subtitleComponent == null ? null : subtitleComponent.getUnformattedText();
	}

	public static void addOrUpdate(GuiToast p_193657_0_, SystemToast.Type p_193657_1_, ITextComponent p_193657_2_, ITextComponent p_193657_3_) {

		SystemToast systemtoast = p_193657_0_.getToast(SystemToast.class, p_193657_1_);

		if (systemtoast == null) {
			p_193657_0_.add(new SystemToast(p_193657_1_, p_193657_2_, p_193657_3_));
		} else {
			systemtoast.setDisplayedText(p_193657_2_, p_193657_3_);
		}
	}

	public IToast.Visibility draw(GuiToast toastGui, long delta) {

		if (newDisplay) {
			firstDrawTime = delta;
			newDisplay = false;
		}

		toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1F, 1F, 1F);
		toastGui.drawTexturedModalRect(0, 0, 0, 64, 160, 32);

		if (subtitle == null) {
			toastGui.getMinecraft().fontRenderer.drawString(title, 18, 12, -256);
		} else {
			toastGui.getMinecraft().fontRenderer.drawString(title, 18, 7, -256);
			toastGui.getMinecraft().fontRenderer.drawString(subtitle, 18, 18, -1);
		}

		return delta - firstDrawTime < 5000L ? IToast.Visibility.SHOW : IToast.Visibility.HIDE;
	}

	public void setDisplayedText(ITextComponent titleComponent, ITextComponent subtitleComponent) {

		title = titleComponent.getUnformattedText();
		subtitle = subtitleComponent == null ? null : subtitleComponent.getUnformattedText();
		newDisplay = true;
	}

	public SystemToast.Type getType() {

		return type;
	}

	public enum Type {
		TUTORIAL_HINT,
		NARRATOR_TOGGLE
	}

}
