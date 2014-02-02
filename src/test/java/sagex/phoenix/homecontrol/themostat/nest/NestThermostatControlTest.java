package sagex.phoenix.homecontrol.themostat.nest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.homecontrol.themostat.IDevice;
import sagex.phoenix.homecontrol.themostat.IDeviceStatus;
import test.InitPhoenix;

public class NestThermostatControlTest {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testFakeNest() {
		FakeNestControl fake = new FakeNestControl();
		List<IDevice> devs = fake.getDevices();
		assertEquals(1, devs.size());
		IDevice dev = devs.get(0);
		assertEquals("02AA01AB501208GY", dev.getId());
		assertEquals("Main", dev.getName());

		IDeviceStatus status = fake.getDeviceStatus(dev);
		assertEquals(true, status.getCanCool());
		assertEquals(true, status.getCanHeat());
		assertEquals("heat", status.getCurrentMode());
		assertEquals("C", status.getTempUnits());
		assertEquals(22.47f, status.getCurrentTemp(), 0.1);
		assertEquals(34.0f, status.getHumidity(), 0.1);
		assertEquals(22.0f, status.getTargetTemp(), 0.1);
	}

	@Test
	public void testRealNest() {
		NestConfiguration config = GroupProxy.get(NestConfiguration.class);
		config.setUsername("");
		config.setPassword("");

		NestThermostatControl nest = new NestThermostatControl();

		List<IDevice> devs = nest.getDevices();
		assertEquals(1, devs.size());
		IDevice dev = devs.get(0);
		assertEquals("02AA01AB501208GY", dev.getId());
		assertEquals("Main", dev.getName());

		IDeviceStatus status = nest.getDeviceStatus(dev);
		assertEquals(true, status.getCanCool());
		assertEquals(true, status.getCanHeat());
		assertNotNull(status.getCurrentMode());
		assertEquals("C", status.getTempUnits());
		assertTrue(status.getCurrentTemp() > 1);
		assertTrue(status.getHumidity() > 1);
		assertTrue(status.getTargetTemp() > 1);
	}
}
