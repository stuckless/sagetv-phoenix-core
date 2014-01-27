package java.io;

import java.io.Serializable;

/**
 * Stub File class for GWT
 * @author seans
 *
 */
public class File implements Serializable {
    private String path;
    public File() {
    }
    
    public File(String path) {
        this.path=path;
    }
    
    public String getAbsolutePath() {
    	return path;
    }
}
