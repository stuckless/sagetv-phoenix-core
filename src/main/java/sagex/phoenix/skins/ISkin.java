package sagex.phoenix.skins;

import java.io.File;

import sagex.phoenix.skins.Skin.State;
import sagex.phoenix.tools.annotation.API;

@API(group = "skin", prefix = "Skin", proxy = true, resolver = "phoenix.skin.GetSkin")
public interface ISkin {
	public abstract String getName();

	public abstract void setName(String name);

	public abstract File getDirectory();

	public abstract State getState();

	public abstract void setState(State state);

	public abstract String getId();

	public abstract void setId(String id);

	public abstract String getVersion();

	public abstract void setVersion(String version);

	public abstract String[] getDependencies();

	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract void setStatus(String status);

	public abstract String getStatus();

	public abstract File getResource(String path);

	public abstract String[] getScreenShots();
}
