package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import phoenix.impl.FanartAPI;
import sagex.SageAPI;
import sagex.api.MediaFileAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.vfs.impl.AlbumInfo;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.stub.MediaFileAPIProxy.MediaFile;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static test.junit.lib.TestUtil.*;

public class TestFanartLocations {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testLocalFanartLocations() throws IOException {
        File f = makeDir("localfanart/video1/VIDEO_TS");
        assertTrue(FanartUtil.isDVDFolder(f));
        makeFile("localfanart/video1.jpg");
        makeFile("localfanart/video1_background.jpg");
        makeFile("localfanart/video1_banner.jpg");

        // Test DVD File Locations when VIDEO_TS is passed
        File f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BACKGROUND, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1_background.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BANNER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1_banner.jpg", f2.getName());

        f2 = FanartUtil.resolvePropertiesFile(f);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1.properties", f2.getName());

        // Test DVD File Locations when DVD folder is passed with VIDEO_TS child
        f = f.getParentFile();
        assertEquals("video1", f.getName());
        assertTrue(FanartUtil.isDVDFolder(f));

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BACKGROUND, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1_background.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BANNER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1_banner.jpg", f2.getName());

        f2 = FanartUtil.resolvePropertiesFile(f);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("video1.properties", f2.getName());

        // now test normal avi files
        f = makeFile("localfanart/movie1.avi");
        assertEquals("movie1.avi", f.getName());
        assertFalse(FanartUtil.isDVDFolder(f));
        makeFile("localfanart/movie1.jpg");
        makeFile("localfanart/movie1_background.jpg");
        makeFile("localfanart/movie1_banner.jpg");

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("movie1.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BACKGROUND, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("movie1_background.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BANNER, true);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("movie1_banner.jpg", f2.getName());

        f2 = FanartUtil.resolvePropertiesFile(f);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("movie1.avi.properties", f2.getName());
    }

    @Test
    public void testLocalFanartForFolders() throws IOException {
        File f = makeDir("localfanart/localmovie/VIDEO_TS");
        assertTrue(FanartUtil.isDVDFolder(f));
        makeFile("localfanart/localmovie/folder.jpg");
        makeFile("localfanart/localmovie/background.jpg");
        makeFile("localfanart/localmovie/banner.jpg");

        // Test DVD File Locations when VIDEO_TS is passed
        File f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, true);
        assertNotNull(f2);
        assertEquals("folder.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BACKGROUND, true);
        assertNotNull(f2);
        assertEquals("background.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BANNER, true);
        assertNotNull(f2);
        assertEquals("localmovie", f2.getParentFile().getName());
        assertEquals("banner.jpg", f2.getName());

        f2 = FanartUtil.resolvePropertiesFile(f);
        assertNotNull(f2);
        assertEquals("localfanart", f2.getParentFile().getName());
        assertEquals("localmovie.properties", f2.getName());

        // now test normal avi files
        f = makeFile("localfanart/singlemoviedir/movie1.avi");
        assertEquals("movie1.avi", f.getName());
        assertFalse(FanartUtil.isDVDFolder(f));
        makeFile("localfanart/singlemoviedir/folder.jpg");
        makeFile("localfanart/singlemoviedir/background.jpg");
        makeFile("localfanart/singlemoviedir/banner.jpg");

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, true);
        assertNotNull(f2);
        assertEquals("singlemoviedir", f2.getParentFile().getName());
        assertEquals("folder.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BACKGROUND, true);
        assertNotNull(f2);
        assertEquals("singlemoviedir", f2.getParentFile().getName());
        assertEquals("background.jpg", f2.getName());

        f2 = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.BANNER, true);
        assertNotNull(f2);
        assertEquals("singlemoviedir", f2.getParentFile().getName());
        assertEquals("banner.jpg", f2.getName());

        f2 = FanartUtil.resolvePropertiesFile(f);
        assertNotNull(f2);
        assertEquals("singlemoviedir", f2.getParentFile().getName());
        assertEquals("movie1.avi.properties", f2.getName());
    }

    @Test
    public void testCentralFanartLocations() {
        File dir = makeDir("centralfanart");

        File file = FanartUtil.getCentralFanartArtifact(MediaType.MOVIE, MediaArtifactType.POSTER, "Jackass 2.5",
                dir.getAbsolutePath(), null);
        assertFileParts(file, "Movies", "Jackass 25", "Posters", "Jackass 25.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.MOVIE, MediaArtifactType.POSTER, "Terminator?", dir.getAbsolutePath(),
                null);
        assertFileParts(file, "Movies", "Terminator", "Posters", "Terminator.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.MOVIE, MediaArtifactType.BANNER, "Terminator?", dir.getAbsolutePath(),
                null);
        assertFileParts(file, "Movies", "Terminator", "Banners", "Terminator.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.MOVIE, MediaArtifactType.BACKGROUND, "Terminator?",
                dir.getAbsolutePath(), null);
        assertFileParts(file, "Movies", "Terminator", "Backgrounds", "Terminator.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, MediaArtifactType.POSTER, "House", dir.getAbsolutePath(), null);
        assertFileParts(file, "TV", "House", "Posters", "House.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, MediaArtifactType.BANNER, "House", dir.getAbsolutePath(), null);
        assertFileParts(file, "TV", "House", "Banners", "House.jpg");

        file = FanartUtil
                .getCentralFanartArtifact(MediaType.TV, MediaArtifactType.BACKGROUND, "House", dir.getAbsolutePath(), null);
        assertFileParts(file, "TV", "House", "Backgrounds", "House.jpg");

        Map<String, String> season = new HashMap<String, String>();
        season.put(FanartUtil.SEASON_NUMBER, "2");
        season.put(FanartUtil.EPISODE_NUMBER, "3");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, MediaArtifactType.POSTER, "House", dir.getAbsolutePath(), season);
        assertFileParts(file, "TV", "House", "Season 2", "Posters", "House.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, MediaArtifactType.BANNER, "House", dir.getAbsolutePath(), season);
        assertFileParts(file, "TV", "House", "Season 2", "Banners", "House.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, MediaArtifactType.BACKGROUND, "House", dir.getAbsolutePath(),
                season);
        assertFileParts(file, "TV", "House", "Season 2", "Backgrounds", "House.jpg");

        // Actor Fanart
        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, "House?", MediaArtifactType.ACTOR, "Hugh Laurie?",
                dir.getAbsolutePath(), null);
        assertFileParts(file, "TV", "House", "Actors", "Hugh Laurie", "Hugh Laurie.jpg");

        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, "House", MediaArtifactType.ACTOR, "Hugh Laurie?",
                dir.getAbsolutePath(), season);
        assertFileParts(file, "TV", "House", "Season 2", "Actors", "Hugh Laurie", "Hugh Laurie.jpg");

        // Actor fanart at the top level
        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, null, MediaArtifactType.ACTOR, "Hugh Laurie?",
                dir.getAbsolutePath(), season);
        assertFileParts(file, "TV", "Actors", "Hugh Laurie", "Hugh Laurie.jpg");

        file = FanartUtil.getCentralFanartArtifact(null, null, MediaArtifactType.ACTOR, "Hugh Laurie?", dir.getAbsolutePath(),
                season);
        assertFileParts(file, "Actors", "Hugh Laurie", "Hugh Laurie.jpg");

        // test genre fanart
        file = FanartUtil.getCentralFanartArtifact(null, "Genres", null, "Family", dir.getAbsolutePath(), season);
        assertFileParts(file, "Genres", "Family", "Family.jpg");

        // test mediatype specific genres
        file = FanartUtil.getCentralFanartArtifact(MediaType.GENRE, "Family", MediaArtifactType.BACKGROUND, null,
                dir.getAbsolutePath(), season);
        assertFileParts(file, "Genres", "Family", "Backgrounds", "Family.jpg");

        // test episode fanart
        file = FanartUtil.getCentralFanartArtifact(MediaType.TV, "House", MediaArtifactType.EPISODE, null, dir.getAbsolutePath(),
                season);
        assertFileParts(file, "TV", "House", "Season 2", "Episodes", "0003.jpg");
    }

    private void assertFileParts(File file, String... parts) {
        LinkedList<String> l = new LinkedList<String>();
        l.addAll(Arrays.asList(parts));

        System.out.println("\n** Testing File: " + file.getAbsolutePath());
        for (String s : parts) {
            System.out.println("** Part: " + s);
        }
        for (int i = l.size() - 1; i >= 0; i--) {
            if ("*".equals(parts[i])) {
                file = file.getParentFile();
                continue;
            }
            assertEquals("Failed for file: " + file.getAbsolutePath(), l.get(i), file.getName());
            file = file.getParentFile();
        }
        System.out.println("\n");
    }

    @Test
    public void testPhoenixAPIForFanart() throws Exception {
        init();
        File fanartDir = makeDir("test/FanartFolder");
        phoenix.fanart.SetFanartCentralFolder(fanartDir.getAbsolutePath());
        String sdir = phoenix.fanart.GetFanartCentralFolder();
        System.out.println("Central Folder: " + sdir);

        // test basics for all types and artifacts
        MediaType types[] = new MediaType[]{MediaType.MOVIE, MediaType.TV};
        MediaArtifactType artifacts[] = new MediaArtifactType[]{MediaArtifactType.POSTER, MediaArtifactType.BACKGROUND,
                MediaArtifactType.BANNER};
        for (MediaType mt : types) {
            for (MediaArtifactType at : artifacts) {
                doTestArtifact(fanartDir, mt, at, "FindingNemo");
            }
        }

        // test genres at the media type level
        for (MediaType mt : types) {
            for (MediaArtifactType at : artifacts) {
                doTestGenres(fanartDir, mt, "Drama", at.name());
            }
        }

        testGenresAtTopLevel(fanartDir);

        for (MediaType mt : types) {
            doTestActors(fanartDir, mt, "OverTheTop", "BillyBang");
            doTestActorsNoMovie(fanartDir, mt, "BillyBang2");
        }

        doTestActorsTopLevel(fanartDir);

        // test tv with season info
        for (MediaArtifactType at : artifacts) {
            doTestTVArtifactWithSeason(fanartDir, MediaType.TV, at, "House");
        }

        for (MediaArtifactType at : artifacts) {
            doTestMovieCategorForTV(fanartDir, at, "Futurama");
        }

        for (MediaType mt : types) {
            for (MediaArtifactType at : artifacts) {
                doTestSetFanart(fanartDir, mt, "MyMovie", at);
            }
        }

        delete(fanartDir);

    }

    private void testGenresAtTopLevel(File fanartDir) throws IOException {
        // now test genre without a top level TV or Movies
        delete(fanartDir);
        File dir = new File(fanartDir, "Genres/Horror/Banners");
        dir.mkdirs();
        File file = new File(dir, "Horror.jpg");
        file.createNewFile();
        fillFile(file);
        file = new File(dir, "Horror2.jpg");
        file.createNewFile();
        fillFile(file);

        File media = makeFile("test/Movies/omg.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        String genre = phoenix.api.GetFanartGenre("Horror", "banner");
        assertNotNull(genre);
        assertFileParts(new File(genre), fanartDir.getName(), "Genres", "Horror", "Banners", "Horror.jpg");

        String all[] = phoenix.api.GetFanartGenres("Horror", "banner");
        assertNotNull(all);
        assertEquals(2, all.length);

        genre = phoenix.api.GetFanartGenre("Horror", "banner");
        assertNotNull(genre);
        assertFileParts(new File(genre), fanartDir.getName(), "Genres", "Horror", "Banners", "Horror.jpg");

        all = phoenix.api.GetFanartGenres("Horror", "banner");
        assertNotNull(all);
        assertEquals(2, all.length);
    }

    private void doTestArtifact(File central, MediaType mediaType, MediaArtifactType artifactType, String name) throws IOException {
        delete(central);
        File media = makeFile("test/" + mediaType.dirName() + "/" + name + ".avi", true);
        System.out.println("Created MediaFile: " + media);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        if (artifactType == MediaArtifactType.POSTER) {
            assertFalse("Should not have fanart at this point", phoenix.fanart.HasFanartPoster(mediaFile));
        }

        File f1 = new File(central, mediaType.dirName() + "/" + name + "/" + artifactType.dirName() + "/" + name + ".jpg");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        fillFile(f1);
        System.out.println("Added Fanart: " + f1);

        File f2 = new File(central, mediaType.dirName() + "/" + name + "/" + artifactType.dirName() + "/" + name + "2.jpg");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        fillFile(f2);
        System.out.println("Added Fanart: " + f2);

        if (artifactType == MediaArtifactType.POSTER) {
            assertTrue("Doesn't have fanart!!", phoenix.fanart.HasFanartPoster(mediaFile));
        }

        String file = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            file = phoenix.fanart.GetFanartBackground(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            file = phoenix.fanart.GetFanartBanner(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            file = phoenix.fanart.GetFanartPoster(mediaFile);
        }

        if (file == null) {
            System.out.println("Messed Up!!");
            System.out.println("MediaFile: " + mediaFile);
            System.out.println("FanartFile: " + f1.getAbsolutePath());
            System.out.println("MediaType: " + mediaType);
            System.out.println("ArtifactType: " + artifactType);
            fail(String.format("Failed with %s; %s; %s\n", f1.getAbsolutePath(), mediaType, artifactType));
        } else {
            System.out.println("** GetFanart" + artifactType.name() + "(" + mediaType.name() + ") PASSED **");
        }

        assertFileParts(new File(file), central.getName(), mediaType.dirName(), name, artifactType.dirName(), "*");

        // now check the list
        String all[] = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            all = phoenix.api.GetFanartBackgrounds(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            all = phoenix.api.GetFanartBanners(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            all = phoenix.api.GetFanartPosters(mediaFile);
        }

        assertNotNull("GetFanartXXXXXs() Failed for: " + name + "; MediaType: " + mediaType + "; Artifact: " + artifactType, all);
        assertEquals(2, all.length);
        System.out.println("GetFanart" + artifactType.name() + "s(" + mediaType.name() + ") PASSED");
    }

    private void doTestMovieCategorForTV(File central, MediaArtifactType artifactType, String name) throws IOException {
        delete(central);
        File media = makeFile("test/TV/" + name + ".avi", true);
        MediaFile mediaFile = (MediaFile) MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());
        mediaFile.category = "Movie";

        assertTrue(MediaFileAPI.IsTVFile(mediaFile));
        assertEquals("Movie", ShowAPI.GetShowCategory(mediaFile));

        File f1 = new File(central, "Movies/" + name + "/" + artifactType.dirName() + "/" + name + ".jpg");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        fillFile(f1);

        File f2 = new File(central, "Movies/" + name + "/" + artifactType.dirName() + "/" + name + "2.jpg");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        fillFile(f2);

        String file = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            file = phoenix.api.GetFanartBackground(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            file = phoenix.api.GetFanartBanner(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            file = phoenix.api.GetFanartPoster(mediaFile);
        }

        assertNotNull(file);

        assertFileParts(new File(file), central.getName(), "Movies", name, artifactType.dirName(), name + ".jpg");
    }

    private void doTestSetFanart(File central, MediaType mediaType, String name, MediaArtifactType artifactType) throws Exception {
        delete(central);
        File media = makeFile("test/" + mediaType.dirName() + "/" + name + ".avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());
        File f1 = new File(central, mediaType.dirName() + "/" + name + "/" + artifactType.dirName() + "/" + name + ".jpg");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        fillFile(f1);

        String marker = "Second";
        File f2 = new File(central, mediaType.dirName() + "/" + name + "/" + artifactType.dirName() + "/" + name + "2.jpg");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        writeToFile(f2, marker);

        if (artifactType == MediaArtifactType.BACKGROUND) {
            phoenix.fanart.SetFanartBackground(mediaFile, f2);
        } else if (artifactType == MediaArtifactType.BANNER) {
            phoenix.fanart.SetFanartBanner(mediaFile, f2);
        } else if (artifactType == MediaArtifactType.POSTER) {
            phoenix.fanart.SetFanartPoster(mediaFile, f2);
        }

        String file = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            file = phoenix.fanart.GetFanartBackground(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            file = phoenix.fanart.GetFanartBanner(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            file = phoenix.fanart.GetFanartPoster(mediaFile);
        }

        assertNotNull("GetFanart failed for " + media, file);
        // it should be the second file, because the file SetFannartXXX call
        assertFileParts(new File(file), name + "2.jpg");
        String markerTest = readFromFile(new File(file));
        assertEquals("MediaType: " + mediaType + "; Name: " + name + "; Artifact: " + artifactType, marker, markerTest);

        String all[] = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            all = phoenix.fanart.GetFanartBackgrounds(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            all = phoenix.fanart.GetFanartBanners(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            all = phoenix.fanart.GetFanartPosters(mediaFile);
        }
        assertEquals(2, all.length);
    }

    public void writeToFile(File f, String s) throws Exception {
        FileWriter fw = new FileWriter(f);
        fw.write(s);
        fw.flush();
        fw.close();
    }

    public String readFromFile(File f) throws Exception {
        BufferedReader r = new BufferedReader(new FileReader(f));
        String s = r.readLine();
        return s;
    }

    private void doTestTVArtifactWithSeason(File central, MediaType mediaType, MediaArtifactType artifactType, String name)
            throws IOException {
        delete(central);
        File media = makeFile("test/" + mediaType.dirName() + "/" + name + "wontworkwithoutmetadata.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());
        String season = "3";
        MediaFileAPI.SetMediaFileMetadata(mediaFile, FanartUtil.SEASON_NUMBER, season);
        MediaFileAPI.SetMediaFileMetadata(mediaFile, FanartUtil.EPISODE_NUMBER, "1");

        // need both in order work
        MediaFileAPI.SetMediaFileMetadata(mediaFile, "MediaTitle", name);
        MediaFileAPI.SetMediaFileMetadata(mediaFile, "MediaType", mediaType.sageValue());

        File f1 = new File(central, mediaType.dirName() + "/" + name + "/Season " + season + "/" + artifactType.dirName() + "/"
                + name + ".jpg");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        fillFile(f1);

        File f2 = new File(central, mediaType.dirName() + "/" + name + "/Season " + season + "/" + artifactType.dirName() + "/"
                + name + "2.jpg");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        fillFile(f2);

        String file = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            file = phoenix.fanart.GetFanartBackground(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            file = phoenix.fanart.GetFanartBanner(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            file = phoenix.fanart.GetFanartPoster(mediaFile);
        } else {
            fail("Invalid Artifact Type: " + artifactType);
        }

        if (file == null) {
            System.out.println("Messed Up!!");
            System.out.println("MediaFile: " + mediaFile);
            System.out.println("FanartFile: " + f1.getAbsolutePath());
            System.out.println("Name: " + name);
            System.out.println("MediaTitle(Metadata): " + MediaFileAPI.GetMediaFileMetadata(mediaFile, "MediaTitle"));
            System.out.println("SeasonNumber(Metadata): " + MediaFileAPI.GetMediaFileMetadata(mediaFile, "SeasonNumber"));
            System.out.println("MediaType: " + mediaType);
            System.out.println("ArtifactType: " + artifactType);
            fail(String.format("Failed with %s; %s; %s\n", f1.getAbsolutePath(), mediaType, artifactType));
        }

        System.out.println("Fanart: " + file);
        assertFileParts(new File(file), central.getName(), mediaType.dirName(), name, "Season " + season, artifactType.dirName(),
                name + ".jpg");

        // now check the list
        String all[] = null;
        if (artifactType == MediaArtifactType.BACKGROUND) {
            all = phoenix.api.GetFanartBackgrounds(mediaFile);
        } else if (artifactType == MediaArtifactType.BANNER) {
            all = phoenix.api.GetFanartBanners(mediaFile);
        } else if (artifactType == MediaArtifactType.POSTER) {
            all = phoenix.api.GetFanartPosters(mediaFile);
        }

        assertNotNull(all);
        assertEquals(2, all.length);
    }

    private void doTestGenres(File central, MediaType mediaType, String genre, String artifactType) throws IOException {
        delete(central);
        File media = makeFile("test/" + mediaType.dirName() + "/Buddy.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        // test genres
        File f1 = new File(central, "/Genres/" + genre + "/" + MediaArtifactType.toMediaArtifactType(artifactType).dirName());
        f1.mkdirs();
        File file = new File(f1, genre + ".jpg");
        file.createNewFile();
        fillFile(file);
        File f2 = new File(central, "/Genres/" + genre + "/" + MediaArtifactType.toMediaArtifactType(artifactType).dirName());
        f2.mkdirs();
        File file2 = new File(f1, genre + "2.jpg");
        file2.createNewFile();
        fillFile(file2);

        String gen = phoenix.api.GetFanartGenre(genre, artifactType);
        assertNotNull(gen);
        System.out.println("Genre: " + gen);
        assertFileParts(new File(gen), central.getName(), "Genres", genre, MediaArtifactType.toMediaArtifactType(artifactType)
                .dirName(), "*");

        String all[] = phoenix.api.GetFanartGenres(genre, artifactType);
        assertNotNull(all);
        assertEquals(2, all.length);
    }

    private void doTestActors(File central, MediaType mediaType, String title, String actor) throws IOException {
        delete(central);

        File media = makeFile("test/" + mediaType.dirName() + "/" + title + ".avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        File dir = new File(central, "/Actors/" + actor);
        dir.mkdirs();
        File file = new File(dir, actor + ".jpg");
        file.createNewFile();
        fillFile(file);
        file = new File(dir, actor + "1.jpg");
        file.createNewFile();
        fillFile(file);

        String act = phoenix.api.GetFanartActor(actor);
        assertNotNull(act);
        assertFileParts(new File(act), central.getName(), "Actors", actor, actor + ".jpg");

        String all[] = phoenix.api.GetFanartActors(actor);
        assertNotNull(all);
        assertEquals(2, all.length);
    }

    private void doTestActorsNoMovie(File central, MediaType mediaType, String actor) throws IOException {
        delete(central);

        File media = makeFile("test/" + mediaType.dirName() + "/nomovie.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        File dir = new File(central, "/Actors/" + actor + "/");
        dir.mkdirs();
        File file = new File(dir, actor + ".jpg");
        file.createNewFile();
        fillFile(file);
        file = new File(dir, actor + "1.jpg");
        file.createNewFile();
        fillFile(file);

        String act = phoenix.api.GetFanartActor(actor);
        assertNotNull(act);
        assertFileParts(new File(act), central.getName(), "Actors", actor, "*");

        String all[] = phoenix.api.GetFanartActors(actor);
        assertNotNull(all);
        assertEquals(2, all.length);
    }

    private void doTestActorsTopLevel(File fanartDir) throws IOException {
        delete(fanartDir);
        File dir = new File(fanartDir, "Actors/Hillary/");
        dir.mkdirs();
        File file = new File(dir, "Hillary.jpg");
        file.createNewFile();
        fillFile(file);
        file = new File(dir, "Hillary2.jpg");
        file.createNewFile();
        fillFile(file);

        File media = makeFile("test/Movies/n2omovie.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        String actor = phoenix.api.GetFanartActor("Hillary");
        assertNotNull(actor);
        assertFileParts(new File(actor), fanartDir.getName(), "Actors", "Hillary", "*");

        String all[] = phoenix.api.GetFanartActors("Hillary");
        assertNotNull(all);
        assertEquals(2, all.length);

        actor = phoenix.api.GetFanartActor("Hillary");
        assertNotNull(actor);
        assertFileParts(new File(actor), fanartDir.getName(), "Actors", "Hillary", "*");

        all = phoenix.api.GetFanartActors("Hillary");
        assertNotNull(all);
        assertEquals(2, all.length);

    }

    @Test
    public void testFanartPaths() {
        File fanartDir = makeDir("test/FanartFolder");
        phoenix.fanart.SetFanartCentralFolder(fanartDir.getAbsolutePath());
        String sdir = phoenix.fanart.GetFanartCentralFolder();
        System.out.println("Central Folder: " + sdir);

        File media = makeFile("test/Movies/Terminator.avi", true);
        Object mediaFile = MediaFileAPI.AddMediaFile(media, media.getAbsolutePath());

        String path = phoenix.fanart.GetFanartPosterPath(mediaFile);
        assertNotNull(path);
        assertFileParts(new File(path), "Terminator", "Posters");

        path = phoenix.fanart.GetFanartBackgroundPath(mediaFile);
        assertNotNull(path);
        assertFileParts(new File(path), "Terminator", "Backgrounds");

        path = phoenix.fanart.GetFanartBannerPath(mediaFile);
        assertNotNull(path);
        assertFileParts(new File(path), "Terminator", "Banners");

        path = phoenix.fanart.GetFanartGenrePath("Horror", "banner");
        assertNotNull(path);
        assertFileParts(new File(path), "Genres", "Horror", "Banners");

        path = phoenix.fanart.GetFanartActorPath("Tom Cruise");
        assertNotNull(path);
        assertFileParts(new File(path), "Actors", "Tom Cruise");
    }

    @Test
    public void testAiringMovieFanart() {
        SimpleStubAPI api = new SimpleStubAPI();
        SageAPI.setProvider(api);

        // suppress these
        api.overrideAPI("IsMediaFileObject", null);
        api.overrideAPI("IsAiringObject", null);
        api.overrideAPI("GetUIContextName", null);
        api.overrideAPI("GetShowExternalID", null);
        api.overrideAPI("GetAiringTitle", null);
        api.overrideAPI("GetShowEpisodeNumber", null);
        api.overrideAPI("GetMediaFileID", null);
        api.overrideAPI("GetUIContextNames", null);

        api.getProperties().put("phoenix/mediametadata/fanartCentralFolder", "/tmp/fanart");

        // build our airing
        Airing a = api.newAiring(100);
        a.put("IsDVD", false);
        a.put("IsBluRay", false);
        a.put("IsVideoFile", false);
        a.put("IsTVFile", false);
        a.put("GetMediaFileForAiring", null);
        a.put("GetShowTitle", "The Terminator");
        a.put("GetShowCategory", "Movie");

        // supress metadata warnings
        a.METADATA.put("MediaTitle", null);
        a.METADATA.put("SeasonNumber", null);
        a.METADATA.put("EpisodeNumber", null);
        a.METADATA.put("DefaultPoster", null);

        String art = phoenix.fanart.GetFanartArtifactDir(100, null, null, "poster", null, null, false);
        assertEquals("/tmp/fanart/Movies/The Terminator/Posters", art);

        // new airing with no movie category
        a = api.newAiring(101);
        a.put("IsDVD", false);
        a.put("IsBluRay", false);
        a.put("IsVideoFile", false);
        a.put("IsTVFile", false);
        a.put("GetMediaFileForAiring", null);
        a.put("GetShowTitle", "The Terminator");
        a.put("GetShowCategory", null);

        // supress metadata warnings
        a.METADATA.put("MediaTitle", null);
        a.METADATA.put("SeasonNumber", null);
        a.METADATA.put("EpisodeNumber", null);
        a.METADATA.put("DefaultPoster", null);

        // without a MV epsisode id it should assume a TV show for Airings
        art = phoenix.fanart.GetFanartArtifactDir(101, null, null, "poster", null, null, false);
        assertEquals("/tmp/fanart/TV/The Terminator/Posters", art);
    }

    @Test
    public void testAiringTVFanart() {
        SimpleStubAPI api = new SimpleStubAPI();
        SageAPI.setProvider(api);

        // suppress these
        api.overrideAPI("IsMediaFileObject", null);
        api.overrideAPI("IsAiringObject", null);
        api.getProperties().put("phoenix/mediametadata/fanartCentralFolder", "/tmp/fanart");

        // build our airing
        Airing a = api.newAiring(100);
        a.put("IsDVD", false);
        a.put("IsBluRay", false);
        a.put("IsVideoFile", false);
        a.put("IsTVFile", false);
        a.put("GetMediaFileForAiring", null);
        a.put("GetShowTitle", "House");

        // supress metadata warnings
        a.METADATA.put("MediaTitle", null);
        a.METADATA.put("SeasonNumber", null);
        a.METADATA.put("EpisodeNumber", null);
        a.METADATA.put("DefaultPoster", null);

        String art = phoenix.fanart.GetFanartArtifactDir(100, null, null, "poster", null, null, false);
        assertEquals("/tmp/fanart/TV/House/Posters", art);
    }

    @Test
    public void testTVWithSeason() throws IOException {
        File dir = makeDir("centralfanart");
        delete(dir);
        dir = makeDir("centralfanart");
        SimpleStubAPI api = new SimpleStubAPI();
        SageAPI.setProvider(api);

        // suppress these
        api.overrideAPI("IsMediaFileObject", true);
        api.overrideAPI("IsAiringObject", true);
        api.overrideAPI("GetUIContextName", null);
        api.overrideAPI("GetShowExternalID", null);
        api.overrideAPI("GetAiringTitle", null);
        api.overrideAPI("GetShowEpisodeNumber", null);
        api.overrideAPI("GetMediaFileID", null);
        api.overrideAPI("GetUIContextNames", null);
        api.overrideAPI("GetShowCategory", null);

        api.getProperties().put("phoenix/mediametadata/fanartCentralFolder", dir.getAbsolutePath());

        // build our airing

        Airing a = api.newMediaFile(100);
        a.put("IsDVD", false);
        a.put("IsBluRay", false);
        a.put("IsVideoFile", false);
        a.put("IsTVFile", false);
        a.put("GetMediaFileForAiring", a);
        a.put("GetShowTitle", "House");

        // supress metadata warnings
        a.METADATA.put("MediaTitle", "House");
        a.METADATA.put("MediaType", "TV");
        a.METADATA.put("DefaultPoster", null);

        SageMediaFile smf = new SageMediaFile(null, a);
        System.out.println("smf: " + smf.getTitle());
        System.out.println("smf: " + smf.getMetadata().getMediaTitle());

        // test just tv without season
        String art1 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "TV/House/Posters").getAbsolutePath(), art1);

        // now add in season
        a.METADATA.put("SeasonNumber", "3");
        a.METADATA.put("EpisodeNumber", "4");

        String art2 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "TV/House/Season 3/Posters").getAbsolutePath(), art2);

        // now create some files in there
        makeFanart(new File(new File(art1), "poster1.jpg"));
        makeFanart(new File(new File(art1), "poster2.jpg"));
        makeFanart(new File(new File(art2), "poster3.jpg"));
        makeFanart(new File(new File(art2), "poster4.jpg"));

        String posters[] = phoenix.fanart.GetFanartPosters(a);
        assertNotNull("Posters were null!", posters);
        assertEquals(4, posters.length);
        for (String s : posters) {
            System.out.println("FANART: " + s);
        }

        // next test if we can get just the series level fanart
        posters = phoenix.fanart.GetFanartArtifacts(a, null, null, "poster", null, new HashMap<String, String>());
        assertNotNull("Posters were null!", posters);
        assertEquals(2, posters.length);
        for (String s : posters) {
            System.out.println("FANART: " + s);
        }

        // next test some episode specific fanart
        String art3 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "episode", null, null, true);
        assertEquals(new File(dir, "TV/House/Season 3/Episodes").getAbsolutePath(), art3);
        makeFanart(new File(new File(art3), "0001.jpg"));
        makeFanart(new File(new File(art3), "0002.jpg"));
        makeFanart(new File(new File(art3), "0003.jpg"));
        makeFanart(new File(new File(art3), "0004.jpg"));

        String episode = phoenix.fanart.GetEpisode(a);
        assertEquals("0004.jpg", new File(episode).getName());

        // testing getting all episodes
        String alleps[] = phoenix.fanart.GetFanartArtifacts(a, null, null, "episode", null, null);
        assertEquals(4, alleps.length);
    }

    private File makeFanart(File f) throws IOException {
        f.getParentFile().mkdirs();
        f.createNewFile();
        fillFile(f);
        return f;
    }

    @Test
    public void testMusicFanart() throws IOException {
        File dir = makeDir("centralfanart");

        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("GetUIContextName", null); // consider overriding api;
        api.overrideAPI("GetUIContextNames", null); // consider overriding api;
        api.overrideAPI("IsTVFile", false); // consider overriding api;
        // {IsMusic=true,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // IsDVD=false, GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetAlbumForFile", new AlbumInfo());

        SageAPI.setProvider(api);
        api.getProperties().put("phoenix/mediametadata/fanartCentralFolder", dir.getAbsolutePath());
        api.overrideAPI("GetAlbumArtist", "Madonna");
        api.overrideAPI("GetAlbumName", "Album Name");

        // build our airing
        Airing a = api.newMediaFile(100);
        a.put("IsDVD", false);
        a.put("IsMusicFile", true);
        a.put("GetMediaTitle", "Like a Virgin");
        // supress metadata warnings
        // a.METADATA.put("MediaTitle", "Like a Virgin");
        // a.METADATA.put("MediaType", "Music");

        SageMediaFile smf = new SageMediaFile(null, a);
        // String art = phoenix.fanart.GetFanartPoster(smf);
        // System.out.println(art);
        String art = phoenix.fanart.GetFanartArtifactDir(smf, (String) null, null, null, null, null, false);
        assertFileParts(new File(art), "Music", "Madonna");

        art = phoenix.fanart.GetFanartArtifactDir(smf, (String) null, null, "Album", null, null, false);
        assertFileParts(new File(art), "Music", "Madonna", "Albums");

        // make a poster file and then fetch it.
        art = phoenix.fanart.GetFanartArtifactDir(smf, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "Music/Madonna/Posters").getAbsolutePath(), art);
        makeFanart(new File(new File(art), "poster.jpg"));

        art = phoenix.fanart.GetFanartPoster(smf);
        assertFileParts(new File(art), "Music", "Madonna", "Posters", "poster.jpg");

        // make an album art file and then fetch it.
        art = phoenix.fanart.GetFanartArtifactDir(smf, null, null, "album", null, null, true);
        assertEquals(new File(dir, "Music/Madonna/Albums").getAbsolutePath(), art);
        makeFanart(new File(new File(art), "Album Name.jpg"));

        art = phoenix.fanart.GetFanartAlbum(smf);
        assertFileParts(new File(art), "Music", "Madonna", "Albums", "Album Name.jpg");

        System.out.println(art);
    }

    @Test
    public void testDefaultFanartForTVSeriesWithSeasons() throws Exception {
        File dir = makeDir("centralfanart");

        SimpleStubAPI api = new SimpleStubAPI();
        api.LOG_USERRECORD = true;
        SageAPI.setProvider(api);

        // suppress these
        api.overrideAPI("IsMediaFileObject", true);
        api.overrideAPI("IsAiringObject", true);
        api.overrideAPI("GetUIContextName", null);
        api.overrideAPI("GetShowExternalID", null);
        api.overrideAPI("GetAiringTitle", null);
        api.overrideAPI("GetShowEpisodeNumber", null);
        api.overrideAPI("GetMediaFileID", null);
        api.overrideAPI("GetUIContextNames", null);
        api.overrideAPI("GetShowCategory", null);

        api.getProperties().put("phoenix/mediametadata/fanartCentralFolder", dir.getAbsolutePath());

        // build our airing

        Airing a = api.newMediaFile(100);
        a.put("IsDVD", false);
        a.put("IsBluRay", false);
        a.put("IsVideoFile", false);
        a.put("IsTVFile", false);
        a.put("GetMediaFileForAiring", a);
        a.put("GetShowTitle", "House");

        // supress metadata warnings
        a.METADATA.put("MediaTitle", "House");
        a.METADATA.put("MediaType", "TV");
        a.METADATA.put("DefaultPoster", null);

        SageMediaFile smf = new SageMediaFile(null, a);

        // test just tv without season
        String art1 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "TV/House/Posters").getAbsolutePath(), art1);

        // default level posters
        makeFanart(new File(new File(art1), "defposter1.jpg"));
        makeFanart(new File(new File(art1), "defposter2.jpg"));

        // now add in season specific images for season 3
        a.METADATA.put("SeasonNumber", "3");
        a.METADATA.put("EpisodeNumber", "4");
        String art2 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "TV/House/Season 3/Posters").getAbsolutePath(), art2);

        // now create some files in there
        makeFanart(new File(new File(art2), "s3poster1.jpg"));
        makeFanart(new File(new File(art2), "s3poster2.jpg"));
        File defaultPoster3 = makeFanart(new File(new File(art2), "s3poster3.jpg"));
        makeFanart(new File(new File(art2), "s3poster4.jpg"));

        // create a special "marked" file that we can later test
        String marker3 = "DEFAULT SEASON 3";
        writeToFile(defaultPoster3, marker3);

        // now add in season specific images for season 4
        a.METADATA.put("SeasonNumber", "4");
        art2 = phoenix.fanart.GetFanartArtifactDir(a, null, null, "poster", null, null, true);
        assertEquals(new File(dir, "TV/House/Season 4/Posters").getAbsolutePath(), art2);

        // now create some files in there
        makeFanart(new File(new File(art2), "s4poster1.jpg"));
        makeFanart(new File(new File(art2), "s4poster2.jpg"));
        makeFanart(new File(new File(art2), "s4poster3.jpg"));
        File defaultPoster4 = makeFanart(new File(new File(art2), "s4poster4.jpg"));

        // create a special "marked" file that we can later test
        String marker4 = "DEFAULT SEASON 4";
        writeToFile(defaultPoster4, marker4);

        // So now we have 2 seasons of fanart for the Airing 'HOUSE', and we
        // need set
        // the default poster for each season and then test that the defaults
        // were different
        // for each season
        a.METADATA.put("SeasonNumber", "3");
        assertEquals(3, new SageMediaFile(null, a).getMetadata().getSeasonNumber());
        phoenix.fanart.SetFanartPoster(a, defaultPoster3.getAbsolutePath());

        // verify that the getPosters return the poster we want
        String poster3 = phoenix.fanart.GetFanartPoster(a);
        assertEquals(defaultPoster3.getName(), new File(poster3).getName());
        assertEquals(marker3, readFromFile(new File(poster3)));

        // now try a different episode and see if we get same poster
        a.METADATA.put("SeasonNumber", "3");
        a.METADATA.put("EpisodeNumber", "4");
        assertEquals(3, new SageMediaFile(null, a).getMetadata().getSeasonNumber());
        assertEquals(4, new SageMediaFile(null, a).getMetadata().getEpisodeNumber());

        // verify that the getPosters return the poster we want based on the
        // setdefault from other episode
        poster3 = phoenix.fanart.GetFanartPoster(a);
        assertEquals(defaultPoster3.getName(), new File(poster3).getName());
        assertEquals(marker3, readFromFile(new File(poster3)));

        // now onto season 4 with setting it's default as well, well then verify
        // that it did not
        // affect season 3
        a.METADATA.put("SeasonNumber", "4");
        assertEquals(4, new SageMediaFile(null, a).getMetadata().getSeasonNumber());
        phoenix.fanart.SetFanartPoster(a, defaultPoster4.getAbsolutePath());

        // verify that the getPosters return the poster we want
        String poster4 = phoenix.fanart.GetFanartPoster(a);
        assertEquals(defaultPoster4.getName(), new File(poster4).getName());
        assertEquals(marker4, readFromFile(new File(poster4)));

        // now try a different episode and see if we get same poster
        a.METADATA.put("SeasonNumber", "4");
        a.METADATA.put("EpisodeNumber", "5");
        assertEquals(4, new SageMediaFile(null, a).getMetadata().getSeasonNumber());
        assertEquals(5, new SageMediaFile(null, a).getMetadata().getEpisodeNumber());

        // verify that the getPosters return the poster we want based on the
        // setdefault from other episode
        poster4 = phoenix.fanart.GetFanartPoster(a);
        assertEquals(defaultPoster4.getName(), new File(poster4).getName());
        assertEquals(marker4, readFromFile(new File(poster4)));

        // and finally, we need to verify that because set the default on season
        // 4, and it worked,
        // that it did not impact the default set for season 3
        a.METADATA.put("SeasonNumber", "3");
        a.METADATA.put("EpisodeNumber", "4");
        assertEquals(3, new SageMediaFile(null, a).getMetadata().getSeasonNumber());
        assertEquals(4, new SageMediaFile(null, a).getMetadata().getEpisodeNumber());

        // verify that the getPosters return the poster we want based on the
        // setdefault from other episode
        poster3 = phoenix.fanart.GetFanartPoster(a);
        assertEquals(defaultPoster3.getName(), new File(poster3).getName());
        assertEquals(marker3, readFromFile(new File(poster3)));
    }

    @Test
    public void testImageCacheKey() {
        FanartAPI api = new FanartAPI();
        HashMap<String, String> md = new HashMap<String, String>();
        md.put("SeasonNumber", "3");
        md.put("EpisodeNumber", "4");

        // verify there is no episode information for posters, even thouge we
        // passed an episode
        String key = api.imageKey("A", MediaType.TV, "House", MediaArtifactType.POSTER, null, md);
        assertEquals("0:TV:House:POSTER:3:", key);

        // verify that we have episode information for episode artifact types
        key = api.imageKey("A", MediaType.TV, "House", MediaArtifactType.EPISODE, null, md);
        assertEquals("0:TV:House:EPISODE:3:4", key);

        key = api.imageKey("A", MediaType.MOVIE, "In Too Deep", MediaArtifactType.BACKGROUND, null, null);
        assertEquals("0:MOVIE:In Too Deep:BACKGROUND:", key);
    }
}
