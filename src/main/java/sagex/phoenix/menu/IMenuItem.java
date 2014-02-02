package sagex.phoenix.menu;

import java.util.List;

import sagex.phoenix.node.INode;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

public interface IMenuItem extends INode<Menu> {
	public Menu getParent();

	// name cannot be variable, since we need it to never change
	public String getName();

	// only valid for menu items, if called on a menu, then they will throw an
	// exception
	public List<Action> getActions();

	public boolean performActions();

	// the dynamic parts of a menu item
	public DynamicVariable<String> description();

	public DynamicVariable<String> label();

	public DynamicVariable<String> background();

	public DynamicVariable<String> icon();

	public DynamicVariable<String> secondaryIcon();

	public DynamicVariable<Boolean> visible();

	public DynamicVariable<String> field(String fldName);

	public DynamicVariable<String> linkedMenuId();

	// a menu item can hold a static reference to another object
	public void setUserData(Object data);

	public Object getUserData();

	public void visit(INodeVisitor<IMenuItem> visitor);
}
