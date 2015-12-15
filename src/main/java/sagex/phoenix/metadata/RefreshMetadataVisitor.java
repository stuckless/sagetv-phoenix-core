package sagex.phoenix.metadata;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.progress.ProgressTracker;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.PathUtils;
import sagex.phoenix.vfs.visitors.FileVisitor;

/**
 * For each media item, get the metadata provider id and refresh fanart/metadata
 * for the item.
 *
 * @author seans
 */
public class RefreshMetadataVisitor extends FileVisitor {
    private Hints hints = null;

    public RefreshMetadataVisitor(Hints hints) {
        this.hints = hints;
    }

    @Override
    public boolean visitFile(IMediaFile file, IProgressMonitor monitor) {
        try {
            log.debug("Refreshing " + PathUtils.getLocation(file));
            String title = null;
            SearchQuery q = new SearchQuery(hints);
            if (file.isType(MediaResourceType.TV.value())) {
                IMetadata md = file.getMetadata();
                if (md.getEpisodeNumber() > 0) {
                    title = file.getTitle() + " " + md.getEpisodeName();
                    q.setMediaType(MediaType.TV);
                    q.set(Field.PROVIDER, md.getMediaProviderID());
                    q.set(Field.ID, md.getMediaProviderDataID());
                    q.set(Field.EPISODE, String.valueOf(md.getEpisodeNumber()));
                    q.set(Field.SEASON, String.valueOf(md.getSeasonNumber()));
                    if (isEmpty(q.get(Field.PROVIDER), q.get(Field.ID), q.get(Field.EPISODE), q.get(Field.SEASON))) {
                        monitor.setTaskName("Skipped: " + md.getRelativePathWithTitle() + " - " + md.getEpisodeName());
                        q = null;
                    }
                }
            } else if (file.isType(MediaResourceType.ANY_VIDEO.value())) {
                IMetadata md = file.getMetadata();
                title = md.getEpisodeName();
                q.setMediaType(MediaType.MOVIE);
                q.set(Field.PROVIDER, md.getMediaProviderID());
                q.set(Field.ID, md.getMediaProviderDataID());
                if (isEmpty(q.get(Field.PROVIDER), q.get(Field.ID))) {
                    monitor.setTaskName("Skipping: " + md.getEpisodeName());
                    q = null;
                }
            } else {
                log.warn("Can't refresh metadata/fanart for: " + file);
            }

            if (q != null) {
                monitor.setTaskName("Refreshing " + title);
                Phoenix.getInstance().getMetadataManager().automaticUpdate(file, q, hints);
                if (monitor instanceof ProgressTracker) {
                    ((ProgressTracker) monitor).addSuccess(file);
                }
                log.info("Refreshed " + title);
            }

            if (monitor instanceof ProgressTracker) {
                ((ProgressTracker) monitor).addSkipped(file, "No provider info");
            }

            return !monitor.isCancelled();
        } catch (Throwable e) {
            log.warn("Refresh Failed for: " + file);
            if (monitor instanceof ProgressTracker) {
                ((ProgressTracker) monitor).addFailed(file, e.getMessage());
            }
        } finally {
            if (monitor != null) {
                monitor.worked(1);
            }
        }
        return true;
    }

    private boolean isEmpty(String... values) {
        if (values == null)
            return true;
        for (String v : values) {
            if (StringUtils.isEmpty(v))
                return true;
        }
        return false;
    }
}
