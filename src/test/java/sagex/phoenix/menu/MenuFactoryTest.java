package sagex.phoenix.menu;

import org.junit.BeforeClass;
import org.junit.Test;
import test.InitPhoenix;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by seans on 31/12/16.
 */
public class MenuFactoryTest {
    @Test
    public void resolveMenuItems() throws Exception {
        SimpleMenuFactory smf = new SimpleMenuFactory();
        Menu menu = new Menu(null);
        smf.resolveMenuItems(menu);
        assertEquals(10, menu.getChildCount());
    }

    @Test
    public void resolveMenuItemsWithLimit() throws Exception {
        SimpleMenuFactory smf = spy(new SimpleMenuFactory());
        Menu menu = new Menu(null);
        menu.field(MenuFactory.VAR_FACTORY_ITEM_LIMIT, "5");
        smf.resolveMenuItems(menu);
        smf.resolveMenuItems(menu); // with no expiry, items are resolved every time
        assertEquals(5, menu.getChildCount());
        verify(smf, times(2)).populateMenuItems(menu);
    }

    @Test
    public void resolveMenuItemsWithExpiry() throws Exception {
        SimpleMenuFactory smf = spy(new SimpleMenuFactory());
        Menu menu = new Menu(null);
        menu.field(MenuFactory.VAR_FACTORY_ITEM_LIMIT, "5");
        menu.field(MenuFactory.VAR_FACTORY_EXPIRY_MS, "500");
        smf.resolveMenuItems(menu);
        assertEquals(5, menu.getChildCount());
        smf.resolveMenuItems(menu); // should not do anything
        verify(smf, times(1)).populateMenuItems(menu);
        assertFalse(smf.isExpired(menu));
        Thread.sleep(600);
        smf.resolveMenuItems(menu); // should force new items
        verify(smf, times(2)).populateMenuItems(menu);
    }

}