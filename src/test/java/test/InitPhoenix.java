package test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;

import sagex.SageAPI;
import sagex.phoenix.util.BaseBuilder;
import sagex.stub.StubSageAPI;

public class InitPhoenix {
    private static boolean initialized = false;
    public static synchronized void init(boolean deleteOld, boolean stubapi) throws IOException {
        if (initialized) {
            System.out.println("InitPhoenix: already done.");
            return;
        }
        
        // allow for xml parsing errors
        BaseBuilder.failOnError = true;
        
        initialized=true;
        
        if (stubapi) {
        	StubSageAPI api = new StubSageAPI();
        	SageAPI.setProvider(api);
        }
        
        BasicConfigurator.configure();
        System.out.println("Copying Phoenix Configuration to Testing Area...");
        File baseDir = new File("target/testing/");
        
        if (deleteOld) {
        	FileUtils.deleteDirectory(baseDir);
        }
        
        FileUtils.copyDirectory(new File("src/main/STVs"), new File(baseDir, "STVs"), new FileFilter() {
            public boolean accept(File pathname) {
                System.out.println("Copy: " + pathname);
                return !(pathname.getName().startsWith("."));
            }
        });

        FileUtils.copyDirectory(new File("../PhoenixUI/STVs/Phoenix/vfs"), new File(baseDir, "STVs/Phoenix/vfs"), new FileFilter() {
            public boolean accept(File pathname) {
                System.out.println("Copy: " + pathname);
                return !(pathname.getName().startsWith("."));
            }
        });
        
        System.out.println("Initializing Phoneix with testing dir: " + baseDir.getAbsolutePath());
        System.setProperty("phoenix/sagetvHomeDir", baseDir.getAbsolutePath());
        
        System.out.println("Phoenix has been initialized.");
    }
}
