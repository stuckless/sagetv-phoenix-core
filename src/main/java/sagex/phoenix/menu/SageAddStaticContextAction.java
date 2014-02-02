package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.Global;

/**
 * Allows a menu action to set a static context variable
 * 
 * @author sean
 */
public class SageAddStaticContextAction extends Action {
	private String name;
	private Object value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public SageAddStaticContextAction() {
	}

	public SageAddStaticContextAction(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean invoke() {
		try {
			Global.AddStaticContext(UIContext.getCurrentContext(), getName(), getValue());
			return true;
		} catch (Exception e) {
			log.error("Failed to invoke add static context: " + action());
			return false;
		}
	}
}
