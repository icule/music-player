package org.icule.player.model;

import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@MyStyle
public interface TagMusicInformationDefinition {
    UUID getMusicId();

    Tag getTag();

    long getLastModification();
}
