package sagex.phoenix.weather.wunderground;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sage.google.weather.WeatherUnderground;
import sagex.UIContext;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.weather.CurrentForecast;
import sagex.phoenix.weather.ForecastPeriod;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.IForecastPeriod;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.LongRangForecast;
import sagex.phoenix.weather.WeatherConfiguration;

/**
 * Weather Underground implementation for Phoenix retrieved using the
 * googelweather.jar resource maintained by Opus4 (Andy)
 *
 * @author jusjoken
 */
public class WundergroundWeatherSupport implements IWeatherSupport2 {
    private Logger log = Logger.getLogger(this.getClass());
    private WeatherUnderground wWeather = WeatherUnderground.getInstance();
    private String error;
    private CurrentForecast current = null;
    private List<ILongRangeForecast> forecast;
    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 15; // update every 15 mins
    private HashMap<String, String> CodesForDaytime = new HashMap<String, String>();
    private HashMap<String, String> CodesForNighttime = new HashMap<String, String>();
    // use the following to check if a value is NA
    private final String NAText = WeatherUnderground.getNotAvailableText();

    private WeatherConfiguration config = GroupProxy.get(WeatherConfiguration.class);

    public WundergroundWeatherSupport() {
        BuildWeatherIconLists();
    }

    private void BuildWeatherIconLists() {
        AddIcon("clear", "32", "31");
        AddIcon("flurries", "14", "46");
        AddIcon("fog", "20", "20");
        AddIcon("hazy", "21", "21");
        AddIcon("cloudy", "26", "26");
        AddIcon("mostlycloudy", "28", "27");
        AddIcon("partlycloudy", "30", "29");
        AddIcon("partlysunny", "30", "29");
        AddIcon("mostlysunny", "34", "33");
        AddIcon("rain", "40", "45");
        AddIcon("sleet", "5", "5");
        AddIcon("snow", "41", "42");
        AddIcon("sunny", "32", "31");
        AddIcon("tstorms", "37", "47");
        AddIcon("chanceflurries", "13", "13");
        AddIcon("chancerain", "39", "45");
        AddIcon("chancesleet", "5", "5");
        AddIcon("chancesnow", "41", "42");
        AddIcon("chancetstorms", "37", "47");
        AddIcon("unknown", "-1", "-1");

        AddIcon("nt_clear", "31", "31");
        AddIcon("nt_flurries", "46", "46");
        AddIcon("nt_fog", "20", "20");
        AddIcon("nt_hazy", "21", "21");
        AddIcon("nt_cloudy", "26", "26");
        AddIcon("nt_mostlycloudy", "27", "27");
        AddIcon("nt_partlycloudy", "29", "29");
        AddIcon("nt_partlysunny", "29", "29");
        AddIcon("nt_mostlysunny", "33", "33");
        AddIcon("nt_rain", "45", "45");
        AddIcon("nt_sleet", "5", "5");
        AddIcon("nt_snow", "42", "42");
        AddIcon("nt_sunny", "31", "31");
        AddIcon("nt_tstorms", "47", "47");
        AddIcon("nt_chanceflurries", "13", "13");
        AddIcon("nt_chancerain", "45", "45");
        AddIcon("nt_chancesleet", "5", "5");
        AddIcon("nt_chancesnow", "42", "42");
        AddIcon("nt_chancetstorms", "47", "47");
    }

    private void AddIcon(String CodeSource, String CodeForDay, String CodeForNight) {
        CodesForDaytime.put(CodeSource, CodeForDay);
        CodesForNighttime.put(CodeSource, CodeForNight);
    }

    private Integer GetCodeFromName(String IconName, IForecastPeriod.Type DayType) {
        // log.info("GetCodeFromName: IconName '" + IconName + "' DayType '" +
        // DayType + "'");
        Integer tIcon = -1;
        if (IconName == null) {
            return -1;
        } else {
            if (DayType.equals(IForecastPeriod.Type.Current) || DayType.equals(IForecastPeriod.Type.Day)) { // return
                // the
                // Day
                // Code
                if (CodesForDaytime.containsKey(IconName)) {
                    // log.info("GetCodeFromName: returning '" +
                    // phoenix.util.ToInt(CodesForDaytime.get(IconName),-1) +
                    // "'");
                    return phoenix.util.ToInt(CodesForDaytime.get(IconName), -1);
                } else {
                    return -1;
                }
            } else { // return the Night Code
                if (CodesForNighttime.containsKey(IconName)) {
                    // log.info("GetCodeFromName: returning '" +
                    // phoenix.util.ToInt(CodesForNighttime.get(IconName),-1) +
                    // "'");
                    return phoenix.util.ToInt(CodesForNighttime.get(IconName), -1);
                } else {
                    return -1;
                }
            }
        }
    }

    @Override
    public String getSourceName() {
        return "Weather Underground";
    }

    @Override
    public boolean update() {
        error = null;

        if (!isConfigured()) {
            error = "Please configure the Weather Underground location";
            return false;
        }

        if (shouldUpdate()) {

            UIContext tUI = new UIContext(sagex.api.Global.GetUIContextName());
            String LangCode = sagex.api.Configuration.GetProperty(tUI, "ui/translation_language_code", "en");
            log.info("Getting Wunderground Weather for '" + wWeather.getWeatherLoc() + "'");
            wWeather.updateNow(LangCode);
            lastUpdated = new Date(System.currentTimeMillis());
            recordedDate = new Date(wWeather.getLastUpdateTime());

            // populate current forecast
            CurrentForecast tCurrent = new CurrentForecast();
            tCurrent.setType(IForecastPeriod.Type.Current);
            tCurrent.setCloudCover(IForecastPeriod.sNotSupported);
            tCurrent.setCode(GetCodeFromName(wWeather.getCurrentCondition("IconName"), IForecastPeriod.Type.Current));
            tCurrent.setCondition(wWeather.getCurrentCondition("Description"));

            Date TodaysDate = new Date(Long.parseLong(wWeather.getCurrentCondition("ObservationTime")) * 1000);
            log.info("update: Observation Date '" + TodaysDate + "'");

            tCurrent.setDate(TodaysDate);
            tCurrent.setDescription(IForecastPeriod.sNotSupported);
            if (wWeather.getCurrentCondition("Dewpoint").equals(NAText)) {
                tCurrent.setDewPoint(IForecastPeriod.sInvalid);
            } else {
                tCurrent.setDewPoint(wWeather.getCurrentCondition("Dewpoint"));
            }
            if (wWeather.getCurrentCondition("FeelsLike").equals(NAText)) {
                tCurrent.setFeelsLike(IForecastPeriod.iInvalid);
            } else {
                Double dFL = NumberUtils.toDouble(wWeather.getCurrentCondition("FeelsLike"), IForecastPeriod.iInvalid);
                tCurrent.setFeelsLike(dFL.intValue());
            }
            if (wWeather.getCurrentCondition("Humidity").equals(NAText)) {
                tCurrent.setHumid(IForecastPeriod.iInvalid);
            } else {
                String tHumid = wWeather.getCurrentCondition("Humidity").replaceAll("%", "").trim();
                tCurrent.setHumid(NumberUtils.toInt(tHumid, IForecastPeriod.iInvalid));
            }
            tCurrent.setPrecip(formatPrecip(wWeather.getCurrentCondition("PrecipToday")));
            if (wWeather.getCurrentCondition("Temp").equals(NAText)) {
                tCurrent.setTemp(IForecastPeriod.iInvalid);
            } else {
                Double dTemp = NumberUtils.toDouble(wWeather.getCurrentCondition("Temp"), IForecastPeriod.iInvalid);
                tCurrent.setTemp(dTemp.intValue());
            }
            if (wWeather.getCurrentCondition("Pressure").equals(NAText)) {
                tCurrent.setPressure(IForecastPeriod.sInvalid);
            } else {
                tCurrent.setPressure(wWeather.getCurrentCondition("Pressure") + " " + wWeather.getPressureUnit(wWeather.getUnits()));
            }
            if (wWeather.getCurrentCondition("PressureTrend").equals("+")) {
                tCurrent.setPressureDir(1);
            } else if (wWeather.getCurrentCondition("PressureTrend").equals("-")) {
                tCurrent.setPressureDir(-1);
            } else {
                tCurrent.setPressureDir(0);
            }
            tCurrent.setSunrise(formatSunriseSunset(wWeather.getAstronomyInfo("SunriseHour"),
                    wWeather.getAstronomyInfo("SunriseMinute")));
            tCurrent.setSunset(formatSunriseSunset(wWeather.getAstronomyInfo("SunsetHour"),
                    wWeather.getAstronomyInfo("SunsetMinute")));

            if (wWeather.getCurrentCondition("UV").equals(NAText)) {
                tCurrent.setUVIndex(IForecastPeriod.sInvalid);
            } else {
                tCurrent.setUVIndex(wWeather.getCurrentCondition("UV"));
            }
            tCurrent.setUVWarn(IForecastPeriod.sNotSupported);

            if (wWeather.getCurrentCondition("Visibility").equals(NAText)) {
                tCurrent.setVisibility(IForecastPeriod.iInvalid);
            } else {
                Double dVis = NumberUtils.toDouble(wWeather.getCurrentCondition("Visibility"), IForecastPeriod.iInvalid);
                tCurrent.setVisibility(dVis.intValue());
            }
            // get the Wind Values
            Double dWind = NumberUtils.toDouble(wWeather.getCurrentCondition("WindSpeed"), IForecastPeriod.iInvalid);
            int WindSpeed = dWind.intValue();
            if (WindSpeed == 0 || WindSpeed == IForecastPeriod.iInvalid) {
                tCurrent.setWindSpeed(0);
                tCurrent.setWindDir(IForecastPeriod.iInvalid);
                tCurrent.setWindDirText(IForecastPeriod.WindCalm);
            } else {
                tCurrent.setWindDirText(formatCompassDirectionText(wWeather.getCurrentCondition("WindDir").trim()));
                tCurrent.setWindDir(formatCompassDirection(tCurrent.getWindDirText()));
                tCurrent.setWindSpeed(WindSpeed);
            }
            current = tCurrent;

            // populate the long range forecast days
            forecast = new ArrayList<ILongRangeForecast>();
            LongRangForecast tForcast = new LongRangForecast();
            // go through each period to add to the long range forecast
            int currentDay = 0;
            int numPeriods = wWeather.get12hrForecastEndPeriod() - wWeather.get12hrForecastStartPeriod() + 1;
            for (int i = wWeather.get12hrForecastStartPeriod(); i <= wWeather.get12hrForecastEndPeriod(); i++) {
                currentDay = (i / 2) + 1;
                boolean isDay = (i % 2 == 0);
                ForecastPeriod tPeriod = new ForecastPeriod();
                Date thisDay = GetDayDate(TodaysDate, currentDay - 1);
                tPeriod.setDate(thisDay);
                tPeriod.setDescription(wWeather.get12hrForecast(i, "FCText"));
                tPeriod.setCondition(wWeather.get24hrForecast(currentDay, "Conditions"));

                if (wWeather.get12hrForecast(i, "ChancePrecip").equals(NAText)) {
                    tPeriod.setPrecip(IForecastPeriod.sInvalid);
                } else if (wWeather.get12hrForecast(i, "ChancePrecip").equals("0")) {
                    tPeriod.setPrecip("");
                } else {
                    tPeriod.setPrecip(wWeather.get12hrForecast(i, "ChancePrecip") + " %");
                }

                if (wWeather.get24hrForecast(currentDay, "HumidAvg").equals(NAText)) {
                    tPeriod.setHumid(IForecastPeriod.iInvalid);
                } else {
                    tPeriod.setHumid(NumberUtils.toInt(wWeather.get24hrForecast(currentDay, "HumidAvg"), IForecastPeriod.iInvalid));
                }

                // get the Wind Values
                WindSpeed = NumberUtils.toInt(wWeather.get24hrForecast(currentDay, "WindAvgSpeed"), IForecastPeriod.iInvalid);
                if (WindSpeed == 0 || WindSpeed == IForecastPeriod.iInvalid) {
                    tPeriod.setWindSpeed(0);
                    tPeriod.setWindDir(IForecastPeriod.iInvalid);
                    tPeriod.setWindDirText(IForecastPeriod.WindCalm);
                } else {
                    tPeriod.setWindDirText(formatCompassDirectionText(wWeather.get24hrForecast(currentDay, "WindAvgDir").trim()));
                    tPeriod.setWindDir(formatCompassDirection(tPeriod.getWindDirText()));
                    tPeriod.setWindSpeed(WindSpeed);
                }

                if (isDay) { // is a Day
                    if (wWeather.get24hrForecast(currentDay, "High").equals(NAText)) {
                        tPeriod.setTemp(IForecastPeriod.iInvalid);
                    } else {
                        Double dTemp = NumberUtils.toDouble(wWeather.get24hrForecast(currentDay, "High"), IForecastPeriod.iInvalid);
                        tPeriod.setTemp(dTemp.intValue());
                    }
                    tPeriod.setCode(GetCodeFromName(wWeather.get12hrForecast(i, "IconName"), IForecastPeriod.Type.Day));
                    tPeriod.setType(IForecastPeriod.Type.Day);
                    tForcast.setForecastPeriodDay(tPeriod);
                    if (i == wWeather.get12hrForecastEndPeriod()) {
                        // last period so there is no night so add to the
                        // forcast
                        tForcast.setForecastPeriodNight(null);
                        forecast.add(tForcast);
                    }
                } else { // is a Night
                    // if the first period is a night then we need to
                    // add a null Day
                    if (i == 0) {
                        tForcast.setForecastPeriodDay(null);
                    }
                    if (wWeather.get24hrForecast(currentDay, "Low").equals(NAText)) {
                        tPeriod.setTemp(IForecastPeriod.iInvalid);
                    } else {
                        Double dTemp = NumberUtils.toDouble(wWeather.get24hrForecast(currentDay, "Low"), IForecastPeriod.iInvalid);
                        tPeriod.setTemp(dTemp.intValue());
                    }
                    tPeriod.setCode(GetCodeFromName(wWeather.get12hrForecast(i, "IconName"), IForecastPeriod.Type.Night));
                    tPeriod.setType(IForecastPeriod.Type.Night);
                    tForcast.setForecastPeriodNight(tPeriod);
                    forecast.add(tForcast);
                    tForcast = new LongRangForecast();
                }
            }
            return true;
        }
        return false;
    }

    private String formatSunriseSunset(String HH, String MM) {
        if (HH.endsWith(NAText) || MM.equals(NAText)) {
            return IForecastPeriod.sInvalid;
        }
        SimpleDateFormat DFormat24 = new SimpleDateFormat("H:mm");
        DateFormat LocalTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        try {
            return LocalTimeFormat.format(DFormat24.parse(HH + ":" + MM));
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(WundergroundWeatherSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IForecastPeriod.sInvalid;
    }

    /**
     * Returns the Larger of the configured Updated Interval vs the Weather's TTL
     */
    private int getTTLInSeconds() {
        int ttl1=ttl * 60;
        int ttl2=config.getUpdateInterval();
        return Math.max(ttl1,ttl2);
    }

    private boolean shouldUpdate() {
        if (lastUpdated == null)
            return true;
        if (wWeather.isCurrentlyUpdating()) return false;
        long later = lastUpdated.getTime() + (getTTLInSeconds() * 1000);
        if (System.currentTimeMillis() > later)
            return true;
        log.debug("shouldUpdate: Not time to perform an update. Last update at '" + lastUpdated + "'");
        return false;
    }

    @Override
    public boolean setLocation(String location) {
        wWeather.setWeatherLocCode(location);
        config.setLocation(location);
        lastUpdated = null;
        return true;
    }

    @Override
    public String getLocation() {
        Map locMap = wWeather.getWeatherLoc();
        String loc = wWeather.getLocationCode(locMap);
        if (loc==null||loc.isEmpty()) loc = config.getLocation();
        return loc;
    }

    @Override
    public void removeLocation() {
        wWeather.removeWeatherLoc();
        config.setLocation(null);
    }

    @Override
    public String getLocationName() {
        return wWeather.getCurrentCondition("DisplayLocFull");
    }

    @Override
    public void setUnits(Units u) {
        if (u == null || u == Units.Metric) {
            wWeather.setUnits("m");
            config.setUnits("m");
        } else {
            wWeather.setUnits("s");
            config.setUnits("s");
        }
        // reset the last updated to force an update on the next update call
        lastUpdated = null;
    }

    @Override
    public Units getUnits() {
        String u = wWeather.getUnits();
        if (StringUtils.isEmpty(u) || u.toLowerCase().startsWith("m")) {
            return Units.Metric;
        } else {
            return Units.Standard;
        }
    }

    @Override
    public ICurrentForecast getCurrentWeather() {
        return current;
    }

    @Override
    public List<ILongRangeForecast> getForecasts() {
        return forecast;
    }

    @Override
    public int getForecastDays() {
        if (forecast == null)
            return 0;
        return forecast.size();
    }

    @Override
    public boolean isConfigured() {
        return !StringUtils.isEmpty(getLocation());
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

    private String formatCompassDirectionText(String compass) {
        if (compass.toLowerCase().equals("north")) {
            return "N";
        } else if (compass.toLowerCase().equals("south")) {
            return "S";
        } else if (compass.toLowerCase().equals("east")) {
            return "E";
        } else if (compass.toLowerCase().equals("west")) {
            return "W";
        } else {
            return compass;
        }
    }

    private int formatCompassDirection(String compass) {
        List dirList = new ArrayList();
        String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW",
                "NW", "NNW"};
        Collections.addAll(dirList, directions);
        int index = dirList.indexOf(compass);
        return (int) (index * 22.5);
    }

    private Date GetDayDate(Date Day, Integer OffSet) {
        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(Day);
        thisDay.add(Calendar.DATE, OffSet);
        return thisDay.getTime();
    }

    private String formatPrecip(String wwPrecip) {
        if (wwPrecip.equals(NAText)) {
            return "";
        }
        Float precip = NumberUtils.toFloat(wwPrecip, 0.0f);
        String retVal = "";
        if (precip == 0 || precip == 0.0) {
            return "";
        } else {
            return precip.toString() + " " + wWeather.getRainUnit(wWeather.getUnits());
        }
    }
}
