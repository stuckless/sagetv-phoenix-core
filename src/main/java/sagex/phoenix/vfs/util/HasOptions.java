package sagex.phoenix.vfs.util;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;

import java.util.List;

public interface HasOptions {
    public List<ConfigurableOption> getOptions();

    public void onUpdate(BaseConfigurable configurable);
}
