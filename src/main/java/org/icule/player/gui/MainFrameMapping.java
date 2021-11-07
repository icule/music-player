package org.icule.player.gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.icule.player.DirectoryScanner;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Playlist;
import org.icule.player.model.Tag;
import org.icule.player.music.MusicPlayer;

import javax.inject.Inject;
import java.io.File;

public class MainFrameMapping {
    @FXML
    private ComboBox<Tag> tagCombo;

    @FXML
    private TextArea musicInformationArea;

    private final MusicPlayer musicPlayer;
    private final DatabaseManager databaseManager;
    private final DirectoryScanner directoryScanner;

    @Inject
    public MainFrameMapping(final MusicPlayer musicPlayer,
                            final DatabaseManager databaseManager,
                            final DirectoryScanner directoryScanner) {
        this.musicPlayer = musicPlayer;
        this.databaseManager = databaseManager;
        this.directoryScanner = directoryScanner;
    }


    @FXML
    private void initialize() {
        tagCombo.setItems(FXCollections.observableArrayList(Tag.values()));
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
}
