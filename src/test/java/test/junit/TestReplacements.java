package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.TextReplacement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestReplacements {

    @BeforeClass
    public static void setup() throws IOException {
        // InitPhoenix.init();
    }

    @Test
    public void testReplacementsWithTextFormat() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("MediaTitle", "House");
        map.put("SeasonNumber", "8");
        map.put("EpisodeNumber", "21");
        map.put("EpisodeName", "Chase");
        map.put("ReleaseDate", String.valueOf(DateUtils.parseDate("2012-02-18").getTime()));

        String mask = "${MediaTitle} - S${SeasonNumber:java.text.DecimalFormat:00}E${EpisodeNumber:java.text.DecimalFormat:00} - ${EpisodeName} (${ReleaseDate:java.text.SimpleDateFormat:yy-MM-dd})";
        String out = TextReplacement.replaceVariables(mask, map);
        assertEquals(out, "House - S08E21 - Chase (12-02-18)");
    }

    @Test
    public void testReplacementsWithStringFormat() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("MediaTitle", "House");
        map.put("SeasonNumber", "8");
        map.put("EpisodeNumber", "21");
        map.put("EpisodeName", "Chase");
        map.put("ReleaseDate", String.valueOf(DateUtils.parseDate("2012-02-18").getTime()));

        String mask = "${MediaTitle} - S${SeasonNumber:%02d}E${EpisodeNumber:%02d} - ${EpisodeName} (${ReleaseDate:java.text.SimpleDateFormat:yy-MM-dd})";
        String out = TextReplacement.replaceVariables(mask, map);
        assertEquals(out, "House - S08E21 - Chase (12-02-18)");
    }

}
