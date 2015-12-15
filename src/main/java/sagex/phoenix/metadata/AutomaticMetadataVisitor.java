package sagex.phoenix.metadata;

import sagex.phoenix.Phoenix;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.progress.ProgressTracker;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.LogUtil;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

public class AutomaticMetadataVisitor implements IMediaResourceVisitor {
    private Hints options = null;

    public AutomaticMetadataVisitor(Hints options) {
        this.options = options;
        if (options == null)
            this.options = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
    }

    @Override
    public boolean visit(IMediaResource res, IProgressMonitor mon) {
        if (res instanceof IMediaFile) {
            try {
                if (mon != null)
                    mon.setTaskName(res.getTitle());

                if (Phoenix.getInstance().getMetadataManager().canScanMediaFile((IMediaFile) res, options)) {
                    Phoenix.getInstance().getMetadataManager().automaticUpdate((IMediaFile) res, options);
                    if (mon instanceof ProgressTracker) {
                        ((ProgressTracker) mon).addSuccess(res);
                    }
                } else {
                    LogUtil.logMetadataSkipped((IMediaFile) res);
                    if (mon instanceof ProgressTracker) {
                        ((ProgressTracker) mon).addSkipped(res, "Rejected by scan filters");
                    }
                }
            } catch (MetadataException e) {
                LogUtil.logMetadataUpdatedError((IMediaFile) res, e);
                Loggers.LOG.warn("AutomaticMetadataVisitor(): lookup failed with an error for " + res, e);
                if (mon instanceof ProgressTracker) {
                    ((ProgressTracker) mon).addFailed(res, e.getMessage());
                }
            } finally {
                if (mon != null) {
                    mon.worked(1);
                }
            }
        }
        return true;
    }
}
