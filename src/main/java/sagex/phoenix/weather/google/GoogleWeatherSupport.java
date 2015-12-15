package sagex.phoenix.weather.google;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sage.google.weather.GoogleWeather;
import sagex.UIContext;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.IForecastPeriod.Type;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Google/NWS implementation for Phoenix By default all weather info is
 * retrieved from the Google Source IF the NWS service is configured, then long
 * range forecast details will be from NWS otherwise those details that Google
 * provides will be available
 *
 * @author jusjoken
 */
public class GoogleWeatherSupport implements IWeatherSupport2 {
    private Logger log = Logger.getLogger(this.getClass());
    private GoogleWeather gWeather = GoogleWeather.getInstance();
    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 45; // update every 45 mins
    private String error;
    private CurrentForecast current = null;
    private List<ILongRangeForecast> forecast;
    private HashMap<String, String> CodesForDaytime = new HashMap<String, String>();
    private HashMap<String, String> CodesForNighttime = new HashMap<String, String>();

    public GoogleWeatherSupport() {
        BuildWeatherIconLists();
    }

    private void BuildWeatherIconLists() {
        // start google Icons here
        AddIcon("sunny", "32", "31");
        AddIcon("mostly_sunny", "34", "33");
        AddIcon("partly_cloudy", "30", "29");
        AddIcon("mostly_cloudy", "28", "27");
        AddIcon("chance_of_storm", "37", "47");
        AddIcon("rain", "12", "12");
        AddIcon("chance_of_rain", "39", "45");
        AddIcon("chance_of_snow", "41", "46");
        AddIcon("cloudy", "26", "26");
        AddIcon("mist", "11", "11");
        AddIcon("storm", "35", "35");
        AddIcon("thunderstorm", "35", "35");
        AddIcon("chance_of_tstorm", "37", "47");
        AddIcon("sleet", "5", "5");
        AddIcon("snow", "16", "16");
        AddIcon("icy", "10", "10");
        AddIcon("dust", "19", "19");
        AddIcon("fog", "20", "20");
        AddIcon("smoke", "22", "22");
        AddIcon("haze", "21", "21");
        AddIcon("flurries", "14", "14");
        // start NWS Icons here - those begining with a "n" are the night icons
        // (if one exists)
        // NWS is only used for Forecasts - so only the "day" or first entry is
        // used
        AddIcon("nbkn", "27", "27");
        AddIcon("bkn", "28", "27");
        AddIcon("nra", "12", "12");
        AddIcon("ra", "12", "12");
        AddIcon("nskc", "31", "31");
        AddIcon("skc", "32", "31");
        AddIcon("nfew", "33", "33");
        AddIcon("few", "34", "33");
        AddIcon("nsct", "29", "29");
        AddIcon("sct", "30", "29");
        AddIcon("hi_nshwrs", "45", "45");
        AddIcon("hi_shwrs", "39", "45");
        AddIcon("novc", "26", "26");
        AddIcon("ovc", "26", "26");
        AddIcon("nrasn", "46", "46");
        AddIcon("rasn", "41", "46");
        AddIcon("sn", "16", "16");
        AddIcon("nsn", "16", "16");
        AddIcon("ntsra", "35", "35");
        AddIcon("tsra", "35", "35");
        AddIcon("nscttsra", "35", "35");
        AddIcon("scttsra", "35", "35");
        AddIcon("hi_tsra", "37", "37");
        AddIcon("hi_ntsra", "47", "47");
        AddIcon("nwind", "23", "23");
        AddIcon("wind", "24", "23");
        AddIcon("sctfg", "20", "20");
        AddIcon("nfg", "20", "20");
        AddIcon("fg", "20", "20");
        AddIcon("cold", "15", "15");
        AddIcon("blizzard", "43", "43");
        AddIcon("ntor", "24", "24");
        AddIcon("tor", "23", "24");
        AddIcon("fzra", "5", "5");
        AddIcon("du", "19", "19");
        AddIcon("nshra", "12", "12");
        AddIcon("shra", "12", "12");
        AddIcon("nfu", "22", "22");
        AddIcon("fu", "22", "22");
        AddIcon("hot", "36", "31");
    }

    private void AddIcon(String CodeSource, String CodeForDay, String CodeForNight) {
        CodesForDaytime.put(CodeSource, CodeForDay);
        CodesForNighttime.put(CodeSource, CodeForNight);
    }

    private Integer GetCodeFromURL(String ConditionURL, Type DayType) {
        // log.info("GetCodeFromURL: ConditionURL '" + ConditionURL +
        // "' DayType '" + DayType + "'");
        String Condition = "";
        Integer tIcon = ConditionURL.lastIndexOf("/");
        if (tIcon == -1) {
            return -1;
        } else {
            Condition = ConditionURL.substring(tIcon + 1);
            Condition = Condition.replaceAll(".gif", "");
            Condition = Condition.replaceAll("." + ImageUtil.EXT_JPG, "");
            Condition = Condition.replaceAll("." + ImageUtil.EXT_PNG, "");
            // remove any % that are part of the image string
            if (Condition.contains("0")) {
                Condition = Condition.replaceAll("10", "");
                Condition = Condition.replaceAll("20", "");
                Condition = Condition.replaceAll("30", "");
                Condition = Condition.replaceAll("40", "");
                Condition = Condition.replaceAll("50", "");
                Condition = Condition.replaceAll("60", "");
                Condition = Condition.replaceAll("70", "");
                Condition = Condition.replaceAll("80", "");
                Condition = Condition.replaceAll("90", "");
            }
            if (DayType.equals(Type.Current) || DayType.equals(Type.Day)) { // return
                // the
                // Day
                // Code
                if (CodesForDaytime.containsKey(Condition)) {
                    // log.info("GetCodeFromURL: returning '" +
                    // phoenix.util.ToInt(CodesForDaytime.get(Condition),-1) +
                    // "'");
                    return phoenix.util.ToInt(CodesForDaytime.get(Condition), -1);
                } else {
                    return -1;
                }
            } else { // return the Night Code
                if (CodesForNighttime.containsKey(Condition)) {
                    // log.info("GetCodeFromURL: returning '" +
                    // phoenix.util.ToInt(CodesForNighttime.get(Condition),-1) +
                    // "'");
                    return phoenix.util.ToInt(CodesForNighttime.get(Condition), -1);
                } else {
                    return -1;
                }
            }
        }
    }

    @Override
    public String getSourceName() {
        if (isNWSConfigured())
            return "Google-NWS Weather";
        else
            return "Google Weather";
    }

    @Override
    public boolean update() {
        error = null;

        if (!isConfigured()) {
            error = "Please configure the Google location";
            return false;
        }

        if (shouldUpdate()) {

            UIContext tUI = new UIContext(sagex.api.Global.GetUIContextName());
            String LangCode = sagex.api.Configuration.GetProperty(tUI, "ui/translation_language_code", "en");
            boolean isNWS = isNWSConfigured();
            if (isNWS) {
                log.info("Getting Google Weather for '" + gWeather.getGoogleWeatherLoc() + "' and NWS Weather for '"
                        + gWeather.getNWSZipCode() + "'");
                gWeather.updateAllNow(LangCode);
            } else {
                log.info("Getting Google Weather for '" + gWeather.getGoogleWeatherLoc() + "'");
                gWeather.updateGoogleNow(LangCode);
            }
            lastUpdated = new Date(System.currentTimeMillis());
            recordedDate = new Date(gWeather.getLastUpdateTime());

            // populate current forecast
            CurrentForecast tCurrent = new CurrentForecast();
            tCurrent.setType(Type.Current);
            tCurrent.setCloudCover(IForecastPeriod.sNotSupported);
            tCurrent.setCode(GetCodeFromURL(gWeather.getGWCurrentCondition("iconURL"), Type.Current));
            tCurrent.setCondition(gWeather.getGWCurrentCondition("CondText"));

            Date TodaysDate = GetCurrentDate();
            tCurrent.setDate(TodaysDate);
            tCurrent.setDescription(IForecastPeriod.sNotSupported);
            tCurrent.setDewPoint(IForecastPeriod.sNotSupported);
            tCurrent.setFeelsLike(IForecastPeriod.iNotSupported);

            String tHumid = gWeather.getGWCurrentCondition("HumidText").replaceAll("Humidity:", "").replaceAll("%", "").trim();
            tCurrent.setHumid(NumberUtils.toInt(tHumid, IForecastPeriod.iInvalid));
            tCurrent.setPrecip(IForecastPeriod.sNotSupported);
            tCurrent.setTemp(NumberUtils.toInt(gWeather.getGWCurrentCondition("Temp"), IForecastPeriod.iInvalid));
            tCurrent.setPressure(IForecastPeriod.sNotSupported);
            tCurrent.setPressureDir(IForecastPeriod.iNotSupported);
            tCurrent.setSunrise(IForecastPeriod.sNotSupported);
            tCurrent.setSunset(IForecastPeriod.sNotSupported);
            tCurrent.setUVIndex(IForecastPeriod.sNotSupported);
            tCurrent.setUVWarn(IForecastPeriod.sNotSupported);
            tCurrent.setVisibility(IForecastPeriod.iNotSupported);
            // get the Wind Values
            String tWind = gWeather.getGWCurrentCondition("WindText");
            if (tWind.contains("0 mph")) {
                tCurrent.setWindSpeed(0);
                tCurrent.setWindDir(IForecastPeriod.iInvalid);
                tCurrent.setWindDirText(IForecastPeriod.WindCalm);
            } else {
                tWind = tWind.replaceAll("Wind:", "").replaceAll("mph", "").trim();
                tCurrent.setWindDirText(tWind.substring(0, tWind.indexOf(" ")));
                tCurrent.setWindDir(formatCompassDirection(tCurrent.getWindDirText()));
                int WindSpeed = NumberUtils.toInt(tWind.substring(tWind.indexOf("at ") + 3), IForecastPeriod.iInvalid);
                if (WindSpeed == 0) {
                    tCurrent.setWindDir(IForecastPeriod.iInvalid);
                    tCurrent.setWindDirText(IForecastPeriod.WindCalm);
                } else {
                    if (getUnits().equals(Units.Standard)) {
                        tCurrent.setWindSpeed(WindSpeed);
                    } else {
                        // Google always provides mph speeds so as Units is
                        // Metric then convert to k/h
                        tCurrent.setWindSpeed((int) (WindSpeed * 1.6));
                    }
                }
            }
            current = tCurrent;

            // populate the long range forecast days
            forecast = new ArrayList<ILongRangeForecast>();
            LongRangForecast tForcast = new LongRangForecast();
            if (isNWS) { // handle the NWS Forecasts
                // go through each period to add to the long range forecast
                int currentDay = 0;
                for (int i = 0; i < gWeather.getNWSPeriodCount(); i++) {
                    ForecastPeriod tPeriod = new ForecastPeriod();
                    tPeriod.setCondition(gWeather.getNWSForecastCondition(i, "summary"));
                    Date thisDay = GetDayDate(TodaysDate, currentDay);
                    tPeriod.setDate(thisDay);
                    tPeriod.setDescription(gWeather.getNWSForecastCondition(i, "forecast_text"));
                    if (gWeather.getNWSForecastCondition(i, "precip").equals("0")) {
                        tPeriod.setPrecip("");
                    } else {
                        tPeriod.setPrecip(gWeather.getNWSForecastCondition(i, "precip") + " %");
                    }
                    tPeriod.setTemp(NumberUtils.toInt(gWeather.getNWSForecastCondition(i, "temp"), IForecastPeriod.iInvalid));
                    tPeriod.setHumid(IForecastPeriod.iNotSupported);
                    tPeriod.setWindDir(IForecastPeriod.iNotSupported);
                    tPeriod.setWindDirText(IForecastPeriod.sNotSupported);
                    tPeriod.setWindSpeed(IForecastPeriod.iNotSupported);
                    if (gWeather.getNWSForecastCondition(i, "tempType").equals("h")) { // h
                        // is
                        // a
                        // Day
                        tPeriod.setCode(GetCodeFromURL(gWeather.getNWSForecastCondition(i, "icon_url"), Type.Day));
                        tPeriod.setType(Type.Day);
                        tForcast.setForecastPeriodDay(tPeriod);
                        if (i == gWeather.getNWSPeriodCount() - 1) {
                            // last period so there is no night so add to the
                            // forcast
                            tForcast.setForecastPeriodNight(null);
                            forecast.add(tForcast);
                        }
                    } else { // l is a Night
                        // if the first period is a night then we need
                        // to add a null Day
                        if (i == 0) {
                            tForcast.setForecastPeriodDay(null);
                        }
                        tPeriod.setType(Type.Night);
                        tPeriod.setCode(GetCodeFromURL(gWeather.getNWSForecastCondition(i, "icon_url"), Type.Night));
                        tForcast.setForecastPeriodNight(tPeriod);
                        currentDay++;
                        forecast.add(tForcast);
                        tForcast = new LongRangForecast();
                    }
                }
            } else { // handle the Google forecasts
                for (int i = 0; i < gWeather.getGWDayCount(); i++) {
                    // handle the day period
                    ForecastPeriod tPeriod = null;
                    Date thisDay = GetDayDate(TodaysDate, i);
                    if (!gWeather.getGWForecastCondition(i, "high").equals("N/A")) {
                        tPeriod = new ForecastPeriod();
                        tPeriod.setCode(GetCodeFromURL(gWeather.getGWForecastCondition(i, "iconURL"), Type.Day));
                        tPeriod.setCondition(gWeather.getGWForecastCondition(i, "CondText"));
                        tPeriod.setDate(thisDay);
                        tPeriod.setTemp(NumberUtils.toInt(gWeather.getGWForecastCondition(i, "high"), IForecastPeriod.iInvalid));
                        tPeriod.setType(Type.Day);
                        tPeriod.setDescription(IForecastPeriod.sNotSupported);
                        tPeriod.setHumid(IForecastPeriod.iNotSupported);
                        tPeriod.setPrecip(IForecastPeriod.sNotSupported);
                        tPeriod.setWindDir(IForecastPeriod.iNotSupported);
                        tPeriod.setWindDirText(IForecastPeriod.sNotSupported);
                        tPeriod.setWindSpeed(IForecastPeriod.iNotSupported);
                    }
                    tForcast.setForecastPeriodDay(tPeriod);

                    // handle the night period
                    tPeriod = new ForecastPeriod();
                    tPeriod.setCode(phoenix.weather2.GetCodeForceNight(GetCodeFromURL(
                            gWeather.getGWForecastCondition(i, "iconURL"), Type.Night)));
                    tPeriod.setCondition(gWeather.getGWForecastCondition(i, "CondText"));
                    tPeriod.setDate(thisDay);
                    tPeriod.setTemp(NumberUtils.toInt(gWeather.getGWForecastCondition(i, "low"), IForecastPeriod.iInvalid));
                    tPeriod.setType(Type.Night);
                    tPeriod.setDescription(IForecastPeriod.sNotSupported);
                    tPeriod.setHumid(IForecastPeriod.iNotSupported);
                    tPeriod.setPrecip(IForecastPeriod.sNotSupported);
                    tPeriod.setWindDir(IForecastPeriod.iNotSupported);
                    tPeriod.setWindDirText(IForecastPeriod.sNotSupported);
                    tPeriod.setWindSpeed(IForecastPeriod.iNotSupported);
                    tForcast.setForecastPeriodNight(tPeriod);

                    // add the forecast for the day
                    forecast.add(tForcast);
                    tForcast = new LongRangForecast();

                }
            }

            return true;
        }
        return false;
    }

    private Date GetCurrentDate() {
        // determine the date as Google does not provide one
        Calendar now = Calendar.getInstance();
        String tDayName = gWeather.getGWForecastCondition(0, "name").toLowerCase();
        if (tDayName == null) {
            return now.getTime();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE");
        String dayName = dateFormat.format(now.getTime()).toLowerCase();
        if (dayName.equals(tDayName)) {
            return now.getTime();
        }
        // check yesterday
        now.add(Calendar.DATE, -1);
        dayName = dateFormat.format(now.getTime()).toLowerCase();
        if (dayName.equals(tDayName)) {
            return now.getTime();
        }
        // check tomorrow
        now.add(Calendar.DATE, 2);
        dayName = dateFormat.format(now.getTime()).toLowerCase();
        if (dayName.equals(tDayName)) {
            return now.getTime();
        }
        // the date was not found so we will go back until we find a match
        // reset the date to yesterday
        now.add(Calendar.DATE, -2);
        int loop = 0;
        do {
            // check the previous day for a match
            now.add(Calendar.DATE, -1);
            dayName = dateFormat.format(now.getTime()).toLowerCase();
            if (dayName.equals(tDayName)) {
                return now.getTime();
            }
            loop++;
        } while (loop < 7);
        // if we get here then we did not find a match... so just return the
        // last day we checked.
        return now.getTime();
    }

    private Date GetDayDate(Date Day, Integer OffSet) {
        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(Day);
        thisDay.add(Calendar.DATE, OffSet);
        return thisDay.getTime();
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

    @Override
    public boolean setLocation(String location) {
        gWeather.setGoogleWeatherLoc(location);
        // attempt to set the NWS location as well
        log.debug("setLocation: attempting set of NWS location to '" + location + "'");
        if (isValidZIP(location)) {
            log.debug("setLocation: setting NWS to '" + location + "'");
            gWeather.setNWSZipCode(location);
        } else {
            gWeather.removeNWSZipCode();
        }
        // reset the last updated to force an update on the next update call
        lastUpdated = null;
        return true;
    }

    private boolean isValidZIP(String ZIPCode) {
        // String regex = "^\\d{5}(-\\d{4})?$";
        String regex = "^\\d{5}";
        log.debug("isValidZIP: checking for ZIP '" + ZIPCode + "'");
        return Pattern.matches(regex, ZIPCode);
    }

    @Override
    public String getLocation() {
        return gWeather.getGoogleWeatherLoc();
    }

    @Override
    public void removeLocation() {
        gWeather.removeGoogleWeatherLoc();
        gWeather.removeNWSZipCode();
    }

    @Override
    public String getLocationName() {
        String tLoc = gWeather.getGWCityName();
        if (tLoc == null)
            return "";
        tLoc = tLoc.substring(0, tLoc.indexOf(","));
        return tLoc;
    }

    @Override
    public void setUnits(Units u) {
        if (u == null || u == Units.Metric) {
            gWeather.setUnits("m");
        } else {
            gWeather.setUnits("s");
        }
        // reset the last updated to force an update on the next update call
        lastUpdated = null;
    }

    @Override
    public Units getUnits() {
        String u = gWeather.getUnits();
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

    public boolean isConfigured() {
        return !StringUtils.isEmpty(getLocation());
    }

    public boolean isNWSConfigured() {
        return !StringUtils.isEmpty(gWeather.getNWSZipCode());
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

    private int formatCompassDirection(String compass) {
        List dirList = new ArrayList();
        String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW",
                "NW", "NNW"};
        Collections.addAll(dirList, directions);
        int index = dirList.indexOf(compass);
        return (int) (index * 22.5);
    }

}
