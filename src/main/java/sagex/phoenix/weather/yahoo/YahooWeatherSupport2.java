package sagex.phoenix.weather.yahoo;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import sage.media.rss.RSSParser;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.WeatherConfiguration;

public class YahooWeatherSupport2 implements IWeatherSupport2 {
	private Logger log = Logger.getLogger(this.getClass());

	private Date lastUpdated = null;
	private Date recordedDate = null;
	private int ttl = 180;

	private String error;

	private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

	private String locationName;

	private ICurrentForecast currentForecast;
	private List<ILongRangeForecast> longRangeForecast;

	public YahooWeatherSupport2() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSourceName() {
		return "Yahoo! Weather";
	}

	@Override
	public boolean update() {
		error = null;

		if (!isConfigured()) {
			error = "Please configure the Yahoo Weather WOEID for your location";
			return false;
		}

		if (shouldUpdate()) {
			// String units = null;
			// if (getUnits() == Units.Metric) {
			// units = "c";
			// } else {
			// units = "f";
			// }

			String woeid = config.getYahooWOEID();

			// http://weather.yahooapis.com/forecastrss?w=24223981&u=c
			String rssUrl = "http://weather.yahooapis.com/forecastrss?w=" + woeid;
			// String rssUrl = "http://weather.yahooapis.com/forecastrss?w=" +
			// woeid + "&u=" + units;
			if (getUnits().equals(Units.Metric)) {
				rssUrl = rssUrl + "&u=c";
			} else {
				rssUrl = rssUrl + "&u=f";
			}
			log.info("Getting Yahoo Weather for " + rssUrl);
			try {
				YahooWeatherHandler handler = new YahooWeatherHandler();
				RSSParser.parseXmlFile(new URL(rssUrl), handler, false);
				lastUpdated = new Date(System.currentTimeMillis());
				ttl = handler.getTtl();

				currentForecast = handler.getCurrent();
				longRangeForecast = handler.getDays();

				locationName = handler.getCity();
				recordedDate = handler.getRecordedDate();

				return true;
			} catch (Exception e) {
				error = "Yahoo weather update failed";
				log.error("Failed to update weather for " + rssUrl, e);
			}
		}

		return false;
	}

	@Override
	public boolean setLocation(String postalOrZip) {
		error = null;
		boolean configured = false;
		lastUpdated = null;
		// convert zip to woeid
		try {
			config.setLocation(postalOrZip);

			String url = "http://query.yahooapis.com/v1/public/yql?q=select%20woeid%20from%20geo.places%20where%20text='"
					+ postalOrZip + "'%20limit%201";
			SAXReader reader = new SAXReader();
			Document document = reader.read(url);
			String woeid = document.getRootElement().element("results").element("place").element("woeid").getText();

			if (woeid != null) {
				config.setYahooWOEID(woeid);
			}

			configured = true;
			lastUpdated = null;
		} catch (Exception e) {
			log.warn("Failed to convert " + postalOrZip + " to woeid", e);
			error = "Failed to convert the Location into a valid Yahoo WOEID";
			configured = false;
		}
		return configured;
	}

	@Override
	public String getLocation() {
		return config.getLocation();
	}

	@Override
	public void removeLocation() {
		locationName = "";
		config.setLocation(null);
	}

	@Override
	public String getLocationName() {
		return locationName;
	}

	@Override
	public void setUnits(Units u) {
		if (u == null || u == Units.Metric) {
			config.setUnits("m");
		} else {
			config.setUnits("s");
		}
		lastUpdated = null;
	}

	@Override
	public Units getUnits() {
		String u = config.getUnits();
		if (StringUtils.isEmpty(u) || u.toLowerCase().startsWith("m")) {
			return Units.Metric;
		} else {
			return Units.Standard;
		}
	}

	@Override
	public ICurrentForecast getCurrentWeather() {
		return currentForecast;
	}

	@Override
	public List<ILongRangeForecast> getForecasts() {
		return longRangeForecast;
	}

	@Override
	public int getForecastDays() {
		if (longRangeForecast != null) {
			return longRangeForecast.size();
		}
		return 0;
	}

	@Override
	public boolean isConfigured() {
		return !StringUtils.isEmpty(config.getYahooWOEID());
	}

	@Override
	public Date getLastUpdated() {
		return lastUpdated;
	}

	@Override
	public Date getRecordedDate() {
		return recordedDate;
	}

	@Override
	public boolean hasError() {
		return error != null;
	}

	@Override
	public String getError() {
		return error;
	}

	private boolean shouldUpdate() {
		if (lastUpdated == null)
			return true;
		long later = lastUpdated.getTime() + (ttl * 60 * 1000);
		if (System.currentTimeMillis() > later)
			return true;
		log.debug("shouldUpdate: Not time to perform an update. Last update at '" + lastUpdated + "'");
		return false;
	}
}
