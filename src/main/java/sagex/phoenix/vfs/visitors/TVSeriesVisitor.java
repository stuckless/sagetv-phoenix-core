package sagex.phoenix.vfs.visitors;

import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.persistence.TVSeriesUtil;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Goes through all the TV shows and for shows that currently do not have a TV
 * SeriesInfo object, then it will find one, and add it.
 *
 * @author seans
 */
public class TVSeriesVisitor extends FileVisitor {
    public TVSeriesVisitor() {
    }

    @Override
    public boolean visitFile(IMediaFile file, IProgressMonitor monitor) {
        try {
            if (!file.isType(MediaResourceType.TV.value())) {
                // nothing to do, we are not TV
                return true;
            }

            ISeriesInfo info = phoenix.media.GetSeriesInfo(file);
            if (info != null) {
                // nothing to do, we have it already
                return true;
            }

            if (TVSeriesUtil.updateTVSeriesInfoForFile(file)) {
                // we are updated
                incrementAffected();
                return true;
            }

            // still no updated, and there is nothing we can about it
        } catch (Exception e) {
            log.warn("Failed while trying to get tv series info for " + file);
        }
        return true;
    }
}
