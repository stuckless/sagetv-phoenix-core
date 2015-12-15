package sagex.phoenix.vfs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.visitors.DebugVisitor;

/**
 * A Combined Media Folder is a special decorated folder that takes all the
 * children of the parent folder and promotes them to top level items.
 * <p/>
 * This is typically used for sage sources, where you want to present all
 * sources as single folder layout.
 *
 * @author seans
 */
public class CombinedMediaFolder extends DecoratedMediaFolder {
    private boolean combine = false;
    private Logger vlog = Loggers.VFS_LOG;
    private boolean isParent = true;

    public CombinedMediaFolder(IMediaFolder decorate, boolean combined, boolean isParent) {
        super(decorate);
        this.combine = true;
        this.isParent = isParent;
    }

    public CombinedMediaFolder(IMediaFolder decorate, boolean combined) {
        this(decorate, combined, true);
    }

    @Override
    protected List<IMediaResource> decorate(List<IMediaResource> originalChildren) {
        if (combine) {
            vlog.debug("Begin Combining Children for Folder: " + getTitle());
            if (vlog.isDebugEnabled()) {
                DebugVisitor walk = new DebugVisitor();
                getUndecoratedFolder().accept(walk, null, DEEP_UNLIMITED);
                log.debug("Dumping RAW Items Before Combining: " + getTitle() + "\n" + walk.toString());
            }

            List<IMediaResource> children = new ArrayList<IMediaResource>();

            if (!isParent) {
                vlog.debug("Combining Children for Folder: " + getTitle());
                combineChildren(originalChildren, children);
            } else {
                vlog.debug("Combining Top Level Children for Parent Folder: " + getTitle());
                // we are a top level folder, so combine our children
                for (IMediaResource r : originalChildren) {
                    if (r instanceof IMediaFolder) {
                        combineChildren(((IMediaFolder) r).getChildren(), children);
                    } else {
                        addMediaResource(r, children);
                    }
                }
            }

            vlog.debug("End Combining Children for Folder: " + getTitle());
            return children;
        } else {
            return originalChildren;
        }
    }

    private void combineChildren(List<IMediaResource> Children, List<IMediaResource> toList) {
        vlog.debug("Begin Adding Chilren: " + Children.size());
        for (IMediaResource r : Children) {
            if (vlog.isDebugEnabled()) {
                vlog.debug("Resource: " + r.getTitle() + "; Parent: " + r.getParent());
            }
            addMediaResource(r, toList);
        }
        vlog.debug("Beging Adding Chilren: " + Children.size());
    }

    private void addMediaResource(IMediaResource r, List<IMediaResource> toList) {
        // if the resource being added is file, the add it.
        // if it's folder, then see if we have folder, and if so, then
        // combine this folder with that folder.

        if (r instanceof IMediaFolder) {
            CombinedMediaFolder fold = findFolder(r.getTitle(), toList);
            if (fold == null) {
                vlog.debug("Creating Combined Folder for: " + r.getTitle());
                toList.add(new CombinedMediaFolder((IMediaFolder) r, true, false));
            } else {
                vlog.debug("Adding children to existing Folder for: " + fold.getTitle());
                combineChildren(((IMediaFolder) r).getChildren(), fold.getChildren());
            }
        } else {
            vlog.debug("Adding Resource: " + r.getTitle());
            toList.add(r);
        }
    }

    private CombinedMediaFolder findFolder(String title, List<IMediaResource> toList) {
        if (title == null)
            return null;
        for (IMediaResource r : toList) {
            if (r instanceof CombinedMediaFolder && title.equals(r.getTitle())) {
                return (CombinedMediaFolder) r;
            }
        }
        return null;
    }

    public void setCombined(boolean combined) {
        if (combined != this.combine) {
            setChanged();
        }
        this.combine = combined;
    }

    public boolean isCombined() {
        return combine;
    }
}
