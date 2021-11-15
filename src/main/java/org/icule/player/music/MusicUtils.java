package org.icule.player.music;

import org.icule.player.model.Music;
import org.icule.player.model.MusicInformation;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MusicUtils {
    private static final MediaPlayerFactory factory = new MediaPlayerFactory();

    private static Media getParsedMedia(final MediaPlayerFactory factory, final String mrl) {
        final Media media = factory.media().newMedia(mrl);

        final CountDownLatch latch = new CountDownLatch(1);

        MediaEventListener listener = new MediaEventAdapter() {
            @Override
            public void mediaParsedChanged(Media media, MediaParsedStatus newStatus) {
                latch.countDown();
            }
        };

        try {
            media.events().addMediaEventListener(listener);
            if (media.parsing().parse()) {
                latch.await();
                return media;
            } else {
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
        finally {
            media.events().removeMediaEventListener(listener);
        }
    }

    private static UUID getId(final String path) {
        Media media = getParsedMedia(factory, path);
        try {
            return UUID.fromString(media.meta().get(Meta.ENCODED_BY));
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Music getMusicFromFile(final File file) throws IOException {
        UUID id = getId(file.getAbsolutePath());
        if (id == null) {
            throw new IOException("The audio file is not correctly tagged.");
        }
        return new Music(id, file.getAbsolutePath(), 3, file.lastModified(), 0);
    }

    public static MusicInformation getMusicInformation(final String path) {
        MusicInformation.Builder builder = new MusicInformation.Builder();
        Media media = getParsedMedia(factory, path);
        MetaApi meta = media.meta();
        if (meta.get(Meta.ARTIST) != null) {
            builder.setArtist(meta.get(Meta.ARTIST));
        }
        else {
            builder.setArtist("Unknown artist");
        }
        if (meta.get(Meta.TITLE) != null) {
            builder.setTitle(meta.get(Meta.TITLE));
        }
        else {
            builder.setTitle("Unknown Title");
        }
        if (meta.get(Meta.ALBUM) != null) {
            builder.setAlbum(meta.get(Meta.ALBUM));
        }
        else {
            builder.setAlbum("Unknown album");
        }
        builder.setDuration(media.info().duration());
        return builder.create();
    }
}
