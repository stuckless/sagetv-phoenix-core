package sagex.phoenix.configuration;

import org.apache.log4j.Logger;

import java.io.IOException;

public class SearchMetadataProvider implements IConfigurationMetadata {
    private static final Logger log = Logger.getLogger(SearchMetadataProvider.class);

    public Group[] load() throws IOException {
        Group searchTab = NewSearchGroup.getSearchParentGroup();
        searchTab.addElement(NewSearchGroup.getNewSearchGroupButton());
        return new Group[]{searchTab};
    }

    public void save() throws IOException {
        log.debug("Saving Search Metadata not implement");
    }
}
