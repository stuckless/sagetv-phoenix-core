package phoenix.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import phoenix.api;

import static org.junit.Assert.*;

/**
 * Created by seans on 05/11/16.
 */
public class ImageAPITest {
    static ImageAPI api;
    @BeforeClass
    public static void setup() {
        api = new ImageAPI();
    }

    @Test
    public void getFontIcon() throws Exception {
        assertEquals("\uE84D", api.GetFontIcon("3d_rotation"));
        assertTrue(api.GetFontIcon("3d_rotation").length()==1);
    }

    @Test
    public void getFontIconNames() throws Exception {
        assertTrue(api.GetFontIconNames().length>10);
    }

}