package sagex.phoenix.metadata.search;

import org.apache.log4j.Logger;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.MetadataConfiguration;

public class SearchUtil {
    public static final Logger log = Logger.getLogger(SearchUtil.class);

    /**
     * For the purposes of searching it will, keep only alpha numeric characters
     * and '&
     *
     * @param s
     * @return
     */
    public static String removeNonSearchCharacters(String s) {
        if (s == null)
            return null;
        String val = (s.replaceAll("[^'\"\\!\\+:,A-Za-z0-9&\\-'\\(\\)\\.]", " ")).replaceAll("[\\ ]+", " ").trim();
        val = specialHandleDots(val);
        return val;
    }

    /**
     * Attempts to handle dots in filename whereby if the dots are part of name,
     * like, C.S.I or S.H.I.E.L.D. then they dots remain.
     *
     * @param val
     * @return
     */
    public static String specialHandleDots(String val) {
        if (val == null)
            return null;

        boolean replaceDot = false;
        int len = val.length();
        StringBuilder sb = new StringBuilder(val);
        for (int i = 0; i < len; i++) {
            if (sb.charAt(i) == '.') {
                replaceDot = true;
                if (i + 2 < len
                        && (Character.isLetter(sb.charAt(i + 1)) && (Character.isWhitespace(sb.charAt(i + 2)) || sb.charAt(i + 2) == '.'))) {
                    // keep the dot because there is either another dot or space
                    // there
                    replaceDot = false;
                } else if (i - 2 >= 0
                        && (Character.isLetter(val.charAt(i - 1)) && (Character.isWhitespace(val.charAt(i - 2)) || val
                        .charAt(i - 2) == '.'))) {
                    // keep the dot because 2 chars back is another dot
                    replaceDot = false;
                }
                if (replaceDot) {
                    sb.setCharAt(i, ' ');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Utility method that can be used to clean the search criteria before
     * sending it to the seach function. eg, it removes any non alphanumeric
     * characters and replaces them with spaces. remove cd volumes, and some
     * common words like dvd, dvrip, etc.
     * <p/>
     * You can control the cd and word tokens following keys in the
     * configuration manager.
     * <p/>
     * <pre>
     * org.jdna.media.metadata.VideoMetaDataUtils.cleanSearchCriteria.cdTokens
     * org.jdna.media.metadata.VideoMetaDataUtils.cleanSearchCriteria.wordTokens
     * </pre>
     */
    public static String cleanSearchCriteria(String s, boolean removeYear) {
        String wordTokens[] = GroupProxy.get(MetadataConfiguration.class).getWordsToClean().split(",");

        log.debug("Cleaning Search Criteria: " + s);
        String parts[] = removeNonSearchCharacters(s).split(" ");
        StringBuffer sb = new StringBuffer();
        int l = parts.length;
        for (int i = 0; i < l; i++) {
            String ss = parts[i].trim();

            // check all tokens for the word tokens to ignore
            boolean found = false;
            for (String t : wordTokens) {
                if (ss.equalsIgnoreCase(t)) {
                    log.debug("Word Token Matched: " + t);
                    found = true;
                    break;
                }
            }
            // stop processing if you find a word token
            if (found)
                break;

            sb.append(ss).append(" ");
        }

        String v = sb.toString().trim();
        if (v.length() == 0) {
            log.warn("After cleaning title we have no title... reseting title to the original: " + s);
            v = s;
        }

        if (removeYear) {
            v = v.replaceAll("19[0-9]+", "");
            v = v.replaceAll("20[0-9]+", "");
        }

        log.debug("Cleaned Title: " + v);
        return v;
    }
}
