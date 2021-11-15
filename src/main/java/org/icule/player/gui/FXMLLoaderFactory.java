package org.icule.player.gui;

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class FXMLLoaderFactory {
    private static Injector injector;

    public static void parametrize(final Injector injector) {
        FXMLLoaderFactory.injector = injector;
    }

    public static FXMLLoader getLoader() {
        FXMLLoader loader = new FXMLLoader();

        loader.setControllerFactory(aClass -> injector.getInstance(aClass));
        return loader;
    }

    public static Scene getScene(final Parent parent) {
        Scene res = new Scene(parent);
        res.getStylesheets().add(FXMLLoaderFactory.class.getResource("/application.css").toExternalForm());
        return res;
    }
}
