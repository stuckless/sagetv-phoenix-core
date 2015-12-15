package sagex.phoenix.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {
    private static String dateParsers[] = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
            // 2012-07-16 01:06 PM - added by jusjoken
            "yyyy-MM-dd hh:mm a", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss", "MM/dd/yyyy", "dd MMM yyyy",
            "yyyy MMM dd", "yyyy MMM", "yyyy",
            // Fri, 20 Aug 2010 00:00:00 -0700
            "EEE, d MMM yyyy HH:mm:ss Z", "EEE, d MMM yyyy HH:mm:ss a Z", "EEE, d MMM yyyy HH:mm a Z",};

    private static Pattern runtimeParser = Pattern.compile("([0-9]+)\\s+min", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
            | Pattern.DOTALL);
    private static Pattern durationParser = Pattern.compile("([0-9]+):([0-9]+):([0-9]+)", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE | Pattern.DOTALL);

    public static Date parseDate(String in) {
        if (StringUtils.isEmpty(in))
            return null;
        in = in.trim();
        try {
            return org.apache.commons.lang.time.DateUtils.parseDate(in, dateParsers);
        } catch (ParseException e) {
            String newin = scrapeDate(in);
            if (newin == null || newin.equals(in)) {
                Loggers.LOG.warn("Failed to parse date string: " + in, e);
            } else {
                Loggers.LOG.warn("Failed to parse date string: " + in + "; Attempting again using: " + newin);
                return parseDate(newin);
            }
        }
        return null;
    }

    /**
     * Sometimes a date might contain other information, such as country, so
     * this will extract the date only.
     *
     * @param in
     * @return
     */
    public static String scrapeDate(String in) {
        if (StringUtils.isEmpty(in))
            return null;
        Pattern p = Pattern.compile("([^\\(]+)");
        Matcher m = p.matcher(in);
        if (m.find())
            return m.group(1).trim();
        return null;
    }

    /**
     * Given a date string, return just a year. A year of 0 means it did not
     * parse a year.
     *
     * @param in
     * @return
     */
    public static int parseYear(String in) {
        if (StringUtils.isEmpty(in))
            return 0;

        Date d = parseDate(in);
        if (d != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal.get(Calendar.YEAR);
        }
        Loggers.LOG.warn("Failed to parse a year for: " + in, new Exception());
        return 0;
    }

    /**
     * Given a date string and a format, return just a year. A year of 0 means
     * it did not parse a year.
     *
     * @param in     data string
     * @param format {@link SimpleDateFormat} format string for the complete date
     * @return
     */
    public static int parseYear(String in, String format) {
        if (StringUtils.isEmpty(in))
            return 0;
        if (StringUtils.isEmpty(format))
            return 0;

        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d;
        try {
            d = f.parse(in);
        } catch (ParseException e) {
            Loggers.LOG.warn("Failed to parse date: " + in + " using format: " + format, e);
            return 0;
        }

        if (d != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal.get(Calendar.YEAR);
        }

        return 0;
    }

    /**
     * Formats a date in teh format yyyy-MM-dd
     *
     * @param in
     * @return formatted date
     */
    public static String formatDate(Date in) {
        if (in == null) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(in);
    }

    /**
     * Parses the running time in the format "108 min" into a millisecond value.
     * If the input string is a parseable long value, then that will be assumed
     * to be the millisecond value.
     *
     * @param in
     * @return
     */
    public static long parseRuntimeInMinutes(String in) {
        if (in == null)
            return 0;
        long l = NumberUtils.toLong(in, 0);
        if (!(l > 0)) {
            Matcher m = runtimeParser.matcher(in);
            if (m.find()) {
                l = NumberUtils.toLong(m.group(1), 0);
                if (l > 0)
                    l = l * 60 * 1000;
            }
        }
        return l;
    }

    /**
     * Parses a duration to millis in the format of hh:mm:ss
     *
     * @param dur
     * @return
     */
    public static long parseDuration(String in) {
        if (in == null)
            return 0;
        long l = NumberUtils.toLong(in, 0);
        if (l == 0) {
            l = (long) NumberUtils.toFloat(in);
        }
        if (!(l > 0)) {
            Matcher m = durationParser.matcher(in);
            if (m.find()) {
                long hh = NumberUtils.toLong(m.group(1), 0) * 60 * 60 * 1000;
                long mm = NumberUtils.toLong(m.group(2), 0) * 60 * 1000;
                long ss = NumberUtils.toLong(m.group(3), 0) * 1000;
                l = hh + mm + ss;
            }
        }
        return l;
    }

    public static String formatDateTime(long l) {
        if (l <= 0) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(new Date(l));
    }

    public static String formatTimeInMinutes(long l) {
        if (l <= 0)
            return null;
        long mins = l / 1000 / 60;
        if (l > 0) {
            return String.valueOf(mins) + " min";
        }
        return "";
    }
}
