package sagex.phoenix.menu;

import java.util.List;

import sagex.phoenix.node.INode;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

public interface IMenuItem extends INode<Menu> {
    public Menu getParent();

    /**
     * The menu/menu item "name" is the unique ID of the menu.  In a menu system, each menu
     * has a unique "name".  If another menu file creates a menu with the same name, then the
     * last menu loaded will the ONLY menu loaded for that name.
     *
     * @return
     */
    public String getName();

    /**
     * Gets the Actions for the Given Menu/Item.
     *
     * @return
     */
    public List<Action> getActions();

    /**
     * Performs the MenuItems's Actions
     * @return
     */
    public boolean performActions();

    /**
     * Performs the MenuItems's Actions setting the Action Context (gMenuItemContext) to the given object.
     * Actions can reference the passed context via the static context variable gMenuItemContext
     *
     * @param context
     * @return
     */
    public boolean performActions(Object context);

    public void visit(INodeVisitor<IMenuItem> visitor);

    /**
     * Menus and Menu Items can reference other menus/menu items.  This allows the menu code to
     * create a set of shared actions that can be references from other menus and items.  The menu
     * loader will transparently manage this, so, nothing has be done from a rendering point of view.
     * But, you can call IsReference(Menu|MenuItem) Menu API to test if a given menu or item is
     * a reference, and in the case of a Menu Builder, you might NOT allow editing of such an item.
     *
     * The format of a references is "MENU_NAME::MENU_ITEM_NAME" where "::" separates the menu
     * name form the menu item.
     *
     * @return
     */
    public String getReference();

    /**
     * Each Menu/Item can hold a piece of user defined data
     * @param data
     */
    public void setUserData(Object data);
    public Object getUserData();



    // the dynamic parts of a menu item
    public DynamicVariable<String> description();

    public DynamicVariable<String> label();

    public DynamicVariable<String> background();

    public DynamicVariable<String> icon();

    public DynamicVariable<String> secondaryIcon();

    public DynamicVariable<Boolean> visible();

    /**
     * Identifies that in the case of a focussed menu, if an action is to be performed but there
     * isn't a focussed menu item, then the first item with isDefault=true will be the item
     * that receives the action request.
     *
     * @return
     */
    public DynamicVariable<Boolean> isDefault();

    /**
     * Menu's can define custom properties that can be stored on the menu/item for later retrieval
     * @param fldName
     * @return
     */
    public DynamicVariable<String> field(String fldName);

    /**
     * Linked Menu ID is basically an options menu attached to a menu item.  For example the
     * menu rendering code could listen for the "Options" command when menu item is focussed,
     * if linkedMenuId returns a non-null menu id, then it then show that menu in an options
     * dialog
     * @return
     */
    public DynamicVariable<String> linkedMenuId();
}
