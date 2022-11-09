package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.log4j.Logger;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.proxy.MetadataProxy;

public class TVDB4SeriesParser {
    private static final Logger log = Logger.getLogger(TVDB4SeriesParser.class);
    private final TVDB4MetadataProvider provider;

    private String seriesId = null;

    public TVDB4SeriesParser(TVDB4MetadataProvider provider, String seriesId) {
        this.provider=provider;
        this.seriesId = seriesId;

        if (seriesId == null || seriesId.isEmpty()) {
            throw new RuntimeException("Can't get series info without a Series Id, and series id was null.");
        }
    }

    public ISeriesInfo getSeriesInfo() throws MetadataException {
        try {
            ISeriesInfo sinfo = MetadataProxy.newInstance(ISeriesInfo.class);

            TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
            if(jsonHandler.validConfig()){
                jsonHandler.GetSeries(seriesId, sinfo);
            }else{
                log.warn("getSeriesInfo: TVDB4 configuration is not valid.");
                return null;
            }
            return sinfo;
        } catch (Exception e) {
            throw new MetadataException("getSeriesInfo: Failed to get series for " + seriesId, e);
        }

    }

}
