package org.lwjgl.opengl;

public class PixelFormat {

    private int bitsPerPixel;
    private int alphaBits;
    private int depthBits;
    private int stencilBits;
    private int samples;
    private int numSamples;

    public PixelFormat() {
    }

    public PixelFormat(int bitsPerPixel, int alphaBits, int depthBits, int stencilBits, int samples) {
        this.bitsPerPixel = bitsPerPixel;
        this.alphaBits = alphaBits;
        this.depthBits = depthBits;
        this.stencilBits = stencilBits;
        this.samples = samples;
    }

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    public int getAlphaBits() {
        return alphaBits;
    }

    public int getDepthBits() {
        return depthBits;
    }

    public int getStencilBits() {
        return stencilBits;
    }

    public int getSamples() {
        return samples;
    }

    public PixelFormat withBitsPerPixel(int bits) {
        this.bitsPerPixel = bits;
        return this;
    }

    public PixelFormat withAlphaBits(int alpha) {
        this.alphaBits = alpha;
        return this;
    }

    public PixelFormat withDepthBits(int depth) {
        this.depthBits = depth;
        return this;
    }

    public PixelFormat withStencilBits(int stencil) {
        stencilBits = stencil;
        return this;
    }

    public PixelFormat withSamples(int samples) {
        this.samples = samples;
        return this;
    }

}
