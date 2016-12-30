package sagex.phoenix.menu;

import org.apache.commons.lang3.StringUtils;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by seans on 30/12/16.
 */
public class DelegateMenu extends Menu implements IMenuDelegates {
    Menu original;
    Menu delegate;

    public DelegateMenu(Menu parent, Menu original, Menu delegate) {
        super(parent);
        this.delegate=delegate;
        this.original=original;

        this.label = original.label();
        this.visible = original.visible();
        this.isDefault = original.isDefault();

        // set some fields that we will allow to override
        if (StringUtils.isEmpty(this.original.label().get())) {
            this.label.setValue(this.delegate.label().getValue());
        }

        if (StringUtils.isEmpty(this.original.visible().getValue())) {
            this.visible().setValue(this.delegate.visible().getValue());
        }

        if (StringUtils.isEmpty(this.original.isDefault().getValue())) {
            this.isDefault().setValue(this.delegate.isDefault().getValue());
        }
    }

    @Override
    public Iterator<IMenuItem> iterator() {
        return delegate.iterator();
    }

    @Override
    public Script getScript() {
        return delegate.getScript();
    }

    @Override
    public void setScript(Script script) {
        delegate.setScript(script);
    }

    @Override
    public void addItem(IMenuItem item) {
        delegate.addItem(item);
    }

    @Override
    public void addItem(IMenuItem item, int pos) {
        delegate.addItem(item, pos);
    }

    @Override
    public void addItem(IMenuItem item, String insertId, Insert insertMode) {
        delegate.addItem(item, insertId, insertMode);
    }

    @Override
    public void addItem(IMenuItem item, String insertAfter) {
        delegate.addItem(item, insertAfter);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public List<Action> getActions() {
        return delegate.getActions();
    }

    @Override
    public void addAction(Action action) {
        delegate.addAction(action);
    }

    @Override
    public void removeItem(IMenuItem item) {
        delegate.removeItem(item);
    }

    @Override
    public int indexOf(IMenuItem item) {
        return delegate.indexOf(item);
    }

    @Override
    public boolean performActions() {
        return delegate.performActions();
    }

    @Override
    public int indexOf(String itemName) {
        return delegate.indexOf(itemName);
    }

    @Override
    public boolean replaceItem(IMenuItem olditem, IMenuItem newitem) {
        return delegate.replaceItem(olditem, newitem);
    }

    @Override
    public void field(String name, String value) {
        delegate.field(name, value);
    }

    @Override
    public void field(String name, DynamicVariable<String> value) {
        delegate.field(name, value);
    }

    @Override
    public DynamicVariable<String> field(String name) {
        return delegate.field(name);
    }

    @Override
    public List<IMenuItem> getItems() {
        return delegate.getItems();
    }

    @Override
    public List<IMenuItem> getVisibleItems() {
        return delegate.getVisibleItems();
    }

    @Override
    public Map<String, DynamicVariable<String>> getFields() {
        return delegate.getFields();
    }

    @Override
    public Menu getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public DynamicVariable<String> background() {
        return delegate.background();
    }

    @Override
    public IMenuItem getItemByName(String name) {
        return delegate.getItemByName(name);
    }

    @Override
    public DynamicVariable<String> icon() {
        return delegate.icon();
    }

    @Override
    public DynamicVariable<String> type() {
        return delegate.type();
    }

    @Override
    public DynamicVariable<String> secondaryIcon() {
        return delegate.secondaryIcon();
    }

    @Override
    public boolean hasChildren() {
        return delegate.hasChildren();
    }

    @Override
    public DynamicVariable<String> description() {
        return delegate.description();
    }

    @Override
    public int getChildCount() {
        return delegate.getChildCount();
    }

    @Override
    public DynamicVariable<String> linkedMenuId() {
        return delegate.linkedMenuId();
    }

    @Override
    public IMenuItem getChild(int pos) {
        return delegate.getChild(pos);
    }

    @Override
    public void setParent(Menu parent) {
        this.parent=parent;
    }

    @Override
    public void sortItems() {
        delegate.sortItems();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public Object getUserData() {
        return delegate.getUserData();
    }

    @Override
    public void setUserData(Object userData) {
        delegate.setUserData(userData);
    }

    @Override
    public void saveOrder() {
        delegate.saveOrder();
    }

    @Override
    public String getReference() {
        return delegate.getReference();
    }

    @Override
    public boolean isReference() {
        return delegate.isReference();
    }

    @Override
    public void visit(INodeVisitor<IMenuItem> visitor) {
        delegate.visit(visitor);
    }

    @Override
    public IMenuItem getOriginalItem() {
        return original;
    }

    @Override
    public IMenuItem getDelegateItem() {
        return delegate;
    }
}
