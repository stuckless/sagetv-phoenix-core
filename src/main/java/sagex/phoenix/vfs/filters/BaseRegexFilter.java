package sagex.phoenix.vfs.filters;

import java.util.regex.Pattern;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.util.ConfigList;

/**
 * Base filter that accepts a regular expression as the filter value. A base
 * class can simply call match(String in) to determine a match.
 *
 * @author seans
 */
public abstract class BaseRegexFilter extends Filter {
    /**
     * regular expression variable name * {@value}
     */
    public static final String OPT_FILEEXTREGEX = "value";

    /**
     * use regular expressions for matching * * {@value}
     */
    public static final String OPT_USE_REGEX = "use-regex-matching";

    protected Pattern regex = null;
    protected boolean useRegex = true;

    public BaseRegexFilter(String regexLabel) {
        super();
        addOption(new ConfigurableOption(OPT_FILEEXTREGEX, regexLabel, null, DataType.string));
        addOption(ConfigList.BooleanOption(OPT_USE_REGEX, "Use Regex Matching", true));
    }

    public boolean match(String in) {
        if (!useRegex) {
            if (in == null)
                return false;
            return in.equals(getOption(OPT_VALUE).getString(null));
        } else {
            return regex.matcher(in).find();
        }
    }

    @Override
    protected void onUpdate() {
        regex = Pattern.compile((String) getOption(OPT_FILEEXTREGEX).value().get(), Pattern.CASE_INSENSITIVE);
        useRegex = getOption(OPT_USE_REGEX).getBoolean(true);
    }
}
