package org.icule.player.music;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Random;

public class MusicPlayer {
    private final MediaPlayer mediaPlayer;
    private final AudioPlayerComponent mediaPlayerComponent;
    private final Random random;
    private int listPosition;


    @Inject
    public MusicPlayer() {
        random = new Random(Instant.now().toEpochMilli());

        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventListenerAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                nextMusic();
            }
        });
        mediaPlayerComponent = new AudioPlayerComponent();
        listPosition = 0;
    }

    public void nextMusic() {
    }
}
