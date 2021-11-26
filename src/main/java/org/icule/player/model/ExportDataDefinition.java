package org.icule.player.model;

import org.icule.player.annotation.MyStyle;
import org.immutables.value.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value.Immutable
@MyStyle
public interface ExportDataDefinition {
    Tag getTag();

    Instant getDate();

    List<UUID> getIdList();
}
