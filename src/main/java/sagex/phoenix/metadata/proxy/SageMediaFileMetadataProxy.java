package sagex.phoenix.metadata.proxy;

import sagex.api.MediaFileAPI;
import sagex.phoenix.metadata.IMetadata;

/**
 * metadata proxy that uses Set/GetMediaFileMetadata for storing/accessing metadata.
 * 
 * @author seans
 * 
 */
public class SageMediaFileMetadataProxy extends AbstractMetadataProxy {
	private Object sageMediaFile = null;
	
	// Fanart is non-persistent
	private String fanart = null;
	
	/**
	 * use newInstance(mediaFile)
	 * 
	 * @param sageMediaFile
	 */
	protected SageMediaFileMetadataProxy(Object sageMediaFile) {
		this.sageMediaFile = sageMediaFile;
	}
	
	@Override
	public String get(SageProperty key) {
		if ("Fanart".equals(key.value())) return fanart;
		
		return MediaFileAPI.GetMediaFileMetadata(sageMediaFile, key.value());
	}
	
	@Override
	public void set(SageProperty key, String value) {
		if ("Fanart".equals(key.value())) {
			this.fanart=value;
			return;
		}
		
		MediaFileAPI.SetMediaFileMetadata(sageMediaFile, key.value(), value);
	}
	
    public static IMetadata newInstance(Object mediaFile) {
        return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(SageMediaFileMetadataProxy.class.getClassLoader(), new Class[] {IMetadata.class}, new SageMediaFileMetadataProxy(mediaFile));
    }
}
