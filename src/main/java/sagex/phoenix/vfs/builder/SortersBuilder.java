package sagex.phoenix.vfs.builder;

import org.xml.sax.SAXException;

import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.sorters.SorterFactory;

public class SortersBuilder extends FactoryItemBuilder<SorterFactory> {
    public SortersBuilder(VFSManager mgr) {
        super(mgr, "sorts");
    }

    @Override
    protected SorterFactory createFactory(String className) throws SAXException {
        try {
            return new SorterFactory(className);
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
    protected void factoryConfigured(SorterFactory factory) {
        manager.getVFSSortFactory().addFactory(factory);
    }
}
