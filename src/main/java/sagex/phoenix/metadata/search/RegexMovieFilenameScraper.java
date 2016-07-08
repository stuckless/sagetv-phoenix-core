package sagex.phoenix.metadata.search;

import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class RegexMovieFilenameScraper extends RegexFilenameScraper {

    public RegexMovieFilenameScraper(File regexFile) {
        super(regexFile);
    }

    @Override
    public String getId() {
        return "regexmovie";
    }

    @Override
    public SearchQuery createSearchQuery(IMediaFile res, Hints hints) {
        String name = getName(res);

        SearchQuery q = new SearchQuery(hints);
        q.setMediaType(MediaType.MOVIE);

        for (Pattern p: regexes) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Testing '" + name + "' using regex " + p.toString());
                }
                Matcher m = p.matcher(name);
                if (m.find()) {
                    String title = clean(group("title", m));
                    String year = group("year", m);
                    if (title==null) {
                        log.warn("Regex does not define a <title> field?? " + p.toString());
                        continue;
                    }

                    q.setRawTitle(title);
                    q.setYear(year);
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

}
