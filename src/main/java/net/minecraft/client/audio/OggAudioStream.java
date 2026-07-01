package net.minecraft.client.audio;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OggAudioStream implements AutoCloseable {
    private final ByteBuffer oggBytes;
    private final long handle;
    private final int channels;
    private final int sampleRate;
    private final int format;

    public OggAudioStream(InputStream stream) throws IOException {
        byte[] bytes = IOUtils.toByteArray(stream);
        this.oggBytes = BufferUtils.createByteBuffer(bytes.length);
        this.oggBytes.put(bytes);
        this.oggBytes.flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            this.handle = STBVorbis.stb_vorbis_open_memory(this.oggBytes, error, null);
            if (this.handle == 0) {
                throw new IOException("Failed to open Ogg Vorbis stream, error: " + error.get(0));
            }
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(this.handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
            this.format = (this.channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        }
    }

    public int getChannels() {
        return this.channels;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public int getFormat() {
        return this.format;
    }

    public synchronized int read(ShortBuffer buffer) {
        int read = STBVorbis.stb_vorbis_get_samples_short_interleaved(this.handle, this.channels, buffer);
        return read * this.channels;
    }

    public synchronized void rewind() {
        STBVorbis.stb_vorbis_seek_start(this.handle);
    }

    public synchronized ShortBuffer readAll() {
        rewind();
        int totalSamples = STBVorbis.stb_vorbis_stream_length_in_samples(this.handle) * this.channels;
        ShortBuffer pcmBuffer = BufferUtils.createShortBuffer(totalSamples);
        int read = read(pcmBuffer);
        pcmBuffer.limit(read);
        return pcmBuffer;
    }

    @Override
    public synchronized void close() {
        if (this.handle != 0) {
            STBVorbis.stb_vorbis_close(this.handle);
        }
    }
}
