package sagex.phoenix.weather.yahoo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.json.JSON;
import sagex.phoenix.util.url.UrlFactory;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.WeatherConfiguration;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

public class YahooWeatherSupport2 implements IWeatherSupport2 {
    private Logger log = Logger.getLogger(this.getClass());

    private static String yqlWeatherQuery = "select * from weather.forecast where woeid=%s and u='%s'";

    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 180;

    private String error;

    private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

    private String locationName;

    private ICurrentForecast currentForecast;
    private List<ILongRangeForecast> longRangeForecast;

    public YahooWeatherSupport2() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getSourceName() {
        return "Yahoo! Weather";
    }

    @Override
    public boolean update() {
        error = null;

        if (!isConfigured()) {
            error = "Please configure the Yahoo Weather WOEID for your location";
            return false;
        }

        if (shouldUpdate()) {
             String units = null;
             if (getUnits() == Units.Metric) {
             units = "c";
             } else {
             units = "f";
             }

            String woeid = config.getYahooWOEID();
            String query = String.format(yqlWeatherQuery, woeid, units);
            try {
                String rssUrl = "https://query.yahooapis.com/v1/public/yql?format=json&q=" + URLEncoder.encode(query,"UTF-8");
                log.info("Getting Yahoo Weather for " + rssUrl);

                YahooWeatherJsonHandler handler = new YahooWeatherJsonHandler();
                handler.parse(rssUrl);
                lastUpdated = new Date(System.currentTimeMillis());
                ttl = handler.getTtl();

                currentForecast = handler.getCurrent();
                longRangeForecast = handler.getDays();

                locationName = handler.getCity();
                recordedDate = handler.getRecordedDate();

                return true;
            } catch (Exception e) {
                error = "Yahoo weather update failed";
                log.error("Failed to update weather for " + query, e);
            }
        }

        return false;
    }

    @Override
    public boolean setLocation(String postalOrZip) {
        error = null;
        boolean configured = false;
        lastUpdated = null;
        // convert zip to woeid
        try {
            config.setLocation(postalOrZip);

            String woeid = convertZipToWoeid(postalOrZip);

            if (woeid != null) {
                config.setYahooWOEID(woeid);
                configured = true;
                lastUpdated = new Date();
            }
        } catch (Exception e) {
            log.warn("Failed to convert " + postalOrZip + " to woeid", e);
            error = "Failed to convert the Location into a valid Yahoo WOEID";
            configured = false;
        }
        return configured;
    }

    public String convertZipToWoeid(String postalOrZip) throws Exception {
        String query = String.format("select woeid from geo.places where text='%s' limit 1", postalOrZip);

        String url = "https://query.yahooapis.com/v1/public/yql?format=json&q="+ URLEncoder.encode(query,"UTF-8");
        String data = UrlUtil.getContentAsString(UrlFactory.newUrl(url));
        String woeid = JSON.getString("query.results.place.woeid", new JSONObject(data));
        return woeid;
    }

    @Override
    public String getLocation() {
        return config.getLocation();
    }

    @Override
    public void removeLocation() {
        locationName = "";
        config.setLocation(null);
    }

    @Override
    public String getLocationName() {
        return locationName;
    }

    @Override
    public void setUnits(Units u) {
        if (u == null || u == Units.Metric) {
            config.setUnits("m");
        } else {
            config.setUnits("s");
        }
        lastUpdated = null;
    }

    @Override
    public Units getUnits() {
        String u = config.getUnits();
        if (StringUtils.isEmpty(u) || u.toLowerCase().startsWith("m")) {
            return Units.Metric;
        } else {
            return Units.Standard;
        }
    }

    @Override
    public ICurrentForecast getCurrentWeather() {
        return currentForecast;
    }

    @Override
    public List<ILongRangeForecast> getForecasts() {
        return longRangeForecast;
    }

    @Override
    public int getForecastDays() {
        if (longRangeForecast != null) {
            return longRangeForecast.size();
        }
        return 0;
    }

    @Override
    public boolean isConfigured() {
        return !StringUtils.isEmpty(config.getYahooWOEID());
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Date getRecordedDate() {
        return recordedDate;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public String getError() {
        return error;
    }

    private boolean shouldUpdate() {
        if (lastUpdated == null)
            return true;
        long later = lastUpdated.getTime() + (ttl * 60 * 1000);
        if (System.currentTimeMillis() > later)
            return true;
        log.debug("shouldUpdate: Not time to perform an update. Last update at '" + lastUpdated + "'");
        return false;
    }
}
