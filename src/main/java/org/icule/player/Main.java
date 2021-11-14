package org.icule.player;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.icule.player.configuration.ConfigurationManager;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.gui.FXMLLoaderFactory;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import org.icule.player.model.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Main extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ConfigurationManager configurationManager = ConfigurationManager.loadConfiguration(getParameters().getRaw().get(0));

        Injector injector = Guice.createInjector(new MainModule(configurationManager));
        FXMLLoaderFactory.parametrize(injector);

        DatabaseManager databaseManager = injector.getInstance(DatabaseManager.class);
        databaseManager.init();
        injector.getInstance(Playlist.class).initPlaylist(getAllMusicToAdd(databaseManager));

        FXMLLoader loader = FXMLLoaderFactory.getLoader();
        loader.setLocation(getClass().getResource("/org/icule/player/gui/MainFrame.fxml"));

        BorderPane pane = loader.load();
        stage.setScene(new Scene(pane));
        stage.setTitle("Music player");

        stage.show();
    }

    private List<Music> getAllMusicToAdd(final DatabaseManager databaseManager) throws DatabaseException {
        List<Music> knownMusicList = new LinkedList<>(databaseManager.getAllMusic());
        List<UUID> toRemoveList = databaseManager.getAllMusicIdForTag(Tag.TO_REMOVE);

        for (UUID uuid : toRemoveList) {
            knownMusicList.removeIf(u -> u.getId().equals(uuid));
        }
        return knownMusicList;
    }
}
