package sagex.phoenix.weather;

import sagex.phoenix.tools.annotation.API;

/**
 * Interface to access the Long Range Forecast data for a specific day note: not
 * all fields will be populated depending on the implementation -
 * non-implemented string fields should return null - non-implemented integer
 * fields should return
 *
 * @author jusjoken
 */
@API(group = "weather2", proxy = true)
public interface ILongRangeForecast {

    /**
     * Returns the forecast data for this specific forecast day
     *
     * @return LongRangeforecastPeriod for the Day
     */
    public IForecastPeriod getForecastPeriodDay();

    /**
     * Returns the forecast data for this specific forecast night
     *
     * @return LongRangeforecastPeriod for the Night
     */
    public IForecastPeriod getForecastPeriodNight();

}
