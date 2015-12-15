package sagex.phoenix.metadata;

import org.apache.log4j.Logger;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.progress.*;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.*;
import sagex.phoenix.vfs.impl.FileResourceFactory;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Allows BMT to provide metadata support to the Phoenix Metadata apis.
 *
 * @author seans
 */
public class PhoenixMetadataSupport implements IMetadataSupport {
    private Logger log = Logger.getLogger(this.getClass());

    private ProgressTrackerManager trackerManager = new ProgressTrackerManager();

    public PhoenixMetadataSupport() {
    }

    public boolean updateMetadataForResult(Object media, IMetadataSearchResult result, Hints options) {
        try {
            IMetadata md = Phoenix.getInstance().getMetadataManager().getMetdata(result);
            Phoenix.getInstance().getMetadataManager().updateMetadata(phoenix.media.GetMediaFile(media), md, null);
        } catch (Exception e) {
            log.error("Failed to update metadata!", e);
            return false;
        }

        return true;
    }

    public float getMetadataScanComplete(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            return (float) mt.internalWorked();
        }
        return 0;
    }

    public boolean isMetadataScanRunning(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            return !(mt.isDone() || mt.isCancelled());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private ProgressTracker<IMediaFile> getTracker(Object tracker) {
        if (tracker != null) {
            return (ProgressTracker<IMediaFile>) trackerManager.getProgress((String) tracker);
        }
        log.warn("Attempted to get Status for null Tracker");
        return null;
    }

    public Object startMetadataScan(Object sageMediaFiles, final Hints options) {
        if (sageMediaFiles == null) {
            log.warn("Ignoring scan for null media items");
            return null;
        }

        final Hints opts = (options != null) ? options : Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();

        ProgressTracker<IMediaFile> tracker = new ProgressTracker<IMediaFile>();

        int size = IProgressMonitor.UNKNOWN;
        IMediaFolder fold = null;
        if (sageMediaFiles instanceof File) {
            fold = FileResourceFactory.createFolder((File) sageMediaFiles);
        } else if (sageMediaFiles instanceof IMediaFile) {
            fold = new VirtualMediaFolder("Single Item Scan");
            ((VirtualMediaFolder) fold).addMediaResource((IMediaResource) sageMediaFiles);
            size = 1;
        } else if (sageMediaFiles instanceof IMediaFolder) {
            fold = (IMediaFolder) sageMediaFiles;
            size = fold.getChildren().size();
        } else {
            size = ((Object[]) sageMediaFiles).length;
            fold = phoenix.umb.GetMediaAsFolder((Object[]) sageMediaFiles, "Scan from STV UI");
        }

        tracker.setLabel(fold.getTitle());
        tracker.beginTask("Scanning media files", size);

        final IMediaFolder folder = fold;
        IRunnableWithProgress<IProgressMonitor> runnable = new IRunnableWithProgress<IProgressMonitor>() {
            @Override
            public void run(IProgressMonitor monitor) {
                // add in our refresh or update visitor

                boolean refresh = options.getBooleanValue(MetadataHints.REFRESH, false);
                int deep = options.getBooleanValue(MetadataHints.SCAN_SUBFOLDERS, true) ? IMediaFolder.DEEP_UNLIMITED : 1;

                IMediaResourceVisitor vis = null;
                if (refresh) {
                    log.info("Automatic refresh fanart in progress: " + options);
                    vis = new RefreshMetadataVisitor(opts);
                } else {
                    log.info("Auto update in progress: " + options);
                    vis = new AutomaticMetadataVisitor(opts);
                }

                folder.accept(vis, monitor, deep);
            }
        };

        return trackerManager.runWithProgress(runnable, tracker);
    }

    public boolean cancelMetadataScan(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            log.info("Cancelling Media Scan: " + tracker);
            mt.setCancelled(true);
        }
        return true;
    }

    public IMetadataSearchResult[] getMetadataSearchResults(Object media, String title, String type) {
        try {
            IMediaFile smf = phoenix.media.GetMediaFile(media);
            if (smf == null) {
                log.warn("Failed to convert resource into a vfs resource for: " + media);
                return null;
            }

            SearchQuery q = Phoenix.getInstance().getSearchQueryFactory()
                    .createSageFriendlyQuery(smf, Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
            ;

            // set the query to be the title, if it's passed
            if (title != null) {
                log.info("Using Specified title: " + title + " for mediafile: " + media);
                q.set(Field.QUERY, title);
            } else {
                q.set(Field.QUERY, q.get(Field.RAW_TITLE));
            }

            // set the specific media type, if was passed
            if (type != null) {
                MediaType mt = MediaType.toMediaType(type);
                if (mt != null) {
                    log.info("Using Specified media type: " + mt + " for mediafile: " + media);
                    q.setMediaType(mt);
                } else {
                    log.warn("failed to convert media type: " + type + " to a valid media type");
                }
            }

            log.info("Metadata Search for: " + q + "; media: " + media);
            List<IMetadataSearchResult> l = Phoenix.getInstance().getMetadataManager().search(q);

            if (l == null || l.size() == 0) {
                log.debug("No matches for: " + q);
            } else {
                return l.toArray(new IMetadataSearchResult[l.size()]);
            }
        } catch (Exception e) {
            log.warn("Failed to do a metadata lookup for: " + media, e);
        }

        return null;
    }

    public IMetadataSearchResult[] getMetadataSearchResults(Object media) {
        return getMetadataSearchResults(media, null, null);
    }

    public Object[] getFailed(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getFailedItems(progress);
        if (items != null) {
            IMediaFile files[] = new IMediaFile[items.size()];
            for (int i = 0; i < files.length; i++) {
                files[i] = items.get(i).getItem();
            }
            return files;
        }
        return new Object[]{};
    }

    public int getFailedCount(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getFailedItems(progress);
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public Object[] getSkipped(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getSkippedItems(progress);
        if (items != null) {
            IMediaFile files[] = new IMediaFile[items.size()];
            for (int i = 0; i < files.length; i++) {
                files[i] = items.get(i).getItem();
            }
        }
        return new Object[]{};
    }

    public int getSkippedCount(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getSkippedItems(progress);
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public Object[] getSuccess(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getSuccessfulItems(progress);
        if (items != null) {
            IMediaFile files[] = new IMediaFile[items.size()];
            for (int i = 0; i < files.length; i++) {
                files[i] = items.get(i).getItem();
            }
        }
        return new Object[]{};
    }

    public int getSuccessCount(Object progress) {
        LinkedList<TrackedItem<IMediaFile>> items = getSuccessfulItems(progress);
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public LinkedList<TrackedItem<IMediaFile>> getSuccessfulItems(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            return mt.getSuccessfulItems();
        }
        return null;
    }

    public LinkedList<TrackedItem<IMediaFile>> getFailedItems(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            return mt.getFailedItems();
        }
        return null;
    }

    public LinkedList<TrackedItem<IMediaFile>> getSkippedItems(Object tracker) {
        ProgressTracker<IMediaFile> mt = getTracker(tracker);
        if (mt != null) {
            return mt.getSkippedItems();
        }
        return null;
    }

    @Override
    public Object[] getTrackers() {
        return trackerManager.getProgressIds().toArray();
    }

    @Override
    public String getMetadataScanLabel(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.getLabel();
        }
        return null;
    }

    @Override
    public Date getMetadataScanLastUpdated(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.getLastUpdated();
        }
        return null;
    }

    @Override
    public String getMetadataScanStatus(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.getTaskName();
        }
        return null;
    }

    @Override
    public boolean isMetadataScanCancelled(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.isCancelled();
        }
        return true;
    }

    @Override
    public int getTotalWork(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.getTotalWork();
        }
        return IProgressMonitor.UNKNOWN;
    }

    @Override
    public int getWorked(Object progress) {
        ProgressTracker<IMediaFile> mt = getTracker(progress);
        if (mt != null) {
            return mt.getWorked();
        }
        return 0;
    }

    @Override
    public void removeMetadataScan(Object progress) {
        trackerManager.removeProgress((String) progress);
    }
}
