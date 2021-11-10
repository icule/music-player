package org.icule.player.model;

import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

@Value.Immutable
@MyStyle
public interface MusicInformationDefinition {
    String getArtist();

    String getTitle();

    String getAlbum();

    long getDuration();
}
