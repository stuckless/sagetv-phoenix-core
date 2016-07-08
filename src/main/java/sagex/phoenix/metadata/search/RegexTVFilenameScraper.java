package sagex.phoenix.metadata.search;

import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class RegexTVFilenameScraper extends RegexFilenameScraper {

    public RegexTVFilenameScraper(File regexFile) {
        super(regexFile);
    }

    @Override
    public String getId() {
        return "regextv";
    }

    @Override
    public SearchQuery createSearchQuery(IMediaFile res, Hints hints) {
        String name = getName(res);

        SearchQuery q = new SearchQuery(hints);
        q.setMediaType(MediaType.TV);

        for (Pattern p: regexes) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Testing '" + name + "' using regex " + p.toString());
                }
                Matcher m = p.matcher(name);
                if (m.find()) {
                    String show = clean(group("show", m));
                    if (show==null) {
                        log.warn("Regex does not define a <show> field?? " + p.toString());
                        continue;
                    }

                    q.setSeason(group("season", m));
                    q.setEpisode(group("episode", m));

                    String airing = group("airing", m);
                    if (airing!=null) {
                        q.setAiringId(airing);
                        q.setRawTitle(ScraperUtils.uncompressTitle(show));
                        q.setEpisodeTitle(ScraperUtils.uncompressTitle(group("title", m)));
                        // we have a sagetv airing
                    } else {
                        // we have non sagetv tv show
                        q.setRawTitle(clean(show));
                        q.setEpisodeTitle(clean(group("title", m)));
                        q.setYear(group("year", m));
                        q.setEpisodeDate(date(group("date", m)));
                        q.setEpisodeRangeEnd(group("episodeEnd", m));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Matched filename '" + name + "' using regex '" + p.toString() + "' from file '" + regexFile + "'");
                    }
                    break;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No match for '" + name + "' using " + p.toString());
                    }
                }
            } catch (Throwable t) {
                log.warn("Error", t);
            }
        }

        if (q.getRawTitle()==null) {
            log.debug("Failed to parse any titles using regexes");
            return null;
        }

        return q;
    }

    private String date(String date) {
        if (date==null) return null;
        return date.replaceAll("\\.","-");
    }
}
