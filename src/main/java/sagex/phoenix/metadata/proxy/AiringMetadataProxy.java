package sagex.phoenix.metadata.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import sagex.api.AiringAPI;
import sagex.api.Configuration;
import sagex.api.ShowAPI;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISageMetadata;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.DateUtils;
import sagex.util.TypesUtil;

/**
 * Represents the {@link IMetadata} for a Sage Airing object. Airings are Read
 * Only and represent the EPG data. This provides enough functionality to get
 * Airings showing up in the VFS and to use the VFS apis. set and clear do
 * nothing, isSet always return true and get always return null.
 *
 * @author seans
 */
public class AiringMetadataProxy implements InvocationHandler, ISageMetadata {
    private Logger log = Logger.getLogger(this.getClass());
    private Object sageAiring;

    private boolean movieAiring = false;

    protected AiringMetadataProxy(Object sageAiring) {
        this.sageAiring = sageAiring;
        String showId = ShowAPI.GetShowExternalID(sageAiring);
        if (showId != null && showId.startsWith("MV")) {
            movieAiring = true;
        } else {
            String altCat = ShowAPI.GetShowCategory(sageAiring);
            if (altCat != null) {
                if (altCat.equals("Movie") || altCat.equals(Configuration.GetProperty("alternate_movie_category", "Movie"))) {
                    movieAiring = true;
                }
            }
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return toString();
        } else if ("isSet".equals(method.getName())) {
            return true;
        } else if ("set".equals(method.getName())) {
            return null;
        } else if ("get".equals(method.getName())) {
            return get((SageProperty) args[0]);
        } else if ("clear".equals(method.getName())) {
            return null;
        }

        SageProperty md = method.getAnnotation(SageProperty.class);
        if (md == null) {
            log.warn("Missing MD annotation on method: " + method.getName());
            return null;
        }

        String name = method.getName();
        if (name.startsWith("set")) {
            // sets are ignored...
            return null;
        }

        if (name.startsWith("get") || name.startsWith("is")) {
            if ("Duration".equals(md.value())) {
                return AiringAPI.GetAiringDuration(sageAiring);
            } else if ("AiringTime".equals(md.value())) {
                return new Date(AiringAPI.GetAiringStartTime(sageAiring));
            } else if ("Title".equals(md.value()) || "MediaTitle".equals(md.value())) {
                return AiringAPI.GetAiringTitle(sageAiring);
            } else if ("EpisodeName".equals(md.value())) {
                if (movieAiring) {
                    return AiringAPI.GetAiringTitle(sageAiring);
                } else {
                    return ShowAPI.GetShowEpisode(sageAiring);
                }
            } else if ("MediaType".equals(md.value())) {
                if (movieAiring) {
                    return MediaType.MOVIE.sageValue();
                } else {
                    return MediaType.TV.sageValue();
                }
            } else if ("Genre".equals(md.value())) {
                List<String> list = new ArrayList<String>();
                String s = ShowAPI.GetShowCategory(sageAiring);
                if (s != null)
                    list.add(s);
                s = ShowAPI.GetShowSubCategory(sageAiring);
                if (s != null)
                    list.add(s);
                return list;
            } else if ("Description".equals(md.value())) {
                return ShowAPI.GetShowDescription(sageAiring);
            } else if ("Year".equals(md.value())) {
                return TypesUtil.fromString(ShowAPI.GetShowYear(sageAiring), int.class);
            } else if ("Rated".equals(md.value())) {
                return ShowAPI.GetShowRated(sageAiring);
            } else if ("RunningTime".equals(md.value())) {
                return AiringAPI.GetAiringDuration(sageAiring);
            } else if ("OriginalAirDate".equals(md.value())) {
                return new Date(ShowAPI.GetOriginalAiringDate(sageAiring));
            } else if ("ExtendedRatings".equals(md.value())) {
                return ShowAPI.GetShowExpandedRatings(sageAiring);
            } else if ("Misc".equals(md.value())) {
                return ShowAPI.GetShowMisc(sageAiring);
            } else if ("HDTV".equals(md.value())) {
                return AiringAPI.IsAiringHDTV(sageAiring);
            } else if ("ParentalRating".equals(md.value())) {
                return ShowAPI.GetShowParentalRating(sageAiring);
            } else if ("ExternalID".equals(md.value())) {
                return ShowAPI.GetShowExternalID(sageAiring);
            } else if ("SeasonNumber".equals(md.value())) {
                return ShowAPI.GetShowSeasonNumber(sageAiring);
            } else if ("EpisodeNumber".equals(md.value())) {
                return ShowAPI.GetShowEpisodeNumber(sageAiring);
            } else if ("Actor".equals(md.value())) {
                return cast(md.value());
            } else if ("Guest".equals(md.value())) {
                return cast(md.value());
            } else if ("Director".equals(md.value())) {
                return cast(md.value());
            } else if ("Producer".equals(md.value())) {
                return cast(md.value());
            } else if ("Writer".equals(md.value())) {
                return cast(md.value());
            } else if ("Fanart".equals(md.value())) {
                return new ArrayList<IMediaArt>();
            } else {
                log.debug("invoke(): Airing Metadata Not Handled for: " + method.getName() + "; " + md.value());
            }
        }

        // returns a default typed value
        return TypesUtil.fromString(null, method.getReturnType());
    }

    public String get(SageProperty md) {
        if ("Duration".equals(md.value())) {
            return String.valueOf(AiringAPI.GetAiringDuration(sageAiring));
        } else if ("AiringTime".equals(md.value())) {
            // return new Date(AiringAPI.GetAiringStartTime(sageAiring));
            return null;
        } else if ("Title".equals(md.value()) || "MediaTitle".equals(md.value())) {
            return AiringAPI.GetAiringTitle(sageAiring);
        } else if ("EpisodeName".equals(md.value())) {
            if (movieAiring) {
                return AiringAPI.GetAiringTitle(sageAiring);
            } else {
                return ShowAPI.GetShowEpisode(sageAiring);
            }
        } else if ("MediaType".equals(md.value())) {
            if (movieAiring) {
                return MediaType.MOVIE.sageValue();
            } else {
                return MediaType.TV.sageValue();
            }
        } else if ("Genre".equals(md.value())) {
            PropertyList<String> list = new PropertyList<String>(new GenrePropertyListFactory());
            String s = ShowAPI.GetShowCategory(sageAiring);
            if (s != null)
                list.add(s);
            s = ShowAPI.GetShowSubCategory(sageAiring);
            if (s != null)
                list.add(s);
            return list.getFactory().fromList(list);
        } else if ("Description".equals(md.value())) {
            return ShowAPI.GetShowDescription(sageAiring);
        } else if ("Year".equals(md.value())) {
            return ShowAPI.GetShowYear(sageAiring);
        } else if ("Rated".equals(md.value())) {
            return ShowAPI.GetShowRated(sageAiring);
        } else if ("RunningTime".equals(md.value())) {
            return String.valueOf(AiringAPI.GetAiringDuration(sageAiring));
        } else if ("OriginalAirDate".equals(md.value())) {
            return DateUtils.formatDate(new Date(ShowAPI.GetOriginalAiringDate(sageAiring)));
        } else if ("ExtendedRatings".equals(md.value())) {
            return ShowAPI.GetShowExpandedRatings(sageAiring);
        } else if ("Misc".equals(md.value())) {
            return ShowAPI.GetShowMisc(sageAiring);
        } else if ("HDTV".equals(md.value())) {
            return String.valueOf(AiringAPI.IsAiringHDTV(sageAiring));
        } else if ("ParentalRating".equals(md.value())) {
            return ShowAPI.GetShowParentalRating(sageAiring);
        } else if ("ExternalID".equals(md.value())) {
            return ShowAPI.GetShowExternalID(sageAiring);
        } else if ("Actor".equals(md.value())) {
            return null;
            // return cast(md.value());
        } else if ("Guest".equals(md.value())) {
            return null;
            // return cast(md.value());
        } else if ("Director".equals(md.value())) {
            return null;
            // return cast(md.value());
        } else if ("Producer".equals(md.value())) {
            return null;
            // return cast(md.value());
        } else if ("Writer".equals(md.value())) {
            return null;
            // return cast(md.value());
        } else if ("Fanart".equals(md.value())) {
            return null;
        } else {
            log.debug("Failed to get Metadata for Field: " + md.value());
            return null;
        }

    }

    private List<ICastMember> cast(String value) {
        List<ICastMember> list = new ArrayList<ICastMember>();
        String actors[] = ShowAPI.GetPeopleListInShowInRole(sageAiring, value);
        if (actors != null) {
            CastMemberPropertyListFactory factory = new CastMemberPropertyListFactory();
            for (String a : actors) {
                ICastMember cm = (ICastMember) factory.decode(a);
                if (cm != null) {
                    list.add(cm);
                }
            }
        }
        return list;
    }

    public String toString() {
        return this.getClass().getName();
    }

    public static IMetadata newInstance(Object mediaFile) {
        return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(AiringMetadataProxy.class.getClassLoader(),
                new Class[]{IMetadata.class}, new AiringMetadataProxy(mediaFile));
    }

    @Override
    public boolean isSet(SageProperty key) {
        return false;
    }

    @Override
    public void clear(SageProperty key) {
    }

    @Override
    public void set(SageProperty key, String value) {
    }
}
