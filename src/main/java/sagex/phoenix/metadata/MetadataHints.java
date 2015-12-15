package sagex.phoenix.metadata;

public class MetadataHints {
    /**
     * Hint to specify that you want to import TV shows into the Sage Recordings
     * * {@value}
     */
    public static final String IMPORT_TV_AS_RECORDING = "import_tv_as_recording";

    /**
     * Hint to indicate that you want to scan ONLY files that currently do not
     * have metadata * {@value}
     */
    public static final String SCAN_MISSING_METADATA_ONLY = "scan_missing_metadata";

    /**
     * Hint to indicate that you want to scan subfolders * {@value}
     */
    public static final String SCAN_SUBFOLDERS = "scan_subfolders";

    /**
     * Hint to specify that this query is for a sage recording that SageTV is
     * currently aware of. * * {@value}
     */
    public static final String KNOWN_RECORDING = "known_recording";

    /**
     * Hint to specify that this query originated from an automatic scan from
     * the automatic plugin. * {@value}
     */
    public static final String AUTOMATIC = "auto";

    /**
     * Hint to specify that you want to update metadata
     */
    public static final String UPDATE_METADATA = "update_metadata";

    /**
     * Hint to specify that you want to update fanart
     */
    public static final String UPDATE_FANART = "update_fanart";

    /**
     * Hint to specify that you want to refresh existing data
     */
    public static final String REFRESH = "refresh";

    /**
     * Hint to specify that you want to preserve the original recording metadata
     * for a media item ie, only update the custom fields, and not the orginal
     * metadata
     */
    public static final String PRESERVE_ORIGINAL_METADATA = "preserve_original";
}
