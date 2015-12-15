package sagex.phoenix.metadata.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchUtilTest {

    @Test
    public void testSpecialHandleDots() {
        verifyDots("Marvel's Agents of S.H.I.E.L.D. - S01E01 - Pilot.mkv", "Marvel's Agents of S.H.I.E.L.D. - S01E01 - Pilot mkv");
        verifyDots("R.I.P.D (2013).iso", "R.I.P.D (2013) iso");
        verifyDots("", "");
    }

    private void verifyDots(String raw, String expected) {
        String newName = SearchUtil.specialHandleDots(raw);
        assertEquals(expected, newName);
    }

}
