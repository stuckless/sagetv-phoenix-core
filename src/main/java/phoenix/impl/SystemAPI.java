package phoenix.impl;

import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.JarUtil;

import java.io.File;
import java.io.IOException;

/**
 * System Level Pheonix operations
 *
 * @author seans
 */
@API(group = "system")
public class SystemAPI {
    public static String GetVersion() {
        try {
            return JarUtil.getJarVersion(new File("JARs/phoenix.jar"));
        } catch (IOException e) {
            return "??";
        }
    }

    public static String GetRequiredSageVersion() {
        return "7.0.23";
    }

    public static String GetRequiredSagexApiVersion() {
        return "7.0.23.0";
    }
}
