package sagex.phoenix.metadata;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;

import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.util.PropertiesUtils;

public class RatingsManager extends SystemConfigurationFileManager implements SystemConfigurationFileManager.ConfigurationFileVisitor{
	private Properties props = new Properties();
	public RatingsManager(File systemDir, File userDir) throws IOException {
		super(systemDir, userDir, new SuffixFileFilter("ratings.properties", IOCase.INSENSITIVE));
	}
	
	public String getRating(MediaType tv, String rating) {
		String foundRating = null;
		if (!StringUtils.isEmpty(rating)) {
			rating = rating.trim().toUpperCase();
			foundRating = props.getProperty(rating);
		}
		
		if (foundRating==null) {
			// find the default rating for the type
			foundRating = props.getProperty("default/" + tv.sageValue());
		}
		
		return foundRating;
	}

	@Override
	public void visitConfigurationFile(ConfigurationType type, File file) {
		try {
			log.info("Loading Ratings file " + file);
			Properties p=PropertiesUtils.load(file);
			if (p!=null) {
				props.putAll(p);
			}
		} catch (IOException e) {
			log.warn("Failed to load Ratings file " + file);
		}
	}

	@Override
	public void loadConfigurations() {
		log.info("Begin Loading Ratings Map");
		props.clear();
		accept(this);
		log.info("End Loading Ratings Map");
	}
}
