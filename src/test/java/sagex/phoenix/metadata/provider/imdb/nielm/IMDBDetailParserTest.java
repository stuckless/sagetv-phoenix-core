package sagex.phoenix.metadata.provider.imdb.nielm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.provider.nielm.NielmIMDBMetaDataProvider;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;
import test.InitPhoenix;

public class IMDBDetailParserTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testDetails() throws MetadataException, IOException {
        NielmIMDBMetaDataProvider provider = (NielmIMDBMetaDataProvider) Phoenix.getInstance().getMetadataManager()
                .getProvider("nielm_imdb");
        assertNotNull("Nielm IMDB is not registered.", provider);

        IMetadata md = provider.getMetaDataByUrl("http://www.imdb.com/title/tt1228705/");

        assertEquals("Iron Man 2", md.getMediaTitle());
        assertTrue(md.getActors().size() > 10);
        assertEquals("Robert Downey Jr.", md.getActors().get(0).getName());
        assertEquals("Tony Stark", md.getActors().get(0).getRole());

        assertNotNull(md.getDescription());
        assertTrue(md.getDescription().length() > 50);

        assertTrue(md.getDirectors().size() > 0);
        assertEquals("Iron Man 2", md.getEpisodeName());

        assertTrue(md.getGenres().size() > 0);
        assertEquals("Action", md.getGenres().get(0));

        assertEquals("tt1228705", md.getIMDBID());
        assertEquals("tt1228705", md.getMediaProviderDataID());
        assertEquals("nielm_imdb", md.getMediaProviderID());
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
        assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("PG-13", md.getRated());
        assertTrue(md.getExtendedRatings().length() > 4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
        assertEquals("Iron Man 2", md.getEpisodeName());
        assertNull(md.getRelativePathWithTitle());

        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);

        assertTrue(md.getWriters().size() > 0);
        assertTrue(md.getProducers().size() > 0);
        assertTrue(md.getChoreographers().size() > 0);
        assertEquals(2010, md.getYear());

        // we can't be sure we'll get data for this :(
        // assertTrue(md.getTagLine().length()>0);
        // assertTrue(md.getQuotes().length()>0);
        // assertTrue(md.getTrivia().length()>0);
    }
}
