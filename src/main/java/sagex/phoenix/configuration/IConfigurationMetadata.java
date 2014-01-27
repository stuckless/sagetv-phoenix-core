package sagex.phoenix.configuration;

import java.io.IOException;

/**
 * Used to load and save configuration metadata items
 * 
 * @author seans
 *
 */
public interface IConfigurationMetadata {
    /**
     * Load the configuration an return it as an array of Group metadata objects.
     * 
     * @throws IOException
     */
    public Group[] load() throws IOException;
    public void save() throws IOException;
}
