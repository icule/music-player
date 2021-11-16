package org.icule.player.database;

import org.icule.player.model.Tag;
import org.icule.player.model.TagMusicInformation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TagDatabaseInterface {
    private static final String TABLE_NAME = "TagTable";
    private static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            "id uuid not null," +
            "tag varchar(25) not null," +
            "lastModification long not null" +
            ");" +
            "ALTER TABLE " + TABLE_NAME + " " +
            "ADD FOREIGN KEY (id) REFERENCES " + MusicDatabaseInterface.TABLE_NAME + "(id);" +
            "CREATE UNIQUE INDEX IF NOT EXISTS tagUnique ON " + TABLE_NAME + "(id, tag);";

    private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME +
            " VALUES(?, ?, ?);";

    private static final String SELECT_FROM_ID = "SELECT * FROM " + TABLE_NAME +
            " WHERE id = ?;";

    private static final String SELECT_FROM_ID_TAG_QUERY = "SELECT * FROM " + TABLE_NAME +
            " WHERE id = ? AND tag = ?;";

    private static final String GET_ALL_ID_FOR_TAG_QUERY = "SELECT id FROM " + TABLE_NAME +
            " WHERE tag = ?;";

    private static final String DELETE_ALL_TAG_FOR_ID_QUERY = "DELETE FROM " + TABLE_NAME +
            " WHERE id = ?;";

    private static final String DELETE_TAG_FOR_ID_QUERY = "DELETE FROM " + TABLE_NAME +
            " WHERE id = ? AND tag = ?;";

    private static final String UPDATE_LAST_MODIFICATION_QUERY = "UPDATE " + TABLE_NAME +
            " SET lastModification=? " +
            " WHERE id = ? AND tag = ?;";

    private final DatabaseManager databaseManager;

    TagDatabaseInterface(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    void init() throws SQLException {
        try (PreparedStatement statement = databaseManager.getStatement(CREATE_QUERY)) {
            statement.execute();
        }
    }

    void addTag(final TagMusicInformation tag) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(INSERT_QUERY)) {
            statement.setObject(1, tag.getMusicId());
            statement.setString(2, tag.getTag().name());
            statement.setLong(3, tag.getLastModification());

            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to insert tag for music %s", tag.getMusicId());
        }
    }

    TagMusicInformation extractTagMusicInformation(final ResultSet resultSet) throws SQLException {
        return new TagMusicInformation((UUID) resultSet.getObject(1),
                                       Tag.valueOf(resultSet.getString(2)),
                                       resultSet.getLong(3));
    }

    List<TagMusicInformation> getTagListForMusic(final UUID id) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(SELECT_FROM_ID)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            List<TagMusicInformation> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(extractTagMusicInformation(resultSet));
            }
            return res;
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to get the tag list for music %s", id);
        }
    }

    TagMusicInformation getTagForMusic(final UUID id, final Tag tag) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(SELECT_FROM_ID_TAG_QUERY)) {
            statement.setObject(1, id);
            statement.setString(2, tag.name());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractTagMusicInformation(resultSet);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to get the tag for music %s", id);
        }
    }

    List<UUID> getMusicIdListForTag(final Tag tag) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(GET_ALL_ID_FOR_TAG_QUERY)) {
            statement.setString(1, tag.name());
            ResultSet resultSet = statement.executeQuery();

            List<UUID> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add((UUID) resultSet.getObject(1));
            }
            return res;
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to get all the music id for tag %s", tag);
        }
    }

    void deleteAllTagForMusic(final UUID id) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(DELETE_ALL_TAG_FOR_ID_QUERY)) {
            statement.setObject(1, id);
            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to delete the tag for music %s", id);
        }
    }

    void deleteTagForMusic(final TagMusicInformation tagMusicInformation) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(DELETE_TAG_FOR_ID_QUERY)) {
            statement.setObject(1, tagMusicInformation.getMusicId());
            statement.setString(2, tagMusicInformation.getTag().name());

            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to delete tag %s for music %s", tagMusicInformation.getTag(), tagMusicInformation.getMusicId());
        }
    }

    void updateLastModificationTime(final TagMusicInformation tagMusicInformation) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(UPDATE_LAST_MODIFICATION_QUERY)) {
            statement.setLong(1, tagMusicInformation.getLastModification());
            statement.setObject(2, tagMusicInformation.getMusicId());
            statement.setString(3, tagMusicInformation.getTag().name());

            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to update the last modification time for the tag %s of music %s", tagMusicInformation.getTag(), tagMusicInformation.getMusicId());
        }
    }
}
