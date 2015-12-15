package sagex.phoenix.common;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Simple structure that manages a directory and it's filter
 *
 * @author sean
 */
public class ManagedDirectory {
    private Comparator<File> nameComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    };

    private File dir = null;
    private FileFilter filter = null;

    public ManagedDirectory(File directory, FileFilter filter) {
        this.dir = directory;
        this.filter = filter;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public FileFilter getFilter() {
        return filter;
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public File[] getFiles() {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles(filter);
            Arrays.sort(files, getNameComparator());
            return files;
        } else {
            return new File[]{};
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dir == null) ? 0 : dir.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManagedDirectory other = (ManagedDirectory) obj;
        if (dir == null) {
            if (other.dir != null)
                return false;
        } else if (!dir.equals(other.dir))
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        return true;
    }

    public Comparator<File> getNameComparator() {
        return nameComparator;
    }

    public void setNameComparator(Comparator<File> nameComparator) {
        this.nameComparator = nameComparator;
    }
}
