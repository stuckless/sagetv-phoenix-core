package sagex.phoenix.metadata.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Loggers;

public class ScraperUtils {
    private static Pattern cdPartScraper = Pattern.compile("(.+)[ _\\\\.-]+(cd|dvd|part|disc)[ _\\\\.-]*([0-9a-d]+)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern numbericCDScraper = Pattern.compile("(cd|dvd|part|disc)[ _\\\\.-]*([0-9]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Takes a title, like, 'TheShowTitle' and creates 'The Show Title'
     *
     * @param title
     * @return
     */
    public static String uncompressTitle(String title) {
        if (title == null)
            return null;
        return title.replaceAll("([A-Z0-9])([^A-Z0-9])", " $1$2").trim().replaceAll("([^A-Z0-9])([A-Z0-9])", "$1 $2").trim().replaceAll("\\s+"," ");
    }

    /**
     * Adjusts the SearchQuery to account for a CD in the title.
     *
     * @param q
     */
    public static void adjustTitleWithCD(SearchQuery q) {
        if (!StringUtils.isEmpty(q.get(Field.RAW_TITLE))) {
            java.util.regex.Matcher m = cdPartScraper.matcher(q.get(Field.RAW_TITLE));
            if (m.find()) {
                String t1 = q.get(Field.RAW_TITLE);
                String t2 = m.group(1);

                // remove non alpha at the end of the line
                t2 = t2.replaceAll("[^a-zA-Z0-9\\p{L}]*$", "");
                q.set(Field.RAW_TITLE, t2);
                Loggers.LOG.debug("Adjusting title: " + t1 + " to: " + t2 + "; because it matches a multi-cd title");
            }
        }
    }

    /**
     * Parses a CD # from a filename
     *
     * @param file
     * @return
     */
    public static int parseCD(String file) {
        if (file == null)
            return -1;
        Matcher m = numbericCDScraper.matcher(file);
        if (m.find()) {
            return NumberUtils.toInt(m.group(2), -1);
        }
        return -1;
    }
}
