package org.icule.player.music;

import javafx.application.Platform;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class MusicPlayer {
    private final DatabaseManager databaseManager;
    private final Playlist playlist;

    private final MediaPlayer mediaPlayer;
    private final AudioPlayerComponent mediaPlayerComponent;
    private final Random random;
    private int listPosition;


    @Inject
    public MusicPlayer(final DatabaseManager databaseManager,
                       final Playlist playlist) {
        this.databaseManager = databaseManager;
        this.playlist = playlist;

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

    private void setItem(final int index) {
        listPosition = index;
        try {
            Music music = databaseManager.getMusic(playlist.getMusic(index));
            Platform.runLater(() -> mediaPlayer.media().play(music.getPath()));
        }
        catch (DatabaseException e) {
            Platform.runLater(() -> mediaPlayer.controls().stop());
        }
    }

    public void nextMusic() {
        setItem(random.nextInt(playlist.getPlaylistLength() - 1));
    }

    public void stopMusic() {
        mediaPlayer.controls().pause();
    }

    public void pauseMusic() {
        mediaPlayer.controls().pause();
    }

    public void resumeMusic() {
        mediaPlayer.controls().play();
    }

    public UUID getCurrentMusicId() {
        return playlist.getMusic(listPosition);
    }
}
