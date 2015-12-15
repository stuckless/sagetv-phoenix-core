package phoenix.impl;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.Phoenix;
import sagex.phoenix.db.UserRecordUtil;
import sagex.phoenix.menu.*;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.var.DynamicVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * STV api calls related to Dynamic Menus.
 * <p/>
 * NOTE: Many Dynamic Menu fields return a {@link DynamicVariable}. A
 * {@link DynamicVariable} is like a regular field but it can dynamically store
 * a property reference, sage expression, or a static value.
 * <p/>
 * For display purposes, any calls that return a {@link DynamicVariable} should
 * display fine in the STV.
 * <p/>
 * BUT, if you need to do logic on a DynamicValue (ie, boolean or int), then you
 * should use the GetComputedValue(DynamicValue) which will return the computed
 * value as a type, ie, boolean, int, etc.
 * <p/>
 * If you need to change the value of a {@link DynamicVariable} then you should
 * use the SetStoredValue method which enabled you to change the value of the
 * variable. Using this method you can change the property, expression or static
 * value of a {@link DynamicVariable}.
 *
 * @author seans
 */
@API(group = "menu")
public class DynamicMenusAPI {
    private Logger log = Logger.getLogger(this.getClass());

    /**
     * Get a Dynamic Menu for the Given Menu Name
     *
     * @param menu Menu name
     * @return Menu object
     */
    public Menu GetMenu(Object menu) {
        if (menu == null)
            return null;
        Menu m = null;
        if (menu instanceof Menu) {
            m = (Menu) menu;
        } else if (menu instanceof String) {
            m = Phoenix.getInstance().getMenuManager().getMenu((String) menu);
        }

        return m;
    }

    /**
     * Gets ALL the menu items for the given menu name or menu object.
     * <p/>
     * If you pass null, then it will assume you are requesting all menu items
     * from the root menu. ie, passing null returns a list of all ALL loaded
     * menu items.
     *
     * @param menu Menu object or Menu name
     * @return Array of visible Menu Items
     */
    public List<IMenuItem> GetMenuItems(Object menu) {
        if (menu == null) {
            return new ArrayList<IMenuItem>(GetMenus());
        }

        Menu m = GetMenu(menu);
        if (m == null)
            return null;

        return m.getItems();
    }

    /**
     * Returns ALL Loaded menus
     *
     * @return
     */
    public List<Menu> GetMenus() {
        return Phoenix.getInstance().getMenuManager().getMenus();
    }

    /**
     * Gets all Visible menu items for the given menu.
     *
     * @param menu Menu
     * @return All menu items
     */
    public List<IMenuItem> GetVisibleItems(Object menu) {
        if (menu == null)
            return null;
        Menu m = GetMenu(menu);
        if (m == null)
            return null;

        List<IMenuItem> items = new ArrayList<IMenuItem>();
        for (IMenuItem mi : m.getItems()) {
            if (IsVisible(mi)) {
                items.add(mi);
            }
        }

        return items;
    }

    /**
     * Get a Menu by name
     *
     * @param name
     * @return
     */
    public IMenuItem GetItemByName(Object menu, String name) {
        if (menu instanceof Menu) {
            return ((Menu) menu).getItemByName(name);
        }
        return null;
    }

    /**
     * Returns the visible value as a {@link DynamicVariable}
     *
     * @param item
     * @return {@link DynamicVariable}
     */
    public DynamicVariable<Boolean> Visible(IMenuItem item) {
        return item.visible();
    }

    /**
     * Returns the visible value as a {@link DynamicVariable}
     *
     * @param item
     * @return {@link DynamicVariable}
     */
    public boolean IsVisible(IMenuItem item) {
        try {
            return UserRecordUtil.getBoolean(MenuItem.STORE_ID, item.getName(), MenuItem.FIELD_VISIBLE, item.visible().get());
        } catch (Throwable t) {
            t.printStackTrace();
            return true;
        }
    }

    /**
     * This sets/removes a hidden flag to menu's UserRecord, to indicate that
     * the user is hiding the menu/item.
     * <p/>
     * Use this API to set the hidden flag for the menu outside the Xml.
     *
     * @param item
     */
    public void SetVisible(IMenuItem item, boolean vis) {
        UserRecordUtil.setField(MenuItem.STORE_ID, item.getName(), MenuItem.FIELD_VISIBLE, vis);
        item.visible().set(vis);
    }

    /**
     * Clears the Visibility Field
     *
     * @param item
     */
    public void ResetVisible(IMenuItem item) {
        UserRecordUtil.clearField(MenuItem.STORE_ID, item.getName(), MenuItem.FIELD_VISIBLE);
    }

    /**
     * Invokes the Menu Action for the given Menu Item
     *
     * @param item Menu Item
     * @return true if the Menu Action was invoked without errors
     */
    public boolean InvokeAction(MenuItem item) {
        return item.performActions();
    }

    /**
     * Gets the Label for the given menu item
     *
     * @param item Menu Item
     * @return label
     */
    public String GetLabel(IMenuItem item) {
        return UserRecordUtil.getField(MenuItem.STORE_ID, item.getName(), MenuItem.FIELD_LABEL, item.label().get());
        // return item.label().get();
    }

    /**
     * Sets the Label for the given menu item
     *
     * @param item Menu Item
     * @return label
     */
    public void SetLabel(IMenuItem item, String label) {
        UserRecordUtil.setField(MenuItem.STORE_ID, item.getName(), MenuItem.FIELD_LABEL, label);
        item.label().set(label);
    }

    /**
     * Gets the Label for the given menu item as a {@link DynamicVariable}
     *
     * @param item Menu Item
     * @return
     */
    public DynamicVariable<String> Label(IMenuItem item) {
        return item.label();
    }

    /**
     * Get the Icon for the given MenuItem
     *
     * @param item MenuItem
     * @return icon
     */
    public String GetIcon(IMenuItem item) {
        return item.icon().get();
    }

    /**
     * Get the Icon for the given MenuItem as a {@link DynamicVariable}
     *
     * @param item MenuItem
     * @return icon
     */
    public DynamicVariable Icon(IMenuItem item) {
        return item.icon();
    }

    /**
     * Get the Secondary Icon for the given MenuItem
     *
     * @param item MenuItem
     * @return icon
     */
    public String GetSecondaryIcon(IMenuItem item) {
        return item.secondaryIcon().get();
    }

    /**
     * Get the Secondary Icon for the given MenuItem as a
     * {@link DynamicVariable}
     *
     * @param item MenuItem
     * @return icon
     */
    public DynamicVariable SecondaryIcon(IMenuItem item) {
        return item.secondaryIcon();
    }

    /**
     * For a given menu item or menu, return the help text / description
     * associated with the item.
     *
     * @param menu MenuItem or Menu object
     * @return help text for the menu or menu item
     */
    public String GetHelp(IMenuItem menu) {
        return menu.description().get();
    }

    /**
     * For a given menu item or menu, return the help text / description
     * associated with the item as a {@link DynamicVariable}.
     *
     * @param menu MenuItem or Menu object
     * @return help text for the menu or menu item
     */
    public DynamicVariable Help(IMenuItem menu) {
        return menu.description();
    }

    /**
     * Get the Background for the given Menu/MenuItem
     *
     * @param item Menu/MenuItem
     * @return icon
     */
    public String GetBackground(IMenuItem item) {
        return item.background().get();
    }

    /**
     * Get the Background for the given Menu/MenuItem as a
     * {@link DynamicVariable}
     *
     * @param item Menu/MenuItem
     * @return icon
     */
    public DynamicVariable Background(IMenuItem item) {
        return item.background();
    }

    /**
     * Get the Field Value for the given Menu/MenuItem
     *
     * @param item      Menu/MenuItem
     * @param fieldName field name
     * @return Field Value
     */
    public String GetField(IMenuItem item, String fieldName) {
        return item.field(fieldName).get();
    }

    /**
     * Get the Field Value for the given Menu/MenuItem as a
     * {@link DynamicVariable}
     *
     * @param item      Menu/MenuItem
     * @param fieldName field name
     * @return Field Value
     */
    public DynamicVariable Field(IMenuItem item, String fieldName) {
        return item.field(fieldName);
    }

    /**
     * Return the user defined menu type for the given menu.
     *
     * @param menu Menu object
     * @return menu type or null if it's not set
     */
    public String GetMenuType(Menu menu) {
        return menu.type().get();
    }

    /**
     * Return the user defined menu type for the given menu.
     *
     * @param menu Menu object
     * @return menu type or null if it's not set
     */
    public DynamicVariable MenuType(Menu menu) {
        return menu.type();
    }

    /**
     * Force a reload of all menus.
     */
    public void ReloadMenus() {
        Phoenix.getInstance().getMenuManager().loadConfigurations();
    }

    /**
     * Return True if the Object being pass is a Menu. This is used when
     * iterating over menu items to determine if a given menu item has children.
     *
     * @param menu
     * @return
     */
    public boolean IsMenu(Object menu) {
        return menu instanceof Menu;
    }

    /**
     * Return True if the Object being pass is a MenuItem.
     *
     * @param item
     * @return
     */
    public boolean IsMenuItem(Object item) {
        return item instanceof IMenuItem;
    }

    /**
     * Returns the menu's parent or null, if the menu doesn't have a parent.
     *
     * @param menu {@link Menu} or {@link MenuItem}
     * @return {@link Menu} or null
     */
    public Menu GetParent(IMenuItem menu) {
        if (menu == null)
            return null;
        return menu.getParent();
    }

    /**
     * Returns the Linked Menu for this menu/menuItem or null, if there is no
     * linked menu.
     *
     * @param item {@link IMenuItem} or {@link Menu}
     * @return {@link Menu} or null
     */
    public Menu GetLinkedMenu(IMenuItem item) {
        try {
            if (item == null || item.linkedMenuId().get() == null)
                return null;
            return GetMenu(item.linkedMenuId().get());
        } catch (Exception e) {
            log.warn("Get Linked Menu Failed for: " + item);
            return null;
        }
    }

    /**
     * Returns the Linked Menu ID for this menu/menuItem as a
     * {@link DynamicVariable}.
     *
     * @param item {@link IMenuItem} or {@link Menu}
     * @return {@link Menu} or null
     */
    public DynamicVariable LinkedMenu(IMenuItem item) {
        return item.linkedMenuId();
    }

    /**
     * Return the unique id or name for this menu. A menu name is not required,
     * but if it does have a name, then it must be unique for ALL menus and
     * items within the entire menu system.
     *
     * @param item {@link IMenuItem} or {@link Menu}
     * @return {@link Menu} or null
     */
    public String GetName(IMenuItem item) {
        return item.getName();
    }

    /**
     * Creates a NEW menu with the given name. The name should be a unique
     * identifier for the menu.
     *
     * @param parent if this is a submenu, then this should be the parent for the
     *               menu
     * @param name   unique menu identifier
     * @param label  label for the menu
     * @return newly created menu
     */
    public Menu CreateMenu(Menu parent, String name, String label) {
        Menu menu = new Menu(parent);
        menu.setName(name);
        menu.label().setValue(label);
        if (parent != null) {
            parent.addItem(menu);
        }
        return menu;
    }

    /**
     * Creates a new menu item for a menu.
     *
     * @param parent menu to which the item will be added
     * @param name   unique menu item id/name
     * @param label  display label
     * @return
     */
    public MenuItem CreateMenuItem(Menu parent, String name, String label) {
        MenuItem mi = new MenuItem(parent);
        mi.setName(name);
        mi.label().set(label);
        if (parent != null) {
            parent.addItem(mi);
        }
        return mi;
    }

    /**
     * @param menu
     * @return
     * @deprecated - use save fragment
     */
    public String SaveMenu(Menu menu) {
        if (menu.getParent() != null) {
            return "Must call SaveMenu on the Parent Menu";
        }

        return "TODO: Save Menu Not Implemented";
    }

    /**
     * Saves the given {@link IMenuItem} (which can be a {@link Menu} or
     * {@link MenuItem} since the both implement {@link IMenuItem} to the
     * userdata menus areas. The filename will be combination of the parent
     * menu, and the item ids combined.
     *
     * @param item
     * @param parentMenuId id of the menu in which to insert this new item (cannot be
     *                     null)
     * @param insertBefore id of the element to insert before (can be null)
     * @param insertAfter  id of the elements to insert after (can be null)
     * @return true if the menu fragment was saved
     */
    public boolean SaveFragment(IMenuItem item, String parentMenuId, String insertBefore, String insertAfter) {
        String name = FileUtils.sanitize(parentMenuId) + "_" + FileUtils.sanitize(item.getName()) + "_fragment.xml";
        File out = new File(Phoenix.getInstance().getMenuManager().getUserFiles().getDir(), name);
        if (out.exists()) {
            log.warn("Overwriting file: " + out);
        }

        FileOutputStream fos = null;
        try {
            if (!out.getParentFile().exists()) {
                FileUtils.mkdirsQuietly(out);
            }
            XmlMenuSerializer ser = new XmlMenuSerializer();
            fos = new FileOutputStream(out);
            ser.serializeFragment(item, parentMenuId, insertBefore, insertAfter, fos);
            fos.flush();
            fos.close();
        } catch (Throwable e) {
            log.warn("Failed to save menu fragment", e);
            return false;
        } finally {
            IOUtils.closeQuietly(fos);
        }
        return true;
    }

    /**
     * Return the Actions for a given menu item.
     *
     * @param item
     * @return
     */
    public List<Action> GetActions(MenuItem item) {
        return item.getActions();
    }

    /**
     * Tests if an action is a SageCommandAction
     *
     * @param a
     * @return
     */
    public boolean IsSageCommandAction(Action a) {
        return a instanceof SageCommandAction;
    }

    /**
     * Tests if an action is a SageEvalAction
     *
     * @param a
     * @return
     */
    public boolean IsSageEvalAction(Action a) {
        return a instanceof SageEvalAction;
    }

    /**
     * Tests if an action is a SageScreen Action
     *
     * @param a
     * @return
     */
    public boolean IsSageScreenAction(Action a) {
        return a instanceof SageScreenAction;
    }

    /**
     * Tests if an action is a SageExecuteAction
     *
     * @param a
     * @return
     */
    public boolean IsExecuteCommandAction(Action a) {
        return a instanceof ExecuteCommandAction;
    }

    /**
     * Gets the Screen for a {@link SageScreenAction} as a
     * {@link DynamicVariable}
     *
     * @param a
     * @param value
     */
    public DynamicVariable<String> Screen(SageScreenAction a) {
        return a.action();
    }

    /**
     * Returns the command/action for a {@link SageCommandAction} as a
     * {@link DynamicVariable}
     *
     * @param a
     * @return
     */
    public DynamicVariable<String> SageCommand(SageCommandAction a) {
        return a.action();
    }

    /**
     * Returns the Sage Expression for the {@link SageEvalAction} as a
     * {@link DynamicVariable}
     *
     * @param a
     * @return
     */
    public DynamicVariable<String> SageExpression(SageEvalAction a) {
        return a.action();
    }

    /**
     * Removes an action from a menu item
     *
     * @param item
     * @param a
     */
    public void RemoveAction(MenuItem item, Action a) {
        item.getActions().remove(a);
    }

    /**
     * Removes a child menu item
     *
     * @param item
     * @param a
     */
    public void RemoveItem(Menu parent, IMenuItem child) {
        parent.removeItem((MenuItem) child);
    }

    /**
     * Adds a {@link SageCommandAction} to a menu item
     *
     * @param item
     * @param command
     * @return
     */
    public SageCommandAction AddSageCommandAction(IMenuItem item, String command) {
        SageCommandAction sa = new SageCommandAction();
        sa.action().setValue(command);
        item.getActions().add(sa);
        return sa;
    }

    /**
     * Adds a {@link SageScreenAction} to a menu item
     *
     * @param item
     * @param screenName
     * @return
     */
    public SageScreenAction AddScreenAction(IMenuItem item, String screenName) {
        SageScreenAction sa = new SageScreenAction();
        sa.action().setValue(screenName);
        item.getActions().add(sa);
        return sa;
    }

    /**
     * Adds a {@link SageEvalAction} to a menu item. A sage eval action allows
     * you to execute a sage expression
     *
     * @param item
     * @param screenName
     * @return
     */
    public SageEvalAction AddSageEvalAction(IMenuItem item, String expr) {
        SageEvalAction sa = new SageEvalAction();
        sa.action().setValue(expr);
        item.getActions().add(sa);
        return sa;
    }

    /**
     * Ands an {@link ExecuteCommandAction} to a menu item
     *
     * @param item           {@link IMenuItem}
     * @param exe            full path to command executable
     * @param args           complete command line args
     * @param os             one of windows, linux, mac, or null for all
     * @param workingDir     complete path to a working directory
     * @param outputVariable StaticGlobal where the output will be stored (can be null)
     * @return
     */
    public ExecuteCommandAction AddExecuteCommandAction(IMenuItem item, String exe, String args, String os, String workingDir,
                                                        String outputVariable) {
        ExecuteCommandAction sa = new ExecuteCommandAction();
        sa.action().setValue(exe);
        sa.setArgs(args);
        sa.setOS(os);
        sa.setOutputVariable(outputVariable);
        sa.workingDir().setValue(workingDir);
        item.getActions().add(sa);
        return sa;
    }

    /**
     * Returns the Computed Value for a given Variable. ie, if the Variable is
     * set a Property, then this returns the value of that property (ie,
     * computed value)
     *
     * @param var {@link DynamicVariable}
     * @return computed value
     */
    public Object GetComputedValue(DynamicVariable var) {
        return var.get();
    }

    /**
     * Returns the value that was used to set the Dynamic Variable. ie, if the
     * dynamic variable is a property dynamic value, then this call returns the
     * property name as the value.
     *
     * @param var {@link DynamicVariable}
     * @return the value stored in this {@link DynamicVariable} (not the
     * computed value)
     */
    public String GetStoredValue(DynamicVariable var) {
        return var.getValue();
    }

    /**
     * Updates the value of the {@link DynamicVariable}. Use this method to
     * update the value, so that if you set a property, expression, or static
     * value, then the dynamic variable can adjust correctly.
     * <p/>
     * See {@link DynamicVariable}.setvalue() for information on what types
     * expressions that you can set
     *
     * @param var
     * @param value
     */
    public void SetStoredValue(DynamicVariable var, Object value) {
        if (value != null) {
            var.setValue(String.valueOf(value));
        }
    }

    /**
     * Inserts the Menu Item before the reference item. If the itemToInsert
     * already exists, then it is removed, and then re-added at the location
     * before the reference item.
     *
     * @param menu
     * @param itemToInsert
     * @param referenceItem
     */
    public void InsertBefore(Menu menu, IMenuItem itemToInsert, IMenuItem referenceItem) {
        List<IMenuItem> items = menu.getItems();
        if (items.contains(itemToInsert)) {
            items.remove(itemToInsert);
        }
        int pos = items.indexOf(referenceItem);
        if (pos == -1) {
            items.add(itemToInsert);
        } else {
            items.add(pos, itemToInsert);
        }
    }

    /**
     * Inserts the Menu Item {@link Action} before the reference {@link Action}.
     * If the itemToInsert already exists, then it is removed, and then re-added
     * at the location before the reference item.
     *
     * @param menuitem
     * @param itemToInsert
     * @param referenceItem
     */
    public void InsertBefore(IMenuItem menuitem, Action itemToInsert, Action referenceItem) {
        List<Action> items = menuitem.getActions();
        if (items.contains(itemToInsert)) {
            items.remove(itemToInsert);
        }
        int pos = items.indexOf(referenceItem);
        if (pos == -1) {
            items.add(itemToInsert);
        } else {
            items.add(pos, itemToInsert);
        }
    }

    /**
     * Inserts the Menu Item After the reference item. If the itemToInsert
     * already exists, then it is removed, and then re-added at the location
     * after the reference item.
     *
     * @param menu
     * @param itemToInsert
     * @param referenceItem
     */
    public void InsertAfter(Menu menu, IMenuItem itemToInsert, IMenuItem referenceItem) {
        List<IMenuItem> items = menu.getItems();
        if (items.contains(itemToInsert)) {
            items.remove(itemToInsert);
        }

        int pos = items.indexOf(referenceItem);
        if (pos == -1) {
            items.add(itemToInsert);
        } else {
            items.add(pos + 1, itemToInsert);
        }
    }

    /**
     * Inserts the Menu Item Action before the reference {@link Action}. If the
     * itemToInsert already exists, then it is removed, and then re-added at the
     * location before the reference item.
     *
     * @param menuitem
     * @param itemToInsert
     * @param referenceItem
     */
    public void InsertAfter(IMenuItem menuitem, Action itemToInsert, Action referenceItem) {
        List<Action> items = menuitem.getActions();
        if (items.contains(itemToInsert)) {
            items.remove(itemToInsert);
        }
        int pos = items.indexOf(referenceItem);
        if (pos == -1) {
            items.add(itemToInsert);
        } else {
            items.add(pos + 1, itemToInsert);
        }
    }

    /**
     * Re-applies the Sort criteria to the menu items for this menu, if there is
     * a sort criteria defined.
     *
     * @param menu
     */
    public void SortItems(Menu menu) {
        if (menu != null)
            menu.sortItems();
    }

    /**
     * Saves the current sort order for the menu items of this menu. The sort
     * order is persisted, and when the menu is re-loaded the items current sort
     * order will be remembered.
     *
     * @param menu
     */
    public void SaveSortOrder(Menu menu) {
        if (menu != null)
            menu.saveOrder();
    }

    /**
     * Moves the item Up the Menu List (ie, closer to the top)
     *
     * @param item
     */
    public void MoveUp(IMenuItem item) {
        phoenix.util.MoveUp(item.getParent().getItems(), item);
    }

    /**
     * Moves the item down the menu list (ie, closer to the end)
     *
     * @param item
     */
    public void MoveDown(IMenuItem item) {
        phoenix.util.MoveDown(item.getParent().getItems(), item);
    }

    /**
     * Gets the userdata object associated with a menu item
     */
    public Object GetUserData(IMenuItem item) {
        return item.getUserData();
    }

    /**
     * returns true if the given menu is a ViewMenu
     *
     * @param item
     * @return
     */
    public boolean IsViewMenu(IMenuItem item) {
        return item != null && (item instanceof ViewMenu);
    }
}
