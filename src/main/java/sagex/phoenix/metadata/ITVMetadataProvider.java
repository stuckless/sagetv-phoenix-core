package sagex.phoenix.metadata;

/**
 * Extends the normal provider interface to allow for some specific TV
 * operations, such as getting series info
 *
 * @author seans
 */
public interface ITVMetadataProvider extends IMetadataProvider {
    /**
     * Given a Series ID that is known to this provider, then return the
     * SeriesID information
     *
     * @param seriesId
     * @return
     * @throws MetadataException
     */
    public ISeriesInfo getSeriesInfo(String seriesId) throws MetadataException;
}
