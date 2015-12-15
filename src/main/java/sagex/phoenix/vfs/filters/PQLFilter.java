package sagex.phoenix.vfs.filters;

import sagex.phoenix.db.PQLParser;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Filters based on a PQL Expression, ie,
 * "SeasonNumber > 4 and EpisodeNumber = 3", etc
 *
 * @author seans
 */
public class PQLFilter extends Filter {
    private String value;
    private IResourceFilter filter = null;

    public PQLFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "PQL Expression", null, DataType.string));
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFolder)
            return true;
        if (filter != null) {
            return filter.accept(res);
        }
        return false;
    }

    @Override
    public void onUpdate() {
        try {
            value = getOption(OPT_VALUE).getString(null);
            PQLParser parser = new PQLParser(value);
            parser.parse();
            filter = parser.getFilter();
        } catch (Throwable e) {
            log.warn("Invalid PQL Expression " + value, e);
        }
    }
}
