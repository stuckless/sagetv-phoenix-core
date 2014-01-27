package sagex.phoenix.services;

import java.io.File;
import java.io.FileFilter;
import java.rmi.Remote;

/**
 * Remote File Services is used to transfer Remote (SageTV Server) files to a client computer,
 * or to allow a remote computer to browser the files on the remote server.
 * 
 * @author seans
 */
public interface RemoteFileServices extends Remote {
    public File[] listFiles(File parentDir, FileFilter filter);
    public File[] listFiles(File parentDir);
    public byte[] getContents(File file);
    public boolean delete(File file);
}
