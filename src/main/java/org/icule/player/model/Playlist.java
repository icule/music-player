package org.icule.player.model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<Music> musicList;

    @Inject
    public Playlist() {
        musicList = new ArrayList<>();
    }

    public int getPlaylistLength() {
        return musicList.size();
    }

    public Music getMusic(int index) {
        return musicList.get(index);
    }

    public void addMusic(final Music music) {
        for (int i = 0; i < music.getRating(); ++i) {
            musicList.add(music);
        }
    }
}
