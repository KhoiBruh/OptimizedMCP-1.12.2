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

	private final ResourceLocation locationFontTexture;
	private final TextureManager textureManager;

	public int FONT_HEIGHT = 9;
	public Random fontRandom = new Random();

	private float x;
	private float y;

	@Setter
	@Getter
	private boolean unicode;

	@Setter
	@Getter
	private boolean bidi;

	private float red;
	private float blue;
	private float green;
	private float alpha;
	private int textColor;

	private boolean bold;
	private boolean italic;
	private boolean random;
	private boolean underline;
	private boolean strikethrough;

	private boolean batching;
	private ResourceLocation boundTexture;
	private BufferBuilder bufferBuilder;

	public FontRenderer(ResourceLocation location, TextureManager textureManager, boolean unicode) {
		locationFontTexture = location;
		this.textureManager = textureManager;
		this.unicode = unicode;
		textureManager.bindTexture(locationFontTexture);

		for (int i = 0; i < 32; ++i) {
			int j = (i >> 3 & 1) * 85;
			int k = (i >> 2 & 1) * 170 + j;
			int l = (i >> 1 & 1) * 170 + j;
			int i1 = (i & 1) * 170 + j;

			if (i == 6) k += 85;
			if (i >= 16) { k /= 4; l /= 4; i1 /= 4; }

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

	public static String getFormatFromString(String text) {
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

	private void bindTextureBatch(ResourceLocation location) {
		if (boundTexture != location) {
			if (batching) {
				Tessellator.getInstance().draw();
				bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			}
			boundTexture = location;
			textureManager.bindTexture(location);
		}
	}

	private void startBatch() {
		if (!batching) {
			batching = true;
			bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			boundTexture = null;
		}
	}

	private void endBatch() {
		if (batching) {
			Tessellator.getInstance().draw();
			batching = false;
			boundTexture = null;
			bufferBuilder = null;
		}
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {
		readFontTexture();
		readGlyphSizes();
	}

	private void readFontTexture() {
		NativeImage image;

		try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(locationFontTexture)) {
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
		float scale = 8F / (float) cellWidth;

		for (int i = 0; i < 256; ++i) {
			int col = i % 16;
			int row = i / 16;

			if (i == 32) {
				charWidth[i] = 4;
			}

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
			charWidth[i] = (int) (0.5D + (double) ((float) lastCol * scale)) + 1;
		}
	}

	private void readGlyphSizes() {
		try (IResource resource = Minecraft.getMinecraft()
		                                    .getResourceManager()
		                                    .getResource(new ResourceLocation("font/glyph_sizes.bin"))) {
			resource.getInputStream().read(glyphWidth);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private float renderChar(char ch, boolean italic) {
		if (ch == ' ') return 4F;

		int idx = CHARS.indexOf(ch);
		return idx != -1 && !unicode ? renderDefaultChar(idx, italic) : renderUnicodeChar(ch, italic);
	}

	private float renderDefaultChar(int ch, boolean italic) {
		int i = ch % 16 * 8;
		int j = ch / 16 * 8;
		int k = italic ? 1 : 0;
		bindTextureBatch(locationFontTexture);
		int l = charWidth[ch];
		float f = (float) l - 0.01F;

		float u1 = (float) i / 128F;
		float v1 = (float) j / 128F;
		float u2 = ((float) i + f - 1F) / 128F;
		float v2 = ((float) j + 7.99F) / 128F;

		if (batching) {
			bufferBuilder.pos(x + (float) k, y, 0F).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(x - (float) k, y + 7.99F, 0F)
			             .tex(u1, v2)
			             .color(red, green, blue, alpha)
			             .endVertex();
			bufferBuilder.pos(x + f - 1F - (float) k, y + 7.99F, 0F)
			             .tex(u2, v2)
			             .color(red, green, blue, alpha)
			             .endVertex();
			bufferBuilder.pos(x + f - 1F + (float) k, y, 0F)
			             .tex(u2, v1)
			             .color(red, green, blue, alpha)
			             .endVertex();
		} else {
			GLS.begin(5);
			GLS.texCoord2f(u1, v1);
			GLS.vertex3f(x + (float) k, y, 0F);
			GLS.texCoord2f(u1, v2);
			GLS.vertex3f(x - (float) k, y + 7.99F, 0F);
			GLS.texCoord2f(u2, v1);
			GLS.vertex3f(x + f - 1F + (float) k, y, 0F);
			GLS.texCoord2f(u2, v2);
			GLS.vertex3f(x + f - 1F - (float) k, y + 7.99F, 0F);
			GLS.end();
		}

		return (float) l;
	}

	private ResourceLocation getUnicodePageLocation(int page) {
		if (UNICODE_PAGES[page] == null)
			UNICODE_PAGES[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));

		return UNICODE_PAGES[page];
	}

	private float renderUnicodeChar(char ch, boolean italic) {
		int glyph = glyphWidth[ch] & 255;
		if (glyph == 0) return 0F;

		int page = ch / 256;
		bindTextureBatch(getUnicodePageLocation(page));
		int left = glyph >>> 4;
		int right = glyph & 15;
		float fLeft = (float) left;
		float fRight = (float) (right + 1);
		float uBase = (float) (ch % 16 * 16) + fLeft;
		float vBase = (float) ((ch & 255) / 16 * 16);
		float width = fRight - fLeft - 0.02F;
		float italicOffset = italic ? 1F : 0F;

		float u1 = uBase / 256F;
		float v1 = vBase / 256F;
		float u2 = (uBase + width) / 256F;
		float v2 = (vBase + 15.98F) / 256F;

		if (batching) {
			bufferBuilder.pos(x + italicOffset, y, 0F).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(x - italicOffset, y + 7.99F, 0F).tex(u1, v2).color(red, green, blue, alpha).endVertex();
			bufferBuilder.pos(x + width / 2F - italicOffset, y + 7.99F, 0F)
			             .tex(u2, v2)
			             .color(red, green, blue, alpha)
			             .endVertex();
			bufferBuilder.pos(x + width / 2F + italicOffset, y, 0F).tex(u2, v1).color(red, green, blue, alpha).endVertex();
		} else {
			GLS.begin(5);
			GLS.texCoord2f(u1, v1);
			GLS.vertex3f(x + italicOffset, y, 0F);
			GLS.texCoord2f(u1, v2);
			GLS.vertex3f(x - italicOffset, y + 7.99F, 0F);
			GLS.texCoord2f(u2, v1);
			GLS.vertex3f(x + width / 2F + italicOffset, y, 0F);
			GLS.texCoord2f(u2, v2);
			GLS.vertex3f(x + width / 2F - italicOffset, y + 7.99F, 0F);
			GLS.end();
		}

		return (fRight - fLeft) / 2F + 1F;
	}

	public int drawStringWithShadow(String text, float x, float y, int color) {
		return drawString(text, x, y, color, true);
	}

	public int drawString(String text, int x, int y, int color) {
		return drawString(text, (float) x, (float) y, color, false);
	}

	public int drawString(String text, float x, float y, int color, boolean dropShadow) {
		GLS.enableAlpha();
		resetStyles();
		int i;

		if (dropShadow) {
			i = renderString(text, x + 1F, y + 1F, color, true);
			i = Math.max(i, renderString(text, x, y, color, false));
		} else {
			i = renderString(text, x, y, color, false);
		}

		return i;
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

	private void resetStyles() {
		random = false;
		bold = false;
		italic = false;
		underline = false;
		strikethrough = false;
	}

	private void renderStringAtPos(String text, boolean shadow) {
		for (int i = 0; i < text.length(); ++i) {
			char c0 = text.charAt(i);

			if (c0 == 167 && i + 1 < text.length()) {
				char fmt = Character.toLowerCase(text.charAt(i + 1));
				int idx = FORMAT_CODES.indexOf(fmt);

				if (idx < 0) {
					idx = 15;
				}

				if (idx < 16) {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;

					if (shadow) idx += 16;

					int color = colorCode[idx];
					textColor = color;
					GLS.color((float) (color >> 16) / 255F, (float) (color >> 8 & 255) / 255F, (float) (color & 255) / 255F, alpha);
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
				} else if (idx == 21) {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;
					GLS.color(red, green, blue, alpha);
				}

				++i;
			} else {
				int charIdx = CHARS.indexOf(c0);

				if (random && charIdx != -1) {
					int targetWidth = getCharWidth(c0);
					while (true) {
						charIdx = fontRandom.nextInt(CHARS_ARRAY.length);
						c0 = CHARS_ARRAY[charIdx];
						if (targetWidth == getCharWidth(c0)) break;
					}
				}

				float offset = unicode ? 0.5F : 1F;
				boolean needsShift = (c0 == 0 || charIdx == -1 || unicode) && shadow;

				if (needsShift) { x -= offset; y -= offset; }

				float f = renderChar(c0, italic);

				if (needsShift) { x += offset; y += offset; }

				if (bold) {
					x += offset;

					if (needsShift) { x -= offset; y -= offset; }

					renderChar(c0, italic);
					x -= offset;

					if (needsShift) { x += offset; y += offset; }

					++f;
				}

				if (strikethrough) {
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder buf = tessellator.getBuffer();
					endBatch();
					GLS.disableTexture2D();
					buf.begin(7, DefaultVertexFormats.POSITION);
					buf.pos(x, y + (float) (FONT_HEIGHT / 2), 0D).endVertex();
					buf.pos(x + f, y + (float) (FONT_HEIGHT / 2), 0D).endVertex();
					buf.pos(x + f, y + (float) (FONT_HEIGHT / 2) - 1F, 0D).endVertex();
					buf.pos(x, y + (float) (FONT_HEIGHT / 2) - 1F, 0D).endVertex();
					tessellator.draw();
					GLS.enableTexture2D();
					startBatch();
				}

				if (underline) {
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder buf = tessellator.getBuffer();
					endBatch();
					GLS.disableTexture2D();
					buf.begin(7, DefaultVertexFormats.POSITION);
					buf.pos(x - 1F, y + (float) FONT_HEIGHT, 0D).endVertex();
					buf.pos(x + f, y + (float) FONT_HEIGHT, 0D).endVertex();
					buf.pos(x + f, y + (float) FONT_HEIGHT - 1F, 0D).endVertex();
					buf.pos(x - 1F, y + (float) FONT_HEIGHT - 1F, 0D).endVertex();
					tessellator.draw();
					GLS.enableTexture2D();
					startBatch();
				}

				x += (float) ((int) f);
			}
		}
	}

	private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow) {
		if (bidi) {
			int stringWidth = getStringWidth(bidiReorder(text));
			x = x + width - stringWidth;
		}

		return renderString(text, (float) x, (float) y, color, dropShadow);
	}

	private int renderString(String text, float x, float y, int color, boolean dropShadow) {
		if (text == null) return 0;

		if (bidi) text = bidiReorder(text);
		if ((color & -67108864) == 0) color |= -16777216;
		if (dropShadow) color = (color & 16579836) >> 2 | color & -16777216;

		red = (float) (color >> 16 & 255) / 255F;
		green = (float) (color >> 8 & 255) / 255F;
		blue = (float) (color & 255) / 255F;
		alpha = (float) (color >> 24 & 255) / 255F;
		GLS.color(red, green, blue, alpha);
		this.x = x;
		this.y = y;
		startBatch();
		renderStringAtPos(text, dropShadow);
		endBatch();
		return (int) this.x;
	}

	public int getStringWidth(String text) {
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
					if (c0 == 'r' || c0 == 'R') {
						boldActive = false;
					}
				} else {
					boldActive = true;
				}

				charWidth = 0;
			}

			width += charWidth;

			if (boldActive && charWidth > 0) {
				++width;
			}
		}

		return width;
	}

	public int getCharWidth(char character) {
		if (character == 167) return -1;
		if (character == ' ') return 4;

		int idx = CHARS.indexOf(character);

		if (character > 0 && idx != -1 && !unicode) {
			return charWidth[idx];
		} else if (glyphWidth[character] != 0) {
			int glyph = glyphWidth[character] & 255;
			int left = glyph >>> 4;
			int right = glyph & 15;
			++right;
			return (right - left) / 2 + 1;
		}

		return 0;
	}

	public String trimStringToWidth(String text, int width) {
		return trimStringToWidth(text, width, false);
	}

	public String trimStringToWidth(String text, int width, boolean reverse) {
		StringBuilder sb = new StringBuilder();
		int accumulated = 0;
		int start = reverse ? text.length() - 1 : 0;
		int step = reverse ? -1 : 1;
		boolean processingFormat = false;
		boolean boldActive = false;

		for (int i = start; i >= 0 && i < text.length() && accumulated < width; i += step) {
			char c0 = text.charAt(i);
			int charWidth = getCharWidth(c0);

			if (processingFormat) {
				processingFormat = false;

				if (c0 != 'l' && c0 != 'L') {
					if (c0 == 'r' || c0 == 'R') {
						boldActive = false;
					}
				} else {
					boldActive = true;
				}
			} else if (charWidth < 0) {
				processingFormat = true;
			} else {
				accumulated += charWidth;

				if (boldActive) {
					++accumulated;
				}
			}

			if (accumulated > width) break;

			if (reverse) {
				sb.insert(0, c0);
			} else {
				sb.append(c0);
			}
		}

		return sb.toString();
	}

	private String trimStringNewline(String text) {
		while (text != null && text.endsWith("\n")) {
			text = text.substring(0, text.length() - 1);
		}

		return text;
	}

	public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
		resetStyles();
		this.textColor = textColor;
		str = trimStringNewline(str);
		renderSplitString(str, x, y, wrapWidth, false);
	}

	private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow) {
		for (String line : formatToWidth(str, wrapWidth)) {
			renderStringAligned(line, x, y, wrapWidth, textColor, addShadow);
			y += FONT_HEIGHT;
		}
	}

	public int getWordWrappedHeight(String str, int maxLength) {
		return FONT_HEIGHT * formatToWidth(str, maxLength).size();
	}

	public List<String> formatToWidth(String text, int wrapWidth) {
		return Arrays.asList(wrapFormattedStringToWidth(text, wrapWidth).split("\n"));
	}

	String wrapFormattedStringToWidth(String str, int wrapWidth) {
		int splitIndex = sizeStringToWidth(str, wrapWidth);

		if (str.length() > splitIndex) {
			String head = str.substring(0, splitIndex);
			char boundary = str.charAt(splitIndex);
			boolean flag = boundary == ' ' || boundary == '\n';
			String tail = getFormatFromString(head) + str.substring(splitIndex + (flag ? 1 : 0));
			return head + "\n" + wrapFormattedStringToWidth(tail, wrapWidth);
		}

		return str;
	}

	private int sizeStringToWidth(String str, int wrapWidth) {
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
							if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
								boldActive = false;
							}
						} else {
							boldActive = true;
						}
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

	public int getColorCode(char character) {
		int i = "0123456789abcdef".indexOf(character);
		return i >= 0 && i < colorCode.length ? colorCode[i] : -1;
	}
}
