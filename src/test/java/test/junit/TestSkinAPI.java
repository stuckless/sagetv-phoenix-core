package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.api.Configuration;
import sagex.phoenix.skins.ISkin;
import sagex.phoenix.skins.Skin.State;
import sagex.phoenix.skins.SkinManager;
import sagex.stub.PropertiesStubAPIProxy;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;

public class TestSkinAPI {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testSkinAPI() {
		Object val = phoenix.skin.GetSkinProperty("menu1/object1/prop", "10");
		assertEquals("10", val);

		// should covert the previous String 10 to int 10, because default value
		// is of type int
		val = phoenix.skin.GetSkinProperty("menu1/object1/prop", 30);
		assertEquals("30", val);

		val = phoenix.skin.GetSkinProperty("menu1/object2/prop", 20);
		assertEquals("20", val);

		val = phoenix.skin.GetSkinProperty("menu1/object3/prop", true);
		assertEquals("true", val);

		// TODO: stub api does not support GetSubpropertiesThatAreLeaves so this
		// fails
		// List props = phoenix.api.GetSkinProperties();
		// assertEquals(3, props.size());
	}

	@Test
	public void testSkinPlugins() throws IOException {
		FileUtils.deleteDirectory(new File("target/testing/userdata/"));
		SkinManager mgr = new SkinManager(new File("src/test/java/test/junit/testskins/"), new File(
				"target/testing/userdata/Phoenix/Skins"));
		mgr.loadConfigurations();

		assertEquals(2, mgr.getPlugins().length);

		ISkin skin1 = mgr.findPlugin("phoenix.helloworld1");
		assertNotNull(skin1);

		ISkin skin2 = mgr.findPlugin("phoenix.helloworld2");
		assertNotNull(skin2);

		mgr.loadSkin("XXX", skin2);

		// NOTE: This only works as a test becauset he stub api uses the sample
		// properties object for all
		// properties, server client, context variables, etc.
		assertTrue(new File(Configuration.GetProperty("Test1", null)).exists());
		assertTrue(new File(Configuration.GetProperty("Test2", null)).exists());
		assertTrue(new File(Configuration.GetProperty("Test3", null)).exists());

		// test overriden image name
		File test1img = new File(Configuration.GetProperty("Test1", null));
		assertEquals(test1img.getName(), "test1replace.png");

		// the only way I can get and test global context vars
		Map<Object, Object> props = ((PropertiesStubAPIProxy) ((StubSageAPI) SageAPI.getProvider()).getProxy("AddGlobalContext"))
				.getProperties();
		String testInsets[] = (String[]) props.get("Test1Insets");
		assertNotNull(testInsets);
		assertEquals(4, testInsets.length);

		for (String s : testInsets) {
			System.out.println("Inset: " + s);
		}

		assertEquals("Turtle", props.get("Test1SomeVar"));

		assertEquals(State.ACTIVE, skin1.getState());
		mgr.stopPlugin(skin1);
		assertEquals(State.RESOLVED, skin1.getState());

		// create a plugin
		ISkin skin = mgr.createPlugin("newplugin", "New Plugin", null);
		assertTrue(skin.getDirectory().exists());
		assertEquals(skin.getState(), State.ACTIVE);

		// force a reload
		mgr.loadConfigurations();
		assertEquals(3, mgr.getPlugins().length);
	}

}
