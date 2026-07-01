package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragonHead;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.Facing;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import java.util.UUID;

public class TileEntitySkullRenderer extends TileEntitySpecialRenderer<TileEntitySkull> {

	private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
	private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
	private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");
	private static final ResourceLocation DRAGON_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon.png");
	public static TileEntitySkullRenderer instance;
	private final ModelDragonHead dragonHead = new ModelDragonHead(0F);
	private final ModelSkeletonHead skeletonHead = new ModelSkeletonHead(0, 0, 64, 32);
	private final ModelSkeletonHead humanoidHead = new ModelHumanoidHead();

	public void render(TileEntitySkull te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		Facing enumfacing = Facing.getFront(te.getBlockMetadata() & 7);
		float f = te.getAnimationProgress(partialTicks);
		renderSkull((float) x, (float) y, (float) z, enumfacing, (float) (te.getSkullRotation() * 360) / 16F, te.getSkullType(), te.getPlayerProfile(), destroyStage, f);
	}

	public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn) {

		super.setRendererDispatcher(rendererDispatcherIn);
		instance = this;
	}

	public void renderSkull(float x, float y, float z, Facing facing, float rotationIn, int skullType, GameProfile profile, int destroyStage, float animateTicks) {

		ModelBase modelbase = skeletonHead;

		if (destroyStage >= 0) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			GLS.matrixMode(5890);
			GLS.pushMatrix();
			GLS.scale(4F, 2F, 1F);
			GLS.translate(0.0625F, 0.0625F, 0.0625F);
			GLS.matrixMode(5888);
		} else {
			switch (skullType) {
				case 0:
				default:
					bindTexture(SKELETON_TEXTURES);
					break;

				case 1:
					bindTexture(WITHER_SKELETON_TEXTURES);
					break;

				case 2:
					bindTexture(ZOMBIE_TEXTURES);
					modelbase = humanoidHead;
					break;

				case 3:
					modelbase = humanoidHead;
					ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();

					if (profile != null) {
						Minecraft minecraft = Minecraft.getMinecraft();
						Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

						if (map.containsKey(Type.SKIN)) {
							resourcelocation = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
						} else {
							UUID uuid = EntityPlayer.getUUID(profile);
							resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
						}
					}

					bindTexture(resourcelocation);
					break;

				case 4:
					bindTexture(CREEPER_TEXTURES);
					break;

				case 5:
					bindTexture(DRAGON_TEXTURES);
					modelbase = dragonHead;
			}
		}

		GLS.pushMatrix();
		GLS.disableCull();

		if (facing == Facing.UP) {
			GLS.translate(x + 0.5F, y, z + 0.5F);
		} else {
			switch (facing) {
				case NORTH:
					GLS.translate(x + 0.5F, y + 0.25F, z + 0.74F);
					break;

				case SOUTH:
					GLS.translate(x + 0.5F, y + 0.25F, z + 0.26F);
					rotationIn = 180F;
					break;

				case WEST:
					GLS.translate(x + 0.74F, y + 0.25F, z + 0.5F);
					rotationIn = 270F;
					break;

				case EAST:
				default:
					GLS.translate(x + 0.26F, y + 0.25F, z + 0.5F);
					rotationIn = 90F;
			}
		}

		float f = 0.0625F;
		GLS.enableRescaleNormal();
		GLS.scale(-1F, -1F, 1F);
		GLS.enableAlpha();

		if (skullType == 3) {
			GLS.enableBlendProfile(GLS.Profile.PLAYER_SKIN);
		}

		modelbase.render(null, animateTicks, 0F, 0F, rotationIn, 0F, 0.0625F);
		GLS.popMatrix();

		if (destroyStage >= 0) {
			GLS.matrixMode(5890);
			GLS.popMatrix();
			GLS.matrixMode(5888);
		}
	}

}
