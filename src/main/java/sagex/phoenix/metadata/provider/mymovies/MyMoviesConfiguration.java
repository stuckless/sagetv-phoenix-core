package sagex.phoenix.metadata.provider.mymovies;

import sagex.phoenix.configuration.Config;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.Converter;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "MyMovies", path = "phoenix/metadata/mymovies", description = "Configuration for MyMovies metadata provider")
public class MyMoviesConfiguration extends GroupProxy {
	@AField(label = "MyMovies Collection Xml", description = "MyMovies xml file", type = ConfigType.FILE)
	private FieldProxy<String> xmlFile = new FieldProxy<String>(null, Converter.TEXT);

	@AField(label = "MyMovies Index last modified date/time", description = "Data/Time the xml file was modified as a long value.  Should not be set directly.", visible = "prop:server:phoenix/core/enableAdvancedOptions", hints = Config.Hint.DATETIME)
	private FieldProxy<Long> xmlFileLastModified = new FieldProxy<Long>(0l);

	public MyMoviesConfiguration() {
		super();
		init();
	}

	public String getXmlFile() {
		return xmlFile.get();
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile.set(xmlFile);
	}

	public long getXmlFileLastModified() {
		return xmlFileLastModified.get();
	}

	public void setXmlFileLastModified(long xmlFileLastModified) {
		this.xmlFileLastModified.set(xmlFileLastModified);
	}
}
