package sagex.phoenix.tools.support;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import sagex.api.Configuration;
import sagex.api.MediaFileAPI;
import sagex.phoenix.util.SageTV;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Troubleshooter {
    private static final Logger log = Logger.getLogger(Troubleshooter.class);

    public static class StubFileZipper extends DirectoryWalker {
        ZipOutputStream zos;

        public StubFileZipper(ZipOutputStream zos) {
            super();
            this.zos = zos;
        }

        public List zip(File startDirectory) throws IOException {
            List results = new ArrayList();
            walk(startDirectory, results);
            return results;
        }

        protected void handleFile(File file, int depth, Collection results) {
            Pattern p = Pattern.compile("\\.vob|\\.bdmv|\\.m2ts|\\.avi|\\.mkv|\\.mpg|\\.divx|\\.m4v|\\.ts",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(file.getName());
            if (file.isFile() && m.find()) {
                try {
                    addStubFile(zos, file);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private void addStubFile(ZipOutputStream zos2, File file) throws Exception {
            ZipEntry ze = new ZipEntry(file.getPath());
            zos.putNextEntry(ze);
            zos.write("X".getBytes());
            zos.closeEntry();
        }
    }

    public static File createSupportZip(String problemDescription, boolean includeLogs, boolean includeProps, boolean locations)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        File output = new File("bmtsupport-" + sdf.format(Calendar.getInstance().getTime()) + ".zip");
        return createSupportZip(problemDescription, includeLogs, includeProps, locations, output);
    }

    /**
     * Attempts to zip up various log files, props, and stub video locations for
     * support purpsoses.
     *
     * @param problemDescription
     * @param includeLogs
     * @param includeProps
     * @param locations
     * @throws Exception
     */
    public static File createSupportZip(String problemDescription, boolean includeLogs, boolean includeProps, boolean locations,
                                        File output) throws Exception {
        final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
        if (problemDescription == null) {
            problemDescription = "-- NO PROBLEM ENTERED --";
        }
        ZipEntry ze = new ZipEntry("PROBLEM.txt");
        zos.putNextEntry(ze);
        zos.write(problemDescription.getBytes());
        addString(zos, "\n");
        addString(zos, "Phoenix Version:  " + phoenix.system.GetVersion());
        addString(zos, "  Sagex Version:  " + sagex.api.Version.GetVersion());
        addString(zos, "   Java Version:  " + System.getProperty("java.version"));
        addString(zos, "   Sage Version:  " + SageTV.getSageVersion());
        addString(zos, " Java Classpath:  " + System.getProperty("java.class.path"));
        addString(zos, "\n");
        zos.closeEntry();

        if (includeLogs) {
            File dir = new File(".");
            dir.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    if (f.getName().endsWith(".log") || f.getName().endsWith("log4j.properties")) {
                        try {
                            // remove path info
                            addFile(zos, f, "logs");
                        } catch (Exception e) {
                            log.warn("Could not add " + f + " to support zip.");
                        }
                    }
                    return false;
                }
            });
            addFile(zos, new File("sagetv_0.txt"), "logs");
            dir = new File("logs");
            if (dir.exists()) {
                dir.listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        if (f.getName().endsWith(".log") || f.getName().endsWith("log4j.properties")) {
                            try {
                                addFile(zos, f, "logs");
                            } catch (Exception e) {
                                log.warn("Could not add " + f + " to support zip.");
                            }
                        }
                        return false;
                    }
                });
            }
        }

        if (includeProps) {
            addFile(zos, new File("Sage.properties"), "properties");
        }

        if (locations) {
            Arrays.asList(Configuration.GetVideoLibraryImportPaths());
            ze = new ZipEntry("FILES.txt");
            zos.putNextEntry(ze);
            addString(zos, "Sage Import Paths");
            List<File> files = Arrays.asList(Configuration.GetVideoLibraryImportPaths());
            if (files != null) {
                for (File f : files) {
                    addString(zos, f.getName());
                }
            }
            addString(zos, "\n");

            addMediaFiles("T", "TV", zos);
            addMediaFiles("TL", "Archived TV", zos);
            addMediaFiles("V", "Videos", zos);
            addMediaFiles("D", "DVD", zos);
            addMediaFiles("B", "BluRay", zos);

            zos.closeEntry();
        }
        zos.close();
        return output;
    }

    private static void addMediaFiles(String mask, String label, ZipOutputStream zos) {
        Object oo[] = MediaFileAPI.GetMediaFiles(mask);
        if (oo == null || oo.length == 0)
            return;
        addString(zos, "BEGIN " + label);
        for (Object o : oo) {
            String files = "";
            File ff[] = MediaFileAPI.GetSegmentFiles(o);
            if (ff != null) {
                for (File f : ff) {
                    files += (f.getAbsolutePath() + ";");
                }
            }
            addString(zos, "   " + MediaFileAPI.GetMediaFileID(o) + "| " + MediaFileAPI.GetMediaFileMetadata(o, "ExternalID")
                    + "| " + files);
        }
        addString(zos, "END   " + label);
        addString(zos, "\n");
    }

    private static void addString(ZipOutputStream zos, String string) {
        try {
            zos.write(string.getBytes());
            zos.write("\n".getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void addFile(ZipOutputStream zos, File file, String prefix) throws Exception {
        if (file.exists() && file.isFile()) {
            String path = file.getName();
            ZipEntry ze = new ZipEntry(prefix + "/" + path);
            zos.putNextEntry(ze);
            FileInputStream fis = new FileInputStream(file);
            IOUtils.copy(fis, zos);
            fis.close();
            zos.closeEntry();
        }
    }

    public static void backupWizBin() throws Exception {
        File outdir = new File("Backups");
        if (!outdir.exists()) {
            log.debug("Creating Backup Folder: " + outdir.getAbsolutePath());
            if (!outdir.mkdirs()) {
                throw new IOException("Failed to create backup dir: " + outdir);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        File output = new File(outdir, "Wiz.bin-" + sdf.format(Calendar.getInstance().getTime()));
        if (output.exists())
            throw new IOException("Backup file exists: " + output.getAbsolutePath());

        File input = new File("Wiz.bin");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(output);
            IOUtils.copyLarge(fis, fos);
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
            if (fos != null) {
                fos.flush();
                IOUtils.closeQuietly(fos);
            }
        }
    }

}
