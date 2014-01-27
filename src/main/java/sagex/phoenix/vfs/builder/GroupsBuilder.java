package sagex.phoenix.vfs.builder;

import org.xml.sax.SAXException;

import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.groups.GroupingFactory;

public class GroupsBuilder extends FactoryItemBuilder<GroupingFactory> {
    public GroupsBuilder(VFSManager mgr) {
        super(mgr,"groups");
    }

	@Override
	protected GroupingFactory createFactory(String className) throws SAXException {
		try {
			return new GroupingFactory(className);
		} catch (InstantiationException e) {
			error("Failed to create factory for  " + className, e);
		} catch (IllegalAccessException e) {
			error("Failed to access factory for  " + className, e);
		} catch (ClassNotFoundException e) {
			error("Failed to find class instance for factory for  " + className, e);
		}
		return null;
	}

	@Override
	protected void factoryConfigured(GroupingFactory factory) {
		manager.getVFSGroupFactory().addFactory(factory);
	}
}
