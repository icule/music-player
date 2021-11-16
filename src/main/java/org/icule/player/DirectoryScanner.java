package org.icule.player;

import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import org.icule.player.music.MusicUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class DirectoryScanner {
    private final Playlist playlist;
    private final DatabaseManager databaseManager;

    @Inject
    public DirectoryScanner(final Playlist playlist,
                            final DatabaseManager databaseManager) {
        this.playlist = playlist;
        this.databaseManager = databaseManager;
    }

    public void scanDirectory(final File file) {
        File[] fileList = file.listFiles();
        if (fileList == null) {
            return;
        }

        for (File f : fileList) {
            if (f.isDirectory()) {
                scanDirectory(f);
            }
            else {
                try {
                    String mimeType = Files.probeContentType(f.toPath());
                    if (mimeType == null || mimeType.contains("audio")) {
                        treatMusic(f);
                    }
                }
                catch (IOException e) {
                    System.out.println("Impossible to get the mimeType of file " + f.getAbsolutePath());
                }
                catch (DatabaseException e) {
                    System.out.println("Impossible to add the music in database");
                }
            }
        }
    }

    private void treatMusic(final File file) throws IOException, DatabaseException {
        Music toAdd = MusicUtils.getMusicFromFile(file);
        Music inDatabase = databaseManager.getMusic(toAdd.getId());
        if (inDatabase == null) {
            databaseManager.addMusic(toAdd);
            playlist.addMusic(toAdd);
        }
        else {
            databaseManager.updateMusic(toAdd.withRating(inDatabase.getRating()).withVolumeOffset(inDatabase.getVolumeOffset()));
        }
    }

    public void checkDatabaseExistence() throws DatabaseException {
        List<Music> musicList = databaseManager.getAllMusic();
        for (Music music : musicList) {
            if (!new File(music.getPath()).exists()) {
                databaseManager.deleteMusic(music.getId());
            }
        }
    }
}
