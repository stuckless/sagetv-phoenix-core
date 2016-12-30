package sagex.phoenix.util;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

public class StringUtils {
    public static String removeHtml(String html) {
        if (html == null)
            return null;
        return html.replaceAll("<[^>]+>", "");
    }

    public static String unquote(String str) {
        if (str == null)
            return null;
        return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
    }

    public static String mapToString(Map map) {
        if (map == null)
            return "null";
        if (map.size() == 0)
            return "empty";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Object o : map.entrySet()) {
            Map.Entry me = (Entry) o;
            sb.append(me.getKey()).append(": ").append(me.getValue()).append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String zeroPad(String encodeString, int padding) {
        try {
            int v = Integer.parseInt(encodeString);
            String pad = String.valueOf(v);
            while (pad.length() < padding) {
                pad = "0" + pad;
            }
            return pad;
        } catch (Exception e) {
            return encodeString;
        }
    }

    /**
     * IF a title has a path in it, then the path is removed.
     *
     * @param title
     * @return
     */
    public static String fixTitle(String title) {
        if (title != null) {
            if (title.indexOf(File.separator) != -1) {
                // removes the folder from a title that has a folder in it
                title = org.apache.commons.lang.StringUtils.substringAfterLast(title, File.separator);
            }
        }
        return title;
    }

    /**
     * Joins a list of objects together using the separator
     *
     * @param sep
     * @param parts
     * @return
     */
    public static String join(String sep, Object... parts) {
        if (parts == null)
            return null;
        int s = parts.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s; i++) {
            if (parts[i] != null) {
                sb.append(parts[i]);
                if (i < s - 1) {
                    sb.append(sep);
                }
            }
        }
        return sb.toString();
    }

    public static String firstNonEmpty(String... parts) {
        for (String s : parts) {
            if (!org.apache.commons.lang.StringUtils.isEmpty(s))
                return s;
        }
        return null;
    }

    public static boolean isCamelCase(String s) {
        if (s == null)
            return false;
        if (s.contains(" "))
            return false; // words with spaces are not camel
        return countUpper(s) > 1;
    }

    public static int countUpper(String s) {
        if (s == null)
            return 0;
        int upper = 0;
        for (char ch : s.toCharArray()) {
            if (Character.isUpperCase(ch))
                upper++;
        }
        return upper;
    }

    /**
     * Null safe comparison of Comparables. {@code null} is assumed to be less
     * than a non-{@code null} value.
     *
     * @param c1 the first comparable, may be null
     * @param c2 the second comparable, may be null
     * @return a negative value if c1 < c2, zero if c1 = c2 and a positive value
     * if c1 > c2
     * @since 2.6
     */
    public static int compare(Comparable c1, Comparable c2) {
        return compare(c1, c2, false);
    }

    /**
     * Null safe comparison of Comparables.
     *
     * @param c1          the first comparable, may be null
     * @param c2          the second comparable, may be null
     * @param nullGreater if true <code>null</code> is considered greater than a Non-
     *                    <code>null</code> value or if false <code>null</code> is
     *                    considered less than a Non-<code>null</code> value
     * @return a negative value if c1 < c2, zero if c1 = c2 and a positive value
     * if c1 > c2
     * @see java.util.Comparator#compare(Object, Object)
     * @since 2.6
     */
    public static int compare(Comparable c1, Comparable c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return (nullGreater ? 1 : -1);
        } else if (c2 == null) {
            return (nullGreater ? -1 : 1);
        }
        return c1.compareTo(c2);
    }

    public static boolean isAnyEmpty(String... values) {
        if (values == null)
            return true;
        for (String v : values) {
            if (org.apache.commons.lang.StringUtils.isEmpty(v))
                return true;
        }
        return false;
    }

    public static boolean hasAny(String... values) {
        if (values==null) return false;
        for (String v : values) {
            if (!org.apache.commons.lang.StringUtils.isEmpty(v))
                return true;
        }
        return false;
    }
}
