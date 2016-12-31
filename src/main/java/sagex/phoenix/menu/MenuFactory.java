package sagex.phoenix.menu;

import org.apache.commons.lang.math.NumberUtils;
import sagex.phoenix.Phoenix;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.var.DynamicVariable;

/**
 * Created by seans on 30/12/16.
 */
public abstract class MenuFactory {
    public static final String VAR_FACTORY_ITEM_VAR = "factoryItemVar";
    public static final String VAR_FACTORY_ITEM_REF = "factoryItemRef";
    public static final String VAR_FACTORY_MENU_REF = "factoryMenuRef";
    public static final String VAR_FACTORY_EXPIRY_MS = "factoryExpiryMS";
    public static final String VAR_FACTORY_ITEM_LIMIT = "factoryItemLimit";

    private long expired=0;
    private boolean resolving=false;

    public MenuFactory() {
    }

    /**
     * This is the entry point into the Factory.  A menu will be provided, and the Factory will either
     * return the menu with the existing items, or clear the items, and return a new set of items.
     */
    public void resolveMenuItems(Menu menuParent) {
        if (resolving || (!isExpired(menuParent) && menuParent.items.size()>0)) {
            // do nothing, we still have our items, or we are trying to resolve them
            return;
        }

        try {
            Loggers.LOG.debug("Resolving Menu Items using Factory: " + menuParent.factoryClass().get());
            resolving=true;
            menuParent.items.clear();
            populateMenuItems(menuParent);
            Loggers.LOG.debug("Done Resolving Menu Items using Factory: " + menuParent.factoryClass().get());
        } finally {
            resolving=false;
            expired = System.currentTimeMillis() + getExpiry(menuParent);
        }
    }

    /**
     * Sub-classes will implement this method and add items to the menu.  Classes will
     * likely use the convenience methods for createNewItem to add items that will inherit the actions of the
     * factoryItemRef.
     *
     * @param menuParent
     */
    protected abstract void populateMenuItems(Menu menuParent);

    /**
     * Convenience method that creates and adds a new menu item, and applies properties to the menu item based on
     * the field value for "factoryItemRef".  If field, "factoryItemVar" exists, then a Static Context Action will
     * be added to the actions, setting the context var to the curent user data object.
     *
     * @param userData
     * @return
     */
    public MenuItem createNewItem(Menu parent, Object userData) {
        MenuItem mi = new MenuItem(parent);
        parent.addItem(mi);
        mi.setUserData(userData);

        DynamicVariable<String> itemVar = parent.field(VAR_FACTORY_ITEM_VAR);
        if (itemVar != null) {
            // ensures the current menu item is added to the static
            // context, before the other actions processed.
            mi.addAction(new SageAddStaticContextAction(itemVar.get(), userData));
        }

        DynamicVariable<String> itemDecorator = parent.field(VAR_FACTORY_ITEM_REF);
        if (itemDecorator!=null) {
            IMenuItem item = resolveMenuItem(itemDecorator.get());
            if (item!=null) {
                mi.updateFrom(item);
            }
        }

        upgradeMenuItem(mi);
        return mi;
    }

    /**
     * Last chance to upgrade the Menu Item.  This might be used to add a custom action to the created menu item.
     * The item will have it's factoryItemRef applied at this point.
     *
     * @param mi
     */
    protected void upgradeMenuItem(MenuItem mi) {
        // subclasses do any last minute upgrades here.
    }

    /**
     * Convenience method to create a new Sub-Menu in the Menu Heirarchy.  It will inherit the properties of the
     * menu referenced by the field "factoryMenuRef"
     *
     * @param parent
     * @param userData
     * @return
     */
    public Menu createNewSubMenu(Menu parent, Object userData, MenuManager menuManager) {
        Menu menu = new Menu(parent);
        parent.addItem(menu);
        menu.setUserData(userData);

        DynamicVariable<String> itemDecorator = parent.field(VAR_FACTORY_MENU_REF);
        if (itemDecorator!=null) {
            IMenuItem item = menuManager.resolveMenuItem(itemDecorator.get());
            if (item!=null) {
                menu.updateFrom(item);
            }
        }

        upgradeMenuItem(menu);
        return menu;
    }

    public boolean isExpired(Menu menu) {
        long expiry = getExpiry(menu);
        return (expiry==0 || expired < System.currentTimeMillis());
    }

    IMenuItem resolveMenuItem(String menuPath) {
        if (menuPath!=null)
            return Phoenix.getInstance().getMenuManager().resolveMenuItem(menuPath);
        return null;
    }

    long getExpiry(Menu menu) {
        DynamicVariable<String> exp = menu.field(VAR_FACTORY_EXPIRY_MS);
        if (exp!=null) {
            return NumberUtils.toLong(exp.get(), 0);
        }
        return 0;
    }

    int getLimit(Menu menu) {
        DynamicVariable<String> limit = menu.field(VAR_FACTORY_ITEM_LIMIT);
        if (limit!=null) {
            return NumberUtils.toInt(limit.get(), 0);
        }
        return 0;
    }

}
