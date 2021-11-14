package org.icule.player.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Tag;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemovedMusicFrame {
    public Label titleLabel;
    public Label artistLabel;
    public Label albumLabel;
    public ListView musicListView;

    private final DatabaseManager databaseManager;
    private Stage stage;

    @Inject
    public RemovedMusicFrame(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @FXML
    public void initialize() throws DatabaseException {
        List<UUID> idList = databaseManager.getAllMusicIdForTag(Tag.TO_REMOVE);

        List<Music> allToRemove = new ArrayList<>();
        for (UUID uuid : idList) {
            allToRemove.add(databaseManager.getMusic(uuid));
        }
        musicListView.setItems(FXCollections.observableList(allToRemove));
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> onCloseAction());
    }

    public void onRemoveTagAction() {

    }

    public void onCloseAction() {
        stage.close();
    }
}
