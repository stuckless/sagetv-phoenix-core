package sagex.phoenix.fanart;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.progress.NullProgressMonitor;
import sagex.phoenix.progress.ProgressTracker;
import sagex.phoenix.util.DirectoryVisitor;
import sagex.phoenix.util.FileExtFileFilter;
import sagex.phoenix.util.ParserUtils;

/**
 * Collection of Useful Functions for dealing with the Central and Local Fanart
 * Locations.
 * <p/>
 * This library should remain portable in that it can be used from within sagetv
 * or in an external library.
 *
 * @author seans
 */
public class FanartUtil {
    private static final Logger log = Logger.getLogger(FanartUtil.class);

    private static final Map<MediaArtifactType, String> localMediaPostfixes = new HashMap<MediaArtifactType, String>();

    static {
        localMediaPostfixes.put(MediaArtifactType.BACKGROUND, "background.jpg");
        localMediaPostfixes.put(MediaArtifactType.BANNER, "banner.jpg");
        localMediaPostfixes.put(MediaArtifactType.POSTER, "folder.jpg");
        localMediaPostfixes.put(MediaArtifactType.ALBUM, "album.jpg");
    }

    public static final String SEASON_NUMBER = "SeasonNumber";
    public static final String EPISODE_NUMBER = "EpisodeNumber";
    public static final String EPISODE_TITLE = "EpisodeTitle";
    public static final String COLLECTION_ID = "CollectionID";

    /**
     * Cleans a Title for use in the Fanart Folder, by applying the following
     * regex to the title to remove unwanted chars.
     * <p/>
     * <pre>
     * [\\\\/:\\*?\"<>|]+
     * </pre>
     *
     * @param title Title to clean
     * @return Cleaned title
     */
    public static String createSafeTitle(String title) {
        if (title == null)
            return null;
        title = title.replaceAll("[\\\\/:\\*?\"<>|\\.]+", "");
        return title.trim();
    }

    /**
     * @deprecated use
     * {@link FanartUtil#getCentralFanartArtifact(MediaType, String, MediaArtifactType, String, String, Map)}
     */
    public static File getCentralFanartArtifact(MediaType mediaType, MediaArtifactType artifactType, String title,
                                                String centralFolder, Map<String, String> metadata) {
        return getCentralFanartArtifact(mediaType, title, artifactType, null, centralFolder, metadata);
    }

    /**
     * Get a Fanart Artifact for a Given Title from the Central Fanart Folder.
     * This can also be used when inserting fanart into the central folder. For
     * example, it will return a file. You test if the file exists and if it
     * does not exist then you can write to the file to create the new file.
     * <p/>
     * Note, that mediaType, mediaTitle, artifactType, and artifactTitle can all
     * be null. The path will be made of up the non-null items that are passed.
     * <p/>
     * ie, a fanart path consists of the following
     * <p/>
     * <pre>
     * CENTRAL_FOLDER[/mediaType][/mediaTitle][/artifactType][/artifactTitle]/[artifactTitle.jpg|mediaTitle.jpg]
     * </pre>
     * <p/>
     * For this reason, you can request different types of fanart that are are
     * not natively supported by the system, by omitting mediaType and
     * artifactType. for example, you can call, (null, "Genres", null, "Family")
     * and it would return a folder like CENTRAL_FOLDER/Genres/Family/Family.jpg
     *
     * @param mediaType     {@link MediaType} (TV, Movie, etc)
     * @param mediaTitle    fanart title identifier (it will be cleaned)
     * @param artifactType  {@link MediaArtifactType} Fanart Artifact Type (Banner,
     *                      Background, etc)
     * @param artifactTitle artifact title, usually used with Actor artifact type to
     *                      provide the actor name
     * @param centralFolder Central Folder Location
     * @param metadata      any extra metadata about the object, ie, SeasonNumber, etc.
     * @return a fanart artifact file, which may not exist.
     */
    public static File getCentralFanartArtifact(MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                                String artifactTitle, String centralFolder, Map<String, String> metadata) {
        File dir = getCentralFanartDir(mediaType, mediaTitle, artifactType, artifactTitle, centralFolder, metadata);

        if (artifactType == MediaArtifactType.EPISODE) {
            if (metadata == null) {
                log.warn("Can't determine the Episode Fanart; Missing Season/Episode information for " + mediaTitle);
                return null;
            } else {
                String file = getEpisodeFilename(metadata);
                if (file != null) {
                    return new File(dir, file);
                }
                log.warn("Failed to create episode specif fanart for metadata " + metadata);
                return null;
            }
        }

        if (artifactType == MediaArtifactType.ALBUM) {
            if (!StringUtils.isEmpty(artifactTitle)) {
                return new File(dir, FanartUtil.createSafeTitle(artifactTitle) + ".jpg");
            }
            log.warn("Failed to get album art since the album was null for " + mediaTitle);
        }

        File art = null;
        if (artifactTitle != null) {
            art = new File(dir, FanartUtil.createSafeTitle(artifactTitle) + ".jpg");
        } else {
            art = new File(dir, FanartUtil.createSafeTitle(mediaTitle) + ".jpg");
        }

        return art;
    }

    public static String getEpisodeFilename(Map<String, String> metadata) {
        int e = NumberUtils.toInt(metadata.get(EPISODE_NUMBER));
        if (e == 0) {
            log.warn("Can't determine the Episode Fanart (no episode information)");
            return null;
        }

        String file = String.format("%04d", e);
        return file + ".jpg";
    }

    /**
     * Return the CentralFanart Dir for the given media types and artifacts. Use
     * by
     * {@link FanartUtil#getCentralFanartArtifact(MediaType, String, MediaArtifactType, String, String, Map)}
     * and
     * {@link FanartUtil#getCentalFanartArtifacts(MediaType, String, MediaArtifactType, String, String, Map)}
     *
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param centralFolder
     * @param metadata
     * @return
     */
    public static File getCentralFanartDir(MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                           String artifactTitle, String centralFolder, Map<String, String> metadata) {
        if (mediaTitle != null) {
            mediaTitle = FanartUtil.createSafeTitle(mediaTitle);
        }

        File dir = new File(centralFolder);

        if (mediaType != null) {
            dir = new File(dir, mediaType.dirName());
        }

        if (mediaTitle != null) {
            // title is the next dir, followed by the artifact type
            dir = new File(dir, mediaTitle);
        }

        if (metadata != null && mediaTitle != null && mediaType == MediaType.TV) {
            String season = metadata.get(SEASON_NUMBER);
            if (season != null && season.trim().length() > 0) {
                dir = new File(dir, getCentralSeasonDir(season));
            }
        }

        // // if this is an episode artifact, then put it under the season
        // folder
        // if (metadata!=null && mediaType == MediaType.TV && artifactType ==
        // MediaArtifactType.EPISODE) {
        // String season = metadata.get(SEASON_NUMBER);
        // if (season!=null && season.trim().length()>0) {
        // dir = new File(dir, getCentralSeasonDir(season));
        // }
        // }

        if (artifactType != null) {
            dir = new File(dir, artifactType.dirName());
        }

        if (artifactTitle != null && artifactType != MediaArtifactType.ALBUM) {
            artifactTitle = FanartUtil.createSafeTitle(artifactTitle);
            dir = new File(dir, artifactTitle);
        }

        return dir;
    }

    private static String getCentralSeasonDir(String season) {
        return "Season " + NumberUtils.toInt(season);
    }

    /**
     * @param mediaType
     * @param artifactType
     * @param title
     * @param centralFolder
     * @param metadata
     * @return
     * @deprecated use
     * {@link FanartUtil#getCentalFanartArtifacts(MediaType, String, MediaArtifactType, String, String, Map)}
     */
    public static File[] getCentalFanartArtifacts(MediaType mediaType, MediaArtifactType artifactType, String title,
                                                  String centralFolder, Map<String, String> metadata) {
        return getCentalFanartArtifacts(mediaType, title, artifactType, null, centralFolder, metadata);
    }

    /**
     * Get an Array of Fanart Artifacts for a Given Title from the Central
     * Fanart Folder
     *
     * @param mediaType     {@link MediaType} (TV, Movie, etc)
     * @param mediaTitle    fanart title identifier (it will be cleaned)
     * @param artifactType  {@link MediaArtifactType} Fanart Artifact Type (Banner,
     *                      Background, etc)
     * @param artifactTitle artifact title (usually only used with actor name for actor
     *                      fanart)
     * @param centralFolder Central Folder Location
     * @return a String array of fanart artifacts or null if it's not found.
     */
    public static File[] getCentalFanartArtifacts(MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                                  String artifactTitle, String centralFolder, Map<String, String> metadata) {
        File dir = getCentralFanartDir(mediaType, mediaTitle, artifactType, artifactTitle, centralFolder, metadata);

        File files[] = null;
        if (dir.exists()) {
            files = dir.listFiles(ImageUtil.ImagesFilter);
        }

        if (log.isDebugEnabled()) {
            log.debug("MediaType: " + mediaType + "; mediaTitle: " + mediaTitle + "; artifact: " + artifactType + "; dir: " + dir
                    + "; files: " + ((files != null) ? files.length : 0));
        }

        return files;
    }

    private static String getBaseFilenameWithSuffix(String name, String mediaPostfix) {
        if (name == null)
            return null;

        int pos = name.lastIndexOf(".");
        if (pos == -1)
            return name + mediaPostfix;

        String baseName = name.substring(0, pos);
        return baseName + mediaPostfix;
    }

    public static File getLocalFanartForFile(File localFile, MediaType mediaType, MediaArtifactType artifactType,
                                             boolean useFolderFanart) {
        if (localFile == null) {
            return null;
        }
        // no local support for actor or genres
        if (artifactType == MediaArtifactType.ACTOR || mediaType == MediaType.ACTOR || mediaType == MediaType.GENRE
                || artifactType == MediaArtifactType.EPISODE)
            return null;

        File file = null;
        localFile = resolveMediaFile(localFile);

        if (localFile == null) {
            log.warn("Unable to resolve local mediafile for local fanart");
            return null;
        }

        String mediaPostFix = localMediaPostfixes.get(artifactType);
        if (mediaPostFix == null) {
            log.info("Can't get local fanart for type: " + artifactType);
        }

        if (SageFanartUtil.IsDVDFile(localFile)) {
            // TODO: Check if we are storing the poster in the Folder or the
            // VIDEO_TS dir
            if (artifactType == MediaArtifactType.POSTER) {
                file = new File(localFile.getParentFile(), getBaseFilenameWithSuffix(localFile.getName(), ".jpg"));
            } else {
                file = new File(localFile.getParentFile(), getBaseFilenameWithSuffix(localFile.getName(), "_" + mediaPostFix));
            }
        } else if (mediaType == MediaType.MUSIC) {

            if (artifactType == MediaArtifactType.ALBUM) {
                file = new File(localFile.getParentFile(), mediaPostFix);
            } else {
                file = new File(localFile.getParentFile(), mediaPostFix);
            }
        } else {
            if (artifactType == MediaArtifactType.POSTER) {
                file = new File(localFile.getParentFile(), getBaseFilenameWithSuffix(localFile.getName(), ".jpg"));
            } else {
                file = new File(localFile.getParentFile(), getBaseFilenameWithSuffix(localFile.getName(), "_" + mediaPostFix));
            }
        }

        // this checks for a "folder.jpg", "background.jpg", "banner.jpg" in the
        // same folder as the media file.
        if (file != null && !file.exists() && useFolderFanart) {
            File nfile = null;
            if (localFile.isDirectory()) {
                nfile = new File(localFile, mediaPostFix);
            } else {
                if (localFile.getParentFile() != null) {
                    nfile = new File(localFile.getParentFile(), mediaPostFix);
                }
            }
            if (nfile != null && nfile.exists()) {
                // if we found a file then use that one, otherwise use the
                // original
                file = nfile;
            }
        }
        return file;
    }

    /**
     * Given Media File, return the "real" file that is the media file. For
     * normal files, this will return itself. For DVD Files, it will return the
     * DVD Parent dir. ie, for a VIDEO_TS dvd dir, it will return the parent of
     * that dir.
     *
     * @param f
     * @return
     */
    public static File resolveMediaFile(File f) {
        if (f == null)
            return null;

        if (f.isDirectory()) {
            if (f.getName().equals("VIDEO_TS") || f.getName().equals("BDMV")) {
                return f.getParentFile();
            }
        }

        return f;
    }

    /**
     * For a given mediafile, resolve it's properties file that sage would use
     * for resolving properties.
     *
     * @param f
     * @return
     */
    public static File resolvePropertiesFile(File f) {
        if (f == null)
            return null;
        f = resolveMediaFile(f);
        return new File(f.getParentFile(), f.getName() + ".properties");
    }

    /**
     * Return true if the File is a Disc folder, ie, DVD or Blu-Ray
     *
     * @param f
     * @return
     */
    public static boolean isDVDFolder(File f) {
        if (f == null)
            return false;

        if (f.isDirectory()) {
            if (f.getName().equals("VIDEO_TS") || f.getName().equals("BDMV")) {
                return true;
            }

            File vts = new File(f, "VIDEO_TS");
            File br = new File(f, "BDMV");
            if (vts.isDirectory() || br.isDirectory()) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    public static List<IMediaArt> getMediaArt(IMetadata md, MediaArtifactType type, int season) {
        return getMediaArt(md,type,season,0);
    }

    public static List<IMediaArt> getMediaArt(IMetadata md, MediaArtifactType type, int season, int collectionID) {
        List<IMediaArt> l = new ArrayList<IMediaArt>();
        for (IMediaArt ma : md.getFanart()) {
            if (collectionID > 0){
                if (ma.getType() == type && collectionID == ma.getCollectionID()) {
                    l.add(ma);
                }
            }else{
                if (ma.getType() == type && season == ma.getSeason()) {
                    l.add(ma);
                }
            }
        }
        return l;
    }

    public static void applyScreenScalingOnSourceImage(File src, File dest, String screenSize) throws Exception {
        applyScreenScalingOnSourceImage(src.toURL(), dest, screenSize);
    }

    public static void applyScreenScalingOnSourceImage(URL src, File dest, String screenSize) throws Exception {
        Dimension dim = parseDimension(screenSize);

        // check if the size is already OK
        Dimension imgSize = ImageUtil.getImageSize(src);
        if (imgSize!=null) {
            if (imgSize.getWidth()<=dim.width && imgSize.getHeight() <=dim.height) {
                log.debug("scale(): Nothing to do, image is already smaller than screen.");
                return;
            }
        }

        // either we can't find the size, or size is too large
        BufferedImage img = ImageUtil.readImage(src);
        if (img.getWidth()<=dim.width && img.getHeight() <=dim.height) {
            log.debug("scale(): Nothing to do, image is already smaller than screen.");
            // nothing to do... since we are already smaller than the screen size
            return;
        }

        BufferedImage newImg = null;
        if (img.getWidth()>=img.getHeight()) {
            // image is wide, so shrink to screen width
            newImg = ImageUtil.createScaledImage(img, dim.width, -1);
        } else {
            // image is higher than width, shrink to screen height
            newImg = ImageUtil.createScaledImage(img, -1, dim.height);
        }

        if (newImg==null) {
            log.debug("scale(): No dest image was created, so nothing to save.");
            // nothing to do
            return;
        }

        if (newImg.getWidth() == img.getWidth()) {
            log.debug("scale(): New image was same size as dest image");
            // nothing to do
            return;
        }

        log.debug("scale(): Writing Dest Image to " + dest);

        // Write the new image
        try {
//            if (dest.toURI().equals(src.toURI())) {
//                // backup src
//                String name = dest.getAbsolutePath();
//                File renTo = new File(name + ".orig");
//                if (!renTo.exists()) {
//                    dest.renameTo(renTo);
//                    dest = new File(name);
//                }
//            }
            ImageUtil.writeImage(newImg, dest);
        } finally {
            // if it failed, then clean up the dest, if it was 0 length
            if (dest.length()==0) {
                if (!dest.delete()) {
                    dest.deleteOnExit();
                }
            }
        }

        // give a hint to run gc
        Runtime.getRuntime().gc();
    }


    public static Dimension parseDimension(String screenSize) {
        if (screenSize==null) return new Dimension(0,0);
        String parts[] = screenSize.split("\\s*x\\s*");
        if (parts.length!=2) parts = screenSize.split("\\s*X\\s*");
        if (parts.length!=2) throw new RuntimeException("Not a valid dimension in the form '###x###' for " + screenSize);
        return new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    public static void applyScreenScalingToAllImageFiles(File startDir, final String screenSize, IProgressMonitor monitor) {
        if (monitor==null) monitor=NullProgressMonitor.INSTANCE;
        monitor.beginTask("Apply Screen Scaling",-1);
        final IProgressMonitor finalMonitor = monitor;
        try {
            DirectoryVisitor.visitDirectory(startDir, new FileExtFileFilter(".jpg,.png"), new DirectoryVisitor.FileVisitor() {
                @Override
                public void visit(File file) {
                    if (finalMonitor.isCancelled()) return;
                    finalMonitor.setTaskName("Scaling " + file.getAbsolutePath());
                    try {
                        try {
                            // NOTE: Overwrites files in place
                            applyScreenScalingOnSourceImage(file, file, screenSize);
                            if (!file.exists() || file.length()==0) {
                                throw new Exception("Failed to scale " + file);
                            }
                        } catch (Exception e) {
                            log.debug("Scaling Failed for " + file, e);
                        }
                    } finally {
                        finalMonitor.worked(1);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Aborting... Failed to process images for scaling", e);
            monitor.setCancelled(true);
            monitor.setTaskName("ERROR: " + e.getMessage());
        } finally {
            monitor.done();
        }
    }
}
