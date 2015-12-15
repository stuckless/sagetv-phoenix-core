package sagex.phoenix.vfs.views;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.util.ElapsedTimer;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.DecoratedMediaFolder;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.groups.GroupNameMediaFolder;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.sorters.Sorter;

import java.util.*;

public class ViewFolder extends DecoratedMediaFolder {
    protected Logger log = Logger.getLogger(this.getClass());

    private ViewFactory factory = null;
    private ViewFolder parent = null;
    private Set<String> tags = null;
    private ViewPresentation presentation = null;
    private int level = 0;

    public ViewFolder(ViewFactory factory, int level, ViewFolder parent, IMediaFolder decorate) {
        super(decorate);
        setParent(parent);
        this.factory = factory;
        this.level = level;
        if (this.factory == null) {
            // add in a dummy factory to hold the views, just in case we later
            // need to sort or group
            this.factory = new ViewFactory();
        }

        try {
            if ((!this.factory.hasViewPresentation(level)) && (parent != null && parent.presentation != null)) {
                // we don't have presentation for this level, so clone the
                // parent one
                presentation = (ViewPresentation) parent.presentation.clone();
                presentation.setLevel(level);

                // we don't bring forward the groupers, since it causes too many
                // issues
                presentation.getGroupers().clear();
                this.factory.addViewPresentations(presentation);
            } else {
                presentation = (ViewPresentation) this.factory.getViewPresentation(level);
            }
        } catch (CloneNotSupportedException e) {
            log.warn("Failed to create presentation for this view", e);
        }
    }

    public Set<String> getTags() {
        if (tags == null && parent != null) {
            tags = parent.getTags();
        }
        if (tags == null) {
            log.debug("Creating Tag Set for View: " + this);
            tags = new TreeSet<String>();
        }
        return tags;
    }

    protected void setParent(ViewFolder parent) {
        this.parent = parent;
    }

    protected List<IMediaResource> decorate(List<IMediaResource> originalChildren) {
        // TODO: If our children is a LazyList, then what?
        // ie, online videos are lazy, so we need to load a single "waiting"
        // item until the
        // the items are loaded. We also, need to ensure that we can reload this
        // list once all items
        // are loaded. We could add a "wait" here, but that defeats the purpose
        // of using a lazy list
        // the goal is only force a list to load once you actually get() the
        // first item

        try {
            String parentTitle = null;
            if (getParent() != null)
                parentTitle = getParent().getTitle();
            String title = getTitle();
            ElapsedTimer timer = new ElapsedTimer();
            Loggers.VFS_LOG.debug("Begin Decoring View Items for: " + title + "; in " + parentTitle + " Original Size: "
                    + originalChildren.size());

            if (Loggers.VFS_LOG.isDebugEnabled()) {
                StringBuffer sb = new StringBuffer();
                sb.append("\nBefore Decorating Folder: " + title + "; in " + parentTitle + "; Items: " + originalChildren.size()
                        + "\n");
                for (IMediaResource r : originalChildren) {
                    sb.append(" + " + r.getTitle());
                    if (r instanceof IMediaFolder)
                        sb.append(" (Folder)");
                    sb.append("\n");
                }
                sb.append("\n\n");
                Loggers.VFS_LOG.debug(sb);
            }

            List<IMediaResource> set = new ArrayList<IMediaResource>();
            Map<String, GroupNameMediaFolder> groups = new LinkedHashMap<String, GroupNameMediaFolder>();
            boolean grouping = presentation.hasGroupers();
            for (IMediaResource r : originalChildren) {

                if (set.contains(r)) {
                    Loggers.VFS_LOG.warn("Set already contains " + r + " so it will not be added again.");
                    continue;
                }

                if (presentation.canAccept(r)) {
                    if (r instanceof IMediaFolder) {
                        Loggers.VFS_LOG.debug("Adding subfolder: " + r.getTitle() + " to " + title);
                        set.add(newViewFolder(factory, level + 1, this, (IMediaFolder) r));
                    } else if (r instanceof IMediaFile) {
                        if (!grouping) {
                            Loggers.VFS_LOG.debug("Adding Non Grouped Item: " + r.getTitle() + " to " + title);
                            // if we are not grouping, the just add it
                            if (!set.add(newViewItem(this, (IMediaFile) getItem(r)))) {
                                log.warn("Failed to add duplicate item to the view; item: " + r + "; view: " + title);
                            }
                        } else {
                            boolean grouped = false;
                            List<String> groupNames = presentation.getGroupNames(r);
                            if (groupNames != null && groupNames.size() > 0) {
                                for (String groupName : groupNames) {
                                    if (!StringUtils.isEmpty(groupName)) {
                                        String groupKey = createGroupKey(groupName);
                                        GroupNameMediaFolder mf = groups.get(groupKey);
                                        if (mf == null) {
                                            Loggers.VFS_LOG.info("Creating Group Folder: " + groupName + " in " + title + " for "
                                                    + r.getTitle());
                                            mf = new GroupNameMediaFolder(this, groupName);
                                            groups.put(groupKey, mf);
                                        }

                                        grouped = true;
                                        Loggers.VFS_LOG.debug("Adding Resourse: " + r.getTitle() + " to group: " + groupName);
                                        mf.addMediaResource(r);
                                    }
                                }
                            }

                            if (!grouped) {
                                Loggers.VFS_LOG.warn("No Group Name for: " + r.getTitle() + "; Adding as a top level item");
                                // groupname return null, just add this as a
                                // toplevel item
                                set.add(newViewItem(this, (IMediaFile) getItem(r)));
                            }
                        }
                    } else {
                        Loggers.LOG.warn("ViewFolder cannot handle media type: " + r);
                    }
                }
            }

            // transfer our grouped items to the set
            if (groups.size() > 0) {
                boolean pruning = presentation.isPruningSingleItems();
                for (IMediaResource r : groups.values()) {
                    if (r instanceof IMediaFolder) {
                        IMediaFolder f = (IMediaFolder) r;
                        if (pruning && f.getChildren().size() == 1) {
                            IMediaResource ch = f.getChildren().get(0);
                            if (ch instanceof IMediaFolder) {
                                set.add(newViewFolder(factory, level + 1, this, (IMediaFolder) ch));
                            } else {
                                set.add(newViewItem(this, (IMediaFile) ch));
                            }
                        } else {
                            set.add(newViewFolder(factory, level + 1, this, (IMediaFolder) r));
                        }
                    } else {
                        set.add(newViewFolder(factory, level + 1, this, (IMediaFolder) r));
                    }
                }
            }

            if (Loggers.VFS_LOG.isDebugEnabled()) {
                StringBuffer sb = new StringBuffer();
                sb.append("\nAfter Decorating Folder: " + title + "; in " + parentTitle + "; Items: " + set.size() + "\n");
                for (IMediaResource r : set) {
                    sb.append(" + " + r.getTitle());
                    if (r instanceof IMediaFolder)
                        sb.append(" (Folder)");
                    sb.append("\n");
                }
                sb.append("\n\n");
                Loggers.VFS_LOG.debug(sb);
            }

            if (presentation.hasSorters()) {
                Loggers.VFS_LOG.debug("Sorting View Items");
                presentation.sort(set);
            }

            Loggers.VFS_LOG.info("End Decoring View Items for: " + getTitle() + "; in " + parentTitle + "; Time: " + timer.delta()
                    + "ms");
            return new ArrayList<IMediaResource>(set);
        } catch (Throwable t) {
            log.warn("Failed to create ViewFolder for: " + getTitle(), t);
            return new ArrayList<IMediaResource>();
        }
    }

    private IMediaResource newViewItem(ViewFolder viewFolder, IMediaFile item) {
        return new ViewItem(viewFolder, item);
    }

    protected IMediaResource newViewFolder(ViewFactory factory, int level, ViewFolder parent, IMediaFolder decorate) {
        return new ViewFolder(factory, level, parent, decorate);
    }

    private String createGroupKey(String groupName) {
        if (groupName == null)
            return null;
        groupName = groupName.toLowerCase();
        groupName = groupName.replaceAll("[^A-Za-z0-9]", "");
        return groupName;
    }

    private IMediaResource getItem(IMediaResource r) {
        if (r instanceof IMediaFile) {
            if (r instanceof ViewItem) {
                return ((ViewItem) r).getDecoratedItem();
            }
        } else {
            if (r instanceof DecoratedMediaFolder) {
                return ((DecoratedMediaFolder) r).getUndecoratedFolder();
            }
        }
        return r;
    }

    @Override
    public IMediaFolder getParent() {
        return parent;
    }

    public boolean hasTag(String tag) {
        return getTags().contains(tag);
    }

    public Filter setFilter(Filter filter) {
        presentation.getFilters().remove(filter);
        presentation.getFilters().add(filter);
        setChanged();
        return filter;
    }

    public Filter removeFilter(Filter filter) {
        presentation.getFilters().remove(filter);
        setChanged();
        return filter;
    }

    public List<Filter> getFilters() {
        return presentation.getFilters();
    }

    public Sorter setSorter(Sorter sort) {
        presentation.getSorters().remove(sort);
        presentation.getSorters().add(sort);
        setChanged();
        return sort;
    }

    public Sorter removeSorter(Sorter sorter) {
        presentation.getSorters().remove(sorter);
        setChanged();
        return sorter;
    }

    public List<Sorter> getSorters() {
        return presentation.getSorters();
    }

    public Grouper setGrouper(Grouper group) {
        presentation.getGroupers().remove(group);
        presentation.getGroupers().add(group);
        setChanged();
        return group;
    }

    public Grouper removeGrouper(Grouper grouper) {
        presentation.getGroupers().remove(grouper);
        setChanged();
        return grouper;
    }

    public List<Grouper> getGroupers() {
        return presentation.getGroupers();
    }

    public void refresh() {
        setChanged();
    }

    public int getPresentationLevel() {
        return level;
    }

    public ViewPresentation getPresentation() {
        return presentation;
    }

    public void setPresentation(ViewPresentation presentation) {
        this.presentation = presentation;
    }

    public boolean hasHint(String hint) {
        return presentation.hasHint(hint);
    }

    public ViewFactory getViewFactory() {
        return factory;
    }

    @Override
    public String toString() {
        return "ViewFolder [level=" + level + ", id=" + getId() + "]";
    }

    @Override
    public boolean delete(Hints hints) {
        if (super.delete(hints)) {
            setChanged();
            return true;
        }
        return false;
    }
}
