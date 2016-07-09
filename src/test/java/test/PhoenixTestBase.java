package test;

import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by seans on 09/07/16.
 */
public class PhoenixTestBase {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true, true);
    }

    @Before
    public void initBeforeTest() throws IOException {
        // before each test, re-init phoenix state
        InitPhoenix.init(true, true, true);
    }
}
