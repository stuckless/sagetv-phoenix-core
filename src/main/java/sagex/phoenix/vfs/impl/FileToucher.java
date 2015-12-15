package sagex.phoenix.vfs.impl;

import org.apache.commons.io.DirectoryWalker;
import sagex.phoenix.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class FileToucher extends DirectoryWalker {
    private long time = 0;

    public FileToucher() {
        super();
    }

    public static void touch(File startDirectory, long time) {
        new FileToucher().touchFiles(startDirectory, time);
    }

    public void touchFiles(File startDirectory, long time) {
        try {
            this.time = time;
            walk(startDirectory, Collections.EMPTY_LIST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection results) throws IOException {
        FileUtils.setLastModifiedQuietly(directory, time);
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        FileUtils.setLastModifiedQuietly(file, time);
    }
}
