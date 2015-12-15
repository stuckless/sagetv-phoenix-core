package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.util.List;

public class GenreStartsWithFilter extends Filter {
    public GenreStartsWithFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "Genre", null, DataType.string));
    }

    public boolean canAccept(IMediaResource res) {
        String genre = getOption(OPT_VALUE).getString(null);

        if (genre == null)
            return false;
        if (res instanceof IMediaFile) {
            List<String> g1 = ((IMediaFile) res).getMetadata().getGenres();
            if (g1 == null || g1.size() == 0)
                return false;
            for (String s : g1) {
                if (s.startsWith(genre)) {
                    return true;
                }
            }
        }
        return false;
    }
}
