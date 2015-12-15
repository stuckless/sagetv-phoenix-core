package sagex.phoenix.vfs.sage;

import org.apache.commons.lang.StringUtils;
import sagex.api.AiringAPI;
import sagex.api.FavoriteAPI;
import sagex.api.MediaFileAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.AiringMetadataProxy;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.*;
import sagex.phoenix.vfs.impl.FileToucher;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SageMediaFile extends VirtualMediaFile implements IMediaFile {
    /**
     * Minimum value that you need to 'touch' a sage media file before sage
     * knows it changed * {@value}
     */
    public static final long MIN_TOUCH_ADJUSTMENT = 1000 * 10;

    private int sageId;

    public SageMediaFile(IMediaFolder parent, Object sageFile) {
        super(parent, null, sageFile, null);
        setTitle(getResolvedTitle());
    }

    @Override
    protected String createId(Object resource) {
        sageId = MediaFileAPI.GetMediaFileID(resource);
        if (sageId != 0) {
            return String.valueOf(sageId);
        }

        // no mediafile id, so try an airing id
        sageId = AiringAPI.GetAiringID(resource);
        if (sageId != 0) {
            return String.valueOf(sageId);
        }

        return null;
    }

    public IAlbumInfo createAlbumInfo() {
        return new SageAlbumInfo(MediaFileAPI.GetAlbumForFile(getMediaObject()));
    }

    public List<File> getFiles() {
        File[] files = MediaFileAPI.GetSegmentFiles(getMediaObject());
        if (files != null) {
            return Arrays.asList(files);
        }
        return Collections.emptyList();
    }

    public boolean isLibraryFile() {
        return MediaFileAPI.IsLibraryFile(getMediaObject());
    }

    public boolean isShowFirstRun() {
        return ShowAPI.IsShowFirstRun(getMediaObject());
    }

    public boolean isShowReRun() {
        return ShowAPI.IsShowReRun(getMediaObject());
    }

    public boolean hasUserCategory(String userCategory) {
        // check if this item contains this userCategory
        // userCategory can be a delimited string and any are valid and checked
        // for a match
        if (userCategory == null) {
            return false;
        }
        String delim = "[,;/]";
        // first check the media file
        String Cats = "";
        Cats = MediaFileAPI.GetMediaFileMetadata(getMediaObject(), "UserCategory");
        for (String Cat : Cats.split(delim)) {
            // userCategory can be a delimited list so check each one
            for (String uCat : userCategory.split(delim)) {
                if (uCat.toLowerCase().trim().equals(Cat.toLowerCase().trim())) {
                    return true;
                }
            }
        }
        // now check the manual recording property for Manual Recordings
        if (AiringAPI.IsManualRecord(getMediaObject())) {
            Cats = AiringAPI.GetManualRecordProperty(getMediaObject(), "UserCategory");
            for (String Cat : Cats.split(delim)) {
                // userCategory can be a delimited list so check each one
                for (String uCat : userCategory.split(delim)) {
                    if (uCat.toLowerCase().trim().equals(Cat.toLowerCase().trim())) {
                        return true;
                    }
                }
            }
        }
        // now check the media object for Favorites
        if (AiringAPI.IsFavorite(getMediaObject())) {
            Cats = FavoriteAPI.GetFavoriteProperty(FavoriteAPI.GetFavoriteForAiring(getMediaObject()), "UserCategory");
            for (String Cat : Cats.split(delim)) {
                // userCategory can be a delimited list so check each one
                for (String uCat : userCategory.split(delim)) {
                    if (uCat.toLowerCase().trim().equals(Cat.toLowerCase().trim())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isWatched() {
        return AiringAPI.IsWatched(getMediaObject());
    }

    public void setWatched(boolean watched) {
        if (watched) {
            AiringAPI.SetWatched(getMediaObject());
        } else {
            AiringAPI.ClearWatched(getMediaObject());
        }
    }

    public boolean isDontLike() {
        return AiringAPI.IsDontLike(getMediaObject());
    }

    public void setDontLike(boolean dontlike) {
        if (dontlike) {
            AiringAPI.SetDontLike(getMediaObject());
        } else {
            AiringAPI.ClearDontLike(getMediaObject());
        }
    }

    public void setLibraryFile(boolean archived) {
        if (archived) {
            MediaFileAPI.MoveFileToLibrary(getMediaObject());
        } else {
            MediaFileAPI.MoveTVFileOutOfLibrary(getMediaObject());
        }
    }

    public boolean isManualRecord() {
        return AiringAPI.IsManualRecord(getMediaObject());
    }

    public void setManualRecord(boolean manual) {
        if (AiringAPI.IsAiringObject(getMediaObject())) {
            if (manual) {
                AiringAPI.Record(getMediaObject());
            } else {
                AiringAPI.CancelRecord(getMediaObject());
            }
            return;
        }
        // does nothing since you cannot change the manual recording property of
        // a sage item
    }

    public Object getThumbnail() {
        return MediaFileAPI.GetThumbnail(getMediaObject());
    }

    public String getResolvedTitle() {
        // recordings store series title in show Title
        // everything else stores the "clean" title in the Show Episode
        String t = null;
        if (isType(MediaResourceType.TV.value()) || isType(MediaResourceType.EPG_AIRING.value())) {
            t = ShowAPI.GetShowTitle(getMediaObject());
            if (StringUtils.isEmpty(t)) {
                t = ShowAPI.GetShowEpisode(getMediaObject());
                log.warn("Found TV Title is wrong place for: " + t);
            }
        } else {
            t = ShowAPI.GetShowEpisode(getMediaObject());
            if (StringUtils.isEmpty(t)) {
                t = ShowAPI.GetShowTitle(getMediaObject());
                log.warn("Found Movie Title is wrong place for: " + t);
            }
        }
        if (StringUtils.isEmpty(t)) {
            t = MediaFileAPI.GetMediaTitle(getMediaObject());
        }
        return sagex.phoenix.util.StringUtils.fixTitle(t);
    }

    public boolean isType(int type) {
        MediaResourceType rtype = MediaResourceType.toMediaResourceType(type);
        if (rtype == null)
            return false;

        if (rtype == MediaResourceType.FILE) {
            return true;
        } else if (rtype == MediaResourceType.FOLDER) {
            return false;
        } else if (rtype == MediaResourceType.BLURAY) {
            return MediaFileAPI.IsBluRay(getMediaObject());
        } else if (rtype == MediaResourceType.VIDEODISC) {
            return isType(MediaResourceType.DVD.value()) && isType(MediaResourceType.BLURAY.value());
        } else if (rtype == MediaResourceType.DVD) {
            return MediaFileAPI.IsDVD(getMediaObject()) && !MediaFileAPI.IsBluRay(getMediaObject());
        } else if (rtype == MediaResourceType.HD) {
            return isType(MediaResourceType.BLURAY.value()) || AiringAPI.IsAiringHDTV(getMediaObject());
        } else if (rtype == MediaResourceType.MUSIC) {
            return MediaFileAPI.IsMusicFile(getMediaObject());
        } else if (rtype == MediaResourceType.PICTURE) {
            return MediaFileAPI.IsPictureFile(getMediaObject());
        } else if (rtype == MediaResourceType.RECORDING) {
            return MediaFileAPI.IsTVFile(getMediaObject());
        } else if (rtype == MediaResourceType.TV) {
            return "TV".equals(MediaFileAPI.GetMediaFileMetadata(getMediaObject(), "MediaType"))
                    || (isType(MediaResourceType.RECORDING.value()) && !MetadataUtil.isRecordedMovie(this))
                    || (isType(MediaResourceType.EPG_AIRING.value()));
        } else if (rtype == MediaResourceType.VIDEO) {
            return MediaFileAPI.IsVideoFile(getMediaObject());
        } else if (rtype == MediaResourceType.ANY_VIDEO) {
            return isType(MediaResourceType.VIDEO.value()) || isType(MediaResourceType.TV.value())
                    || isType(MediaResourceType.DVD.value()) || isType(MediaResourceType.BLURAY.value());
        } else if (rtype == MediaResourceType.EPG_AIRING) {
            return AiringAPI.IsAiringObject(getMediaObject()) && !MediaFileAPI.IsMediaFileObject(getMediaObject());
        } else if (rtype == MediaResourceType.ONLINE) {
            // sage media files are not "online" videos
            return false;
        } else if (rtype == MediaResourceType.DUMMY) {
            return false;
        } else if (rtype == MediaResourceType.MISSINGTV) {
            return false;
        } else if (super.isType(type)) {
            return true;
        } else {
            log.debug("isType(" + rtype + "[" + type + "]" + ") is unhandled.");
            return false;
        }
    }

    public long getWatchedDuration() {
        return AiringAPI.GetWatchedDuration(getMediaObject());
    }

    public boolean delete(Hints hints) {
        boolean deleted = false;
        if (hints != null && hints.getBooleanValue(HINT_DELETE_WITHOUT_PREJUDICE, false)) {
            deleted = MediaFileAPI.DeleteFileWithoutPrejudice(getMediaObject());
        } else {
            deleted = MediaFileAPI.DeleteFile(getMediaObject());
        }

        if (deleted) {
            // should remove ourself fromt he parent
            super.delete(hints);
        }

        return deleted;
    }

    public boolean exists() {
        // SageTV Airings do not exist
        if (isType(MediaResourceType.EPG_AIRING.value())) {
            return false;
        }

        return getFiles().size() > 0;
    }

    public long lastModified() {
        List<File> files = getFiles();
        if (files != null && files.size() > 0) {
            return files.get(0).lastModified();
        }
        return 0;
    }

    public void touch(long time) {
        for (File f : getFiles()) {
            FileToucher.touch(f, time);
        }
    }

    @Override
    public IMetadata createMetadata() {
        if (isType(MediaResourceType.EPG_AIRING.value())) {
            log.debug("Created an Immutable Metadata object for the sage airing: " + getMediaObject());
            // basically for non existing sage files (ie, just simple airings,
            // then do not write directly to the file
            // return
            // COWMetadataProxy.newInstance(MetadataUtil.createMetadata(getMediaObject()));
            return AiringMetadataProxy.newInstance(getMediaObject());
        } else {
            // creates a metadata object that has direct writes to the wiz.bin
            // metadata
            return MetadataUtil.createMetadata(getMediaObject());
        }
    }

    @Override
    public long getEndTime() {
        return AiringAPI.GetAiringEndTime(getMediaObject());
    }

    @Override
    public long getStartTime() {
        return AiringAPI.GetAiringStartTime(getMediaObject());
    }

    @Override
    public String toString() {
        return "SageMediaFile [sageId=" + sageId + ", sageObject=" + getMediaObject() + "]";
    }
}
