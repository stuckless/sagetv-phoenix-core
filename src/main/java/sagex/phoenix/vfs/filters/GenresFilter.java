package sagex.phoenix.vfs.filters;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class GenresFilter extends Filter {
	public static final String OPT_USE_REGEX = "use-regex-matching";

	private boolean useRegex = false;
	private Pattern regex = null;
	private String value = null;

	public GenresFilter() {
		super();
		addOption(new ConfigurableOption(OPT_VALUE, "Genre", null, DataType.string, true, ListSelection.single, (String) null));
		addOption(new ConfigurableOption(OPT_USE_REGEX, "Use Regex Matching", "false", DataType.bool, true, ListSelection.single,
				"true:Yes,no:No"));
	}

	public boolean canAccept(IMediaResource res) {
		String genre = value;
		if (genre == null)
			return false;

		if (res instanceof IMediaFile) {
			List<String> g1 = ((IMediaFile) res).getMetadata().getGenres();
			if (g1 == null || g1.size() == 0)
				return false;
			for (String s : g1) {
				if (useRegex && regex != null) {
					Matcher m = regex.matcher(s);
					if (m.find()) {
						return true;
					}
				} else {
					if (genre.equalsIgnoreCase(s)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Map<String, String> getOptionList(String id) {
		// TODO: Return genre map
		return super.getOptionList(id);
	}

	@Override
	protected void onUpdate() {
		value = getOption(OPT_VALUE).getString(null);
		useRegex = getOption(OPT_USE_REGEX).getBoolean(false);
		if (useRegex) {
			regex = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
		}
	}
}
