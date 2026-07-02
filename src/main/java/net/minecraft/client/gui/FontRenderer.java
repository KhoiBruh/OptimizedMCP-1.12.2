package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLS;
import net.minecraft.client.renderer.NativeImage;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FontRenderer implements IResourceManagerReloadListener {

	private static final ResourceLocation[] UNICODE_PAGES = new ResourceLocation[256];

	private static final String CHARS = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";
	private static final char[] CHARS_ARRAY = CHARS.toCharArray();
	private static final String FORMAT_CODES = "0123456789abcdefklmnor";

	private final int[] charWidth = new int[256];
	private final byte[] glyphWidth = new byte[65536];
	private final int[] colorCode = new int[32];

	private final ResourceLocation fontTexture;
	private final TextureManager textureManager;

	public int FONT_HEIGHT = 9;
	public Random fontRandom = new Random();

	@Setter
	@Getter
	private boolean unicode;

	@Setter
	@Getter
	private boolean bidi;

	public FontRenderer(ResourceLocation location, TextureManager textureManager, boolean unicode) {
		fontTexture = location;
		this.textureManager = textureManager;
		this.unicode = unicode;
		textureManager.bindTexture(fontTexture);

		for (int i = 0; i < 32; ++i) {
			int j = (i >> 3 & 1) * 85;
			int k = (i >> 2 & 1) * 170 + j;
			int l = (i >> 1 & 1) * 170 + j;
			int i1 = (i & 1) * 170 + j;

			if (i == 6) k += 85;
			if (i >= 16) {
				k /= 4;
				l /= 4;
				i1 /= 4;
			}

			colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
		}

		readGlyphSizes();
	}

	private static boolean isFormatColor(char colorChar) {
		return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
	}

	private static boolean isFormatSpecial(char formatChar) {
		return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
	}

	public static String getFormat(String text) {
		StringBuilder s = new StringBuilder();
		int i = -1;
		int j = text.length();

		while ((i = text.indexOf(167, i + 1)) != -1) {
			if (i < j - 1) {
				char c0 = text.charAt(i + 1);
				if (isFormatColor(c0)) {
					s = new StringBuilder("§" + c0);
				} else if (isFormatSpecial(c0)) {
					s.append("§").append(c0);
				}
			}
		}

		return s.toString();
	}

	public void reload(IResourceManager resourceManager) {
		readTexture();
		readGlyphSizes();
	}

	private void readTexture() {
		NativeImage image;

		try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(fontTexture)) {
			image = TextureUtil.readImage(resource.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int[] pixelData = new int[imageWidth * imageHeight];
		image.getRGB(0, 0, imageWidth, imageHeight, pixelData, 0, imageWidth);
		image.close();

		int cellHeight = imageHeight / 16;
		int cellWidth = imageWidth / 16;
		float scale = 8F / cellWidth;

		for (int i = 0; i < 256; ++i) {
			int col = i % 16;
			int row = i / 16;

			if (i == 32) charWidth[i] = 4;

			int lastCol;
			for (lastCol = cellWidth - 1; lastCol >= 0; --lastCol) {
				int xOffset = col * cellWidth + lastCol;
				boolean empty = true;

				for (int yCell = 0; yCell < cellHeight; ++yCell) {
					int rowOffset = (row * cellWidth + yCell) * imageWidth;
					if ((pixelData[xOffset + rowOffset] >> 24 & 255) != 0) {
						empty = false;
						break;
					}
				}

				if (!empty) break;
			}

			++lastCol;
			charWidth[i] = (int) (0.5D + (lastCol * scale)) + 1;
		}
	}

	private void readGlyphSizes() {
		try (
			IResource resource = Minecraft.getMinecraft()
			                              .getResourceManager()
			                              .getResource(new ResourceLocation("font/glyph_sizes.bin"))
		) {
			resource.getInputStream().read(glyphWidth);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private float emitDefault(int ch, boolean italic, float x, float y, float r, float g, float b, float a, GlyphSink sink) {
		textureManager.bindTexture(fontTexture);
		int texId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);

		int col = ch % 16 * 8;
		int row = ch / 16 * 8;
		float k = italic ? 1 : 0;
		int glyphW = charWidth[ch];
		float f = glyphW - 0.01F;

		float u1 = col / 128F;
		float v1 = row / 128F;
		float u2 = (col + f - 1F) / 128F;
		float v2 = (row + 7.99F) / 128F;

		sink.glyph(texId, x, y, x + f - 1F, y + 7.99F, u1, v1, u2, v2, r, g, b, a, k);
		return glyphW;
	}

	private ResourceLocation getUnicodePage(int page) {
		if (UNICODE_PAGES[page] == null)
			UNICODE_PAGES[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));

		return UNICODE_PAGES[page];
	}

	private float emitUnicode(char ch, boolean italic, float x, float y, float r, float g, float b, float a, GlyphSink sink) {
		int glyph = glyphWidth[ch] & 255;
		if (glyph == 0) return 0;

		int page = ch / 256;
		textureManager.bindTexture(getUnicodePage(page));
		int texId = GLS.getInteger(GL11.GL_TEXTURE_BINDING_2D);

		int left = glyph >>> 4;
		int right = glyph & 15;
		float fLeft = (float) left;
		float fRight = right + 1;
		float uBase = (ch % 16 * 16) + fLeft;
		float vBase = (float) (ch & 255) / 16 * 16;
		float width = fRight - fLeft - 0.02F;
		float k = italic ? 1 : 0;

		float u1 = uBase / 256;
		float v1 = vBase / 256;
		float u2 = (uBase + width) / 256;
		float v2 = (vBase + 15.98F) / 256;

		sink.glyph(texId, x, y, x + width / 2F, y + 7.99F, u1, v1, u2, v2, r, g, b, a, k);
		return (fRight - fLeft) / 2 + 1;
	}

	private float emitChar(char ch, boolean italic, float x, float y, float r, float g, float b, float a, GlyphSink sink) {
		if (ch == ' ') return 4;
		int idx = CHARS.indexOf(ch);
		return idx != -1 && !unicode ? emitDefault(idx, italic, x, y, r, g, b, a, sink) : emitUnicode(ch, italic, x, y, r, g, b, a, sink);
	}

	/**
	 * @deprecated Use {@link DrawContext#text(FontRenderer, String, int, int, int)} instead.
	 */
	@Deprecated
	public int drawShadowText(String text, float x, float y, int color) {
		GLS.enableAlpha();
		TessellatorSink sink = new TessellatorSink();
		int i = (int) emitGlyphs(text, x + 1, y + 1, color, true, sink);
		i = Math.max(i, (int) emitGlyphs(text, x, y, color, false, sink));
		sink.finish();
		return i;
	}

	/**
	 * @deprecated Use {@link DrawContext#text(FontRenderer, String, int, int, int)} instead.
	 */
	@Deprecated
	public int drawText(String text, int x, int y, int color) {
		return drawText(text, (float) x, (float) y, color, false);
	}

	/**
	 * @deprecated Use DrawContext text methods instead.
	 */
	@Deprecated
	public void drawSplit(String str, int x, int y, int wrapWidth, int textColor) {
		str = trimNewline(str);
		TessellatorSink sink = new TessellatorSink();
		for (String line : formatToWidth(str, wrapWidth)) {
			emitGlyphs(line, x, y, textColor, false, sink);
			y += FONT_HEIGHT;
		}
		sink.finish();
	}

	/**
	 * @deprecated Use {@link DrawContext#text(FontRenderer, String, int, int, int)} instead.
	 */
	@Deprecated
	public int drawText(String text, float x, float y, int color, boolean shadow) {
		GLS.enableAlpha();
		TessellatorSink sink = new TessellatorSink();
		int i;
		if (shadow) {
			i = (int) emitGlyphs(text, x + 1, y + 1, color, true, sink);
			i = Math.max(i, (int) emitGlyphs(text, x, y, color, false, sink));
		} else {
			i = (int) emitGlyphs(text, x, y, color, false, sink);
		}
		sink.finish();
		return i;
	}

	public float emitGlyphs(String text, float x, float y, int color, boolean shadow, GlyphSink sink) {
		if (text == null) return 0;

		if (bidi) text = bidiReorder(text);
		if ((color & -67108864) == 0) color |= -16777216;
		if (shadow) color = (color & 16579836) >> 2 | color & -16777216;

		float red = (float) (color >> 16 & 255) / 255F;
		float green = (float) (color >> 8 & 255) / 255F;
		float blue = (float) (color & 255) / 255F;
		float alpha = (float) (color >> 24 & 255) / 255F;

		boolean random = false;
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		boolean strikethrough = false;

		float curX = x;
		float curY = y;

		for (int i = 0; i < text.length(); ++i) {
			char c0 = text.charAt(i);

			if (c0 == '§' && i + 1 < text.length()) {
				char format = Character.toLowerCase(text.charAt(i + 1));
				int idx = FORMAT_CODES.indexOf(format);

				if (idx < 0) idx = 15;

				if (idx < 16) {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;

					if (shadow) idx += 16;

					int textColor = colorCode[idx];
					red = (textColor >> 16 & 255) / 255F;
					green = (textColor >> 8 & 255) / 255F;
					blue = (textColor & 255) / 255F;
				} else if (idx == 16) {
					random = true;
				} else if (idx == 17) {
					bold = true;
				} else if (idx == 18) {
					strikethrough = true;
				} else if (idx == 19) {
					underline = true;
				} else if (idx == 20) {
					italic = true;
				} else {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;
				}

				++i;
			} else {
				int charIdx = CHARS.indexOf(c0);

				if (random && charIdx != -1) {
					int targetWidth = getCharWidth(c0);

					do {
						charIdx = fontRandom.nextInt(CHARS_ARRAY.length);
						c0 = CHARS_ARRAY[charIdx];
					} while (targetWidth != getCharWidth(c0));
				}

				float offset = unicode ? 0.5F : 1;
				boolean needsShift = (c0 == 0 || charIdx == -1 || unicode) && shadow;

				if (needsShift) {
					curX -= offset;
					curY -= offset;
				}

				float f = emitChar(c0, italic, curX, curY, red, green, blue, alpha, sink);

				if (needsShift) {
					curX += offset;
					curY += offset;
				}

				if (bold) {
					curX += offset;
					if (needsShift) {
						curX -= offset;
						curY -= offset;
					}

					emitChar(c0, italic, curX, curY, red, green, blue, alpha, sink);

					curX -= offset;
					if (needsShift) {
						curX += offset;
						curY += offset;
					}

					++f;
				}

				if (strikethrough) {
					sink.rect(curX, curY + FONT_HEIGHT / 2F - 1, curX + f, curY + FONT_HEIGHT / 2F, red, green, blue, alpha);
				}

				if (underline) {
					sink.rect(curX - 1, curY + FONT_HEIGHT - 1, curX + f, curY + FONT_HEIGHT, red, green, blue, alpha);
				}

				curX += (int) f;
			}
		}

		return curX;
	}

	private String bidiReorder(String text) {
		try {
			Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(2);
		} catch (ArabicShapingException e) {
			return text;
		}
	}

	public int getWidth(String text) {
		if (text == null) return 0;

		int width = 0;
		boolean boldActive = false;

		for (int i = 0; i < text.length(); ++i) {
			char c0 = text.charAt(i);
			int charWidth = getCharWidth(c0);

			if (charWidth < 0 && i < text.length() - 1) {
				++i;
				c0 = text.charAt(i);

				if (c0 != 'l' && c0 != 'L') {
					if (c0 == 'r' || c0 == 'R') boldActive = false;
				}
				else boldActive = true;

				charWidth = 0;
			}

			width += charWidth;

			if (boldActive && charWidth > 0) ++width;
		}

		return width;
	}

	public int getCharWidth(char character) {
		if (character == '§') return -1;
		if (character == ' ') return 4;

		int idx = CHARS.indexOf(character);

		if (character == 0 || idx == -1 || unicode) {
			if (glyphWidth[character] != 0) {
				int glyph = glyphWidth[character] & 255;
				int left = glyph >>> 4;
				int right = glyph & 15;
				++right;
				return (right - left) / 2 + 1;
			}
		}
		else return charWidth[idx];

		return 0;
	}

	public String trimToWidth(String text, int width) {
		return trimToWidth(text, width, false);
	}

	public String trimToWidth(String text, int width, boolean reverse) {
		StringBuilder sb = new StringBuilder();

		int accumulated = 0;
		int start = reverse ? text.length() - 1 : 0;
		int step = reverse ? -1 : 1;

		boolean format = false;
		boolean boldActive = false;

		for (int i = start; i >= 0 && i < text.length() && accumulated < width; i += step) {
			char c0 = text.charAt(i);
			int charWidth = getCharWidth(c0);

			if (format) {
				format = false;

				if (c0 != 'l' && c0 != 'L') {
					if (c0 == 'r' || c0 == 'R') boldActive = false;
				}
				else boldActive = true;
			} else if (charWidth < 0) {
				format = true;
			} else {
				accumulated += charWidth;

				if (boldActive) ++accumulated;
			}

			if (accumulated > width) break;

			if (reverse) sb.insert(0, c0);
			else sb.append(c0);
		}

		return sb.toString();
	}

	public int getWrappedHeight(String str, int maxLength) {
		return FONT_HEIGHT * formatToWidth(str, maxLength).size();
	}

	public List<String> formatToWidth(String text, int wrapWidth) {
		return Arrays.asList(wrapFormatToWidth(text, wrapWidth).split("\n"));
	}

	String wrapFormatToWidth(String str, int wrapWidth) {
		int splitIndex = sizeToWidth(str, wrapWidth);

		if (str.length() > splitIndex) {
			String head = str.substring(0, splitIndex);
			char boundary = str.charAt(splitIndex);
			boolean flag = boundary == ' ' || boundary == '\n';
			String tail = getFormat(head) + str.substring(splitIndex + (flag ? 1 : 0));
			return head + "\n" + wrapFormatToWidth(tail, wrapWidth);
		}

		return str;
	}

	private int sizeToWidth(String str, int wrapWidth) {
		int length = str.length();
		int width = 0;
		int pos = 0;
		int lastSpace = -1;

		boolean boldActive = false;

		for (; pos < length; ++pos) {
			char c0 = str.charAt(pos);

			switch (c0) {
				case '\n' -> --pos;

				case ' ', '§' -> {
					if (c0 == ' ') lastSpace = pos;

					if (pos < length - 1) {
						++pos;
						char c1 = str.charAt(pos);

						if (c1 != 'l' && c1 != 'L') {
							if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) boldActive = false;
						}
						else boldActive = true;
					}

					width += getCharWidth(c0);
					if (boldActive) ++width;
				}

				default -> {
					width += getCharWidth(c0);
					if (boldActive) ++width;
				}
			}

			if (c0 == '\n') {
				++pos;
				lastSpace = pos;
				break;
			}

			if (width > wrapWidth) break;
		}

		return pos != length && lastSpace != -1 && lastSpace < pos ? lastSpace : pos;
	}

	private String trimNewline(String text) {
		while (text != null && text.endsWith("\n")) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	public int getColorCode(char character) {
		int i = "0123456789abcdef".indexOf(character);
		return i >= 0 && i < colorCode.length ? colorCode[i] : -1;
	}

	private class TessellatorSink implements GlyphSink {
		private BufferBuilder buf;
		private boolean started;

		private void ensureStarted() {
			if (!started) {
				buf = Tessellator.getInstance().getBuffer();
				buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				started = true;
			}
		}

		@Override
		public void glyph(int textureId, float left, float top, float right, float bottom,
		                  float u1, float v1, float u2, float v2,
		                  float r, float g, float b, float a, float italicOffset) {
			ensureStarted();
			buf.pos(left + italicOffset, top, 0F).tex(u1, v1).color(r, g, b, a).endVertex();
			buf.pos(left - italicOffset, bottom, 0F).tex(u1, v2).color(r, g, b, a).endVertex();
			buf.pos(right - italicOffset, bottom, 0F).tex(u2, v2).color(r, g, b, a).endVertex();
			buf.pos(right + italicOffset, top, 0F).tex(u2, v1).color(r, g, b, a).endVertex();
		}

		@Override
		public void rect(float x1, float y1, float x2, float y2,
		                 float r, float g, float b, float a) {
			finish();
			GLS.disableTexture2D();
			buf = Tessellator.getInstance().getBuffer();
			buf.begin(7, DefaultVertexFormats.POSITION);
			buf.pos(x1, y2, 0D).endVertex();
			buf.pos(x2, y2, 0D).endVertex();
			buf.pos(x2, y1, 0D).endVertex();
			buf.pos(x1, y1, 0D).endVertex();
			Tessellator.getInstance().draw();
			GLS.enableTexture2D();
			started = false;
		}

		void finish() {
			if (started) {
				Tessellator.getInstance().draw();
				started = false;
				buf = null;
			}
		}
	}
}
