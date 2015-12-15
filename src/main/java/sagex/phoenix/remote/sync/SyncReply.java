package sagex.phoenix.remote.sync;

import java.util.List;

public class SyncReply {
    private String id;
    public int totalFiles;
    public int totalPages;
    public int page;
    public List<SyncMediaFile> files;

    public SyncReply(String id) {
        this.id = id;
    }

}
