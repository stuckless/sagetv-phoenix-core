package sagex.phoenix.menu;

public class Fragment extends Menu {
	private String parentMenu = null;
	private String insertAfter = null;
	private String insertBefore = null;
	
	public Fragment(Menu parent) {
		super(parent);
	}

	public String getParentMenu() {
		return parentMenu;
	}

	public void setParentMenu(String parentMenu) {
		this.parentMenu = parentMenu;
	}

	public String getInsertAfter() {
		return insertAfter;
	}

	public void setInsertAfter(String insertAfter) {
		this.insertAfter = insertAfter;
	}

	public String getInsertBefore() {
		return insertBefore;
	}

	public void setInsertBefore(String insertBefore) {
		this.insertBefore = insertBefore;
	}
}