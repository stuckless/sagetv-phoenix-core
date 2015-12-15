package sagex.phoenix.metadata.search;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ScraperManager extends SystemConfigurationFileManager implements
        SystemConfigurationFileManager.ConfigurationFileVisitor, IFilenameScraper {
    protected List<IFilenameScraper> scrapers = new ArrayList<IFilenameScraper>();
    protected String name;

    public ScraperManager(String name, File systemDir, File userDir) {
        super(systemDir, userDir, new SuffixFileFilter(new String[]{".js", ".class", ".xml"}));
        this.name = name;
    }

    @Override
    public void loadConfigurations() {
        scrapers.clear();
        log.info("Begin Loading " + name + " Filename Scrapers");

        accept(this);
        // sort by prioriy
        Collections.sort(scrapers, new Comparator<IFilenameScraper>() {
            @Override
            public int compare(IFilenameScraper s1, IFilenameScraper s2) {
                int p1 = s1.getPriority(), p2 = s2.getPriority();
                if (p1 > p2)
                    return 1;
                if (p1 < p2)
                    return -1;
                return 0;
            }
        });
        log.info("End Loading " + name + " Filename Scrapers");
    }

    @Override
    public void visitConfigurationFile(ConfigurationType type, File file) {
        String ext = FilenameUtils.getExtension(file.getName());
        try {
            if ("js".equals(ext)) {
                addScraper(type, loadJavascriptScraper(type, file));
            } else if ("class".equals(ext)) {
                addScraper(type, loadClassScraper(type, file));
            } else if ("xml".equals(ext)) {
                addScraper(type, loadXmbcScraper(type, file));
            } else {
                log.warn("Unknown Scraper: " + file);
            }
        } catch (Throwable t) {
            log.warn("Failed to load " + type + " " + name + " scraper: " + file, t);
        }
    }

    protected IFilenameScraper loadXmbcScraper(ConfigurationType type, File file) throws Exception {
        log.warn("Class needs to implement an Xbmc Scraper!");
        return null;
    }

    protected IFilenameScraper loadClassScraper(ConfigurationType type, File file) throws Exception {
        return (IFilenameScraper) Class.forName(FilenameUtils.getBaseName(file.getName())).newInstance();
    }

    protected IFilenameScraper loadJavascriptScraper(ConfigurationType type, File file) throws Exception {
        log.warn("Javascript scraper not implemented: " + file);
        return null;
    }

    public void addScraper(ConfigurationType type, IFilenameScraper scraper) {
        if (scraper == null) {
            return;
        }

        boolean contains = contains(scraper);
        if (type == ConfigurationType.Plugin && contains) {
            log.warn("Plugins cannot override system scrapers.  Use different scraper id and priority.");
            return;
        }

        scrapers.add(scraper);
        log.info("Added " + type + " " + name + " Scraper: " + scraper.getId() + " with priority " + scraper.getPriority());
    }

    public boolean contains(IFilenameScraper scraper) {
        for (IFilenameScraper p : scrapers) {
            if (scraper.getId().equals(p.getId()))
                return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public SearchQuery createSearchQuery(IMediaFile file, Hints hints) {
        SearchQuery q = null;
        for (IFilenameScraper s : scrapers) {
            try {
                q = s.createSearchQuery(file, hints);
            } catch (Exception e) {
                log.warn("Scraper: " + s.getId() + " produced an error!", e);
            }

            if (q != null && !StringUtils.isEmpty(q.get(Field.RAW_TITLE))) {
                log.info("Scraper " + s.getId() + " created a query: " + q + " for file " + file + " with hints " + hints);
                break;
            }
        }

        if (q == null) {
            log.warn("No " + name + " Scraper could handle file: " + file);
        }

        return q;
    }
}
