package sagex.phoenix.util.url;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.MetadataConfiguration;
import test.InitPhoenix;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.*;

public class UrlUtilTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void openUrlConnection() throws IOException {
        URL url = new URL("https://www.thetvdb.com/banners/posters/311790-3.jpg");
        System.out.println("opening: " +url);
        URLConnection conn = UrlUtil.openUrlConnection(url, null, null, 0, true);
        assertNotNull(conn);
        System.out.println("opened: " +url);
    }
}