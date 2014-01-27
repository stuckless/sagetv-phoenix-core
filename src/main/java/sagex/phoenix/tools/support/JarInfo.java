package sagex.phoenix.tools.support;

import java.io.File;
import java.io.Serializable;

public class JarInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private File file;
    private String title, vendor, version;
    
    public JarInfo() {
    }
    
    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }
    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the vendor
     */
    public String getVendor() {
        return vendor;
    }
    /**
     * @param vendor the vendor to set
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
