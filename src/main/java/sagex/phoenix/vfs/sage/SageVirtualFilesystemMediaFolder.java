package sagex.phoenix.vfs.sage;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.api.MediaFileAPI;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.impl.FileResourceFactory;

/**
 * Creates a Virtual FileSystem layout for collection of Sage MediaFile objects
 * from the given directory.
 * <p/>
 * The advantage of this layout is that a virtual folder layout is presented but
 * it will ever only include files that are in the Sage Library.
 *
 * @author seans
 */
public class SageVirtualFilesystemMediaFolder extends VirtualMediaFolder {
    private String types;

    public SageVirtualFilesystemMediaFolder(IMediaFolder parent, File dir, String types) {
        super(parent, dir.getName(), dir, StringUtils.isEmpty(dir.getName()) ? String.valueOf(dir) : dir.getName(), true);
        this.types = types;
    }

    protected void addSageMediaFile(List<IMediaResource> children, Object mf) {
        if (mf == null) {
            // don't add it, it's not a sage file
            return;
        }

        children.add(new SageMediaFile(this, mf));
    }

    @Override
    public void populateChildren(List<IMediaResource> children) {
        File dir = (File) getMediaObject();
        if (dir != null) {
            log.debug("Initializing Sage Media for Dir: " + dir.getAbsolutePath() + "; Types: " + types);
            Object files[] = null;
            if (types == null) {
                files = MediaFileAPI.GetMediaFiles();
            } else {
                files = MediaFileAPI.GetMediaFiles(types);
            }

            String path = dir.getAbsolutePath() + File.separator;

            if (files == null || files.length == 0) {
                log.warn("No Children for: " + getTitle() + "; types: " + types);
                return;
            }

            for (Object mf : files) {
                File f = MediaFileAPI.GetFileForSegment(mf, 0);
                if (f == null) {
                    log.warn("No File Segment for Sage File: " + mf);
                    continue;
                }

                String path2 = f.getAbsolutePath();
                if (path2.startsWith(path)) {
                    int offset = path.length();
                    String subpath = path2.substring(offset);
                    String parts[] = StringUtils.split(subpath, File.separator);
                    if (parts.length == 1) {
                        addSageMediaFile(children, MediaFileAPI.GetMediaFileForFilePath(new File(dir, parts[0])));
                    } else if (parts.length > 1) {
                        File special = new File(dir, parts[0]);
                        if (FileResourceFactory.isDVD(special)) {
                            addSageMediaFile(children,
                                    MediaFileAPI.GetMediaFileForFilePath(FileResourceFactory.resolveDVD(special)));
                            continue;
                        }
                        if (FileResourceFactory.isBluRay(special)) {
                            addSageMediaFile(children,
                                    MediaFileAPI.GetMediaFileForFilePath(FileResourceFactory.resolveBluRay(special)));
                            continue;
                        }

                        if (getSageFolder(children, special) == null) {
                            children.add(new SageVirtualFilesystemMediaFolder(this, special, types));
                        }
                    } else {
                        log.debug("Failed to split parts for: " + subpath + "; using: " + File.separator);
                    }
                }
            }
        }
    }

    private SageVirtualFilesystemMediaFolder getSageFolder(List<IMediaResource> children, File file) {
        for (IMediaResource r : children) {
            if (r instanceof SageVirtualFilesystemMediaFolder) {
                if (file.equals(((SageVirtualFilesystemMediaFolder) r).getDir())) {
                    return (SageVirtualFilesystemMediaFolder) r;
                }
            }
        }
        return null;
    }

    protected File getDir() {
        return (File) getMediaObject();
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
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SageVirtualFilesystemMediaFolder other = (SageVirtualFilesystemMediaFolder) obj;
        if (types == null) {
            if (other.types != null)
                return false;
        } else if (!types.equals(other.types))
            return false;
        return true;
    }
}
