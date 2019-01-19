package sagex.phoenix.weather;

import java.util.Date;

import sagex.phoenix.tools.annotation.API;

/**
 * Interface to access the Forecast data for a Period of a day or the Current
 * Weather A Period is typically a Day or a Night where a day will have 2
 * periods A period could be the Current Period or Current Weather note: not all
 * fields will be populated depending on the implementation - non-implemented
 * string fields should return null - non-implemented integer fields should
 * return
 *
 * @author jusjoken
 */
@API(group = "weather2", proxy = true)
public interface IForecastPeriod {
    /**
     * Used to determine if the period is Day or Night
     */
    public static enum Type {
        Day, Night, Current
    }

    public static final String sNotSupported = "sNotSupported";
    public static final int iNotSupported = Integer.MAX_VALUE - 1;
    public static final Double dNotSupported = Double.MAX_VALUE - 1;
    public static final String sInvalid = "sInvalid";
    public static final int iInvalid = Integer.MAX_VALUE;
    public static final Double dInvalid = Double.MAX_VALUE;
    public static final String WindCalm = "Calm";

    /**
     * Returns the {@link Date} for this specific forecast period
     *
     * @return {@link Date}
     */
    public Date getDate();

    /**
     * Returns the type of this period (Day/Night/Current)
     *
     * @return period type (Day or Night or Current)
     */
    public sagex.phoenix.weather.IForecastPeriod.Type getType();

    /**
     * Returns the temperature for the current weather forecast period.
     *
     * @return current temp
     */
    public int getTemp();

    /**
     * Returns a short descriptive text for this specific forecast period. It
     * will be something like, "Mostly Cloudy", or "Partly Sunny"
     *
     * @return short description of the periods weather condition.
     */
    public String getCondition();

    /**
     * Return the high weather condition code, similar as described in
     * http://developer.yahoo.com/weather/#codes
     * <p/>
     * <pre>
     * 0	tornado
     * 1	tropical storm
     * 2	hurricane
     * 3	severe thunderstorms
     * 4	thunderstorms
     * 5	mixed rain and snow
     * 6	mixed rain and sleet
     * 7	mixed snow and sleet
     * 8	freezing drizzle
     * 9	drizzle
     * 10	freezing rain
     * 11	showers
     * 12	showers
     * 13	snow flurries
     * 14	light snow showers
     * 15	blowing snow
     * 16	snow
     * 17	hail
     * 18	sleet
     * 19	dust
     * 20	foggy
     * 21	haze
     * 22	smoky
     * 23	blustery
     * 24	windy
     * 25	cold
     * 26	cloudy
     * 27	mostly cloudy (night)
     * 28	mostly cloudy (day)
     * 29	partly cloudy (night)
     * 30	partly cloudy (day)
     * 31	clear (night)
     * 32	sunny
     * 33	fair (night)
     * 34	fair (day)
     * 35	mixed rain and hail
     * 36	hot
     * 37	isolated thunderstorms
     * 38	scattered thunderstorms
     * 39	scattered thunderstorms
     * 40	scattered showers
     * 41	heavy snow
     * 42	scattered snow showers
     * 43	heavy snow
     * 44	partly cloudy
     * 45	thundershowers
     * 46	snow showers
     * 47	isolated thundershowers
     * -1	not available
     * </pre>
     * <p/>
     * May vary by implementation but should be a code between 0 and 47 Should
     * be -1 if not available Implementations should map their condition codes
     * to the codes listed
     *
     * @return condition code for the period
     */
    public int getCode();

    /**
     * Return the Formatted precipitation for the forecast period - can be in
     * either inches, mm, or may be % so should be fully formatted for display
     *
     * @return Period Precipitation
     */
    public String getPrecip();

    /**
     * Return the humidity for the forecast Period - should be -1 if invalid or
     * unavailable
     *
     * @return Period Humidity
     */
    public int getHumid();

    /**
     * Returns the wind speed for the forecast period. - should be 0 for CALM -
     * should be -1 if invalid or unavailable
     *
     * @return wind speed
     */
    public int getWindSpeed();

    /**
     * Returns the degrees wind direction for the forecast period.
     *
     * @return wind direction in degrees
     */
    public int getWindDir();

    /**
     * Returns the wind direction for the forecast period.
     *
     * @return wind direction as text
     */
    public String getWindDirText();

    /**
     * Return the Long Description of the forecast Period
     *
     * @return long Period Description
     */
    public String getDescription();

}
