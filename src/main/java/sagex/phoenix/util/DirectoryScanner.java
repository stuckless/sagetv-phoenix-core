package sagex.phoenix.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.DirectoryWalker;

/**
 * Simple Scanner that will notify a listener for each File/Dir it finds.
 * 
 * @author seans
 * 
 */
public class DirectoryScanner extends DirectoryWalker {
    private FileFilter filter;
    public DirectoryScanner(FileFilter filter) {
        this.filter=filter;
    }
    
    public void scan(File startDir, Collection<File> collection) {
        try {
            walk(startDir, collection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection results) throws IOException {
        if (filter==null) {
            results.add(directory);
        }
        if (filter!=null && filter.accept(directory)) {
            results.add(directory);
        }
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) throws IOException {
        if (filter==null) {
            results.add(file);
        }
        if (filter!=null && filter.accept(file)) {
            results.add(file);
        }
    }
}
