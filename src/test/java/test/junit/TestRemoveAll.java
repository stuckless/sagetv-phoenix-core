package test.junit;

import org.junit.Test;
import phoenix.impl.UtilAPI;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestRemoveAll {
    @Test
    public void testRemoveAll() {
        UtilAPI util = new UtilAPI();

        String a1[] = {"A", "B", "C", "D"};
        String a2[] = {"B", "C"};

        List l1 = util.RemoveAll(a1, a2);
        assertNotNull(l1);
        assertEquals(2, l1.size());
        assertEquals("A", l1.get(0));
        assertEquals("D", l1.get(1));
    }
}
