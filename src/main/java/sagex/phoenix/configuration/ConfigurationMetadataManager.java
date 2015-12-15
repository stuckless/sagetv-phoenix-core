package sagex.phoenix.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import sagex.phoenix.common.SystemConfigurationFileManager;

public class ConfigurationMetadataManager extends SystemConfigurationFileManager implements
        SystemConfigurationFileManager.ConfigurationFileVisitor {
    private static final Logger log = Logger.getLogger(ConfigurationMetadataManager.class);

    private static final String ROOT_NODE = "root";

    private Map<String, IConfigurationElement> fields = new TreeMap<String, IConfigurationElement>();
    private Group rootMetadata = null;

    public ConfigurationMetadataManager(File systemDir, File userDir) {
        super(systemDir, userDir, new SuffixFileFilter(".xml", IOCase.INSENSITIVE));
    }

    public void addMetadata(Group groups[]) throws Exception {
        if (groups != null) {
            for (Group g : groups) {
                addMetadata(g);
            }
        }
    }

    public void addMetadata(Group group) throws Exception {
        // now the data is loaded, lets add the items to our store
        IConfigurationElement ce = rootMetadata.findElement(group.getId());
        if (ce != null) {
            log.debug("Updateing Group: " + group.getId());
            // if out label/description is null, then update with this one
            if (group.getLabel() == null)
                group.setLabel(ce.getLabel());
            if (group.getDescription() == null)
                group.setDescription(ce.getDescription());
            ((Group) ce).addAll(group);
        } else {
            log.debug("Adding Group: " + group.getId());
            rootMetadata.addElement(group);
        }

        indexFields(group);
    }

    protected void indexFields(Group g) {
        // now take all the elements and add them to our main table for faster
        // access
        g.visit(new IConfigurationMetadataVisitor() {
            public void accept(IConfigurationElement el) {
                if (el.getElementType() == IConfigurationElement.FIELD) {
                    fields.put(el.getId(), el);
                    log.debug("Indexed Field: " + el.getId());
                }
            }
        });
    }

    public IConfigurationElement getConfigurationElement(String key) {
        return fields.get(key);
    }

    /**
     * Adds a transient (ie non persistent) configuration element field to the
     * field list. This is mainly used to add a typed field to the metadata
     * after the system has been loaded.
     * <p/>
     * Any calls to reload() will not load these fields.
     *
     * @param el
     */
    public void addTransientConfigurationElement(IConfigurationElement el) {
        fields.put(el.getId(), el);
    }

    public Group getMetadata() {
        return rootMetadata;
    }

    public IConfigurationElement[] getParentGroups() {
        return rootMetadata.getChildren();
    }

    public IConfigurationElement findElement(String id) {
        return rootMetadata.findElement(id);
    }

    @Override
    public void visitConfigurationFile(ConfigurationType type, File file) {
        try {
            log.info("Loading Configuration Metadata in " + file);
            XmlMetadataProvider prov = new XmlMetadataProvider(file);
            Group gr[] = prov.load();
            if (gr == null || gr.length == 0)
                throw new IOException("File loaded, but no metadata was included.");
            addMetadata(gr);
        } catch (Exception e) {
            log.warn("Failed to some or all of the configuration metadata in file " + file, e);
        }
    }

    @Override
    public void loadConfigurations() {
        log.info("Begin Loading Configuration Metadata");
        fields.clear();
        rootMetadata = new Group(ROOT_NODE);
        accept(this);
        log.info("End Loading Configuration Metadata");
    }
}
