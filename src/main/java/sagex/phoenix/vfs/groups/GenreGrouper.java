package sagex.phoenix.vfs.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class GenreGrouper implements IGrouper, IMultiGrouper {

    public String getGroupName(IMediaResource res) {
        String genre = null;
        if (res instanceof IMediaFile) {
            List<String> genres = ((IMediaFile) res).getMetadata().getGenres();
            if (genres != null && genres.size() > 0) {
                genre = genres.get(0);
            }
        }
        return genre;
    }

    @Override
    public List<String> getGroupNames(IMediaResource res) {
        if (res instanceof IMediaFile) {
            List<String> genres = ((IMediaFile) res).getMetadata().getGenres();
            if (genres != null && genres.size() > 0) {
                List<String> l = new ArrayList<String>();
                for (String g : genres) {
                    l.add(g);
                }
                return l;
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
