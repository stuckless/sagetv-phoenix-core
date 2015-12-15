package sagex.phoenix.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sagex.phoenix.configuration.proxy.GroupParser;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.XmlUtil;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class XmlMetadataParser extends BaseBuilder {
    private SAXParserFactory parserFactory = SAXParserFactory.newInstance();

    public XmlMetadataParser() {
        super();
    }

    private Group app = new Group();
    private Group g = app;
    private Field el = null;

    private enum DescriptionState {
        NONE, READ_GROUP, READ_FIELD, READ_APPLICATION
    }

    ;

    private DescriptionState descState = DescriptionState.NONE;

    public Group parseMetadata(InputStream xml) throws Exception {
        app.setElementType(IConfigurationElement.APPLICATION);
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(xml, this);

        return app;
    }

    public static Group parse(InputStream xml) throws Exception {
        XmlMetadataParser parser = new XmlMetadataParser();
        return parser.parseMetadata(xml);
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (name.equals("configuration")) {
            // this is a unique application id... each configuration metadata
            // must tell us to which application it belongs
            app.setId(attributes.getValue("id"));
            if (app.getId() == null)
                throw new SAXException("configuration element MUST have an id");
            app.setLabel(attributes.getValue("label"));
            descState = DescriptionState.READ_APPLICATION;
            log.debug("Using Application Id: " + app.getId());
            return;
        } else if (name.equals("group")) {
            if (!StringUtils.isEmpty(attributes.getValue("class"))) {
                try {
                    // if the <group> has a class attribute then create the
                    // group using the GroupParser from classes.
                    g.addElement(GroupParser.parseGroup(Class.forName(attributes.getValue("class"))));
                    log.debug("Added Metadata Group: " + g.getId() + " From Class: " + attributes.getValue("class"));
                } catch (Throwable t) {
                    log.error("Failed to Create a Group from Class: " + attributes.getValue("class"), t);
                }
                return;
            }

            Group gr = new Group();
            // group paths are optional
            // if the group has a key/id/path then append that to all of its
            // elements.
            if (g == null || g.getId() == null || g.getElementType() == IConfigurationElement.APPLICATION) {
                gr.setId(attributes.getValue("path"));
            } else {
                gr.setId(g.getId() + "/" + attributes.getValue("path"));
            }

            gr.setLabel(attributes.getValue("label"));
            gr.setIsVisible(XmlUtil.attr(attributes, "visible", "true"));

            String hints = XmlUtil.attr(attributes, "hints", null);
            if (hints != null) {
                String hts[] = hints.split("\\s*,\\s*");
                for (String h : hts) {
                    gr.getHints().setBooleanHint(h, true);
                }
            }

            if (g != null)
                g.addElement(gr);
            g = gr;
            descState = DescriptionState.READ_GROUP;
            log.debug("Added Metadata Group: " + g.getId() + "; " + g.getLabel());
            return;
        } else if (name.equals("field")) {
            el = new Field();
            // if the group has a key/id/path then append that to all of its
            // elements.
            if (attributes.getValue("key") == null && attributes.getValue("fullkey") == null) {
                throw new SAXException("Missing Field Key for Group: " + g.getId() + "; Application: " + app.getId());
            }

            if (attributes.getValue("fullkey") != null) {
                el.setId(attributes.getValue("fullkey"));
            } else {
                if (g.getId() != null) {
                    el.setId(g.getId() + "/" + attributes.getValue("key"));
                } else {
                    el.setId(attributes.getValue("key"));
                }
            }

            el.setLabel(attributes.getValue("label"));
            el.setDefaultValue(attributes.getValue("defaultValue"));
            el.setIsVisible(XmlUtil.attr(attributes, "visible", "true"));
            el.setListSeparator(attributes.getValue("listSeparator"));
            el.setType(ConfigType.toConfigType(XmlUtil.attr(attributes, "type", ConfigType.TEXT.name())));
            String hints = XmlUtil.attr(attributes, "hints", null);
            if (hints != null) {
                String hts[] = hints.split("\\s*,\\s*");
                for (String h : hts) {
                    el.getHints().setBooleanHint(h, true);
                }
            }
            el.setScope(ConfigScope.toConfigScope(attributes.getValue("scope")));
            g.addElement(el);
            descState = descState.READ_FIELD;
            log.debug("Added Metadata Field: " + el.getId());
        } else if ("options".equals(name)) {
            String expr = XmlUtil.attr(attributes, "expression", null);
            if (!StringUtils.isEmpty(expr)) {
                el.setOptionFactory(new SageExpressionOptionFactory(expr));
            } else {
                String className = XmlUtil.attr(attributes, "class", StaticOptionsFactory.class.getName());
                try {
                    IOptionFactory fact = (IOptionFactory) Class.forName(className).newInstance();
                    el.setOptionFactory(fact);
                } catch (InstantiationException e) {
                    error("Can't create Options Class " + className);
                } catch (IllegalAccessException e) {
                    error("Can't access Options Class " + className);
                } catch (ClassNotFoundException e) {
                    error("Options Class not found " + className);
                }

                // allow ranges
                String range = XmlUtil.attr(attributes, "range", null);
                if (range != null) {
                    String ranges[] = range.split("\\s*-\\s*");
                    if (ranges.length != 2) {
                        error("Invalid 'range' attribute value " + range);
                    } else {
                        int r1 = NumberUtils.toInt(ranges[0]);
                        int r2 = NumberUtils.toInt(ranges[1]);
                        for (int i = r1; i <= r2; i++) {
                            ((StaticOptionsFactory) el.getOptionFactory()).addOption(String.valueOf(i), String.valueOf(i));
                        }
                    }
                }
            }
            // if the element type is text, then change to choice, since
            // options are passed.
            if (el.getType() == ConfigType.TEXT) {
                // if we options and list separator then for the type to
                // multichoice
                // otherwise set to choice
                // is options force us to be either single or multi choice
                // fields
                if (!StringUtils.isEmpty(el.getListSeparator())) {
                    el.setType(ConfigType.MULTICHOICE);
                } else {
                    el.setType(ConfigType.CHOICE);
                }
            }
        } else if ("option".equals(name)) {
            IOptionFactory fact = el.getOptionFactory();
            if (fact == null) {
                fact = new StaticOptionsFactory();
                el.setOptionFactory(fact);
            }
            if (!(fact instanceof StaticOptionsFactory)) {
                error("Can't add options to factory " + fact.getClass().getName());
            }
            ((StaticOptionsFactory) fact).addOption(XmlUtil.attr(attributes, "name", null),
                    XmlUtil.attr(attributes, "value", XmlUtil.attr(attributes, "name", null)));
        } else if ("eval".equals(name)) {
            // do nothing
        } else if ("description".equals(name)) {
            // do nothing
        } else {
            error("Unknown Configuration Element " + name);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals("group")) {
            if (g.getParent() != null) {
                g = (Group) g.getParent();
                log.debug("Reset To Group: " + g.getId());
            }
        } else if (name.equals("description")) {
            String data = getData();
            if (descState == DescriptionState.READ_GROUP) {
                g.setDescription(data);
            } else if (descState == DescriptionState.READ_FIELD) {
                el.setDescription(data);
            } else if (descState == DescriptionState.READ_APPLICATION) {
                app.setDescription(data);
            }
            descState = DescriptionState.NONE;
        } else if (name.equals("option")) {
            String val = getData();
            if (val != null && el != null && el.getOptionFactory() instanceof StaticOptionsFactory) {
                val = val.trim();
                if (val.length() > 0 && el.getOptions().size() > 0) {
                    // update the last option with this value
                    el.getOptions().get(el.getOptions().size() - 1).setValue(data);
                }
            }
        } else if ("eval".equals(name)) {
            // TODO: handle eval button actions
        }
    }
}
