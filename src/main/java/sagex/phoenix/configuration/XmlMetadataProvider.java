package sagex.phoenix.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class XmlMetadataProvider implements IConfigurationMetadata {
	private static final Logger log = Logger.getLogger(XmlMetadataProvider.class);
	private File xmlFile;

	public XmlMetadataProvider(File file) {
		this.xmlFile = file;
	}

	public Group[] load() throws IOException {
		if (!xmlFile.exists()) {
			log.error("Missing Configuration File: " + xmlFile.getAbsolutePath());
			return null;
		}

		log.debug("Loading Xml Metadata File: " + xmlFile.getAbsolutePath());
		InputStream is = null;
		try {
			is = new FileInputStream(xmlFile);
			return new Group[] { XmlMetadataParser.parse(is) };
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public void save() throws IOException {
		log.debug("Saving Xml Metadata Not Implementd; File: " + xmlFile.getAbsolutePath());
	}

	public String toString() {
		return "XmlMetadataProvider[" + xmlFile.getAbsolutePath() + "]";
	}
}
