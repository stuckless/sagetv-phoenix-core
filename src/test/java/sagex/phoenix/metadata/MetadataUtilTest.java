package sagex.phoenix.metadata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataUtilTest {
    public MetadataUtilTest() {
    }

    @Test
    public void testFillMetadata() throws Exception {
        IMetadata m1 = MetadataUtil.createMetadata();
        IMetadata m2 = MetadataUtil.createMetadata();

        m1.setMediaTitle("Movie 1");
        m2.setMediaTitle("Movie 2");

        m1.setDescription("Desc");
        m2.setDescription("");

        m1.getGenres().add("G1");
        m2.getGenres().add("G2");

        m1.getActors().add(new CastMember("CastName", "CastRole"));

        m1.setSeasonNumber(5);
        m1.setEpisodeNumber(4);
        m2.setEpisodeNumber(0);

        m1.setDiscNumber(6);
        m2.setDiscNumber(9);

        assertEquals("Movie 2", m2.getMediaTitle());
        assertEquals("", m2.getDescription());
        assertEquals("G2", m2.getGenres().get(0));
        assertEquals(0, m2.getActors().size());
        assertEquals(0, m2.getSeasonNumber());
        assertEquals(0, m2.getEpisodeNumber());
        assertEquals(9, m2.getDiscNumber());

        MetadataUtil.fillMetadata(m1, m2);
        assertEquals("Movie 2", m2.getMediaTitle());
        assertEquals("Desc", m2.getDescription());
        assertEquals(1, m2.getGenres().size());
        assertEquals("G2", m2.getGenres().get(0));
        assertEquals(1, m2.getActors().size());
        assertEquals("CastName", m2.getActors().get(0).getName());
        assertEquals(5, m2.getSeasonNumber());
        assertEquals(4, m2.getEpisodeNumber());
        assertEquals(9, m2.getDiscNumber());
    }
}
