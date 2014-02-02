package sagex.phoenix.metadata.provider.imdb;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "IMDB Configuration (Not Used)", path = "phoenix/metadata/imdb", description = "Configuration for IMDB Metadata Parser")
public class IMDBConfiguration extends GroupProxy {
	@AField(label = "IMDb Domain", description = "Base IMDb Domain", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
	private FieldProxy<String> IMDbDomain = new FieldProxy<String>("www.imdb.com");

	@AField(label = "IMDb Preferred Poster Width", description = "Preferred height or width of an imdb thumbnail", visible = "server:prop:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
	private FieldProxy<Integer> forcedIMDBImageSize = new FieldProxy<Integer>(512);

	public IMDBConfiguration() {
		super();
		init();
	}

	public int getForcedIMDBImageSize() {
		return forcedIMDBImageSize.get();
	}

	public void setForcedIMDBImageSize(int forcedIMDBImageSize) {
		this.forcedIMDBImageSize.set(forcedIMDBImageSize);
	}

	public String getIMDbDomain() {
		return IMDbDomain.get();
	}

	public void setIMDbDomain(String dbDomain) {
		IMDbDomain.set(dbDomain);
	}
}
