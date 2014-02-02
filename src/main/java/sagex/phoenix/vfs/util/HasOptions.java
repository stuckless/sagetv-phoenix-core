package sagex.phoenix.vfs.util;

import java.util.List;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;

public interface HasOptions {
	public List<ConfigurableOption> getOptions();

	public void onUpdate(BaseConfigurable configurable);
}
