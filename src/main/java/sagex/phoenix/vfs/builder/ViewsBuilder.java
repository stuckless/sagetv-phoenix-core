package sagex.phoenix.vfs.builder;

import java.util.Comparator;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.factory.IConfigurable;
import sagex.phoenix.util.PublicCloneable;
import sagex.phoenix.util.XmlUtil;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.groups.GroupingFactory;
import sagex.phoenix.vfs.groups.IGrouper;
import sagex.phoenix.vfs.sorters.Sorter;
import sagex.phoenix.vfs.sorters.SorterFactory;
import sagex.phoenix.vfs.sources.SageSourcesFactory;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewPresentation;

public class ViewsBuilder extends VFSManagerBuilder {
    private ViewFactory view = null;
    private ViewPresentation presentation = null;

    // static of configurable objects
    // private IConfigurable configurable = null;
    private Stack<IConfigurable> configurables = new Stack<IConfigurable>();

    private ConfigurableOption option = null;

    public ViewsBuilder(VFSManager mgr) {
        super(mgr);
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        clearData();

        if (name.equals("views")) {
        } else if (name.equals("view")) {
            view = new ViewFactory();
            IConfigurable configurable = view;
            configurables.push(configurable);
            addOption(attributes, Factory.OPT_NAME, configurable);
            addOption(attributes, Factory.OPT_LABEL, configurable);
            addOption(attributes, Factory.OPT_VISIBLE, configurable);
            addOption(attributes, ViewFactory.OPT_FLAT, configurable);
            addOption(attributes, ViewFactory.OPT_PRUNE_SINGLE_ITEM_FOLDERS, configurable);
        } else if (name.equals("description")) {
        } else if (name.equals("tag")) {
            view.addTag(XmlUtil.attr(attributes, "value"));
            manager.addTag(XmlUtil.attr(attributes, "value"), null, null);
        } else if (name.equals("hint")) {
            presentation.getHints().add(XmlUtil.attr(attributes, "value"));
        } else if (name.equals("option")) {
            option = new ConfigurableOption(XmlUtil.attr(attributes, "name"), XmlUtil.attr(attributes, "value"));
        } else if (name.equals("source")) {
            PublicCloneable source = manager.getVFSSourceFactory().getFactory(XmlUtil.attr(attributes, "name"));
            if (source == null) {
                error("unknown source: " + XmlUtil.attr(attributes, "name"));
                view.setHasErrors(true);
                view.setErrorMessage("Missing View Source: " + XmlUtil.attr(attributes, "name"));
                configurables.push(new SageSourcesFactory());
            } else {
                try {
                    configurables.push((IConfigurable) source.clone());
                } catch (Exception e) {
                    error("Unable to add source to view " + XmlUtil.attr(attributes, "name"), e);
                }
            }
        } else if (name.equals("view-source")) {
            PublicCloneable source = manager.getVFSViewFactory().getFactory(XmlUtil.attr(attributes, "name"));
            if (source == null) {
                error("unknown source: " + XmlUtil.attr(attributes, "name"));
                view.setHasErrors(true);
                view.setErrorMessage("Missing View: " + XmlUtil.attr(attributes, "name"));
                configurables.push(new ViewFactory());
            } else {
                try {
                    configurables.push((IConfigurable) source.clone());
                } catch (Exception e) {
                    error("Unable to add view source to view " + XmlUtil.attr(attributes, "name"), e);
                }
            }
        } else if (name.equals("presentation")) {
            presentation = new ViewPresentation();
            String lev = XmlUtil.attr(attributes, "level");
            if (StringUtils.isEmpty(lev)) {
                presentation.setLevel(view.getViewPresentations().size());
            } else {
                presentation.setLevel(NumberUtils.toInt(lev, 1) - 1);
            }
        } else if (name.equals("group")) {
            GroupingFactory f = manager.getVFSGroupFactory().getFactory(XmlUtil.attr(attributes, "by"));
            if (f == null) {
                error("unknown grouper by: " + XmlUtil.attr(attributes, "by"));
                view.setHasErrors(true);
                view.setErrorMessage("Missing Groups: " + XmlUtil.attr(attributes, "by"));
                configurables.push(new GroupingFactory((IGrouper)null));
            } else {
                configurables.push(f.create(null));
            }
        } else if (name.equals("filter")) {
            FilterFactory f = manager.getVFSFilterFactory().getFactory(XmlUtil.attr(attributes, "by"));
            if (f == null) {
                error("unknown filter by: " + XmlUtil.attr(attributes, "by"));
                view.setHasErrors(true);
                view.setErrorMessage("Missing Filter: " + XmlUtil.attr(attributes, "by"));
                configurables.push(new FilterFactory());
            } else {
                IConfigurable configurable = f.create(null);
                if (configurable == null)
                    error("failed to create filter for " + XmlUtil.attr(attributes, "by"));
                addOption(attributes, Filter.OPT_VALUE, configurable);
                addOption(attributes, Filter.OPT_SCOPE, configurable);
                configurables.push(configurable);
            }
        } else if (name.equals("sort")) {
            SorterFactory f = manager.getVFSSortFactory().getFactory(XmlUtil.attr(attributes, "by"));
            if (f == null) {
                error("unknown sort by: " + XmlUtil.attr(attributes, "by"));
                view.setHasErrors(true);
                view.setErrorMessage("Missing Sort: " + XmlUtil.attr(attributes, "by"));
                configurables.push(new SorterFactory((Comparator<IMediaResource>) null));
            } else {
                IConfigurable configurable = f.create(null);
                addOption(attributes, Sorter.OPT_SORT_ORDER, configurable);
                addOption(attributes, Sorter.OPT_FOLDERS_FIRST, configurable);
                configurables.push(configurable);
            }
        } else {
            error("Unknown View Tag: " + name);
        }
    }

    private void addOption(Attributes attributes, String optName, IConfigurable configurable2) throws SAXException {
        if (configurable2 == null)
            error("can't add option to null configurable item");
        String value = XmlUtil.attr(attributes, optName);

        if (StringUtils.isEmpty(value)) {
            // nothing to set
            return;
        }

        if (configurable2.getOption(optName) == null) {
            // nothing to configure
            return;
        }

        configurable2.getOption(optName).value().set(value);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals("view")) {
            if (view.getFolderSources().size() == 0 && view.getViewSources().size() == 0) {
                error("Can't add a view with no sources! view: " + view.getName());
            }
            manager.getVFSViewFactory().addFactory(view);
            configurables.pop();

            getLog().info("Loaded View: " + view.getName());

            reset();
        } else if (name.equals("description")) {
            view.getOption(Factory.OPT_DESCRIPTION).value().set(getData());
        } else if (name.equals("option")) {
            if (!StringUtils.isEmpty(getData())) {
                option.value().set(getData());
            }
            configurables.peek().addOption(option);
            option = null;
        } else if (name.equals("source")) {
            view.addFolderSource((Factory<IMediaFolder>) configurables.pop());
        } else if (name.equals("view-source")) {
            view.addViewSource((ViewFactory) configurables.pop());
        } else if (name.equals("presentation")) {
            view.addViewPresentations(presentation);
            presentation = null;
        } else if (name.equals("group")) {
            presentation.getGroupers().add((Grouper) configurables.pop());
        } else if (name.equals("filter")) {
            if (presentation == null) {
                // filter at the view level
                view.addRootFilter((Filter) configurables.pop());
            } else {
                // filter at the presentation level
                presentation.getFilters().add((Filter) configurables.pop());
            }
        } else if (name.equals("sort")) {
            presentation.getSorters().add((Sorter) configurables.pop());
        }
    }

    private void reset() {
        view = null;
        presentation = null;
        configurables = new Stack<IConfigurable>();
        option = null;
    }
}
