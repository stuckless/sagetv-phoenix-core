package sagex.phoenix.tools.support;

import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarUtil {
    private static Pattern jarPat = Pattern.compile("(.*)-([0-9]+\\.[0-9]+.*)\\.jar");
    private static Pattern jarPat2 = Pattern.compile("(.*)\\.jar");

    public static List<JarInfo> getJarInfo(File jarDir) {
        File files[] = jarDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".jar");
            }
        });

        List<JarInfo> info = new ArrayList<JarInfo>();
        if (files == null || files.length == 0) {
            return info;
        }

        for (File f : files) {
            try {
                JarInfo ji = new JarInfo();
                JarFile jf = new JarFile(f);
                Manifest mf = jf.getManifest();
                ji.setFile(f);
                Attributes attr = mf.getMainAttributes();
                ji.setTitle(attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE));
                ji.setVendor(attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR));
                ji.setVersion(attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION));

                if (ji.getTitle() == null && ji.getVersion() == null) {
                    Matcher m = jarPat.matcher(ji.getFile().getName());
                    if (m.find()) {
                        ji.setTitle(m.group(1));
                        ji.setVersion(m.group(2));
                    }
                }

                if (ji.getTitle() == null) {
                    Matcher m = jarPat2.matcher(ji.getFile().getName());
                    if (m.find()) {
                        ji.setTitle(m.group(1));
                        ji.setVersion("99.0");
                    }
                }

                info.add(ji);

                // System.out.println("File: " + f);
                // for (Object key : attr.keySet()) {
                // System.out.printf("%s: %s\n", key, attr.get(key));
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    public static List<JarInfo> findDuplicateJars(File jarDir) {
        List<JarInfo> dups = new ArrayList<JarInfo>();
        List<JarInfo> jars = getJarInfo(jarDir);
        Map<String, JarInfo> vers = new HashMap<String, JarInfo>();
        for (JarInfo ji : jars) {
            JarInfo lastJar = vers.get(ji.getTitle());
            if (lastJar == null) {
                vers.put(ji.getTitle(), ji);
                continue;
            }

            if (ji.getVersion().equals(lastJar.getVersion())) {
                // store the one without the version in the filename
                Matcher m = jarPat.matcher(ji.getFile().getName());
                if (m.find()) {
                    dups.add(ji);
                } else {
                    dups.add(lastJar);
                    vers.put(ji.getTitle(), ji);
                }
                continue;
            }

            if (IsAtLeastVersion(ji.getVersion(), lastJar.getVersion())) {
                dups.add(lastJar);
                vers.put(ji.getTitle(), ji);
            } else {
                dups.add(ji);
            }
        }
        return dups;
    }

    /**
     * Can test if the first version is at least equal to or greater than the
     * second version.
     *
     * @param verToTest   first version
     * @param baseVersion second version
     * @return true if verToTest is >= baseVersion
     */
    public static boolean IsAtLeastVersion(String verToTest, String baseVersion) {
        if (verToTest == null || baseVersion == null)
            return false;
        if (verToTest.equals(baseVersion))
            return true;

        verToTest = verToTest.replaceAll("-", "\\.");
        baseVersion = baseVersion.replaceAll("-", "\\.");

        String v1[] = verToTest.split("\\.");
        String v2[] = baseVersion.split("\\.");

        for (int i = 0; i < v1.length && i < v2.length; i++) {
            int n1 = NumberUtils.toInt(v1[i], -1);
            int n2 = NumberUtils.toInt(v2[i], -1);
            if (n1 > n2)
                return true;
            if (n1 < n2)
                return false;
        }

        if (v1.length > v2.length)
            return true;

        return false;
    }

}
