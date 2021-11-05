package org.icule.player.database;

import org.icule.player.configuration.ConfigurationManager;
import org.icule.player.model.Music;
import org.icule.player.model.Tag;
import org.icule.player.model.TagMusicInformation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final Connection connection;

    private final MusicDatabaseInterface musicDatabaseInterface;
    private final TagDatabaseInterface tagDatabaseInterface;

    public DatabaseManager(final ConfigurationManager configurationManager) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        String jdbcString = "jdbc:h2:" + configurationManager.getDatabasePath();
        connection = DriverManager.getConnection(jdbcString);

        musicDatabaseInterface = new MusicDatabaseInterface(this);
        tagDatabaseInterface = new TagDatabaseInterface(this);
    }

    public void init() throws DatabaseException {
        try {
            musicDatabaseInterface.init();
            tagDatabaseInterface.init();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to init the database.");
        }
    }

    public void commit() throws DatabaseException {
        try {
            connection.commit();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to commit to database. Original Error: %s", e.getMessage());
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        }
        catch (SQLException e) {
            //there is nothing to do
        }
    }

    PreparedStatement getStatement(final String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    public synchronized void addMusic(final Music music) throws DatabaseException {
        try {
            musicDatabaseInterface.addMusic(music);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }

    public synchronized void updateMusic(final Music music) throws DatabaseException {
        try {
            musicDatabaseInterface.updateMusic(music);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }

    public synchronized Music getMusic(final UUID id) throws DatabaseException {
        return musicDatabaseInterface.getMusicFromId(id);
    }

    public synchronized List<Music> getAllMusic() throws DatabaseException {
        return musicDatabaseInterface.getAllMusic();
    }

    public synchronized void deleteMusic(final UUID id) throws DatabaseException {
        try {
            tagDatabaseInterface.deleteAllTagForMusic(id);
            musicDatabaseInterface.deleteFromId(id);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }

    public synchronized void addTagMusicInformation(final TagMusicInformation tagMusicInformation) throws DatabaseException {
        try {
            tagDatabaseInterface.addTag(tagMusicInformation);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }

    public synchronized void deleteTagMusicInformation(final TagMusicInformation tagMusicInformation) throws DatabaseException {
        try {
            tagDatabaseInterface.deleteTagForMusic(tagMusicInformation);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }

    public synchronized List<UUID> getAllMusicIdForTag(final Tag tag) throws DatabaseException {
        return tagDatabaseInterface.getMusicIdListForTag(tag);
    }

    public synchronized List<TagMusicInformation> getAllTagForMusic(final UUID id) throws DatabaseException {
        return tagDatabaseInterface.getTagListForMusic(id);
    }

    public synchronized void updateLastModificationTime(final TagMusicInformation tagMusicInformation) throws DatabaseException {
        try {
            tagDatabaseInterface.updateLastModificationTime(tagMusicInformation);
            commit();
        }
        catch (DatabaseException e) {
            rollback();
            throw e;
        }
    }
}
