package sagex.phoenix.vfs.builder;

import org.xml.sax.SAXException;

import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VFSManager;

public class SourcesBuilder extends FactoryItemBuilder<Factory<IMediaFolder>> {
    public SourcesBuilder(VFSManager mgr) {
        super(mgr,"sources");
    }

	@Override
	protected Factory<IMediaFolder> createFactory(String className) throws SAXException {
		try {
			return (Factory<IMediaFolder>) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			error("Failed to create factory for  " + className, e);
		} catch (IllegalAccessException e) {
			error("Failed to access factory for  " + className, e);
		} catch (ClassNotFoundException e) {
			error("Failed to find class instance for factory for  " + className, e);
		} catch (Exception t) {
			error("Failed to load source from class " + className, t);
		}
		return null;
	}

	@Override
	protected void factoryConfigured(Factory<IMediaFolder> factory) {
		manager.getVFSSourceFactory().addFactory(factory);
	}
}
