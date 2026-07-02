package net.minecraft.client.gui;

public interface GlyphSink {
    void glyph(int textureId, float left, float top, float right, float bottom,
               float u1, float v1, float u2, float v2,
               float r, float g, float b, float a, float italicOffset);

    void rect(float x1, float y1, float x2, float y2,
              float r, float g, float b, float a);
}
