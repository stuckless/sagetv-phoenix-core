package sagex.phoenix.vfs.filters;

import java.util.Set;
import java.util.TreeSet;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Filters are used to restrict a view based on a criteria. Subclasses must
 * implement the canAccept() method, and define their configurable options in
 * the constructor. You must call super() from the constructor to ensure that
 * the base filter options get added.
 * 
 * Subclasses must also extend the clone method, if they have objects that
 * require deep cloning.
 * 
 * @author seans
 */
public abstract class Filter extends BaseConfigurable implements IResourceFilter, Cloneable, HasName, HasLabel {
	/**
	 * Can be "include" or "exclude" * * {@value}
	 */
	public static final String OPT_SCOPE = "scope";

	/**
	 * Most filters can have a "value", if they do, then they should use this
	 * option name. * * {@value}
	 */
	public static final String OPT_VALUE = "value";

	// the state is cached for performance reasons
	private boolean include = true;

	private String label, factoryid;

	private Set<String> tags = new TreeSet<String>();

	public Filter() {
		super();
		addOption(new ConfigurableOption(OPT_SCOPE, "Include/Exclude", "include", DataType.string, true, ListSelection.single,
				"include:Include,exclude:Exclude"));
	}

	public boolean accept(IMediaResource res) {
		if (isChanged()) {
			updateLocalValues();
		}

		boolean ok = canAccept(res);

		if (!include) {
			ok = !ok;
		}

		return ok;
	}

	private void updateLocalValues() {
		include = "include".equals(getOption(OPT_SCOPE).getString("include"));

		onUpdate();

		clearChanged();
	}

	/**
	 * Subclasses can override this method to udpate thier local states when
	 * options change. This will only be called at the time that accept() method
	 * is called.
	 */
	protected void onUpdate() {
	}

	protected abstract boolean canAccept(IMediaResource res);

	/**
	 * convenience method that sets the filter's 'value' option and then updated
	 * the changed status if the value changed.
	 * 
	 * @param newValue
	 */
	public void setValue(String newValue) {
		ConfigurableOption op = getOption("value");
		if (op != null) {
			op.value().setValue(newValue);
			updateOption(op);
		} else {
			log.warn("Filter does not support a 'value'");
		}
	}

	/**
	 * Return the value for the filter
	 * 
	 * @return
	 */
	public String getValue() {
		ConfigurableOption co = getOption(OPT_VALUE);
		if (co == null) {
			return null;
		}
		Object v = co.value().get();
		if (v == null)
			return null;
		return String.valueOf(v);
	}

	/**
	 * returns true if this filter's scope is to Include rather Exclude items.
	 * 
	 * @return
	 */
	public boolean isInclude() {
		return include;
	}

	/**
	 * Convenience method that sets the filter to be an include filter
	 */
	public void setInclude() {
		ConfigurableOption op = getOption(OPT_SCOPE);
		op.value().set("include");
		updateOption(op);
	}

	/**
	 * Convenience method that sets the filter to be an exclude filter
	 */
	public void setExclude() {
		ConfigurableOption op = getOption(OPT_SCOPE);
		op.value().set("exclude");
		updateOption(op);
	}

	public void setFactoryid(String factoryid) {
		this.factoryid = factoryid;
	}

	public String getFactoryid() {
		return factoryid;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return getFactoryid();
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
}
