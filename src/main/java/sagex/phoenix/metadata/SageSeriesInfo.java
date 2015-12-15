package sagex.phoenix.metadata;

import org.apache.commons.lang.math.NumberUtils;
import sagex.api.SeriesInfoAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps the Sage SeriesInfo to provide a Read-Only {@link ISeriesInfo}
 * implementation
 *
 * @author seans
 */
public class SageSeriesInfo implements ISeriesInfo {
    public static final String CUSTOM_UserRating = "UserRating";
    public static final String CUSTOM_ContentRating = "ContentRating";
    public static final String CUSTOM_Runtime = "Runtime";
    public static final String CUSTOM_Zap2It = "Zap2ItID";

    private Object seriesInfo = null;
    private List<String> genres = null;
    private List<ICastMember> cast = null;

    public SageSeriesInfo(Object seriesInfo) {
        this.seriesInfo = seriesInfo;
    }

    /*
     * returns the object being wrapped
     */
    public Object getObject() {
        return seriesInfo;
    }

    @Override
    public String getAirDOW() {
        return SeriesInfoAPI.GetSeriesDayOfWeek(seriesInfo);
    }

    @Override
    public String getAirHrMin() {
        return SeriesInfoAPI.GetSeriesHourAndMinuteTimeslot(seriesInfo);
    }

    @Override
    public List<ICastMember> getCast() {
        if (cast == null) {
            cast = new ArrayList<ICastMember>();
            int chars = SeriesInfoAPI.GetNumberOfCharactersInSeries(seriesInfo);
            if (chars > 0) {
                for (int i = 0; i < chars; i++) {
                    CastMember cm = new CastMember();
                    cm.setName(SeriesInfoAPI.GetSeriesActor(seriesInfo, i));
                    cm.setRole(SeriesInfoAPI.GetSeriesCharacter(seriesInfo, i));
                    cast.add(cm);
                }
            }
        }
        return cast;
    }

    @Override
    public String getContentRating() {
        return SeriesInfoAPI.GetSeriesInfoProperty(seriesInfo, CUSTOM_ContentRating);
    }

    @Override
    public String getDescription() {
        return SeriesInfoAPI.GetSeriesDescription(seriesInfo);
    }

    @Override
    public String getFinaleDate() {
        return SeriesInfoAPI.GetSeriesFinaleDate(seriesInfo);
    }

    @Override
    public List<String> getGenres() {
        if (genres == null) {
            genres = new ArrayList<String>();
            String cat = SeriesInfoAPI.GetSeriesCategory(seriesInfo);
            if (cat != null) {
                genres.add(cat);
            }
            cat = SeriesInfoAPI.GetSeriesSubCategory(seriesInfo);
            if (cat != null) {
                genres.add(cat);
            }
        }
        return genres;
    }

    @Override
    public String getHistory() {
        return SeriesInfoAPI.GetSeriesHistory(seriesInfo);
    }

    public Object getImage() {
        return SeriesInfoAPI.GetSeriesImage(seriesInfo);
    }

    @Override
    public String getNetwork() {
        return SeriesInfoAPI.GetSeriesNetwork(seriesInfo);
    }

    @Override
    public String getPremiereDate() {
        return SeriesInfoAPI.GetSeriesPremiereDate(seriesInfo);
    }

    @Override
    public String getSeriesInfoID() {
        return SeriesInfoAPI.GetSeriesID(seriesInfo);
    }

    @Override
    public String getTitle() {
        return SeriesInfoAPI.GetSeriesTitle(seriesInfo);
    }

    @Override
    public int getUserRating() {
        return NumberUtils.toInt(SeriesInfoAPI.GetSeriesInfoProperty(seriesInfo, CUSTOM_UserRating));
    }

    @Override
    public void setAirDOW(String airdow) {
    }

    @Override
    public void setAirHrMin(String airtime) {
    }

    @Override
    public void setContentRating(String rating) {
        SeriesInfoAPI.SetSeriesInfoProperty(seriesInfo, CUSTOM_ContentRating, rating);
    }

    @Override
    public void setDescription(String desc) {
    }

    @Override
    public void setFinaleDate(String date) {
    }

    @Override
    public void setHistory(String history) {
    }

    @Override
    public void setImage(String url) {
    }

    @Override
    public void setNetwork(String network) {
    }

    @Override
    public void setPremiereDate(String date) {
    }

    @Override
    public void setSeriesInfoID(String id) {
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setUserRating(int rating) {
        SeriesInfoAPI.SetSeriesInfoProperty(seriesInfo, CUSTOM_UserRating, String.valueOf(rating));
    }

    @Override
    public long getRuntime() {
        return NumberUtils.toLong(SeriesInfoAPI.GetSeriesInfoProperty(seriesInfo, CUSTOM_Runtime));
    }

    @Override
    public void setRuntime(long runtime) {
        SeriesInfoAPI.SetSeriesInfoProperty(seriesInfo, CUSTOM_Runtime, String.valueOf(runtime));
    }

    @Override
    public String getZap2ItID() {
        return SeriesInfoAPI.GetSeriesInfoProperty(seriesInfo, CUSTOM_Zap2It);
    }

    @Override
    public void setZap2ItID(String id) {
        SeriesInfoAPI.SetSeriesInfoProperty(seriesInfo, CUSTOM_Zap2It, id);
    }
}
