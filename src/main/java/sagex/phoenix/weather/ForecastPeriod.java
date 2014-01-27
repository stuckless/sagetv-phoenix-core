package sagex.phoenix.weather;

import java.io.Serializable;
import java.util.Date;

public class ForecastPeriod implements IForecastPeriod, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Date date;
	private Type type;
	private int temp;
	private int code;
	private String condition;
	private String description;
	private int humid;
	private String precip;
	private int windDir;
	private String windDirText;
	private int windSpeed;
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getTemp() {
		return temp;
	}
	public void setTemp(Integer temp) {
		this.temp = temp;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}

        @Override
        public String toString() {
            return "ForecastPeriod{" + "date=" + date + ", type=" + type + ", temp=" + temp + ", code=" + code + ", condition=" + condition + ", humid=" + humid + ", precip=" + precip + ", windDir=" + windDir + ", windDirText=" + windDirText + ", windSpeed=" + windSpeed + ", description=" + description + '}';
        }

        public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getHumid() {
		return humid;
	}
	public void setHumid(int humid) {
		this.humid = humid;
	}
	public String getPrecip() {
		return precip;
	}
	public void setPrecip(String precip) {
		this.precip = precip;
	}
	public int getWindDir() {
		return windDir;
	}
	public void setWindDir(int windDir) {
		this.windDir = windDir;
	}
        public String getWindDirText() {
            return windDirText;
        }
        public void setWindDirText(String windDirText) {
            this.windDirText = windDirText;
        }
	public int getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}
	

}
