package sagex.phoenix.fanart;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.download.DownloadHandler;
import sagex.phoenix.download.DownloadItem;
import sagex.phoenix.metadata.*;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.StoredStringSet;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

public class FanartStorage implements DownloadHandler {
    private final Logger log = Logger.getLogger(FanartStorage.class);

    private MetadataConfiguration metadataConfig = GroupProxy.get(MetadataConfiguration.class);

    public void saveFanart(IMediaFile mediaFileParent, String title, IMetadata md, Hints options, String fanartDir)
            throws MetadataException {
        if (title == null) {
            title = md.getMediaTitle();
        }

        if (title == null) {
            title = md.getEpisodeName();
        }

        if (mediaFileParent.isType(MediaResourceType.MUSIC.value())) {
            title = mediaFileParent.getAlbumInfo().getArtist();
        }

        if (title == null) {
            throw new MetadataException("Cannot determin media title.  Cannot store fanart.", mediaFileParent, md);
        }

        File f = new File(fanartDir);
        if (!f.exists()) {
            if (!f.mkdirs())
                throw new MetadataException(
                        "Central Fanart Folder does not exist, and it cannot be created.  Folder: " + fanartDir, mediaFileParent,
                        md);
        }

        if ( (md.getFanart()==null||md.getFanart().size()==0) && phoenix.fanart.IsMissingFanart(mediaFileParent) ) {
            log.info("We appear to be missing any fanart for this item, we will attempt to find locate some");
            try {
                IMetadata mdNew = Phoenix.getInstance().getMetadataManager().searchByExisting(mediaFileParent, md);
                if (mdNew!=null) {
                    md = mdNew;
                }
            } catch (Throwable t) {
                log.warn("Failed to find 'new' fanart for item");
            }
        }

        for (MediaArtifactType mt : MediaArtifactType.values()) {
            saveCentralFanart(title, md, mt, options, fanartDir);
        }
    }

    private void saveCentralFanart(String title, IMetadata md, MediaArtifactType mt, Hints options, String fanartDir)
            throws MetadataException {
        MediaType mediaType = MediaType.toMediaType(md.getMediaType());
        Map<String, String> extraMD = new HashMap<String, String>();

        if (mediaType == null) {
            throw new MetadataException("Invalid MediaType " + md.getMediaType());
        }

        // this is the directory into which to downlaod the artifacts.
        File artifactDir = FanartUtil.getCentralFanartDir(mediaType, title, mt, null, fanartDir, extraMD);
        try {
            downloadFanartArtifacts(mt, artifactDir, md, FanartUtil.getMediaArt(md, mt, 0), options);
        } catch (IOException e) {
            throw new MetadataException("Failed to download fanart artifacts for " + title, e);
        }

        // download collection specific fanart for Movies that are part of a collection as well
        if (mediaType == MediaType.MOVIE && md.getCollectionID() > 0) {
            artifactDir = FanartUtil.getCentralFanartDir(MediaType.MOVIE_COLLECTION, md.getCollectionName(), mt, null, fanartDir, extraMD);
            try {
                downloadFanartArtifacts(mt, artifactDir, md, FanartUtil.getMediaArt(md, mt, 0, md.getCollectionID()), options);
            } catch (IOException e) {
                throw new MetadataException("Failed to download Movie Collection fanart for " + title + "; CollectionID: " + md.getCollectionID() + " Collection: " + md.getCollectionName(),
                        e);
            }
        }

        // download season specific fanart for TV as well
        if (mediaType == MediaType.TV) {
            if (md.getSeasonNumber() > 0) {
                extraMD.put(FanartUtil.SEASON_NUMBER, String.valueOf(md.getSeasonNumber()));
            }
            if (md.getEpisodeNumber() > 0) {
                extraMD.put(FanartUtil.EPISODE_NUMBER, String.valueOf(md.getEpisodeNumber()));
            }
            artifactDir = FanartUtil.getCentralFanartDir(mediaType, title, mt, null, fanartDir, extraMD);
            try {
                downloadFanartArtifacts(mt, artifactDir, md, FanartUtil.getMediaArt(md, mt, md.getSeasonNumber()), options);
            } catch (IOException e) {
                throw new MetadataException("Failed to download Season fanart for " + title + "; Season: " + md.getSeasonNumber(),
                        e);
            }
        }

    }

    private void downloadFanartArtifacts(MediaArtifactType mt, File fanartDir, IMetadata md, List<IMediaArt> artwork, Hints options)
            throws IOException {
        int downloaded = 0;
        int max = metadataConfig.getMaxDownloadableImages();
        if (max == -1) {
            max = 99;
        }
        if (artwork != null && artwork.size() > 0) {
            // eventually i'll add a "REFRESH_FANART" command of some type
            if (options!=null && options.getBooleanValue(MetadataHints.REFRESH, false)) {
                // if we are refreshing, then delete all files in this directory
                log.info("Refreshing Fanart.  Removing old fanart.");
                fanartDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.getName().equalsIgnoreCase("images")) {
                            FileUtils.deleteIfLastModifiedGreater(file, 5 * 60 * 1000, log);
                        } else if (file.getName().toLowerCase().endsWith(".jpg")) {
                            FileUtils.deleteIfLastModifiedGreater(file, 5 * 60 * 1000, log);
                        } else if (file.getName().toLowerCase().endsWith(".png")) {
                            FileUtils.deleteIfLastModifiedGreater(file, 5 * 60 * 1000, log);
                        }
                        return false;
                    }
                });
            }

            max = Math.min(max, artwork.size());
            for (IMediaArt ma : artwork) {
                downloadFanartArtifact(mt, ma, md, fanartDir, options);
                downloaded++;
                if (downloaded >= max)
                    break;
            }
        } else {
            log.warn("No " + mt + " for " + fanartDir.getAbsolutePath() + " in the metadata.");
        }
    }

    private void downloadFanartArtifact(MediaArtifactType mt, IMediaArt mediaArt, IMetadata md, File downloadDir, Hints options)
            throws IOException {
        String url = mediaArt.getDownloadUrl();
        File downloadFile = null;

        if (!shouldSkipFile(downloadDir, url)) {
            // we create a tmp name, since various different source could have
            // the same name
            String name = new String(Hex.encodeHex(DigestUtils.md5(url)));

            if (mt == MediaArtifactType.EPISODE) {
                Map<String, String> m = new HashMap<String, String>();
                m.put(FanartUtil.SEASON_NUMBER, String.valueOf(md.getSeasonNumber()));
                m.put(FanartUtil.EPISODE_NUMBER, String.valueOf(md.getEpisodeNumber()));
                downloadFile = new File(downloadDir, FanartUtil.getEpisodeFilename(m));
            } else {
                downloadFile = new File(downloadDir, name + "-" + new File(url).getName());
            }

            if (downloadFile.getName().indexOf(".") == -1) {
                // no ext, so use a default .jpg ext
                downloadFile = new File(downloadDir, downloadFile.getName() + ".jpg");
            }

            // NOTE: We are updating it here, since otherwise, the downloader
            // which happens in the background will download the items if they
            // are
            // added very quickly, which results in the remote server being
            // hammered
            try {
                updateSkipFile(downloadFile, url);
            } catch (IOException e) {
                log.warn("Failed to add item to the image skip file: " + url);
            }

            if (downloadFile.exists()) {
                log.warn("Will not overwrite file: " + downloadFile
                        + "; Consider removing the file if you want it updated in the future.");
                return;
            }

            DownloadItem di = new DownloadItem();
            di.setRemoteURL(new URL(url));
            di.setLocalFile(downloadFile);
            di.setHandler(this);

            // add in the media artifact type... which will be used if scaling is required
            di.setUserObject(mt);

            // schedule download
            Phoenix.getInstance().getDownloadManager().download(di);
        }
    }

    private synchronized boolean shouldSkipFile(File artifactDir, String url) {
        boolean skip = false;
        try {
            File storedFile = new File(artifactDir, "images");
            StoredStringSet set = new StoredStringSet();
            if (storedFile.exists()) {
                StoredStringSet.load(set, storedFile);
                if (set.contains(url)) {
                    log.info("Skipping Image file: " + url + " because it's in the image skip file.");
                    skip = true;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load/save the skip file for image (probably a permission issue): " + artifactDir);
        }
        return skip;
    }

    @Override
    public void onComplete(DownloadItem item) {
        Loggers.METADATA.info("FANART-DOWNLOAD: " + item.getLocalFile() + "; " + item.getRemoteURL());
        File file = item.getLocalFile();
        if (!file.exists()) {
            Loggers.METADATA.warn("FANART-DOWNLOAD-FAILED: " + item.getRemoteURL());
            return;
        }
        if (file.length() < (metadataConfig.getDeleteImagesSmallerThan() * 1024)) {
            Loggers.METADATA.warn("FANART-DOWNLOAD-TOO-SMALL: " + item.getRemoteURL() + "; SIZE: " + file.length());
            return;
        }

        if (metadataConfig.scaleLargeFanart()) {
            try {
                FanartUtil.applyScreenScalingOnSourceImage(file, file, metadataConfig.getMaxScreenSize());
            } catch (Exception e) {
                Loggers.LOG.warn("Fanart Scaling Failed", e);
            }
        }
    }

    private synchronized void updateSkipFile(File file, String url) throws IOException {
        File artifactDir = file.getParentFile();
        File storedFile = new File(artifactDir, "images");
        StoredStringSet set = new StoredStringSet();
        if (storedFile.exists()) {
            StoredStringSet.load(set, storedFile);
        }
        if (!storedFile.getParentFile().exists()) {
            sagex.phoenix.util.FileUtils.mkdirsQuietly(storedFile.getParentFile());
        }
        set.add(url);
        StoredStringSet.save(set, storedFile, "Ignoring these image files");
        log.debug("Added Image file: " + url + " to the image skip file.");
    }

    @Override
    public void onError(DownloadItem item) {
        Loggers.METADATA.info("FANART-ERROR: " + item.getLocalFile() + ";" + item.getRemoteURL() + "; "
                + ((item.getError() != null) ? item.getError().getMessage() : ""));
    }

    @Override
    public void onStart(DownloadItem item) {
    }
}
