package sagex.phoenix.menu;

import org.apache.commons.lang3.StringUtils;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

import java.util.List;
import java.util.Map;

/**
 * Created by seans on 30/12/16.
 */
public class DelegateMenuItem extends MenuItem implements IMenuDelegates {
    MenuItem original;
    MenuItem delegate;

    public DelegateMenuItem(Menu parent, MenuItem original, MenuItem delegate) {
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
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean performActions() {
        return delegate.performActions();
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
    public Map<String, DynamicVariable<String>> getFields() {
        return delegate.getFields();
    }

    @Override
    public Menu getParent() {
        return parent;
    }

    @Override
    public DynamicVariable<String> background() {
        return delegate.background();
    }

    @Override
    public DynamicVariable<String> icon() {
        return delegate.icon();
    }

    @Override
    public DynamicVariable<String> secondaryIcon() {
        return delegate.secondaryIcon();
    }

    @Override
    public DynamicVariable<String> description() {
        return delegate.description();
    }

    @Override
    public DynamicVariable<String> linkedMenuId() {
        return delegate.linkedMenuId();
    }

    @Override
    public void setParent(Menu parent) {
        this.parent=parent;
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
    public void visit(INodeVisitor<IMenuItem> visitor) {
        delegate.visit(visitor);
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
    public IMenuItem getOriginalItem() {
        return original;
    }

    @Override
    public IMenuItem getDelegateItem() {
        return delegate;
    }
}
