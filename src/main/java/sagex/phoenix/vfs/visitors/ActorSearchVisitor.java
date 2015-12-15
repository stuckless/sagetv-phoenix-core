package sagex.phoenix.vfs.visitors;

import java.util.List;

import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class ActorSearchVisitor extends FileVisitor {
    private String searchContains = null;
    private List<IMediaResource> addTo;

    public ActorSearchVisitor(String searchContains, List<IMediaResource> addTo) {
        if (searchContains == null) {
            searchContains = "";
        }

        this.searchContains = searchContains.toLowerCase();
        this.addTo = addTo;
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        IMetadata md = res.getMetadata();
        if (md == null)
            return false;

        List<ICastMember> actors = md.getActors();
        if (actors != null) {
            for (ICastMember cm : actors) {
                if (isMatch(cm)) {
                    addTo.add(res);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMatch(ICastMember cm) {
        String name = cm.getName();
        if (name != null && name.toLowerCase().contains(searchContains)) {
            return true;
        }

        String role = cm.getRole();
        if (role != null && role.toLowerCase().contains(searchContains)) {
            return true;
        }

        return false;
    }

}
