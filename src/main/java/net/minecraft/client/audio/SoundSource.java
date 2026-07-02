package net.minecraft.client.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.nio.ShortBuffer;

public class SoundSource {

	private static final int STREAM_BUFFER_SIZE = 44100; // ~0.5s of stereo audio
	private final int sourceId;
	private boolean active;
	private OggAudioStream stream;
	private int[] streamBuffers;
	private boolean loop;
	private boolean streaming;

	public SoundSource() {
		sourceId = AL10.alGenSources();
		active = false;
	}

	public int getSourceId() {
		return sourceId;
	}

	public boolean isActive() {
		return active;
	}

	public void stop() {
		AL10.alSourceStop(sourceId);
		AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0);
		active = false;

		if (streaming) {
			if (streamBuffers != null) {
				AL10.alDeleteBuffers(streamBuffers);
				streamBuffers = null;
			}
			if (stream != null) {
				stream.close();
				stream = null;
			}
			streaming = false;
		}
	}

	public void playStatic(int bufferId, float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
		stop();
		streaming = false;
		configure(pitch, volume, x, y, z, loop, attenuation, referenceDistance);
		AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
		AL10.alSourcePlay(sourceId);
		active = true;
	}

	public void playStream(OggAudioStream stream, float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
		stop();
		streaming = true;
		this.stream = stream;
		this.loop = loop;
		configure(pitch, volume, x, y, z, false, attenuation, referenceDistance);

		streamBuffers = new int[3];
		for (int i = 0; i < 3; i++) {
			streamBuffers[i] = AL10.alGenBuffers();
		}

		int queued = 0;
		ShortBuffer buffer = BufferUtils.createShortBuffer(STREAM_BUFFER_SIZE);
		for (int i = 0; i < 3; i++) {
			buffer.clear();
			int read = stream.read(buffer);

			if (read > 0) {
				buffer.position(0);
				buffer.limit(read);
				AL10.alBufferData(streamBuffers[i], stream.getFormat(), buffer, stream.getSampleRate());
				AL10.alSourceQueueBuffers(sourceId, streamBuffers[i]);
				queued++;
			} else break;
		}

		if (queued > 0) {
			AL10.alSourcePlay(sourceId);
			active = true;
		} else stop();
	}

	private void configure(float pitch, float volume, float x, float y, float z, boolean loop, int attenuation, float referenceDistance) {
		AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
		AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
		AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);

		if (attenuation == 0) {
			AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
			AL10.alSource3f(sourceId, AL10.AL_POSITION, 0, 0, 0);
		} else {
			AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
			AL10.alSource3f(sourceId, AL10.AL_POSITION, x, y, z);
			AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, referenceDistance);
			AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, 1.0f);
		}
	}

	public void updateStream() {
		if (!active || !streaming || stream == null) return;

		int processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
		if (processed <= 0) return;

		ShortBuffer buffer = BufferUtils.createShortBuffer(STREAM_BUFFER_SIZE);
		while (processed > 0) {
			int bufferId = AL10.alSourceUnqueueBuffers(sourceId);
			buffer.clear();
			int read = stream.read(buffer);

			if (read <= 0 && loop) {
				stream.rewind();
				read = stream.read(buffer);
			}

			if (read > 0) {
				buffer.position(0);
				buffer.limit(read);
				AL10.alBufferData(bufferId, stream.getFormat(), buffer, stream.getSampleRate());
				AL10.alSourceQueueBuffers(sourceId, bufferId);
				processed--;
			} else {
				stop();
				return;
			}
		}

		int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) AL10.alSourcePlay(sourceId);
	}

	public boolean isPlaying() {
		if (!active) return false;
		int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			active = false;
			return false;
		}

		return true;
	}

	public void setVolume(float volume) {
		AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
	}

	public void setPitch(float pitch) {
		AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
	}

	public void setPosition(float x, float y, float z) {
		int relative = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_RELATIVE);
		if (relative == AL10.AL_FALSE) AL10.alSource3f(sourceId, AL10.AL_POSITION, x, y, z);
	}

	public void cleanup() {
		stop();
		AL10.alDeleteSources(sourceId);
	}

}
