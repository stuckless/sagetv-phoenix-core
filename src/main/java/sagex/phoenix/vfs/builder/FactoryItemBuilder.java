package sagex.phoenix.vfs.builder;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.vfs.VFSManager;

public abstract class FactoryItemBuilder<T extends Factory<?>> extends VFSManagerBuilder {
    private String itemName, label, className, visible = null;

    private T factory = null;
    private ConfigurableOption option = null;
    private String factoryItemTag;

    public FactoryItemBuilder(VFSManager mgr, String factoryItemTag) {
        super(mgr);
        this.factoryItemTag = factoryItemTag;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        clearData();

        if ("item".equals(name)) {
            itemName = (XmlUtil.attr(attributes, "name", null));
            label = (XmlUtil.attr(attributes, "label", null));
            visible = (XmlUtil.attr(attributes, "visible", "true"));
            className = (XmlUtil.attr(attributes, "class", null));

            // factory = (Factory<?>) Class.forName(className).newInstance();
            factory = createFactory(className);
            if (factory==null) {
                error("Missing Factory for " + className);
            } else {
                factory.getOption(Factory.OPT_NAME).value().set(itemName);
                factory.getOption(Factory.OPT_LABEL).value().set(label);
                factory.getOption(Factory.OPT_VISIBLE).value().set(visible);
            }
        } else if ("description".equals(name)) {
        } else if ("tag".equals(name)) {
            String tag = XmlUtil.attr(attributes, "value", null);
            String desc = XmlUtil.attr(attributes, "label", null);
            String visible = XmlUtil.attr(attributes, "visible", null);

            if (factory != null) {
                // tag the factory
                factory.addTag(tag);
            }

            // add to the known tags
            manager.addTag(tag, desc, visible);
        } else if ("option".equals(name)) {
            option = new ConfigurableOption(XmlUtil.attr(attributes, "name"));
            option.value().set(XmlUtil.attr(attributes, "value"));
        } else if (factoryItemTag.equals(name)) {
            // do nothing, just here to stop the error
        } else {
            warning("unhandled factory item tag: " + name);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("item".equals(name)) {
            factoryConfigured(factory);
            reset();
        } else if ("description".equals(name)) {
            if (!StringUtils.isEmpty(getData())) {
                option.value().set(getData());
            }
            factory.updateOption(option);
        } else if ("description".equals(name)) {
            factory.getOption(Factory.OPT_DESCRIPTION).value().set(getData());
        }
    }

    private void reset() {
        itemName = label = className = visible = null;
    }

    protected abstract void factoryConfigured(T factory);

    protected abstract T createFactory(String className) throws SAXException;
}
