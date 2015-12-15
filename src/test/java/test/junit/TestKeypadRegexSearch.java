package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import phoenix.impl.UtilAPI;

public class TestKeypadRegexSearch {

    @Test
    public void testKeypadRegexSearch() {
        List<String> titles = new ArrayList<String>();
        titles.add("Legends of the Fall");
        titles.add("Terminator");
        titles.add("Break a Leg");
        titles.add("Mr. Bigfellow");
        titles.add("Dr. Goon");
        titles.add("Lean on Me");
        titles.add("1 on");

        search("5", titles, "Legends of the Fall", "Break a Leg", "Mr. Bigfellow", "Lean on Me");
        search("53", titles, "Legends of the Fall", "Break a Leg", "Lean on Me");
        search("534", titles, "Legends of the Fall", "Break a Leg");
        search("5343", titles, "Legends of the Fall");

        search("7", titles, "Legends of the Fall", "Terminator", "Break a Leg", "Mr. Bigfellow", "Dr. Goon");
        search("71", titles, "Mr. Bigfellow", "Dr. Goon");
        search("710", titles, "Mr. Bigfellow", "Dr. Goon");
        search("7102", titles, "Mr. Bigfellow");

        search("1", titles, "Mr. Bigfellow", "Dr. Goon", "1 on");
        search("106", titles, "1 on");

        // these should return nothing
        search("71021", titles);
        search("53439", titles);
    }

    private List<String> search(String nums, List<String> titles, String... realResults) {
        UtilAPI api = new UtilAPI();
        String regex = api.CreateRegexFromKeypad(nums);
        System.out.println("Nums: " + nums + "; Regex: " + regex);
        Pattern pat = Pattern.compile(regex);
        List<String> results = new ArrayList<String>();
        for (String s : titles) {
            Matcher m = pat.matcher(s);
            if (m.find())
                results.add(s);
        }

        if (realResults == null || realResults.length == 0) {
            assertTrue(nums + " must return 0 results", results.size() == 0);
        }

        assertEquals(nums + " must return " + realResults.length + " results", realResults.length, results.size());

        for (int i = 0; i < realResults.length; i++) {
            assertEquals("Result didn't match", realResults[i], results.get(i));
            System.out.println("Regex: " + regex + " matched: " + realResults[i]);
        }
        System.out.println("-----");
        return results;
    }
}
