package sagex.phoenix.configuration.impl;

import org.apache.log4j.Logger;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.IConfigurationProvider;
import sagex.phoenix.util.IterableEnumeration;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * Simple file based, {@link Properties} based configuration provider that does
 * not provide scoped properties. Server, Client, and User all share the same
 * {@link Properties}
 *
 * @author sean
 */
public class PropertiesConfiguration implements IConfigurationProvider {
    private File propFile = null;
    private Properties props = new Properties();
    private static final Logger log = Logger.getLogger(PropertiesConfiguration.class);

    public PropertiesConfiguration(File properties) throws IOException {
        this.propFile = properties;
    }

    public void save() throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(propFile);
            props.store(fw, "Configuration");
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                } catch (IOException e) {
                }
                fw.close();
            }
        }
    }

    public String getProperty(ConfigScope scope, String key) {
        return props.getProperty(key);
    }

    public void setProperty(ConfigScope scope, String key, String value) {
        props.setProperty(key, value);
    }

    public void load() throws IOException {
        if (propFile.exists()) {
            log.info("Loading Properties: " + propFile.getAbsolutePath());
            InputStream is = null;
            try {
                is = new FileInputStream(propFile);
                props.load(is);
            } finally {
                if (is != null)
                    is.close();
            }
        } else {
            log.info("Property File Not Found: " + propFile.getAbsolutePath());
        }
    }

    public Iterator<String> keys(ConfigScope scope) {
        return new IterableEnumeration<String>((Enumeration<String>) props.propertyNames());
    }
}
