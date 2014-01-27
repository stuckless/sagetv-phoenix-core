package sagex.phoenix.vfs.ov;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;

/**
 * Creates a VFS source of media items by mapping xml elements to metadata elements
 *  
 * @author sls
 */
public class XmlSourceFactory extends Factory<IMediaFolder> {
	private XmlOptions options = null;
	public XmlSourceFactory(XmlOptions options) {
		super();
		this.options=options;
		for (ConfigurableOption co: options.getOptions()) {
			addOption(co);
		}
	}
	
	public XmlSourceFactory() {
		this(new XmlOptions());
	}
	
	/**
	 * @param configurableOptions unused at this time
	 */
    @Override
    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
    	// update our options with the factory options
    	for (ConfigurableOption co: getOptions()) {
    		options.addOption(co.getName(), co.value().getValue(), co.getLabel());
    	}
    	
    	// add in user options
    	if (configurableOptions!=null) {
        	for (ConfigurableOption co: configurableOptions) {
        		options.addOption(co.getName(), co.value().getValue(), co.getLabel());
        	}
    	}
    	
    	XmlFolder folder = new XmlFolder(null, getLabel(), options);
    	return folder;
    }
}
