package sagex.phoenix.metadata.provider.tvdb4;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "TheTVDB4 Configuration", path = "phoenix/metadata/tvdb4", description = "Configuration for The TVDb v4")
public class TVDB4Configuration  extends GroupProxy {
    @AField(label = "TVDB4 API PIN", description = "Personal PIN needed to access TheTVDB v4 API.  This is needed for the paid TVDB v4 service.", scope = ConfigScope.SERVER)
    private FieldProxy<String> pin = new FieldProxy<String>("");

    @AField(label = "TVDB4 Language", description = "3 letter language code (all lowercase)", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<String> language = new FieldProxy<String>("eng");

    @AField(label = "TVDB4 Token", description = "Token for access. Expires after 30 days", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<String> token = new FieldProxy<String>("");

    public TVDB4Configuration() {
        super();
        init();
    }

    public String getPIN() {
        return pin.get();
    }

    public void setPIN(String pin) {
        this.pin.set(pin);
    }

    public String getLanguage() {
        return language.get();
    }

    public void setLanguage(String lang) {
        this.language.set(lang);
    }

    public String getToken() {
        return token.get();
    }

    public void setToken(String token) {
        this.token.set(token);
    }

}
