package sagex.phoenix.vfs.ov;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.ov.XmlOptions.XmlMetadata;

public class XmlHandler extends BaseBuilder {
    private XmlFolder folder = null;
    private XmlOptions options = null;
    private List<IMediaResource> resources = null;
    private XmlFile file = null;
    StringBuilder sdata = new StringBuilder();

    public XmlHandler(String name, XmlFolder folder, List<IMediaResource> resources) {
        super(name);
        this.folder = folder;
        this.resources = resources;
        this.options = folder.getOptions();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        sdata = new StringBuilder();
        if (qName.equals(options.getItemElement())) {
            file = folder.createFile();
            return;
        }

        Set<XmlMetadata> mdSet = options.getMetadataKeysForElement(qName);
        if (file != null && mdSet != null) {
            for (XmlMetadata md : mdSet) {
                if (md.XmlAttribute != null) {
                    updateMetadata(file, md, XmlUtil.attr(attributes, md.XmlAttribute));
                }
            }
        }
    }

    private void updateMetadata(IMediaFile file, XmlMetadata md, String val) {
        if (val == null)
            return;

        Pattern regex = options.getRegex(md.MetadataKey);
        if (regex != null) {
            Matcher m = regex.matcher(val);
            if (m.find()) {
                if (!StringUtils.isEmpty(m.group(1))) {
                    val = m.group(1);
                }
            }
        }
        if (val != null) {
            val = val.trim();
            if (md.MetadataKey.equals(FieldName.Duration)) {
                long v = DateUtils.parseDuration(val);
                val = String.valueOf(v);
            }
        }

        file.getMetadata().set(MetadataUtil.getSageProperty(md.MetadataKey), val);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(options.getItemElement())) {
            file.updateIds();
            resources.add(file);
            file = null;
            return;
        }

        Set<XmlMetadata> mdSet = options.getMetadataKeysForElement(qName);
        if (file != null && mdSet != null) {
            for (XmlMetadata md : mdSet) {
                if (md.XmlAttribute == null) {
                    // pull the data from the element
                    updateMetadata(file, md, sdata.toString().trim());
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        sdata.append(getData());
    }
}
