package net.minecraft.client.renderer;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.system.MemoryUtil.*;

public class NativeImage implements AutoCloseable {

	public static final int FORMAT_RGB = 3;
	public static final int FORMAT_RGBA = 4;

	@Getter
	private final int format;

	@Getter
	private final int width;

	@Getter
	private final int height;

	@Getter
	private final long pointer;

	private boolean closed;

	public NativeImage(int width, int height, boolean useAlpha) {
		this(width, height, useAlpha ? FORMAT_RGBA : FORMAT_RGB, true);
	}

	public NativeImage(int width, int height, int format, boolean useCalloc) {
		this.width = width;
		this.height = height;
		this.format = format;
		long size = (long) width * height * format;
		pointer = useCalloc ? nmemCalloc(1, size) : nmemAlloc(size);
	}

	private NativeImage(int width, int height, int format, long pointer) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.pointer = pointer;
	}

	public static NativeImage read(InputStream stream) throws IOException {
		return read(stream, FORMAT_RGBA);
	}

	public static NativeImage read(InputStream stream, int format) throws IOException {
		ByteBuffer buffer = null;

		try {
			buffer = readBuffer(stream);
			buffer.rewind();

			try (MemoryStack memorystack = MemoryStack.stackPush()) {
				IntBuffer w = memorystack.mallocInt(1);
				IntBuffer h = memorystack.mallocInt(1);
				IntBuffer comp = memorystack.mallocInt(1);
				ByteBuffer imagePixels = STBImage.stbi_load_from_memory(buffer, w, h, comp, format);
				if (imagePixels == null) {
					throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
				}
				return new NativeImage(w.get(0), h.get(0), format, memAddress(imagePixels));
			}
		} finally {
			if (buffer != null) memFree(buffer);
		}
	}

	public static NativeImage read(File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			return read(stream);
		}
	}

	private static ByteBuffer readBuffer(InputStream inputStream) throws IOException {
		int bufferSize = inputStream.available();
		if (bufferSize <= 0) bufferSize = 4096;
		ByteBuffer buffer = memAlloc(bufferSize);
		ReadableByteChannel channel = Channels.newChannel(inputStream);

		try {
			while (true) {
				int bytesRead = channel.read(buffer);
				if (bytesRead == -1) break;
				if (buffer.remaining() == 0) buffer = memRealloc(buffer, buffer.capacity() * 2);
			}
		} finally {
			IOUtils.closeQuietly(channel);
		}

		buffer.flip();
		return buffer;
	}

	public ByteBuffer getBuffer() {
		return memByteBuffer(pointer, width * height * format);
	}

	private void checkBounds(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IllegalArgumentException(String.format("Out of bounds: x=%d, y=%d, width=%d, height=%d", x, y, width, height));
		}
	}

	public int getPixel(int x, int y) {
		checkBounds(x, y);
		long offset = pointer + ((long) y * width + x) * format;
		int r = memGetByte(offset) & 0xFF;
		int g = memGetByte(offset + 1) & 0xFF;
		int b = memGetByte(offset + 2) & 0xFF;

		if (format == FORMAT_RGBA) {
			int a = memGetByte(offset + 3) & 0xFF;
			return (a << 24) | (r << 16) | (g << 8) | b;
		} else return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public void setPixel(int x, int y, int argb) {
		checkBounds(x, y);
		long offset = pointer + ((long) y * width + x) * format;
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;

		if (format == FORMAT_RGBA) {
			memPutByte(offset, (byte) r);
			memPutByte(offset + 1, (byte) g);
			memPutByte(offset + 2, (byte) b);
			memPutByte(offset + 3, (byte) a);
		} else {
			memPutByte(offset, (byte) r);
			memPutByte(offset + 1, (byte) g);
			memPutByte(offset + 2, (byte) b);
		}
	}

	public int[] getPixels() {
		int[] pixels = new int[width * height];
		getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}

	public void getRGB(int startX, int startY, int w, int h, int[] rgb, int offset, int size) {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				rgb[offset + y * size + x] = getPixel(startX + x, startY + y);
			}
		}
	}

	public void setRGB(int startX, int startY, int w, int h, int[] rgb, int offset, int size) {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				setPixel(startX + x, startY + y, rgb[offset + y * size + x]);
			}
		}
	}

	public void fillRect(int x, int y, int w, int h, int argb) {
		for (int row = y; row < y + h; row++) {
			for (int col = x; col < x + w; col++) {
				setPixel(col, row, argb);
			}
		}
	}

	public void makeAreaOpaque(int x, int y, int w, int h) {
		if (format != FORMAT_RGBA) return;
		for (int row = y; row < y + h; row++) {
			for (int col = x; col < x + w; col++) {
				long offset = pointer + ((long) row * width + col) * format + 3;
				memPutByte(offset, (byte) 0xFF);
			}
		}
	}

	public void makeAreaTransparent(int x, int y, int w, int h) {
		if (format != FORMAT_RGBA) return;
		for (int row = y; row < y + h; row++) {
			for (int col = x; col < x + w; col++) {
				long offset = pointer + ((long) row * width + col) * format + 3;
				memPutByte(offset, (byte) 0x00);
			}
		}
	}

	public void blendPixel(int x, int y, int argb) {
		checkBounds(x, y);
		int srcA = (argb >> 24) & 0xFF;
		if (srcA == 0) return;
		if (srcA == 255) {
			setPixel(x, y, argb);
			return;
		}

		int dstArgb = getPixel(x, y);
		int dstA = (dstArgb >> 24) & 0xFF;
		int dstR = (dstArgb >> 16) & 0xFF;
		int dstG = (dstArgb >> 8) & 0xFF;
		int dstB = dstArgb & 0xFF;

		int srcR = (argb >> 16) & 0xFF;
		int srcG = (argb >> 8) & 0xFF;
		int srcB = argb & 0xFF;

		int outA = srcA + dstA * (255 - srcA) / 255;
		if (outA == 0) return;

		int outR = (srcR * srcA + dstR * dstA * (255 - srcA) / 255) / outA;
		int outG = (srcG * srcA + dstG * dstA * (255 - srcA) / 255) / outA;
		int outB = (srcB * srcA + dstB * dstA * (255 - srcA) / 255) / outA;

		setPixel(x, y, (outA << 24) | (outR << 16) | (outG << 8) | outB);
	}

	public void drawImage(NativeImage src, int x, int y) {
		for (int row = 0; row < src.getHeight(); row++) {
			for (int col = 0; col < src.getWidth(); col++) {
				blendPixel(x + col, y + row, src.getPixel(col, row));
			}
		}
	}

	public void copyArea(int srcX, int srcY, int destX, int destY, int w, int h, boolean flipX, boolean flipY) {
		int[] temp = new int[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				temp[y * w + x] = getPixel(srcX + x, srcY + y);
			}
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int dx = flipX ? (w - 1 - x) : x;
				int dy = flipY ? (h - 1 - y) : y;
				setPixel(destX + dx, destY + dy, temp[y * w + x]);
			}
		}
	}

	public void drawImageScaled(NativeImage src, int srcX, int srcY, int srcW, int srcH, int destX, int destY, int destW, int destH) {
		for (int dy = 0; dy < destH; dy++) {
			for (int dx = 0; dx < destW; dx++) {
				int sx = srcX + (dx * srcW / destW);
				int sy = srcY + (dy * srcH / destH);
				setPixel(destX + dx, destY + dy, src.getPixel(sx, sy));
			}
		}
	}

	public void copyFrom(NativeImage src, int srcX, int srcY, int destX, int destY, int w, int h) {
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				setPixel(destX + col, destY + row, src.getPixel(srcX + col, srcY + row));
			}
		}
	}

	public void flipVertically() {
		long rowBytes = (long) width * format;
		long tmpPtr = nmemAlloc(rowBytes);

		try {
			int half = height / 2;
			for (int row = 0; row < half; row++) {
				long topOffset = pointer + row * rowBytes;
				long bottomOffset = pointer + (height - 1 - row) * rowBytes;
				memCopy(topOffset, tmpPtr, rowBytes);
				memCopy(bottomOffset, topOffset, rowBytes);
				memCopy(tmpPtr, bottomOffset, rowBytes);
			}
		} finally {
			nmemFree(tmpPtr);
		}
	}

	public NativeImage getSubImage(int x, int y, int w, int h) {
		NativeImage sub = new NativeImage(w, h, format, false);
		sub.copyFrom(this, x, y, 0, 0, w, h);
		return sub;
	}

	public byte[] getBytes() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		STBImageWrite.stbi_write_png_to_func(
			new STBIWriteCallback() {
				@Override
				public void invoke(long context, long data, int size) {
					ByteBuffer bytebuffer = memByteBuffer(data, size);
					byte[] bytes = new byte[size];
					bytebuffer.get(bytes);

					try {
						output.write(bytes);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			},
			0L, width, height, format, getBuffer(), width * format
		);

		return output.toByteArray();
	}

	public void write(File file) throws IOException {
		STBImageWrite.stbi_write_png(file.getAbsolutePath(), width, height, format, getBuffer(), width * format);
	}

	@Override
	public void close() {
		if (!closed) {
			closed = true;
			if (pointer != 0) nmemFree(pointer);
		}
	}

}
