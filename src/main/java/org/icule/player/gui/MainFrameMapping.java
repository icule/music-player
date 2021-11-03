package org.icule.player.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.icule.player.model.Tag;

import javax.inject.Inject;

public class MainFrameMapping {
    @FXML
    private ComboBox<Tag> tagCombo;

    @FXML
    private TextArea musicInformationArea;

    @Inject
    public MainFrameMapping() {
    }

    @FXML
    private void initialize() {
        tagCombo.setItems(FXCollections.observableArrayList(Tag.values()));
    }

    @FXML
    public void nextButtonAction() {
    }

    @FXML
    public void playButtonAction() {
    }

    @FXML
    public void pauseButtonAction() {
    }

    @FXML
    public void stopActionButton() {
    }

    @FXML
    public void tagButtonAction() {
    }

}
