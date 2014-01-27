package sagex.phoenix.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sagex.phoenix.util.NamedValue;

/**
 * Factory for returning a static option list
 * 
 * @author sean
 */
public class StaticOptionsFactory implements IOptionFactory, Iterable<NamedValue> {
	private List<NamedValue> options = new ArrayList<NamedValue>();

	public StaticOptionsFactory() {
	}

	/**
	 * Creates option list using a string in the format "value1:label1, value2:label2, value3:label3,..."
	 * alternately you can use semi-colon instead of comma
	 * 
	 * @param optionString
	 */
	public StaticOptionsFactory(String optionString) {
		if (optionString != null) {
			String varr[] = optionString.split("\\s*[,;]+\\s*");
			for (String v : varr) {
				String nvp[] = v.split("\\s*:\\s*");
				if (nvp.length>1) {
					addOption(nvp[1], nvp[0]);
				} else {
					addOption(nvp[0], nvp[0]);
				}
			}
		}
	}

	/**
	 * Creates an option list from an existing array of options.  The option values can be in the form
	 * ["value1:label1","value2:label2",...]
	 * @param optionString
	 */
	public StaticOptionsFactory(String optionArray[]) {
		if (optionArray != null) {
			for (String v : optionArray) {
				String nvp[] = v.split("\\s*:\\s*");
				if (nvp.length>1) {
					addOption(nvp[1], nvp[0]);
				} else {
					addOption(nvp[0], nvp[0]);
				}
			}
		}
	}
	

	/**
	 * Returns the static options list, key is ignored
	 */
	@Override
	public List<NamedValue> getOptions(String key) {
		return options;
	}

	/**
	 * adds an option to the list
	 * 
	 * @param name
	 * @param value
	 */
	public void addOption(String name, String value) {
		options.add(new NamedValue(name, value));
	}
	
	/**
	 * adds a named value
	 * 
	 * @param name
	 * @param value
	 */
	public void addOption(NamedValue nv) {
		options.add(nv);
	}

	@Override
	public Iterator<NamedValue> iterator() {
		return options.iterator();
	}
}
