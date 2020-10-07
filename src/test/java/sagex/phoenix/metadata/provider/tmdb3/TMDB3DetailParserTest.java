package sagex.phoenix.metadata.provider.tmdb3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.provider.tmdb.TMDBConfiguration;
import sagex.phoenix.metadata.provider.tmdb.TMDBMetadataProvider;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;
import test.InitPhoenix;

public class TMDB3DetailParserTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testDetailsByIMDBID() throws MetadataException, IOException {
        TMDBMetadataProvider provider = (TMDBMetadataProvider) Phoenix.getInstance().getMetadataManager().getProvider("tmdb");
        assertNotNull("TMDB3 is not registered.", provider);

        IMetadata md = ((HasFindByIMDBID) provider).getMetadataForIMDBId("tt1228705");
        verifyMetadata(md);
    }

    @Test
    public void testDetailsByID() throws MetadataException, IOException {
        TMDBMetadataProvider provider = (TMDBMetadataProvider) Phoenix.getInstance().getMetadataManager().getProvider("tmdb");
        assertNotNull("TMDB3 is not registered.", provider);

        SearchQuery q = new SearchQuery(MediaType.MOVIE, Field.ID, "10138");
        List<IMetadataSearchResult> results = provider.search(q);
        assertEquals(1, results.size());
        verifyMetadata(provider.getMetaData(results.get(0)));
    }

    @Test
    public void testSearch() throws MetadataException {
        TMDBMetadataProvider provider = (TMDBMetadataProvider) Phoenix.getInstance().getMetadataManager().getProvider("tmdb");
        assertNotNull("TMDB3 is not registered.", provider);

        SearchQuery q = new SearchQuery(MediaType.MOVIE, "Iron Man 2", "2010");
        // set the title in the QUERY fields, since providers look in the QUERY
        // field
        q.set(Field.QUERY, "Iron Man 2");
        List<IMetadataSearchResult> results = provider.search(q);
        assertTrue(results.size() > 0);

        for (IMetadataSearchResult r : results) {
            System.out.printf("RESULT: %s; %s; %s\n", r.getTitle(), r.getYear(), r.getScore());
        }

        // Iron Man 2 should be first result
        IMetadataSearchResult sr = results.get(0);
        assertEquals("10138", sr.getId());
        assertEquals("tmdb", sr.getProviderId());
        assertEquals(MediaType.MOVIE, sr.getMediaType());
        assertEquals("Iron Man 2", sr.getTitle());
        assertEquals("2010", String.valueOf(sr.getYear()));
        assertTrue(sr.getScore() > .9);
    }

    public void verifyMetadata(IMetadata md) {
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertTrue(md.getActors().size() > 10);
        boolean foundCast = false;
        for (ICastMember cm : md.getActors()) {
            if (cm.getName().equals("Robert Downey Jr.")) {
                foundCast = true;
                assertEquals("Robert Downey Jr.", cm.getName());
                assertEquals("Tony Stark", cm.getRole());
            }
        }
        assertTrue("No cast found", foundCast);

        assertNotNull(md.getDescription());
        assertTrue(md.getDescription().length() > 50);

        assertTrue(md.getDirectors().size() > 0);
        assertEquals("Iron Man 2", md.getEpisodeName());

        assertTrue(md.getGenres().size() > 0);
        assertEquals("Adventure", md.getGenres().get(0));

        assertEquals("tt1228705", md.getIMDBID());
        assertEquals("10138", md.getMediaProviderDataID());
        assertEquals("tmdb", md.getMediaProviderID());
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
        assertEquals(DateUtils.parseDate("2010-04-28").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("PG-13", md.getRated());
        // no extended ratings
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
        assertEquals("Iron Man 2", md.getEpisodeName());
        assertNull(md.getRelativePathWithTitle());

        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);

        assertTrue(md.getDirectors().size() > 0);
        assertTrue(md.getWriters().size() > 0);
        assertTrue(md.getProducers().size() > 0);
        assertEquals(2010, md.getYear());

        assertTrue(md.getTagLine().length() > 0);

        assertTrue(md.getTrailerUrl().length() > 0);
        assertTrue(md.getTrailerUrl().contains("www.youtube.com"));

        TMDBConfiguration config = GroupProxy.get(TMDBConfiguration.class);
        assertTrue(count(md.getFanart(), MediaArtifactType.POSTER) > 0);
        assertTrue(count(md.getFanart(), MediaArtifactType.BACKGROUND) > 0);

        for (IMediaArt ma : md.getFanart()) {
            assertTrue(ma.getDownloadUrl().startsWith("http"));
        }

        // assertTrue(md.getQuotes().length()>0);
        // assertTrue(md.getTrivia().length()>0);
    }

    private int count(List<IMediaArt> fanart, MediaArtifactType type) {
        int count = 0;
        for (IMediaArt ma : fanart) {
            if (ma.getType() == type)
                count++;
        }
        return count;
    }
}
