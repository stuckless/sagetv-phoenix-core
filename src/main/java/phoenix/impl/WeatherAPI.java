package phoenix.impl;

import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherData;
import sagex.phoenix.weather.yahoo.WeatherData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * WeatherAPI provides access to weather information, including current forecast
 * and long range forecasts.
 *
 * @deprecated use the weather2 apis.  This class will just proxy some calls the to the Weather2 service and will be removed at some point
 * @author seans
 */
@API(group = "weather")
@Deprecated
public class WeatherAPI {
    public WeatherAPI() {
    }

    /**
     * Forces the current and forecasted weather to update. It is up to the
     * implementation to ensure that weather updates are cached. If the weather
     * is updated since the last call then it will return true. If an Error
     * happens, then IsError will return true and GetError will contain the
     * failure message.
     *
     * @return true if the weather was updated
     */
    public boolean Update() {
        return phoenix.weather2.Update();
    }

    /**
     * Sets the user's location as a Postal Code or Zip Code. If an
     * implementation doesn't natively use this information, it should try to
     * convert the location into something can be consumed by the
     * implementation. For example, the Yahoo Weather Service doesn't use zip
     * code so it will convert it into the Yahoo WOEID. If the implemenation
     * can't convert the postal or zip code, then this method will return false.
     *
     * @param postalOrZip ZIP or Postal Code
     * @return true if the implementation accepted the zip code.
     */
    public boolean SetLocation(String postalOrZip) {
        return phoenix.weather2.SetLocation(postalOrZip);
    }

    /**
     * Get the current weather location's ZIP or Postal Code. It may return
     * null, if the weather hasn't been configured.
     *
     * @return
     */
    public String GetLocation() {
        return phoenix.weather2.GetLocation();
    }

    /**
     * Set the Unit for the weather service. Valid values are 'm' for Metric,
     * and 's' for Standard (imperial) units.
     *
     * @param units
     */
    public void SetUnits(String units) {
        phoenix.weather2.SetUnits(units);
    }

    /**
     * Return the configured units for the Weather Service
     *
     * @return
     */
    public String GetUnits() {
        return phoenix.weather2.GetUnits();
    }

    /**
     * Returns the current weather information. You should call update() before
     * calling this method since this will not force an update automatically.
     *
     * @return {@link IWeatherData} instance for the current weather conditions
     * @deprecated Use phoenix.weather2.GetCurrentWeather()
     */
    public IWeatherData GetCurrentWeather() {
        ICurrentForecast cf = phoenix.weather2.GetCurrentWeather();
        WeatherData w = new WeatherData();
        w.setCode(String.valueOf(cf.getCode()));
        w.setDate(cf.getDate());
        w.setTemp(String.valueOf(cf.getTemp()));
        return w;
    }

    /**
     * Returns the long range forecast. Depending on the implementation is may
     * include today's weather.
     *
     * @return {@link List} of {@link IWeatherData} instances for each day,
     * ordered by day.
     */
    public List<IWeatherData> GetForecast() {
        List<ILongRangeForecast> forecastPeriods = phoenix.weather2.GetForecasts();
        List<IWeatherData> list = new ArrayList<IWeatherData>();

        if (forecastPeriods!=null) {
            for (ILongRangeForecast lr : forecastPeriods) {
                WeatherData w = new WeatherData();
                w.setDate(lr.getForecastPeriodDay().getDate());
                w.setHigh(String.valueOf(lr.getForecastPeriodDay().getTemp()));
                w.setLow(String.valueOf(lr.getForecastPeriodNight().getTemp()));
                list.add(w);
            }
        }

        return list;
    }

    /**
     * Return true if the Weather Service is configured.
     *
     * @return true if configured
     */
    public boolean IsConfigured() {
        return phoenix.weather2.IsConfigured();
    }

    /**
     * Returns the number of days in the weather forecast
     *
     * @return days in the forecast
     */
    public int GetForecastDays() {
        return phoenix.weather2.GetForecastDays();
    }

    /**
     * Returns the {@link Date} the weather was last updated.
     *
     * @return {@link Date} of last update
     */
    public Date GetLastUpdated() {
        return phoenix.weather2.GetLastUpdated();
    }

    /**
     * Returns the location name (usually the City) if known. This may be null
     * until an update happens.
     *
     * @return location name, usually the city
     */
    public String GetLocationName() {
        return phoenix.weather2.GetLocationName();
    }

    /**
     * Return true if there was a Weather Service error
     *
     * @return true if error
     */
    public boolean HasError() {
        return phoenix.weather2.HasError();
    }

    /**
     * Returns the error if HasError return true, otherwise it will return null.
     *
     * @return
     */
    public String GetError() {
        return phoenix.weather2.GetError();
    }
}
