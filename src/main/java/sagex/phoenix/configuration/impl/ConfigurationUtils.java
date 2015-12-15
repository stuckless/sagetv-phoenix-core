package sagex.phoenix.configuration.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationUtils {
    public static List<String> filterKeys(String startsWith, Iterator<String> keys) {
        List<String> props = new ArrayList<String>();

        if (keys != null && startsWith != null) {
            for (; keys.hasNext(); ) {
                String s = keys.next();
                if (s.startsWith(startsWith)) {
                    props.add(s);
                }
            }
        }

        return props;
    }

    public static List<String> toList(Iterator<String> iterator) {
        ArrayList<String> list = new ArrayList<String>();
        if (iterator != null) {
            while (iterator.hasNext())
                list.add(iterator.next());
        }
        return list;
    }

    public static List<String> filterKeys(String startsWith, List<String> keys) {
        List<String> props = new ArrayList<String>();

        if (keys != null && startsWith != null) {
            for (String s : keys) {
                if (s.startsWith(startsWith)) {
                    props.add(s);
                }
            }
        }

        return props;
    }

    public static List<String> filterContainsKeys(String contains, List<String> keys) {
        List<String> props = new ArrayList<String>();

        if (keys != null && contains != null) {
            for (String s : keys) {
                if (s.contains(contains)) {
                    props.add(s);
                }
            }
        }

        return props;
    }
}
