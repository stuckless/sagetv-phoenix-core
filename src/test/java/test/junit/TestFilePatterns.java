package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static test.junit.lib.TestUtil.makeDir;
import static test.junit.lib.TestUtil.makeFile;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import test.InitPhoenix;

public class TestFilePatterns {
    @BeforeClass
    public static void setup() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testDVD() {
        isDVDFile("C:\\vidoes\\Video_TS", true);
        isDVDFile("/vidoes/Video_TS/", true);
        isDVDFile("/vidoes/Movie/MyMovie.vob", true);
        isDVDFile("/vidoes/Movie/MyMovie.vob/movie", false);

        File f = makeFile("vidoes/Terminator/VIDEO_TS/video.vob", true);
        assertEquals(true, FileResourceFactory.isDVD(f));
        File f2 = FileResourceFactory.resolveDVD(f);
        assertEquals("VIDEO_TS", f2.getName());

        f = f.getParentFile();
        assertEquals(true, FileResourceFactory.isDVD(f));
        f2 = FileResourceFactory.resolveDVD(f);
        assertEquals("VIDEO_TS", f2.getName());

        f = f.getParentFile();
        assertEquals(true, FileResourceFactory.isDVD(f));
        f2 = FileResourceFactory.resolveDVD(f);
        assertEquals("VIDEO_TS", f2.getName());
    }

    @Test
    public void testBluRay() {
        isBluRay("C:\\vidoes\\bdmv", true);
        isBluRay("/vidoes/bdmv/", true);
        isBluRay("/vidoes/Movie/MyMovie.m2ts", true);
        isBluRay("/vidoes/Movie/MyMovie.m2ts/movie", false);
    }

    @Test
    public void testImages() {
        isImage("/images/a.png", true);
        isImage("/images/a.gif", true);
        isImage("/images/a.bmp", true);
    }

    @Test
    public void testMusic() {
        isMusic("/images/a.mp3", true);
        isMusic("/images/a.ogg", true);
        isMusic("/images/a.wma", true);
    }

    @Test
    public void testVideo() {
        isVideo("/images/a.mpg", true);
        isVideo("/images/a.ts", true);
        isVideo("/images/a.m2ts", true);
        isVideo("/images/a.avi", true);
    }

    @Test
    public void testTV() {
        isTV("/images/House S01E1 SomeShow.mpg", true);
        isTV("/images/House S01 E1 SomeShow.mpg", true);
        isTV("/images/House S01x01 SomeShow.mpg", true);
        isTV("/images/Season1/House.avi", true);
        isTV("/images/Season 1/House.avi", true);
    }

    @Test
    public void testRecordings() {
        isRecording("/images/House-FirstEpisode-00000000-00.mpg", true);
        isRecording("/images/SomeRecording-00000000-1.mpg", true);
    }

    @Test
    public void testFileMediaFolder() {
        File f = makeDir("myvideos");
        makeFile("myvideos/Terminator/VIDEO_TS/a.vob", true);
        makeFile("myvideos/Terminator 2/a.vob", true);
        makeFile("myvideos/Terminator 3.avi", true);
        makeFile("myvideos/Terminator Salvation.avi", true);

        IMediaFolder folder = FileResourceFactory.createFolder(null, f);
        assertEquals(4, folder.getChildren().size());
        for (IMediaResource r : folder.getChildren()) {
            System.out.println("Resource: " + r.getTitle() + "; DVD: " + r.isType(MediaResourceType.DVD.value()));
            assertTrue(!"VIDEO_TS".equals(r.getTitle()));
            assertTrue(!r.getTitle().contains(".vob"));
            if (r.getTitle().contains(".avi")) {
                assertTrue(r.isType(MediaResourceType.VIDEO.value()));
            } else {
                assertTrue(r.isType(MediaResourceType.DVD.value()));
            }
            assertTrue(r.isType(MediaResourceType.ANY_VIDEO.value()));
        }
    }

    private void isRecording(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isRecordingFile(new File(path)));
    }

    private void isTV(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isTvFile(new File(path)));
    }

    private void isVideo(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isVideoFile(new File(path)));
    }

    private void isMusic(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isMusicFile(new File(path)));
    }

    private void isImage(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isImageFile(new File(path)));
    }

    private void isBluRay(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isBluRay(new File(path)));
    }

    private void isDVDFile(String path, boolean isMatched) {
        assertEquals("match failed for: " + path, isMatched, FileResourceFactory.isDVD(new File(path)));
    }
}
