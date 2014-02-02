package sagex.phoenix.weather;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author jusjoken
 */
public class CurrentForecast extends ForecastPeriod implements ICurrentForecast, Serializable {
	private static final long serialVersionUID = 1L;
	private String cloudCover;
	private Date date;
	private String dewPoint;
	private int feelsLike;
	private String pressure;
	private int pressureDir;
	private String sunrise;
	private String sunset;
	private String UVIndex;
	private String UVWarn;
	private int visibility;

	public String getCloudCover() {
		return cloudCover;
	}

	public void setCloudCover(String cloudCover) {
		this.cloudCover = cloudCover;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDewPoint() {
		return dewPoint;
	}

	public void setDewPoint(String dewPoint) {
		this.dewPoint = dewPoint;
	}

	public int getFeelsLike() {
		return feelsLike;
	}

	public void setFeelsLike(int feelsLike) {
		this.feelsLike = feelsLike;
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
	}

	public int getPressureDir() {
		return pressureDir;
	}

	public void setPressureDir(int pressureDir) {
		this.pressureDir = pressureDir;
	}

	@Override
	public String toString() {
		return "CurrentForecast [" + " temp=" + this.getTemp() + " condition=" + this.getCondition() + " code=" + this.getCode()
				+ " precip=" + this.getPrecip() + " humid=" + this.getHumid() + " windspeed=" + this.getWindSpeed() + " winddir="
				+ this.getWindDir() + " winddirtext=" + this.getWindDirText() + " description=" + this.getDescription()
				+ " [cloudCover=" + cloudCover + ", date=" + date + ", dewPoint=" + dewPoint + ", feelsLike=" + feelsLike
				+ ", pressure=" + pressure + ", pressureDir=" + pressureDir + ", sunrise=" + sunrise + ", sunset=" + sunset
				+ ", UVIndex=" + UVIndex + ", UVWarn=" + UVWarn + ", visibility=" + visibility + "]";
	}

	public String getSunrise() {
		return sunrise;
	}

	public void setSunrise(String sunrise) {
		this.sunrise = sunrise;
	}

	public String getSunset() {
		return sunset;
	}

	public void setSunset(String sunset) {
		this.sunset = sunset;
	}

	public String getUVIndex() {
		return UVIndex;
	}

	public void setUVIndex(String uVIndex) {
		UVIndex = uVIndex;
	}

	public String getUVWarn() {
		return UVWarn;
	}

	public void setUVWarn(String uVWarn) {
		UVWarn = uVWarn;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
}
