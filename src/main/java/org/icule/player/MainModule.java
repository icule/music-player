package org.icule.player;

import com.google.inject.AbstractModule;
import org.icule.player.configuration.ConfigurationManager;

public class MainModule extends AbstractModule {
    private final ConfigurationManager configurationManager;

    public MainModule(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ConfigurationManager.class).toInstance(configurationManager);
    }
}
