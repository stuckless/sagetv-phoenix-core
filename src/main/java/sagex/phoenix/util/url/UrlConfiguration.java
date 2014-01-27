package sagex.phoenix.util.url;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(path = "bmt/urlconfiguration", label="URL Settings", description = "Configures Properties regarding how URLs are handled.")
public class UrlConfiguration extends GroupProxy {
    @AField(label="URL Cache Expiry", description = "How long, in seconds, URLs remain in the cache", visible="prop:phoenix/core/enableAdvancedOptions")
    protected FieldProxy<Integer> cacheExpiryInSeconds = new FieldProxy<Integer>(60 * 30);

    @AField(label="URL Factory Class", description = "URL Factory class name for creating new Url objects", visible="false")
    protected FieldProxy<String> urlFactoryClass      = new FieldProxy<String>(CachedUrlFactory.class.getName());

    @AField(label="Http User Agent", description = "HTTP User Agent that is sent with each hew http request", visible="prop:phoenix/core/enableAdvancedOptions")
    protected FieldProxy<String> httpUserAgent        =  new FieldProxy<String>("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.1) Gecko/2008072820 Firefox/3.0.1");

    @AField(label="Connect Timeout (ms)", description = "How before a connection will timeout when trying to establish a connection", visible="prop:phoenix/core/enableAdvancedOptions")
    protected FieldProxy<Integer> connectTimeout        =  new FieldProxy<Integer>(1000*10);

    @AField(label="Read Timeout (ms)", description = "How long a connection will remain open before it will timeout while reading", visible="prop:phoenix/core/enableAdvancedOptions")
    protected FieldProxy<Integer> readTimeout        =  new FieldProxy<Integer>(1000*10);

    public UrlConfiguration() {
        super();
        init();
    }

    public int getCacheExpiryInSeconds() {
        return cacheExpiryInSeconds.get();
    }


    public void setCacheExpiryInSeconds(int cacheExpiryInSeconds) {
        this.cacheExpiryInSeconds.set(cacheExpiryInSeconds);
    }

    public int getReadTimeoutMS() {
        return readTimeout.get();
    }
    
    public void setReadTimeoutMS(int ms) {
    	readTimeout.set(ms);
    }

    public int getConnectTimeoutMS() {
        return connectTimeout.get();
    }
    
    public void setConnectTimeoutMS(int ms) {
    	connectTimeout.set(ms);
    }

    public String getUrlFactoryClass() {
        return urlFactoryClass.get();
    }

    public void setUrlFactoryClass(String urlFactoryClass) {
        this.urlFactoryClass.set(urlFactoryClass);
    }

    public String getHttpUserAgent() {
        return httpUserAgent.get();
    	//return "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17";
    }

    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent.set(httpUserAgent);
    }
}
