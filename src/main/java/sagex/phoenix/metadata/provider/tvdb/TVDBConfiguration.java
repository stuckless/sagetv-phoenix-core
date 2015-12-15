package sagex.phoenix.metadata.provider.tvdb;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "TheTVDB Configuration", path = "bmt/tvdb", description = "Configuration for The TBDb")
public class TVDBConfiguration extends GroupProxy {
    @AField(label = "TVDB Language", description = "2 letter language code (all lowercase)", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<String> language = new FieldProxy<String>("en");

    public TVDBConfiguration() {
        super();
        init();
    }

    public String getLanguage() {
        return language.get();
    }

    public void setLanguage(String lang) {
        this.language.set(lang);
    }
}
