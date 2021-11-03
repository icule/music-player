package org.icule.player.gui;

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;

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
}
