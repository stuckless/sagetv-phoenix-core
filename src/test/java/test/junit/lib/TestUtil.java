package test.junit.lib;

import junit.framework.TestCase;
import sagex.phoenix.image.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestUtil extends TestCase {
    // verify that we are in the right testing directory
    static {
        try {
            File f = new File(".").getCanonicalFile();
            if ("testing".equals(f.getName())) {
                throw new RuntimeException("Testing Working dir should be target/testing/ but instead is " + f.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void delete(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                delete(f);
            }
        }
        dir.delete();
        assertFalse("Did not delete: " + dir.getAbsolutePath(), dir.exists());
    }

    public static File makeDir(String dir) {
        File f = new File("../../target/junit", dir);
        f.mkdirs();
        assertTrue("Failed to create dir!", f.exists());
        return f;
    }

    public static File makeFile(String file) {
        return makeFile(file, false);
    }

    public static File makeFile(String file, boolean createDirs) {
        File f = new File("../../target/junit", file);
        try {
            if (f.exists())
                f.delete();
            if (!f.getParentFile().exists())
                f.getParentFile().mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Can't create file: " + file);
        }
        assertTrue("Failed to create file!", f.exists());
        return f;
    }

    public static void fillFile(File f) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            fos.write("test".getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to write to file: " + f.getAbsolutePath());
        }
    }

    public static void verifyImageSize(BufferedImage bi, int width, int height) {
        assertEquals("incorrect width", width, bi.getWidth());
        assertEquals("incorrect height", height, bi.getHeight());
    }

    public static void verifyImageSize(File bi, int width, int height) throws IOException {
        verifyImageSize(ImageUtil.readImage(bi), width, height);
    }

    public static void verifyImageSize(Object bi, int width, int height) throws IOException {
        if (bi instanceof File) {
            verifyImageSize((File) bi, width, height);
        } else {
            verifyImageSize((BufferedImage) bi, width, height);
        }
    }

    public static BufferedImage createBufferedImage(int width, int height) {
        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return buffer;
    }

    public static File createImageFile(String file, int width, int height) {
        File f = makeFile(file);
        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        try {
            ImageUtil.writeImage(buffer, f);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not create image: " + file);
        }
        return f;
    }
}
