package org.icule.player.database;

public class DatabaseException extends Exception {
    public DatabaseException(final Throwable cause,
                             final String message,
                             final Object... objects) {
        super(String.format(message, objects), cause);
    }
}
