package net.minecraft.client.renderer;

import net.minecraft.client.renderer.NativeImage;

public interface IImageBuffer {

	NativeImage parseUserSkin(NativeImage image);

	void skinAvailable();

}
