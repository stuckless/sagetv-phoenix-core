package sagex.phoenix.weather.darksky;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.json.JSON;
import sagex.phoenix.util.url.UrlFactory;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.IForecastPeriod.Type;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parses Weather Json for Dark Sky
 */
public class DarkSkyWeatherJsonHandler {
    private Logger log = Logger.getLogger(this.getClass());
    private HashMap<String, String> CodesForDaytime = new HashMap<String, String>();
    private HashMap<String, String> CodesForNighttime = new HashMap<String, String>();

    private String city, region, country;
    private String windSpeed, windDirection;
    private String visibility, pressure, rising;
    private Double humidity,windChill,precip;
    private String sunrise, sunset;
    private Date recordedDate;

    private CurrentForecast current;
    private List<ILongRangeForecast> days = new ArrayList<ILongRangeForecast>();

    public DarkSkyWeatherJsonHandler() {
        BuildWeatherIconLists();
    }

    private void BuildWeatherIconLists() {
        AddIcon("clear-day", "32", "31");
        AddIcon("clear-night", "31", "31");
        AddIcon("rain", "40", "45");
        AddIcon("snow", "41", "42");
        AddIcon("sleet", "5", "5");
        AddIcon("wind", "23", "23");
        AddIcon("fog", "20", "20");
        AddIcon("cloudy", "26", "26");
        AddIcon("partly-cloudy-day", "30", "29");
        AddIcon("partly-cloudy-night", "29", "29");
        AddIcon("unknown", "-1", "-1");
    }

    private void AddIcon(String CodeSource, String CodeForDay, String CodeForNight) {
        CodesForDaytime.put(CodeSource, CodeForDay);
        CodesForNighttime.put(CodeSource, CodeForNight);
    }

    private Integer GetCodeFromName(String IconName, IForecastPeriod.Type DayType) {
        Integer tIcon = -1;
        if (IconName == null) {
            return -1;
        } else {
            if (DayType.equals(IForecastPeriod.Type.Current) || DayType.equals(IForecastPeriod.Type.Day)) {
                if (CodesForDaytime.containsKey(IconName)) {
                    return phoenix.util.ToInt(CodesForDaytime.get(IconName), tIcon);
                } else {
                    return -1;
                }
            } else {
                if (CodesForNighttime.containsKey(IconName)) {
                    return phoenix.util.ToInt(CodesForNighttime.get(IconName), tIcon);
                } else {
                    return -1;
                }
            }
        }
    }

    public void parse(String urlString) throws IOException, JSONException {
        this.parse(urlString,IForecastPeriod.sInvalid,IForecastPeriod.sInvalid,IForecastPeriod.sInvalid);
    }

    public void parse(String urlString, String City, String Region, String Country) throws IOException, JSONException {
        city = City;
        region = Region;
        country = Country;
        final String requestTZ;

        String data = UrlUtil.getContentAsString(UrlFactory.newUrl(urlString));
        JSONObject root = new JSONObject(data);
        JSONObject currently = root.getJSONObject("currently");
        if (currently == null) throw new IOException("JSON Response for 'currently' Weather did not contain a valid response");
        JSONObject daily = root.getJSONObject("daily");
        requestTZ = JSON.getString("timezone", root);

        //TODO: recorded date - perhaps the date of the currently
        recordedDate = convertUNIXDate(JSON.getString("time", currently));

        current = new CurrentForecast();
        current.setType(Type.Current);
        Double tCloudCover = NumberUtils.toDouble(JSON.getString("cloudCover", currently))*100;
        current.setCloudCover(String.valueOf(tCloudCover.intValue()));

        current.setDewPoint(JSON.getString("dewPoint", currently));

        precip = NumberUtils.toDouble(JSON.getString("precipProbability", currently))*100;
        current.setPrecip(String.valueOf(precip.intValue()));
        current.setUVIndex(JSON.getString("uvIndex", currently));
        current.setUVWarn(IForecastPeriod.sNotSupported);
        current.setDate(convertUNIXDate(JSON.getString("time", currently)));

        current.setCode(GetCodeFromName(JSON.getString("icon", currently),Type.Current));
        current.setCondition(JSON.getString("summary", currently));

        //this is the Feels Like temp
        windChill = NumberUtils.toDouble(JSON.getString("apparentTemperature", currently));
        current.setFeelsLike(windChill.intValue());
        humidity = NumberUtils.toDouble(JSON.getString("humidity", currently))*100;
        current.setHumid(humidity.intValue());
        pressure = JSON.getString("pressure", currently);
        current.setPressure(pressure);
        current.setPressureDir(IForecastPeriod.iNotSupported);

        current.setTemp(JSON.getInt("temperature", currently, IForecastPeriod.iInvalid));
        visibility = JSON.getString("visibility", currently);
        current.setVisibility(formatVisibility(visibility));

        current.setDescription(JSON.getString("summary", daily));

        windSpeed = JSON.getString("windSpeed", currently);
        windDirection = JSON.getString("windBearing", currently);
        current.setWindSpeed((int) Math.round(NumberUtils.toDouble(windSpeed, IForecastPeriod.iInvalid)));
        if (current.getWindSpeed() == 0) {
            current.setWindDir(IForecastPeriod.iInvalid);
            current.setWindDirText(IForecastPeriod.WindCalm);
        } else {
            current.setWindDir(NumberUtils.toInt(windDirection, IForecastPeriod.iInvalid));
            current.setWindDirText(formatCompassDirection(current.getWindDir()));
        }

        JSON.each("data", daily, new JSON.ArrayVisitor() {
            public void visitItem(int i, JSONObject item) {
                LongRangForecast r = new LongRangForecast();
                ForecastPeriod day = new ForecastPeriod();
                ForecastPeriod night = new ForecastPeriod();
                r.setForecastPeriodDay(day);
                r.setForecastPeriodNight(night);

                days.add(r);

                //set the sunrise and sunset from the first day to the current forecast
                if (i==0){
                    sunrise = formatSunriseSunset(JSON.getString("sunriseTime", item),requestTZ);
                    current.setSunrise(sunrise);
                    sunset = formatSunriseSunset(JSON.getString("sunsetTime", item),requestTZ);
                    current.setSunset(sunset);
                }

                day.setCode(GetCodeFromName(JSON.getString("icon", item),Type.Day));
                day.setCondition(JSON.getString("summary", item));
                day.setDate(convertUNIXDate(JSON.getString("time", item)));
                day.setDescription(JSON.getString("summary", item));
                humidity = NumberUtils.toDouble(JSON.getString("humidity", item))*100;
                day.setHumid(humidity.intValue());

                precip = NumberUtils.toDouble(JSON.getString("precipProbability", item))*100;
                day.setPrecip(String.valueOf(precip.intValue()));
                day.setTemp(JSON.getInt("temperatureHigh", item, IForecastPeriod.iInvalid));
                day.setType(Type.Day);
                windSpeed = JSON.getString("windSpeed", item);
                windDirection = JSON.getString("windBearing", item);
                day.setWindSpeed((int) Math.round(NumberUtils.toDouble(windSpeed, IForecastPeriod.iInvalid)));
                if (day.getWindSpeed() == 0) {
                    day.setWindDir(IForecastPeriod.iInvalid);
                    day.setWindDirText(IForecastPeriod.WindCalm);
                } else {
                    day.setWindDir(NumberUtils.toInt(windDirection, IForecastPeriod.iInvalid));
                    day.setWindDirText(formatCompassDirection(day.getWindDir()));
                }

                night.setCode(GetCodeFromName(JSON.getString("icon", item),Type.Night));
                night.setCondition(JSON.getString("summary", item));
                night.setDate(convertUNIXDate(JSON.getString("time", item)));
                night.setDescription(JSON.getString("summary", item));
                humidity = NumberUtils.toDouble(JSON.getString("humidity", item))*100;
                night.setHumid(humidity.intValue());
                precip = NumberUtils.toDouble(JSON.getString("precipProbability", item))*100;
                night.setPrecip(String.valueOf(precip.intValue()));
                night.setTemp(JSON.getInt("temperatureLow", item, IForecastPeriod.iInvalid));
                night.setType(Type.Night);
                night.setWindSpeed((int) Math.round(NumberUtils.toDouble(windSpeed, IForecastPeriod.iInvalid)));
                if (night.getWindSpeed() == 0) {
                    night.setWindDir(IForecastPeriod.iInvalid);
                    night.setWindDirText(IForecastPeriod.WindCalm);
                } else {
                    night.setWindDir(NumberUtils.toInt(windDirection, IForecastPeriod.iInvalid));
                    night.setWindDirText(formatCompassDirection(night.getWindDir()));
                }

            }
        });
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

    public String getWindChill() {
        return windChill.toString();
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getHumidity() {
        return humidity.toString();
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

    public CurrentForecast getCurrent() {
        return current;
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

    private Date convertUNIXDate(String inDate){
        return new java.util.Date(Long.parseLong(inDate)*1000);
    }

    private String formatSunriseSunset(String sDate, String TZ) {
        Date dDate = convertUNIXDate(sDate);
        SimpleDateFormat sdfRequestTZ = new SimpleDateFormat("h:mm");
        TimeZone tzRequestTZ = TimeZone.getTimeZone(TZ);
        sdfRequestTZ.setTimeZone(tzRequestTZ);
        return sdfRequestTZ.format(dDate);
    }


}
