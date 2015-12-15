package sagex.phoenix.vfs.impl;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

public class FileMediaFolder extends VirtualMediaFolder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Can only be created using the {@link FileResourceFactory}, to ensure that
     * the correct type of folder is created
     *
     * @param thisDir
     */
    FileMediaFolder(File thisDir) {
        this(null, thisDir);
    }

    FileMediaFolder(IMediaFolder parent, File thisDir) {
        super(parent, thisDir.getAbsolutePath(), thisDir, thisDir.getName());
    }

    @Override
    protected void populateChildren(List<IMediaResource> children) {
        if (getDir() != null) {
            File[] files = getDir().listFiles();
            if (files != null) {
                for (File f : files) {
                    IMediaResource r = FileResourceFactory.createResource(this, f);
                    if (r != null) {
                        children.add(r);
                    } else {
                        log.warn("Failed to create File Resource for: " + f.getAbsolutePath());
                    }
                }
            }
        }
    }

    protected File getDir() {
        return (File) getMediaObject();
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.vfs.impl.MediaFolder#delete()
     */
    @Override
    public boolean delete(Hints hints) {
        boolean deleted = super.delete(hints);

        if (getDir() != null) {
            FileCleaner.clean(getDir());
            deleted = !getDir().exists();
        }
        return deleted;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.vfs.impl.MediaResource#exists()
     */
    @Override
    public boolean exists() {
        if (getDir() != null)
            return getDir().exists();
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.vfs.impl.MediaResource#lastModified()
     */
    @Override
    public long lastModified() {
        if (getDir() != null) {
            return getDir().lastModified();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.vfs.impl.MediaResource#touch()
     */
    @Override
    public void touch(long time) {
        super.touch(time);
        if (getDir() != null) {
            FileToucher.touch(getDir(), time);
        }
    }
}
