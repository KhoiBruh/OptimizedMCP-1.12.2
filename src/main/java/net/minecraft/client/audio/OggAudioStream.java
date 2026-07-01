package net.minecraft.client.audio;

import lombok.Getter;
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

    @Getter
    private final int channels;

    @Getter
    private final int sampleRate;

    @Getter
    private final int format;

    public OggAudioStream(InputStream stream) throws IOException {
	    var bytes = stream.readAllBytes();
        oggBytes = BufferUtils.createByteBuffer(bytes.length);
        oggBytes.put(bytes);
        oggBytes.flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            handle = STBVorbis.stb_vorbis_open_memory(oggBytes, error, null);
            if (handle == 0) throw new IOException("Failed to open Ogg Vorbis stream, error: " + error.get(0));
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(handle, info);
            channels = info.channels();
            sampleRate = info.sample_rate();
            format = (channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        }
    }

	public synchronized int read(ShortBuffer buffer) {
        int read = STBVorbis.stb_vorbis_get_samples_short_interleaved(handle, channels, buffer);
        return read * channels;
    }

    public synchronized void rewind() {
        STBVorbis.stb_vorbis_seek_start(handle);
    }

    public synchronized ShortBuffer readAll() {
        rewind();
        int totalSamples = STBVorbis.stb_vorbis_stream_length_in_samples(handle) * channels;
        ShortBuffer pcmBuffer = BufferUtils.createShortBuffer(totalSamples);
        int read = read(pcmBuffer);
        pcmBuffer.limit(read);
        return pcmBuffer;
    }

    @Override
    public synchronized void close() {
        if (handle != 0) STBVorbis.stb_vorbis_close(handle);
    }
}
