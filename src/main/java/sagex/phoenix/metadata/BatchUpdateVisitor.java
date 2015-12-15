package sagex.phoenix.metadata;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

import java.util.Map;

/**
 * batch updates each mediafile with the given set of metadata properties
 *
 * @author sean
 */
public class BatchUpdateVisitor implements IMediaResourceVisitor {
    private Map<String, String> props;

    public BatchUpdateVisitor(Map<String, String> props) {
        this.props = props;
    }

    @Override
    public boolean visit(IMediaResource res, IProgressMonitor monitor) {
        if (res instanceof IMediaFile) {
            Loggers.LOG.debug("Batch Updating: " + res.getTitle());
            MetadataUtil.batchUpdate(props, ((IMediaFile) res).getMetadata());
        }
        return true;
    }
}
