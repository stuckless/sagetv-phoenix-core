package sagex.phoenix.weather.yahoo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import sage.media.rss.RSSHandler;
import sagex.phoenix.json.JSON;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.Url;
import sagex.phoenix.util.url.UrlFactory;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.IForecastPeriod.Type;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Weather Json for Yahoo
 */
public class YahooWeatherJsonHandler {
    private Logger log = Logger.getLogger(this.getClass());
    private static HashMap<String, String> codeMap = new HashMap<String, String>();

    static {
        codeMap.put("-1", "Unknown");
        codeMap.put("0", "tornado");
        codeMap.put("1", "tropical storm");
        codeMap.put("2", "hurricane");
        codeMap.put("3", "severe thunderstorms");
        codeMap.put("4", "thunderstorms");
        codeMap.put("5", "mixed rain and snow");
        codeMap.put("6", "mixed rain and sleet");
        codeMap.put("7", "mixed snow and sleet");
        codeMap.put("8", "freezing drizzle");
        codeMap.put("9", "drizzle");
        codeMap.put("10", "freezing rain");
        codeMap.put("11", "showers");
        codeMap.put("12", "showers");
        codeMap.put("13", "snow flurries");
        codeMap.put("14", "light snow showers");
        codeMap.put("15", "blowing snow");
        codeMap.put("16", "snow");
        codeMap.put("17", "hail");
        codeMap.put("18", "sleet");
        codeMap.put("19", "dust");
        codeMap.put("20", "foggy");
        codeMap.put("21", "haze");
        codeMap.put("22", "smoky");
        codeMap.put("23", "blustery");
        codeMap.put("24", "windy");
        codeMap.put("25", "cold");
        codeMap.put("26", "cloudy");
        codeMap.put("27", "mostly cloudy (night)");
        codeMap.put("28", "mostly cloudy (day)");
        codeMap.put("29", "partly cloudy (night)");
        codeMap.put("30", "partly cloudy (day)");
        codeMap.put("31", "clear (night)");
        codeMap.put("32", "sunny");
        codeMap.put("33", "fair (night)");
        codeMap.put("34", "fair (day)");
        codeMap.put("35", "mixed rain and hail");
        codeMap.put("36", "hot");
        codeMap.put("37", "isolated thunderstorms");
        codeMap.put("38", "scattered thunderstorms");
        codeMap.put("39", "scattered thunderstorms");
        codeMap.put("40", "scattered showers");
        codeMap.put("41", "heavy snow");
        codeMap.put("42", "scattered snow showers");
        codeMap.put("43", "heavy snow");
        codeMap.put("44", "partly cloudy");
        codeMap.put("45", "thundershowers");
        codeMap.put("46", "snow showers");
        codeMap.put("47", "isolated thundershowers");
        codeMap.put("3200", "not available");
    }

    private String city, region, country;
    private String unitTemp, unitDist, unitPress, unitSpeed;
    private String windChill, windSpeed, windDirection;
    private String humidity, visibility, pressure, rising;
    private String sunrise, sunset;
    private Date recordedDate;
    private int ttl;
    private String imageUrl;

    private String text;

    // private List<IWeatherData> days = new ArrayList<IWeatherData>();
    // private WeatherData current;

    private CurrentForecast current;
    private List<ILongRangeForecast> days = new ArrayList<ILongRangeForecast>();

    public YahooWeatherJsonHandler() {
    }

    public void parse(String urlString) throws IOException, JSONException {
        String data = UrlUtil.getContentAsString(UrlFactory.newUrl(urlString));
        JSONObject jo = new JSONObject(data);
        JSONObject channel = JSON.get("query.results.channel", jo);
        if (channel == null) throw new IOException("JSON Response for Weather did not contain a valid response");
        ttl = JSON.getInt("ttl", channel);
        recordedDate = DateUtils.parseDate(JSON.getString("lastBuildDate", channel));

        JSONObject location = JSON.get("location", channel);
        city = JSON.get("city", location);
        region = JSON.get("region", location);
        country = JSON.get("country", location);

        JSONObject units = JSON.get("units", channel);
        unitTemp = JSON.get("temperature", units);
        unitDist = JSON.get("distance", units);
        unitPress = JSON.get("pressure", units);
        unitSpeed = JSON.get("speed", units);

        JSONObject wind = JSON.get("wind", channel);
        windChill = JSON.getString("chill", wind);
        windSpeed = JSON.getString("speed", wind);
        windDirection = JSON.getString("direction", wind);

        JSONObject atmosphere = JSON.get("atmosphere", channel);
        humidity = JSON.get("humidity", atmosphere);
        visibility = JSON.get("visibility", atmosphere);
        pressure = JSON.get("pressure", atmosphere);
        rising = JSON.get("rising", atmosphere);

        JSONObject astronomy = JSON.get("astronomy", channel);
        sunrise = JSON.get("sunrise", astronomy);
        sunset = JSON.get("sunset", astronomy);

        JSONObject condition = JSON.get("item.condition", channel);
        current = new CurrentForecast();
        current.setType(Type.Current);
        current.setCloudCover(IForecastPeriod.sNotSupported);
        current.setDescription(IForecastPeriod.sNotSupported);
        current.setDewPoint(IForecastPeriod.sNotSupported);
        current.setPrecip(IForecastPeriod.sNotSupported);
        current.setUVIndex(IForecastPeriod.sNotSupported);
        current.setUVWarn(IForecastPeriod.sNotSupported);
        current.setCode(JSON.getInt("code", condition, IForecastPeriod.iInvalid));
        current.setCondition(JSON.getString("text", condition));
        current.setDate(DateUtils.parseDate(JSON.getString("date", condition)));
        current.setFeelsLike(NumberUtils.toInt(windChill, IForecastPeriod.iInvalid));
        current.setHumid(NumberUtils.toInt(humidity, IForecastPeriod.iInvalid));
        current.setPressure(pressure);
        current.setPressureDir(NumberUtils.toInt(rising, IForecastPeriod.iInvalid));
        current.setSunrise(sunrise);
        current.setSunset(sunset);
        current.setTemp(JSON.getInt("temp", condition, IForecastPeriod.iInvalid));
        current.setVisibility(formatVisibility(visibility));
        current.setWindSpeed((int) Math.round(NumberUtils.toDouble(windSpeed, IForecastPeriod.iInvalid)));

        if (current.getWindSpeed() == 0) {
            current.setWindDir(IForecastPeriod.iInvalid);
            current.setWindDirText(IForecastPeriod.WindCalm);
        } else {
            current.setWindDir(NumberUtils.toInt(windDirection, IForecastPeriod.iInvalid));
            current.setWindDirText(formatCompassDirection(current.getWindDir()));
        }

        JSON.each("item.forecast", channel, new JSON.ArrayVisitor() {
            public void visitItem(int i, JSONObject item) {
                LongRangForecast r = new LongRangForecast();
                ForecastPeriod day = new ForecastPeriod();
                ForecastPeriod night = new ForecastPeriod();
                r.setForecastPeriodDay(day);
                r.setForecastPeriodNight(night);

                days.add(r);

                day.setCode(JSON.getInt("code", item, -1));
                day.setCondition(JSON.getString("text", item));
                day.setDate(DateUtils.parseDate(JSON.getString("date", item)));
                day.setDescription(IForecastPeriod.sNotSupported);
                day.setHumid(IForecastPeriod.iNotSupported);
                day.setPrecip(IForecastPeriod.sNotSupported);
                day.setTemp(JSON.getInt("high", item, Integer.MAX_VALUE));
                day.setType(Type.Day);
                day.setWindDir(IForecastPeriod.iNotSupported);
                day.setWindDirText(IForecastPeriod.sNotSupported);
                day.setWindSpeed(IForecastPeriod.iNotSupported);

                night.setCode(phoenix.weather2.GetCodeForceNight(JSON.getInt("code", item, -1)));
                night.setCondition(JSON.getString("text", item));
                night.setDate(DateUtils.parseDate(JSON.getString("date", item)));
                night.setDescription(IForecastPeriod.sNotSupported);
                night.setHumid(IForecastPeriod.iNotSupported);
                night.setPrecip(IForecastPeriod.sNotSupported);
                night.setTemp(JSON.getInt("low", item, Integer.MAX_VALUE));
                night.setType(Type.Night);
                night.setWindDir(IForecastPeriod.iNotSupported);
                night.setWindDirText(IForecastPeriod.sNotSupported);
                night.setWindSpeed(IForecastPeriod.iNotSupported);

            }
        });
    }

    public static HashMap<String, String> getCodeMap() {
        return codeMap;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getUnitTemp() {
        return unitTemp;
    }

    public String getUnitDist() {
        return unitDist;
    }

    public String getUnitPress() {
        return unitPress;
    }

    public String getUnitSpeed() {
        return unitSpeed;
    }

    public String getWindChill() {
        return windChill;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getPressure() {
        return pressure;
    }

    public String getRising() {
        return rising;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public List<ILongRangeForecast> getDays() {
        return days;
    }

    public int getTtl() {
        return ttl;
    }

    public CurrentForecast getCurrent() {
        return current;
    }

    private static Pattern woidPattern = Pattern.compile(".*yahoo.*/[^-]+-([0-9]{1,10})/", Pattern.CASE_INSENSITIVE);

    /**
     * Parses the yahoo WOID from a Yahoo Weather URL
     * http://weather.yahoo.com/canada/ontario/london-4063/
     *
     * @param url
     * @return
     */
    public static String parseWOID(String url) {
        if (url == null)
            return null;
        Matcher m = woidPattern.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private int formatVisibility(String Vis) {
        Double vis = NumberUtils.toDouble(Vis, IForecastPeriod.iInvalid);
        if (vis == IForecastPeriod.iInvalid)
            return IForecastPeriod.iInvalid;
        return (int) Math.round(vis);
    }

    private String formatCompassDirection(int degrees) {
        String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW",
                "NW", "NNW"};
        int index = (int) ((degrees / 22.5) + .5);
        return directions[index % 16];
    }
}
