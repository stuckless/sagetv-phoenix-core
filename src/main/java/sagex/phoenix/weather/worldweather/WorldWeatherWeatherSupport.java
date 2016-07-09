package sagex.phoenix.weather.worldweather;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.WeatherConfiguration;

/**
 * @author jusjoken
 */
public class WorldWeatherWeatherSupport implements IWeatherSupport2 {

    // NOTE: World weather will no longer work, since they no longer provide a free key

    private String freeKey = "p6y5b5g5haupghedu7p42nbb"; // jusjoken - 1000 hits per day
    private String premiumKey = "4072fc6aa84641468cb145931160907"; // trial ends Sept 7, 2016
    private boolean isPremium=false;
    private Logger log = Logger.getLogger(this.getClass());

    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 45;

    private String error;

    private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

    private String locationName;

    private ICurrentForecast current;
    private List<ILongRangeForecast> forecast;

    public WorldWeatherWeatherSupport() {
    }

    @Override
    public String getSourceName() {
        return "World Weather Online";
    }

    @Override
    public boolean update() {
        if (shouldUpdate()) {

            String thislocation = config.getLocation();

            String rssUrl;
            if (!isPremium) {
                rssUrl = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=" + thislocation
                        + "&format=xml&num_of_days=5&includeLocation=yes&extra=localObsTime&key=" + freeKey;
            } else {
                rssUrl = "http://api.worldweatheronline.com/premium/v1/weather.ashx?q=" + thislocation
                        + "&format=xml&num_of_days=5&includeLocation=yes&extra=localObsTime&key=" + premiumKey;
            }
            log.info("Getting WorldWeather Weather for " + rssUrl);
            try {
                WorldWeatherWeatherHandler handler = new WorldWeatherWeatherHandler(getUnits());
                XmlUtil.parseXml(rssUrl, handler);
                lastUpdated = new Date(System.currentTimeMillis());

                current = handler.getCurrentWeather();
                forecast = handler.getLongRangeForecast();

                locationName = handler.getCity();
                recordedDate = handler.getRecordedDate();

                return true;
            } catch (Exception e) {
                error = "WorldWeather weather update failed";
                log.error("Failed to update weather for " + rssUrl, e);
            }
        }
        return false;
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

    @Override
    public boolean setLocation(String location) {
        config.setLocation(location);
        // reset the last updated to force an update on the next update call
        lastUpdated = null;
        return true;
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
        String tLoc = locationName;
        if (tLoc == null)
            return "";
        if (tLoc.contains("(")) {
            tLoc = tLoc.substring(0, tLoc.indexOf("("));
        }
        return tLoc.trim();
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
        return current;
    }

    @Override
    public List<ILongRangeForecast> getForecasts() {
        return forecast;
    }

    @Override
    public int getForecastDays() {
        if (forecast == null)
            return 0;
        return forecast.size();
    }

    @Override
    public boolean isConfigured() {
        return !StringUtils.isEmpty(getLocation());
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

}
