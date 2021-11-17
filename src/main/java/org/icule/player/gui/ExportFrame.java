package org.icule.player.gui;

import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ExportFrame {
    public TextArea exportTextArea;
    private String content;

    public void setContent(final String content) {
        this.content = content;
        exportTextArea.setText(content);
    }

    public void onCopyToClipboard() {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }
}
