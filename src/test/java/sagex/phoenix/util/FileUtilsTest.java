package sagex.phoenix.util;

import org.apache.commons.io.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by seans on 19/09/16.
 */
public class FileUtilsTest {
    @Test
    public void sanitize() throws Exception {
        assertEquals("Pokémon S01E02.ts",FileUtils.sanitize("Pokémon S01E02.ts"));
    }

}