package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.weather.IWeatherData;
import sagex.phoenix.weather.IWeatherSupport.Units;
import sagex.phoenix.weather.yahoo.YahooWeatherSupport;
import test.InitPhoenix;


public class TestYahooWeather {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testYahooWeatherParsing() {
    	YahooWeatherSupport w =  new YahooWeatherSupport();
    	w.setLocation("n5y5m4");
    	if (w.hasError()) {
    		fail("Failed to set location: " + w.getError());
    	}
    	
    	w.setUnits(Units.Metric);
    	
    	boolean updated = w.update();
    	if (w.hasError()) {
    		fail("Failed to update weather: " + w.getError());
    	}
    	assertEquals("Failed to update weather", true, updated);
    	
    	updated = w.update();
    	if (w.hasError()) {
    		fail("Failed to update weather: " + w.getError());
    	}
    	assertEquals("Weather updated, but should not have", false, updated);

    	assertEquals("London", w.getLocationName());
    	
    	IWeatherData curWeather = w.getCurrentWeather();
    	System.out.println(curWeather);
    	assertNotNull("Temp was null", curWeather.getTemp());
    	assertNotNull("Date was null", curWeather.getDate());
    	assertNotNull("Code was null", curWeather.getCode());
    	assertNotNull("Sunrise was null", curWeather.getSunrise());
    	assertNotNull("Sunset was null", curWeather.getSunset());
    	
    	List<IWeatherData> days = w.getForecast();
    	assertTrue("Not forecast data", days.size()>0);
    	for (IWeatherData wd : days) {
    		System.out.println(wd);
        	assertNotNull("Date was null", wd.getDate());
        	assertNotNull("Code was null", wd.getCode());
        	assertNotNull("Day was null", wd.getDay());
        	assertNotNull("High was null", wd.getHigh());
        	assertNotNull("Low was null", wd.getLow());
    	}
    }
}
