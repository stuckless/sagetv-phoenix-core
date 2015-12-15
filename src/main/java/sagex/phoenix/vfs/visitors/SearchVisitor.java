package sagex.phoenix.vfs.visitors;

import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.db.PQLParser;
import sagex.phoenix.db.ParseException;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.filters.IResourceFilter;

/**
 * Used to search a mediafolder for a given mediafile
 * <p/>
 * The Query can be a complete metadata query using metadata field names, such
 * as,
 * <p/>
 * <pre>
 * Title contains 'House' and (SeasonNumber = 3 or SeasonNumber = 4)
 * </pre>
 *
 * @author seans
 */
public class SearchVisitor extends FileVisitor {
    private List<IMediaFile> files = new ArrayList<IMediaFile>();
    private IResourceFilter searchFilter = null;
    private String search = null;

    public SearchVisitor(IResourceFilter filter) {
        this.searchFilter = filter;
    }

    /**
     * @param query
     * @param isQuery
     * @throws ParseException
     */
    public SearchVisitor(String query) throws ParseException {
        this.search = query;
        PQLParser parser = new PQLParser(this.search);
        parser.parse();
        this.searchFilter = parser.getFilter();
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        if (searchFilter != null) {
            if (searchFilter.accept(res)) {
                files.add(res);
                incrementAffected();
                return true;
            }
        }
        return true;
    }

    public List<IMediaFile> getFiles() {
        return files;
    }

    public String getQuery() {
        return search;
    }

    public IResourceFilter getFilter() {
        return searchFilter;
    }
}
