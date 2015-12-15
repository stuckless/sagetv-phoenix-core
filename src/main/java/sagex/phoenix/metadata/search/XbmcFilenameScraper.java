package sagex.phoenix.metadata.search;

import java.io.File;

import org.apache.log4j.Logger;

import sagex.phoenix.scrapers.xbmc.XbmcScraper;
import sagex.phoenix.scrapers.xbmc.XbmcScraperParser;

public abstract class XbmcFilenameScraper implements IFilenameScraper {
    protected Logger log = Logger.getLogger(this.getClass());

    protected XbmcScraper scraper;

    public XbmcFilenameScraper(File scraperFile) throws Exception {
        XbmcScraperParser p = new XbmcScraperParser();
        scraper = p.parseScraper(scraperFile);
    }

    @Override
    public String getId() {
        return scraper.getName();
    }

    @Override
    public int getPriority() {
        // basically prioritizes based on first char of name. (need a better
        // way, but this will do for now)
        return scraper.getName().charAt(0);
    }
}
