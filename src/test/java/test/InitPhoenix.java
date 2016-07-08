package test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sagex.SageAPI;
import sagex.phoenix.util.BaseBuilder;
import sagex.stub.StubSageAPI;

public class InitPhoenix {
    private static boolean initialized = false;

    public static File PHOENIX_HOME;
    public static File PROJECT_ROOT;

    public static synchronized void init(boolean deleteOld, boolean stubapi) throws IOException {
        if (initialized) {
            System.out.println("InitPhoenix: already done.");
            return;
        }

        // allow for xml parsing errors
        BaseBuilder.failOnError = true;

        if (stubapi) {
            StubSageAPI api = new StubSageAPI();
            SageAPI.setProvider(api);
        }

        System.out.println("Configure Logging");
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.DEBUG);
        System.out.println("Copying Phoenix Configuration to Testing Area...");
        //File baseDir = new File("."); // test runner should put us into target/testing

        PROJECT_ROOT = getProjectRoot(new File("."));
        if (!new File(PROJECT_ROOT,"src").exists()) {
            throw new RuntimeException("PROJECT ROOT doesn't appear correct: " + PROJECT_ROOT.getAbsolutePath());
        }
        PHOENIX_HOME = new File(PROJECT_ROOT, "target/testing");
        PHOENIX_HOME.mkdirs();

        if (deleteOld) {
            if ("testing".equals(PHOENIX_HOME.getCanonicalFile().getName())) {
                FileUtils.cleanDirectory(PHOENIX_HOME);
            } else {
                throw new RuntimeException("Trying clean baseDir that is not the testing dir: " + PHOENIX_HOME.getAbsolutePath());
            }
        }

        FileUtils.copyDirectory(new File(PROJECT_ROOT,"src/plugins/phoenix-core/STVs"), new File(PHOENIX_HOME, "STVs"), new FileFilter() {
            public boolean accept(File pathname) {
                System.out.println("Copy: " + pathname);
                return !(pathname.getName().startsWith("."));
            }
        });

        System.out.println("Initializing Phoneix with testing dir: " + PHOENIX_HOME.getAbsolutePath());
        System.setProperty("phoenix/sagetvHomeDir", PHOENIX_HOME.getAbsolutePath());

        System.out.println("Phoenix has been initialized.");
        initialized = true;
    }

    private static File getProjectRoot(File dir) {
        File d = new File(dir, "src");
        if (d.exists() && d.isDirectory()) return dir;
        return getProjectRoot(dir.getParentFile());
    }
}
