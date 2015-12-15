package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.junit.lib.TestUtil.makeDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.ISageAPIProvider;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.ITVMetadataProvider;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataManager;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.HasIMDBID;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.impl.AlbumInfo;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestMetadataProviderFactory {
	static MetadataManager mgr;

	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);

		mgr = Phoenix.getInstance().getMetadataManager();
	}

	@Test
	public void testFactoryLoading() {
		for (IMetadataProvider p : mgr.getProviders()) {
			System.out.println("Provider: " + p);
		}

		IMetadataProvider prov = mgr.getProvider("imdb");
		assertNotNull("Failed to load imdb provider!", prov);
		assertNotNull(prov.getInfo().getName());
		assertNotNull(prov.getInfo().getIconUrl());
		assertNotNull(prov.getInfo().getDescription());
		assertEquals(MediaType.MOVIE, prov.getInfo().getSupportedSearchTypes().get(0));

		prov = mgr.getProvider("imdb.xml");
		assertNotNull("Failed to load xbmc imdb provider!", prov);
		assertNotNull(prov.getInfo().getName());
		assertNotNull(prov.getInfo().getIconUrl());
		assertNotNull(prov.getInfo().getDescription());
		assertEquals(MediaType.MOVIE, prov.getInfo().getSupportedSearchTypes().get(0));

		prov = mgr.getProvider("tvdb");
		assertNotNull("Failed to load tvdb provider!", prov);
		assertNotNull(prov.getInfo().getName());
		assertNotNull(prov.getInfo().getDescription());
		assertEquals(MediaType.TV, prov.getInfo().getSupportedSearchTypes().get(0));

		// -- SEAN -- DvdProfiler and MyMovies is removed, might get added as a separate plugin
		
//		prov = mgr.getProvider("dvdprofiler");
//		assertNotNull("Failed to load tvdb provider!", prov);
//		assertNotNull(prov.getInfo().getName());
//		assertNotNull(prov.getInfo().getDescription());
//		assertEquals(MediaType.MOVIE, prov.getInfo().getSupportedSearchTypes().get(0));

//		prov = mgr.getProvider("mymovies");
//		assertNotNull("Failed to load mymovies provider!", prov);
//		assertNotNull(prov.getInfo().getName());
//		assertNotNull(prov.getInfo().getDescription());
//		assertEquals(MediaType.MOVIE, prov.getInfo().getSupportedSearchTypes().get(0));

		assertTrue("getProviders() failed", mgr.getProviders().size() > 0);
		for (IMetadataProvider p : mgr.getProviders()) {
			System.out.println("Provider: " + p.getInfo().getId() + "; Name: " + p.getInfo().getName());
		}
	}



	@Deprecated
	@Test
	public void testIMDBLookup() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.MOVIE, "Iron Man 2", "2010");
		List<IMetadataSearchResult> results = mgr.search("imdb", query);
		assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);

		// ensure we get iron man
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals("tt1228705", result.getId());
		assertEquals("imdb", result.getProviderId());
		assertEquals(MediaType.MOVIE, result.getMediaType());
		assertEquals(2010, result.getYear());
		assertEquals("Iron Man 2", result.getTitle());
		assertEquals("http://www.imdb.com/title/tt1228705/", result.getUrl());

		// get the metadata, validate it
		IMetadata md = mgr.getMetdata(result);
		assertTrue(md.getActors().size() > 10);
		assertEquals("Robert Downey Jr.", md.getActors().get(0).getName());
		assertEquals("Tony Stark", md.getActors().get(0).getRole());

		assertNotNull(md.getDescription());
		// assertTrue(md.getDirectors().size() > 0);
		assertEquals("Iron Man 2", md.getEpisodeName());

		assertTrue(md.getFanart().size() > 1); // should have more than just a
												// poster
		System.out.println("# of Fanart Artifacts: " + md.getFanart().size());

		// for (IMediaArt ma : md.getFanart()) {
		// System.out.println("IMDB Fanart: " + ma);
		// }

		assertTrue(md.getGenres().size() > 0);
		assertEquals("Action", md.getGenres().get(0));

		assertEquals("tt1228705", md.getIMDBID());
		assertEquals("tt1228705", md.getMediaProviderDataID());
		assertEquals("imdb", md.getMediaProviderID());
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
		assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
		assertEquals("PG-13", md.getRated());
		assertTrue(md.getExtendedRatings().length() > 5);
		assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
		assertEquals("Iron Man 2", md.getEpisodeName());
		assertNull(md.getRelativePathWithTitle());

		System.out.println("User Rating: " + md.getUserRating());
		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 10);
		// assertTrue(md.getWriters().size() > 0);
		assertEquals(2010, md.getYear());
	}

	// IMDB will be removed at some point, so don't care that it doesn't work
//	@Deprecated
//	@Test
//	public void testIMDBLookupWithAmpData() throws Exception {
//		SearchQuery query = new SearchQuery(MediaType.MOVIE, "Fast & Furious", "2009");
//		List<IMetadataSearchResult> results = mgr.search("imdb", query);
//		assertTrue("Search for Fast & Furious return nothing!", results.size() > 0);
//
//		// ensure we get iron man
//		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
//		assertEquals("tt1013752", result.getId());
//		assertEquals("imdb", result.getProviderId());
//		assertEquals(MediaType.MOVIE, result.getMediaType());
//		assertEquals(2009, result.getYear());
//		assertEquals("Fast & Furious", result.getTitle());
//		assertEquals("http://www.imdb.com/title/tt1013752/", result.getUrl());
//
//		// get the metadata, validate it
//		IMetadata md = mgr.getMetdata(result);
//		assertTrue(md.getActors().size() > 10);
//		assertEquals("Vin Diesel", md.getActors().get(0).getName());
//		assertEquals("Dominic Toretto", md.getActors().get(0).getRole());
//
//		for (ICastMember cm : md.getActors()) {
//			assertFalse("Name has html entities", cm.getName().contains("&x"));
//			assertFalse("Role has html entities", cm.getRole().contains("&x"));
//		}
//
//		assertNotNull(md.getDescription());
//		// assertTrue(md.getDirectors().size() > 0);
//		assertEquals("Fast & Furious", md.getEpisodeName());
//
//		assertTrue(md.getFanart().size() > 1); // should have more than just a
//												// poster
//
//		assertTrue(md.getGenres().size() > 0);
//		assertEquals("Action", md.getGenres().get(0));
//
//		assertEquals("tt1013752", md.getIMDBID());
//		assertEquals("tt1013752", md.getMediaProviderDataID());
//		assertEquals("imdb", md.getMediaProviderID());
//		assertEquals("Fast & Furious", md.getMediaTitle());
//		assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
//		assertEquals(DateUtils.parseDate("2009-04-03").getTime(), md.getOriginalAirDate().getTime());
//		assertEquals("PG-13", md.getRated());
//		assertTrue(md.getExtendedRatings().length() > 5);
//		assertEquals(6420000, md.getRunningTime());
//		assertEquals("Fast & Furious", md.getEpisodeName());
//		assertNull(md.getRelativePathWithTitle());
//
//		System.out.println("User Rating: " + md.getUserRating());
//		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 10);
//		// assertTrue(md.getWriters().size() > 0);
//		assertEquals(2009, md.getYear());
//	}

	@Deprecated
	@Test
	public void testIMDBLookupBYID() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.MOVIE, "XXXXXXX", "2010");
		query.set(Field.PROVIDER, "imdb");
		query.set(Field.ID, "tt1228705");

		List<IMetadataSearchResult> results = mgr.search("imdb", query);
		assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);

		// ensure we get iron man
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals("tt1228705", result.getId());
		assertEquals("imdb", result.getProviderId());
		assertEquals(MediaType.MOVIE, result.getMediaType());
		assertEquals(2010, result.getYear());
		assertEquals("Iron Man 2", result.getTitle());

		// search by id, uses tt as the url
		// assertEquals("http://www.imdb.com/title/tt1228705/",
		// result.getUrl());

		// get the metadata, validate it
		long t1 = System.currentTimeMillis();
		IMetadata md = mgr.getMetdata(result);
		long t2 = System.currentTimeMillis();

		System.out.println("IMDB Lookup and parse took " + (t2 - t1) + "ms");

		assertTrue(md.getActors().size() > 10);
		assertEquals("Robert Downey Jr.", md.getActors().get(0).getName());
		assertEquals("Tony Stark", md.getActors().get(0).getRole());

		assertNotNull(md.getDescription());
		// assertTrue(md.getDirectors().size() > 0);
		// assertEquals("Jon Favreau", md.getDirectors().get(0).getName());

		// assertTrue(md.getWriters().size() > 0);
		// assertEquals("Justin Theroux (screenplay)",
		// md.getWriters().get(0).getName());

		assertEquals("Iron Man 2", md.getEpisodeName());

		assertTrue(md.getFanart().size() > 1); // should have more than just a
												// poster

		// for (IMediaArt ma : md.getFanart()) {
		// System.out.println("IMDB Fanart: " + ma);
		// }

		assertTrue(md.getGenres().size() > 0);
		assertEquals("Action", md.getGenres().get(0));

		assertEquals("tt1228705", md.getIMDBID());
		assertEquals("tt1228705", md.getMediaProviderDataID());
		assertEquals("imdb", md.getMediaProviderID());
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
		assertEquals(DateUtils.parseDate("7 May 2010").getTime(), md.getOriginalAirDate().getTime());
		assertEquals("PG-13", md.getRated());
		assertTrue(md.getExtendedRatings().length() > 5);
		assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
		assertEquals("Iron Man 2", md.getEpisodeName());
		assertNull(md.getRelativePathWithTitle());

		System.out.println("User Rating: " + md.getUserRating());
		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 5);
		assertEquals(2010, md.getYear());

	}

	@Test
	public void testTMDBLookup() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.MOVIE, "Iron Man 2", "2010");
		List<IMetadataSearchResult> results = mgr.search("tmdb", query);
		assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);

		// ensure we get iron man
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals("10138", result.getId());
		assertEquals("tmdb", result.getProviderId());
		assertEquals(MediaType.MOVIE, result.getMediaType());
		assertEquals(2010, result.getYear());
		assertEquals("Iron Man 2", result.getTitle());

		// get the metadata, validate it
		IMetadata md = mgr.getMetdata(result);
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertTrue(md.getActors().size() > 10);

		assertNotNull(md.getDescription());
		assertTrue(md.getDirectors().size() > 0);
		assertEquals("Iron Man 2", md.getEpisodeName());

		assertTrue(md.getFanart().size() > 1); // should have more than just a
												// poster

		assertTrue(md.getGenres().size() > 0);

		assertEquals("tt1228705", md.getIMDBID());
		assertEquals("10138", md.getMediaProviderDataID());
		assertEquals("tmdb", md.getMediaProviderID());
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
		assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
		assertEquals("PG-13", md.getRated());
		// no extended ratings in tmdb
		// assertTrue(md.getExtendedRatings().length()>4);
		assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
		assertEquals("Iron Man 2", md.getEpisodeName());
		assertNull(md.getRelativePathWithTitle());

		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
		assertTrue(md.getWriters().size() > 0);
		assertEquals(2010, md.getYear());
	}

	@Test
	public void testTMDBLookupBYID() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.MOVIE, "XXXXX", "2010");
		query.set(Field.PROVIDER, "tmdb");
		query.set(Field.ID, "10138");

		List<IMetadataSearchResult> results = mgr.search("tmdb", query);
		assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);
		for (IMetadataSearchResult r : results) {
			System.out.println("Result: " + r);
		}

		// ensure we get iron man
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals("10138", result.getId());
		assertEquals("tmdb", result.getProviderId());
		assertEquals(MediaType.MOVIE, result.getMediaType());
		assertEquals(2010, result.getYear());
		assertEquals("Iron Man 2", result.getTitle());

		// get the metadata, validate it
		IMetadata md = mgr.getMetdata(result);
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertTrue(md.getActors().size() > 10);
		assertTrue("Missing 'Robert Downey Jr.'", MetadataUtil.getActor("Robert Downey Jr.", md.getActors())!=null);

		assertNotNull(md.getDescription());
		assertTrue(md.getDirectors().size() > 0);
		assertEquals("Iron Man 2", md.getEpisodeName());

		assertTrue(md.getFanart().size() > 1); // should have more than just a
												// poster

		assertTrue(md.getGenres().size() > 0);

		assertEquals("tt1228705", md.getIMDBID());
		assertEquals("10138", md.getMediaProviderDataID());
		assertEquals("tmdb", md.getMediaProviderID());
		assertEquals("Iron Man 2", md.getMediaTitle());
		assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
		assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
		assertEquals("PG-13", md.getRated());
		// no extended ratings in tmdb
		// assertTrue(md.getExtendedRatings().length()>4);
		assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
		assertEquals("Iron Man 2", md.getEpisodeName());
		assertNull(md.getRelativePathWithTitle());

		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
		assertTrue(md.getWriters().size() > 0);
		assertEquals(2010, md.getYear());
	}
	
	@Test
	public void testTVDBApostrope() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "South Park", null);
		query.set(Field.PROVIDER, "tvdb");
		//query.set(Field.ID, "75897");
		query.set(Field.QUERY, "South Park");
		query.set(Field.SEASON, "19");
		query.set(Field.EPISODE, "4");

		List<IMetadataSearchResult> results = mgr.search("tvdb", query);
		for (IMetadataSearchResult r : results) {
			System.out.println("Result: " + r);
		}
		
		// ensure we get iron man
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
//		assertEquals("10138", result.getId());
//		assertEquals("tmdb", result.getProviderId());
//		assertEquals(MediaType.MOVIE, result.getMediaType());
//		assertEquals(2010, result.getYear());
//		assertEquals("Iron Man 2", result.getTitle());
		
		
		IMetadata md = mgr.getMetdata(result);
		System.out.println(md.getMediaTitle());
		System.out.println(md.getEpisodeName());
		System.out.println(md.getDescription());		
	}
	

	@Test
	public void testTMDBLookupWithSpecialCharacters() throws Exception {
		testSearch("tmdb", MediaType.MOVIE, "A Bug's Life", null, "A Bug's Life", "1998");
		testSearch("tmdb", MediaType.MOVIE, "The Incredibles", null, "The Incredibles", "2004");
		testSearch("tmdb", MediaType.MOVIE, "Monsters, Inc.", null, "Monsters, Inc.", "2001");
	}

	public void testSearch(String provider, MediaType type, String title, String year, String expectedTitle, String expectedYear)
			throws MetadataException {
		SearchQuery query = new SearchQuery(type, title, year);
		List<IMetadataSearchResult> results = mgr.search(provider, query);
		assertTrue("Search for " + title + " return nothing!", results.size() > 0);
		IMetadataSearchResult r = results.get(0);
		System.out.printf("Title: %s; %d; %s\n", r.getTitle(), r.getYear(), r.getScore());
		assertEquals(expectedTitle, r.getTitle());
		assertEquals(expectedYear, String.valueOf(r.getYear()));
	}

	@Test
	public void testTVDBBySeasonEpisode() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
		query.set(Field.SEASON, "2");
		query.set(Field.EPISODE, "7");

		testTVDBMetadata(query);
	}

	@Test
	public void testTVDBByID() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "GGGGG", "2004");
		query.set(Field.PROVIDER, "tvdb");
		query.set(Field.ID, "73255");

		query.set(Field.SEASON, "2");
		query.set(Field.EPISODE, "7");

		testTVDBMetadata(query);
	}

	@Test
	public void testTVDBByTitle() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
		query.set(Field.EPISODE_TITLE, "Hunting");

		testTVDBMetadata(query);
	}

	@Test
	public void testTVDBSeriesFanartOnly() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
		query.set(Field.EPISODE_TITLE, "123ASDASD999DSSDSD");

		List<IMetadataSearchResult> results = mgr.search("tvdb", query);
		assertTrue("Search for returned nothing!", results.size() > 0);
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals(2004, result.getYear());
		assertEquals("73255", result.getId());
		assertEquals("tvdb", result.getProviderId());
		assertEquals(MediaType.TV, result.getMediaType());
		assertEquals("House", result.getTitle());
		// tvdb just passes the id as url, it's never used, so it's just for
		// reference
		assertEquals("73255", result.getUrl());

		IMetadata md = mgr.getMetdata(result);
		assertTrue("Failed to get Series Only Fanart", md.getFanart().size() > 0);
		for (IMediaArt ma : md.getFanart()) {
			System.out.println("Fanart: " + ma.getDownloadUrl());
			assertTrue("Should not have season in fanart", ma.getSeason() == 0);
		}
	}

	@Test
	public void testTVDBByDate() throws Exception {
		SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
		query.set(Field.EPISODE_DATE, "2005-11-22");

		testTVDBMetadata(query);
	}

	private void testTVDBMetadata(SearchQuery query) throws Exception {
		List<IMetadataSearchResult> results = mgr.search("tvdb", query);
		assertTrue("Search for returned nothing!", results.size() > 0);

		// ensure we get twilight zone from 1985
		IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
		assertEquals(2004, result.getYear());
		assertEquals("73255", result.getId());
		assertEquals("tvdb", result.getProviderId());
		assertEquals(MediaType.TV, result.getMediaType());
		assertEquals("House", result.getTitle());
		// tvdb just passes the id as url, it's never used, so it's just for
		// reference
		assertEquals("73255", result.getUrl());

		// get the metadata, validate it
		IMetadata md = mgr.getMetdata(result);
		assertEquals("House", md.getMediaTitle());
		assertTrue(md.getActors().size() > 2);
		assertEquals("Hugh Laurie", md.getActors().get(0).getName());
		assertEquals("Dr. Gregory House", md.getActors().get(0).getRole());

		assertNotNull(md.getDescription());
		assertTrue(md.getDirectors().size() > 0);
		assertEquals("House", md.getRelativePathWithTitle());

		// episode specicif stuff
		assertEquals(2, md.getSeasonNumber());
		assertEquals(7, md.getEpisodeNumber());
		assertEquals("Hunting", md.getEpisodeName());
		assertTrue(md.getGuests().size() > 0);
		assertEquals("Hamilton Mitchell", md.getGuests().get(0).getName());

		assertTrue(md.getFanart().size() > 1); // should have more than just a
												// poster

		assertTrue(md.getGenres().size() > 0);
		assertTrue("Genres should have Drama but has: " + md.getGenres(),md.getGenres().contains("Drama"));

		assertEquals("tt0606027", md.getIMDBID());
		assertEquals("73255", md.getMediaProviderDataID());
		assertEquals("tvdb", md.getMediaProviderID());
		assertEquals("House", md.getMediaTitle());
		assertEquals(MediaType.TV.sageValue(), md.getMediaType());
		assertEquals(DateUtils.parseDate("2005-11-22").getTime(), md.getOriginalAirDate().getTime());
		assertEquals("TV14", md.getParentalRating());
		assertNull(md.getRated());
		assertEquals(0, md.getYear());
		// no extended ratings in tmdb
		// assertTrue(md.getExtendedRatings().length()>4);
		assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("45"), md.getRunningTime());
		assertEquals("House", md.getRelativePathWithTitle());
		assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
		assertTrue(md.getWriters().size() > 0);
	}

	@Test
	public void testTvSeriesInfo() throws MetadataException {
		IMetadataProvider prov = mgr.getProvider("tvdb");
		assertTrue(prov instanceof ITVMetadataProvider);
		ITVMetadataProvider tv = (ITVMetadataProvider) prov;
		IMetadataSearchResult res = mgr.createResultForId("tvdb", "73255");
		ISeriesInfo info = tv.getSeriesInfo(res.getId());

		assertNull("Series ID Must never have a value, unless it is a sagetv series info id", info.getSeriesInfoID());

		// show is cancelled, so no AirDOW
		//assertEquals("Monday", info.getAirDOW());
		// assertEquals("9:00 PM", info.getAirHrMin());
		assertEquals("TV14", info.getContentRating());
		assertTrue(info.getDescription() != null && info.getDescription().length() > 0);
		// assertEquals(null, info.getFinaleDate());
		// assertEquals(, info.getHistory());
		assertTrue(((String) info.getImage()).length() > 0);
		assertEquals("FOX (US)", info.getNetwork());
		assertTrue(info.getPremiereDate().length() > 0);
		assertEquals("House", info.getTitle());
		assertTrue(info.getUserRating() > 30);
		assertEquals("EP00688359", info.getZap2ItID());
		assertTrue(info.getGenres().size() > 0);
		assertTrue(info.getCast().size() > 0);

		for (String g : info.getGenres()) {
			System.out.println("Genre: " + g);
		}

		for (ICastMember cm : info.getCast()) {
			System.out.println("Cast: " + cm);
		}
	}

	@Deprecated
	@Test
	public void testAmiliaCompareWithAPIsForIMDB() throws MetadataException {
		SearchQuery q = new SearchQuery(Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
		q.setMediaType(MediaType.MOVIE);
		q.set(Field.CLEAN_TITLE, "Amelia");
		q.set(Field.YEAR, "2009");
		q.set(Field.EPISODE_DATE, "2009-10-23");
		q.set(Field.RAW_TITLE, "Amelia");
		q.set(Field.EPISODE_TITLE, "Amelia");

		List<IMetadataSearchResult> results = Phoenix.getInstance().getMetadataManager().search("imdb", q);
		assertTrue(results.size() > 0);
		for (IMetadataSearchResult r : results) {
			System.out.printf("result: %s %s %s\n", r.getTitle(), r.getYear(), r.getScore());
		}

		IMetadata md = Phoenix.getInstance().getMetadataManager().getMetdata(results.get(0));
		assertTrue(md.getFanart().size() > 1);
		for (IMediaArt f : md.getFanart()) {
			System.out.printf("fanart: %s %s\n", f.getType(), f.getDownloadUrl());
		}
		// type=MOVIE, fields={CLEAN_TITLE: Amelia,YEAR: 2009,EPISODE_DATE:
		// 2009-10-23,RAW_TITLE: Amelia,EPISODE_TITLE: Amelia,}, hints=Hints
		// [hints={update_fanart: true,update_metadata:
		// true,scan_missing_metadata: true,scan_subfolders:
		// true,import_tv_as_recording: false,}]]; Because it's a SageTV Movie

		IMetadataSearchResult res[] = phoenix.metadatascan.Search(q);
		assertTrue(res.length > 0);
		IMetadata md2 = phoenix.metadatascan.GetMetadata(res[0]);
		assertTrue(md2.getFanart().size() > 1);
		// assertEquals(md2.getFanart().size(), md.getFanart().size());
	}

	@Test
	public void testHTBackdropsMusic() throws MetadataException {
		SearchQuery q = new SearchQuery(Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
		q.setMediaType(MediaType.MUSIC);
		q.set(Field.ARTIST, "Madonna");

		List<IMetadataSearchResult> results = Phoenix.getInstance().getMetadataManager().search("htb", q);
		assertTrue(results.size() > 0);
		for (IMetadataSearchResult r : results) {
			System.out.printf("result: %s %s %s\n", r.getTitle(), r.getId(), r.getScore());
		}

		IMetadata md = Phoenix.getInstance().getMetadataManager().getMetdata(results.get(0));
		assertTrue(md.getFanart().size() > 1);
		for (IMediaArt f : md.getFanart()) {
			System.out.printf("fanart: %s %s\n", f.getType(), f.getDownloadUrl());
		}

		System.out.println("---------------------");

		// now check if we get a hit using search by id
		q = new SearchQuery(Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
		q.setMediaType(MediaType.MUSIC);
		q.set(Field.ID, "79239441-bfd5-4981-a70c-55c3f15c1287");

		results = Phoenix.getInstance().getMetadataManager().search("htb", q);
		assertEquals(results.size(), 1);
		for (IMetadataSearchResult r : results) {
			System.out.printf("result: %s %s %s\n", r.getTitle(), r.getId(), r.getScore());
			assertTrue(r.getScore() >= 1.0f);
		}

		md = Phoenix.getInstance().getMetadataManager().getMetdata(results.get(0));
		assertTrue(md.getFanart().size() > 1);
		for (IMediaArt f : md.getFanart()) {
			System.out.printf("fanart: %s %s\n", f.getType(), f.getDownloadUrl());
		}
		assertEquals("Madonna", md.getMediaTitle());
	}

	@Test
	public void testSaveMusicFanart() throws MetadataException, InterruptedException {
		SimpleStubAPI api = new SimpleStubAPI();

		Airing smf = api.newMediaFile(1);
		smf.put("IsMusicFile", true);
		smf.put("IsTVFile", false); // consider overriding api;
									// {IsAiringObject=true, IsMusicFile=true,
									// IsMediaFileObject=true,
									// GetMediaFileID=1}:
									// test.junit.lib.SimpleStubAPI$Airing,
		smf.put("GetShowEpisode", ""); // consider overriding api;
										// {IsAiringObject=true,
										// IsMusicFile=true,
										// IsMediaFileObject=true,
										// GetMediaFileID=1}:
										// test.junit.lib.SimpleStubAPI$Airing,
		smf.put("GetShowTitle", ""); // consider overriding api;
										// {IsAiringObject=true,
										// IsMusicFile=true,
										// IsMediaFileObject=true,
										// GetMediaFileID=1}:
										// test.junit.lib.SimpleStubAPI$Airing,
		smf.put("GetMediaTitle", "Material Girl"); // consider overriding api;
													// {IsAiringObject=true,
													// IsMusicFile=true,
													// IsMediaFileObject=true,
													// GetMediaFileID=1}:
													// test.junit.lib.SimpleStubAPI$Airing,

		api.overrideAPI("GetAlbumForFile", new AlbumInfo());
		api.overrideAPI("GetAlbumArtist", "Madonna"); // consider overriding
														// api;
														// sagex.phoenix.vfs.impl.AlbumInfo@272b72f4:
														// sagex.phoenix.vfs.impl.AlbumInfo,
		api.overrideAPI("GetAlbumName", "Like A Virgin"); // consider overriding
															// api;
															// sagex.phoenix.vfs.impl.AlbumInfo@272b72f4:
															// sagex.phoenix.vfs.impl.AlbumInfo,

		SageAPI.setProvider(api);

		IMediaFile mf = new SageMediaFile(null, smf);
		assertTrue("not a music file", mf.isType(MediaResourceType.MUSIC.value()));
		Hints options = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
		SearchQuery q = Phoenix.getInstance().getMetadataManager().createQuery(mf, options);
		assertEquals("not a music search", q.getMediaType(), MediaType.MUSIC);
		assertEquals(q.get(Field.RAW_TITLE), "Material Girl");
		assertEquals(q.get(Field.ARTIST), "Madonna");
		assertEquals(q.get(Field.ALBUM), "Like A Virgin");

		assertTrue("Can't accept music query??", Phoenix.getInstance().getMetadataManager().canScanMediaFile(mf, options));

		// setup central fanart
		File fanartDir = makeDir("test/FanartFolder");
		phoenix.fanart.SetFanartCentralFolder(fanartDir.getAbsolutePath());
		String sdir = phoenix.fanart.GetFanartCentralFolder();
		System.out.println("Central Folder: " + sdir);

		// automatic update, and then check for fanart
		Phoenix.getInstance().getMetadataManager().automaticUpdate(mf, options);

		System.out.println("Sleeping while downloading...");
		Thread.currentThread().sleep(10 * 1000);

		// check files
		File music = new File(fanartDir, "Music/Madonna/Posters");
		assertTrue("Failed to create fanart dir: " + music, music.exists());
		File files[] = music.listFiles();
		assertTrue("failed to download fanart", files != null && files.length > 0);
		for (File f : files) {
			System.out.println(f);
		}
	}

	@Test
	public void testTVDBProblemMatchers() {
		testTVDBTitle("American Dad!", "73141", "American Dad!");
		testTVDBTitle("American Dad", "73141", "American Dad!");
	}

	private void testTVDBTitle(String search, String resultId, String resultTitle) {
		try {
			SearchQuery query = new SearchQuery(MediaType.TV, search, null);
			query.set(Field.SEASON, "1");
			query.set(Field.EPISODE, "1");

			List<IMetadataSearchResult> results = mgr.search("tvdb", query);
			assertTrue("Search for returned nothing!", results.size() > 0);

			// ensure we get twilight zone from 1985
			IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
			assertEquals(resultId, result.getId());
			assertEquals("tvdb", result.getProviderId());
			assertEquals(MediaType.TV, result.getMediaType());
			assertEquals(resultTitle, result.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage() + " -- " + search);
		}
	}

	@Test
	public void testDefaults() {
		List<IMetadataProvider> provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.TV);
		assertEquals("tvdb", provs.get(0).getInfo().getId());
		provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.MOVIE);
		assertEquals("tmdb", provs.get(0).getInfo().getId());
	}
}
