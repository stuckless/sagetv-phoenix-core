package sagex.phoenix.configuration;

/**
 * NewSearchGroup is a Special Placeholder Class that identifies a
 * NewSearchGroup element that will we used to create new SearchResultGroup
 * elements
 * 
 * @author seans
 * 
 */
public class NewSearchGroup extends VirtualGroup {
	public static final String NEW_SEARCH_PARENT_ID = "phoenix_configuration_search";
	public static final String NEW_SEARCH_GROUP_ID = "newsearch";
	public static final Group parentSearchGroup = new Group(NEW_SEARCH_GROUP_ID);
	static {
		parentSearchGroup.setElementType(IConfigurationElement.APPLICATION);
		parentSearchGroup.setLabel("Search");
	}
	private static final Group NEW_SEARCH_GROUP_INSTANCE = new NewSearchGroup(NEW_SEARCH_GROUP_ID);

	protected NewSearchGroup(String id) {
		super(NEW_SEARCH_GROUP_ID);
		setLabel("New Search");
	}

	@Override
	public void addElement(IConfigurationElement e) {
		throw new RuntimeException("Cannot Add Elements to a NewSearchGroup element");
	}

	public static Group newSearch(String search) {
		SearchResultGroup result = new SearchResultGroup(search);
		parentSearchGroup.addElement(result);
		return result;
	}

	public static Group getSearchParentGroup() {
		return parentSearchGroup;
	}

	public static Group getNewSearchGroupButton() {
		return NEW_SEARCH_GROUP_INSTANCE;
	}

	public static void removeSearch(SearchResultGroup el) {
		parentSearchGroup.removeElement(el);
	}
}
