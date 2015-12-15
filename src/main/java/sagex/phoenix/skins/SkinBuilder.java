package sagex.phoenix.skins;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sagex.phoenix.util.XmlUtil;

public class SkinBuilder extends DefaultHandler {
    private static final Logger log = Logger.getLogger(SkinBuilder.class);

    public static Skin buildPlugin(File pluginDir) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();

        Skin plugin = new Skin(pluginDir);
        SkinBuilder builder = new SkinBuilder(plugin);

        File xml = plugin.getResource("skin.xml");
        if (xml.exists()) {
            parser.parse(xml, builder);
        } else {
            log.info("No Skin declaration file for Skin: " + pluginDir);
            // create a plugin using just the directory entry
            plugin.setVersion("1.0");
            plugin.setName(pluginDir.getName());
            plugin.setId(pluginDir.getName());
            plugin.setDescription(pluginDir.getAbsolutePath());
        }

        return plugin;
    }

    private StringBuilder data = new StringBuilder();
    private Skin plugin = null;

    public SkinBuilder(Skin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        data = new StringBuilder();
        if ("plugin".equals(name)) {
            plugin.setId(XmlUtil.attr(attributes, "id", null));
            plugin.setName(XmlUtil.attr(attributes, "name", null));
            plugin.setVersion(XmlUtil.attr(attributes, "version", "1.0"));
            return;
        }

        if ("depend".equals(name)) {
            plugin.addDependency(XmlUtil.attr(attributes, "id", null));
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("description".equals(name)) {
            plugin.setDescription(getData());
            return;
        }
    }

    public ISkin getSkin() {
        return plugin;
    }

    private String getData() {
        String s = data.toString();
        if (s == null)
            return null;
        return s.trim();
    }

}
