package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.IForecastPeriod;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.darksky.DarkSkyWeatherSupport;
import test.InitPhoenix;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jusjoken on 1/14/2019.
 */
public class TestDarkSkyWeather {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testDarkSkyWeather() {
        DarkSkyWeatherSupport w = new DarkSkyWeatherSupport();
        w.setTesting(true);
        w.removeLocation();
        w.setLocation("55373");
        System.out.println("DarkSky location: " + w.getLocation());

        if (w.hasError()) {
            fail("Failed to set location: " + w.getError());
        }

        w.setUnits(IWeatherSupport2.Units.Standard);
        assertEquals("Failed to update units to Standard", true, w.getUnits().equals(IWeatherSupport2.Units.Standard));

        boolean updated = w.update();
        System.out.println("DarkSky location: " + w.getLocation() + " Name '" + w.getLocationName() + "'");
        if (w.hasError()) {
            fail("Failed to update weather: " + w.getError());
        }
        assertEquals("Failed to update weather", true, updated);

        System.out.println("DarkSky test: 2nd update should not be allowed as not enough time passed");
        updated = w.update();
        if (w.hasError()) {
            fail("Failed to update weather: " + w.getError());
        }
        assertEquals("Weather updated, but should not have", false, updated);
        if (!updated){
            System.out.println("DarkSky test: 2nd update was not allowed - test passed");
        }

        assertEquals("Rockford, MN", w.getLocationName());

        testWeather(w);

        // now retest with metric for the same location
        System.out.println("DarkSky test: testing update - same location with units changed to metric");
        w.setUnits(IWeatherSupport2.Units.Metric);
        assertEquals("Failed to update units to Metric", true, w.getUnits().equals(IWeatherSupport2.Units.Metric));
        updated = w.update();
        if (w.hasError()) {
            fail("Failed to update weather on change of units: " + w.getError());
        }
        assertEquals("Weather did not update after change of units", true, updated);


        testWeather(w);

        // repeat tests for Canada postal
        w.removeLocation();
        w.setLocation("n5y5m4");
        System.out.println("DarkSky location: " + w.getLocation());

        if (w.hasError()) {
            fail("Failed to set location: " + w.getError());
        }

        w.setUnits(IWeatherSupport2.Units.Metric);
        assertEquals("Failed to update units to Metric", true, w.getUnits().equals(IWeatherSupport2.Units.Metric));

        updated = w.update();
        System.out.println("DarkSky location: " + w.getLocation() + " Name '" + w.getLocationName() + "'");
        if (w.hasError()) {
            fail("Failed to update weather: " + w.getError());
        }
        assertEquals("Failed to update weather", true, updated);

        updated = w.update();
        if (w.hasError()) {
            fail("Failed to update weather: " + w.getError());
        }
        assertEquals("Weather updated, but should not have", false, updated);

        assertEquals("London, ON", w.getLocationName());

        testWeather(w);

    }

    private void testWeather(DarkSkyWeatherSupport w) {
        ICurrentForecast curWeather = w.getCurrentWeather();
        System.out.println(w.getLocationName() + " - " + w.getUnits().name() + " - " + w.getRecordedDate() + " - " + curWeather);
        // assertEquals("Current forecast day not correct", "Now",
        // curWeather.getDay());
        assertEquals("Current forecast type not correct", IForecastPeriod.Type.Current, curWeather.getType());
        assertNotNull("Temp was null", curWeather.getTemp());
        assertNotNull("Date was null", curWeather.getDate());
        assertNotNull("Code was null", curWeather.getCode());
        assertNotNull("Condition was null", curWeather.getCondition());

        // validate supported elements
        SupportedTest("curWeather.getCloudCover", curWeather.getCloudCover(), true);
        SupportedTest("curWeather.getCode", curWeather.getCode(), true);
        SupportedTest("curWeather.getCondition", curWeather.getCondition(), true);
        SupportedTest("curWeather.getDescription", curWeather.getDescription(), true);
        SupportedTest("curWeather.getDewPoint", curWeather.getDewPoint(), true);
        SupportedTest("curWeather.getFeelsLike", curWeather.getFeelsLike(), true);
        SupportedTest("curWeather.getHumid", curWeather.getHumid(), true);
        SupportedTest("curWeather.getPrecip", curWeather.getPrecip(), true);
        SupportedTest("curWeather.getPressure", curWeather.getPressure(), true);
        SupportedTest("curWeather.getPressureDir", curWeather.getPressureDir(), false);
        SupportedTest("curWeather.getSunrise", curWeather.getSunrise(), true);
        SupportedTest("curWeather.getSunset", curWeather.getSunset(), true);
        SupportedTest("curWeather.getTemp", curWeather.getTemp(), true);
        SupportedTest("curWeather.getUVIndex", curWeather.getUVIndex(), true);
        SupportedTest("curWeather.getUVWarn", curWeather.getUVWarn(), false);
        SupportedTest("curWeather.getVisibility", curWeather.getVisibility(), true);
        SupportedTest("curWeather.getWindDir", curWeather.getWindDir(), true);
        SupportedTest("curWeather.getWindDirText", curWeather.getWindDirText(), true);
        SupportedTest("curWeather.getWindSpeed", curWeather.getWindSpeed(), true);

        List<ILongRangeForecast> days = w.getForecasts();
        assertTrue("No forecast data", days.size() > 0);
        int daynum = 0;
        for (ILongRangeForecast wd : days) {
            IForecastPeriod day = wd.getForecastPeriodDay();
            System.out.println("  Day " + daynum + " '" + day);
            IForecastPeriod night = wd.getForecastPeriodNight();
            System.out.println("Night " + daynum + " '" + night);
            if (daynum == 0) {
                assertNotNull("Night Period for first day was null", night);
                assertEquals("Forecast type for '" + daynum + "' not correct", IForecastPeriod.Type.Night, night.getType());
            }
            if (daynum == 1) {
                assertNotNull("Day Period for second day was null", day);
                assertEquals("Forecast type for '" + daynum + "' not correct", IForecastPeriod.Type.Day, day.getType());
            }
            if (day != null) {
                assertNotNull("No Condition", day.getCondition());
                assertNotNull("No Code", day.getCode());
                assertNotNull("No Date", day.getDate());
                assertNotNull("No Temp", day.getTemp());
                assertNotNull("No Type", day.getType());
                assertEquals(day.getType(), IForecastPeriod.Type.Day);
                // validate supported elements
                SupportedTest("day.getCode", day.getCode(), true);
                SupportedTest("day.getCondition", day.getCondition(), true);
                SupportedTest("day.getHumid", day.getHumid(), true);
                SupportedTest("day.getTemp", day.getTemp(), true);
                SupportedTest("day.getWindDir", day.getWindDir(), true);
                SupportedTest("day.getWindDirText", day.getWindDirText(), true);
                SupportedTest("day.getWindSpeed", day.getWindSpeed(), true);
                SupportedTest("day.getDescription", day.getDescription(), true);
                SupportedTest("day.getPrecip", day.getPrecip(), true);
            }

            if (night != null) {
                assertNotNull("No Condition", night.getCondition());
                assertNotNull("No Code", night.getCode());
                assertNotNull("No Date", night.getDate());
                assertNotNull("No Temp", night.getTemp());
                assertNotNull("No Type", night.getType());
                assertEquals(night.getType(), IForecastPeriod.Type.Night);
                // validate supported elements
                SupportedTest("night.getCode", night.getCode(), true);
                SupportedTest("night.getCondition", night.getCondition(), true);
                SupportedTest("night.getHumid", night.getHumid(), true);
                SupportedTest("night.getTemp", night.getTemp(), true);
                SupportedTest("night.getWindDir", night.getWindDir(), true);
                SupportedTest("night.getWindDirText", night.getWindDirText(), true);
                SupportedTest("night.getWindSpeed", night.getWindSpeed(), true);
                SupportedTest("night.getDescription", night.getDescription(), true);
                SupportedTest("night.getPrecip", night.getPrecip(), true);
            }
            daynum++;
        }

    }

    private void SupportedTest(String testElement, int valueToTest, boolean expectedResult) {
        assertEquals("Supported not set correctly for '" + testElement + "'", expectedResult,
                valueToTest != IForecastPeriod.iNotSupported);
    }

    private void SupportedTest(String testElement, String valueToTest, boolean expectedResult) {
        if (valueToTest!=null){
            assertEquals("Supported not set correctly for '" + testElement + "'", expectedResult,
                    !valueToTest.equals(IForecastPeriod.sNotSupported));
        }
    }


}
