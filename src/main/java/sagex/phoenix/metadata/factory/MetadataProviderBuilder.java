package sagex.phoenix.metadata.factory;

import java.lang.reflect.Constructor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataProviderInfo;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.XmlUtil;

public class MetadataProviderBuilder extends BaseBuilder {
	private MetadataProviderInfo info = null;
	private IMetadataProvider provider = null;

	public MetadataProviderBuilder(String name) {
		super(name);
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if ("metadata".equals(name)) {
			info = new MetadataProviderInfo();
			info.setId(XmlUtil.attr(attributes, "id", null));
		} else if ("class".equals(name)) {
		} else if ("name".equals(name)) {
		} else if ("description".equals(name)) {
		} else if ("mediatype".equals(name)) {
		} else if ("icon".equals(name)) {
		} else if ("fanart".equals(name)) {
		} else {
			warning("Unhandled Metadata Provider Tag: " + name);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if ("metadata".equals(name)) {
			if (provider == null) {
				error("Failed to parse a valid metadata provider!");
			}
		} else if ("class".equals(name)) {
			try {
				Class<IMetadataProvider> cl = (Class<IMetadataProvider>) Class.forName(getData());
				Constructor<IMetadataProvider> con = cl.getConstructor(IMetadataProviderInfo.class);
				provider = con.newInstance(info);
			} catch (Exception t) {
				error("Failed to create Metadata Provider: " + getData(), t);
			}
		} else if ("name".equals(name)) {
			info.setName(getData());
		} else if ("description".equals(name)) {
			info.setDescription(getData());
		} else if ("mediatype".equals(name)) {
			MediaType mt = MediaType.toMediaType(getData());
			if (mt == null) {
				error("Invalid Media Type: " + getData());
			}
			info.getSupportedSearchTypes().add(mt);
		} else if ("icon".equals(name)) {
			info.setIconUrl(getData());
		} else if ("fanart".equals(name)) {
			info.setFanartProviderId(getData());
		} else {
			warning("Unhandled Metadata Provider Tag: " + name);
		}
	}

	public IMetadataProvider getProvider() {
		return provider;
	}
}
