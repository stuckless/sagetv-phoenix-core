package sagex.phoenix.remote.streaming;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;

public class MediaStreamerManagerTest {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testVLCStream() throws InterruptedException {
		MediaRequest req = new MediaRequest();
		req.setBaseUrl("http://192.168.1.12/test123/"); // clientid should be in the base url
		req.setClientId("test123");
		req.setMediaId("mf123");
		req.setOutputDir("/home/sls/WWW/" + req.getClientId());
		req.setSingleSource("/home/sls/BETA/MEDIA/Video/REALVIDEO/AncientAliens-GodsAliens-3611836-0.ts");
		MediaResponse resp = Phoenix.getInstance().getMediaStreamer().createRequest(req);
		
		assertEquals("http://192.168.1.12/test123/mf123.m3u8", resp.getControlInfo().getMediaUrl());
		
		Thread.currentThread().sleep(10000);
		Phoenix.getInstance().getMediaStreamer().abortProcess(req.getClientId());
	}
	
	@Test
	public void testGenericCommandStream() throws InterruptedException {
		Phoenix.getInstance().getMediaStreamer().getConfig().getScriptConfig().setCommand("/home/sls/SparkleShare/PHOENIX/Phoenix/src/main/STVs/Phoenix/streaming/vlcstream.sh");
		MediaRequest req = new MediaRequest();
		req.setRequestingGenericStreamer(true);
		req.setBaseUrl("http://192.168.1.12/test123/"); // clientid should be in the base url
		req.setClientId("test123");
		req.setMediaId("mf123");
		req.setOutputDir("/home/sls/WWW/" + req.getClientId());
		req.setSingleSource("/home/sls/BETA/MEDIA/Video/REALVIDEO/AncientAliens-GodsAliens-3611836-0.ts");
		MediaResponse resp = Phoenix.getInstance().getMediaStreamer().createRequest(req);
		
		assertEquals("http://192.168.1.12/test123/mf123.m3u8", resp.getControlInfo().getMediaUrl());
		
		Thread.currentThread().sleep(10000);
		Phoenix.getInstance().getMediaStreamer().abortProcess(req.getClientId());
	}
}
