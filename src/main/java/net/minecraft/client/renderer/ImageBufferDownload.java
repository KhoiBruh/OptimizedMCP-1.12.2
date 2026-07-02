package net.minecraft.client.renderer;

import net.minecraft.client.renderer.NativeImage;

public class ImageBufferDownload implements IImageBuffer {

	private final int imageSize = 64;
	

	public NativeImage parseUserSkin(NativeImage image) {
		if (image != null) {
			NativeImage bufferedimage = new NativeImage(imageSize, imageSize, true);
			bufferedimage.drawImage(image, 0, 0);
			boolean flag = image.getHeight() == 32;

			if (flag) {
				bufferedimage.fillRect(0, 32, 64, 32, 0);
				bufferedimage.copyArea(4, 16, 20, 48, 4, 4, true, false);
				bufferedimage.copyArea(8, 16, 24, 48, 4, 4, true, false);
				bufferedimage.copyArea(8, 20, 16, 52, 4, 12, true, false);
				bufferedimage.copyArea(4, 20, 20, 52, 4, 12, true, false);
				bufferedimage.copyArea(0, 20, 24, 52, 4, 12, true, false);
				bufferedimage.copyArea(12, 20, 28, 52, 4, 12, true, false);
				bufferedimage.copyArea(44, 16, 36, 48, 4, 4, true, false);
				bufferedimage.copyArea(48, 16, 40, 48, 4, 4, true, false);
				bufferedimage.copyArea(48, 20, 32, 52, 4, 12, true, false);
				bufferedimage.copyArea(44, 20, 36, 52, 4, 12, true, false);
				bufferedimage.copyArea(40, 20, 40, 52, 4, 12, true, false);
				bufferedimage.copyArea(52, 20, 44, 52, 4, 12, true, false);
			}

			
			
			setAreaOpaque(bufferedimage, 0, 0, 32, 16);

			if (flag) setAreaTransparent(bufferedimage, 32, 0, 64, 32);

			setAreaOpaque(bufferedimage, 0, 16, 64, 32);
			setAreaOpaque(bufferedimage, 16, 48, 48, 64);
			return bufferedimage;
		} else {
			return null;
		}
	}

	public void skinAvailable() {
	}

	private void setAreaTransparent(NativeImage image, int x, int y, int width, int height) {
		for (int i = x; i < width; ++i) {
			for (int j = y; j < height; ++j) {
				int k = image.getPixel(i, j);

				if ((k >> 24 & 255) < 128) {
					return;
				}
			}
		}

		for (int l = x; l < width; ++l) {
			for (int i1 = y; i1 < height; ++i1) {
				image.setPixel(l, i1, image.getPixel(l, i1) & 16777215);
			}
		}
	}

	/**
	 * Makes the given area of the image opaque
	 */
	private void setAreaOpaque(NativeImage image, int x, int y, int width, int height) {
		for (int i = x; i < width; ++i) {
			for (int j = y; j < height; ++j) {
				image.setPixel(i, j, image.getPixel(i, j) | -16777216);
			}
		}
	}

}
