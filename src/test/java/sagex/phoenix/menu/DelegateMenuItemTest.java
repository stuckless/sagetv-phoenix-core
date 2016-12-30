package sagex.phoenix.menu;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by seans on 30/12/16.
 */
public class DelegateMenuItemTest {
    @Test
    public void testDelegate() {
        // create action menu
        Menu actionmenu = new Menu(null);
        MenuItem actionitem = (MenuItem)createItem(actionmenu, "actiontest1","Action Test 1");
        actionmenu.addItem(actionitem);
        actionmenu.addItem(createItem(actionmenu, "actiontest2","Action Test 2"));

        // add sub menu to the action menu
        Menu subAction = new Menu(actionmenu);
        subAction.setName("submenu");
        subAction.label().set("Sub Menu");
        subAction.addItem(createItem(subAction,"SA1", "Sub Action 1"));
        subAction.addItem(createItem(subAction,"SA2", "Sub Action 2"));
        actionmenu.addItem(subAction);

        // create new menu with references (these are just placeholders here)
        Menu menu = new Menu(null);
        MenuItem refItem = (MenuItem) createReferenceItem(menu, "actiontest1");
        menu.addItem(refItem);
        menu.addItem(createReferenceItem(menu, "ref123")); // unknown references, that's ok
        menu.addItem(createReferenceItem(menu, "ref456")); // unknown references, that's ok

        // add sub menu reference to the menu
        Menu subMenu = new Menu(menu);
        subMenu.setReference("submenu");
        menu.addItem(subMenu);

        // create a delegate item
        DelegateMenuItem dmi = new DelegateMenuItem(menu, refItem, actionitem);
        assertEquals(actionitem.getName(), dmi.getName());
        assertEquals(actionitem.label().get(), dmi.label().get());
        assertTrue(dmi.getDelegateItem() == actionitem);

        // replace the real item with the delegate item
        assertTrue(menu.replaceItem(refItem, dmi));
        assertEquals(4, menu.getItems().size());

        // create delegate menu
        DelegateMenu dm = new DelegateMenu(menu, subMenu, subAction);

        // replace the menu reference
        assertTrue(menu.replaceItem(subMenu, subAction));

        // menu has access to the delegate item
        MenuItem testMenuItem = (MenuItem) menu.getItemByName("actiontest1");
        assertNotNull(testMenuItem);
        assertTrue(testMenuItem.getClass().getName(), testMenuItem instanceof DelegateMenuItem);
        assertEquals("Action Test 1", testMenuItem.label().get());

        // test sub menu reference
        Menu subActionMenu = (Menu)menu.getItemByName("submenu");
        assertNotNull(subActionMenu);
        assertEquals("Sub Menu", subActionMenu.label().get());
        assertEquals(2, subActionMenu.getItems().size());
    }

    private IMenuItem createReferenceItem(Menu parent, String refId) {
        MenuItem mi = new MenuItem(parent);
        mi.setReference(refId);
        return mi;
    }

    private IMenuItem createItem(Menu parent, String id, String label) {
        MenuItem mi = new MenuItem(parent);
        mi.setName(id);
        mi.label().set(label);
        return mi;
    }
}