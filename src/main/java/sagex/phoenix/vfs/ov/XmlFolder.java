package sagex.phoenix.vfs.ov;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.util.DOMUtils;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.Url;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualOnlineMediaFolder;

public class XmlFolder extends VirtualOnlineMediaFolder {
	private XmlOptions options = new XmlOptions();

	public XmlFolder(IMediaFolder parent, String title, String feedurl) {
		super(parent, feedurl, feedurl, title, false);
		this.options = new XmlOptions();
		options.setFeedUrl(feedurl);
		log.info("Processing Feed: " + feedurl);
	}

	public XmlFolder(IMediaFolder parent, String title, XmlOptions options) {
		super(parent, options.getFeedUrl(), options.getFeedUrl(), title, false);
		this.options = options;
	}

	@Override
	protected void populateChildren(List<IMediaResource> children) {
		IUrl u = new Url(options.getFeedUrl());
		try {
			DOMUtils.parseXml(u, new XmlHandler(options.getFeedUrl(), this, children));
		} catch (IOException e) {
			log.warn("IO Error: Failed to parse xml " + options.getFeedUrl());
		} catch (SAXException e) {
			log.warn("Xml Error: Failed to parse xml " + options.getFeedUrl());
		}
	}

	public XmlOptions getOptions() {
		return options;
	}

	public XmlFile createFile() {
		XmlFile f = new XmlFile(this);
		if (!StringUtils.isEmpty(options.getMediaType())) {
			f.getMetadata().set(MetadataUtil.getSageProperty(FieldName.MediaType), options.getMediaType());
		}
		return f;
	}
}
