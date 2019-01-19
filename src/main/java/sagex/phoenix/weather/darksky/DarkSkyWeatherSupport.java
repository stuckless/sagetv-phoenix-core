package sagex.phoenix.weather.darksky;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.yahoo.YahooWeatherJsonHandler;

public class DarkSkyWeatherSupport implements IWeatherSupport2 {
    private Logger log = Logger.getLogger(this.getClass());

    /* DarkSky Weather API
    * URL format
    * https://api.darksky.net/forecast/[APISecret]/[LAT],[LONG]
    */

    //TODO: this is temp only as the user will need to provide a key
    private String sAPI = "efab29b240e101a622bda5ada4232973";
    private String userKey;
    private final String KEY_NOT_FOUND = "KEY_NOT_FOUND";

    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 180;
    private boolean locationRetrieved = false;
    private boolean hasUserKey = false;
    private boolean disallowUpdates = false;
    private boolean testing = false;

    private String error;

    private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

    private String locationName;
    private Double latitude;
    private Double longitude;
    private String city, region, country;

    private ICurrentForecast currentForecast;
    private List<ILongRangeForecast> longRangeForecast;

    public DarkSkyWeatherSupport() {
        //check for user key from darksky.properties
        //if not found then do not allow more than 1 update
        loadUserKey();
    }


    @Override
    public String getSourceName() {
        return "Dark Sky";
    }

    @Override
    public boolean update() {
        error = null;

        if (!disallowUpdates){
            if (!isConfigured()) {
                error = "Please configure the Dark Sky Weather provider for your location";
                return false;
            }

            if (shouldUpdate()) {
                String units = null;
                if (getUnits() == Units.Metric) {
                    units = "ca";
                } else {
                    units = "us";
                }

                try {

                    String urlLocation = UrlUtil.encode(latitude + "," + longitude);
                    String urlUnits = "units=" + UrlUtil.encode(units);
                    String k = "";
                    k = new StringBuilder(sAPI).reverse().toString();
                    if (hasUserKey){
                        k = userKey;
                    }
                    String rssUrl = "https://api.darksky.net/forecast/" + UrlUtil.encode(k) + "/" + urlLocation + "?" + urlUnits;
                    log.info("Getting Dark Sky Weather for " + rssUrl);

                    DarkSkyWeatherJsonHandler handler = new DarkSkyWeatherJsonHandler();
                    handler.parse(rssUrl, city, region, country);

                    lastUpdated = new Date(System.currentTimeMillis());

                    currentForecast = handler.getCurrent();
                    longRangeForecast = handler.getDays();
                    recordedDate = handler.getRecordedDate();

                    //see if we are using a userkey - otherwise do not allow further updates
                    if (!hasUserKey && testing==false){
                        log.info("Dark Sky Weather: no user key provided in darksky.properties - update not processed");
                        disallowUpdates = true;
                    }
                    return true;
                } catch (Exception e) {
                    error = "Dark Sky weather update failed";
                    log.error("Failed to update weather for " + latitude + "," + longitude, e);
                }
            }

        }


        return false;
    }

    @Override
    public boolean setLocation(String postalOrZip) {
        removeLocation();
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
     * Get the location details
     * Dark Sky needs Lat Long and does not provide City, State etc. so lets grab all that from Yahoo
     */
    private boolean GetLocationDetails(String location){
        boolean validLatLong = false;
        if (StringUtils.isEmpty(location) || location==null){
            error = "GetLocationDetails: Location passed in was no valid";
            return validLatLong;
        }
        //use the Yahoo handler to get the Lat Long and location details
        YahooWeatherJsonHandler handler = new YahooWeatherJsonHandler();
        validLatLong = handler.LatLongFromLocation(location);

        if (validLatLong){
            latitude = handler.getLat();
            longitude = handler.getLong();
            locationName = handler.getCity() + "," + handler.getRegion();
            city = handler.getCity();
            region = handler.getRegion();
            country = handler.getCountry();
            locationRetrieved = true;
        }else {
            log.warn("Failed to convert " + location + " to Lat-Long");
            error = "Failed to convert the Location into a valid Lat-Long using Yahoo API";
            locationRetrieved = false;
        }
        return validLatLong;
    }

    @Override
    public String getLocation() {
        return config.getLocation();
    }

    @Override
    public void removeLocation() {
        locationName = "";
        config.setLocation(null);
        latitude = null;
        longitude = null;
        locationRetrieved=false;
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
        error = null;
        if (locationRetrieved){
            return true;
        }
        if (GetLocationDetails(this.getLocation())){
            return true;
        }
        error = "Latitude and Longitude or a valid location are required";
        return false;
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

    public void setTesting(boolean testing) {
        this.testing = testing;
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

    private void loadUserKey(){
        if (testing){
            userKey = "";
            log.info("loadUserKey: user key override for testing set");
            return;
        }
        String UserKeyPropsFileName = "darksky.properties";
        File SageTVRoot = new File(System.getProperty("phoenix/sagetvHomeDir", "."));
        File UserKeyPropsFile = new File(SageTVRoot, UserKeyPropsFileName);
        String UserKeyPropsPath = UserKeyPropsFile.toString();
        Properties UserKeyProps = new Properties();

        //read the user key from the properties file
        log.info("loadUserKey: looking for user key in '" + UserKeyPropsPath + "'" );
        try {
            FileInputStream in = new FileInputStream(UserKeyPropsPath);
            try {
                UserKeyProps.load(in);
                userKey = UserKeyProps.getProperty("key",KEY_NOT_FOUND);
                if (userKey.equals(KEY_NOT_FOUND)){
                    userKey = "";
                    hasUserKey = false;
                    log.info("loadUserKey: user key not found loading key property from darksky.properties.");
                }else{
                    hasUserKey = true;
                    log.info("loadUserKey: user key found" );
                }
            } finally {
                in.close();
            }
        } catch (Exception ex) {
            log.info("loadUserKey: file not found loading key property from darksky.properties. " + ex);
        }



    }
}
