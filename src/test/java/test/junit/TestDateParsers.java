package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import sagex.phoenix.util.DateUtils;

public class TestDateParsers {
	@Test
	public void testDates() {
		BasicConfigurator.configure();
		Date d1 = DateUtils.parseDate("2010-5-12");
		System.out.println(d1);
		assertNotNull(d1);

		d1 = DateUtils.parseDate("21 May 2010");
		System.out.println(d1);
		assertNotNull(d1);

		d1 = DateUtils.parseDate("21 May 2010 (Canada)");
		System.out.println(d1);
		assertNotNull(d1);

		d1 = DateUtils.parseDate("2010");
		System.out.println(d1);
		assertNotNull(d1);
	}

	@Test
	public void testMinutes() {
		long time = 45 * 60 * 1000;
		long mins = DateUtils.parseRuntimeInMinutes(String.valueOf(time));
		assertEquals(mins, time);

		mins = DateUtils.parseRuntimeInMinutes("45 minutes");
		assertEquals(mins, time);

		mins = DateUtils.parseRuntimeInMinutes("45 min");
		assertEquals(mins, time);

		mins = DateUtils.parseRuntimeInMinutes("45 mins");
		assertEquals(mins, time);
	}
}
