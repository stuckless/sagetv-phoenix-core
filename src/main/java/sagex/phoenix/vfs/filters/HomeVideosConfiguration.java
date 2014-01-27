package sagex.phoenix.vfs.filters;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label="Home Videos", path = "phoenix/homevideos", description = "Home Video options")
public class HomeVideosConfiguration extends GroupProxy {
    @AField(label="Folders", description = "Semi-colon list of folders that are home video folders", listSeparator=";")
    private FieldProxy<String> folders = new FieldProxy<String>("");

    @AField(label="Category", description = "Category (genre) to assign to home videos as they are processed by the automatic metadata scanner")
    private FieldProxy<String> category = new FieldProxy<String>("");

    public HomeVideosConfiguration() {
        super();
        init();
    }

	public String getFolders() {
		return folders.get();
	}

	public void setFolders(String folders) {
		this.folders.set(folders);
	}

	public String getCategory() {
		return category.get();
	}

	public void setCategory(String category) {
		this.category.set(category);
	}
}
