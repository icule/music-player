package org.icule.player.database;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.icule.player.TestUtils;
import org.icule.player.configuration.ConfigurationManager;
import org.icule.player.model.Music;
import org.icule.player.model.Tag;
import org.icule.player.model.TagMusicInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

public class DatabaseManagerTest {
    private DatabaseManager databaseManager;
    private Music music1;
    private Music music2;

    private TagMusicInformation info1;
    private TagMusicInformation info2;
    private TagMusicInformation info3;

    @Before
    public void setUp() throws FileNotFoundException, SQLException, ClassNotFoundException, DatabaseException {
        ConfigurationManager configurationManager = ConfigurationManager.loadConfiguration("configuration.json");
        deleteDatabase();

        databaseManager = new DatabaseManager(configurationManager);
        databaseManager.init();

        music1 = new Music(UUID.randomUUID(), "test", 3, Instant.now().minusSeconds(10).toEpochMilli());
        music2 = new Music(UUID.randomUUID(), "test2", 1, Instant.now().toEpochMilli());

        info1 = new TagMusicInformation(music1.getId(), Tag.GOOD, music1.getLastModification());
        info2 = new TagMusicInformation(music1.getId(), Tag.TO_REMOVE, music1.getLastModification());
        info3 = new TagMusicInformation(music2.getId(), Tag.GOOD, music2.getLastModification());
    }

    @After
    public void tearDown() throws DatabaseException {
        databaseManager.close();
        deleteDatabase();
    }

    private void deleteDatabase() {
        File f = new File("./data.db.mv.db");
        f.delete();
        f = new File("./data.db.trace.db");
        f.delete();
    }

    @Test
    public void testAddMusic() throws DatabaseException {
        assertNull(databaseManager.getMusic(music1.getId()));
        databaseManager.addMusic(music1);

        assertEquals(music1, databaseManager.getMusic(music1.getId()));
        assertNull(databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), CoreMatchers.is(Collections.singletonList(music1)));

        databaseManager.addMusic(music2);
        assertEquals(music1, databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(music1, music2));

        TestUtils.assertException(() -> databaseManager.addMusic(music1), DatabaseException.class);
    }

    @Test
    public void testUpdateMusic() throws DatabaseException {
        databaseManager.updateMusic(music1);

        databaseManager.addMusic(music1);
        databaseManager.addMusic(music2);

        assertEquals(music1, databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(music1, music2));

        Music updated = music1.withRating(2).withLastModification(Instant.now().toEpochMilli()).withPath("new");
        databaseManager.updateMusic(updated);

        assertEquals(updated, databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(updated, music2));
    }

    @Test
    public void testDeleteMusic() throws DatabaseException {
        databaseManager.deleteMusic(music2.getId());

        databaseManager.addMusic(music1);
        databaseManager.addMusic(music2);

        assertEquals(music1, databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(music1, music2));

        databaseManager.deleteMusic(music1.getId());
        assertNull(databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(music2));

        databaseManager.addMusic(music1);
        assertEquals(music1, databaseManager.getMusic(music1.getId()));
        assertEquals(music2, databaseManager.getMusic(music2.getId()));
        assertThat(databaseManager.getAllMusic(), Matchers.containsInAnyOrder(music1, music2));
    }

    @Test
    public void testAddTag() throws DatabaseException {


        TestUtils.assertException(() -> databaseManager.addTagMusicInformation(info1), DatabaseException.class);
        databaseManager.addMusic(music1);
        databaseManager.addMusic(music2);
        databaseManager.addTagMusicInformation(info1);
        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.is(Collections.singletonList(info1)));
        assertThat(databaseManager.getAllMusicIdForTag(info1.getTag()), Matchers.is(Collections.singletonList(music1.getId())));

        TestUtils.assertException(() -> databaseManager.addTagMusicInformation(new TagMusicInformation(music1.getId(), Tag.GOOD, Instant.now().toEpochMilli())),
                                  DatabaseException.class);
        databaseManager.addTagMusicInformation(info2);

        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.containsInAnyOrder(info1, info2));
        databaseManager.addTagMusicInformation(info3);

        assertThat(databaseManager.getAllMusicIdForTag(Tag.GOOD), Matchers.containsInAnyOrder(music1.getId(), music2.getId()));
    }

    @Test
    public void testModifyTag() throws DatabaseException {
        databaseManager.addMusic(music1);
        databaseManager.addTagMusicInformation(info1);
        databaseManager.addTagMusicInformation(info2);

        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.containsInAnyOrder(info1, info2));

        TagMusicInformation updated = info1.withLastModification(Instant.now().plusSeconds(45).toEpochMilli());
        databaseManager.updateLastModificationTime(updated);
        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.containsInAnyOrder(updated, info2));
    }

    @Test
    public void testDeleteTag() throws DatabaseException {
        databaseManager.addMusic(music1);
        databaseManager.addTagMusicInformation(info1);
        databaseManager.addTagMusicInformation(info2);

        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.containsInAnyOrder(info1, info2));
        databaseManager.deleteTagMusicInformation(info1);
        assertThat(databaseManager.getAllTagForMusic(music1.getId()), Matchers.is(Collections.singletonList(info2)));
    }

    @Test
    public void testDeleteMusicDeleteTag() throws DatabaseException {
        databaseManager.addMusic(music1);
        databaseManager.addTagMusicInformation(info1);
        databaseManager.addTagMusicInformation(info2);

        assertThat(databaseManager.getAllMusicIdForTag(Tag.GOOD), Matchers.containsInAnyOrder(music1.getId()));
        databaseManager.deleteMusic(music1.getId());

        assertTrue(databaseManager.getAllMusicIdForTag(Tag.GOOD).isEmpty());
    }
}