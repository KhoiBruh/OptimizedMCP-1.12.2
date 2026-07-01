package net.minecraft.client.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.nio.ShortBuffer;

public class SoundSource {
    private final int sourceId;
    private boolean active;
    private OggAudioStream stream;
    private int[] streamBuffers;
    private boolean loop;
    private boolean isStreaming;
    private static final int STREAM_BUFFER_SIZE = 44100; // ~0.5s of stereo audio

    public SoundSource() {
        this.sourceId = AL10.alGenSources();
        this.active = false;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public boolean isActive() {
        return this.active;
    }

    public void stop() {
        AL10.alSourceStop(this.sourceId);
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0);
        this.active = false;

        if (this.isStreaming) {
            if (this.streamBuffers != null) {
                AL10.alDeleteBuffers(this.streamBuffers);
                this.streamBuffers = null;
            }
            if (this.stream != null) {
                this.stream.close();
                this.stream = null;
            }
            this.isStreaming = false;
        }
    }

    public void playStatic(int bufferId, float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
        stop();
        this.isStreaming = false;
        configure(pitch, volume, x, y, z, loop, attenuation, referenceDistance);
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, bufferId);
        AL10.alSourcePlay(this.sourceId);
        this.active = true;
    }

    public void playStream(OggAudioStream stream, float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
        stop();
        this.isStreaming = true;
        this.stream = stream;
        this.loop = loop;
        configure(pitch, volume, x, y, z, false, attenuation, referenceDistance);

        this.streamBuffers = new int[3];
        for (int i = 0; i < 3; i++) {
            this.streamBuffers[i] = AL10.alGenBuffers();
        }

        int queued = 0;
        ShortBuffer tempBuffer = BufferUtils.createShortBuffer(STREAM_BUFFER_SIZE);
        for (int i = 0; i < 3; i++) {
            tempBuffer.clear();
            int read = stream.read(tempBuffer);
            if (read > 0) {
                tempBuffer.position(0);
                tempBuffer.limit(read);
                AL10.alBufferData(this.streamBuffers[i], stream.getFormat(), tempBuffer, stream.getSampleRate());
                AL10.alSourceQueueBuffers(this.sourceId, this.streamBuffers[i]);
                queued++;
            } else {
                break;
            }
        }

        if (queued > 0) {
            AL10.alSourcePlay(this.sourceId);
            this.active = true;
        } else {
            stop();
        }
    }

    private void configure(float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
        AL10.alSourcef(this.sourceId, AL10.AL_PITCH, pitch);
        AL10.alSourcef(this.sourceId, AL10.AL_GAIN, volume);
        AL10.alSourcei(this.sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);

        if (attenuation == 0) { // AttenuationType.NONE
            AL10.alSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(this.sourceId, AL10.AL_POSITION, 0, 0, 0);
        } else {
            AL10.alSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(this.sourceId, AL10.AL_POSITION, x, y, z);
            AL10.alSourcef(this.sourceId, AL10.AL_REFERENCE_DISTANCE, referenceDistance);
            AL10.alSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR, 1.0f);
        }
    }

    public void updateStream() {
        if (!this.active || !this.isStreaming || this.stream == null) return;

        int processed = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
        if (processed <= 0) return;

        ShortBuffer tempBuffer = BufferUtils.createShortBuffer(STREAM_BUFFER_SIZE);
        while (processed > 0) {
            int bufferId = AL10.alSourceUnqueueBuffers(this.sourceId);
            tempBuffer.clear();
            int read = this.stream.read(tempBuffer);

            if (read <= 0 && this.loop) {
                this.stream.rewind();
                read = this.stream.read(tempBuffer);
            }

            if (read > 0) {
                tempBuffer.position(0);
                tempBuffer.limit(read);
                AL10.alBufferData(bufferId, this.stream.getFormat(), tempBuffer, this.stream.getSampleRate());
                AL10.alSourceQueueBuffers(this.sourceId, bufferId);
                processed--;
            } else {
                // End of stream, stop streaming
                stop();
                return;
            }
        }

        int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED) {
            AL10.alSourcePlay(this.sourceId);
        }
    }

    public boolean isPlaying() {
        if (!this.active) return false;
        int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED) {
            this.active = false;
            return false;
        }
        return true;
    }

    public void setVolume(float volume) {
        AL10.alSourcef(this.sourceId, AL10.AL_GAIN, volume);
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(this.sourceId, AL10.AL_PITCH, pitch);
    }

    public void setPosition(float x, float y, float z) {
        int relative = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE);
        if (relative == AL10.AL_FALSE) {
            AL10.alSource3f(this.sourceId, AL10.AL_POSITION, x, y, z);
        }
    }

    public void cleanup() {
        stop();
        AL10.alDeleteSources(this.sourceId);
    }
}
