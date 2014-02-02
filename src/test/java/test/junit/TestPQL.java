package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.api.MediaFileAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.db.PQLParser;
import sagex.phoenix.db.ParseException;
import sagex.phoenix.progress.NullProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.filters.IResourceFilter;
import sagex.phoenix.vfs.sage.MediaFilesMediaFolder;
import sagex.phoenix.vfs.visitors.CollectorResourceVisitor;
import sagex.phoenix.vfs.visitors.FilteredResourceVisitor;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestPQL {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testPQL() throws ParseException {
		// these tests just test if the parser will fail or not

		PQLParser parser = new PQLParser("SeasonNumber > 1 and (EpisodeNumber = '5' or EpisodeNumber = '7')");
		parser.parse();
		System.out.println("PARSED: " + parser.getQuery());

		parser = new PQLParser("((SeasonNumber > 1) and ((EpisodeNumber = '5' or EpisodeNumber = '7')))");
		parser.parse();
		System.out.println("PARSED: " + parser.getQuery());

		parser = new PQLParser("(SeasonNumber > 1 and EpisodeNumber = '5') or (EpisodeNumber = '7' and SeasonNumber = '3')");
		parser.parse();
		System.out.println("PARSED: " + parser.getQuery());

		parser = new PQLParser("Genre contains 'Horror' or Genre contains 'Thriller'");
		parser.parse();
		System.out.println("PARSED: " + parser.getQuery());

		parser = new PQLParser("Genre is null or UserRating is not null");
		parser.parse();
		System.out.println("PARSED: " + parser.getQuery());

		try {
			parser = new PQLParser("Full House");
			parser.parse();
			fail("Parser should have failed");
		} catch (Exception e) {
			// should fail
			System.out.println(e.getMessage());
		}

		try {
			parser = new PQLParser("Full House Time");
			parser.parse();
			fail("Parser should have failed");
		} catch (Exception e) {
			// should fail
			System.out.println(e.getMessage());
		}

		try {
			parser = new PQLParser("SeasonNumber != 10");
			parser.parse();
			fail("Parser should have failed, != is invalid");
		} catch (Throwable e) {
			// should fail
			System.out.println(e.getMessage());
		}

		try {
			parser = new PQLParser("Season is null");
			parser.parse();
			fail("Parser should have failed, season is invalid");
		} catch (Throwable e) {
			// should fail
			System.out.println(e.getMessage());
		}

	}

	@Test
	public void testQuery() throws ParseException {
		SimpleStubAPI api = new SimpleStubAPI();
		int id = 1;
		Airing mf = api.newMediaFile(id++);
		mf.put("GetMediaTitle", "House");
		mf.put("IsTVFile", true);
		mf.put("GetShowTitle", "House");
		mf.put("GetShowEpisode", "Pilot");
		mf.METADATA.put("Title", "House");
		mf.METADATA.put("MediaType", "TV");
		mf.METADATA.put("SeasonNumber", "2");

		mf = api.newMediaFile(id++);
		mf.put("GetMediaTitle", "House");
		mf.put("IsTVFile", true);
		mf.put("GetShowTitle", "House");
		mf.put("GetShowEpisode", "Pilot");
		mf.METADATA.put("Title", "House");
		mf.METADATA.put("MediaType", "TV");
		mf.METADATA.put("SeasonNumber", "3");

		mf = api.newMediaFile(id++);
		mf.put("GetMediaTitle", "Bones");
		mf.put("IsTVFile", true);
		mf.put("GetShowTitle", "Bones");
		mf.put("GetShowEpisode", "Bone Matter");
		mf.METADATA.put("Title", "Bones");
		mf.METADATA.put("MediaType", "TV");

		mf = api.newMediaFile(id++);
		mf.put("GetMediaTitle", "Bones");
		mf.put("IsTVFile", true);
		mf.put("GetShowTitle", "Bones");
		mf.put("GetShowEpisode", "Bones");
		mf.METADATA.put("Title", "Bones");
		mf.METADATA.put("MediaType", "");
		SageAPI.setProvider(api);

		Object files[] = MediaFileAPI.GetMediaFiles();
		MediaFilesMediaFolder folder = new MediaFilesMediaFolder(null, files, "Files");

		// test simple and query
		PQLParser parser = new PQLParser("Title = 'House' and SeasonNumber > 2");
		parser.parse();
		IResourceFilter filter = parser.getFilter();

		CollectorResourceVisitor vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(1, vis.getCollection().size());

		// test is null
		parser = new PQLParser("MediaType is null");
		parser.parse();
		filter = parser.getFilter();

		vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(1, vis.getCollection().size());

		// test is not null
		parser = new PQLParser("MediaType is not null");
		parser.parse();
		filter = parser.getFilter();

		vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(3, vis.getCollection().size());

		// test equals
		parser = new PQLParser("Title = 'Bones'");
		parser.parse();
		filter = parser.getFilter();

		vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(2, vis.getCollection().size());

		// test contains
		parser = new PQLParser("Title contains 'one'");
		parser.parse();
		filter = parser.getFilter();

		vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(2, vis.getCollection().size());

		// test complex grouping
		parser = new PQLParser(
				"Title = 'Glee' or ((Title = 'House' and SeasonNumber = '4') or (Title='Bones' and MediaType = 'TV'))");
		parser.parse();
		filter = parser.getFilter();

		vis = new CollectorResourceVisitor(MediaResourceType.FILE);
		folder.accept(new FilteredResourceVisitor(filter, vis), NullProgressMonitor.INSTANCE, IMediaResource.DEEP_UNLIMITED);
		assertEquals(1, vis.getCollection().size());
		assertEquals("Bones", ShowAPI.GetShowTitle(vis.getCollection().get(0).getMediaObject()));
		assertEquals("Bone Matter", ShowAPI.GetShowEpisode(vis.getCollection().get(0).getMediaObject()));
	}
}
