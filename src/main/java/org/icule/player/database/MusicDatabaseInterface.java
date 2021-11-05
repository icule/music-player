package org.icule.player.database;

import org.icule.player.model.Music;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicDatabaseInterface {
    static final String TABLE_NAME = "MusicInfo";
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            "id uuid PRIMARY_KEY, " +
            "path varchar(512), " +
            "rating int, " +
            "lastModification long);";

    private static final String SELECT_FROM_ID_QUERY = "SELECT * FROM " + TABLE_NAME +
            " WHERE id = ?;";

    private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME +
            " VALUES(?, ?, ?, ?);";

    private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME +
            " SET path = ?, rating = ?, filename = ? " +
            " WHERE id = ?;";

    private static final String GET_ALL_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE 1;";

    private static final String DELETE_FROM_ID_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

    private final DatabaseManager databaseManager;

    public MusicDatabaseInterface(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    void init() throws SQLException {
        try (PreparedStatement statement = databaseManager.getStatement(CREATE_TABLE_QUERY)) {
            statement.execute();
        }
    }

    void addMusic(final Music music) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(INSERT_QUERY)) {
            statement.setObject(1, music.getId());
            statement.setString(2, music.getPath());
            statement.setInt(3, music.getRating());
            statement.setLong(4, music.getLastModification());

            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to add music to database.");
        }
    }

    void updateMusic(final Music music) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(UPDATE_QUERY)) {
            statement.setString(1, music.getPath());
            statement.setInt(2, music.getRating());
            statement.setLong(3, music.getLastModification());

            statement.setObject(4, music.getId());
            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to update music %s", music.getId());
        }
    }

    private Music extractMusic(final ResultSet resultSet) throws SQLException {
        return new Music((UUID)resultSet.getObject(1),
                         resultSet.getString(2),
                         resultSet.getInt(3),
                         resultSet.getLong(4));
    }

    Music getMusicFromId(final UUID uuid) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(SELECT_FROM_ID_QUERY)) {
            statement.setObject(1, uuid);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractMusic(resultSet);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to query the music %s", uuid);
        }
    }

    List<Music> getAllMusic() throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(GET_ALL_QUERY)) {
            ResultSet resultSet = statement.executeQuery();

            List<Music> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(extractMusic(resultSet));
            }
            return res;
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to extract all the music of the database.");
        }
    }

    void deleteFromId(final UUID id) throws DatabaseException {
        try (PreparedStatement statement = databaseManager.getStatement(DELETE_FROM_ID_QUERY)) {
            statement.setObject(1, id);

            statement.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e, "Impossible to delete the music %s", id);
        }
    }
}
