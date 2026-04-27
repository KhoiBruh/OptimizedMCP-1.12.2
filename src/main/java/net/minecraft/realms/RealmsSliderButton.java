package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class RealmsSliderButton extends RealmsButton {

	private final float minValue;
	private final float maxValue;
	public float value;
	public boolean sliding;
	private int steps;

	public RealmsSliderButton(int buttonId, int x, int y, int width, int maxValueIn, int p_i1056_6_) {

		this(buttonId, x, y, width, p_i1056_6_, 0, 1.0F, (float) maxValueIn);
	}

	public RealmsSliderButton(int buttonId, int x, int y, int width, int p_i1057_5_, int valueIn, float minValueIn, float maxValueIn) {

		super(buttonId, x, y, width, 20, "");
		value = 1.0F;
		minValue = minValueIn;
		maxValue = maxValueIn;
		value = toPct((float) valueIn);
		getProxy().displayString = getMessage();
	}

	public String getMessage() {

		return "";
	}

	public float toPct(float p_toPct_1_) {

		return MathHelper.clamp((clamp(p_toPct_1_) - minValue) / (maxValue - minValue), 0.0F, 1.0F);
	}

	public float toValue(float p_toValue_1_) {

		return clamp(minValue + (maxValue - minValue) * MathHelper.clamp(p_toValue_1_, 0.0F, 1.0F));
	}

	public float clamp(float p_clamp_1_) {

		p_clamp_1_ = clampSteps(p_clamp_1_);
		return MathHelper.clamp(p_clamp_1_, minValue, maxValue);
	}

	protected float clampSteps(float p_clampSteps_1_) {

		if (steps > 0) {
			p_clampSteps_1_ = (float) (steps * Math.round(p_clampSteps_1_ / (float) steps));
		}

		return p_clampSteps_1_;
	}

	public int getYImage(boolean p_getYImage_1_) {

		return 0;
	}

	public void renderBg(int p_renderBg_1_, int p_renderBg_2_) {

		if (getProxy().visible) {
			if (sliding) {
				value = (float) (p_renderBg_1_ - (getProxy().x + 4)) / (float) (getProxy().getButtonWidth() - 8);
				value = MathHelper.clamp(value, 0.0F, 1.0F);
				float f = toValue(value);
				clicked(f);
				value = toPct(f);
				getProxy().displayString = getMessage();
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_LOCATION);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			blit(getProxy().x + (int) (value * (float) (getProxy().getButtonWidth() - 8)), getProxy().y, 0, 66, 4, 20);
			blit(getProxy().x + (int) (value * (float) (getProxy().getButtonWidth() - 8)) + 4, getProxy().y, 196, 66, 4, 20);
		}
	}

	public void clicked(int p_clicked_1_, int p_clicked_2_) {

		value = (float) (p_clicked_1_ - (getProxy().x + 4)) / (float) (getProxy().getButtonWidth() - 8);
		value = MathHelper.clamp(value, 0.0F, 1.0F);
		clicked(toValue(value));
		getProxy().displayString = getMessage();
		sliding = true;
	}

	public void clicked(float p_clicked_1_) {

	}

	public void released(int p_released_1_, int p_released_2_) {

		sliding = false;
	}

}
