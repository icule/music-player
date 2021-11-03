package org.icule.player.configuration;

import com.google.gson.Gson;
import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

import java.io.FileNotFoundException;
import java.io.FileReader;

@Value.Immutable
@MyStyle
public abstract class ConfigurationManagerDefinition {
    public abstract String getDatabasePath();

    public static ConfigurationManager loadConfiguration(final String path) throws FileNotFoundException {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(path), ConfigurationManager.class);
    }
}
