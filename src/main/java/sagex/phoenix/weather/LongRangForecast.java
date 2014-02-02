package sagex.phoenix.weather;

import java.io.Serializable;

public class LongRangForecast implements ILongRangeForecast, Serializable {
	private static final long serialVersionUID = 1L;
	private ForecastPeriod forecastPeriodDay;
	private ForecastPeriod forecastPeriodNight;

	public ForecastPeriod getForecastPeriodDay() {
		return forecastPeriodDay;
	}

	public void setForecastPeriodDay(ForecastPeriod forecastPeriodDay) {
		this.forecastPeriodDay = forecastPeriodDay;
	}

	public ForecastPeriod getForecastPeriodNight() {
		return forecastPeriodNight;
	}

	public void setForecastPeriodNight(ForecastPeriod forecastPeriodNight) {
		this.forecastPeriodNight = forecastPeriodNight;
	}

	@Override
	public String toString() {
		return "LongRangForecast{" + "forecastPeriodDay=" + forecastPeriodDay + ", forecastPeriodNight=" + forecastPeriodNight
				+ '}';
	}

}
