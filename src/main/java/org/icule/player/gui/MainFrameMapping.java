package org.icule.player.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import org.icule.player.DirectoryScanner;
import org.icule.player.database.DatabaseException;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.*;
import org.icule.player.music.MusicListener;
import org.icule.player.music.MusicPlayer;
import org.icule.player.music.MusicUtils;

import javax.inject.Inject;
import java.io.File;
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
    }

    public void onScanDirectoryAction() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Select a directory.");
        File selected = fileChooser.showDialog(null);
        if (selected != null) {
            directoryScanner.scanDirectory(selected);
        }
    }

    @Override
    public void musicStarted(final UUID musicId) {
        Platform.runLater((() -> displayMusicInfo(musicId)));
    }

    private void displayMusicInfo(final UUID musicId) {
        try {
            Music music = databaseManager.getMusic(musicId);
            MusicInformation musicInformation = MusicUtils.getCurrentMusicInformation(music.getPath());
            List<TagMusicInformation> tagList = databaseManager.getAllTagForMusic(musicId);

            titleLabel.setText(musicInformation.getTitle());
            artistLabel.setText(musicInformation.getArtist());
            albumLabel.setText(musicInformation.getArtist());
            durationLabel.setText("" + musicInformation.getDuration() / 1000); //duration is in ms
            pathLabel.setText(music.getPath());
            idLabel.setText(music.getId().toString());

            System.out.println(music.getRating());
            rateComboBox.getSelectionModel().select(music.getRating() - 1);

            StringBuilder tagListBuilder = new StringBuilder();
            for (TagMusicInformation tagMusicInformation : tagList) {
                tagListBuilder.append("[").append(tagMusicInformation.getTag()).append("] ");
            }
            tagLabel.setText(tagListBuilder.toString());
        }
        catch (Exception e) {
            titleLabel.setText("Impossible to get the information from music");
        }
    }

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
            alert.setTitle("Operation error.");
            alert.setHeaderText(null);
            alert.setContentText("Impossible to add rating.");

            alert.showAndWait();
        }

    }
}
