package sagex.phoenix.util.url;

import sagex.phoenix.util.Loggers;

public class UrlFactory implements IUrlFactory {
    protected static IUrlFactory factory = new CachedUrlFactory();

    public static IUrl newUrl(String url) {
        try {
            return factory.createUrl(url);
        } catch (Exception e) {
            Loggers.LOG.error("Factory failed to create a url from the factory, so we are returning default url.", e);
            // we never a let url return the error
            return new Url(url);
        }
    }

    public IUrl createUrl(String url) {
        return new Url(url);
    }
    
    public static void setFactory(IUrlFactory factory) {
    	UrlFactory.factory=factory;
    }
}
