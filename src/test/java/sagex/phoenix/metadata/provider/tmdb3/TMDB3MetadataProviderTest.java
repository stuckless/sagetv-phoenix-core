package sagex.phoenix.metadata.provider.tmdb3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import test.InitPhoenix;

public class TMDB3MetadataProviderTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testTrailersCausingFailures() {
        TMDB3MetadataProvider provider = (TMDB3MetadataProvider) Phoenix.getInstance().getMetadataManager().getProvider("tmdb3");
        assertNotNull("TMDB3 is not registered.", provider);

        try {
            IMetadata md = ((HasFindByIMDBID) provider).getMetadataForIMDBId("tt0303387");
            // null metadata replies are fine
            if (md != null) {
                System.out.println("Movie: " + md.getMediaTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Lookup failed!");
        }
    }

}
