package test.junit;

import org.junit.Test;
import phoenix.impl.DateTimeAPI;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

public class TestDateTime {
    private DateTimeAPI api = new DateTimeAPI();

    @Test
    public void testTime() {
        for (int i = 0; i < 24; i++) {
            if (i == 2)
                continue; // just ignore 2 because of dst
            validateTime(i, 15, api.Time(i, 15));
        }
        System.out.println("----------");
        validateTime(16, 00, api.Time(16, 00, "pm"));
        validateTime(14, 30, api.Time(2, 30, "pm"));
        validateTime(4, 30, api.Time(4, 30, "am"));
    }

    @Test
    public void testToday() {
        long today[] = api.StartAndEnd(api.Now(), 0);
        validateTime(00, 00, today[0]);
        validateTime(23, 59, today[1]);
    }

    @Test
    public void testAddHours() {
        long time = api.Time(10, 30);
        validateTime(12, 30, api.AddHours(time, 2));
        validateTime(8, 30, api.AddHours(time, -2));

        validateTime(20, 30, api.AddHours(time, 10));
        validateTime(10, 30, api.AddHours(time, 24));
        validateTime(10, 30, api.AddHours(time, 48));
    }

    @Test
    public void testAddMinutes() {
        long time = api.Time(10, 30);
        validateTime(10, 45, api.AddMinutes(time, 15));
        validateTime(11, 00, api.AddMinutes(time, 30));
        validateTime(9, 00, api.AddMinutes(time, -90));
    }

    @Test
    public void testAddDays() {
        long time = api.Time(10, 30);
        validateTime(10, 30, api.AddDays(time, 2));
        validateTime(10, 30, api.AddDays(time, 30));
        validateTime(10, 30, api.AddDays(time, -10));
    }

    private void validateTime(int hr, int min, long time) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(time);
        assertEquals(hr, c.get(c.HOUR_OF_DAY));
        assertEquals(min, c.get(c.MINUTE));
    }
}
