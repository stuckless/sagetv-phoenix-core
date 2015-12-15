package sagex.phoenix.metadata.persistence;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.api.SeriesInfoAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.*;
import sagex.phoenix.util.LogUtil;
import sagex.phoenix.util.Pair;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

import java.util.List;

/**
 * Utility Functions for TV Series Info
 *
 * @author seans
 */
public class TVSeriesUtil {
    private static Logger log = Logger.getLogger(TVSeriesUtil.class);

    public TVSeriesUtil() {
    }

    public static ISeriesInfo getSeriesInfo(IMediaFile file) {
        ISeriesInfo info = phoenix.media.GetSeriesInfo(file);
        return info;
    }

    /**
     * Locates a local {@link ISeriesInfo} based on an exact match of the Title
     * and optional year.
     *
     * @param title
     * @param year
     * @return {@link ISeriesInfo} or null if nothing found
     */
    public static ISeriesInfo findByShowTitle(String title, int year) {
        if (title == null)
            return null;

        Object all[] = SeriesInfoAPI.GetAllSeriesInfo();
        for (Object o : all) {
            String sageTitle = SeriesInfoAPI.GetSeriesTitle(o);
            if (sageTitle == null)
                continue;

            if (sageTitle.equalsIgnoreCase(title)) {
                return new SageSeriesInfo(o);
            }

            if (year > 0) {
                if (sageTitle.equalsIgnoreCase(title + " (" + year + ")")) {
                    return new SageSeriesInfo(o);
                }
            }
        }

        return null;

    }

    /**
     * Locates a local {@link ISeriesInfo} based on an exact match of the Title.
     *
     * @param title
     * @return {@link ISeriesInfo} or null if nothing found
     */
    public static ISeriesInfo findByShowTitle(String title) {
        return findByShowTitle(title, 0);
    }

    /**
     * Creates a new SeriesInfoID from the given provider and data id
     *
     * @param provId
     * @param dataId
     * @return
     */
    public static int createNewSeriesInfoId(String provId, String dataId) {
        // sage ids are between 10,000,000 - 99,999,999
        // so our ids need to be larger than those
        int baseid = 200000000;
        int data = NumberUtils.toInt(dataId);
        if (data == 0) {
            log.warn("Unable to create a SeriesInfoID for " + provId + ":" + dataId);
            return 0;
        }

        int id = baseid + data;
        if (id >= 100000 && id <= 99999999) {
            log.warn("New ID Collides with SageTV ids... returning 0");
            return 0;
        }

        return id;
    }

    /**
     * Returns a {@link Pair} of String Arrays that represent the People and
     * Characters for the given {@link ICastMember} list
     *
     * @param cast
     * @return
     */
    public static Pair<String[], String[]> createCastStrings(List<ICastMember> cast) {
        if (cast == null || cast.size() == 0) {
            return new Pair<String[], String[]>(new String[]{}, new String[]{});
        }

        String people[] = new String[cast.size()];
        String roles[] = new String[cast.size()];

        for (int i = 0; i < cast.size(); i++) {
            ICastMember cm = cast.get(i);
            people[i] = cm.getName();
            roles[i] = cm.getRole();
        }
        return new Pair<String[], String[]>(people, roles);
    }

    /**
     * Will find and update the TVSeriesInfo for a tv show
     *
     * @param file
     * @return
     */
    public static boolean updateTVSeriesInfoForFile(IMediaFile file) {
        try {
            if (file.isType(MediaResourceType.TV.value())) {
                IMetadata newMD = file.getMetadata();
                ISeriesInfo curInfo = phoenix.media.GetSeriesInfo(file);
                if (curInfo == null) {
                    // no series info, so see if we can find one by title
                    ISeriesInfo info = TVSeriesUtil.findByShowTitle(newMD.getMediaTitle(), newMD.getYear());
                    if (info == null) {
                        // no series info, do a lookup using the tv provider
                        IMetadataProvider prov = Phoenix.getInstance().getMetadataManager().getProvider(newMD.getMediaProviderID());
                        if (prov != null && prov instanceof ITVMetadataProvider) {
                            // save this TVSeriesInfo and then link it to the
                            // mediafile
                            ISeriesInfo newinfo = ((ITVMetadataProvider) prov).getSeriesInfo(newMD.getMediaProviderDataID());
                            if (newinfo != null) {
                                // we have a series info, now save it
                                int id = TVSeriesUtil.createNewSeriesInfoId(newMD.getMediaProviderID(),
                                        newMD.getMediaProviderDataID());
                                if (id > 0) {
                                    Pair<String[], String[]> cast = TVSeriesUtil.createCastStrings(newinfo.getCast());
                                    Object sageinfo = SeriesInfoAPI.AddSeriesInfo(id, newinfo.getTitle(), newinfo.getNetwork(),
                                            newinfo.getDescription(), newinfo.getHistory(), newinfo.getPremiereDate(),
                                            newinfo.getFinaleDate(), newinfo.getAirDOW(), newinfo.getAirHrMin(),
                                            (String) newinfo.getImage(), cast.first(), cast.second());
                                    if (sageinfo != null) {
                                        info = new SageSeriesInfo(sageinfo);
                                        info.setRuntime(newinfo.getRuntime());
                                        info.setUserRating(newinfo.getUserRating());
                                        info.setContentRating(newinfo.getContentRating());
                                        info.getGenres().addAll(newinfo.getGenres());
                                        log.info("Added new Series Info " + sageinfo);
                                        LogUtil.logNewTVSeriesInfoAdded(info);
                                    }
                                }
                            }
                        }
                    }
                    if (info != null) {
                        log.debug("Assigned Series Info " + info.getSeriesInfoID() + " to file " + file);
                        newMD.setSeriesInfoID(NumberUtils.toInt(info.getSeriesInfoID()));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update the Series Info for " + file, e);
        }
        return false;
    }
}
