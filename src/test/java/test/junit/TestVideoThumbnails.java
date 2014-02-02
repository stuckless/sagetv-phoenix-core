package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.vfs.sage.SageMediaFile;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestVideoThumbnails {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testVideoThumbnails() {
		SimpleStubAPI api = new SimpleStubAPI();
		SageAPI.setProvider(api);

		Airing mf = api.newMediaFile(1);
		mf.put("GetAiringDuration", 30l * 60 * 1000);
		mf.put("IsTVFile", false);
		mf.put("GetShowEpisode", "Pinky");
		mf.put("GetShowTitle", "Pinky");
		mf.put("GetMediaTitle", "Pinky");
		mf.METADATA.put("MediaType", "Movie");

		api.overrideAPI("GenerateThumbnail", new SimpleStubAPI.IAPI() {
			@Override
			public Object handleAPI(String name, Object[] args) throws Exception {
				File f = ((File) args[4]);
				f.getParentFile().mkdirs();
				f.createNewFile();
				return f;
			}
		});

		SageMediaFile smf = new SageMediaFile(null, mf);

		File files[] = phoenix.videothumbs.GenerateThumbnailsEvery(smf, 5 * 60, 200, 100);
		assertNotNull("Failed to create thumbs", files);
		assertEquals(files.length, 6);
		System.out.println("Have Thumbs: " + files.length);

		// doing it a second time should create new files, and delete the old
		// ones
		files = phoenix.videothumbs.GenerateThumbnailsEvery(smf, 5 * 60, 240, 120);
		assertNotNull("Failed to create thumbs", files);
		assertEquals(files.length, 6);
		System.out.println("Have Thumbs: " + files.length);
		for (File f : files) {
			System.out.println("File: " + f);
		}

		// test doing thumbs evenly
		files = phoenix.videothumbs.GenerateThumbnailsEvenly(smf, 10, 280, 140);
		assertNotNull("Failed to create thumbs", files);
		assertEquals(files.length, 10);
		System.out.println("Have Thumbs: " + files.length);
		for (File f : files) {
			System.out.println("File: " + f);
		}
	}
}
