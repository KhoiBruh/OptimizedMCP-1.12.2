package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelRabbit;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class RenderRabbit extends RenderLiving<EntityRabbit> {

	private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/rabbit/brown.png");
	private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/rabbit/white.png");
	private static final ResourceLocation BLACK = new ResourceLocation("textures/entity/rabbit/black.png");
	private static final ResourceLocation GOLD = new ResourceLocation("textures/entity/rabbit/gold.png");
	private static final ResourceLocation SALT = new ResourceLocation("textures/entity/rabbit/salt.png");
	private static final ResourceLocation WHITE_SPLOTCHED = new ResourceLocation("textures/entity/rabbit/white_splotched.png");
	private static final ResourceLocation TOAST = new ResourceLocation("textures/entity/rabbit/toast.png");
	private static final ResourceLocation CAERBANNOG = new ResourceLocation("textures/entity/rabbit/caerbannog.png");

	public RenderRabbit(RenderManager p_i47196_1_) {

		super(p_i47196_1_, new ModelRabbit(), 0.3F);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntityRabbit entity) {

		String s = TextFormatting.getTextWithoutFormattingCodes(entity.getName());

		if ("Toast".equals(s)) {
			return TOAST;
		} else {
			return switch (entity.getRabbitType()) {
				default -> BROWN;
				case 1 -> WHITE;
				case 2 -> BLACK;
				case 3 -> WHITE_SPLOTCHED;
				case 4 -> GOLD;
				case 5 -> SALT;
				case 99 -> CAERBANNOG;
			};
		}
	}

}
