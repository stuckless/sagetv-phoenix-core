package sagex.phoenix.vfs.builder;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.filters.*;

import java.util.Set;
import java.util.TreeSet;

public class FiltersBuilder extends FactoryItemBuilder<FilterFactory> {
    private String groupName, groupLabel, groupVisible;
    private AbstractResourceFilterContainer groupedFilter = null;
    private FilterFactory filterRef = null;
    private Set<ConfigurableOption> opts = null;
    private ConfigurableOption opt = null;

    public FiltersBuilder(VFSManager mgr) {
        super(mgr, "filters");
    }

    @Override
    protected FilterFactory createFactory(String className) throws SAXException {
        try {
            return new FilterFactory(className);
        } catch (InstantiationException e) {
            error("Failed to create factory for  " + className, e);
        } catch (IllegalAccessException e) {
            error("Failed to access factory for  " + className, e);
        } catch (ClassNotFoundException e) {
            error("Failed to find class instance for factory for  " + className, e);
        }

        return null;
    }

    @Override
    protected void factoryConfigured(FilterFactory factory) {
        manager.getVFSFilterFactory().addFactory(factory);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("item-group".equals(name)) {
            FilterFactory factory = new FilterFactory(groupedFilter);
            factory.getOption(Factory.OPT_NAME).value().set(groupName);
            factory.getOption(Factory.OPT_LABEL).value().set(groupLabel);
            factory.getOption(Factory.OPT_VISIBLE).value().set(groupVisible);
            factoryConfigured(factory);
            groupedFilter = null;
        } else if ("filter".equals(name)) {
            groupedFilter.addFilter(filterRef.create(opts));
            filterRef = null;
            opts = null;
        } else if ("option".equals(name)) {
            if (filterRef != null) {
                String data = getData();
                if (!StringUtils.isEmpty(data)) {
                    opt.value().setValue(data);
                }
                opts.add(opt);
            }
        } else {
            super.endElement(uri, localName, name);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        clearData();

        // need to handle option/end option and set the option within the filter
        // for a filter group

        if ("item-group".equals(name)) {
            if ("or".equals(XmlUtil.attr(attributes, "mode", "or"))) {
                groupedFilter = new OrResourceFilter();
            } else {
                groupedFilter = new AndResourceFilter();
            }
            groupName = XmlUtil.attr(attributes, "name");
            groupLabel = XmlUtil.attr(attributes, "label");
            groupVisible = XmlUtil.attr(attributes, "visible");
        } else if ("filter".equals(name)) {
            filterRef = (FilterFactory) manager.getVFSFilterFactory().getFactory(XmlUtil.attr(attributes, "by"));
            if (filterRef == null) {
                error("filters:item-group: unknown factory: " + XmlUtil.attr(attributes, "by"));
            }
            String value = XmlUtil.attr(attributes, "value");
            opts = new TreeSet<ConfigurableOption>();
            if (!StringUtils.isEmpty(value)) {
                opts.add(new ConfigurableOption(Filter.OPT_VALUE, value));
            }
            String scope = XmlUtil.attr(attributes, "scope");
            if (!StringUtils.isEmpty(scope)) {
                opts.add(new ConfigurableOption(Filter.OPT_SCOPE, scope));
            }
            if (groupedFilter == null) {
                error("filters:filters: apears to be missing grouped filter??");
            }
        } else if ("option".equals(name)) {
            if (filterRef != null) {
                opt = new ConfigurableOption(XmlUtil.attr(attributes, "name"));
                opt.value().set(XmlUtil.attr(attributes, "value"));
            } else {
                super.startElement(uri, localName, name, attributes);
            }
        } else {
            super.startElement(uri, localName, name, attributes);
        }
    }
}
