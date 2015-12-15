package sagex.phoenix.remote.sync;

import sagex.api.MediaFileAPI;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@API(group = "sync")
public class SyncService {
    public static final int PAGE_SIZE = 1000;

    private Map<String, SyncSession> sessions = new HashMap<String, SyncSession>();

    public SyncService() {
    }

    /**
     * Convenience method for syncMediaFiles(null, mediaMask, 0). This creates a
     * new Sync Session each time it is called. Typically after calling this,
     * you would either call syncMediaFiles(syncId, page) or
     * destroySession(syncId).
     *
     * @param mediaMask
     * @return
     */
    public SyncReply syncMediaFiles(String mediaMask) {
        return syncMediaFiles(null, mediaMask, 0);
    }

    /**
     * Continues a sync session, requesting the next page for the given sync
     * session id
     *
     * @param syncId
     * @param page
     * @return
     */
    public SyncReply syncMediaFiles(String syncId, int page) {
        return syncMediaFiles(syncId, null, page);
    }

    /**
     * Begins/Continues a Sync Session whereby a SyncReply is returned
     * containing the session id the first page of sync data. This sync is a
     * "quick" sync, in that only a minimal amount of data is returned.
     * Subsequent calls will pass a syncId and a page until all data is synced.
     * Once the data is synced, destroySession(syncId) should be called to free
     * up memory.
     *
     * @param syncId
     * @param mediaMask
     * @param page
     * @return
     */
    public SyncReply syncMediaFiles(String syncId, String mediaMask, int page) {
        // grab or create the session
        SyncSession sess = null;
        if (syncId != null) {
            sess = sessions.get(syncId);
        }
        if (sess == null) {
            sess = generateNewSession(syncId, mediaMask);
        }

        // get the media files
        Object[] files = sess.mediaFiles.get();
        if (files == null) {
            files = getMediaFiles(sess.mediaMask);
            sess.mediaFiles = new SoftReference<Object[]>(files);
        }

        int start = page * PAGE_SIZE;
        if (files.length == 0 || start >= files.length) {
            return new SyncReply(sess.id);
        }

        // create the reply
        SyncReply reply = new SyncReply(sess.id);
        reply.totalFiles = files.length;
        reply.totalPages = (int) Math.ceil(((double) files.length) / ((double) PAGE_SIZE));
        reply.page = page;

        int end = Math.min(files.length, start + PAGE_SIZE);

        List<SyncMediaFile> sfiles = new ArrayList<SyncMediaFile>(PAGE_SIZE);
        for (int i = start; i < end; i++) {
            sfiles.add(newMediaFile(files[i]));
        }

        reply.files = sfiles;
        return reply;
    }

    public boolean destroySession(String id) {
        SyncSession sess = sessions.remove(id);
        if (sess != null) {
            sess.mediaFiles = null;
        }

        return sess != null;
    }

    SyncSession generateNewSession(String syncId, String mediaMask) {
        SyncSession sess = new SyncSession();
        if (syncId != null) {
            sess.id = syncId;
        } else {
            sess.id = generationNewId();
        }
        sess.mediaMask = mediaMask;

        return sess;
    }

    String generationNewId() {
        return String.valueOf(System.nanoTime());
    }

    SyncMediaFile newMediaFile(Object m) {
        SyncMediaFile smf = new SyncMediaFile();
        IMediaFile imf = phoenix.media.GetMediaFile(m);
        smf.ID = MediaFileAPI.GetMediaFileID(m);
        smf.Title = imf.getTitle();
        smf.Description = imf.getMetadata().getDescription();

        if (imf.isWatched()) {
            smf.Watched = true;
        } else {
            smf.WatchedDuration = imf.getWatchedDuration();
        }

        IMetadata md = imf.getMetadata();

        if (imf.isType(MediaResourceType.TV.value())) {
            smf.Type = "T";
            smf.EpisodeName = md.getEpisodeName();
            smf.SeasonNumber = md.getSeasonNumber();
            smf.EpisodeNumber = md.getEpisodeNumber();
            if (md.getOriginalAirDate() != null) {
                smf.OriginalAirDate = md.getOriginalAirDate().getTime();
            }
            if (md.getAiringTime() != null) {
                smf.AiringTime = md.getAiringTime().getTime();
            }
        } else if (imf.isType(MediaResourceType.HOME_MOVIE.value())) {
            smf.Type = "H";
        } else if (imf.isType(MediaResourceType.ANY_VIDEO.value())) {
            smf.Type = "V";
            smf.Year = md.getYear();
            smf.RunningTime = md.getRunningTime();
        } else if (imf.isType(MediaResourceType.MUSIC.value())) {
            smf.Type = "M";
            IAlbumInfo info = imf.getAlbumInfo();
            if (info != null) {
                smf.Artist = info.getArtist();
                smf.Album = info.getName();
            }
        } else if (imf.isType(MediaResourceType.PICTURE.value())) {
            smf.Type = "P";
        } else {
            smf.Type = "O";
        }

        if (imf.isType(MediaResourceType.RECORDING.value())) {
            smf.IsRecording = true;
        }

        if ("V".equals(smf.Type)) {
            if (imf.isType(MediaResourceType.BLURAY.value())) {
                smf.DiscType = "B";
            } else if (imf.isType(MediaResourceType.DVD.value())) {
                smf.DiscType = "D";
            }
        }

        return smf;
    }

    Object[] getMediaFiles(String mediaMask) {
        return MediaFileAPI.GetMediaFiles(mediaMask);
    }
}
