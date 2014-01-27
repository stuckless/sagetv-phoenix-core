package sagex.phoenix.plugin;

import sagex.phoenix.configuration.IConfigurationElement;
import sagex.plugin.IPropertyVisibility;

public class ConfigurationVisibility implements IPropertyVisibility {
	private IConfigurationElement el;
	public ConfigurationVisibility(IConfigurationElement el) {
		this.el=el;
	}

	@Override
	public boolean isVisible() {
		if (el==null) return false;
		return el.isVisible();
	}
}
