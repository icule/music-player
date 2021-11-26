package org.icule.player.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.*;
import org.icule.player.music.MusicUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ToFixMusicFrame implements ChangeListener<Music> {
    private static PseudoClass modifiedClass = PseudoClass.getPseudoClass("modified");

    public Label titleLabel;
    public Label artistLabel;
    public Label albumLabel;

    public ListView<Music> musicListView;

    private final DatabaseManager databaseManager;
    public Button removeTagButton;
    public Button actualizeTagButton;
    private Stage stage;
    private Map<UUID, Long> lastModificationMap;

    @Inject
    public ToFixMusicFrame(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @FXML
    public void initialize() throws DatabaseException {
        List<UUID> idList = databaseManager.getAllMusicIdForTag(Tag.TO_FIX);

        List<Music> allToFix = new ArrayList<>();
        lastModificationMap = new HashMap<>();

        for (UUID uuid : idList) {
            allToFix.add(databaseManager.getMusic(uuid));
            TagMusicInformation tag = databaseManager.getTagForMusic(uuid, Tag.TO_FIX);
            lastModificationMap.put(tag.getMusicId(), tag.getLastModification());
        }
        musicListView.setItems(FXCollections.observableList(allToFix));

        musicListView.setCellFactory(musicListView1 -> {
            return new ListCell<>(){
                @Override
                public void updateItem(Music music, boolean empty) {
                    super.updateItem(music, empty);
                    if (empty || music == null) {
                        setText(null);
                    } else {
                        setText(music.getPath());
                        if (music.getLastModification() > lastModificationMap.get(music.getId())) {
                            pseudoClassStateChanged(modifiedClass, true);
                        }
                        else {
                            pseudoClassStateChanged(modifiedClass, false);
                        }
                    }
                }
            };
        });
        musicListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicListView.getSelectionModel().selectedItemProperty().addListener(this);
        removeTagButton.setDisable(true);
        actualizeTagButton.setDisable(true);
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> onCloseAction());
    }

    @FXML
    public void onRemoveTagAction() throws DatabaseException {
        Music selected = musicListView.getSelectionModel().getSelectedItem();
        TagMusicInformation tagMusicInformation = new TagMusicInformation(selected.getId(), Tag.TO_FIX, selected.getLastModification());
        databaseManager.deleteTagMusicInformation(tagMusicInformation);
        musicListView.getItems().remove(selected);
    }

    @FXML
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
            actualizeTagButton.setDisable(true);

        }
        else {
            displayMusic(t1);
            removeTagButton.setDisable(false);
            if (lastModificationMap.get(t1.getId()) < t1.getLastModification()) {
                actualizeTagButton.setDisable(false);
            }
            else {
                actualizeTagButton.setDisable(true);
            }
        }
    }

    @FXML
    public void onExportAction() throws IOException {
        FXMLLoader loader = FXMLLoaderFactory.getLoader();
        loader.setLocation(getClass().getResource("/org/icule/player/gui/ExportFrame.fxml"));

        BorderPane pane = loader.load();
        Stage stage = new Stage();
        stage.setScene(FXMLLoaderFactory.getScene(pane));
        stage.setTitle("Music tagged with removed.");

        ExportFrame frame = loader.getController();

        List<UUID> idList = musicListView.getItems().stream().map(Music::getId).collect(Collectors.toList());
        ExportData content = new ExportData(Tag.TO_CUT, Instant.now(), idList);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        frame.setContent(gson.toJson(content));

        stage.show();
    }

    @FXML
    public void onActualizeTagAction() throws DatabaseException {
        Music selected = musicListView.getSelectionModel().getSelectedItem();
        TagMusicInformation tagMusicInformation = databaseManager.getTagForMusic(selected.getId(), Tag.TO_FIX);
        databaseManager.updateLastModificationTime(tagMusicInformation.withLastModification(selected.getLastModification()));
        lastModificationMap.put(selected.getId(), selected.getLastModification());
        musicListView.refresh();
    }
}
