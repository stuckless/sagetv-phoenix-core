package sagex.phoenix.services;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;

public class RemoteFileServicesImpl implements RemoteFileServices, Serializable {
    private transient Logger log = Logger.getLogger(RemoteFileServicesImpl.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public boolean delete(File file) {
        return file.delete();
    }

    /**
     * This will return the contents of the file as a byte array... This should
     * only be used on small files.
     */
    public byte[] getContents(File file) {
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                return IOUtils.toByteArray(fis = new FileInputStream(file));
            } catch (Exception e) {
                log.warn("Failed to read file: " + file);
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException e) {
                        log.warn("Failed to close file");
                    }
            }
        }
        return null;
    }

    public File[] listFiles(File parentDir, FileFilter filter) {
        return parentDir.listFiles(filter);
    }

    public File[] listFiles(File parentDir) {
        return parentDir.listFiles();
    }
}
