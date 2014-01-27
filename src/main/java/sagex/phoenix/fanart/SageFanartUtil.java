package sagex.phoenix.fanart;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.SageAPI;
import sagex.api.AiringAPI;
import sagex.api.AlbumAPI;
import sagex.api.Configuration;
import sagex.api.MediaFileAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.metadata.MediaType;

public class SageFanartUtil {
    private static final Logger log = Logger.getLogger(SageFanartUtil.class);
    private static boolean isGetMediaFileMetadataInstalled = true;
    private static boolean isSetMediaFileMetadataInstalled = true;
    private static Pattern tvSeriesNameScraperRegex = null;
    private static Pattern tvSeriesSeasonScraperRegex = null;

    public static boolean IsDVDFile(Object mediaObject) {
        try {
            if (mediaObject instanceof File) {
                return FanartUtil.isDVDFolder((File) mediaObject);
            } else {
                return MediaFileAPI.IsDVD(mediaObject) || MediaFileAPI.IsBluRay(mediaObject);
            }
        } catch (Throwable e) {
            return false;
        }
    }
    
    public static boolean IsDVDDrive(Object mediaObject) {
        try {
            return MediaFileAPI.IsDVDDrive(mediaObject);
        } catch (Throwable e) {
            return false;
        }
    }
    
    public static File GetFile(Object mediaObject) {
        if (mediaObject instanceof File) {
            return (File) mediaObject;
        }

        File files[] = MediaFileAPI.GetSegmentFiles(mediaObject);
        if (files != null && files.length>0) {
            return files[0];
        } else {
            return null;
        }
    }

    public static SimpleMediaFile GetSimpleMediaFile(Object mediaObject) {
        return GetSimpleMediaFile(mediaObject, false);
    }

    public static SimpleMediaFile GetSimpleMediaFile(Object mediaObject, boolean ignoreMediaTitle) {
        SimpleMediaFile mf = new SimpleMediaFile();

        if (mediaObject == null) {
            return mf;
        }

        if (!ignoreMediaTitle) {
            mf.setTitle(GetMediaFileMetadata(mediaObject, "MediaTitle"));
            if (!isEmpty(mf.getTitle())) {
                mf.setMediaType(MediaType.toMediaType(GetMediaFileMetadata(mediaObject, "MediaType")));
                if (mf.getMediaType() == MediaType.TV) {
                    mf.setSeason(NumberUtils.toInt(GetMediaFileMetadata(mediaObject, FanartUtil.SEASON_NUMBER), 0));
                }
            }
        }

        if (isEmpty(mf.getTitle()) || mf.getMediaType() == null) {
            if (IsDVDFile(mediaObject)) {
                mf.setMediaType(MediaType.MOVIE);
                mf.setTitle(ShowAPI.GetShowEpisode(mediaObject));
                
                // one last attempt to see if this is a TV dvd
                if (isParseFileTitleForTVSeriesEnabled()) {
                    updateSimpleMediaFileFromFilename(mf, mediaObject);
                }
                if(isEmpty(mf.getTitle())){
					//this is a DVD or Blue-ray drive, pass back the folder name to suppress the error and enable posters
                	File f = GetFile(mediaObject);
                	if (f!=null) {
                	    mf.setTitle(f.getName());
                	} else {
                	    mf.setTitle("No Title???");
                	}
                }
            } else if (MediaFileAPI.IsVideoFile(mediaObject) && !MediaFileAPI.IsTVFile(mediaObject)) {
                mf.setMediaType(MediaType.MOVIE);
                mf.setTitle(ShowAPI.GetShowEpisode(mediaObject));
                if (isEmpty(mf.getTitle())) {
                    mf.setTitle(ShowAPI.GetShowTitle(mediaObject));
                }

                // one last attempt to see if we this is a TV video file
                if (isParseFileTitleForTVSeriesEnabled()) {
                    updateSimpleMediaFileFromFilename(mf, mediaObject);
                }
            } else if (MediaFileAPI.IsTVFile(mediaObject) || AiringAPI.GetMediaFileForAiring(mediaObject) == null) {
                mf.setMediaType(MediaType.TV);
                mf.setTitle(ShowAPI.GetShowTitle(mediaObject));
                
                // Now check the alternate category
                String altCat = ShowAPI.GetShowCategory(mediaObject);
                if (altCat != null) {
                    if (altCat.equals("Movie") || altCat.equals(phoenix.api.GetProperty("alternate_movie_category"))) {
                        mf.setMediaType(MediaType.MOVIE);
                    }
                }
            } else if (MediaFileAPI.IsMusicFile(mediaObject)) {
                mf.setMediaType(MediaType.MUSIC);
                mf.setTitle(GetAlbumName(mediaObject));
            }             
        }

        if (log.isDebugEnabled()) {
            if (mf.getMediaType() == MediaType.TV) {
                log.debug("GetSimpleMedia(): Sage Object: " + mediaObject + "; MediaType: " + mf.getMediaType() + "; MediaTitle: " + mf.getTitle() + "; Season: " + mf.getSeason());
            } else {
                log.debug("GetSimpleMedia(): Sage Object: " + mediaObject + "; MediaType: " + mf.getMediaType() + "; MediaTitle: " + mf.getTitle());
            }
        }
        
        return mf;
    }
    
    private static SimpleMediaFile updateSimpleMediaFileFromFilename(SimpleMediaFile mf, Object mediaObject) {
        File f = GetFile(mediaObject);
        Matcher m = GetTVTitleScraperRegexp().matcher(f.getAbsolutePath());
        if (m.find()) {
            mf.setTitle(m.group(1));
            mf.setMediaType(MediaType.TV);
            
            if (m.groupCount()==2) {
               mf.setSeason(NumberUtils.toInt(m.group(2),0)); 
            } else {
               m = GetTVSeasonScraperRegexp().matcher(f.getAbsolutePath());
               if (m.find()) {
                   mf.setSeason(NumberUtils.toInt(m.group(1),0)); 
               }
            }
        }
        return mf;
    }

    public static boolean isParseFileTitleForTVSeriesEnabled() {
        return GetBooleanProperty("phoenix/mediametadata/parseMediaFileForTVSeries", "false");
    }

    public static Pattern GetTVTitleScraperRegexp() {
        if (tvSeriesNameScraperRegex==null) {
            // default will parse a tv title from...
            // c:\\movies\\Bones S02 D03\\ - Show Title.mpg 
            tvSeriesNameScraperRegex = Pattern.compile((String) Configuration.GetProperty("phoenix/mediametadata/tvSeriesRegex", ".*[\\\\/](.*)s([0-9]{1,2})\\s*[exd]([[0-9]]{1,2})"));
        }
        return tvSeriesNameScraperRegex;
    }

    public static Pattern GetTVSeasonScraperRegexp() {
        if (tvSeriesSeasonScraperRegex==null) {
            // default will parse a tv title from...
            // c:\\movies\\Bones S02 D03\\ - Show Title.mpg 
            tvSeriesSeasonScraperRegex = Pattern.compile((String) Configuration.GetProperty("phoenix/mediametadata/tvSeasonRegex", ".*[\\\\/].*s([0-9]{1,2})\\s*[exd]([[0-9]]{1,2})"));
        }
        return tvSeriesSeasonScraperRegex;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    public static Object GetAlbum(Object mediaObject) {
        return MediaFileAPI.GetAlbumForFile(mediaObject);
    }

    public static String GetAlbumName(Object mediaObject) {
        return AlbumAPI.GetAlbumName(GetAlbum(mediaObject));
    }

    public static String GetMediaFileMetadata(Object mediaObject, String id) {
        if (mediaObject == null || id == null) return null;

        String value = null;
        if (isGetMediaFileMetadataInstalled) {
            try {
                value = (String) SageAPI.getProvider().callService("GetMediaFileMetadata", new Object[] { mediaObject, id });
            } catch (Exception e) {
                log.info("Disabling GetMediaFileMetadata(), since it doesn't appear to be installed.");
                isGetMediaFileMetadataInstalled = false;
            }
        }
        return value;
    }

    public static void SetMediaFileMetadata(Object mediaObject, String id, String value) {
        if (mediaObject == null || id == null) return;

        if (isSetMediaFileMetadataInstalled ) {
            try {
                SageAPI.getProvider().callService("SetMediaFileMetadata", new Object[] { mediaObject, id , value});
            } catch (Exception e) {
                log.info("Disabling SetMediaFileMetadata(), since it doesn't appear to be installed.");
                isSetMediaFileMetadataInstalled = false;
            }
        }
    }

    public static String GetAlbumArtist(Object mediaObject) {
        return AlbumAPI.GetAlbumArtist(GetAlbum(mediaObject));
    }

    public static String GetAlbumPersonArtist(Object mediaObject) {
        return ShowAPI.GetPeopleInShowInRole(mediaObject, "Artist");
    }

    public static boolean GetBooleanProperty(String key, String defValue) {
        Object o = Configuration.GetProperty(key, defValue);
        if (o == null) return false;
        String v = String.valueOf(o);
        if ("true".equalsIgnoreCase(v)) return true;
        if ("1".equalsIgnoreCase(v)) return true;
        return false;
    }
}
