package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.util.PhoenixManagedScriptEngineProxy;
import sagex.phoenix.vfs.ov.IUrlResolver;
import test.InitPhoenix;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestUrlResolvers {
    static Map<String, String> map = new HashMap<String, String>();

    @BeforeClass
    public static void setup() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testJavaScriptResolver() {
        File file = new File("../../src/test/java/test/junit/sampleJavascriptResolver.js");
        IUrlResolver res = PhoenixManagedScriptEngineProxy.newInstance(file, IUrlResolver.class);
        String url = "http://www.youtube.com/xx3ddfxxZ";
        assertEquals(true, res.canAccept(url));
        assertEquals("http://youtube.com/realfile.flv", res.getUrl(url));
        System.out.println("New Url: " + res.getUrl(url));
    }

    public void testYoutubeScriptResolver() {

    }
}
