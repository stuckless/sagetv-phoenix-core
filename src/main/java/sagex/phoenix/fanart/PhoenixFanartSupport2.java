package sagex.phoenix.fanart;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Central Folder V2 Support as defined the <a
 * href="http://forums.sagetv.com/forums/showthread.php?p=343902&postcount=35"
 * >SageTV Thread</a><br>
 * <p/>
 * <pre>
 * Base Folders
 * CentralFolder\TV
 * CentralFolder\Movies
 * CentralFolder\Music
 * GentralFolder\Actors
 * CentralFolder\Genres
 *
 * TV Folders
 * TV\SeriesTitle\seriesInfo.data (Text file containing base series information Description, Genre, etc)
 * TV\SeriesTitle\Backgrounds\(All Base Backgrounds Images)
 * TV\SeriesTitle\Posters\(All Base Posters Images)
 * TV\SeriesTitle\Banners\(All Base Banners Images)
 * TV\SeriesTitle\Actors\(All Base Actor Images)
 * TV\SeriesTitle\Season #\Backgrounds\(All Season Specific Backgrounds Images)
 * TV\SeriesTitle\Season #\Posters\(All Season Specific Posters Images)
 * TV\SeriesTitle\Season #\Banners\(All Season Specific Banners Images)
 * TV\SeriesTitle\Season #\Actors\(All Season Specific Actor Images)
 * TV\Genres\GenreName\(All GenreName Images)
 *
 * Movies
 * Movies\MovieTitle\Backgrounds\(All Background Images)
 * Movies\MovieTitle\Posters\(All Poster Images)
 * Movies\MovieTitle\Banners\(All Banner Images)
 * Movies\MovieTitle\Actors\(All Actor Images)
 * Movies\Genres\Family\(All Family Genre Images)
 *
 * Actors
 * GentralFolder\Actors\Actor Name\ActorName.jpg
 *
 * Genres
 * GentralFolder\Genres\Genre Name\Posters\(All Genres images)
 * GentralFolder\Genres\Genre Name\Banners\(All Genres images)
 * GentralFolder\Genres\Genre Name\Backgrounds\(All Genres images)
 *
 * Music
 * Music\ArtistName\Posters\
 * Music\ArtistName\Backgrounds\
 * Music\ArtistName\Albums\Album1.jpg
 * Music\ArtistName\Albums\Album2.jpg
 * Music\ArtistName\Albums\Album2.jpg
 * </pre>
 * <p/>
 * <br/>
 * To enable Central Fanart Support, set the following properties
 * <p/>
 * <pre>
 * phoenix/mediametadata/fanartEnabled=true
 * phoenix/mediametadata/fanartCentralFolder=YOUR_CENTRAL_FOLDER
 * </pre>
 * <p/>
 * <br/>
 *
 * @author seans
 */
public class PhoenixFanartSupport2 implements IFanartSupport2, IHasCentralizedFanart {
    private static final Logger log = Logger.getLogger(PhoenixFanartSupport2.class);

    private MetadataConfiguration fanartConfig = null;
    private LocalFanartSupport2 localFanart = new LocalFanartSupport2();

    public PhoenixFanartSupport2() {
        fanartConfig = GroupProxy.get(MetadataConfiguration.class);
        initializeFanartFolder(fanartConfig.getFanartCentralFolder());
    }

    private void initializeFanartFolder(String dir) {
        log.info("Phoenix Fanart initializing");
        if (StringUtils.isEmpty(dir)) {
            dir = System.getProperty("user.dir") + File.separator + "STVs" + File.separator + "Phoenix" + File.separator + "Fanart";
            fanartConfig.setCentralFanartFolder(dir);
        }
        File fanartFolder = new File(dir);
        if (!fanartFolder.exists()) {
            log.warn("Fanart folder does not exist, creating: " + fanartFolder);
            if (!fanartFolder.mkdirs()) {
                log.warn("Failed to create the fanart folder, this may be a permissions problem:  Folder; " + fanartFolder);
            }
            if (!fanartFolder.canWrite()) {
                log.warn("You don't have permissions to write to your fanart folder: " + fanartFolder);
            }
        }
        log.info("Phoenix Fanart initialized");
    }

    public boolean IsFanartEnabled() {
        return fanartConfig.isFanartEnabled();
    }

    public void SetIsFanartEnabled(boolean value) {
        fanartConfig.setFanartEnabled(value);
    }

    public String GetFanartCentralFolder() {
        return fanartConfig.getFanartCentralFolder();
    }

    public void SetFanartCentralFolder(String folder) {
        log.debug("Setting Central Fanart Folder: " + folder);
        fanartConfig.setCentralFanartFolder(folder);
        initializeFanartFolder(folder);
    }

    public void AddFanartArtifacts(List<File> files, String fanartFolder, IMediaFile mediaObject, MediaType mediaType,
                                   String mediaTitle, MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata) {
        File dir = FanartUtil.getCentralFanartDir(mediaType, mediaTitle, artifactType, artifactTitle, fanartFolder, metadata);
        if (dir.exists()) {
            if (artifactType == MediaArtifactType.ALBUM) {
                File f = getSingleImageFile(dir, artifactTitle);
                if (f != null && f.exists()) {
                    files.add(f);
                }
            } else {
                Collection<File> af = FileUtils.listFiles(dir, ImageUtil.IMAGE_FORMATS, false);
                if (af != null && af.size() > 0) {
                    files.addAll(af);
                }
            }
        }
    }

    private File getSingleImageFile(File dir, String baseName) {
        if (!dir.exists())
            return null;
        if (baseName == null)
            return null;
        for (String ext : ImageUtil.IMAGE_FORMATS) {
            File f = new File(dir, baseName + "." + ext);
            if (f.exists())
                return f;
        }
        return null;
    }

    public List<File> GetFanartArtifacts(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                         String artifactTitle, Map<String, String> metadata) {
        List<File> files = new ArrayList<File>();

        String fanartFolder = GetFanartCentralFolder();

        if (!fanartConfig.getUseSeason()) {
            // if we are setup to not use season specific fanart, null out
            // metadata
            metadata = null;
        }

        // get fanart artifacts from the main directory
        if (mediaType != MediaType.MUSIC) {
            AddFanartArtifacts(files, fanartFolder, mf, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        }

        // if tv files, then also include series level fanart
        if (mediaType == MediaType.TV && metadata != null && metadata.containsKey(FanartUtil.SEASON_NUMBER)) {
            AddFanartArtifacts(files, fanartFolder, mf, mediaType, mediaTitle, artifactType, artifactTitle, null);
        }

        // for music fanart
        if (mediaType == MediaType.MUSIC) {
            if (artifactType == MediaArtifactType.ALBUM) {
                // add album fanart
                AddFanartArtifacts(files, fanartFolder, mf, mediaType, mf.getAlbumInfo().getArtist(), MediaArtifactType.ALBUM, mf
                        .getAlbumInfo().getName(), null);
            } else {
                // add artist fanart
                AddFanartArtifacts(files, fanartFolder, mf, mediaType, mf.getAlbumInfo().getArtist(), artifactType, artifactTitle,
                        null);
            }
        }

        // add in local fanart as well
        if (fanartConfig.getLocalFanartEnabled()) {
            List<File> locals = localFanart.GetFanartArtifacts(mf, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
            if (locals != null && locals.size() > 0) {
                files.addAll(locals);
            }
        }

        return files;
    }

    @Override
    public String getDisplayInformation() {
        return "Phoenix Fanart";
    }

    @Override
    public File GetFanartArtifactsDir(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                      String artifactTitle, Map<String, String> metadata) {
        return FanartUtil.getCentralFanartDir(mediaType, mediaTitle, artifactType, artifactTitle, GetFanartCentralFolder(),
                metadata);
    }
}
