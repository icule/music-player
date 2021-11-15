package org.icule.player.music;

import javafx.application.Platform;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicPlayer {
    private final DatabaseManager databaseManager;
    private final Playlist playlist;
    private final List<MusicListener> musicListenerList;

    private final MediaPlayer mediaPlayer;
    private Music currentMusic;


    @Inject
    public MusicPlayer(final DatabaseManager databaseManager,
                       final Playlist playlist) {
        this.databaseManager = databaseManager;
        this.playlist = playlist;
        musicListenerList = new ArrayList<>();

        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventListenerAdapter() {
            @Override
            public void finished(final MediaPlayer mediaPlayer) {
                nextMusic();
            }
            @Override
            public void error(final MediaPlayer mediaPlayer) {
                nextMusic();
            }
        });
    }

    private void setItem(final UUID musicId) {
        try {
            currentMusic = databaseManager.getMusic(musicId);
            Platform.runLater(() -> mediaPlayer.media().play(currentMusic.getPath()));
            fireNewMusic();
        }
        catch (DatabaseException e) {
            Platform.runLater(() -> mediaPlayer.controls().stop());
        }
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void nextMusic() {
        setItem(playlist.getNextMusic());
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

    public void addMusicListener(final MusicListener musicListener) {
        this.musicListenerList.add(musicListener);
    }

    public void removeMusicListener(final MusicListener musicListener) {
        this.musicListenerList.remove(musicListener);
    }

    private void fireNewMusic() {
        for (MusicListener listener : musicListenerList) {
            listener.musicStarted(currentMusic.getId());
        }
    }
}
