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
import java.util.Locale;
import java.util.Random;

public class FontRenderer implements IResourceManagerReloadListener {

	private static final ResourceLocation[] UNICODE_PAGES = new ResourceLocation[256];

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

			if (i == 6) {
				k += 85;
			}

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
		NativeImage bufferedimage;

		try (IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(locationFontTexture)) {
			bufferedimage = TextureUtil.readImage(iresource.getInputStream());
		} catch (IOException ioexception) {
			throw new RuntimeException(ioexception);
		}

		int lvt_3_2_ = bufferedimage.getWidth();
		int lvt_4_1_ = bufferedimage.getHeight();
		int[] lvt_5_1_ = new int[lvt_3_2_ * lvt_4_1_];
		bufferedimage.getRGB(0, 0, lvt_3_2_, lvt_4_1_, lvt_5_1_, 0, lvt_3_2_);
		bufferedimage.close();
		int lvt_6_1_ = lvt_4_1_ / 16;
		int lvt_7_1_ = lvt_3_2_ / 16;
		float lvt_9_1_ = 8F / (float) lvt_7_1_;

		for (int lvt_10_1_ = 0; lvt_10_1_ < 256; ++lvt_10_1_) {
			int j1 = lvt_10_1_ % 16;
			int k1 = lvt_10_1_ / 16;

			if (lvt_10_1_ == 32) {
				charWidth[lvt_10_1_] = 4;
			}

			int l1;

			for (l1 = lvt_7_1_ - 1; l1 >= 0; --l1) {
				int i2 = j1 * lvt_7_1_ + l1;
				boolean flag1 = true;

				for (int j2 = 0; j2 < lvt_6_1_; ++j2) {
					int k2 = (k1 * lvt_7_1_ + j2) * lvt_3_2_;

					if ((lvt_5_1_[i2 + k2] >> 24 & 255) != 0) {
						flag1 = false;
						break;
					}
				}

				if (!flag1) {
					break;
				}
			}

			++l1;
			charWidth[lvt_10_1_] = (int) (0.5D + (double) ((float) l1 * lvt_9_1_)) + 1;
		}
	}

	private void readGlyphSizes() {
		try (IResource iresource = Minecraft.getMinecraft()
		                                    .getResourceManager()
		                                    .getResource(new ResourceLocation("font/glyph_sizes.bin"))) {
			iresource.getInputStream().read(glyphWidth);
		} catch (IOException ioexception) {
			throw new RuntimeException(ioexception);
		}
	}

	private float renderChar(char ch, boolean italic) {
		if (ch == ' ') {
			return 4F;
		} else {
			int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(ch);
			return i != -1 && !unicode ? renderDefaultChar(i, italic) : renderUnicodeChar(ch, italic);
		}
	}

	/**
	 * Render a single character with the default.png font at current (posX,posY) location...
	 */
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
		int i = glyphWidth[ch] & 255;

		if (i != 0) {
			int j = ch / 256;
			bindTextureBatch(getUnicodePageLocation(j));
			int k = i >>> 4;
			int l = i & 15;
			float f = (float) k;
			float f1 = (float) (l + 1);
			float f2 = (float) (ch % 16 * 16) + f;
			float f3 = (float) ((ch & 255) / 16 * 16);
			float f4 = f1 - f - 0.02F;
			float f5 = italic ? 1F : 0F;

			float u1 = f2 / 256F;
			float v1 = f3 / 256F;
			float u2 = (f2 + f4) / 256F;
			float v2 = (f3 + 15.98F) / 256F;

			if (batching) {
				bufferBuilder.pos(x + f5, y, 0F).tex(u1, v1).color(red, green, blue, alpha).endVertex();
				bufferBuilder.pos(x - f5, y + 7.99F, 0F).tex(u1, v2).color(red, green, blue, alpha).endVertex();
				bufferBuilder.pos(x + f4 / 2F - f5, y + 7.99F, 0F)
				             .tex(u2, v2)
				             .color(red, green, blue, alpha)
				             .endVertex();
				bufferBuilder.pos(x + f4 / 2F + f5, y, 0F).tex(u2, v1).color(red, green, blue, alpha).endVertex();
			} else {
				GLS.begin(5);
				GLS.texCoord2f(u1, v1);
				GLS.vertex3f(x + f5, y, 0F);
				GLS.texCoord2f(u1, v2);
				GLS.vertex3f(x - f5, y + 7.99F, 0F);
				GLS.texCoord2f(u2, v1);
				GLS.vertex3f(x + f4 / 2F + f5, y, 0F);
				GLS.texCoord2f(u2, v2);
				GLS.vertex3f(x + f4 / 2F - f5, y + 7.99F, 0F);
				GLS.end();
			}

			return (f1 - f) / 2F + 1F;
		}
		else return 0F;
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
		} catch (ArabicShapingException var3) {
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
				int i1 = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1))
				                                                .toLowerCase(Locale.ROOT)
				                                                .charAt(0));

				if (i1 < 16) {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;

					if (i1 < 0 || i1 > 15) {
						i1 = 15;
					}

					if (shadow) {
						i1 += 16;
					}

					int j1 = colorCode[i1];
					textColor = j1;
					GLS.color((float) (j1 >> 16) / 255F, (float) (j1 >> 8 & 255) / 255F, (float) (j1 & 255) / 255F, alpha);
				} else if (i1 == 16) {
					random = true;
				} else if (i1 == 17) {
					bold = true;
				} else if (i1 == 18) {
					strikethrough = true;
				} else if (i1 == 19) {
					underline = true;
				} else if (i1 == 20) {
					italic = true;
				} else if (i1 == 21) {
					random = false;
					bold = false;
					strikethrough = false;
					underline = false;
					italic = false;
					GLS.color(red, green, blue, alpha);
				}

				++i;
			} else {
				int j = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);

				if (random && j != -1) {
					int k = getCharWidth(c0);
					char c1;

					while (true) {
						j = fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
						c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(j);

						if (k == getCharWidth(c1)) {
							break;
						}
					}

					c0 = c1;
				}

				float f1 = unicode ? 0.5F : 1F;
				boolean flag = (c0 == 0 || j == -1 || unicode) && shadow;

				if (flag) {
					x -= f1;
					y -= f1;
				}

				float f = renderChar(c0, italic);

				if (flag) {
					x += f1;
					y += f1;
				}

				if (bold) {
					x += f1;

					if (flag) {
						x -= f1;
						y -= f1;
					}

					renderChar(c0, italic);
					x -= f1;

					if (flag) {
						x += f1;
						y += f1;
					}

					++f;
				}

				if (strikethrough) {
					endBatch();
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					GLS.disableTexture2D();
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
					bufferbuilder.pos(x, y + (float) (FONT_HEIGHT / 2), 0D).endVertex();
					bufferbuilder.pos(x + f, y + (float) (FONT_HEIGHT / 2), 0D).endVertex();
					bufferbuilder.pos(x + f, y + (float) (FONT_HEIGHT / 2) - 1F, 0D).endVertex();
					bufferbuilder.pos(x, y + (float) (FONT_HEIGHT / 2) - 1F, 0D).endVertex();
					tessellator.draw();
					GLS.enableTexture2D();
					startBatch();
				}

				if (underline) {
					endBatch();
					Tessellator tessellator1 = Tessellator.getInstance();
					BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
					GLS.disableTexture2D();
					bufferbuilder1.begin(7, DefaultVertexFormats.POSITION);
					int l = underline ? -1 : 0;
					bufferbuilder1.pos(x + (float) l, y + (float) FONT_HEIGHT, 0D).endVertex();
					bufferbuilder1.pos(x + f, y + (float) FONT_HEIGHT, 0D).endVertex();
					bufferbuilder1.pos(x + f, y + (float) FONT_HEIGHT - 1F, 0D).endVertex();
					bufferbuilder1.pos(x + (float) l, y + (float) FONT_HEIGHT - 1F, 0D).endVertex();
					tessellator1.draw();
					GLS.enableTexture2D();
					startBatch();
				}

				x += (float) ((int) f);
			}
		}
	}

	private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow) {
		if (bidi) {
			int i = getStringWidth(bidiReorder(text));
			x = x + width - i;
		}

		return renderString(text, (float) x, (float) y, color, dropShadow);
	}

	private int renderString(String text, float x, float y, int color, boolean dropShadow) {
		if (text != null) {
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

		return 0;
	}

	public int getStringWidth(String text) {
		if (text != null) {
			int i = 0;
			boolean flag = false;

			for (int j = 0; j < text.length(); ++j) {
				char c0 = text.charAt(j);
				int k = getCharWidth(c0);

				if (k < 0 && j < text.length() - 1) {
					++j;
					c0 = text.charAt(j);

					if (c0 != 'l' && c0 != 'L') {
						if (c0 == 'r' || c0 == 'R') {
							flag = false;
						}
					} else {
						flag = true;
					}

					k = 0;
				}

				i += k;

				if (flag && k > 0) {
					++i;
				}
			}

			return i;
		}

		return 0;
	}

	/**
	 * Returns the width of this character as rendered.
	 */
	public int getCharWidth(char character) {
		if (character == 167) {
			return -1;
		} else if (character == ' ') {
			return 4;
		} else {
			int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(character);

			if (character > 0 && i != -1 && !unicode) {
				return charWidth[i];
			} else if (glyphWidth[character] != 0) {
				int j = glyphWidth[character] & 255;
				int k = j >>> 4;
				int l = j & 15;
				++l;
				return (l - k) / 2 + 1;
			} else {
				return 0;
			}
		}
	}

	public String trimStringToWidth(String text, int width) {
		return trimStringToWidth(text, width, false);
	}

	public String trimStringToWidth(String text, int width, boolean reverse) {
		StringBuilder stringbuilder = new StringBuilder();
		int i = 0;
		int j = reverse ? text.length() - 1 : 0;
		int k = reverse ? -1 : 1;
		boolean flag = false;
		boolean flag1 = false;

		for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
			char c0 = text.charAt(l);
			int i1 = getCharWidth(c0);

			if (flag) {
				flag = false;

				if (c0 != 'l' && c0 != 'L') {
					if (c0 == 'r' || c0 == 'R') {
						flag1 = false;
					}
				} else {
					flag1 = true;
				}
			} else if (i1 < 0) {
				flag = true;
			} else {
				i += i1;

				if (flag1) {
					++i;
				}
			}

			if (i > width) {
				break;
			}

			if (reverse) {
				stringbuilder.insert(0, c0);
			} else {
				stringbuilder.append(c0);
			}
		}

		return stringbuilder.toString();
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
		for (String s : formatToWidth(str, wrapWidth)) {
			renderStringAligned(s, x, y, wrapWidth, textColor, addShadow);
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
		int i = sizeStringToWidth(str, wrapWidth);

		if (str.length() > i) {
			String s = str.substring(0, i);
			char c0 = str.charAt(i);
			boolean flag = c0 == ' ' || c0 == '\n';
			String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
			return s + "\n" + wrapFormattedStringToWidth(s1, wrapWidth);
		}

		return str;
	}

	private int sizeStringToWidth(String str, int wrapWidth) {
		int i = str.length();
		int j = 0;
		int k = 0;
		int l = -1;

		for (boolean flag = false; k < i; ++k) {
			char c0 = str.charAt(k);

			switch (c0) {
				case '\n':
					--k;
					break;

				case ' ':
					l = k;

				case '§':
					if (k < i - 1) {
						++k;
						char c1 = str.charAt(k);

						if (c1 != 'l' && c1 != 'L') {
							if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
								flag = false;
							}
						} else {
							flag = true;
						}
					}

				default:
					j += getCharWidth(c0);

					if (flag) {
						++j;
					}

					break;
			}

			if (c0 == '\n') {
				++k;
				l = k;
				break;
			}

			if (j > wrapWidth) break;
		}

		return k != i && l != -1 && l < k ? l : k;
	}

	public int getColorCode(char character) {
		int i = "0123456789abcdef".indexOf(character);
		return i >= 0 && i < colorCode.length ? colorCode[i] : -1;
	}

}
