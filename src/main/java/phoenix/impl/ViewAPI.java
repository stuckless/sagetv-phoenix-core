package phoenix.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.db.UserRecordUtil;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.factory.IConfigurable;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.groups.GroupingFactory;
import sagex.phoenix.vfs.sorters.Sorter;
import sagex.phoenix.vfs.sorters.SorterFactory;
import sagex.phoenix.vfs.views.OnlineViewFolder;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;

/**
 * View Management APIs.
 *
 * @author seans
 */
@API(group = "umb")
public class ViewAPI {
    private Logger log = Logger.getLogger(ViewAPI.class);

    /**
     * Gets the ViewFactories that are available for the given tag. If
     * includeInvisble is true then both hidden and visible views will be
     * returned
     *
     * @param tag
     * @param includeInvisible
     * @return
     */
    public Set<ViewFactory> GetViewFactories(String tag, boolean includeInvisible) {
        Set<ViewFactory> set = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories(tag, includeInvisible);
        updateVisibilityStatus(set);
        if (!includeInvisible) {
            set = removeHiddenViews(set);
        }
        return set;
    }

    private Set<ViewFactory> removeHiddenViews(Collection<ViewFactory> set) {
        Set<ViewFactory> ts = new HashSet<ViewFactory>();
        if (set != null) {
            for (ViewFactory f : set) {
                if (IsVisibleView(f)) {
                    ts.add(f);
                }
            }
        }
        return ts;
    }

    /**
     * Returns only the visible view factories for the given tag
     *
     * @param tag
     * @return
     */
    public Set<ViewFactory> GetViewFactories(String tag) {
        Set<ViewFactory> set = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories(tag);
        set = removeHiddenViews(set);
        return set;
    }

    /**
     * Returns factories with errors
     *
     * @return
     */
    public List<ViewFactory> GetViewFactoriesWithErrors() {
        return Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactoriesWithErrors();
    }


    private void updateVisibilityStatus(Collection<ViewFactory> factories) {
        if (factories == null)
            return;
        for (ViewFactory v : factories) {
            v.setVisible(IsVisibleView(v));
        }
    }

    /**
     * Return all visible view factories
     *
     * @return
     */
    public List<ViewFactory> GetViewFactories() {
        // TODO: Change GetViewFactories to return Set
        return new ArrayList(GetVisibleViews());
    }

    /**
     * Returns all visible view factories
     *
     * @return
     */
    public Set<ViewFactory> GetVisibleViews() {
        List<ViewFactory> f = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories(true);
        Set<ViewFactory> visible = removeHiddenViews(f);
        return visible;
    }

    /**
     * Returns all hidden view factories
     *
     * @return
     */
    public Set<ViewFactory> GetHiddenViews() {
        List<ViewFactory> set = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories(true);
        Set<ViewFactory> ts = new HashSet<ViewFactory>();
        if (set != null) {
            for (ViewFactory f : set) {
                if (IsVisibleView(f)) {
                } else {
                    ts.add(f);
                }
            }
        }
        return ts;
    }

    /**
     * Creates a View for the given view name
     *
     * @param name
     * @return
     */
    public ViewFolder CreateView(String name) {
        ViewFactory vf = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactory(name);
        if (vf != null) {
            return (ViewFolder) Create(vf);
        }
        log.warn("Invalid View: " + name);
        return null;
    }

    /**
     * Returns the list of configurable options for a given
     * view/sort/filter/grouper
     *
     * @param conf
     * @return
     */
    public List<ConfigurableOption> GetOptions(IConfigurable conf) {
        List<ConfigurableOption> opts = new ArrayList<ConfigurableOption>();
        for (String s : conf.getOptionNames()) {
            opts.add(conf.getOption(s));
        }
        return opts;
    }

    /**
     * @param factory
     * @return
     * @deprecated - use CreateSorter/Grouper/Filter
     */
    public Object Create(Factory factory) {
        return factory.create(null);
    }

    /**
     * Creates a Grouper for the given id or GrouperFactory object
     *
     * @param factory
     * @return
     */
    public Grouper CreateGrouper(Object factory) {
        Factory f = null;
        if (factory instanceof String) {
            f = Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactory((String) factory);
        } else {
            f = (Factory) factory;
        }

        if (f != null) {
            return (Grouper) f.create(null);
        }

        log.warn("Invalid Grouper Factory: " + factory);
        return null;
    }

    /**
     * Creates a Filter for the given id or {@link FilterFactory} object
     *
     * @param factory
     * @return
     */
    public Filter CreateFilter(Object factory) {
        Factory f = null;
        if (factory instanceof String) {
            f = Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactory((String) factory);
        } else {
            f = (Factory) factory;
        }

        if (f != null) {
            return (Filter) f.create(null);
        }

        log.warn("Invalid Filter Factory: " + factory);
        return null;
    }

    /**
     * Creates a Sorter for the given id or object
     *
     * @param factory
     * @return
     */
    public Sorter CreateSorter(Object factory) {
        Factory f = null;
        if (factory instanceof String) {
            f = Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactory((String) factory);
        } else {
            f = (Factory) factory;
        }

        if (f != null) {
            return (Sorter) f.create(null);
        }

        log.warn("Invalid Sorter Factory: " + factory);
        return null;
    }

    /**
     * Gets the name (id) for the configurable item (ie, used for
     * sorts/filters/groupers)
     *
     * @param conf
     * @return
     */
    public String GetName(IConfigurable conf) {
        if (conf == null)
            return null;

        if (conf instanceof HasName) {
            return ((HasName) conf).getName();
        }

        ConfigurableOption opt = conf.getOption(Factory.OPT_NAME);
        if (opt == null)
            return null;
        return (String) opt.value().get();
    }

    /**
     * Get the display label for the configurable item (ie, used for
     * sorts/filters/groupers)
     *
     * @param conf
     * @return
     */
    public String GetLabel(IConfigurable conf) {
        if (conf == null)
            return null;
        if (conf instanceof HasLabel) {
            return ((HasLabel) conf).getLabel();
        }
        ConfigurableOption opt = conf.getOption(Factory.OPT_LABEL);
        if (opt == null)
            return null;
        return (String) opt.value().get();
    }

    /**
     * returns true if the Configurable item is a visible (ie, should be shown
     * to the end user). Some configuation items should not be editable by the
     * end user.
     *
     * @param conf
     * @return
     */
    public boolean IsVisible(IConfigurable conf) {
        if (conf == null)
            return true;

        if (conf instanceof ViewFactory) {
            return IsVisibleView((ViewFactory) conf);
        } else {
            return conf.getOption(Factory.OPT_VISIBLE).getBoolean(true);
        }
    }

    /**
     * Gets a Configuration Option by name. For example if you have Sorter, you
     * can require the "sort-order" option to toggle it.
     *
     * @param conf
     * @param name
     * @return
     */
    public ConfigurableOption GetOption(IConfigurable conf, String name) {
        if (conf == null)
            return null;
        return conf.getOption(name);
    }

    /**
     * Notifies the Sorter/Filter/Grouper that it has been changed. You should
     * do this after changing values in a Sorter/Grouper/Filter so that the item
     * can update it's state.
     *
     * @param conf
     */
    public void SetChanged(IConfigurable conf) {
        if (conf == null)
            return;
        conf.setChanged(true);
    }

    /**
     * Notifies the View that it has been changed. You should do this after
     * chaning sort/filter/grouper values to allow the view to update itself
     * with the new information.
     *
     * @param view
     */
    public void SetChanged(ViewFolder view) {
        view.setChanged();
    }

    /**
     * Forces a view to refresh itself on the call to GetChildren. Normally
     * GetChildren() results are cached, so if you change the view state then
     * you should call refresh()
     *
     * @param view
     */
    public void Refresh(ViewFolder view) {
        view.refresh();
    }

    /**
     * Sets a filter on a view. If there is an existing filter with the same
     * name, then it is removed, and the new one is added.
     *
     * @param view
     * @param filter
     */
    public void SetFilter(ViewFolder view, Filter filter) {
        view.setFilter(filter);
    }

    /**
     * Removes a filter from the view
     *
     * @param view
     * @param filter
     */
    public void RemoveFilter(ViewFolder view, Filter filter) {
        view.removeFilter(filter);
    }

    /**
     * Get the filters that are currently attached to the view
     *
     * @param view
     * @return
     */
    public List<Filter> GetFilters(ViewFolder view) {
        return view.getFilters();
    }

    /**
     * Sets the grouper for a view. If a grouper with the same name exists, then
     * it is removed, and the view and the new one replaces it.
     *
     * @param view
     * @param grouper
     */
    public void SetGrouper(ViewFolder view, Grouper grouper) {
        view.setGrouper(grouper);
    }

    /**
     * Removes a grouper from the view
     *
     * @param view
     * @param grouper
     */
    public void RemoveGrouper(ViewFolder view, Grouper grouper) {
        view.removeGrouper(grouper);
    }

    /**
     * Gets the currently active groupers on the view
     *
     * @param view
     * @return
     */
    public List<Grouper> GetGroupers(ViewFolder view) {
        return view.getGroupers();
    }

    /**
     * Sets a sorter on the view. If a sorter with the same name is current on
     * the view, then it is replaced with this new one.
     *
     * @param view
     * @param sorter
     */
    public void SetSorter(ViewFolder view, Sorter sorter) {
        view.setSorter(sorter);
    }

    /**
     * Removes a sorter from the view.
     *
     * @param view
     * @param sorter
     */
    public void RemoveSorter(ViewFolder view, Sorter sorter) {
        view.removeSorter(sorter);
    }

    /**
     * Gets the currently active sorters on the view
     *
     * @param view
     * @return
     */
    public List<Sorter> GetSorters(ViewFolder view) {
        return view.getSorters();
    }

    /**
     * Gets the Filters that are available for this view. These are the Filters
     * that can be applied, but may not already by applied.
     *
     * @param view
     * @return
     */
    public Set<FilterFactory> GetAvailableFilters(ViewFolder view) {
        return Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories(view.getTags());
    }

    /**
     * Gets the Sorters that are available for this view. These are the Sorters
     * that can be applied, but may not already by applied.
     *
     * @param view
     * @return
     */
    public Set<SorterFactory> GetAvailableSorters(ViewFolder view) {
        return Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactories(view.getTags());
    }

    /**
     * Gets the Groupers that are available for this view. These are the
     * Groupers that can be applied, but may not already by applied.
     *
     * @param view
     * @return
     */
    public Set<GroupingFactory> GetAvailableGroupers(ViewFolder view) {
        return Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactories(view.getTags());
    }

    /**
     * Checks if a given view folder has a 'hint'. When configuring
     * 'presentation' levels you can set hints, like, 'series' that can later be
     * checked to see if a given view folder has that hint. In the case of a
     * 'series' hint, you may want to have the display fetch 'banners' since you
     * know the data in the view folder is 'series' data.
     *
     * @param hint
     * @param folder
     * @return
     */
    public boolean HasHint(String hint, ViewFolder folder) {
        return folder.hasHint(hint);
    }

    /**
     * Returns true if the given view have the specified tag. Tags differ from
     * hints, since hints can be different for each presentation level whereas
     * tags are applied globally. ie, a view factory that is tagged with 'tv'
     * will make the 'tv' tag available to children, but it cannot be changed at
     * each presentation level.
     *
     * @param tag
     * @param folder
     * @return
     */
    public boolean HasTag(String tag, ViewFolder folder) {
        return folder.hasTag(tag);
    }

    /**
     * Returns true if the given {@link IConfigurable}, ie, ({@link Sorter},
     * {@link Filter}, {@link Grouper}) has the specified tag.
     *
     * @param tag
     * @param configurable {@link IConfigurable} instance
     * @return
     */
    public boolean HasTag(IConfigurable configurable, String tag) {
        return configurable != null && tag != null && configurable.getTags().contains(tag);
    }

    /**
     * Creates a cloned copy of the given ViewFolder
     *
     * @param folder
     * @return
     */
    public ViewFolder CloneView(ViewFolder folder) {
        ViewFactory f = folder.getViewFactory();
        return f.create(null);
    }

    /**
     * Current not implemented :(
     *
     * @param folder
     * @param overwrite
     */
    public void SaveView(ViewFolder folder, boolean overwrite) {
        log.warn("SaveView() not implemented :(");
    }

    /**
     * Calls DeleteItem(res, true)
     *
     * @param res
     */
    public boolean DeleteItem(IMediaResource res) {
        return DeleteItem(res, true);
    }

    /**
     * Physically deletes a file and removes it from its parent.
     * <p/>
     * withoutPrejudice is a flag to tell sage tv that this item is being
     * deleted as a result of an incorrect recording, etc, so that intelligent
     * recordings can function correctly.
     *
     * @param res
     * @param withoutPrejudice
     */
    public boolean DeleteItem(IMediaResource res, boolean withoutPrejudice) {
        // physically delete the file
        boolean deleted = res.delete(new Hints(IMediaResource.HINT_DELETE_WITHOUT_PREJUDICE, String.valueOf(withoutPrejudice)));

        if (deleted) {
            // remove it from the parent files
            if (res.getParent() != null) {
                res.getParent().getChildren().remove(res);
            }
        }
        return deleted;
    }

    /**
     * Returns the named filter for the view. If the filter does not exist in
     * the current list of filters applied to the view, then null, is returned.
     *
     * @param name
     * @param view
     * @return
     */
    public Filter GetFilter(String name, ViewFolder view) {
        if (name == null || view == null)
            return null;
        List<Filter> filters = GetFilters(view);
        if (filters == null || filters.size() == 0)
            return null;
        for (Filter f : filters) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns the named sorter for the view. If the sorter does not exist in
     * the current list of sorters applied to the view, then null, is returned.
     *
     * @param name
     * @param view
     * @return
     */
    public Sorter GetSorter(String name, ViewFolder view) {
        if (name == null || view == null)
            return null;
        List<Sorter> list = GetSorters(view);
        if (list == null || list.size() == 0)
            return null;
        for (Sorter f : list) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /**
     * @param name
     * @param view
     * @return
     * @deprecated incorrect spelling use GetSorter()
     */
    @Deprecated
    public Sorter GetSoter(String name, ViewFolder view) {
        return GetSorter(name, view);
    }

    /**
     * Returns the named grouper for the view. If the grouper does not exist in
     * the current list of groupers applied to the view, then null, is returned.
     *
     * @param name
     * @param view
     * @return
     */
    public Grouper GetGrouper(String name, ViewFolder view) {
        if (name == null || view == null)
            return null;
        List<Grouper> list = GetGroupers(view);
        if (list == null || list.size() == 0)
            return null;
        for (Grouper f : list) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /**
     * returns the view factory associated with this folder.
     *
     * @param folder
     * @return
     */
    public ViewFactory GetViewFactory(ViewFolder folder) {
        return folder.getViewFactory();
    }

    /**
     * Returns true if the view hasn't been hidden.
     *
     * @param view
     * @return true if the visible is considered to be visible
     */
    public boolean IsVisibleView(ViewFactory view) {
        try {
            // log.warn(String.format("IsVisible(): %s: %s,%s", view.getName(),
            // view.isVisible(), UserRecordUtil.getBoolean(ViewFactory.STORE_ID,
            // view.getName(), ViewFactory.FIELD_VISIBLE, view.isVisible())));
            return UserRecordUtil.getBoolean(ViewFactory.STORE_ID, view.getName(), ViewFactory.FIELD_VISIBLE, view.isVisible());
        } catch (Throwable t) {
            log.warn("failed to test for view visibility", t);
            return true;
        }
    }

    /**
     * This sets/removes a hidden flag to views's UserRecord, to indicate that
     * the user is hiding the view.
     * <p/>
     * Use this API to set the hidden flag for the view outside the Xml.
     *
     * @param item
     */
    public void SetVisible(ViewFactory item, boolean vis) {
        UserRecordUtil.setField(ViewFactory.STORE_ID, item.getName(), ViewFactory.FIELD_VISIBLE, vis);
        item.setVisible(vis);
    }

    /**
     * Clears the Visibility Field on the view
     *
     * @param item
     */
    public void ResetVisible(ViewFactory item) {
        UserRecordUtil.clearField(ViewFactory.STORE_ID, item.getName(), ViewFactory.FIELD_VISIBLE);
    }

    /**
     * Returns true if the folder is loaded, or loads before the timeout
     * specified. If a the folder is loaded, then it returns immediately, but if
     * it is not loaded, then it will when it is loaded, but it won't wait
     * longer than the timeout.
     *
     * @param itemout
     */
    public boolean IsLoaded(IMediaFolder folder, int itemout) {
        if (folder instanceof OnlineViewFolder) {
            return ((OnlineViewFolder) folder).isLoaded(itemout);
        }
        return true;
    }

    /**
     * Returns true if the folder is an Online Media folder. The UI may want to
     * choose to display Online Folders differently, since they load
     * asychronously.
     *
     * @param folder
     * @return
     */
    public boolean IsOnlineFolder(IMediaFolder folder) {
        return folder instanceof OnlineViewFolder || folder.isType(MediaResourceType.ONLINE.value());
    }
}
