package sagex.phoenix.weather;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.BooleanConverter;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Phoenix Weather Options", path = "phoenix/weather", description = "Phoenix Weather Options")
public class WeatherConfiguration extends GroupProxy {
    @AField(label = "Location", description = "ZIP Code or Postal Code for Weather Service", scope = ConfigScope.SERVER)
    private FieldProxy<String> location = new FieldProxy<String>("");

    @AField(label = "Units", description = "Display Units in Metric or Standard", listSeparator = ",", list = "m:Metric,s:Standard", scope = ConfigScope.SERVER)
    private FieldProxy<String> units = new FieldProxy<String>("m");

    @AField(label = "Yahoo WOEID", description = "Yahoo's WOEID for use with Yahoo Weather Service", scope = ConfigScope.SERVER)
    private FieldProxy<String> yahooWOEID = new FieldProxy<String>("");

    @AField(label = "Weather Check Interval (seconds)", description = "Weather will only update if this many seconds has passed since the last successful update", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> updateInterval = new FieldProxy<Integer>(30*60); // default 30 minutes

    @AField(label = "Weather Implementation", description = "Weather Implemenation", scope = ConfigScope.SERVER
            , list = "noweather:No Weather", listSeparator = "," )
    private FieldProxy<String> weatherSupport = new FieldProxy<String>("noweather");

    @AField(label = "Locked", description = "If locked, then weather cannot changed via APIs", scope = ConfigScope.SERVER )
    private FieldProxy<Boolean> locked = new FieldProxy<Boolean>(false);

    public WeatherConfiguration() {
        super();
        init();
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getUnits() {
        return units.get();
    }

    public void setUnits(String units) {
        this.units.set(units);
    }

    public String getYahooWOEID() {
        return yahooWOEID.get();
    }

    public void setYahooWOEID(String yahooWOEID) {
        this.yahooWOEID.set(yahooWOEID);
    }

    public int getUpdateInterval() {
        return updateInterval.get();
    }

    public void setUpdateInterval(int seconds) {
        updateInterval.set(seconds);
    }

    public String getWeatherSupportClass() {
        return weatherSupport.get();
    }

    public void setWeatherSupportClass(String klass) {
        weatherSupport.set(klass);
    }

    public boolean isLocked() {
        return locked.get();
    }

    public void setLocked(boolean locked) {
        this.locked.set(locked);
    }
}
