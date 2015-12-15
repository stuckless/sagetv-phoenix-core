package sagex.phoenix.menu;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.views.ViewFolder;

/**
 * @author sean
 */
public class ViewMenu extends Menu implements Iterable<IMenuItem>, IMenuItem {
    protected String contextVar = "VFSMenuMediaFile";
    protected boolean preload = false;

    public String getContextVar() {
        return contextVar;
    }

    public void setContextVar(String contextVar) {
        this.contextVar = contextVar;
    }

    public boolean isPreloaded() {
        return preload;
    }

    public void setPreload(boolean preload) {
        this.preload = preload;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    protected int limit = 10;

    protected boolean resolved = false;
    protected ViewFolder folder = null;

    public ViewMenu(Menu parent) {
        this(parent, null);
    }

    public ViewMenu(Menu parent, ViewFolder parentFolder) {
        super(parent);
        this.folder = parentFolder;
        resolved = false;
    }

    /**
     * Resolves the view name into a list of menu items, and then returns it.
     *
     * @return
     */
    public List<IMenuItem> getItems() {
        if (resolved) {
            return items;
        }
        resolved = true;
        ViewFolder fold = getFolder();
        int i = 0;
        boolean addDefaultAction = getActions().size() == 0;

        for (IMediaResource r : fold) {
            if (r instanceof IMediaFolder) {
                addItem(new ViewMenu(this, (ViewFolder) r));
            } else {
                MenuItem mi = new MenuItem(this);
                mi.background().set(phoenix.fanart.GetFanartBackground(r));
                mi.icon().set(phoenix.fanart.GetFanartPoster(r));
                mi.label().set(phoenix.media.GetFormattedTitle(r));
                mi.description().set(((IMediaFile) r).getMetadata().getDescription());
                mi.setUserData(r);
                // add our actions to the menu item
                if (contextVar != null) {
                    // ensures the current menu item is added to the static
                    // context, before
                    // the other actions processed.
                    mi.addAction(new SageAddStaticContextAction(contextVar, r));
                    if (addDefaultAction) {
                        SageEvalAction sea = new SageEvalAction();
                        sea.action().setValue(String.format("phoenix_umb_Play( %s, GetUIContextName())", contextVar));
                        mi.addAction(sea);
                    }
                }

                for (Action a : getActions()) {
                    mi.addAction(a);
                }

                addItem(mi);
            }

            if (++i > limit) {
                log.info("Stopped processing view items, since we reached the limit: " + limit);
                break;
            }
        }

        return items;
    }

    private ViewFolder getFolder() {
        if (folder == null) {
            // not resolved, so let's get the view, add the items.
            folder = phoenix.umb.CreateView(getName());
            if (folder == null) {
                log.warn("Invalid View Folder Name for Menu:" + getName() + " Menu will be unstable.");
                folder = new ViewFolder(new sagex.phoenix.vfs.views.ViewFactory(), 0, null, new VirtualMediaFolder("ERROR: "
                        + getName()));
            }
        }
        return folder;
    }

    public List<IMenuItem> getVisibleItems() {
        return getItems();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * View Menus have default actions that get passed to the children
     */
    @Override
    public List<Action> getActions() {
        return actions;
    }

    /**
     * You cannot perfom an action on a menu
     */
    @Override
    public boolean performActions() {
        return false;
    }
}
