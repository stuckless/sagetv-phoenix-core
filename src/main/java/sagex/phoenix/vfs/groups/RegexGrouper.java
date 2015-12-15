package sagex.phoenix.vfs.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.HasOptions;

/**
 * Groups based on the parent's groupName and a regular expression. ie, you can
 * use this to group based on the first character of the title by passing a
 * regular expression of '.'.
 *
 * @author sean
 */
public class RegexGrouper extends TitleGrouper implements HasOptions {
    /**
     * {@value}
     */
    public static final String OPT_REGEX = "regex";

    private List<ConfigurableOption> options = new ArrayList<ConfigurableOption>();
    private Pattern pattern = null;

    private IGrouper grouper;

    public RegexGrouper(IGrouper grouper) {
        options.add(new ConfigurableOption(OPT_REGEX, "Regex", null, DataType.string));
        this.grouper = grouper;
    }

    @Override
    public List<ConfigurableOption> getOptions() {
        return options;
    }

    @Override
    public void onUpdate(BaseConfigurable parent) {
        String pat = parent.getOption(OPT_REGEX).getString(null);
        if (pat != null) {
            pattern = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = null;
        }
    }

    @Override
    public String getGroupName(IMediaResource res) {
        String grp = grouper.getGroupName(res);
        if (pattern != null && res instanceof IMediaFile) {
            if (grp != null) {
                Matcher m = pattern.matcher(grp);
                if (m.find()) {
                    grp = grp.substring(m.start(), m.end());
                }
            }
        }
        return grp;
    }
}
