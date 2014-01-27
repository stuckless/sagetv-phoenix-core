package sagex.phoenix.util;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Jar Utils
 * 
 * @author seans
 *
 */
public class JarUtil {
	/**
	 * Return jar version for the given jar file
	 * 
	 * @param jarFile
	 * @return jar version or null
	 * @throws IOException if the jar manifest cannot be read
	 */
	public static String getJarVersion(File jarFile) throws IOException {
        JarFile jf = new JarFile(jarFile);
        Manifest mf = jf.getManifest();
        if (mf==null) return null;
        Attributes attr = mf.getMainAttributes();
        if (attr==null) return null;
        return attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
	}
}
