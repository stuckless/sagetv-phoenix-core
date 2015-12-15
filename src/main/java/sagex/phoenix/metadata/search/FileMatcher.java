package sagex.phoenix.metadata.search;

import java.io.File;
import java.util.regex.Pattern;

import sagex.phoenix.metadata.MediaType;

public class FileMatcher implements Comparable<FileMatcher> {
    private String id;
    private MediaType mediaType = MediaType.MOVIE;
    private File file;
    private String title, year;
    private Pattern fileRegex;
    private ID metadata;
    private ID fanart;

    private File sourceFile = null;

    public FileMatcher() {
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the fileregex
     */
    public Pattern getFileRegex() {
        return fileRegex;
    }

    /**
     * @param fileregex the fileregex to set
     */
    public void setFileRegex(Pattern fileregex) {
        this.fileRegex = fileregex;
    }

    public void setFileRegex(String regex) {
        this.fileRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * @return the metadata
     */
    public ID getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(ID metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the fanart
     */
    public ID getFanart() {
        return fanart;
    }

    /**
     * @param fanart the fanart to set
     */
    public void setFanart(ID fanart) {
        this.fanart = fanart;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the mediaType
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * @param mediaType the mediaType to set
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "FileMatcher [id=" + id + ", mediaType=" + mediaType + ", file=" + file + ", title=" + title + ", year=" + year
                + ", fileRegex=" + fileRegex + ", metadata=" + metadata + ", fanart=" + fanart + ", sourceFile=" + sourceFile + "]";
    }

    /**
     * The source file from which this filematcher was loaded
     *
     * @return
     */
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * The source file from which this filematcher was loaded
     *
     * @return
     */
    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        FileMatcher other = (FileMatcher) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int compareTo(FileMatcher o) {
        return id.compareTo(o.id);
    }
}
