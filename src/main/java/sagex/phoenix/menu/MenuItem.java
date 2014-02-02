package sagex.phoenix.menu;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

public class MenuItem implements IMenuItem {

	/**
	 * {@value}
	 */
	public static final String STORE_ID = "phoenix.menus";

	/**
	 * {@value}
	 */
	public static final String FIELD_VISIBLE = "visible";

	/**
	 * {@value}
	 */
	public static final String FIELD_LABEL = "label";

	protected Logger log = Logger.getLogger(this.getClass());

	protected Menu parent;
	protected String name;

	protected DynamicVariable<String> background = new DynamicVariable<String>(String.class, null);
	protected DynamicVariable<String> label = new DynamicVariable<String>(String.class, null);
	protected DynamicVariable<Boolean> visible = new DynamicVariable<Boolean>(Boolean.class, "true");
	protected DynamicVariable<String> icon = new DynamicVariable<String>(String.class, null);
	protected DynamicVariable<String> secondaryIcon = new DynamicVariable<String>(String.class, null);
	protected DynamicVariable<String> description = new DynamicVariable<String>(String.class, null);
	protected DynamicVariable<String> linkedMenuId = new DynamicVariable<String>(String.class, null);

	protected Map<String, DynamicVariable<String>> fields = new HashMap<String, DynamicVariable<String>>();
	protected List<Action> actions = new LinkedList<Action>();

	protected Object userData;

	public MenuItem(Menu parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void addAction(Action action) {
		actions.add(action);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + ", label=" + label + "]";
	}

	public boolean performActions() {
		log.debug("Performing Menu item Actions for MenuItem: " + this);
		boolean ok = true;
		for (Action a : actions) {
			if (!a.invoke()) {
				ok = false;
				break;
			}
		}
		return ok;
	}

	public void field(String name, String value) {
		fields.put(name, new DynamicVariable<String>(String.class, value));
	}

	public void field(String name, DynamicVariable<String> value) {
		fields.put(name, value);
	}

	public DynamicVariable<String> field(String name) {
		if (!fields.containsKey(name)) {
			field(name, (String) null);
		}
		return fields.get(name);
	}

	public Map<String, DynamicVariable<String>> getFields() {
		return fields;
	}

	public Menu getParent() {
		return parent;
	}

	public DynamicVariable<String> background() {
		return background;
	}

	public DynamicVariable<String> label() {
		return label;
	}

	public DynamicVariable<Boolean> visible() {
		return visible;
	}

	public DynamicVariable<String> icon() {
		return icon;
	}

	public DynamicVariable<String> secondaryIcon() {
		return secondaryIcon;
	}

	public DynamicVariable<String> description() {
		return description;
	}

	public DynamicVariable<String> linkedMenuId() {
		return linkedMenuId;
	}

	public void setParent(Menu parent) {
		this.parent = parent;
	}

	@Override
	public String getId() {
		return getName();
	}

	public Object getUserData() {
		return userData;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	@Override
	public void visit(INodeVisitor<IMenuItem> visitor) {
		visitor.visit(this);
	}
}
