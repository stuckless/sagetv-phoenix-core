package sagex.phoenix.configuration;

import org.apache.log4j.Logger;
import sagex.phoenix.Phoenix;
import sagex.phoenix.util.ElapsedTimer;

/**
 * A Search Result Group is a simple group that will collect all configuration
 * elements that match it's "label" as the the search string.
 * <p/>
 * The actual search is deferred until getChildren() is called.
 *
 * @author seans
 */
public class SearchResultGroup extends VirtualGroup {
    private static final Logger log = Logger.getLogger(SearchResultGroup.class);

    public SearchResultGroup(String id) {
        super(id);
        setSearchString(id);
    }

    public String getSearchString() {
        return getLabel();
    }

    public void setSearchString(String text) {
        setLabel(text);
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        elements.clear();
        final String search = label;
        ElapsedTimer timer = new ElapsedTimer();
        IConfigurationElement[] parents = Phoenix.getInstance().getConfigurationMetadataManager().getParentGroups();
        for (IConfigurationElement g : parents) {
            if (!NewSearchGroup.NEW_SEARCH_PARENT_ID.equals(g.getId()) && !NewSearchGroup.NEW_SEARCH_GROUP_ID.equals(g.getId())) {
                g.visit(new IConfigurationMetadataVisitor() {
                    public void accept(IConfigurationElement el) {
                        if (el.isVisible() && el instanceof Field
                                && (find(search, el.getId()) || find(search, el.getLabel()) || find(search, el.getDescription()))) {
                            addElement(new WrappedSearchResultField((Field) el));
                        }
                    }
                });
            }
        }
        log.debug("Search for: " + search + " took " + timer.delta() + "ms");
    }

    protected boolean find(String search, String in) {
        if (search == null || in == null)
            return false;
        return in.toUpperCase().contains(search.toUpperCase());
    }
}
