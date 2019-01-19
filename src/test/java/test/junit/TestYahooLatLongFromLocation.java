package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.weather.IForecastPeriod;
import sagex.phoenix.weather.yahoo.YahooWeatherJsonHandler;
import test.InitPhoenix;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by jusjoken on 1/14/2019.
 */
public class TestYahooLatLongFromLocation {

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testYahooLatLongFromLocation() {
        //test US ZIP
        testLocation("55373",45.084751,-93.718323);

        //test City Name,State
        testLocation("Rockford,MN",45.091599,-93.744888);

        // repeat tests for Canada postal
        testLocation("n5y5m4",43.03355,-81.2257);

        testLocation("This is not a valid location 1234567",IForecastPeriod.dInvalid,IForecastPeriod.dInvalid);

    }

    private void testLocation(String testLocation, Double expectedLat, Double expectedLong){
        Double dLat = 0.0;
        Double dLong = 0.0;

        System.out.println("Testing location: " + testLocation);

        YahooWeatherJsonHandler handler = new YahooWeatherJsonHandler();
        handler.LatLongFromLocation(testLocation);

        dLat = handler.getLat();
        dLong = handler.getLong();

        System.out.println("Location lat-long returned: lat = " + dLat + " long = " + dLong);

        if (dLat.equals(IForecastPeriod.dInvalid) || dLong.equals(IForecastPeriod.dInvalid)){
            if (dLat.equals(expectedLat) && dLong.equals(expectedLong) ){
                System.out.println("Testing invalid location: '" + testLocation + "' - passed");
            }else{
                fail("Failed to get lat-long from location: '" + testLocation + "'");
            }
        }else{
            assertEquals("Location: '" + testLocation + "'. Lat value not as expected",expectedLat, dLat);
            assertEquals("Location: '" + testLocation + "'. Long value not as expected",expectedLong, dLong);
        }

    }

}
