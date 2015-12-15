package sagex.phoenix.menu;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.db.UserRecordUtil;
import sagex.phoenix.node.IContainer;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Menu extends MenuItem implements Iterable<IMenuItem>, IMenuItem, IContainer<Menu, IMenuItem> {
    public static final String FIELD_SORTORDER = "sortorder";

    public enum Insert {
        before, after
    }

    protected Script script;
    protected List<IMenuItem> items = new LinkedList<IMenuItem>();
    protected DynamicVariable<String> type = new DynamicVariable<String>(String.class, null);

    public Menu(Menu parent) {
        super(parent);
    }

    public Iterator<IMenuItem> iterator() {
        return items.iterator();
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public void addItem(IMenuItem item) {
        items.add(item);
        ((MenuItem) item).setParent(this);
    }

    public void addItem(IMenuItem item, int pos) {
        items.add(pos, item);
        ((MenuItem) item).setParent(this);
    }

    public void addItem(IMenuItem item, String insertId, Insert insertMode) {
        if (insertMode == null || insertId == null) {
            addItem(item);
            return;
        }

        IMenuItem i = getItemByName(insertId);
        if (i == null) {
            addItem(item);
            return;
        }

        if (insertMode == Insert.after) {
            addItem(item, indexOf(i) + 1);
        } else if (insertMode == Insert.before) {
            addItem(item, indexOf(i));
        }
    }

    public void addItem(IMenuItem item, String insertAfter) {
        if (insertAfter == null) {
            addItem(item);
            return;
        }

        IMenuItem i = getItemByName(insertAfter);
        if (i == null) {
            addItem(item);
            return;
        }

        addItem(item, indexOf(i) + 1);
    }

    public void removeItem(IMenuItem item) {
        items.remove(item);
        ((MenuItem) item).setParent(null);
    }

    public int indexOf(IMenuItem item) {
        return items.indexOf(item);
    }

    public int indexOf(String itemName) {
        return items.indexOf(getItemByName(itemName));
    }

    public boolean replaceItem(IMenuItem olditem, IMenuItem newitem) {
        boolean replaced = false;
        int i = indexOf(olditem);
        if (i != -1) {
            removeItem(olditem);
            addItem(newitem, i);
            replaced = true;
        }
        return replaced;
    }

    /**
     * Returns ALL items, doesn't matter... does not check for visibility
     *
     * @return
     */
    public List<IMenuItem> getItems() {
        return items;
    }

    public List<IMenuItem> getVisibleItems() {
        List<IMenuItem> list = new LinkedList<IMenuItem>();

        for (IMenuItem mi : items) {
            if (mi.visible().get()) {
                list.add(mi);
            }
        }

        return list;
    }

    public String toString() {
        return "Menu[name: " + name + ", items: " + items.size() + "]";
    }

    public IMenuItem getItemByName(String name) {
        if (name == null)
            return null;
        for (IMenuItem mi : items) {
            if (name.equals(mi.getName()))
                return mi;
        }
        return null;
    }

    public DynamicVariable<String> type() {
        return type;
    }

    /**
     * Menus cannot have actions
     */
    @Override
    public List<Action> getActions() {
        return Collections.EMPTY_LIST;
    }

    /**
     * You cannot perfom an action on a menu
     */
    @Override
    public boolean performActions() {
        return false;
    }

    @Override
    public boolean hasChildren() {
        return getItems().size() > 0;
    }

    @Override
    public int getChildCount() {
        return getItems().size();
    }

    @Override
    public IMenuItem getChild(int pos) {
        return getItems().get(pos);
    }

    public void sortItems() {
        String sortorder = UserRecordUtil.getField(STORE_ID, getName(), FIELD_SORTORDER);
        if (!StringUtils.isEmpty(sortorder)) {
            log.info("Apply Menu Order " + sortorder);
            StaticMenuSorter sorter = new StaticMenuSorter(this, sortorder);
            Collections.sort(getItems(), sorter);
        }
    }

    public void saveOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getItems().size(); i++) {
            IMenuItem mi = getItems().get(i);
            if (sb.length() > 0)
                sb.append(",");
            sb.append(mi.getName()).append(":").append(String.valueOf(i));
        }
        UserRecordUtil.setField(STORE_ID, getName(), FIELD_SORTORDER, sb.toString());
    }

    public void visit(INodeVisitor<IMenuItem> visitor) {
        visitor.visit(this);
        for (IMenuItem mi : items) {
            mi.visit(visitor);
        }
    }
}
