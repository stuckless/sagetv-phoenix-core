package phoenix.impl;

import sagex.api.Configuration;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;

import java.util.Date;
import java.util.List;

/**
 * Created by seans on 11/03/17.
 */
public class DummyWeather implements IWeatherSupport2 {
    String location;
    Units units;
    Date lastUpdated;

    public DummyWeather() {
    }

    @Override
    public String getSourceName() {
        return "dummy";
    }

    @Override
    public boolean update() {
        lastUpdated=new Date(System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean setLocation(String location) {
        this.location=location;
        return true;
    }

    @Override
    public String getLocation() {
        if (location==null) {
            location = Configuration.GetServerProperty("phoenix/weather/location","toledo");
        }
        return location;
    }

    @Override
    public void removeLocation() {
        location=null;
    }

    @Override
    public String getLocationName() {
        return location;
    }

    @Override
    public void setUnits(Units u) {
        this.units=u;
    }

    @Override
    public Units getUnits() {
        return units;
    }

    @Override
    public ICurrentForecast getCurrentWeather() {
        return null;
    }

    @Override
    public List<ILongRangeForecast> getForecasts() {
        return null;
    }

    @Override
    public int getForecastDays() {
        return 0;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Date getRecordedDate() {
        return lastUpdated;
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public String getError() {
        return null;
    }
}
