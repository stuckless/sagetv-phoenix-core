package sagex.phoenix.weather.yahoo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.weather.*;

public class YahooWeatherSupport2 implements IWeatherSupport2 {
    //2021-09-18 - jusjoken
    //deprecated - weather is no longer available from within phoenix - now using GWeather jar externally
    //this class is only left in place as an example if added back in the future - but this is no longer called
    private Logger log = Logger.getLogger(this.getClass());

    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 180;

    private String error;

    private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

    private String locationName;
    private Double latitude;
    private Double longitude;

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
            error = "Please configure your location";
            return false;
        }

        if (shouldUpdate()) {
             String units = null;
             if (getUnits() == Units.Metric) {
             units = "c";
             } else {
             units = "f";
             }

            String location = config.getLocation();
            try {
                log.info("Getting Yahoo Weather for location:" + location + " with units:" + units);

                YahooWeatherJsonHandler handler = new YahooWeatherJsonHandler();
                handler.parse(location,units);
                lastUpdated = new Date(System.currentTimeMillis());

                currentForecast = handler.getCurrent();
                longRangeForecast = handler.getDays();

                locationName = handler.getCity() + "," + handler.getRegion();
                latitude = handler.getLat();
                longitude = handler.getLong();
                recordedDate = handler.getRecordedDate();

                return true;
            } catch (Exception e) {
                error = "Yahoo weather update failed";
                log.error("Failed to update weather for location:" + location + " with units:" + units, e);
            }
        }

        return false;
    }

    @Override
    public boolean setLocation(String postalOrZip) {
        error = null;
        lastUpdated = null;
        boolean hasLocation = false;
        if (!StringUtils.isEmpty(postalOrZip)){
            config.setLocation(postalOrZip);
            hasLocation = true;
        }else{
            error = "Location was not set";
        }
        return hasLocation;
    }

    /**
     *
     * @param postalOrZip
     * @return woeid
     * @throws Exception
     * @deprecated Yahoo no longer uses the woeid - use Postal/Zip or City,ST instead
     */
    @Deprecated
    public String convertZipToWoeid(String postalOrZip) throws Exception {
        return postalOrZip;
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
        return !StringUtils.isEmpty(config.getLocation());
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

    /**
     * Returns the Larger of the configured Updated Interval vs the Weather's TTL
     */
    private int getTTLInSeconds() {
        int ttl1=ttl * 60;
        int ttl2=config.getUpdateInterval();
        return Math.max(ttl1,ttl2);
    }

    private boolean shouldUpdate() {
        if (lastUpdated == null)
            return true;
        long later = lastUpdated.getTime() + (getTTLInSeconds() * 1000);
        if (System.currentTimeMillis() > later)
            return true;
        log.debug("shouldUpdate: Not time to perform an update. Last update at '" + lastUpdated + "'");
        return false;
    }
}
