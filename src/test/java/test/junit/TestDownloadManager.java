package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.download.DownloadConfiguration;
import sagex.phoenix.download.DownloadHandler;
import sagex.phoenix.download.DownloadItem;
import sagex.phoenix.download.DownloadManager;
import test.InitPhoenix;

public class TestDownloadManager implements DownloadHandler {
    DownloadItem good = null;
    DownloadItem start = null;
    DownloadItem error = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testDownloadManager() throws Exception {
        DownloadManager mgr = new DownloadManager();

        File f1 = InitPhoenix.ProjectHome("target/junit/logo.gif");
        if (f1.exists()) f1.delete();
        DownloadItem item = new DownloadItem(new URL("http://www.google.ca/intl/en_ALL/images/logos/images_logo_lg.gif"), f1);
        item.setHandler(this);
        mgr.download(item);

        Thread.currentThread().sleep(2000);
        assertTrue("Did not download", good != null && error == null && start != null);
        assertTrue("Did not download file!", f1.exists() && f1.length() > 0);

        good = error = start = null;
        File f2 = InitPhoenix.ProjectHome("target/junit/logo2.gif");
        DownloadItem item2 = new DownloadItem(new URL("http://www.google.ca/sdsdsssdf.gif"), f2);
        item2.setHandler(this);
        item2.setMaxReties(10);
        mgr.download(item2);
        Thread.currentThread().sleep(25000);

        DownloadConfiguration config = GroupProxy.get(DownloadConfiguration.class);
        // assertEquals(mgr.getStatus().threads,2);
        assertTrue("downloaded", good == null && error != null && start != null);
        assertTrue("downloaded file!", !f2.exists());

        System.out.println("Done");
    }

    @Override
    public void onComplete(DownloadItem item) {
        System.out.println("Downloaded: " + item);
        good = item;
    }

    @Override
    public void onError(DownloadItem item) {
        System.out.println("Error: " + item);
        item.getError().printStackTrace();

        error = item;
    }

    @Override
    public void onStart(DownloadItem item) {
        System.out.println("Starting: " + item);
        start = item;
    }
}
