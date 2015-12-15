package sagex.phoenix.weather;

import java.util.Date;

import sagex.phoenix.tools.annotation.API;

/**
 * Interface to access the Current Weather Forecast data for the current day
 * note: not all fields will be populated depending on the implementation -
 * non-implemented string fields should return null - non-implemented integer
 * fields should return
 *
 * @author jusjoken
 */
@API(group = "weather2", proxy = true)
public interface ICurrentForecast extends IForecastPeriod {
    /**
     * Returns the {@link Date} for the current weather forecast
     *
     * @return {@link Date}
     */
    public Date getDate();

    /**
     * Returns the Sunrise time, such as, "7:28 am", if known
     *
     * @return formatted time of sunrise
     */
    public String getSunrise();

    /**
     * Return the Sunset time, such as, "4:53 pm", if known
     *
     * @return formatted time of sunset
     */
    public String getSunset();

    /**
     * Return the Visibility for the current weather forecast
     *
     * @return Visibility
     */
    public int getVisibility();

    /**
     * Return the Windchill or Feels like temperature for the current weather
     * forecast
     *
     * @return Feel Like Temp
     */
    public int getFeelsLike();

    /**
     * Return the Barometric Pressure for the current weather forecast
     *
     * @return Pressure
     */
    public String getPressure();

    /**
     * Return an indicator of the Barometric Pressure rising/falling for the
     * current weather forecast 0 steady (default) 1 rising -1 falling
     *
     * @return Pressure Direction
     */
    public int getPressureDir();

    /**
     * Return the DewPoint for the current weather forecast
     *
     * @return DewPoint
     */
    public String getDewPoint();

    /**
     * Return the Formatted cloud cover for the current weather forecast
     *
     * @return formatted Cloud Cover
     */
    public String getCloudCover();

    /**
     * Return the UVIndex for the current weather forecast
     *
     * @return UVIndex
     */
    public String getUVIndex();

    /**
     * Return the UVWarn for the current weather forecast
     *
     * @return UVWarn
     */
    public String getUVWarn();

}
