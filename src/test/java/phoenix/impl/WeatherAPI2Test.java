package phoenix.impl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sagex.SageAPI;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.yahoo.YahooWeatherSupport2;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by seans on 11/03/17.
 */
public class WeatherAPI2Test {
    static SimpleStubAPI api;
    @BeforeClass
    public static void init() throws IOException {
        api = new SimpleStubAPI();
        api.LOG_MISSING_GETPROPERTY=true;
        api.LOG_SETPROPERTY=true;

        api.getProperties().put(WeatherAPI2.API_IMPL_PROP, "dummy");
        api.getProperties().put("phoenix/weather/location","london, ontario");
        api.getProperties().put("phoenix/weather/units","m");
        WeatherAPI2.API_IMPL.put("dummy", DummyWeather.class.getName());
        WeatherAPI2.API_IMPL_NAME.put("dummy", "Dummy");

        SageAPI.setProvider(api);

        InitPhoenix.init(false, api, false);
    }

    @Before
    public void initWeather() {
        phoenix.weather2.SetIsLocked(false);
        IWeatherSupport2 impl = phoenix.weather2.SetWeatherImpl("dummy");
        assertTrue(impl instanceof DummyWeather);
        phoenix.weather2.SetIsLocked(true);
    }

    @Test
    public void testWeatherLocked() {
        // verify we are using dummy weather
        assertEquals("dummy",phoenix.weather2.GetWeatherImplKey());
        assertEquals("Dummy",phoenix.weather2.GetWeatherImplName());

        // verify locked
        assertTrue(phoenix.weather2.IsLocked());

        // try to override weather..
        phoenix.weather2.SetWeatherImpl("yahoo");
        // verify we are still using dummy weather
        assertEquals("dummy",phoenix.weather2.GetWeatherImplKey());
        assertEquals("Dummy",phoenix.weather2.GetWeatherImplName());

        assertEquals("Metric", phoenix.weather2.GetUnits());
        phoenix.weather2.SetUnits("Standard");
        assertEquals("Metric", phoenix.weather2.GetUnits());

        assertEquals("london, ontario", phoenix.weather2.GetLocation());
        phoenix.weather2.SetLocation("toronto, ontario");
        assertEquals("london, ontario", phoenix.weather2.GetLocation());

        // unlock it..
        phoenix.weather2.SetIsLocked(false);
        phoenix.weather2.SetWeatherImpl("yahoo");
        assertEquals("yahoo",phoenix.weather2.GetWeatherImplKey());

        phoenix.weather2.SetWeatherImpl("dummy");
        assertEquals("dummy",phoenix.weather2.GetWeatherImplKey());

        assertEquals("Metric", phoenix.weather2.GetUnits());
        phoenix.weather2.SetUnits("Standard");
        assertEquals("Standard", phoenix.weather2.GetUnits());

        assertEquals("london, ontario", phoenix.weather2.GetLocation());
        phoenix.weather2.SetLocation("toronto, ontario");
        assertEquals("toronto, ontario", phoenix.weather2.GetLocation());

        // lock it again
        phoenix.weather2.SetIsLocked(true);
    }

    @Test
    public void testUpdate() {
        // verify we are using dummy weather
        assertEquals("dummy",phoenix.weather2.GetWeatherImplKey());
        assertEquals("Dummy",phoenix.weather2.GetWeatherImplName());

        // should work
        assertTrue(phoenix.weather2.Update());
        // should fail
        assertFalse(phoenix.weather2.Update());

        // reset the last checked
        phoenix.weather2.SetLastChedked(0);

        // should work
        assertTrue(phoenix.weather2.Update());
        // should fail
        assertFalse(phoenix.weather2.Update());

        // test that if try to set the same impl twice it will not accept it.
        phoenix.weather2.SetIsLocked(false);
        IWeatherSupport2 impl = phoenix.weather2.GetWeatherImplInstance();
        assertNotNull(impl);
        assertTrue(impl instanceof DummyWeather);
        phoenix.weather2.SetWeatherImpl("dummy");
        IWeatherSupport2 implNew = phoenix.weather2.GetWeatherImplInstance();
        assertTrue(impl == implNew); // same instance, it never changed

        System.out.println("Time till next update " + phoenix.weather2.GetTimeUntilNextCheckAllowed() );
    }

    @Test
    public void testInvalidImpl() {
        // verify we are using dummy weather
        assertEquals("dummy",phoenix.weather2.GetWeatherImplKey());
        assertEquals("Dummy",phoenix.weather2.GetWeatherImplName());

        IWeatherSupport2 impl = phoenix.weather2.GetWeatherImplInstance();
        assertNotNull(impl);
        assertTrue(impl instanceof DummyWeather);

        phoenix.weather2.SetIsLocked(false);
        IWeatherSupport2 implNew = phoenix.weather2.SetWeatherImpl("abc");
        assertNull(implNew);
        assertTrue(phoenix.weather2.GetWeatherImplInstance() instanceof DummyWeather);

        // if you set legacy providers it will default to yahoo
        implNew = phoenix.weather2.SetWeatherImpl("google");
        assertTrue(implNew instanceof YahooWeatherSupport2);
    }
}