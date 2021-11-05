package org.icule.player.model;

import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@MyStyle
public abstract class MusicDefinition {
    public abstract UUID getId();

    public abstract String getPath();

    public abstract int getRating();

    public abstract long getLastModification();
}
