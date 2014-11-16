package gamax92.jsfgblaster.audio;

import pl.asie.lib.audio.StreamingAudioPlayer;
import pl.asie.lib.audio.StreamingPlaybackManager;

public class SFXRPlaybackManager extends StreamingPlaybackManager {

	public SFXRPlaybackManager(boolean isClient) {
		super(isClient);
	}

	@Override
	public StreamingAudioPlayer create() {
		return new StreamingAudioPlayer(32768, false, false, 4); //(int) Math.round(Config.TAPEDRIVE_BUFFER_MS / 250));
	}

}
