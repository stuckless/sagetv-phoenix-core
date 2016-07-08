package sagex.phoenix.metadata.search;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seans on 08/07/16.
 */
public abstract class RegexFilenameScraper implements IFilenameScraper {
    File regexFile;Logger log = Logger.getLogger(this.getClass());
    List<Pattern> regexes = new ArrayList<>();

    public RegexFilenameScraper(File file) {
        loadRegex(file);
    }

    void loadRegex(File regexFile) {
        this.regexFile=regexFile;
        try {
            log.debug("Loading Regex Filename Scrapers from " + regexFile);
            List<Pattern> patterns = new ArrayList<>();
            for (String s: FileUtils.readLines(regexFile)) {
                if (s.trim().length()==0 || s.trim().charAt(0)=='#') continue;
                try {
                    Pattern p = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
                    patterns.add(p);
                } catch (Throwable t) {
                    log.error("Skipping invalid Regex Pattern: '"+s+"'", t);
                }
            }
            if (patterns.size()>0) {
                this.regexes = patterns;
                log.info("Loaded " + patterns.size() + " filename patterns from " + regexFile);
            } else {
                log.warn("No patterns loaded from " + this.regexFile);
            }
        } catch (Throwable t) {
            log.error("Failed to process Regex File: " + regexFile);
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    protected String clean(String title) {
        if (title==null) return null;
        title=title.trim();
        if (title.endsWith("-")) return clean(title.substring(0, title.length()-1));
        return title;
    }

    protected String group(String field, Matcher m) {
        try {
            return m.group(field).trim();
        } catch (Throwable t) {
        }
        return null;
    }

    protected String getName(IMediaFile res) {
        String filenameUri = null;

        try {
            filenameUri = URLDecoder.decode(PathUtils.getLocation(res));
        } catch (Throwable t) {
            filenameUri = PathUtils.getLocation(res);
        }

        String name = new File(filenameUri).getName();
        if (name.indexOf(' ')==-1 && StringUtils.countMatches(name,'.')>1) {
            // we have name with no spaces but multiple dots... let's expand it.
            name = SearchUtil.specialHandleDots(name);
        }
        return name;
    }
}
