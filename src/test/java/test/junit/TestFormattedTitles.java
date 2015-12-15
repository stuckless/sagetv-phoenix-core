package test.junit;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import test.InitPhoenix;

public class TestFormattedTitles {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testFormattedTVTitles() {
		IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/House S01E01.avi"));
		IMetadata md = mf.getMetadata();
		md.setMediaType("TV");
		md.setEpisodeName("Test Episode");
		md.setMediaTitle("House");
		md.setRelativePathWithTitle("House");

		md = mf.getMetadata();

		String title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("House - Test Episode", title);

		md.setSeasonNumber(1);
		md.setEpisodeNumber(5);
		title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("House - S01E05 - Test Episode", title);
	}

	@Test
	public void testFormattedMoviesTitles() {
		IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Movie.avi"));
		IMetadata md = mf.getMetadata();
		md.setMediaType("Movie");
		md.setEpisodeName("Test Movie");
		md.setMediaTitle("Test Movie");
		md.setRelativePathWithTitle("Test Movie");

		md = mf.getMetadata();

		String title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie", title);

		md.setYear(2010);
		title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie (2010)", title);

		md.setDiscNumber(2);
		title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie (2010) - Disc 02", title);
	}

	@Test
	public void testFormattedMoviesTitlesWithPath() {
		IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Movie.avi"));
		IMetadata md = mf.getMetadata();
		md.setMediaType("Movie");
		md.setMediaTitle("Test Movie");
		md.setEpisodeName("Movies/Test Movie");

		md = mf.getMetadata();

		String title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie", title);

		md.setYear(2010);
		title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie (2010)", title);

		md.setDiscNumber(2);
		title = phoenix.media.GetFormattedTitle(mf);
		assertEquals("Test Movie (2010) - Disc 02", title);
	}

}
