package phoenix.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.api.AiringAPI;
import sagex.api.Configuration;
import sagex.api.MediaFileAPI;
import sagex.api.SeriesInfoAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.cache.ICache;
import sagex.phoenix.cache.MapCache;
import sagex.phoenix.db.UserRecordUtil;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.fanart.IFanartSupport;
import sagex.phoenix.fanart.IFanartSupport2;
import sagex.phoenix.fanart.IHasCentralizedFanart;
import sagex.phoenix.fanart.LocalFanartSupport;
import sagex.phoenix.fanart.PhoenixFanartSupport;
import sagex.phoenix.fanart.PhoenixFanartSupport2;
import sagex.phoenix.fanart.SageMCFanartSupport;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISageCustomMetadataRW;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.Utils;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Fanart is a STV api that encapsulates accessing Backgrounds, Banners,
 * Posters, etc for SageTV media objects.
 * <p/>
 * see {@link PhoenixFanartSupport} for more specific information about where it
 * will look for fanart for the new Central Folder Structure see
 * {@link LocalFanartSupport} for more specific information about where the
 * LocalFanartSupport will look for fanart see {@link SageMCFanartSupport} for
 * more specific information about where it will look for fanart for SageMC
 * <p/>
 * The default support is for {@link PhoenixFanartSupport} but you can switch to
 * using SageMC by setting the following property.
 * <p/>
 * Any developers wishing to use thier own Fanart Support system can careate an
 * implemenation for {@link IFanartSupport} and register it using the
 * phoenix/mediametadata/fanartSupportClass property
 * <p/>
 * <pre>
 * phoenix/mediametadata/fanartSupportClass=sagex.phoenix.fanart.SageMCFanartSupport
 * </pre>
 *
 * @author seans
 */
@API(group = "fanart")
public class FanartAPI {
    /**
     * Store for the tv series fanart defaults * {@value}
     */
    public static final String STORE_SERIES_FANART = "phoenix.seriesfanart";
    /**
     * Store the tv season fanart defaults * {@value}
     */
    private static final String STORE_SEASON_FANART = "phoenix.seasonfanart";

    private static final Logger log = Logger.getLogger(FanartAPI.class);

    private IFanartSupport2 api = null;

    // short term caches
    private ICache<File> singleCache = new MapCache<File>(1000 * 60 * 5);
    private ICache<String[]> multiCache = new MapCache<String[]>(1000 * 60 * 5);

    public FanartAPI() {
        try {
            String prop = (String) Configuration.GetProperty("phoenix/mediametadata/fanartSupportClass",
                    PhoenixFanartSupport.class.getName());

            api = (IFanartSupport2) Class.forName(prop).newInstance();
        } catch (Throwable e) {
            log.warn("Failed to load fanart support class; defaulting to: " + PhoenixFanartSupport2.class.getName(), e);
            api = new PhoenixFanartSupport2();
            Configuration.SetProperty("phoenix/mediametadata/fanartSupportClass", PhoenixFanartSupport2.class.getName());
        }

    }

    /**
     * Given a Sage Airing, Show, or MediaFile, get the background.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject - SageTV MediaFile, Airing, or Show object
     * @return file location of the Background
     */
    public String GetFanartBackground(Object mediaObject) {
        return GetFanartArtifact(mediaObject, null, null, MediaArtifactType.BACKGROUND, null, null);
    }

    private Object sage(Object file) {
        if (file == null)
            return null;
        return phoenix.media.GetSageMediaFile(file);
    }

    private String toString(File file) {
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    private String toStringIfNotNull(File file) {
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Given a Sage Airing, Show, or MediaFile, get the backgrounds.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject - SageTV MediaFile, Airing, or Show object
     * @return Array of backgrounds or null if there are none
     */
    public String[] GetFanartBackgrounds(Object mediaObject) {
        return GetFanartArtifacts(mediaObject, null, null, MediaArtifactType.BACKGROUND, null, null);
    }

    /**
     * Given a Sage MediaFile, Airing or Show, return the fanart Banner for the
     * item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV MediaFile, Airing, or Show
     * @return Return Banner for the given media object
     */
    public String GetFanartBanner(Object mediaObject) {
        return GetFanartArtifact(mediaObject, null, null, MediaArtifactType.BANNER, null, null);
    }

    /**
     * Given a Sage MediaFile, Airing or Show, return the fanart Banners for the
     * item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV MediaFile, Airing, or Show
     * @return Array of banners for the media item
     */
    public String[] GetFanartBanners(Object mediaObject) {
        return GetFanartArtifacts(mediaObject, null, null, MediaArtifactType.BANNER, null, null);
    }

    /**
     * Given a Sage MediaFile, Airing or Show, return the fanart Poster for the
     * item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV MediaFile, Airing, or Show
     * @return Return Banner for the given media object
     */
    public String GetFanartPoster(Object mediaObject) {
        return GetFanartArtifact(mediaObject, null, null, MediaArtifactType.POSTER, null, null);
    }

    /**
     * Given a Sage Music File , return the fanart Album for the item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV Music File
     * @return Return Album for the given media object
     */
    public String GetFanartAlbum(Object mediaObject) {
        return GetFanartArtifact(mediaObject, null, null, MediaArtifactType.ALBUM, null, null);
    }

    /**
     * Given a Sage Music File , return the fanart Album Dir for the item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV Music File
     * @return Return Album Dir for the given media object
     */
    public String GetFanartAlbumPath(Object mediaObject, boolean create) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.ALBUM, null, null, create);
    }

    /**
     * Return the Genre fanart for the given artifact (banner, poster,
     * background)
     *
     * @param genre
     * @param artifactType String value of "banner", "background", "poster"
     * @return
     */
    public String GetFanartGenre(String genre, String artifactType) {
        return GetFanartArtifact(null, MediaType.GENRE, genre, MediaArtifactType.toMediaArtifactType(artifactType), null, null);
    }

    /**
     * Return the Genre fanart dir for the given artifact (banner, poster,
     * background)
     *
     * @param genre
     * @param artifactType String value of "banner", "background", "poster"
     * @return
     */
    public String GetFanartGenrePath(String genre, String artifactType) {
        return GetFanartArtifactDir(null, MediaType.GENRE, genre, MediaArtifactType.toMediaArtifactType(artifactType), null, null,
                false);
    }

    /**
     * Return all the Genre fanart artifacts for the given artifact (banner,
     * poster, background)
     *
     * @param genre
     * @param artifactType String value of "banner", "background", "poster"
     * @return
     */
    public String[] GetFanartGenres(String genre, String artifactType) {
        return GetFanartArtifacts(null, MediaType.GENRE, genre, MediaArtifactType.toMediaArtifactType(artifactType), null, null);
    }

    /**
     * Return the Movie Collection fanart artifact for the given artifact (banner,
     * poster, background)
     *
     * @param collectionname
     * @param artifactType String value of "banner", "background", "poster"
     * @return
     */
    public String GetFanartMovieCollection(String collectionname, String artifactType) {
        return GetFanartArtifact(null, MediaType.MOVIE_COLLECTION, collectionname, MediaArtifactType.toMediaArtifactType(artifactType), null, null);
    }

    /**
     * Return the actor fanart. Actors only have posters/thumbnails, so
     * artifactType is not passed/used.
     *
     * @param actor
     * @return
     */
    public String GetFanartActor(String actor) {
        return GetFanartArtifact(null, MediaType.ACTOR, actor, null, null, null);
    }

    /**
     * Return the actor fanart folder. Actors only have posters/thumbnails, so
     * artifactType is not passed/used.
     *
     * @param actor
     * @return
     */
    public String GetFanartActorPath(String actor) {
        return GetFanartArtifactDir(null, MediaType.ACTOR, actor, null, null, null, false);
    }

    /**
     * Return all the actor fanart images. Actors only have posters/thumbnails,
     * so artifactType is not passed/used.
     *
     * @param actor
     * @return
     */
    public String[] GetFanartActors(String actor) {
        return GetFanartArtifacts(null, MediaType.ACTOR, actor, null, null, null);
    }

    /**
     * Given a Sage MediaFile, Airing or Show, return the fanart Posters for the
     * item.
     * <p/>
     * The API will resolve whether or not Fanart is enabled and whether or not
     * the Central Fanart directory is to be used.
     *
     * @param mediaObject SageTV MediaFile, Airing, or Show
     * @return Return Banner for the given media object
     */
    public String[] GetFanartPosters(Object mediaObject) {
        return GetFanartArtifacts(mediaObject, null, null, MediaArtifactType.POSTER, null, null);
    }

    /**
     * Returns true if Fanart is enabled.
     *
     * @return true if Fanart is enabled
     */
    public boolean IsFanartEnabled() {
        try {
            return api.IsFanartEnabled();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    /**
     * Set whether or not Fanart should be enabled.
     *
     * @param value true if you to set Fanart Enabled, or false to disable Fanart
     */
    public void SetIsFanartEnabled(boolean value) {
        api.SetIsFanartEnabled(value);
    }

    /**
     * Returns the Central Fanart Folder.
     *
     * @return Location of the Central Fanart Folder
     */
    public String GetFanartCentralFolder() {
        if (api instanceof IHasCentralizedFanart) {
            return ((IHasCentralizedFanart) api).GetFanartCentralFolder();
        }
        return null;
    }

    /**
     * Set the location for the Central Fanart Folder
     *
     * @param folder Set a new location for the Central Fanart Folder
     */
    public void SetFanartCentralFolder(Object folder) {
        if (folder == null) {
            return;
        }
        String f = null;
        if (folder instanceof File) {
            f = ((File) folder).getAbsolutePath();
        } else if (folder instanceof String) {
            f = (String) folder;
        }
        if (api instanceof IHasCentralizedFanart) {
            ((IHasCentralizedFanart) api).SetFanartCentralFolder(f);
        }
    }

    private File toFile(Object fanart) {
        if (fanart instanceof String) {
            return new File((String) fanart);
        }
        if (fanart instanceof File) {
            return (File) fanart;
        }
        log.debug("Invalid File Object: " + fanart);
        return null;
    }

    /**
     * Sets the Current Poster to use as the Default. Fanart directory must
     * already exist, and the fanart parameter must exist as a file in the
     * Fanart diretory.
     *
     * @param mediaObject media object on which the default fanart is being set
     * @param fanart      File or String path that must exist, and it must be a file in
     *                    the Fanart directory for that given mediafile.
     */
    public void SetFanartBackground(Object mediaObject, Object fanart) {
        SetFanartArtifact(mediaObject, toFile(fanart), null, null, MediaArtifactType.BACKGROUND, null, null);
    }

    /**
     * Sets the Current Poster to use as the Default. Fanart directory must
     * already exist, and the fanart parameter must exist as a file in the
     * Fanart diretory.
     *
     * @param mediaObject media object on which the default fanart is being set
     * @param fanart      File or String path that must exist, and it must be a file in
     *                    the Fanart directory for that given mediafile.
     */
    public void SetFanartBanner(Object mediaObject, Object fanart) {
        SetFanartArtifact(mediaObject, toFile(fanart), null, null, MediaArtifactType.BANNER, null, null);
    }

    /**
     * Sets the Current Poster to use as the Default. Fanart directory must
     * already exist, and the fanart parameter must exist as a file in the
     * Fanart diretory.
     *
     * @param mediaObject media object on which the default fanart is being set
     * @param fanart      File or String path that must exist, and it must be a file in
     *                    the Fanart directory for that given mediafile.
     */
    public void SetFanartPoster(Object mediaObject, Object fanart) {
        SetFanartArtifact(mediaObject, toFile(fanart), null, null, MediaArtifactType.POSTER, null, null);
    }

    /**
     * Return true if the media item has at least 1 poster
     *
     * @param mediaObject media object
     * @return true if it has fanart
     */
    public boolean HasFanartPoster(Object mediaObject) {
        return GetFanartPoster(mediaObject) != null;
    }

    /**
     * Return true if the media item has at least 1 background
     *
     * @param mediaObject media object
     * @return true if it has fanart
     */
    public boolean HasFanartBackground(Object mediaObject) {
        return GetFanartBackground(mediaObject) != null;
    }

    /**
     * Return true if the media item has at least 1 banner
     *
     * @param mediaObject media object
     * @return true if it has fanart
     */
    public boolean HasFanartBanner(Object mediaObject) {
        return GetFanartBanner(mediaObject) != null;
    }

    /**
     * Return true is this media object is missing any fanart (poster,
     * backgroun, or banner)
     *
     * @param mediaObject media object
     * @return true if it's missing any fanart
     */
    public boolean IsMissingFanart(Object mediaObject) {
        if (phoenix.media.IsType(mediaObject, MediaResourceType.VIDEO.value())) {
            return !(HasFanartBackground(mediaObject) && HasFanartPoster(mediaObject));
        } else if (phoenix.media.IsType(mediaObject, MediaResourceType.TV.value())) {
            return !(HasFanartBackground(mediaObject) && HasFanartBanner(mediaObject) && HasFanartPoster(mediaObject));
        } else {
            return false;
        }
    }

    /**
     * return the Folder where backgrounds for this media object are located.
     *
     * @param mediaObject media object
     * @return folder path
     */
    public String GetFanartBackgroundPath(Object mediaObject) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.BACKGROUND, null, null, false);
    }

    /**
     * return the Folder where banners for this media object are located.
     *
     * @param mediaObject media object
     * @return folder path
     */
    public String GetFanartBannerPath(Object mediaObject) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.BANNER, null, null, false);
    }

    /**
     * return the Folder where posters for this media object are located.
     *
     * @param mediaObject media object
     * @return folder path
     */
    public String GetFanartPosterPath(Object mediaObject) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.POSTER, null, null, false);
    }

    /**
     * return the Folder where backgrounds for this media object are located.
     *
     * @param mediaObject media object
     * @param create      if true, then create the dir if it does not exist.
     * @return folder path
     */
    public String GetFanartBackgroundPath(Object mediaObject, boolean create) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.BACKGROUND, null, null, create);
    }

    /**
     * return the Folder where banners for this media object are located.
     *
     * @param mediaObject media object
     * @param create      if true, then create the dir if it does not exist.
     * @return folder path
     */
    public String GetFanartBannerPath(Object mediaObject, boolean create) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.BANNER, null, null, create);
    }

    /**
     * return the Folder where posters for this media object are located.
     *
     * @param mediaObject media object
     * @param create      if true, then create the dir if it does not exist.
     * @return folder path
     */
    public String GetFanartPosterPath(Object mediaObject, boolean create) {
        return GetFanartArtifactDir(mediaObject, null, null, MediaArtifactType.POSTER, null, null, create);
    }

    /**
     * This method parallels the GetFanartArtifac() call, except this method
     * will return ALL fanart the given set of parameters.
     *
     * @param mediaObject   Sage MediaFile/Airing object (can be null, if you are asking
     *                      for Genres or Actors)
     * @param mediaType     one of tv, movie, music, actor, genre
     * @param mediaTitle    media tile (or genre title if mediaType is genre, or actor
     *                      name if mediaType is actor)
     * @param artifactType  one of poster, background, banner
     * @param artifactTitle this is normally always null, but if it's not, then it will
     *                      basically become a child sub directory in the fanart path
     * @param metadata      any extra metadata, such as SeasonNumber if you doing season
     *                      specific fanart
     * @return
     */
    public String[] GetFanartArtifacts(Object mediaObject, String mediaType, String mediaTitle, String artifactType,
                                       String artifactTitle, Map<String, String> metadata) {
        return GetFanartArtifacts(sage(mediaObject), MediaType.toMediaType(mediaType), mediaTitle,
                MediaArtifactType.toMediaArtifactType(artifactType), artifactTitle, metadata);
    }

    /**
     * Generic Fanart API Call for fetching fanart. Basically each parameter,
     * when not null, will join to form the complete fanart path. For example,
     * if you called,<br/>
     * <p/>
     * <pre>
     * GetFanartArtifact(MediaFile, &quot;tv&quot;, &quot;House&quot;, &quot;poster&quot;, null, null)
     * </pre>
     * <p/>
     * <br/>
     * then it would basically resolve to a fanart folder path of<br/>
     * <p/>
     * <pre>
     * FANARTDIR/TV/House/Posters/
     * </pre>
     * <p/>
     * <br/>
     * If you pass mediaType and mediaTitle as null, but mediaObject is not
     * null, then mediaType and mediaTitle will be automatically fetched from
     * the mediaObject.</br></br> If you want to do season specific fanart (only
     * valid for TV), then pass a Map as the metadata with a key of SeasonNumber
     * and a value for SeasonNumber. If passed, then season specific fanart will
     * be resolved. If No season specific fanart is available, then standard
     * fanart will be available. <br/>
     * <br/>
     * the mediaObject can be any of File, Sage MediaFile, Sage Airing,
     * MediaFile ID, or an {@link IMediaFile} object
     *
     * @param mediaObject   Sage MediaFile/Airing object (can be null, if you are asking
     *                      for Genres or Actors)
     * @param mediaType     one of tv, movie, music, actor, genre
     * @param mediaTitle    media tile (or genre title if mediaType is genre, or actor
     *                      name if mediaType is actor)
     * @param artifactType  one of poster, background, banner
     * @param artifactTitle this is normally always null, but if it's not, then it will
     *                      basically become a child sub directory in the fanart path
     * @param metadata      any extra metadata, such as SeasonNumber if you doing season
     *                      specific fanart
     * @return
     */
    public String GetFanartArtifact(Object mediaObject, String mediaType, String mediaTitle, String artifactType,
                                    String artifactTitle, Map<String, String> metadata) {
        return GetFanartArtifact(sage(mediaObject), MediaType.toMediaType(mediaType), mediaTitle,
                MediaArtifactType.toMediaArtifactType(artifactType), artifactTitle, metadata);
    }

    /**
     * Much like the GetFanartArtifact() call, except this only return the
     * directory. If create == true, then the directory structure will be
     * created, if it does not exist. Null is returned if the folder does not
     * exist and cannot be created.
     *
     * @param mediaObject
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param metadata
     * @param create        Create the folder if it does not exist
     * @return
     */
    public String GetFanartArtifactDir(Object mediaObject, String mediaType, String mediaTitle, String artifactType,
                                       String artifactTitle, Map<String, String> metadata, boolean create) {
        return GetFanartArtifactDir(sage(mediaObject), MediaType.toMediaType(mediaType), mediaTitle,
                MediaArtifactType.toMediaArtifactType(artifactType), artifactTitle, metadata, create);
    }

    public String GetFanartArtifactDir(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                       String artifactTitle, Map<String, String> metadata, boolean create) {

        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        mediaType = resolveMediaType(mediaType, mf);
        mediaTitle = resolveMediaTitle(mediaTitle, mf);
        metadata = resolveFanartMetadata(metadata, mediaType, mf);
        artifactTitle = resolveFanartArtifactTitle(artifactType, artifactTitle, mf);

        File f = api.GetFanartArtifactsDir(mf, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        if (f == null)
            return null;
        if (!f.exists() && create) {
            FileUtils.mkdirsQuietly(f);
        }

        return toStringIfNotNull(f);
    }

    private String resolveFanartArtifactTitle(MediaArtifactType artifactType, String artifactTitle, IMediaFile mf) {
        if (!StringUtils.isEmpty(artifactTitle))
            return artifactTitle;
        if (artifactType == MediaArtifactType.ALBUM) {
            IAlbumInfo info = mf.getAlbumInfo();
            if (info != null) {
                artifactTitle = info.getName();
            }
        }
        return artifactTitle;
    }

    private MediaType resolveMediaType(MediaType mediaType, IMediaFile mf) {
        if (mf == null)
            return mediaType;
        if (mediaType != null)
            return mediaType;

        MediaType mt = null;
        IMetadata md = mf.getMetadata();
        if (md != null) {
            mt = MediaType.toMediaType(md.getMediaType());
        }
        if (mt == null) {
            if (mf.isType(MediaResourceType.TV.value())) {
                mt = MediaType.TV;
            } else if (mf.isType(MediaResourceType.MUSIC.value())) {
                mt = MediaType.MUSIC;
            } else {
                mt = MediaType.MOVIE;
            }
        }

        // do last check for movie files
        // TODO: Move this to the IMetadata implementations
        // AiringMetadataProxy has it already
        String altCat = ShowAPI.GetShowCategory(mf.getMediaObject());
        if (altCat != null) {
            if (altCat.equals("Movie") || altCat.equals(Configuration.GetProperty("alternate_movie_category", "Movie"))) {
                mt = MediaType.MOVIE;
            }
        }

        return mt;
    }

    /**
     * this is meant to return a subset of metadata properties that is useful
     * for determining fanart locations
     *
     * @param mediaType
     * @param mediaObject
     * @return
     */
    private Map<String, String> resolveFanartMetadata(Map<String, String> metaadata, MediaType mediaType, IMediaFile mf) {
        if (mf == null)
            return null;

        // if we are given a metadata map, then use use it, even if it's empty.
        // this allows us to bypass the season specific fanart by passing in an
        // empty metadata map
        if (metaadata != null)
            return metaadata;

        if (mediaType == MediaType.TV) {
            IMetadata md = mf.getMetadata();
            if (md.getEpisodeNumber() > 0) {
                Map<String, String> props = new HashMap<String, String>();
                props.put(FanartUtil.SEASON_NUMBER, String.valueOf(md.getSeasonNumber()));
                props.put(FanartUtil.EPISODE_NUMBER, String.valueOf(md.getEpisodeNumber()));
                return props;
            }
        }
        return null;
    }

    private String[] GetFanartArtifacts(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                        String artifactTitle, Map<String, String> metadata) {

        Object file = sage(mediaObject);
        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        mediaType = resolveMediaType(mediaType, mf);
        mediaTitle = resolveMediaTitle(mediaTitle, mf);
        metadata = resolveFanartMetadata(metadata, mediaType, mf);
        String key = imageKey(file, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        String[] files = multiCache.get(key);

        if (files == null) {
            List<File> fa = api.GetFanartArtifacts(mf, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
            if (fa != null && fa.size() > 0) {
                files = new String[fa.size()];
                for (int i = 0; i < fa.size(); i++) {
                    files[i] = fa.get(i).getAbsolutePath();
                }
            }

            if (files != null) {
                multiCache.put(key, files);
            }
        }

        return files;
    }

    private String GetFanartArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                     String artifactTitle, Map<String, String> metadata) {

        // Added quick caching of image files.
        Object file = sage(mediaObject);
        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        mediaType = resolveMediaType(mediaType, mf);
        mediaTitle = resolveMediaTitle(mediaTitle, mf);
        artifactTitle = resolveFanartArtifactTitle(artifactType, artifactTitle, mf);
        metadata = resolveFanartMetadata(metadata, mediaType, mf);
        String key = imageKey(file, mediaType, mediaTitle, artifactType, artifactTitle, metadata);

        File fa = singleCache.get(key);
        if (fa == null) {
            // check if there is a default artifact
            File def = GetDefaultArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
            if (def != null) {
                singleCache.put(key, def);
                fa = def;
            } else {
                // grab first fanart artifact
                String files[] = GetFanartArtifacts(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
                if (files != null && files.length > 0) {
                    // just use the first one
                    fa = new File(files[0]);
                    singleCache.put(key, fa);
                }
            }
        }

        if (fa == null) {
            log.debug("Fanart was null for: " + mediaObject);
        }

        return toString(fa);
    }

    private String resolveMediaTitle(String mediaTitle, IMediaFile mf) {
        if (mf == null)
            return mediaTitle;
        if (!StringUtils.isEmpty(mediaTitle))
            return mediaTitle;

        // check for music
        if (mf.isType(MediaResourceType.MUSIC.value())) {
            IAlbumInfo info = mf.getAlbumInfo();
            if (info != null) {
                mediaTitle = info.getArtist();
            }
            if (!StringUtils.isEmpty(mediaTitle))
                return mediaTitle;
        }

        IMetadata md = mf.getMetadata();
        if (md != null) {
            mediaTitle = md.getMediaTitle();
            if (StringUtils.isEmpty(mediaTitle))
                mediaTitle = null;
        }

        return Utils.returnNonNull(mediaTitle, mf.getTitle());
    }

    public File GetDefaultArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                   String artifactTitle, Map<String, String> metadata) {

        Object file = sage(mediaObject);
        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        mediaType = resolveMediaType(mediaType, mf);
        mediaTitle = resolveMediaTitle(mediaTitle, mf);
        artifactTitle = resolveFanartArtifactTitle(artifactType, artifactTitle, mf);
        metadata = resolveFanartMetadata(metadata, mediaType, mf);

        if (file == null || artifactType == null)
            return null;

        String key = null;
        if (artifactType == MediaArtifactType.POSTER) {
            key = ISageCustomMetadataRW.FieldName.DEFAULT_POSTER;
        } else if (artifactType == MediaArtifactType.BACKGROUND) {
            key = ISageCustomMetadataRW.FieldName.DEFAULT_BACKGROUND;
        } else if (artifactType == MediaArtifactType.BANNER) {
            key = ISageCustomMetadataRW.FieldName.DEFAULT_BANNER;
        }

        // check if the Metadata has SEASON specific data and handle differently
        Boolean isTVSeason = Boolean.FALSE;
        if (metadata != null) {
            if (metadata.containsKey(FanartUtil.SEASON_NUMBER)) {
                isTVSeason = Boolean.TRUE;
            }
        }

        String def = null;
        if (isTVSeason) {
            String title = resolveMediaTitle(mf.getTitle(), mf);
            String SeasonNumber = metadata.get(FanartUtil.SEASON_NUMBER);
            String SeasonTitle = resolveMediaSeasonTitle(title, SeasonNumber);
            def = UserRecordUtil.getField(STORE_SEASON_FANART, SeasonTitle, artifactType.name());
        } else if (mf.isType(MediaResourceType.TV.value())) {
            // defaults for TV shows need to be stored against the seriesname
            String title = resolveMediaTitle(mf.getTitle(), mf);
            def = UserRecordUtil.getField(STORE_SERIES_FANART, title, artifactType.name());
        } else {
            def = MediaFileAPI.GetMediaFileMetadata(mf.getMediaObject(), key);
        }

        if (!StringUtils.isEmpty(def)) {
            File f = new File(def);

            // check if the fanart was set explicitly
            if (f.exists() && f.isFile()) {
                return f;
            }

            // resolve it relative to the fanart folder
            if (GetFanartCentralFolder() != null) {
                f = new File(GetFanartCentralFolder(), def);
            }

            return f;
        }

        return null;
    }

    /**
     * Sets the Current fanart artifact for the given parameters. For a complete
     * list of what the parameters are, see the GetFanartArtifact method.
     *
     * @param mediaObject
     * @param fanart
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param metadata
     */
    public void SetFanartArtifact(Object mediaObject, File fanart, String mediaType, String mediaTitle, String artifactType,
                                  String artifactTitle, Map<String, String> metadata) {
        SetFanartArtifact(mediaObject, fanart, MediaType.toMediaType(mediaType), mediaTitle,
                MediaArtifactType.toMediaArtifactType(artifactType), artifactTitle, metadata);
    }

    public static String resolveMediaSeasonTitle(String mediaTitle, String SeasonNumber) {
        return mediaTitle + "-" + FanartUtil.SEASON_NUMBER + "-" + SeasonNumber;
    }

    private void SetFanartArtifact(Object mediaObject, File fanart, MediaType mediaType, String mediaTitle,
                                   MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata) {

        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        mediaObject = sage(mediaObject);
        if (mf == null || mediaObject == null) {
            return;
        }
        mediaType = resolveMediaType(mediaType, mf);
        metadata = resolveFanartMetadata(metadata, mediaType, mf);
        mediaTitle = mf.getTitle();

        // check if the Metadata has SEASON specific data and handle differently
        Boolean isTVSeason = Boolean.FALSE;
        if (metadata != null) {
            if (metadata.containsKey(FanartUtil.SEASON_NUMBER)) {
                isTVSeason = Boolean.TRUE;
            }
        }

        try {
            if (!(api instanceof IHasCentralizedFanart)) {
                log.warn("SetFanartArtifact only valid for central fanart implementations");
            }

            String central = (new File(GetFanartCentralFolder())).getCanonicalPath();
            String file = fanart.getCanonicalPath();

            if (!file.startsWith(central)) {
                throw new Exception("You can only set a fanart artifact relative to the fanart folder. Folder: " + central
                        + "; fanart: " + file);
            }

            String art = file.substring(central.length());
            if (art.startsWith(File.separator)) {
                art = StringUtils.strip(art, File.separator);
            }

            // for tv series, store it against the series name
            if (isTVSeason) {
                // special handling for SEASON Defaults
                String SeasonNumber = metadata.get(FanartUtil.SEASON_NUMBER);
                String SeasonTitle = resolveMediaSeasonTitle(mediaTitle, SeasonNumber);
                log.debug("SetFanartArtifact: using special TV SEASON logic for '" + SeasonTitle + "'");
                UserRecordUtil.setField(STORE_SEASON_FANART, SeasonTitle, artifactType.name(), file);
            } else if (mf.isType(MediaResourceType.TV.value())) {
                // tv without season information
                UserRecordUtil.setField(STORE_SERIES_FANART, mediaTitle, artifactType.name(), file);
            } else {
                String key = null;
                if (artifactType == MediaArtifactType.POSTER) {
                    key = ISageCustomMetadataRW.FieldName.DEFAULT_POSTER;
                } else if (artifactType == MediaArtifactType.BACKGROUND) {
                    key = ISageCustomMetadataRW.FieldName.DEFAULT_BACKGROUND;
                } else if (artifactType == MediaArtifactType.BANNER) {
                    key = ISageCustomMetadataRW.FieldName.DEFAULT_BANNER;
                }
                if (key == null)
                    throw new Exception("Invalid Artifact Type: " + artifactType + "; Can't set default artifact.");
                MediaFileAPI.SetMediaFileMetadata(mediaObject, key, art);
            }
        } catch (Exception e) {
            log.warn("Failed to set the default fanart artifact!", e);
        }

        // caches need to be cleared so that we can pick up the changes
        ClearMemoryCaches();
    }

    /**
     * Gets a Series Poster for a given media file OR title
     *
     * @param mediaObject Sage MediaFile or Series Title
     * @return Path to Poster Fanart
     */
    public String GetSeriesPoster(Object mediaObject) {
        Map<String, String> map = Collections.emptyMap();
        if (mediaObject instanceof String) {
            return GetFanartArtifact(null, MediaType.TV, (String) mediaObject, MediaArtifactType.POSTER, null, map);
        } else {
            return GetFanartArtifact(mediaObject, MediaType.TV, null, MediaArtifactType.POSTER, null, map);
        }
    }

    /**
     * Gets a Series Banner for a given media file OR title
     *
     * @param mediaObject Sage MediaFile or Series Title
     * @return Path to Banner Fanart
     */
    public String GetSeriesBanner(Object mediaObject) {
        Map<String, String> map = Collections.emptyMap();
        if (mediaObject instanceof String) {
            return GetFanartArtifact(null, MediaType.TV, (String) mediaObject, MediaArtifactType.BANNER, null, map);
        } else {
            return GetFanartArtifact(mediaObject, MediaType.TV, null, MediaArtifactType.BANNER, null, map);
        }
    }

    /**
     * Gets a Series Background for a given media file OR title
     *
     * @param mediaObject Sage MediaFile or Series Title
     * @return Path to Background Fanart
     */
    public String GetSeriesBackground(Object mediaObject) {
        Map<String, String> map = Collections.emptyMap();
        if (mediaObject instanceof String) {
            return GetFanartArtifact(null, MediaType.TV, (String) mediaObject, MediaArtifactType.BACKGROUND, null, map);
        } else {
            return GetFanartArtifact(mediaObject, MediaType.TV, null, MediaArtifactType.BACKGROUND, null, map);
        }
    }

    /**
     * Gets the Episode Fanart image for a given media file
     *
     * @param mediaObject Sage MediaFile
     * @return Path to Episode Fanart
     */
    public String GetEpisode(Object mediaObject) {
        IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
        Map<String, String> metadata = resolveFanartMetadata(null, MediaType.TV, mf);
        if (metadata == null)
            return null;
        int epNum = NumberUtils.toInt(metadata.get("EpisodeNumber"), -1);

        if (epNum < 0)
            return null;
        String dir = GetFanartArtifactDir(mediaObject, MediaType.TV, null, MediaArtifactType.EPISODE, null, null, false);
        if (dir == null)
            return null;
        File fdir = new File(dir);
        if (!fdir.exists())
            return null;
        String name = String.format("%04d.%s", epNum, ImageUtil.EXT_JPG);
        File ep = new File(fdir, name);
        if (!ep.exists()) {
            ep = new File(fdir, String.format("%04d.%s", epNum, ImageUtil.EXT_PNG));
        }
        if (ep.exists())
            return ep.getAbsolutePath();

        return null;
    }

    /**
     * Gets the Episode Fanart image for a given media file. If create is true,
     * and the fanart doesn't exist, then an image is created from the existing
     * mediafile.
     *
     * @param mediaObject Sage MediaFile
     * @return Path to Episode Fanart
     */
    public String GetEpisode(Object mediaObject, boolean create) {
        String path = GetEpisode(mediaObject);
        log.debug("Using exising episode image for " + mediaObject + "; " + path);
        if (path == null && create) {
            IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
            Map<String, String> metadata = resolveFanartMetadata(null, MediaType.TV, mf);
            if (metadata == null) {
                log.debug("No TV Metadata for " + mediaObject);
                return null;
            }

            int epNum = NumberUtils.toInt(metadata.get("EpisodeNumber"), -1);

            if (epNum < 0) {
                log.debug("No Episode for TV item " + mediaObject);
                return null;
            }
            String dir = GetFanartArtifactDir(mediaObject, MediaType.TV, null, MediaArtifactType.EPISODE, null, null, false);
            if (dir == null) {
                log.debug("Failed to find dir for Episode " + mediaObject);
                return null;
            }

            File fdir = new File(dir);
            if (!fdir.exists()) {
                // create the dir
                FileUtils.mkdirsQuietly(fdir);
            }

            String name = String.format("%04d.jpg", epNum);
            File ep = new File(fdir, name);
            if (!ep.exists()) {
                ep = new File(fdir, String.format("%04d.png", epNum));
            }

            // looks like we have it, so just return it
            if (ep.exists())
                return ep.getAbsolutePath();

            // so it doesn't exist, so lets create one
            if (ep.getParentFile() != null && !ep.getParentFile().exists()) {
                FileUtils.mkdirsQuietly(ep.getParentFile());
            }

            if (!MediaFileAPI.GenerateThumbnail(phoenix.media.GetSageMediaFile(mf), 390, 0, 0, ep)) {
                log.debug("Failed to create episode url for " + mediaObject);
                return null;
            }

            path = ep.getAbsolutePath();
        }
        return path;
    }

    /**
     * Gets the Episode Fanart image for a given media file, or if the Episode
     * is null, then it will return the default thumbnail.
     *
     * @param mediaObject Sage MediaFile
     * @return Path to Episode Fanart or the series image if the episode image
     * is null
     */
    public Object GetDefaultEpisode(Object mediaObject) {
        String img = GetEpisode(mediaObject);
        if (StringUtils.isEmpty(img)) {
            mediaObject = phoenix.media.GetSageMediaFile(mediaObject);
            if (SeriesInfoAPI.HasSeriesImage(mediaObject)) {
                return SeriesInfoAPI.GetSeriesImage(phoenix.media.GetSageMediaFile(mediaObject));
            }
            if (MediaFileAPI.HasAnyThumbnail(mediaObject)) {
                return MediaFileAPI.GetThumbnail(mediaObject);
            }
            return null;
        } else {
            return img;
        }
    }

    /**
     * Clears the in memory cache of images. Caching in memory is fairly short
     * term, but if you need to to flush the memory to pick up changed, then you
     * can use this API.
     * <p/>
     * This method is called automatically when setFanartArifact is used,
     * otherwise, the poster, background, banner, etc, would not be seen for
     * several minutes.
     */
    public void ClearMemoryCaches() {
        singleCache.clear();
        multiCache.clear();
    }

    /**
     * Creates a cache key based on the all the media information available
     *
     * @param mediaObject
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param metadata
     * @return
     */
    public String imageKey(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                           String artifactTitle, Map<String, String> metadata) {

        if (AiringAPI.IsAiringObject(mediaObject)) {
            return sagex.phoenix.util.StringUtils.join(":", AiringAPI.GetAiringID(mediaObject), mediaType, mediaTitle,
                    artifactType, artifactTitle, (metadata == null) ? null : metadata.get("SeasonNumber"),
                    // we only want episodes in the key for episode fanart
                    (metadata != null && MediaArtifactType.EPISODE.equals(artifactType)) ? metadata.get("EpisodeNumber") : null);
        } else {
            return sagex.phoenix.util.StringUtils.join(":", MediaFileAPI.GetMediaFileID(mediaObject), mediaType, mediaTitle,
                    artifactType, artifactTitle, (metadata == null) ? null : metadata.get("SeasonNumber"),
                    // we only want episodes in the key for episode fanart
                    (metadata != null && MediaArtifactType.EPISODE.equals(artifactType)) ? metadata.get("EpisodeNumber") : null);
        }
    }
}
