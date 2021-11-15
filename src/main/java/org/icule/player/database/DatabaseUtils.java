package org.icule.player.database;

import org.icule.player.model.Music;
import org.icule.player.model.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DatabaseUtils {
    public static List<Music> getAllMusicForPlaylist(final DatabaseManager databaseManager) throws DatabaseException {
        List<Music> knownMusicList = new LinkedList<>(databaseManager.getAllMusic());
        List<UUID> toRemoveList = databaseManager.getAllMusicIdForTag(Tag.TO_REMOVE);

        for (UUID uuid : toRemoveList) {
            knownMusicList.removeIf(u -> u.getId().equals(uuid));
        }
        return knownMusicList;
    }
}
