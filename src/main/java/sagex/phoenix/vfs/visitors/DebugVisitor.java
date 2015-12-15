package sagex.phoenix.vfs.visitors;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;

/**
 * Will Build a debug string of the folder contents.
 *
 * @author seans
 */
public class DebugVisitor extends StructureVisitor {
    private StringBuilder sb = new StringBuilder();
    private int padding = 0;

    public DebugVisitor() {
        super();
    }

    @Override
    public void file(IMediaFile r, IProgressMonitor mon) {
        sb.append(padding()).append("Item: " + r.getTitle() + "; [" + r + "]\n");
    }

    @Override
    public void beforeFolder(IMediaFolder folder, IProgressMonitor monitor) {
        sb.append(padding()).append("Begin Folder: " + folder.getTitle() + "; Size: " + folder.getChildren().size() + "\n");
        padding += 3;
    }

    @Override
    public void afterFolder(IMediaFolder folder, IProgressMonitor monitor) {
        padding -= 3;
        sb.append(padding()).append("End Folder  : " + folder.getTitle() + "\n");
    }

    private String padding() {
        return StringUtils.leftPad("", padding);
    }

    public String getDebugInfo() {
        return toString();
    }

    /**
     * will return the all the debug information that was captured as a string.
     */
    public String toString() {
        return sb.toString();
    }
}
