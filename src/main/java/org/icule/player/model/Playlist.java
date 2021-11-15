package org.icule.player.model;

import javax.inject.Inject;
import java.util.*;

public class Playlist {
    private final List<UUID> musicList;
    private final Random random;

    @Inject
    public Playlist() {
        musicList = new LinkedList<>();
        random = new Random();
    }

    public synchronized UUID getNextMusic() {
        return musicList.get(random.nextInt(musicList.size() - 1));
    }

    public synchronized void addMusic(final Music music) {
        for (int i = 0; i < music.getRating(); ++i) {
            musicList.add(music.getId());
        }
    }

    public synchronized void updateRating(final Music music) {
        musicList.removeIf(u -> u.equals(music.getId()));
        addMusic(music);
    }

    public synchronized void removeMusic(final Music music) {
        musicList.removeIf(u -> u .equals(music.getId()));
    }

    public synchronized void initPlaylist(final List<Music> initialList) {
        musicList.clear();
        for (Music music : initialList) {
            addMusic(music);
        }
    }
}
