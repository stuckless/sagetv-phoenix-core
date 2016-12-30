package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import phoenix.impl.UtilAPI;
import sagex.ISageAPIProvider;
import sagex.SageAPI;
import sagex.api.Global;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.Field;
import sagex.phoenix.menu.*;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.sources.MediaFolderSourceFactory;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.stub.StubAPIProxy;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;

public class TestMenus {
    private static File TestMenusFile;
    private static File TestMenusDTDDir;
    private static File TestMenusUserdataDir;

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);

        TestMenusFile = InitPhoenix.ProjectHome("src/test/java/test/junit/menus/TestMenu.xml");
        TestMenusDTDDir = InitPhoenix.ProjectHome("src/plugins/phoenix-core/STVs/Phoenix/Menus");
        TestMenusUserdataDir = InitPhoenix.ProjectHome("target/userdata/Phoenix/Menus");

        // add this dummy field
        Field f = new Field();
        f.setDefaultValue("false");
        f.setId("phoenix/debug/enableLogging");
        f.setType(ConfigType.BOOL);
        Phoenix.getInstance().getConfigurationMetadataManager().addTransientConfigurationElement(f);
    }

    @Test
    public void testMenuBuilder() throws Throwable, SAXException, IOException {
        SageAPI.setProvider(new StubSageAPI());

        List<Menu> menus = MenuBuilder.buildMenus(TestMenusFile, TestMenusDTDDir);
        assertNotNull(menus);
        assertEquals(5, menus.size());

        Menu m1 = menus.get(0);
        assertEquals("TestMenu", m1.getName());
        assertEquals("Test Menu", m1.label().get());
        assertEquals("TestMenuBackground.jpg", m1.background().get());
        assertEquals("TV", m1.type().get());
        assertEquals("This is a description about the menu", m1.description().get());
        // assertNotNull(m1.getScript());
        assertEquals(9, m1.getItems().size());
        assertEquals("Field1Value", m1.field("field1").get());
        assertEquals("Field2Value", m1.field("field2").get());

        IMenuItem mi1 = m1.getItemByName("item1");
        assertTrue(mi1.visible().get());
        assertEquals("Videos.jpg", mi1.background().get());
        assertEquals("This is a description/help text about the item", mi1.description().get());
        assertEquals("icon1.jpg", phoenix.menu.GetIcon(mi1));
        assertEquals("My Videos", mi1.label().get());
        assertEquals("item1", mi1.getName());
        assertEquals("icon2.jpg", mi1.secondaryIcon().get());
        assertEquals(1, ((MenuItem) mi1).getActions().size());
        assertEquals("Field3Value", mi1.field("field3").get());
        assertEquals("Field4Value", mi1.field("field4").get());

        Action action = ((MenuItem) mi1).getActions().get(0);
        assertTrue(action instanceof SageScreenAction);
        assertEquals("Browser - Videos", action.action().get());

        // now test that the hidden props make the item hidden
        IMenuItem mi = m1.getItemByName("testStaticHidden");
        assertNotNull(mi);
        assertFalse(mi.visible().get());
        assertFalse(mi.visible().get());

        // test that menus without a name, return a digest of their label
        mi = m1.getItemByName(DigestUtils.md5Hex("Launch Firefox"));
        assertNotNull("Launch FireFox doesn't have a name and a digest should be returned", mi);

        phoenix.config.SetProperty("phoenix/debug/enableLogging", "false");
        // System.out.println("VAL: " +
        // phoenix.config.GetProperty("phoenix/debug/enableLogging") +
        // "; Class: " +
        // phoenix.config.GetProperty("phoenix/debug/enableLogging").getClass());
        mi = m1.getItemByName("testPropertyHidden");
        assertNotNull(mi);
        assertFalse("isVisible should be not true", mi.visible().get());

        phoenix.config.SetProperty("phoenix/debug/enableLogging", "true");
        // System.out.println("VAL: " +
        // phoenix.config.GetProperty("phoenix/debug/enableLogging") +
        // "; Class: " +
        // phoenix.config.GetProperty("phoenix/debug/enableLogging").getClass());
        mi = m1.getItemByName("testPropertyHidden");
        assertNotNull(mi);
        assertTrue("isVisible should be true", mi.visible().get());

        // phoenix.api.SetProperty("phoenix/debug/enableLogging", true);
        // assertTrue("isVisible should be true when not set", mi.isVisible());
        // assertTrue("isVisibleOnPropery should be true",
        // mi.isVisibleOnProperty());

        // now test the submenus
        mi = m1.getItemByName("submenu");
        assertNotNull(mi);
        assertEquals("Sub Menu", mi.label().get());
        assertTrue("submenu should be a Menu", mi instanceof Menu);

        Menu sub2 = (Menu) ((Menu) mi).getItemByName("AnotherSubMenu");
        assertEquals("back.jpg", sub2.background().get());
        assertEquals("icon.jpg", sub2.icon().get());
        assertEquals("second.jpg", sub2.secondaryIcon().get());

    }

    @Test
    public void testMenuBuilderUsingPhoenixAPI() throws Throwable, SAXException, IOException {
        SageAPI.setProvider(new StubSageAPI());

        FileUtils.copyFileToDirectory(TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();

        List<Menu> menus = MenuBuilder.buildMenus(TestMenusFile, TestMenusDTDDir);
        assertNotNull(menus);
        assertEquals(5, menus.size());

        Menu m1 = menus.get(0);
        assertEquals("Test Menu", phoenix.menu.GetLabel(m1));
        assertEquals("TestMenuBackground.jpg", phoenix.menu.GetBackground(m1));
        assertEquals("TV", phoenix.menu.GetMenuType(m1));
        assertEquals("This is a description about the menu", phoenix.menu.GetHelp(m1));
        // assertNotNull(m1.getScript());
        assertEquals("Field1Value", phoenix.menu.GetField(m1, "field1"));
        assertEquals("Field2Value", phoenix.menu.GetField(m1, "field2"));

        IMenuItem mi1 = phoenix.menu.GetItemByName(m1, "item1");
        assertTrue(phoenix.menu.Visible(mi1).get());
        assertEquals("Videos.jpg", phoenix.menu.GetBackground(mi1));
        assertEquals("This is a description/help text about the item", phoenix.menu.GetHelp(mi1));
        assertEquals("icon1.jpg", phoenix.menu.GetIcon(mi1));
        assertEquals("My Videos", phoenix.menu.GetLabel(mi1));
        assertEquals("icon2.jpg", phoenix.menu.GetSecondaryIcon(mi1));
        assertEquals(1, ((MenuItem) mi1).getActions().size());
        assertEquals("Field3Value", phoenix.menu.GetField(mi1, "field3"));
        assertEquals("Field4Value", phoenix.menu.GetField(mi1, "field4"));

        Action action = ((MenuItem) mi1).getActions().get(0);
        assertTrue(action instanceof SageScreenAction);
        assertEquals("Browser - Videos", action.action().get());

        // test scripting
        System.out.println("Testing Scripting...");
        IMenuItem scriptItem = phoenix.menu.GetItemByName(m1, "testScript");
        assertNotNull("Missing script item", scriptItem);
        phoenix.menu.InvokeAction((MenuItem) scriptItem);
        System.out.println("Scripting Worked");

        // now test that the hidden props make the item hidden
        IMenuItem mi = phoenix.menu.GetItemByName(m1, "testStaticHidden");
        assertNotNull(mi);
        assertFalse(phoenix.menu.IsVisible(mi));

        phoenix.config.SetProperty("phoenix/debug/enableLogging", "false");
        mi = m1.getItemByName("testPropertyHidden");
        assertNotNull(mi);
        assertFalse("isVisible should be False", phoenix.menu.IsVisible(mi));

        // phoenix.api.SetProperty("phoenix/debug/enableLogging", true);
        // assertTrue("isVisible should be true",
        // phoenix.api.IsMenuItemVisible(mi));

        // now test the submenus
        mi = phoenix.menu.GetItemByName(m1, "submenu");
        assertNotNull(mi);
        assertEquals("Sub Menu", phoenix.menu.GetLabel(mi));
        assertTrue("submenu should be a Menu", phoenix.menu.IsMenu(mi));

        // see if the linked menu on the videos item is ok
        IMenuItem mi2 = phoenix.menu.GetItemByName(mi, "LinkedItem");
        assertNotNull("Didn't find the LinkedItem on the submenu", mi2);
        Menu linkedMenu = phoenix.menu.GetLinkedMenu(mi2);
        assertNotNull("Didn't find the LinkedMenu on the submenu", linkedMenu);
        assertEquals("Linked Menu", phoenix.menu.GetLabel(linkedMenu));

        Menu sub2 = (Menu) ((Menu) mi).getItemByName("AnotherSubMenu");
        assertEquals("back.jpg", phoenix.menu.GetBackground(sub2));
        assertEquals("icon.jpg", phoenix.menu.GetIcon(sub2));
        assertEquals("second.jpg", phoenix.menu.GetSecondaryIcon(sub2));
    }

    @Test
    public void testMenuCreate() throws IOException {
        Menu menu = phoenix.menu.CreateMenu(null, "MyMenu", "Label");
        phoenix.menu.Background(menu).setValue("back1.png");
        phoenix.menu.Help(menu).setValue("menu description");
        phoenix.menu.Icon(menu).setValue("micon.png");
        phoenix.menu.MenuType(menu).setValue("TV");
        phoenix.menu.Visible(menu).setValue("false");

        assertEquals("micon.png", menu.icon().get());
        assertEquals("micon.png", menu.icon().getValue());
        assertEquals("MyMenu", menu.getName());
        assertEquals("Label", menu.label().get());

        MenuItem mi = phoenix.menu.CreateMenuItem(menu, "i1", "item1");
        phoenix.menu.Background(mi).setValue("back.png");
        phoenix.menu.Help(mi).setValue("This is a test");
        phoenix.menu.Icon(mi).setValue("icon.jpg");
        phoenix.menu.LinkedMenu(mi).setValue("linked");
        phoenix.menu.Field(mi, "F1").setValue("f1");

        assertEquals("i1", mi.getName());
        assertEquals("item1", phoenix.menu.GetLabel(mi));
        assertEquals("item1", mi.label().getValue());
        assertEquals("back.png", mi.background().get());

        Action a1 = phoenix.menu.AddScreenAction(mi, "Test Screen");
        Action a2 = phoenix.menu.AddSageCommandAction(mi, "Back");
        Action a3 = phoenix.menu.AddExecuteCommandAction(mi, "ls", "-al", "Linux", ".", "testvar");
        Action a4 = phoenix.menu.AddSageEvalAction(mi, "MediaFiles=GetMediaFiles()");

        XmlMenuSerializer serializer = new XmlMenuSerializer();
        serializer.serialize(menu, System.out);

        File f = InitPhoenix.ProjectHome("target/testing/userdata/Phoenix/Menus/MyMenu.xml");
        if (f.exists())
            f.delete();
        assertFalse(f.exists());
        Phoenix.getInstance().getMenuManager().saveMenu(menu);
        f = InitPhoenix.ProjectHome("target/testing/userdata/Phoenix/Menus/MyMenu.xml");
        assertTrue(f.exists());

        Phoenix.getInstance().getMenuManager().loadConfigurations();
        Menu m = Phoenix.getInstance().getMenuManager().getMenu("MyMenu");
        assertNotNull(m);
        assertEquals("back1.png", m.background().get());

        // test moving items around
        IMenuItem mi2 = phoenix.menu.CreateMenuItem(menu, "l2", "item2");
        assertTrue(menu.getItems().size() == 2);
        assertEquals("item2", menu.getItems().get(1).label().get());

        phoenix.menu.InsertBefore(menu, mi2, mi);
        assertEquals("item2", menu.getItems().get(0).label().get());

        phoenix.menu.InsertAfter(menu, mi2, mi);
        assertEquals("item2", menu.getItems().get(1).label().get());

        // test moving actions around
        phoenix.menu.InsertAfter(mi, a1, a4);
        assertEquals("Test Screen", mi.getActions().get(3).action().get());

        phoenix.menu.InsertBefore(mi, a3, a2);
        assertEquals("ls", mi.getActions().get(0).action().get());

        System.out.println("Menu Create API passed");
    }

    @Test
    public void testGetMenus() {
        List<Menu> menus = Phoenix.getInstance().getMenuManager().getMenus();
        assertNotNull(menus);
        assertTrue(menus.size() > 0);
        int size = menus.size();

        assertEquals(size, phoenix.menu.GetMenus().size());
        assertEquals(size, phoenix.menu.GetMenuItems(null).size());
    }

    @Test
    public void testExpressions() throws FileNotFoundException, SAXException, IOException {
        List<Menu> menus = MenuBuilder.buildMenus(TestMenusFile, TestMenusDTDDir);
        assertNotNull(menus);

        Menu menu = menus.get(0);
        assertNotNull("Menu was Null", menu);

        for (IMenuItem i : menu) {
            System.out.println("Name: " + i.getName());
        }

        IMenuItem item = phoenix.menu.GetItemByName(menu, "testexpression");
        assertNotNull("item is null", item);

        assertNotNull(item.label().getValue());
        System.out.println(item.label().getValue());

        StubSageAPI api = (StubSageAPI) SageAPI.getProvider();
        api.addProxy("EvaluateExpression", new StubAPIProxy() {
            @Override
            public Object call(String s, Object[] objects) {
                System.out.println("CMD: " + s);
                if (objects!=null) {
                    for (Object o: objects) {
                        System.out.println("ARG: " + o);
                    }
                }
                return "My Test Label";
            }
        });
            System.out.println("ITEM LABEL: " + item.label().get());
            assertNotNull(item.label().get());
    }

    @Test
    public void testVFSViews() throws FileNotFoundException, SAXException, IOException {
        VirtualMediaFolder mf = new VirtualMediaFolder("Movies");
        mf.addMediaResource(new VirtualMediaFile("Movie1"));
        mf.addMediaResource(new VirtualMediaFile("Movie2"));

        ViewFactory f = new ViewFactory();
        f.setName("testvfsmovies");
        f.addFolderSource(new MediaFolderSourceFactory(mf));

        Phoenix.getInstance().getVFSManager().getVFSViewFactory().addFactory(f);

        ViewMenu vm = new ViewMenu(null);
        vm.setName("testvfsmovies");

        assertEquals(2, vm.getItems().size());
        MenuItem mi = (MenuItem) vm.getItems().get(0);
        assertEquals(2, mi.getActions().size());
        assertTrue(mi.getActions().get(0) instanceof SageAddStaticContextAction);
        assertTrue(mi.getActions().get(1) instanceof SageEvalAction);
        assertEquals("phoenix_umb_Play( VFSMenuMediaFile, GetUIContextName())", mi.getActions().get(1).action().getValue());
    }

    @Test
    public void testUserMenuOverride() throws IOException {
        System.out.println("SystemMenuDir: " + Phoenix.getInstance().getMenuManager().getSystemFiles().getDir());
        System.out.println("UserMenuDir: " + Phoenix.getInstance().getMenuManager().getUserFiles().getDir());

        FileUtils.copyFileToDirectory(TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();
        Menu testmenu = Phoenix.getInstance().getMenuManager().getMenu("TestMenu");
        assertNotNull(testmenu);

        MenuItem mi = new MenuItem(testmenu);
        mi.setName("newitem1");
        mi.label().set("My New Item");
        testmenu.addItem(mi);

        // save the menu
        Phoenix.getInstance().getMenuManager().saveMenu(testmenu);

        // load the new menus
        Phoenix.getInstance().getMenuManager().loadConfigurations();

        testmenu = Phoenix.getInstance().getMenuManager().getMenu("TestMenu");
        assertNotNull(testmenu);

        mi = (MenuItem) testmenu.getItemByName("newitem1");
        assertNotNull(mi);
        assertEquals("My New Item", mi.label().get());
    }

    @Test
    public void testUserVisibilityOverride() throws FileNotFoundException, SAXException, IOException {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("GetUIContextNames", null); // consider overriding api;
        api.overrideAPI("GetUIContextName", null); // consider overriding api;
        SageAPI.setProvider(api);

        List<Menu> menus = MenuBuilder.buildMenus(TestMenusFile, TestMenusDTDDir);
        assertNotNull(menus);

        Menu menu = menus.get(0);
        assertEquals("TestMenu", menu.getName());
        IMenuItem item = menu.getItemByName("item1");
        assertNotNull(item);
        assertTrue(phoenix.menu.IsVisible(item));

        phoenix.menu.SetVisible(item, false);
        assertFalse(phoenix.menu.IsVisible(item));

        Menu item2 = (Menu) menu.getItemByName("submenu");
        assertNotNull(item2);
        phoenix.menu.SetVisible(item2, false);

        // now reload the menu, and see if the visible was sticky
        menus = MenuBuilder.buildMenus(TestMenusFile, TestMenusDTDDir);
        assertNotNull(menus);

        menu = menus.get(0);
        assertEquals("TestMenu", menu.getName());
        item = menu.getItemByName("item1");
        assertNotNull(item);
        assertFalse(phoenix.menu.IsVisible(item));

        // now test the submenu item
        item = menu.getItemByName("submenu");
        assertNotNull(item);
        assertFalse(phoenix.menu.IsVisible(item));
    }

    @Test
    public void testFragments() throws IOException {
        SimpleStubAPI api = new SimpleStubAPI();
        SageAPI.setProvider(api);
        FileUtils.copyFileToDirectory(TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        FileUtils.copyFileToDirectory(new File(TestMenusFile.getParentFile(), "Fragments.xml"), InitPhoenix.ProjectHome(
                "target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();
        Menu m = Phoenix.getInstance().getMenuManager().getMenu("TestMenu");
        assertNotNull(m);

        // test insert after
        int pos1 = m.indexOf("submenu");
        int pos2 = m.indexOf("submenu2");
        assertTrue("Failed to insert after; positions; " + pos1 + "; " + pos2, pos1 != -1 && pos2 != -1 && pos2 == (pos1 + 1));

        // test insert before
        pos1 = m.indexOf("testStaticHidden");
        pos2 = m.indexOf("mylaunchfirefox");
        assertTrue("Failed to insert before; positions; " + pos1 + "; " + pos2, pos1 != -1 && pos2 != -1 && pos2 == (pos1 - 1));

        // test replace menu
        MenuItem mi = (MenuItem) m.getItemByName("testexpression");
        assertFalse("Menu should be overriden and hidden", mi.visible().get());
        assertEquals("Hide Expression", mi.label().get());
    }

    @Test
    public void testSorting() {
        Menu menu = new Menu(null);
        menu.setName("menu");
        MenuItem mi = new MenuItem(menu);
        mi.setName("mi7");
        menu.addItem(mi);
        for (int i = 0; i < 7; i++) {
            mi = new MenuItem(menu);
            mi.setName("mi" + i);
            mi.label().set("Menu Item " + i);
            menu.addItem(mi);
        }

        StaticMenuSorter sorter = new StaticMenuSorter(menu, "mi2:0,mi1:1,mi0:2,mi4:3,mi3:4");
        System.out.println("== Unsorted Menus");
        for (int i = 0; i < menu.getItems().size(); i++) {
            System.out.printf("%02d - %s\n", i, menu.getItems().get(i).getName());
        }
        Collections.sort(menu.getItems(), sorter);
        System.out.println("\n== Sorted Menus");
        for (int i = 0; i < menu.getItems().size(); i++) {
            System.out.printf("%02d - %s\n", i, menu.getItems().get(i).getName());
        }

        assertEquals("mi2", menu.getItems().get(0).getName());
        assertEquals("mi1", menu.getItems().get(1).getName());
        assertEquals("mi0", menu.getItems().get(2).getName());
        assertEquals("mi4", menu.getItems().get(3).getName());
        assertEquals("mi3", menu.getItems().get(4).getName());
        assertEquals("mi7", menu.getItems().get(5).getName());
        assertEquals("mi5", menu.getItems().get(6).getName());
        assertEquals("mi6", menu.getItems().get(7).getName());
    }

    @Test
    public void testMenuMoving() {
        Menu menu = new Menu(null);
        menu.setName("menu");
        for (int i = 0; i < 4; i++) {
            MenuItem mi = new MenuItem(menu);
            mi = new MenuItem(menu);
            mi.setName("mi" + i);
            mi.label().set("Menu Item " + i);
            menu.addItem(mi);
        }

        UtilAPI util = new UtilAPI();
        IMenuItem mi = menu.getItems().get(0);
        // move the first menu item up the list (should do nothing)
        util.MoveUp(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi0", "mi1", "mi2", "mi3"});

        util.MoveDown(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi0", "mi2", "mi3"});

        util.MoveDown(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi2", "mi0", "mi3"});

        util.MoveDown(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi2", "mi3", "mi0"});

        // should not move any further we are at the end
        util.MoveDown(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi2", "mi3", "mi0"});

        util.MoveUp(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi2", "mi0", "mi3"});

        util.MoveUp(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi1", "mi0", "mi2", "mi3"});

        util.MoveUp(menu.getItems(), mi);
        validateMenuOrder(menu, new String[]{"mi0", "mi1", "mi2", "mi3"});
    }

    private void validateMenuOrder(Menu menu, String[] ids) {
        for (int i = 0; i < ids.length; i++) {
            assertEquals(menu.getItems().get(i).getName(), ids[i]);
        }
    }

    @Test
    public void testMenuVisitor() {
        Menu menu = new Menu(null);
        menu.setName("menu");
        for (int i = 0; i < 4; i++) {
            MenuItem mi = new MenuItem(menu);
            mi = new MenuItem(menu);
            mi.setName("mi" + i);
            mi.label().set("Menu Item " + i);
            menu.addItem(mi);
        }
        Menu menu2 = new Menu(menu);
        for (int i = 0; i < 4; i++) {
            MenuItem mi = new MenuItem(menu2);
            mi = new MenuItem(menu2);
            mi.setName("mi" + i);
            mi.label().set("Menu Item " + i);
            menu2.addItem(mi);
        }
        menu.addItem(menu2);

        final List<IMenuItem> allitems = new ArrayList<IMenuItem>();
        menu.visit(new INodeVisitor<IMenuItem>() {
            @Override
            public void visit(IMenuItem node) {
                allitems.add(node);
            }
        });
        assertEquals(10, allitems.size());
    }

    @Test
    public void testMenuSaving() throws IOException {
        MenuItem mi = phoenix.menu.CreateMenuItem(null, "sls.testmenu1", "Test Menu 1");
        phoenix.menu.AddSageEvalAction(mi, "AddStaticContext()");
        phoenix.menu.AddScreenAction(mi, "UMB");

        assertTrue("failed to save menu fragment", phoenix.menu.SaveFragment(mi, "test.parent", null, "parent.after"));

        File frag = InitPhoenix.ProjectHome("target/testing/userdata/Phoenix/Menus/test.parent_sls.testmenu1_fragment.xml");
        String sfrag = FileUtils.readFileToString(frag);
        assertTrue(sfrag.contains("</fragment>"));
        assertTrue(sfrag.contains("name=\"sls.testmenu1\""));
        assertTrue(sfrag.contains("insertAfter=\"parent.after\""));
        System.out.println(sfrag);
    }

    @Test
    public void testMenuReferences() throws IOException {
        SageAPI.setProvider(new StubSageAPI());

        FileUtils.copyFileToDirectory(TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();

        Menu menu = Phoenix.getInstance().getMenuManager().getMenu("test.actions");
        assertEquals(4, menu.getChildCount());
        System.out.println("Menu: " + menu);

        IMenuItem item = menu.getItemByName("back");
        assertNotNull("Failed to find 'back' in list of items", item);
        assertTrue("'back' should type " + item.getClass().getName(), item instanceof DelegateMenuItem);
        assertEquals("Back", item.label().get());

        Menu sharedMenu = (Menu) menu.getItemByName("shared.options");
        assertNotNull("Failed to find reference menu 'shared.options'", sharedMenu);
        assertTrue("Shared Menu should be a delegate menu but is " + sharedMenu.getClass().getName(), sharedMenu instanceof DelegateMenu);
    }

    @Test
    public void testMenuReferencesWithSave() throws IOException {
        SageAPI.setProvider(new StubSageAPI());

        FileUtils.copyFileToDirectory(TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();

        Menu menu = Phoenix.getInstance().getMenuManager().getMenu("test.actions");
        assertEquals(4, menu.getChildCount());
        System.out.println("Menu: " + menu);

        XmlMenuSerializer menuSerializer = new XmlMenuSerializer();
        menuSerializer.serialize(menu, System.out);
    }

}
