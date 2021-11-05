package org.icule.player.model;

import java.util.UUID;

public interface TagMusicInformationDefinition {
    UUID getMusicId();

    Tag getTag();

    long getLastModification();
}
