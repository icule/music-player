package org.icule.player.configuration;

import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

@Value.Immutable
@MyStyle
public interface ConfigurationManagerDefinition {
    String getDatabasePath();
}
