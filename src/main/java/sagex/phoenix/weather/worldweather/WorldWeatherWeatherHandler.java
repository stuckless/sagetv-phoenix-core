package sagex.phoenix.weather.worldweather;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.IForecastPeriod.Type;
import sagex.phoenix.weather.IWeatherSupport2.Units;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * @author jusjoken
 */
public class WorldWeatherWeatherHandler extends BaseBuilder {
    private enum State {
        Current, LongRange
    }

    private static HashMap<Integer, Integer> codeMap = new HashMap<Integer, Integer>();

    // map the WorldWeather weather code to an icon code
    static {
        codeMap.put(395, 4);
        codeMap.put(392, 35);
        codeMap.put(389, 4);
        codeMap.put(386, 35);
        codeMap.put(377, 7);
        codeMap.put(374, 6);
        codeMap.put(371, 14);
        codeMap.put(368, 13);
        codeMap.put(365, 7);
        codeMap.put(362, 5);
        codeMap.put(359, 12);
        codeMap.put(356, 11);
        codeMap.put(353, 9);
        codeMap.put(350, 18);
        codeMap.put(338, 16);
        codeMap.put(335, 16);
        codeMap.put(332, 14);
        codeMap.put(329, 14);
        codeMap.put(326, 13);
        codeMap.put(323, 13);
        codeMap.put(320, 7);
        codeMap.put(317, 8);
        codeMap.put(314, 7);
        codeMap.put(311, 8);
        codeMap.put(308, 12);
        codeMap.put(305, 12);
        codeMap.put(302, 11);
        codeMap.put(299, 11);
        codeMap.put(296, 9);
        codeMap.put(293, 9);
        codeMap.put(284, 7);
        codeMap.put(281, 8);
        codeMap.put(266, 9);
        codeMap.put(263, 9);
        codeMap.put(260, 20);
        codeMap.put(248, 20);
        codeMap.put(230, 43);
        codeMap.put(227, 43);
        codeMap.put(200, 37);
        codeMap.put(185, 18);
        codeMap.put(182, 18);
        codeMap.put(179, 18);
        codeMap.put(176, 9);
        codeMap.put(143, 9);
        codeMap.put(122, 26);
        codeMap.put(119, 28);
        codeMap.put(116, 30);
        codeMap.put(113, 32);
    }

    private Logger log = Logger.getLogger(this.getClass());
    private String city, region, country;
    // private String tempC, tempF;
    // private String windChill, windSpeed, windDirection;
    // private String humidity, visibility, pressure, rising;
    // private String sunrise, sunset;
    private Date recordedDate;
    private CurrentForecast current = new CurrentForecast();
    private List<ILongRangeForecast> longRange = new ArrayList<ILongRangeForecast>();
    private LongRangForecast currentLongRange = null;
    private State state = State.Current;
    private Units units = null;

    public WorldWeatherWeatherHandler(Units units) {
        super(WorldWeatherWeatherHandler.class.getSimpleName());
        // we need the units since we have to know whether or not to
        // grab metric elements or standard elements
        this.units = units;
    }

    public CurrentForecast getCurrentWeather() {
        return current;
    }

    public List<ILongRangeForecast> getLongRangeForecast() {
        return longRange;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attr) {
        // log.info("startElement: string '" + string + "' string1 '" + string1
        // + "' elName '" + elName + "' atrbts '" + attr + "'");
        if ("current_condition".equals(name)) {
            // set the state to be Current
            state = State.Current;
            current.setType(Type.Current);
            current.setDescription(IForecastPeriod.sNotSupported);
            current.setDewPoint(IForecastPeriod.sNotSupported);
            current.setFeelsLike(IForecastPeriod.iNotSupported);
            current.setPressureDir(IForecastPeriod.iNotSupported);
            current.setSunrise(IForecastPeriod.sNotSupported);
            current.setSunset(IForecastPeriod.sNotSupported);
            current.setUVIndex(IForecastPeriod.sNotSupported);
            current.setUVWarn(IForecastPeriod.sNotSupported);
        } else if ("weather".equals(name)) {
            // create a local weather object and add it to the
            // long range forecast... it'll get filled
            // eventually. Also set the state to be LongRange
            // so that we begin update the long range data
            currentLongRange = new LongRangForecast();
            currentLongRange.setForecastPeriodDay(new ForecastPeriod());
            currentLongRange.getForecastPeriodDay().setType(Type.Day);
            currentLongRange.getForecastPeriodDay().setDescription(IForecastPeriod.sNotSupported);
            currentLongRange.getForecastPeriodDay().setHumid(IForecastPeriod.iNotSupported);
            currentLongRange.setForecastPeriodNight(new ForecastPeriod());
            currentLongRange.getForecastPeriodNight().setType(Type.Night);
            currentLongRange.getForecastPeriodNight().setDescription(IForecastPeriod.sNotSupported);
            currentLongRange.getForecastPeriodNight().setHumid(IForecastPeriod.iNotSupported);
            longRange.add(currentLongRange);
            state = State.LongRange;
        } else {
            // log.info("startElement: elName '" + name + "' atrbts '" + attr +
            // "'");
        }
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

    public Date getRecordedDate() {
        return recordedDate;
    }

    @Override
    public void endElement(String uri, String localName, String name) {
        // log.info("endElement: uri '" + uri + "' string1 '" + localName +
        // "' string2 '" + name + "'");

        // because most of the data that we want is not in attributes,
        // we have to wait until the end element to grab the text.
        // ie, for the element <areaName><![CDATA[London]]></areaName>,
        // startElement(areaName) will be called, then the data in the
        // element will be collected, and then finally, endElement(areaName)
        // will be called. So the data, "London", is not accessible until
        // we get to the endElement handler. Typically for
        // xml parsing, attributes are nice, but they some limitations

        // because you are extending the BaseBuilder (in Phoenix) it
        // handles collecting the data in the element, and you just
        // need to call getData() to get the element's data.

        if ("areaName".equals(name)) {
            city = getData();
        } else if ("region".equals(name)) {
            region = getData();
        } else if ("country".equals(name)) {
            country = getData();
        } else if ("localObsDateTime".equals(name)) {
            recordedDate = DateUtils.parseDate(getData());
        } else if ("temp_F".equals(name) && units == Units.Standard) {
            current.setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("temp_C".equals(name) && units == Units.Metric) {
            current.setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("weatherCode".equals(name) && state == State.Current) {
            current.setCode(convertCode(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid)));
        } else if ("weatherDesc".equals(name) && state == State.Current) {
            current.setCondition(getData());
        } else if ("windspeedMiles".equals(name) && state == State.Current && units == Units.Standard) {
            // because windsppedMiles is used current weather elements
            // and the long range weather elements, then we need to use the
            // State variable check to update either current weather
            // or the long rang weather
            current.setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("windspeedKmph".equals(name) && state == State.Current && units == Units.Metric) {
            current.setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("winddir16Point".equals(name) && state == State.Current) {
            current.setWindDirText(getData());
        } else if ("winddirDegree".equals(name) && state == State.Current) {
            current.setWindDir(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("precipMM".equals(name) && state == State.Current) {
            current.setPrecip(formatPrecip(getData()));
        } else if ("humidity".equals(name)) {
            current.setHumid(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("visibility".equals(name)) {
            current.setVisibility(formatVisibility(getData()));
        } else if ("pressure".equals(name)) {
            current.setPressure(formatPressure(getData()));
        } else if ("cloudcover".equals(name)) {
            current.setCloudCover(getData() + " %");
        } else if ("current_condition".equals(name)) {
            // final validation on current forecast items as needed
            if (current.getWindSpeed() == 0) {
                current.setWindDir(IForecastPeriod.iInvalid);
                current.setWindDirText(IForecastPeriod.WindCalm);
            }
        } else if ("date".equals(name) && state == State.LongRange) {
            if (current.getDate() == null) {
                current.setDate(convertDate(getData()));
            }
            currentLongRange.getForecastPeriodDay().setDate(convertDate(getData()));
            currentLongRange.getForecastPeriodNight().setDate(convertDate(getData()));
        } else if ("tempMaxF".equals(name) && units == Units.Standard) {
            currentLongRange.getForecastPeriodDay().setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("tempMaxC".equals(name) && units == Units.Metric) {
            currentLongRange.getForecastPeriodDay().setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("tempMinF".equals(name) && units == Units.Standard) {
            currentLongRange.getForecastPeriodNight().setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("tempMinC".equals(name) && units == Units.Metric) {
            currentLongRange.getForecastPeriodNight().setTemp(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("windspeedMiles".equals(name) && state == State.LongRange && units == Units.Standard) {
            currentLongRange.getForecastPeriodDay().setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
            currentLongRange.getForecastPeriodNight().setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("windspeedKmph".equals(name) && state == State.LongRange && units == Units.Metric) {
            currentLongRange.getForecastPeriodDay().setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
            currentLongRange.getForecastPeriodNight().setWindSpeed(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("winddir16Point".equals(name) && state == State.LongRange) {
            currentLongRange.getForecastPeriodDay().setWindDirText(getData());
            currentLongRange.getForecastPeriodNight().setWindDirText(getData());
        } else if ("winddirDegree".equals(name) && state == State.LongRange) {
            currentLongRange.getForecastPeriodDay().setWindDir(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
            currentLongRange.getForecastPeriodNight().setWindDir(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid));
        } else if ("weatherCode".equals(name) && state == State.LongRange) {
            currentLongRange.getForecastPeriodDay().setCode(convertCode(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid)));
            currentLongRange.getForecastPeriodNight().setCode(
                    phoenix.weather2.GetCodeForceNight(convertCode(NumberUtils.toInt(getData(), IForecastPeriod.iInvalid))));
        } else if ("weatherDesc".equals(name) && state == State.LongRange) {
            currentLongRange.getForecastPeriodDay().setCondition(getData());
            currentLongRange.getForecastPeriodNight().setCondition(getData());
        } else if ("precipMM".equals(name) && state == State.LongRange) {
            currentLongRange.getForecastPeriodDay().setPrecip(formatPrecip(getData()));
            currentLongRange.getForecastPeriodNight().setPrecip(formatPrecip(getData()));
        } else if ("weather".equals(name)) {
            // finalize anything for the current long range forecast item
            if (currentLongRange.getForecastPeriodDay().getWindSpeed() == 0) {
                currentLongRange.getForecastPeriodDay().setWindDir(IForecastPeriod.iInvalid);
                currentLongRange.getForecastPeriodDay().setWindDirText(IForecastPeriod.WindCalm);
            }
            if (currentLongRange.getForecastPeriodNight().getWindSpeed() == 0) {
                currentLongRange.getForecastPeriodNight().setWindDir(IForecastPeriod.iInvalid);
                currentLongRange.getForecastPeriodNight().setWindDirText(IForecastPeriod.WindCalm);
            }
            // null out currentLongRange, since it'll created again
            // when needed
            currentLongRange = null;
        }
    }

    private int convertCode(int wwCode) {
        if (codeMap.containsKey(wwCode)) {
            return codeMap.get(wwCode);
        }
        return -1;
    }

    private String formatPrecip(String wwPrecip) {
        Float precip = NumberUtils.toFloat(wwPrecip, 0.0f);
        String retVal = "";
        if (precip == 0 || precip == 0.0) {
            return "";
        }
        if (units.equals(Units.Metric)) {
            retVal = new DecimalFormat("#.#").format(precip) + " mm";
        } else {
            // convert to inches
            precip = precip * 0.0393700787f;
            retVal = new DecimalFormat("#.#").format(precip) + " in";
        }
        if (retVal.startsWith("0 ") || retVal.startsWith("0.0 ")) {
            return "";
        } else {
            return retVal;
        }
    }

    private int formatVisibility(String wwVis) {
        int vis = NumberUtils.toInt(wwVis, IForecastPeriod.iInvalid);
        if (vis == IForecastPeriod.iInvalid)
            return IForecastPeriod.iInvalid;
        if (units.equals(Units.Standard)) {
            // convert to miles rounded
            vis = Math.round(vis * 0.621371192f);
        }
        return vis;
    }

    private String formatPressure(String wwPressure) {
        Float pressure = NumberUtils.toFloat(wwPressure, 0.0f);
        if (units.equals(Units.Metric)) {
            return new DecimalFormat("####").format(pressure);
        } else {
            // convert millibars to inches of mercury
            pressure = pressure * 0.0295301f;
            return new DecimalFormat("##.00").format(pressure);
        }
    }

    private Date convertDate(String string) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(string);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(WorldWeatherWeatherHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
