package sagex.phoenix.remote.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;

public class ScriptingEngingTest {
    private static interface MyInterface {
        public String callMe(String name);
    }

    private static final String CLIENTID = "junitclient";

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testScriptEngine() throws IOException, NoSuchMethodException, ScriptException {
        String script = IOUtils.toString(ScriptingEngingTest.class.getResourceAsStream("testscript.js"));
        final ScriptingServiceFactory serv = Phoenix.getInstance().getScriptingServices();
        serv.registerScript(CLIENTID, script);

        // test if the engine calls __onLoad()
        boolean loaded = (Boolean) serv.callService(CLIENTID, "isLoaded", null);
        assertTrue("__onload not called in script", loaded);

        // test that we can map a js function to an interface method
        MyInterface r = serv.newInterfaceHandler(MyInterface.class, CLIENTID, "callMe", "testMyInterfaceCall");
        String val = r.callMe("123");
        assertEquals(val, "123");
        System.out.println("MyInteface: " + r.toString());
    }
}
