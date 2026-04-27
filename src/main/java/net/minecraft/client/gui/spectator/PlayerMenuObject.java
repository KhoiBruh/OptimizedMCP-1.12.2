package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PlayerMenuObject implements ISpectatorMenuObject {

	private final GameProfile profile;
	private final ResourceLocation resourceLocation;

	public PlayerMenuObject(GameProfile profileIn) {

		profile = profileIn;
		resourceLocation = AbstractClientPlayer.getLocationSkin(profileIn.getName());
		AbstractClientPlayer.getDownloadImageSkin(resourceLocation, profileIn.getName());
	}

	public void selectItem(SpectatorMenu menu) {

		Minecraft.getMinecraft().getConnection().sendPacket(new CPacketSpectate(profile.getId()));
	}

	public ITextComponent getSpectatorName() {

		return new TextComponentString(profile.getName());
	}

	public void renderIcon(float brightness, int alpha) {

		Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
		GlStateManager.color(1.0F, 1.0F, 1.0F, (float) alpha / 255.0F);
		Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
		Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
	}

	public boolean isEnabled() {

		return true;
	}

}
