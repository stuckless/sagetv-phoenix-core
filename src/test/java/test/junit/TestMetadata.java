package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.api.MediaFileAPI;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.COWMetadataProxy;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.proxy.SageMediaFileMetadataProxy;
import test.InitPhoenix;
import test.junit.lib.FilesTestCase;

public class TestMetadata {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testSageMetadata() {
        IMetadata all = MetadataUtil.createMetadata();
        assertNull(all.getAiringTime());

        all.setPartNumber(3);
        assertEquals(3, all.getPartNumber());

        assertEquals(false, all.isHDTV());
        all.setHDTV(true);
        assertEquals(true, all.isHDTV());

        List<ICastMember> list = all.getActors();
        assertNotNull(list);
        assertEquals(0, list.size());
        assertNotNull(all);
        assertNotNull(all.getActors());

        all.getActors().add(new CastMember("Sean", "Programmer"));
        all.getActors().add(new CastMember("Ethan", "QA"));
        list = all.getActors();
        assertEquals(2, list.size());

        all.setOriginalAirDate(Calendar.getInstance().getTime());
        assertNotNull(all.getOriginalAirDate());
    }

    @Test
    public void testSageMediaFileMetadata() {
        Object o = MediaFileAPI.AddMediaFile(new File("../../target/test/test.avi"), "test");
        IMetadata md = MetadataUtil.createMetadata(o);
        assertNotNull("Metadata was null", md);
        md.setEpisodeName("Hello");
        assertEquals("Hello", md.getEpisodeName());
        md.setPartNumber(3);
        assertEquals(3, md.getPartNumber());
    }

    @Test
    public void testMetadata() {
        File f = FilesTestCase.makeFile("test/TestMovie1.avi");
        Object file = MediaFileAPI.AddMediaFile(f, "test");

        IMetadata md = SageMediaFileMetadataProxy.newInstance(file);
        Assert.assertNotNull("Create Metadata Failed", md);
        md.setEpisodeName("Test Movie");
        assertEquals("Test Movie", md.getEpisodeName());

        System.out.println("1UserRating: " + md.getUserRating());
        md.setUserRating(87);
        System.out.println("2UserRating: " + md.getUserRating());

        System.out.println("1SeasonFinal: " + md.isSeasonFinal());
        md.setSeasonFinal(true);
        System.out.println("2SeasonFinal: " + md.isSeasonFinal());

        System.out.println("1OrigDate: " + md.getOriginalAirDate());
        md.setOriginalAirDate(Calendar.getInstance().getTime());
        System.out.println("2OrigDate: " + md.getOriginalAirDate());
    }

    @Test
    public void testLists() {
        Map<String, String> map = new HashMap<String, String>();
        String genre = "Genre";
        map.put(genre, "Family;Fun");
        IMetadata md = MetadataProxy.newInstance(map);
        List<String> genres = md.getGenres();

        assertNotNull(genres);
        assertEquals("List does not have correct # of items", 2, genres.size());

        genres.add("Action");
        genres.add("Adventure");
        genres.remove("Fun");
        assertEquals("List does not have correct # of items", 3, genres.size());

        assertEquals("Family", genres.get(0));
        assertEquals("Action", genres.get(1));
        assertEquals("Adventure", genres.get(2));

        assertEquals("Family/Action/Adventure", map.get(genre));

        genres.clear();
        assertEquals("", map.get(genre));

        genres.add("Fun");
        assertEquals("Fun", map.get(genre));
    }

    @Test
    public void testCastList() {
        Map<String, String> map = new HashMap<String, String>();
        String actor = "Actor";
        map.put(actor, "Sean -- Programmer;Xian -- QA");
        IMetadata md = MetadataProxy.newInstance(map);
        List<ICastMember> cast = md.getActors();
        assertEquals("List Size is not correct", 2, cast.size());

        // test a second call md.getActors return the same list
        List<ICastMember> cast2 = md.getActors();
        assertEquals(cast, cast2);

        cast.add(new CastMember("Ethan", "Dancer"));
        assertEquals("List Size is not correct", 3, cast.size());

        cast.remove(cast.get(1));
        assertEquals("List Size is not correct", 2, cast.size());

        assertEquals("Sean", cast.get(0).getName());
        assertEquals("Ethan", cast.get(1).getName());
        assertEquals("Programmer", cast.get(0).getRole());
        assertEquals("Dancer", cast.get(1).getRole());

        assertEquals("Sean -- Programmer;Ethan -- Dancer", map.get("Actor"));

        cast.clear();
        assertEquals("", map.get(actor));
        cast.add(new CastMember("Sean", null));
        assertEquals("Sean", map.get(actor));
    }

    @Test
    public void testFanartList() {
        Map<String, String> map = new HashMap<String, String>();
        String fanart = "Fanart";

        map.put(fanart, "3|POSTER|http://download/fanart/house.jpg");
        IMetadata md = MetadataProxy.newInstance(map);
        List<IMediaArt> art = md.getFanart();
        assertEquals("List Size is not correct", 1, art.size());

        assertEquals(3, art.get(0).getSeason());
        assertEquals(MediaArtifactType.POSTER, art.get(0).getType());
        assertEquals("http://download/fanart/house.jpg", art.get(0).getDownloadUrl());

        // test a second call md.getActors return the same list
        List<IMediaArt> art2 = md.getFanart();
        assertEquals(art, art2);

        art.add(new MediaArt(MediaArtifactType.POSTER, "http://download/fanart/glee.jpg"));
        assertEquals("List Size is not correct", 2, art.size());

        art.remove(art.get(0));
        assertEquals("List Size is not correct", 1, art.size());

        assertEquals(0, art.get(0).getSeason());
        assertEquals(MediaArtifactType.POSTER, art.get(0).getType());
        assertEquals("http://download/fanart/glee.jpg", art.get(0).getDownloadUrl());

        art.clear();
        assertEquals("", map.get(fanart));
        art.add(new MediaArt(MediaArtifactType.BACKGROUND, "http://dl/background/house.jpg", 2));
        assertEquals("2|BACKGROUND|http://dl/background/house.jpg", map.get(fanart));
    }

    @Test
    public void testCOWMetadata() {
        IMetadata parent = MetadataProxy.newInstance();
        IMetadata cow = COWMetadataProxy.newInstance(parent);

        assertNull(cow.getRelativePathWithTitle());

        cow.setRelativePathWithTitle("House");
        assertEquals("House", cow.getRelativePathWithTitle());

        assertNull(parent.getRelativePathWithTitle());

        ((COWMetadataProxy) Proxy.getInvocationHandler(cow)).commit();
        assertEquals("House", parent.getRelativePathWithTitle());
        assertEquals("House", cow.getRelativePathWithTitle());
    }

    @Test
    public void testCopyFrom() throws Exception {
        IMetadata m1 = MetadataProxy.newInstance();
        m1.setRelativePathWithTitle("Test1");
        m1.getFanart().add(new MediaArt(MediaArtifactType.POSTER, "http://dl.com"));
        m1.setEpisodeNumber(77);

        IMetadata m2 = MetadataProxy.newInstance();
        m2.setRelativePathWithTitle("Test2");
        m2.getFanart().add(new MediaArt(MediaArtifactType.BANNER, "http://dl2.com"));
        m2.setEpisodeNumber(66);

        MetadataUtil.copyMetadata(m2, m1);

        assertEquals("Test2", m1.getRelativePathWithTitle());
        assertEquals(MediaArtifactType.BANNER, m1.getFanart().get(0).getType());
        assertEquals("http://dl2.com", m1.getFanart().get(0).getDownloadUrl());
        assertEquals(66, m1.getEpisodeNumber());
    }
}
