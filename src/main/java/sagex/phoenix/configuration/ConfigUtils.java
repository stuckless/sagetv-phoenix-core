package sagex.phoenix.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;
import sagex.phoenix.util.NamedValue;

public class ConfigUtils {
	/**
	 * Given an object source, try to convert to a list of {@link NamedValue} objects, that
	 * can be used by the Configuration Options (ie, {@link IOptionFactory}.
	 * 
	 * @param source
	 * @return list of options, or null, if the source is null;
	 */
	public static List<NamedValue> getOptions(Object source) {
		if (source==null) return null;

		List<NamedValue> opts = new ArrayList<NamedValue>();
		if (source instanceof Collection) {
			for (Object o: (Collection)source) {
				addOption(opts, o);
			}
		} else if (source.getClass().isArray()) {
			for (Object o: (Object[])source) {
				addOption(opts, o);
			}
		} else {
			addOption(opts, source.toString());
		}
		return opts;
	}

	private static void addOption(List<NamedValue> opts, Object o) {
		if (o==null) return;
		
		if (o instanceof NamedValue) {
			opts.add((NamedValue)o);
		} else if (o instanceof Map.Entry) {
			Map.Entry me = (Entry) o;
			opts.add(new NamedValue(String.valueOf(me.getKey()), String.valueOf(me.getValue())));
		} else if (o instanceof HasName) {
			String value = ((HasName)o).getName();
			String name = (o instanceof HasLabel)?((HasLabel)o).getLabel():value;
			opts.add(new NamedValue(name, value));
		} else {
			opts.add(new NamedValue(o.toString(), o.toString()));
		}
	}
}
