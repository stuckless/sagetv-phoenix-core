package sagex.phoenix.configuration;

import sage.SageTVPlugin;

/**
 * Represents a Configuration Type
 *
 * @author sean
 */
public enum ConfigType {
    BOOL(SageTVPlugin.CONFIG_BOOL, Config.Type.BOOL, "boolean"), NUMBER(SageTVPlugin.CONFIG_INTEGER, Config.Type.NUMBER, "int",
            "integer", "float", "double", "long"), TEXT(SageTVPlugin.CONFIG_TEXT, Config.Type.TEXT, "string"), CHOICE(
            SageTVPlugin.CONFIG_CHOICE, Config.Type.CHOICE, "select"), MULTICHOICE(SageTVPlugin.CONFIG_MULTICHOICE,
            Config.Type.MULTICHOICE, "multiselect"), FILE(SageTVPlugin.CONFIG_FILE, Config.Type.FILE), DIRECTORY(
            SageTVPlugin.CONFIG_DIRECTORY, Config.Type.DIRECTORY, "directory"), BUTTON(SageTVPlugin.CONFIG_BUTTON,
            Config.Type.BUTTON), PASSWORD(SageTVPlugin.CONFIG_PASSWORD, Config.Type.PASSWORD);

    private int sageId;
    private String[] alternates;

    ConfigType(int sageId, String... alternates) {
        this.sageId = sageId;
        this.alternates = alternates;
    }

    public int sageId() {
        return sageId;
    }

    public String[] alternates() {
        return alternates;
    }

    /**
     * Convert a Configuration Type Number to it's Configuration Type. If it
     * can't be converted then 'text' is used.
     *
     * @param type
     * @return
     */
    public ConfigType toConfigType(int type) {
        for (ConfigType ct : values()) {
            if (type == ct.sageId) {
                return ct;
            }
        }

        return TEXT;
    }

    /**
     * Converts a Configuration Type name to its Configuration Type. If it can't
     * be converted the 'text' is used.
     *
     * @param type
     * @return
     */
    public static ConfigType toConfigType(String type) {
        if (type == null)
            return TEXT;
        for (ConfigType ct : values()) {
            if (ct.name().equalsIgnoreCase(type))
                return ct;
            if (ct.alternates() != null) {
                for (String a : ct.alternates()) {
                    if (type.equalsIgnoreCase(a))
                        return ct;
                }
            }
        }
        return TEXT;
    }

    /**
     * Returns the common string Name for the configuration type.
     *
     * @param type
     * @return
     */
    public static String toCommonName(ConfigType type) {
        return type.alternates[0];
    }

    /**
     * Fixes the type to ensure that it resolves to the common name, or
     * "unknown" if the type is not known.
     *
     * @param type
     * @return
     */
    public static String toCommonName(String type) {
        return toCommonName(toConfigType(type));
    }
}
