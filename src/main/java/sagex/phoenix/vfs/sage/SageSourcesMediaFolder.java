package sagex.phoenix.vfs.sage;

import sagex.api.Configuration;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

import java.io.File;
import java.util.List;

/**
 * A Virtual Folder that will contain all the files from the sage imported
 * library sources.
 *
 * @author seans
 */
public class SageSourcesMediaFolder extends VirtualMediaFolder {
    private String types = null;

    public SageSourcesMediaFolder(String types, String folderName) {
        super(null, folderName, null, folderName, true);
        this.types = types;
    }

    @Override
    public void populateChildren(List<IMediaResource> ch) {
        if (types == null) {
            addSource(ch, types, Configuration.GetLibraryImportPaths());
        } else {
            if (types.contains("V") || types.contains("B") || types.contains("D")) {
                addSource(ch, types, Configuration.GetVideoLibraryImportPaths());
            }
            if (types.contains("T")) {
                addSource(ch, types, Configuration.GetVideoDirectories());
            }
            if (types.contains("P")) {
                addSource(ch, types, Configuration.GetPictureLibraryImportPaths());
            }
            if (types.contains("M")) {
                addSource(ch, types, Configuration.GetMusicLibraryImportPaths());
            }
        }
    }

    protected void addSource(List<IMediaResource> ch, String type, File files[]) {
        for (File f : files) {
            SageVirtualFilesystemMediaFolder smf = new SageVirtualFilesystemMediaFolder(this, f, type);
            if (smf.getChildren().size() > 0) {
                log.info("Added files from Sage Import Source: " + f.getAbsolutePath() + "; Type: " + type + "; FolderName: "
                        + smf.getTitle() + "; Children: " + smf.getChildren().size());
                ch.add(smf);
            } else {
                log.warn("No Files for Sage Import: " + f.getAbsolutePath());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((types == null) ? 0 : types.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        /**
         * NOTE: equals works here because 2 SageSourcesMediaFolder that are
         * created using the same types will be equal
         */
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SageSourcesMediaFolder other = (SageSourcesMediaFolder) obj;
        if (types == null) {
            if (other.types != null)
                return false;
        } else if (!types.equals(other.types))
            return false;
        return true;
    }
}
