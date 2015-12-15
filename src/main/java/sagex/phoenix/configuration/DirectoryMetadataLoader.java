package sagex.phoenix.configuration;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DirectoryMetadataLoader implements IConfigurationMetadata {
    private static final Logger log = Logger.getLogger(DirectoryMetadataLoader.class);

    private File dir = null;

    public DirectoryMetadataLoader(File dir) {
        this.dir = dir;
    }

    public Group[] load() throws IOException {
        log.info("Loading Configurations from directory: " + dir.getAbsolutePath());

        if (!dir.exists() && !dir.isDirectory()) {
            log.warn("Directory Loader, ignoring dir: " + dir.getAbsolutePath() + " since it is not a valid directory");
            return null;
        }

        List<Group> groups = new LinkedList<Group>();

        File files[] = dir.listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".xml");
            }
        });

        for (File f : files) {
            if (f.getName().endsWith(".xml")) {
                log.info("Loading Xml Metadata: " + f.getAbsolutePath());
                XmlMetadataProvider prov = new XmlMetadataProvider(f);
                Group gr[] = prov.load();
                if (gr != null && gr.length > 0) {
                    for (Group g : gr) {
                        groups.add(g);
                    }
                } else {
                    log.warn("No Metadata for: " + f.getAbsolutePath());
                }
            }
        }

        return groups.toArray(new Group[groups.size()]);
    }

    public void save() throws IOException {
        log.warn("Save Not Implemented for DirectoryMetadataLoader");
    }
}
