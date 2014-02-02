package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.HasOptions;

public class TitleSorter implements Comparator<IMediaResource>, Serializable, HasOptions {
	private static final long serialVersionUID = 1L;
	private List<ConfigurableOption> options = new ArrayList<ConfigurableOption>();
	boolean ignoreThe = false;
	boolean ignoreAll = false;

	public TitleSorter() {
		options.add(new ConfigurableOption("ignore-the", "Disregard 'the' when sorting", "false", DataType.bool, true,
				ListSelection.single, "true:Yes,no:No"));
		options.add(new ConfigurableOption("ignore-all", "Disregard 'a', 'an', and 'the' when sorting", "false", DataType.bool,
				true, ListSelection.single, "true:Yes,no:No"));
	}

	public int compare(IMediaResource o1, IMediaResource o2) {
		if (o1 == null || o2 == null) {
			return -1;
		}
		String t1 = o1.getTitle();
		String t2 = o2.getTitle();
		if (t1 == null || t2 == null) {
			Loggers.LOG.warn("Title should never be null", new Exception());
			return -1;
		}

		if (ignoreThe || ignoreAll) {
			return removeLeadingArticles(t1, ignoreAll).compareToIgnoreCase(removeLeadingArticles(t2, ignoreAll));
		} else {
			return t1.compareToIgnoreCase(t2);
		}
	}

	/**
	 * Takes a string and returns the same string with "The ", "An ", and "A "
	 * removed from the beginning of it.
	 * 
	 * @param in
	 *            input String.
	 * @param all
	 *            if false, just 'The ' is removed. Otherwise The, An, or A is
	 *            removed.
	 * @return
	 */
	private String removeLeadingArticles(final String in, boolean all) {
		String out = in;

		if (in.toLowerCase().startsWith("the ")) {
			return out.substring(4);
		}

		if (all) {
			if (in.toLowerCase().startsWith("a ")) {
				return out.substring(2);
			}

			if (in.toLowerCase().startsWith("an ")) {
				return out.substring(3);
			}
		}

		return out;
	}

	public List<ConfigurableOption> getOptions() {
		return options;
	}

	public void onUpdate(BaseConfigurable parent) {
		ignoreThe = parent.getOption("ignore-the").getBoolean(false);
		ignoreAll = parent.getOption("ignore-all").getBoolean(false);
	}

}