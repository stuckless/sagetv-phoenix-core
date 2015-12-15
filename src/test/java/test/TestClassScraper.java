package test;

import sagex.phoenix.metadata.search.IFilenameScraper;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

public class TestClassScraper implements IFilenameScraper {

    @Override
    public String getId() {
        return "testclassscraper";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public SearchQuery createSearchQuery(IMediaFile file, Hints hints) {
        if (PathUtils.getLocation(file).contains("ClassTest")) {
            SearchQuery q = new SearchQuery(hints);
            q.set(Field.RAW_TITLE, "Test Scraper");
            q.set(Field.YEAR, "2011");
        }
        return null;
    }

}
