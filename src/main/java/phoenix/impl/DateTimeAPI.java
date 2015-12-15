package phoenix.impl;

import java.util.Calendar;

import sagex.phoenix.tools.annotation.API;

@API(group = "time")
public class DateTimeAPI {
    /**
     * Returns current time (same as Sage's Time() call)
     *
     * @return
     */
    public long Now() {
        return System.currentTimeMillis();
    }

    /**
     * Return the time for today, for the given hour and minute. Hour is 24hr
     * time.
     *
     * @param hr     24hr time
     * @param minute 0-59
     * @return date/time
     */
    public long Time(int hr, int minute) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hr);
        now.set(Calendar.MINUTE, minute);
        now.clear(Calendar.SECOND);
        return now.getTimeInMillis();
    }

    /**
     * Return the time for the given date, for the given hour and minute. Hour
     * is 24hr time.
     *
     * @param hr     24hr time
     * @param minute 0-59
     * @return date/time
     */
    public long Time(long date, int hr, int minute) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(date);
        now.set(Calendar.HOUR_OF_DAY, hr);
        now.set(Calendar.MINUTE, minute);
        now.clear(Calendar.SECOND);
        return now.getTimeInMillis();
    }

    /**
     * Returns the time for the given hr, min, and 'am' or 'pm' (ie, 2, 45,
     * 'pm')
     *
     * @param hr
     * @param minute
     * @param ampm
     * @return
     */
    public long Time(int hr, int minute, String ampm) {
        if (hr < 12 && "pm".equalsIgnoreCase(ampm)) {
            hr += 12;
        }
        return Time(hr, minute);
    }

    /**
     * Adds number of hours to a given time (can be negative hours)
     *
     * @param time
     * @param hrs
     * @return
     */
    public long AddHours(long time, int hrs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.HOUR_OF_DAY, hrs);
        return cal.getTimeInMillis();
        // return time + (hrs*60*60*1000);
    }

    /**
     * Adds number of mintues to a given time( can be negative minutes)
     *
     * @param time
     * @param minutes
     * @return
     */
    public long AddMinutes(long time, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTimeInMillis();
        // return time + (minutes*60*1000);
    }

    /**
     * Adds number of days to a time (can be negative days)
     *
     * @param time
     * @param minutes
     * @return
     */
    public long AddDays(long time, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTimeInMillis();
    }

    /**
     * Returns the start and end times for for the given date a long array where
     * the first element is the start time and the second element is the end
     * time.
     * <p/>
     * When calling StartAndEnd you can pass a number for the day offset. This
     * will offset the date by the number of days. ie, offset of 0 is today,
     * offset of -1 is yesterday, offset of +1 is tomorrow.
     * <p/>
     * So to get the start and end times for tomorrow you can use
     * StartAndEnd(Now(), 1) and it will return the 2 date/time long values for
     * the start of the day until the end of the day. (ie, 00:00 and 23:59)
     *
     * @return
     */
    public long[] StartAndEnd(long date, int dayOffset) {
        long d = AddDays(date, dayOffset);
        return new long[]{Time(d, 0, 0), Time(d, 23, 59)};
    }

    /**
     * Convenience method for StartAndEnd(Now(), 0)
     *
     * @return
     */
    public long[] StartAndEnd() {
        return StartAndEnd(Now(), 0);
    }
}
