package sagex.phoenix.vfs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataUtil;

public class ConfigList {
    public static String BOOLEAN_LIST = "true:Yes,no:No";

    public static ConfigurableOption BooleanOption(String optName, String label, boolean defValue) {
        return new ConfigurableOption(optName, label, String.valueOf(defValue), DataType.bool, true, ListSelection.single,
                BOOLEAN_LIST);
    }

    /**
     * Builds a list for a {@link ConfigurableOption}. Input must be
     * "key:Label,key1:Label1,..."
     *
     * @param list
     * @return
     */
    public static String staticList(String... list) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.length - 2; i += 2) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list[i]).append(":").append(list[i + 1]);
        }
        return sb.toString();
    }

    public static String mediaTypeList() {
        List<String> mt = new ArrayList<String>();
        for (MediaType m : MediaType.values()) {
            mt.add(m.name());
            mt.add(m.dirName());
        }
        return staticList(mt.toArray(new String[]{}));
    }

    public static String metadataList() {
        List<String> mt = new ArrayList<String>();

        ArrayList<String> list = new ArrayList<String>(MetadataUtil.getPropertyKeys().keySet());
        Collections.sort(list);
        for (String s : list) {
            if (s.startsWith("Format"))
                continue;
            mt.add(s);
            mt.add(s);
        }
        return staticList(mt.toArray(new String[]{}));
    }

    public static String fileDateList() {
        List<String> mt = new ArrayList<String>();
        mt.add("YEAR");
        mt.add("MONTH");
        mt.add("DAY");
        return staticList(mt.toArray(new String[]{}));

    }
}
