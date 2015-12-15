package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import phoenix.impl.ConfigurationAPI;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.ConfigurationManager;
import sagex.phoenix.configuration.ConfigurationMetadataManager;
import sagex.phoenix.configuration.Field;
import sagex.phoenix.configuration.Group;
import sagex.phoenix.configuration.IConfigurationElement;
import sagex.phoenix.configuration.IConfigurationMetadataVisitor;
import sagex.phoenix.configuration.XmlMetadataParser;
import sagex.phoenix.configuration.impl.SageConfigurationProvider;
import sagex.phoenix.configuration.proxy.GroupParser;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.util.NamedValue;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;
import test.junit.cm.MyProxyGroup;
import test.junit.lib.SimpleStubAPI;

public class TestConfigurationMetadata {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testDirectoryLoader() throws Exception {
		ConfigurationMetadataManager cmm = new ConfigurationMetadataManager(new File("src/main/STVs/Phoenix/Configuration"),
				new File("target/testing/userdata/Phoenix/Configuration"));
		cmm.loadConfigurations();
		assertNotNull("Failed to create ConfigurationMetadataManager", cmm);
		assertNotNull("ConfigurationMetadataManager didn't load metadta", cmm.getMetadata());
		assertTrue("ConfigurationMetadata deoesn't have the expected # of groups", cmm.getParentGroups().length > 0);
		IConfigurationElement sage = cmm.findElement("sage");
		assertNotNull("Did not find a 'sage' element!", sage);
		assertEquals(IConfigurationElement.APPLICATION, sage.getElementType());
	}

	@Test
	public void testXmlMetadata() throws Exception {
		ConfigurationMetadataManager cmm = new ConfigurationMetadataManager(new File("src/main/STVs/Phoenix/Configuration"),
				new File("target/testing/userdata/Phoenix/Configuration"));
		cmm.loadConfigurations();
		assertNotNull("Failed to create ConfigurationMetadataManager", cmm);
		assertNotNull("ConfigurationMetadataManager didn't load metadta", cmm.getMetadata());
		assertTrue("ConfigurationMetadata deoesn't have the expected # of groups", cmm.getParentGroups().length > 0);

		assertEquals("Root Group should be 'root'!", "root", cmm.getMetadata().getId());
		assertEquals("Root Group should be a Group!", IConfigurationElement.GROUP, cmm.getMetadata().getElementType());

		IConfigurationElement sage = cmm.findElement("sage");
		assertNotNull("Did not find a 'sage' element!", sage);
		assertEquals("", IConfigurationElement.APPLICATION, sage.getElementType());
		assertNotNull("Failed to parse id", sage.getId());
		assertNotNull("Failed to parse description", sage.getDescription());
		assertNotNull("Failed to parse label", sage.getLabel());
		assertNotNull("Failed to set parent", sage.getParent());
		assertEquals("Sage Parent should be the root!", cmm.getMetadata(), sage.getParent());

		// test no group elements
		IConfigurationElement el = cmm.getConfigurationElement("debug_exif_parser");
		assertNotNull("Missing Debug Element", el);
		assertNotNull("Failed to parse id", el.getId());
		assertNotNull("Failed to parse description", el.getDescription());
		assertNotNull("Failed to parse label", el.getLabel());
		assertNotNull("Failed to set parent", el.getParent());

		assertEquals("Element should be a Field", el.getClass(), Field.class);
		assertNotNull("No Default Value", ((Field) el).getDefaultValue());
		assertNotNull("No Type", ((Field) el).getType());

		assertEquals("Element Type is invalid", IConfigurationElement.FIELD, el.getElementType());

		// test elements with group id
		el = cmm.getConfigurationElement("phoenix/core/enableAdvancedOptions");
		assertNotNull("Missing Mult-Level VFS Debug Element", el);
		assertNotNull("Failed to parse id", el.getId());
		assertNotNull("Failed to parse description", el.getDescription());
		assertNotNull("Failed to parse label", el.getLabel());
		assertNotNull("Failed to set parent", el.getParent());

		el = cmm.getConfigurationElement("mediafile_metadata_parser_plugins");
		assertNotNull(el);
		assertEquals(";", ((Field) el).getListSeparator());
		assertEquals(ConfigType.TEXT, ((Field) el).getType());

		// test dynamically add new metadata
		Group g = new Group();
		g.setElementType(g.APPLICATION);
		g.setLabel("Test");
		g.setId("testx");

		Group g1 = new Group();
		g1.setId("this/is/a");
		g.addElement(g1);

		Field f = new Field();
		f.setId(g1.getId() + "/test");
		g1.addElement(f);

		cmm.addMetadata(g);
		assertEquals("ConfigurationMetadata deoesn't have the expected # of groups", 5, cmm.getParentGroups().length);

		g.visit(new IConfigurationMetadataVisitor() {
			public void accept(IConfigurationElement el) {
				System.out.println("El: " + el.getId() + "; " + el.getParent().getId());
			}
		});

		el = cmm.getConfigurationElement("this/is/a/test");
		assertNotNull("Failed Dynmaic Update", el);
	}

	@Test
	public void testConfigurationProvider() throws Exception {
		ConfigurationMetadataManager cmm = new ConfigurationMetadataManager(new File("src/main/STVs/Phoenix/Configuration"),
				new File("src/test/java/test/junit/cm/"));
		cmm.loadConfigurations();

		ConfigurationManager cm = new ConfigurationManager(cmm, new SageConfigurationProvider());
		cm.setStrictConfiguration(false);

		String key = "testxxxprop";
		String propVal = "123";
		assertEquals("Default value (server) is broken", propVal, cm.getServerProperty(key, propVal));
		assertEquals("Set Property on Get (server) is broken", null, cm.getServerProperty(key, null));
		assertNull("ClientScope broken", cm.getClientProperty(key, null));
		assertNull("UserScope broken", cm.getUserProperty(key, null));

		key = "testxxyprop";
		assertEquals("Default value (client) is broken", propVal, cm.getClientProperty(key, propVal));
		assertEquals("Set Property on Get (client) is broken", null, cm.getClientProperty(key, null));
		assertNull("ServerScope broken", cm.getServerProperty(key, null));
		assertNull("UserScope broken", cm.getUserProperty(key, null));

		key = "testxxzprop";
		assertEquals("Default value (user) is broken", propVal, cm.getUserProperty(key, propVal));
		assertEquals("Set Property on Get (user) is broken", null, cm.getUserProperty(key, null));
		assertNull("ClientScope broken", cm.getClientProperty(key, null));
		assertNull("ServerScope broken", cm.getServerProperty(key, null));

		// test that object conversion and metadata lookups are working...
		String o = cm.getServerProperty("debug_exif_parser", "true");
		assertEquals("Metadata applied to properties is broken", "false", cm.getServerProperty("xxdebugxx", "false"));

		// test scope

		Field f = (Field) cmm.findElement("test2/field2");
		assertNotNull(f);
		assertEquals(ConfigScope.USER, f.getScope());
		assertEquals("Value2", f.getDefaultValue());
		assertEquals("Value2", cm.getUserProperty("test2/field2", null));
		assertEquals("Value3", cm.getUserProperty("test2/field2", "Value3"));
		cm.setUserProperty("test2/field2", "Value4");
		assertEquals("Value4", cm.getUserProperty("test2/field2", null));
		assertEquals("Value2", cm.getClientProperty("test2/field2", null)); // client
																			// will
																			// return
																			// Value2,
																			// since
																			// it's
																			// the
																			// default
		assertEquals("Value4", cm.getProperty("test2/field2", null)); // should
																		// lookup
																		// the
																		// user
																		// scope
																		// and
																		// use
																		// it
		cm.setProperty("test2/field2", "Value5"); // should set the user scope
		assertEquals("Value5", cm.getProperty("test2/field2", null)); // should
																		// lookup
																		// the
																		// user
																		// scope
																		// and
																		// use
																		// it
		assertEquals("Value5", cm.getUserProperty("test2/field2", null)); // should
																			// lookup
																			// the
																			// user
																			// scope
																			// and
																			// use
																			// it

		// now test a NON configured field
		assertEquals("NotConfigured", cm.getProperty("test2/NOTCONFIGURED", "NotConfigured"));
		cm.setProperty("test2/NOTCONFIGURED", "Configured");
		assertEquals("Configured", cm.getProperty("test2/NOTCONFIGURED", null));

		// add
		Group g = (Group) cmm.findElement("mygroup");
		Phoenix.getInstance().getConfigurationMetadataManager().addMetadata(g);

		// now test user scope in config proxy
		f = (Field) cmm.findElement("mygroup/testuserscope");
		assertNotNull(f);
		assertTrue(f.getScope() == ConfigScope.USER);
		assertEquals("UserScoped1", f.getDefaultValue());
		assertEquals("UserScoped1", cm.getProperty("mygroup/testuserscope", null));
		MyProxyGroup pg = GroupProxy.get(MyProxyGroup.class);
		assertEquals("UserScoped1", pg.testuserscope.get());

		// now set the user scope externally and check it using proxy
		cm.setProperty("mygroup/testuserscope", "UserScoped2");
		assertEquals("UserScoped2", cm.getProperty("mygroup/testuserscope", null));
		assertEquals("UserScoped2", pg.testuserscope.get());
		pg.testuserscope.set("UserScoped3");
		assertEquals("UserScoped3", cm.getProperty("mygroup/testuserscope", null));
		assertEquals("UserScoped3", cm.getUserProperty("mygroup/testuserscope", null));
		assertEquals("UserScoped1", cm.getClientProperty("mygroup/testuserscope", null));
	}

	@Test
	public void testUserProperties() {
		SageAPI.setProvider(new StubSageAPI());
		SageConfigurationProvider c = new SageConfigurationProvider();
		c.setProperty(ConfigScope.USER, "test", "testval");
		assertEquals("User Configuration Properties Failed!", "testval", c.getProperty(ConfigScope.USER, "test"));
	}

	@Test
	public void testGroupProxy() {
		MyProxyGroup grp = new MyProxyGroup();
		assertEquals("mygroup", grp.getGroupPath());
		assertEquals("mygroup/name", grp.name.getKey());
		assertEquals("test/name2", grp.name2.getKey());

		Group gr = GroupParser.parseGroup(grp.getClass());
		assertNotNull(gr.findElement("mygroup/name"));
		assertNotNull(gr.findElement("test/name2"));

		assertTrue("Boolean Failed!", grp.testboolean.get());
		grp.testboolean.set(false);
		assertTrue("Boolean Failed!", !grp.testboolean.get());

		assertEquals((Integer) 1, grp.testint.get());
		grp.testint.set(2);
		assertEquals((Integer) 2, grp.testint.get());
	}

	@Test
	public void testXmlParser() throws FileNotFoundException, Exception {

		SimpleStubAPI stub = new SimpleStubAPI();
		SageAPI.setProvider(stub);
		stub.overrideAPI("GetUIContextName", null); // consider overriding api;
		stub.addExpression("phoenix_config_CreateOptionList(phoenix_umb_GetViewFactories(\"tv\"))", new String[] { "1", "2" }/*
																															 * phoenix
																															 * .
																															 * umb
																															 * .
																															 * GetViewFactories
																															 * (
																															 * "tv"
																															 * )
																															 */);

		XmlMetadataParser mp = new XmlMetadataParser();
		Group g = mp.parseMetadata(new FileInputStream(new File("src/test/java/test/junit/cm/test-config.xml")));
		assertEquals(3, g.getChildCount());

		Group g1 = (Group) g.getChild(0);
		assertEquals("test1", g1.getId());
		assertEquals("Test1", g1.getLabel());

		Field f = (Field) g1.getChild(2);
		assertEquals("test1/field3", f.getId());
		assertEquals(ConfigType.CHOICE, f.getType()); // gets fixed up
		assertEquals("Field 3", f.getLabel());
		assertEquals("3", f.getDefaultValue());
		assertEquals("Field 3 Description", f.getDescription());
		assertEquals(4, f.getOptions().size());
		assertEquals("1", f.getOptions().get(0).getValue());
		assertEquals("One", f.getOptions().get(0).getName());
		assertEquals("2", f.getOptions().get(1).getValue());
		assertEquals("Two", f.getOptions().get(1).getName());
		assertEquals(ConfigScope.CLIENT, f.getScope());

		f = (Field) g1.getChild(6);
		assertEquals("test1/field7", f.getId());
		assertEquals(ConfigType.MULTICHOICE, f.getType());
		assertEquals("Field 7", f.getLabel());
		assertEquals("2;4", f.getDefaultValue());
		assertEquals(";", f.getListSeparator());
		assertEquals("Field 7 Description", f.getDescription());
		assertEquals(4, f.getOptions().size());
		assertEquals("1", f.getOptions().get(0).getValue());
		assertEquals("One", f.getOptions().get(0).getName());
		assertEquals("2", f.getOptions().get(1).getValue());
		assertEquals("Two", f.getOptions().get(1).getName());
		assertEquals(ConfigScope.SERVER, f.getScope());

		f = (Field) g1.getChild(0);
		assertEquals("test1/field1", f.getId());
		assertEquals(ConfigType.BOOL, f.getType());
		assertEquals("Field 1", f.getLabel());
		assertEquals("false", f.getDefaultValue());
		assertNotNull("Should have boolean options", f.getOptions());
		assertEquals(2, f.getOptions().size());
		assertEquals("true", f.getOptions().get(0).getValue());
		assertEquals("True", f.getOptions().get(0).getName());
		assertEquals("false", f.getOptions().get(1).getValue());
		assertEquals("False", f.getOptions().get(1).getName());
		assertEquals(ConfigScope.USER, f.getScope());

		// now test some of the APIs
		ConfigurationAPI api = new ConfigurationAPI();

		// test is types
		assertTrue(api.IsBoolean(g1.getChild(0)));
		assertTrue(api.IsButton(g1.getChild(1)));
		assertTrue(api.IsChoice(g1.getChild(2)));
		assertTrue(api.IsDirectory(g1.getChild(3)));
		assertTrue(api.IsFile(g1.getChild(4)));
		assertTrue(api.IsNumber(g1.getChild(5)));
		assertTrue(api.IsMultiChoice(g1.getChild(6)));
		assertTrue(api.IsPassword(g1.getChild(7)));
		assertTrue(api.IsText(g1.getChild(8)));

		// test single selected
		f = (Field) g1.getChild(2);
		assertFalse(api.IsSelected(api.GetOption(f, "1"), f, "3"));
		assertFalse(api.IsSelected(api.GetOption(f, "2"), f, "3"));
		assertTrue(api.IsSelected(api.GetOption(f, "3"), f, "3"));
		assertFalse(api.IsSelected(api.GetOption(f, "4"), f, "3"));

		// test boolean selected
		f = (Field) g1.getChild(0);
		assertTrue(api.IsSelected(api.GetOption(f, "true"), f, "true"));
		assertFalse(api.IsSelected(api.GetOption(f, "false"), f, "true"));

		// test multi selected
		f = (Field) g1.getChild(6);
		assertFalse(api.IsSelected(api.GetOption(f, "1"), f, "2;4"));
		assertTrue(api.IsSelected(api.GetOption(f, "2"), f, "2;4"));
		assertFalse(api.IsSelected(api.GetOption(f, "3"), f, "2;4"));
		assertTrue(api.IsSelected(api.GetOption(f, "4"), f, "2;4"));

		// test ranges
		Group g2 = (Group) g.findElement("test2");
		assertNotNull(g2);
		f = (Field) g2.findElement("test2/field6");
		assertNotNull(f);
		assertEquals(ConfigType.CHOICE, f.getType()); // note it is configured
														// as text, but options
														// will force us to be a
														// choice
		assertEquals("6", f.getDefaultValue());
		assertEquals(4, f.getOptions().size());
		assertEquals("4", f.getOptions().get(0).getValue());
		assertEquals("5", f.getOptions().get(1).getValue());
		assertEquals("6", f.getOptions().get(2).getValue());
		assertEquals("7", f.getOptions().get(3).getValue());
		assertTrue(api.IsSelected(api.GetOption(f, "4"), f, "4"));

		// test loading for MyProxyGroup
		Group g3 = (Group) g.findElement("mygroup");
		assertNotNull(g3);
		f = (Field) g3.findElement("mygroup/testint");
		assertNotNull(f);
		assertEquals(ConfigType.NUMBER, f.getType());

		f = (Field) g3.findElement("mygroup/testboolean");
		assertNotNull(f);
		assertEquals(ConfigType.BOOL, f.getType());

		// test expression options
		f = (Field) g.findElement("test2/field7");
		assertNotNull(f);
		List<NamedValue> list = f.getOptions();
		assertNotNull(list);
		assertTrue(list.size() > 0);
		for (NamedValue nv : list) {
			System.out.println("List Value: " + nv.getValue() + "; Name: " + nv.getName());
		}

		// test type remains even though there are options
		f = (Field) g2.findElement("test2/field8");
		assertEquals(ConfigType.NUMBER, f.getType());
		assertTrue(f.getOptions().size() == 4);

		// test buttons
		f = (Field) g.findElement("test1/field2");
		assertEquals(ConfigType.BUTTON, f.getType());
		assertTrue(phoenix.config.IsType(f, "button"));
		assertTrue(phoenix.config.IsButton(f));
	}

	@Test
	public void TestToggle() throws FileNotFoundException, Exception {
		SimpleStubAPI stub = new SimpleStubAPI();
		SageAPI.setProvider(stub);
		stub.overrideAPI("GetUIContextName", null); // consider overriding api;

		XmlMetadataParser mp = new XmlMetadataParser();
		Group g = mp.parseMetadata(new FileInputStream(new File("src/test/java/test/junit/cm/test-config.xml")));
		// add this to phoenix, so that we can use the toggle api
		Phoenix.getInstance().getConfigurationMetadataManager().addMetadata(g);

		Field f = phoenix.config.GetField("test2/field6");
		assertNotNull(f);
		assertEquals(ConfigType.CHOICE, f.getType());
		assertTrue(phoenix.config.HasOptions(f));
		phoenix.config.SetProperty(f, "5");
		assertEquals("5", phoenix.config.GetProperty(f));
		phoenix.config.Toggle(f);
		assertEquals("6", phoenix.config.GetProperty(f));
		phoenix.config.Toggle(f);
		assertEquals("7", phoenix.config.GetProperty(f));

		// should now wrap
		phoenix.config.Toggle(f);
		assertEquals("Toggle didn't wrap the set", "4", phoenix.config.GetProperty(f));

		// now test boolean toggle
		f = phoenix.config.GetField("mygroup/testboolean");
		assertNotNull(f);
		assertEquals(ConfigType.BOOL, f.getType());
		phoenix.config.SetProperty(f, true);
		assertEquals("true", phoenix.config.GetProperty(f));
		phoenix.config.Toggle(f);
		assertEquals("false", phoenix.config.GetProperty(f));
		phoenix.config.Toggle(f);
		assertEquals("true", phoenix.config.GetProperty(f));
	}
}
