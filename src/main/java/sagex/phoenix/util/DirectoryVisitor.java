package sagex.phoenix.util;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by seans on 24/12/16.
 */
public class DirectoryVisitor extends DirectoryWalker {
    private final FileVisitor visitor;

    public interface FileVisitor {
        void visit(File file);
    }

    public DirectoryVisitor(FileFilter filter, FileVisitor vis) {
        super(filter, -1);
        this.visitor=vis;
    }

    protected boolean handleDirectory(File directory, int depth, Collection results) {
       return true;
    }

    protected void handleFile(File file, int depth, Collection results) {
        visitor.visit(file);
    }

    /**
     * Will apply the FileVisitor to each file that matches the FileFilter, recursively.
     *
     * @param filter
     * @param visitor
     */
    public static void visitDirectory(File startDir, final FileFilter filter, FileVisitor visitor) throws IOException {
        DirectoryVisitor dirVis = new DirectoryVisitor(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (filter==null || file.isDirectory()) return true;
                System.out.println("File: " + file + "; " + filter.accept(file));
                return filter.accept(file);
            }
        }, visitor);
        dirVis.walk(startDir, new LinkedList());
    }
}
