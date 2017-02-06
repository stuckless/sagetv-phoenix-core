package sagex.phoenix.remote.gson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.menu.Action;
import sagex.phoenix.menu.IMenuItem;
import sagex.phoenix.menu.Menu;
import sagex.phoenix.menu.MenuItem;
import sagex.phoenix.menu.NamedAction;
import sagex.phoenix.util.var.DynamicVariable;
import sagex.phoenix.util.var.Variable;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;
import test.junit.TestMenus;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * Created by seans on 01/01/17.
 */
public class GsonSerializerTest {
    @BeforeClass
    public static void init() throws IOException {
        TestMenus.init();
    }

    @Test
    public void testMenuSerializer() throws IOException {
        SageAPI.setProvider(new StubSageAPI());

        FileUtils.copyFileToDirectory(TestMenus.TestMenusFile, InitPhoenix.ProjectHome("target/testing/STVs/Phoenix/Menus"));
        Phoenix.getInstance().getMenuManager().loadConfigurations();

        Menu menu = Phoenix.getInstance().getMenuManager().getMenu("TestMenu");
        Gson gson = PhoenixGSONBuilder.getGsonInstance();
        String json = gson.toJson(menu);
        System.out.println("===== BEGIN MENU =====");
        System.out.println(json);
        System.out.println("===== END MENU =====");
    }

    @Test
    public void testFactorySerializer() {
        SageAPI.setProvider(new StubSageAPI());
        List<ViewFactory> factories = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories();
        Gson gson = PhoenixGSONBuilder.getGsonPrettyInstance();
        String json = gson.toJson(factories.get(0));
        System.out.println("===== BEGIN FACTORIES =====");
        System.out.println(json);
        System.out.println("===== END FACTORIES =====");
    }
}