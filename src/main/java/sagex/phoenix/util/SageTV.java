package sagex.phoenix.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sagex.api.Configuration;

@Deprecated
public class SageTV {
    private static final Logger log = Logger.getLogger(SageTV.class);
    private static final Pattern sageVersionPattern = Pattern.compile("([0-9\\.]+)");

    /**
     * Use the following logic
     * <p/>
     * if GetUIContextNames returns 1 item, then use it if it returns more than
     * 1, then determine which one to use by
     * <p/>
     * Parse the current execution thread for the context name. Threads with a
     * contextname will look like this.... ActiveRender-001d098ac46c
     *
     * @return
     */
    public static String getCurrentContext() {
        return null;
    }

    public static String getSageVersion() {
        String ver = Configuration.GetServerProperty("version", "0.0.0");
        Matcher m = sageVersionPattern.matcher(ver);
        if (m.find()) {
            return m.group(1);
        } else {
            return ver;
        }
    }
}
