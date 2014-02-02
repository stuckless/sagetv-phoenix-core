package sagex.phoenix.configuration;

import java.util.List;

import sagex.phoenix.util.Hints;
import sagex.phoenix.util.NamedValue;

public class Field extends AbstractElement {
	private ConfigType type = ConfigType.TEXT;
	private String defaultValue;
	private IOptionFactory optionFactory = null;
	private String listSeparator;
	private ConfigScope scope = ConfigScope.CLIENT;
	private Hints hints = new Hints();

	public Hints getHints() {
		return hints;
	}

	public void setHints(Hints hints) {
		this.hints = hints;
	}

	public ConfigType getType() {
		return type;
	}

	public void setType(ConfigType type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<NamedValue> getOptions() {
		if (optionFactory == null && type == ConfigType.BOOL) {
			optionFactory = BooleanOptionsFactory.DEFAULT_BOOLEAN_FACTORY;
		}

		if (optionFactory != null) {
			return optionFactory.getOptions(getId());
		}
		return null;
	}

	/**
	 * Gets the Options factory for this item. An options factory is responsible
	 * for returning a list of possible values for a field.
	 * 
	 * @return
	 */
	public IOptionFactory getOptionFactory() {
		return optionFactory;
	}

	public void setOptionFactory(IOptionFactory optionFactory) {
		this.optionFactory = optionFactory;
	}

	public void setListSeparator(String listSepartor) {
		this.listSeparator = listSepartor;
	}

	/**
	 * Gets the list separator for this field, IF the field types is a
	 * multichoice field type.
	 * 
	 * @return
	 */
	public String getListSeparator() {
		return listSeparator;
	}

	/**
	 * gets the scope for the field, ie, Client, Server, User
	 * 
	 * @return
	 */
	public ConfigScope getScope() {
		return scope;
	}

	/**
	 * sets the scope for the field, ie, Client, Server, User
	 */
	public void setScope(ConfigScope scope) {
		this.scope = scope;
	}

	public Field() {
		super(FIELD);
	}

}
