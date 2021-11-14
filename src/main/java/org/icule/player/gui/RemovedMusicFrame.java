package org.icule.player.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.*;
import org.icule.player.music.MusicUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemovedMusicFrame implements ChangeListener<Music> {
    public Label titleLabel;
    public Label artistLabel;
    public Label albumLabel;
    public ListView<Music> musicListView;

    private final DatabaseManager databaseManager;
    private final Playlist playlist;
    public Button removeTagButton;
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

        musicListView.setCellFactory(musicListView1 -> {
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
        });
        musicListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicListView.getSelectionModel().selectedItemProperty().addListener(this);
        removeTagButton.setDisable(true);
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> onCloseAction());
    }

    public void onRemoveTagAction() throws DatabaseException {
        Music selected = musicListView.getSelectionModel().getSelectedItem();
        TagMusicInformation tagMusicInformation = new TagMusicInformation(selected.getId(), Tag.TO_REMOVE, selected.getLastModification());
        databaseManager.deleteTagMusicInformation(tagMusicInformation);
        playlist.addMusic(selected);
        musicListView.getItems().remove(selected);
    }

    public void onCloseAction() {
        stage.close();
    }

    private void resetField() {
        titleLabel.setText("");
        artistLabel.setText("");
        albumLabel.setText("");
    }

    private void displayMusic(final Music music) {
        MusicInformation musicInformation = MusicUtils.getMusicInformation(music.getPath());
        titleLabel.setText(musicInformation.getTitle());
        artistLabel.setText(musicInformation.getArtist());
        albumLabel.setText(musicInformation.getAlbum());
    }

    @Override
    public void changed(ObservableValue<? extends Music> observableValue, Music music, Music t1) {
        if (t1 == null) {
            resetField();
            removeTagButton.setDisable(true);
        }
        else {
            displayMusic(t1);
            removeTagButton.setDisable(false);
        }
    }
}
