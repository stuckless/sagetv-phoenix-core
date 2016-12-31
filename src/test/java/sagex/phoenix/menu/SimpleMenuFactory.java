package sagex.phoenix.menu;

/**
 * Created by seans on 30/12/16.
 */
public class SimpleMenuFactory extends MenuFactory {
    public SimpleMenuFactory() {
    }

    @Override
    protected void populateMenuItems(Menu menuParent) {
        int limit = getLimit(menuParent);
        if (limit<=0) limit=10;
        for (int i=0;i<limit;i++) {
            MenuItem mi = createNewItem(menuParent, i);
            mi.setName(String.valueOf(i));
            mi.label().set("Item" + i);
        }
    }
}
