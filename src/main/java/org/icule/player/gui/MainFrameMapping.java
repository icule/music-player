package org.icule.player.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.icule.player.DirectoryScanner;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.database.DatabaseUtils;
import org.icule.player.model.*;
import org.icule.player.music.MusicListener;
import org.icule.player.music.MusicPlayer;
import org.icule.player.music.MusicUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainFrameMapping implements MusicListener {
    public Label titleLabel;
    public Label artistLabel;
    public Label albumLabel;
    public Label idLabel;
    public Label pathLabel;
    public Label durationLabel;
    public Label tagLabel;

    @FXML
    public ComboBox<Integer> rateComboBox;
    public Slider volumeSlider;

    @FXML
    private ComboBox<Tag> tagCombo;

    private final MusicPlayer musicPlayer;
    private final DatabaseManager databaseManager;
    private final DirectoryScanner directoryScanner;
    private final Playlist playlist;

    @Inject
    public MainFrameMapping(final MusicPlayer musicPlayer,
                            final DatabaseManager databaseManager,
                            final DirectoryScanner directoryScanner,
                            final Playlist playlist) {
        this.musicPlayer = musicPlayer;
        this.databaseManager = databaseManager;
        this.directoryScanner = directoryScanner;
        this.playlist = playlist;

        musicPlayer.addMusicListener(this);
    }


    @FXML
    private void initialize() {
        tagCombo.setItems(FXCollections.observableArrayList(Tag.values()));

        List<Integer> ratingList = new ArrayList<>();
        for (int i = 1; i <= 5; ++i) {
            ratingList.add(i);
        }
        rateComboBox.setItems(FXCollections.observableList(ratingList));
        volumeSlider.valueProperty().addListener((observableValue, number, t1) -> handleVolumeChange());
    }

    @FXML
    public void nextButtonAction() {
        musicPlayer.nextMusic();
    }

    @FXML
    public void playButtonAction() {
        musicPlayer.resumeMusic();
    }

    @FXML
    public void pauseButtonAction() {
        musicPlayer.pauseMusic();
    }

    @FXML
    public void stopActionButton() {
        musicPlayer.stopMusic();
    }

    @FXML
    public void tagButtonAction() {
        if (tagCombo.getSelectionModel().isEmpty()) {
            return;
        }

        try {
            Tag selectedTag = tagCombo.getValue();
            UUID currentMusicId = musicPlayer.getCurrentMusic().getId();
            List<TagMusicInformation> tagList = databaseManager.getAllTagForMusic(currentMusicId);
            Music music = databaseManager.getMusic(currentMusicId);
            if (tagList.stream().anyMatch(t -> t.getTag() == selectedTag)) {
                TagMusicInformation tagMusicInformation = tagList.stream().filter(t -> t.getTag() == selectedTag).findFirst().get();
                databaseManager.deleteTagMusicInformation(tagMusicInformation);
                tagList.remove(tagMusicInformation);
            }
            else {

                TagMusicInformation tagMusicInformation = new TagMusicInformation(music.getId(),
                                                                                  selectedTag,
                                                                                  music.getLastModification());
                databaseManager.addTagMusicInformation(tagMusicInformation);
                tagList.add(tagMusicInformation);

                playlist.removeMusic(music);
                musicPlayer.nextMusic();
            }

            displayTagList(tagList);
        }
        catch (DatabaseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(FXMLLoaderFactory.class.getResource("/application.css").toExternalForm());
            alert.setTitle("Operation error.");
            alert.setHeaderText(null);
            alert.setContentText("Impossible to add the tag to music.");

            alert.showAndWait();
        }
    }

    @FXML
    public void onScanDirectoryAction() throws DatabaseException {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Select a directory.");
        File selected = fileChooser.showDialog(null);
        if (selected != null) {
            directoryScanner.scanDirectory(selected);
            playlist.initPlaylist(DatabaseUtils.getAllMusicForPlaylist(databaseManager));
        }
    }

    @Override
    public void musicStarted(final UUID musicId) {
        Platform.runLater((() -> displayMusicInfo(musicId)));
    }

    private void displayMusicInfo(final UUID musicId) {
        try {
            Music music = databaseManager.getMusic(musicId);
            MusicInformation musicInformation = MusicUtils.getMusicInformation(music.getPath());
            List<TagMusicInformation> tagList = databaseManager.getAllTagForMusic(musicId);

            titleLabel.setText(musicInformation.getTitle());
            artistLabel.setText(musicInformation.getArtist());
            albumLabel.setText(musicInformation.getArtist());
            durationLabel.setText("" + musicInformation.getDuration() / 1000); //duration is in ms
            pathLabel.setText(music.getPath());
            pathLabel.setTooltip(new Tooltip(music.getPath()));
            idLabel.setText(music.getId().toString());

            rateComboBox.getSelectionModel().select(music.getRating() - 1);
            volumeSlider.setValue(music.getVolumeOffset());

            displayTagList(tagList);
        }
        catch (Exception e) {
            titleLabel.setText("Impossible to get the information from music");
        }
    }

    private void displayTagList(final List<TagMusicInformation> toDisplayList) {
        StringBuilder tagListBuilder = new StringBuilder();
        for (TagMusicInformation tagMusicInformation : toDisplayList) {
            tagListBuilder.append("[").append(tagMusicInformation.getTag()).append("] ");
        }
        tagLabel.setText(tagListBuilder.toString());
    }

    @FXML
    public void onRateAction() {
        if (rateComboBox.getSelectionModel().isEmpty()) {
            return;
        }
        try {
            Music music = databaseManager.getMusic(musicPlayer.getCurrentMusic().getId());
            int newRating = rateComboBox.getValue();
            if (newRating == music.getRating()) {
                return;
            }
            Music updated = music.withRating(newRating);

            databaseManager.updateMusic(updated);
            playlist.updateRating(updated);
        }
        catch (DatabaseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(FXMLLoaderFactory.class.getResource("/application.css").toExternalForm());
            alert.setTitle("Operation error.");
            alert.setHeaderText(null);
            alert.setContentText("Impossible to add rating.");

            alert.showAndWait();
        }

    }

    @FXML
    public void onShowRemoveMusicAction() throws IOException {
        FXMLLoader loader = FXMLLoaderFactory.getLoader();
        loader.setLocation(getClass().getResource("/org/icule/player/gui/RemovedMusicFrame.fxml"));

        BorderPane pane = loader.load();
        Stage stage = new Stage();
        stage.setScene(FXMLLoaderFactory.getScene(pane));
        stage.setTitle("Music tagged with removed.");

        RemovedMusicFrame frame = loader.getController();
        frame.setStage(stage);

        stage.show();
    }

    @FXML
    public void onCheckMusicAction() throws DatabaseException {
        directoryScanner.checkDatabaseExistence();
        playlist.initPlaylist(DatabaseUtils.getAllMusicForPlaylist(databaseManager));
    }

    private void handleVolumeChange() {
        musicPlayer.setVolume((int) volumeSlider.getValue());

        try {
            Music currentMusic = databaseManager.getMusic(musicPlayer.getCurrentMusic().getId());
            databaseManager.updateMusic(currentMusic.withVolumeOffset((int) volumeSlider.getValue()));
        }
        catch (DatabaseException e) {
            e.printStackTrace();
        }

    }
}
