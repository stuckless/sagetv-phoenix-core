package sagex.phoenix.metadata.provider.tvdb;

import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.BannerType;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Episode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.ITVMetadataProvider;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;

import java.util.List;

public class TVDBItemParser {
    private static final Logger log = Logger.getLogger(TVDBItemParser.class);

    private IMetadata md = null;
    private IMetadataSearchResult result = null;
    private TVDBMetadataProvider provider;

    public TVDBItemParser(TVDBMetadataProvider prov, IMetadataSearchResult result) {
        this.provider = prov;
        this.result = result;
    }

    public IMetadata getMetadata() throws MetadataException {
        if (md == null) {
            try {
                log.debug("Getting Metadata for Result: " + result);

                // parse and fill
                md = MetadataProxy.newInstance();

                // set our provider info
                md.setMediaProviderID(provider.getInfo().getId());
                md.setMediaProviderDataID(result.getId());

                // update with the query args, and then overwrite if needed
                md.setSeasonNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.SEASON.name())));
                md.setEpisodeNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.EPISODE.name())));
                md.setDiscNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.DISC.name())));
                md.setOriginalAirDate(DateUtils.parseDate(result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name())));
                md.setMediaType(result.getMediaType().sageValue());

                addSeriesInfo(md);

                String season = result.getExtra().get(SearchQuery.Field.SEASON.name());
                String episode = result.getExtra().get(SearchQuery.Field.EPISODE.name());
                String date = result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name());
                String title = result.getExtra().get(SearchQuery.Field.EPISODE_TITLE.name());

                if (!StringUtils.isEmpty(season) && !StringUtils.isEmpty(episode)) {
                    addSeasonEpisodeInfo(md, season, episode);
                }

                if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(date)) {
                    addSeasonEpisodeInfoByDate(md, date);
                }

                if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(title)) {
                    addSeasonEpisodeInfoByTitle(title);
                }

                // now add in banners, no point in doing it early
                addBanners(md, season);
            } catch (MetadataException me) {
                throw me;
            } catch (Throwable e) {
                throw new MetadataException("Failed while parsing series: " + result, e);
            }
        }

        return md;
    }

    private void addSeriesInfo(IMetadata md) throws Exception {
        ISeriesInfo info = ((ITVMetadataProvider) provider).getSeriesInfo(md.getMediaProviderDataID());
        // copy some of the series info to the IMetadata object
        md.getActors().addAll(info.getCast());
        md.setParentalRating(MetadataUtil.fixContentRating(MediaType.TV, info.getContentRating()));
        md.getGenres().addAll(info.getGenres());
        md.setUserRating(info.getUserRating());

        md.setRelativePathWithTitle(info.getTitle());
        md.setMediaTitle(info.getTitle());

        md.setRunningTime(info.getRuntime());
    }

    private void updateMetadataFromElement(IMetadata md, Episode el) {
        md.setSeasonNumber(el.getSeasonNumber());
        md.setEpisodeNumber(el.getEpisodeNumber());
        md.setEpisodeName(sagex.phoenix.util.StringUtils.unquote(el.getEpisodeName()));

        // actually this is redundant because the tvdb is already YYYY-MM-DD,
        // but this will
        // ensure that we are safe if out internal mask changes
        md.setOriginalAirDate(DateUtils.parseDate(el.getFirstAired()));
        // YEAR is not set for TV Metadata, we get that from the Series Info
        // md.setYear(DateUtils.parseYear(DOMUtils.getElementValue(el,
        // "FirstAired")));
        md.setDescription(el.getOverview());
        md.setUserRating(MetadataSearchUtil.parseUserRating(el.getRating()));
        md.setIMDBID(el.getImdbId());

        String epImage = el.getFilename();
        if (!StringUtils.isEmpty(epImage)) {
            // Added for EvilPenguin
            md.getFanart().add(
                    new MediaArt(MediaArtifactType.EPISODE, epImage, md.getSeasonNumber()));
        }

        addCastMember(el.getGuestStars(), md.getGuests());
        addCastMember(el.getWriters(), md.getWriters());
        addCastMember(el.getDirectors(), md.getDirectors());
    }

    private void addCastMember(List<String> in, List<ICastMember> cast) {
        if (in!=null && in.size()>0) {
            for (String d : in) {
                if (!StringUtils.isEmpty(d)) {
                    CastMember cm = new CastMember();
                    cm.setName(d.trim());
                    cast.add(cm);
                }
            }
        }
    }

    private void addSeasonEpisodeInfoByDate(IMetadata md, String date) throws TvDbException {
            // tvdb requires dashes not dots
            if (date != null)
                date = date.replace('.', '-');

            List<Episode> episodes = provider.getTVDBApi().getAllEpisodes(result.getId(), provider.getLanguage());

            if (episodes!=null && episodes.size()>0) {
                for (Episode e : episodes) {
                    if (date.equals(e.getFirstAired())) {
                        // we get the by date xml and then request season/episode
                        // specific one, because the by date xml
                        // is not the same as the by season/episode
                        updateMetadataFromElement(md, e);
                        break;
                    }
                }
            }
    }

    private void addSeasonEpisodeInfoByTitle(String title) throws TvDbException {
            log.info("TVDB Title: " + title);

            List<Episode> nl = provider.getTVDBApi().getAllEpisodes(result.getId(), provider.getLanguage());
            boolean updated = updateIfScored(nl, title, 1.0f);
            if (!updated) {
                float matchScore = 0.8f;
                log.debug("Couldn't find an exact title match, so using a fuzzy match score of " + matchScore);

                // do another search, this time use a less sensitive matching
                // criteria
                updated = updateIfScored(nl, title, matchScore);
            }

            if (!updated) {
                log.info("Unable to match a direct title for: " + title);
            }
    }

    private boolean updateIfScored(List<Episode> nl, String title, float scoreToMatch) {
        if (nl==null) return false;

        boolean updated = false;
        int s = nl.size();
        for (int i = 0; i < s; i++) {
            Episode el = nl.get(i);
            String epTitle = el.getEpisodeName();
            float score = MetadataSearchUtil.calculateCompressedScore(title, epTitle);

            if (score >= scoreToMatch) {
                log.debug("Found a title match: " + epTitle + "; Updating Metadata.");
                updateMetadataFromElement(md, el);
                updated = true;
                break;
            }
        }
        return updated;
    }

    private void addSeasonEpisodeInfo(IMetadata md, String season, String episode) {
        int inSeason = NumberUtils.toInt(season, -1);
        int inEpisode = NumberUtils.toInt(episode, -1);

        if (inSeason > 0 && inEpisode > 0) {
            try {
                updateMetadataFromElement(md, provider.getTVDBApi().getEpisode(result.getId(), inSeason, inEpisode, provider.getLanguage()));
            } catch (Exception e) {
                log.warn("Failed to get season/episode specific information for " + result.getId() + "; Season: " + season
                        + "; episode: " + episode, e);
            }
        } else {
            // TODO: Someday, allow for Season 0 and specials
            log.warn("Can't do lookup by season/epsidoe for season: " + season + "; episode: " + episode);
        }
    }

    private void addBanners(IMetadata md, MediaArtifactType mat, List<Banner> list) {
        if (list!=null && list.size()>0) {
            for (Banner b: list) {
                if (provider.getLanguage().equals(b.getLanguage())) {
                    MediaArt ma = new MediaArt();
                    ma.setType(mat);
                    addFanartUrl(md, ma, b.getUrl());
                }
            }
        }
    }

    private void addBanners(IMetadata md, String season) throws TvDbException {
        int inSeason = NumberUtils.toInt(season, -9);
            Banners banners = provider.getTVDBApi().getBanners(result.getId());

            addBanners(md, MediaArtifactType.POSTER, banners.getPosterList());
            addBanners(md, MediaArtifactType.BACKGROUND, banners.getFanartList());
            addBanners(md, MediaArtifactType.BANNER, banners.getSeriesList());

            if (inSeason>0) {
                List<Banner> list = banners.getSeasonList();
                MediaArtifactType mat = MediaArtifactType.BACKGROUND;
                if (list != null && list.size() > 0) {
                    for (Banner b : list) {
                        if (provider.getLanguage().equals(b.getLanguage())) {
                            if (inSeason == b.getSeason()) {
                                MediaArt ma = null;
                                if (b.getBannerType2() == BannerType.SEASON) {
                                    ma = new MediaArt();
                                    ma.setType(MediaArtifactType.POSTER);
                                } else if (b.getBannerType2() == BannerType.SEASONWIDE) {
                                    ma = new MediaArt();
                                    ma.setType(MediaArtifactType.BANNER);
                                }
                                if (ma != null) {
                                    ma.setSeason(inSeason);
                                    addFanartUrl(md, ma, b.getUrl());
                                }
                            }
                        }
                    }
                }
            }
    }

    private void addFanartUrl(IMetadata md, MediaArt ma, String path) {
        if (StringUtils.isEmpty(path))
            return;
        ma.setDownloadUrl(path);
        md.getFanart().add(ma);
    }
}
