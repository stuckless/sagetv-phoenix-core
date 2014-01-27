package test.junit.lib;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import sagex.phoenix.vfs.impl.FileCleaner;

public class FilesTestCase extends TestCase {

    public FilesTestCase() {
        super();
    }

    public FilesTestCase(String name) {
        super(name);
    }
    
    public static File makeFile(String path) {
        File f = new File("target/junit/");
        f = new File(f, path);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals("can't create file: " + f.getAbsolutePath(), true, f.exists());
        return f;
    }
    
    public static File makeDir(String path) {
        File f = new File("target/junit/");
        f = new File(f, path);
        f.mkdirs();
        assertEquals("can't create dir: " + f.getAbsolutePath(), true, f.exists() && f.isDirectory());
        return f;
    }
    
    public static File getFile(String path) {
        File f = new File("target/junit/");
        f = new File(f, path);
        assertEquals("Missing File: " + f.getAbsolutePath(), true, f.exists());
        return f;
    }
    
    public void testFileDelete() {
        makeFile("test1/test/1.avi");
        makeFile("test1/test/2.avi");
        makeFile("test1/test2/3.avi");
        makeFile("test1/4.avi");
        File dir = makeDir("test1");
        assertEquals("exists", true, dir.exists());
        FileCleaner.clean(dir);
        assertEquals("exists after clean", false, dir.exists());
    }
}
