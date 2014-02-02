package sagex.phoenix.metadata.provider.dvdprofiler;

import sagex.phoenix.configuration.Config;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.Converter;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "DVD Profiler", path = "phoenix/metadata/dvdprofiler", description = "Configuration for the DVD Profiler provider")
public class DVDProfilerConfiguration extends GroupProxy {
	@AField(label = "DVD Profiler Image Dir", description = "Local DVD Profiler image directory", type = ConfigType.DIRECTORY)
	private FieldProxy<String> imageDir = new FieldProxy<String>(null, Converter.TEXT);

	@AField(label = "DVD Profiler Xml", description = "Local DVD Profiler xml file", type = ConfigType.FILE)
	private FieldProxy<String> xmlFile = new FieldProxy<String>(null, Converter.TEXT);

	@AField(label = "DVD Profiler Xml last modified date/time", description = "Data/Time the xml file was modified as a long value.  Should not be set directly.", visible = "false", hints = Config.Hint.DATETIME)
	private FieldProxy<Long> xmlFileLastModified = new FieldProxy<Long>(0l);

	public DVDProfilerConfiguration() {
		super();
		init();
	}

	public String getImageDir() {
		return imageDir.get();
	}

	public void setImageDir(String imageDir) {
		this.imageDir.set(imageDir);
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