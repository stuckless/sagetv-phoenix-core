package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.search.FileMatcher;
import sagex.phoenix.metadata.search.FileMatcherManager.FileMatcherXmlBuilder;
import sagex.phoenix.metadata.search.ID;
import sagex.phoenix.metadata.search.XmlFileMatcherSerializer;
import test.InitPhoenix;

public class TestFileMatchers {

	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testXmlSerializer() throws IOException, ParserConfigurationException, SAXException {
		List<FileMatcher> matchers = new ArrayList<FileMatcher>();
		FileMatcher fm = new FileMatcher();
		fm.setFileRegex(Pattern.compile("[\\/]File-"));
		fm.setMediaType(MediaType.TV);
		fm.setMetadata(new ID("prov", "providerid"));
		fm.setFile(new File("test.avi"));
		fm.setTitle("MyTitle");
		fm.setYear("2011");
		fm.setFanart(new ID("fanart", "fanartid"));
		matchers.add(fm);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XmlFileMatcherSerializer ser = new XmlFileMatcherSerializer();
		ser.serialize(matchers, baos);

		// now parse the xml...
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser parser = saxFactory.newSAXParser();
		FileMatcherXmlBuilder builder = new FileMatcherXmlBuilder(new File("testmatchers.xml"));
		parser.parse(bais, builder);

		List<FileMatcher> newmatchers = builder.getMatchers();
		assertEquals(1, newmatchers.size());
		FileMatcher m = newmatchers.get(0);
		assertEquals(m.getYear(), "2011");
		assertEquals(m.getTitle(), "MyTitle");
		assertEquals(m.getFile().getName(), "test.avi");
		assertEquals(m.getFileRegex().pattern(), Pattern.compile("[\\/]File-").pattern());
		assertEquals(m.getMediaType(), MediaType.TV);
		assertEquals(m.getMetadata().getName(), "prov");
		assertEquals(m.getMetadata().getValue(), "providerid");
		assertEquals(m.getFanart().getName(), "fanart");
		assertEquals(m.getFanart().getValue(), "fanartid");
	}

	@Test
	public void testSaveMediaTitles() throws Exception {
		FileMatcher fm = new FileMatcher();
		fm.setFileRegex(Pattern.compile("[\\\\/]File-"));
		fm.setMediaType(MediaType.TV);
		fm.setMetadata(new ID("prov", "providerid"));
		fm.setTitle("MyTitle");
		fm.setYear("2011");
		fm.setFanart(new ID("fanart", "fanartid"));
		Phoenix.getInstance().getMediaTitlesManager().addRegexMatcher(fm);
		assertNotNull(fm.getId());
		assertEquals(1, Phoenix.getInstance().getMediaTitlesManager().saveMatchers());
	}
}
