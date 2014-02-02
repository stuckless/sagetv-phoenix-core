package sagex.phoenix.vfs.builder;

import org.xml.sax.SAXException;

import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.filters.FilterFactory;

public class TagsBuilder extends FactoryItemBuilder<FilterFactory> {
	public TagsBuilder(VFSManager mgr) {
		super(mgr, "tags");
	}

	@Override
	protected FilterFactory createFactory(String className) throws SAXException {
		return null;
	}

	@Override
	protected void factoryConfigured(FilterFactory factory) {
	}
}
