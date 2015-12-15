package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.ov.XmlFolder;
import sagex.phoenix.vfs.ov.XmlOptions;
import sagex.phoenix.vfs.ov.XmlSourceFactory;
import sagex.phoenix.vfs.sources.RSSFeedFactory;
import test.InitPhoenix;

public class TestXmlFolders {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testRSSFeed() {
		String url = new File("../../src/test/java/test/junit/newtrailers.rss").toURI().toString();
		XmlOptions options = new XmlOptions();
		options.setFeedUrl(url);
		options.setRegex(FieldName.MediaTitle, "([^-]+)");
		options.addMetadataOption(FieldName.Misc, "title", "Title");
		options.setRegex(FieldName.Misc, ".*-\\s*(.*)");

		XmlFolder f = new XmlFolder(null, "Simple RSS", options);
		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(20, f.getChildren().size());

		assertEquals("The Artist", f.getChildren().get(0).getTitle());
		assertEquals("Featurette", ((IMediaFile) f.getChildren().get(0)).getMetadata().getMisc());
		assertEquals("http://trailers.apple.com/trailers/weinstein/theartist/", ((IMediaFile) f.getChildren().get(0)).getMetadata()
				.getMediaUrl());
	}

	@Test
	public void testRSSFeedFactory() {
		String url = new File("../../src/test/java/test/junit/newtrailers.rss").toURI().toString();

		RSSFeedFactory factory = new RSSFeedFactory();
		factory.setOptionValue("feedurl", url);
		factory.setOptionValue("mediatype", "Movie");

		XmlFolder f = (XmlFolder) factory.create(null);
		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(20, f.getChildren().size());

		assertEquals("The Artist - Featurette", f.getChildren().get(0).getTitle());
		assertEquals("http://trailers.apple.com/trailers/weinstein/theartist/", ((IMediaFile) f.getChildren().get(0)).getMetadata()
				.getMediaUrl());
	}

	@Test
	public void testAtomFeed() {
		String url = new File("../../src/test/java/test/junit/apple-atomfeed.xml").toURI().toString();
		XmlOptions options = new XmlOptions();
		options.setFeedUrl(url);
		options.setItemElement("entry");
		options.addMetadataOption(FieldName.MediaTitle, "title", "Title");
		options.addMetadataOption(FieldName.Description, "summary", "Title");
		options.addMetadataOption(FieldName.MediaUrl, "link@href", null);
		options.setRegex(FieldName.MediaTitle, "([^-]+)");
		options.addMetadataOption(FieldName.Misc, "title", null);
		options.setRegex(FieldName.Misc, ".*-\\s*(.*)");

		XmlFolder f = new XmlFolder(null, "Simple RSS", options);
		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(10, f.getChildren().size());

		assertEquals("The Help", f.getChildren().get(0).getTitle());
		assertEquals("http://a1151.v.phobos.apple.com/us/r1000/080/Video/eb/27/f3/mzm.xcbhexre..640x458.h264lc.d2.p.m4v",
				((IMediaFile) f.getChildren().get(0)).getMetadata().getMediaUrl());
	}

	@Test
	public void testAtomFeedFactory() {
		String url = new File("../../src/test/java/test/junit/apple-atomfeed.xml").toURI().toString();

		RSSFeedFactory factory = new RSSFeedFactory();
		factory.setOptionValue("feedurl", url);
		factory.setOptionValue("mediatype", "Movie");
		factory.setOptionValue("feedtype", "atom");
		XmlFolder f = (XmlFolder) factory.create(null);

		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(10, f.getChildren().size());

		assertEquals("The Help", f.getChildren().get(0).getTitle());
		assertEquals("http://a1151.v.phobos.apple.com/us/r1000/080/Video/eb/27/f3/mzm.xcbhexre..640x458.h264lc.d2.p.m4v",
				((IMediaFile) f.getChildren().get(0)).getMetadata().getMediaUrl());
	}

	@Test
	public void testMusicRSSFeed() {
		String url = new File("../../src/test/java/test/junit/hot-100.xml").toURI().toString();
		XmlOptions options = new XmlOptions();
		options.setFeedUrl(url);
		options.setMediaType("Music");
		options.addMetadataOption(FieldName.EpisodeName, "title", null);
		options.setRegex(FieldName.EpisodeName, "[0-9]+:\\s([^,]+),\\s*(.*)");

		XmlFolder f = new XmlFolder(null, "Music RSS", options);
		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(100, f.getChildren().size());

		assertEquals("We Found Love", f.getChildren().get(0).getTitle());
		assertEquals("http://www.billboard.com/charts/hot-100", ((IMediaFile) f.getChildren().get(0)).getMetadata().getMediaUrl());
		assertTrue(f.getChildren().get(0).isType(MediaResourceType.MUSIC.value()));
		assertTrue(f.getChildren().get(0).isType(MediaResourceType.ONLINE.value()));
	}

	@Test
	public void testMusicRSSFeed2() {
		String url = new File("../../src/test/java/test/junit/hot-100.xml").toURI().toString();
		XmlOptions options = new XmlOptions();
		options.setFeedUrl(url);
		options.setMediaType("Music");
		options.addMetadataOption(FieldName.Title, "title", null);
		options.setRegex(FieldName.Title, "[0-9]+:\\s([^,]+),\\s*(.*)");
		options.addMetadataOption(FieldName.Artist, "title", null);
		options.setRegex(FieldName.Artist, "[0-9]+:\\s[^,]+,\\s*(.*)");

		XmlFolder f = new XmlFolder(null, "Music RSS", options);
		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(100, f.getChildren().size());

		assertEquals("We Found Love", f.getChildren().get(0).getTitle());
		assertEquals("Rihanna Featuring Calvin Harris", ((IMediaFile) f.getChildren().get(0)).getAlbumInfo().getArtist());
		assertEquals("http://www.billboard.com/charts/hot-100", ((IMediaFile) f.getChildren().get(0)).getMetadata().getMediaUrl());
	}

	@Test
	public void testMusicRSSFeedViaFactory() {
		String url = new File("../../src/test/java/test/junit/hot-100.xml").toURI().toString();

		XmlSourceFactory factory = new XmlSourceFactory();
		factory.setOptionValue("feedurl", url);
		factory.setOptionValue("MediaType", "Music");
		factory.setOptionValue("EpisodeName-element", "title");
		factory.setOptionValue("EpisodeName-regex", "[0-9]+:\\s([^,]+),\\s*(.*)");

		XmlFolder f = (XmlFolder) factory.create(null);

		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(100, f.getChildren().size());

		assertEquals("We Found Love", f.getChildren().get(0).getTitle());
		assertEquals("http://www.billboard.com/charts/hot-100", ((IMediaFile) f.getChildren().get(0)).getMetadata().getMediaUrl());
	}

	@Test
	public void testItunesRSSViaXmlFactory() {
		// http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/xml
		// http://trailers.apple.com/trailers/home/rss/newtrailers.rss
		String url = new File("../../src/test/java/test/junit/itunes-topmovies.rss.xml").toURI().toString();

		XmlSourceFactory factory = new XmlSourceFactory();
		factory.setOptionValue("feedurl", url);
		factory.setOptionValue(XmlOptions.ITEM_ELEMENT, "entry");
		factory.setOptionValue("MediaType", "Movie");
		factory.setOptionValue("Artist-element", "im:artist");
		factory.setOptionValue("MediaTitle-element", "im:name");
		factory.setOptionValue("Description-element", "summary");

		XmlFolder f = (XmlFolder) factory.create(null);

		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(10, f.getChildren().size());

		assertEquals("Rise of the Planet of the Apes", f.getChildren().get(0).getTitle());
		assertEquals(
				"RISE OF THE PLANET OF THE APES is a revolution; an action-packed epic featuring stunning visual effects and creatures unlike anything ever seen before. At the story's heart is Caesar (Andy Serkis), a chimpanzee who gains human-like intelligence and emotions from an experimental drug. Raised like a child by the drug's creator (James Franco), Caesar ultimately finds himself taken from the humans he loves and imprisoned. Seeking justice, Caesar assembles a simian army and escapes -- putting man and primate on a collision course that could change the planet forever.",
				((IMediaFile) f.getChildren().get(0)).getMetadata().getDescription());
	}

	@Test
	public void testItunesRSSViaRSSFactory() {
		// http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/xml
		// http://trailers.apple.com/trailers/home/rss/newtrailers.rss
		String url = new File("../../src/test/java/test/junit/itunes-topmovies.rss.xml").toURI().toString();

		RSSFeedFactory factory = new RSSFeedFactory();
		factory.setOptionValue("feedurl", url);
		factory.setOptionValue("feedtype", "atom");

		XmlFolder f = (XmlFolder) factory.create(null);

		assertTrue("no children", f.getChildren().size() > 0);
		assertEquals(10, f.getChildren().size());

		assertEquals("Rise of the Planet of the Apes", f.getChildren().get(0).getTitle());
		assertEquals(
				"RISE OF THE PLANET OF THE APES is a revolution; an action-packed epic featuring stunning visual effects and creatures unlike anything ever seen before. At the story's heart is Caesar (Andy Serkis), a chimpanzee who gains human-like intelligence and emotions from an experimental drug. Raised like a child by the drug's creator (James Franco), Caesar ultimately finds himself taken from the humans he loves and imprisoned. Seeking justice, Caesar assembles a simian army and escapes -- putting man and primate on a collision course that could change the planet forever.",
				((IMediaFile) f.getChildren().get(0)).getMetadata().getDescription());
		IMetadata md = ((IMediaFile) f.getChildren().get(0)).getMetadata();
		assertEquals("Rupert Wyatt", md.getAlbumArtists().get(0).getName());
		assertEquals("Action & Adventure", md.getGenres().get(0));
		assertEquals(126960, md.getDuration());
	}

}
