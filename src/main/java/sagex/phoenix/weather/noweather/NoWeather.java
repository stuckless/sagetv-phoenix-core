package sagex.phoenix.weather.noweather;

import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;

import java.util.Date;
import java.util.List;

/**
 * Created by jusjoken on 9/18/2021.
 */
public class NoWeather implements IWeatherSupport2 {


    @Override
    public String getSourceName() {
        return null;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean setLocation(String location) {
        return false;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public void removeLocation() {

    }

    @Override
    public String getLocationName() {
        return null;
    }

    @Override
    public void setUnits(Units u) {

    }

    @Override
    public Units getUnits() {
        return null;
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
        return false;
    }

    @Override
    public Date getLastUpdated() {
        return null;
    }

    @Override
    public Date getRecordedDate() {
        return null;
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
