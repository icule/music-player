package org.icule.player;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.icule.player.configuration.ConfigurationManager;
import org.icule.player.database.DatabaseManager;
import org.icule.player.model.Playlist;
import org.icule.player.music.MusicPlayer;

public class MainModule extends AbstractModule {
    private final ConfigurationManager configurationManager;

    public MainModule(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ConfigurationManager.class).toInstance(configurationManager);
        bind(Playlist.class).in(Singleton.class);
        bind(MusicPlayer.class).in(Singleton.class);
        bind(DatabaseManager.class).in(Singleton.class);
    }
}
