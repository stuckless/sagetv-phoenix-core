package sagex.phoenix.vfs.ov;

import sagex.phoenix.util.PhoenixManagedScriptEngineProxy;

/**
 * Used to resolve one url to another.  ie, some sites provide a video url (ie, youtube) but
 * it's not the 'real' url.  Implementations of this class will translate the 'fake' url into
 * the real url that is capable of fetching the direct music/video stream.
 * 
 * To create an {@link IUrlResolver} from a script file use {@link PhoenixManagedScriptEngineProxy}.newInstance(file, {@link IUrlResolver}.class)
 * 
 * @author seans
 *
 */
public interface IUrlResolver {
	/**
	 * Return true if this resolver can handle this url (usually you'll just do some pattern
	 * matching on the url to see if this is a url that you can handle)
	 * 
	 * @param url
	 * @return
	 */
	public boolean canAccept(String url);
	
	/**
	 * Given the url, resolve it into another url that is capable for fetching the music/video
	 * stream directly.
	 * 
	 * @param url
	 * @return
	 */
	public String getUrl(String url);
}
