package sagex.phoenix.weather;

import java.util.Date;
import java.util.List;

/**
 * @author jusjoken
 */
public interface IWeatherSupport2 {
    /**
     * Used to configure the Units for a weather source.
     */
    public static enum Units {
        Metric, Standard
    }

    /**
     * Returns the name for this specific Weather Source
     *
     * @return Source name
     */
    public String getSourceName();

    /**
     * Returns true if the weather source update was successful.
     *
     * @return true if the update was retrieved
     */
    public boolean update();

    /**
     * Sets the location for the weather source to use.
     *
     * @return true if the location was set
     */
    public boolean setLocation(String location);

    /**
     * Returns the location code used by the weather source. typically a zip
     * code, postal code or special location code
     *
     * @return specific location code
     */
    public String getLocation();

    /**
     * Clears the location from the weather source.
     *
     * @return
     */
    public void removeLocation();

    /**
     * Returns the location name used by the weather source. typically a City
     * Name
     *
     * @return specific location code
     */
    public String getLocationName();

    /**
     * Used to set the units the source uses on the next update.
     *
     * @return
     */
    public void setUnits(Units u);

    /**
     * Returns the units the source uses during updates.
     *
     * @return {@link Units}
     */
    public Units getUnits();

    /**
     * Returns the CurrentWeather retrieved from the last update. the
     * implementation should ensure an update is called prior to this.
     *
     * @return {@link ICurrentForecast}
     */
    public ICurrentForecast getCurrentWeather();

    /**
     * Returns a list of all available days of Long Range Forecasts. the
     * implementation should ensure an update is called prior to this.
     *
     * @return list of {@link ILongRangeForecast}
     */
    public List<ILongRangeForecast> getForecasts();

    /**
     * Returns the number of days available in the Long Range Forecast
     *
     * @return count of Long Range Forecast Days
     */
    public int getForecastDays();

    /**
     * Verifies if the weather source has a valid configuration
     *
     * @return true if a valid configuration is set
     */
    public boolean isConfigured();

    /**
     * Returns the {@link Date} this source was last refreshed this is the date
     * phoenix requested an update to the weather data
     *
     * @return {@link Date} last updated
     */
    public Date getLastUpdated();

    /**
     * Returns the {@link Date} the weather data was recorded by the source this
     * is the date the source API returns if available
     *
     * @return {@link Date} recorded by source
     */
    public Date getRecordedDate();

    /**
     * Returns if any error has occurred updating or changing settings of the
     * weather source
     *
     * @return true if an error has occurred
     */
    public boolean hasError();

    /**
     * Returns the error message from the last update or change of settings
     *
     * @return error message if any
     */
    public String getError();

}
