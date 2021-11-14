package org.icule.player.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Music;
import org.icule.player.model.Playlist;
import org.icule.player.model.Tag;
import org.icule.player.model.TagMusicInformation;

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
    private final Playlist playlist;
    private Stage stage;

    @Inject
    public RemovedMusicFrame(final DatabaseManager databaseManager,
                             final Playlist playlist) {
        this.databaseManager = databaseManager;
        this.playlist = playlist;
    }

    @FXML
    public void initialize() throws DatabaseException {
        List<UUID> idList = databaseManager.getAllMusicIdForTag(Tag.TO_REMOVE);

        List<Music> allToRemove = new ArrayList<>();
        for (UUID uuid : idList) {
            allToRemove.add(databaseManager.getMusic(uuid));
        }
        musicListView.setItems(FXCollections.observableList(allToRemove));

        musicListView.setCellFactory(new Callback<ListView<Music>, ListCell>() {
            @Override
            public ListCell<Music> call(ListView<Music> listView) {
                return new ListCell<>(){
                    @Override
                    public void updateItem(Music music, boolean empty) {
                        super.updateItem(music, empty);
                        if (empty || music == null) {
                            setText(null);
                        } else {
                            setText(music.getPath());
                        }
                    }
                };
            }
        });
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> onCloseAction());
    }

    public void onRemoveTagAction() throws DatabaseException {
        Music selected = (Music) musicListView.getSelectionModel().getSelectedItem();
        TagMusicInformation tagMusicInformation = new TagMusicInformation(selected.getId(), Tag.TO_REMOVE, selected.getLastModification());
        databaseManager.deleteTagMusicInformation(tagMusicInformation);
        playlist.addMusic(selected);
    }

    public void onCloseAction() {
        stage.close();
    }
}
