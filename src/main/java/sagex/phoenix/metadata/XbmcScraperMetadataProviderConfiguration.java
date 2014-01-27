package sagex.phoenix.metadata;

import java.io.File;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.metadata.provider.xbmc.XbmcMetadataProvider;
import sagex.phoenix.scrapers.xbmc.XbmcScraper;
import sagex.phoenix.scrapers.xbmc.XbmcScraperParser;

public class XbmcScraperMetadataProviderConfiguration extends SystemConfigurationFileManager implements SystemConfigurationFileManager.ConfigurationFileVisitor {
	private MetadataManager manager;

	public XbmcScraperMetadataProviderConfiguration(MetadataManager manager, File systemDir, File userDir) {
		super(systemDir, userDir, new SuffixFileFilter(".xml",	IOCase.INSENSITIVE));
		this.manager = manager;
	}

	@Override
	public void loadConfigurations() {
		log.info("Begin Loading Xbmc Metadata Providers");
		accept(this);
		log.info("End Loading Xbmc Metadata Providers");
	}

	@Override
	public void visitConfigurationFile(ConfigurationType type, File file) {
		try {
			log.info("Loading XBMC Metadata Scraper: " + file);
			XbmcScraperParser parser = new XbmcScraperParser();
			XbmcScraper scraper = parser.parseScraper(file);
			XbmcMetadataProvider p = new XbmcMetadataProvider(scraper);
			manager.addMetaDataProvider(p);
		} catch (Exception e) {
			log.error("Failed to create Xbmc Scraper: " + file, e);
		}
	}
}
