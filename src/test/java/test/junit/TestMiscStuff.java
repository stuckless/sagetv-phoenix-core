package test.junit;


import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.util.StringUtils;

public class TestMiscStuff {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testStringJoin() {
		assertEquals("1", StringUtils.join(":", "1"));
		assertEquals("1:2", StringUtils.join(":", "1","2"));
		assertEquals("1:2:3", StringUtils.join(":", "1","2","3"));
		assertEquals("3", StringUtils.join(":", null,null,"3"));
		assertEquals("1:3", StringUtils.join(":", "1",null,"3"));
	}
}
