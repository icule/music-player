package org.icule.player.music;

import javafx.application.Platform;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import org.icule.player.model.Tag;
import org.icule.player.model.TagMusicInformation;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicPlayer {
    public static final int DEFAULT_VOLUME = 71;

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

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float v) {
                if (mediaPlayer.audio().volume() == -1 ) {
                    handleBrokenAudioFile();
                }
            }
        });
    }

    private void handleBrokenAudioFile() {
        try {
            List<TagMusicInformation> currentMusicTagList = databaseManager.getAllTagForMusic(currentMusic.getId());
            if (currentMusicTagList.stream().noneMatch(t -> t.getTag().equals(Tag.TO_FIX))) {
                databaseManager.addTagMusicInformation(new TagMusicInformation(currentMusic.getId(),
                                                                               Tag.TO_FIX,
                                                                               currentMusic.getLastModification()));
            }
        }
        catch (DatabaseException e) {
            e.printStackTrace();
        }
        nextMusic();
    }

    private void setItem(final UUID musicId) {
        try {
            currentMusic = databaseManager.getMusic(musicId);
            Platform.runLater(() -> {
                mediaPlayer.media().play(currentMusic.getPath());
                mediaPlayer.audio().setVolume(currentMusic.getVolumeOffset());
            });
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

    public void setVolume(int volume) {
        mediaPlayer.audio().setVolume(volume);
    }
}
