package sagex.phoenix.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Set that can persist itself to a file as a simple set of strings.
 *
 * @author seans
 */
public class StoredStringSet implements Set<String> {
    private Set<String> set;

    public StoredStringSet() {
        this(new TreeSet<String>());
    }

    public StoredStringSet(Set<String> set) {
        this.set = set;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public void load(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#") || line.trim().length() == 0)
                continue;
            set.add(line);
        }
    }

    public void store(Writer writer, String comments) throws IOException {
        BufferedWriter bw = new BufferedWriter(writer);
        bw.write("# " + comments + "\n");
        for (String s : set) {
            bw.write(s);
            bw.write("\n");
        }
        bw.flush();
    }

    public boolean add(String e) {
        return set.add(e);
    }

    public boolean addAll(Collection<? extends String> c) {
        return set.addAll(c);
    }

    public void clear() {
        set.clear();
    }

    public boolean contains(Object o) {
        return set.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return set.equals(o);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public Iterator<String> iterator() {
        return set.iterator();
    }

    public boolean remove(Object o) {
        return set.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    public int size() {
        return set.size();
    }

    public Object[] toArray() {
        return set.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    public static void load(StoredStringSet set, File f) throws IOException {
        Reader r = null;
        try {
            r = new FileReader(f);
            set.load(r);
        } finally {
            if (r != null)
                r.close();
        }
    }

    public static void save(StoredStringSet set, File f, String msg) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            set.store(fw, msg);
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                } catch (IOException e) {
                }
                fw.close();
            }
        }
    }
}
