package org.icule.player.model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Playlist {
    private final List<UUID> musicList;

    @Inject
    public Playlist() {
        musicList = new ArrayList<>();
    }

    public int getPlaylistLength() {
        return musicList.size();
    }

    public UUID getMusic(int index) {
        return musicList.get(index);
    }

    public void addMusic(final Music music) {
        for (int i = 0; i < music.getRating(); ++i) {
            musicList.add(music.getId());
        }
    }
}
