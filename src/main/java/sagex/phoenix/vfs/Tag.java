package sagex.phoenix.vfs;

public class Tag {
	private String tag;
	private String label;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	private boolean visible = true;

	public Tag(String tag, String label, boolean visible) {
		this.tag = tag;
		this.label = label;
		this.visible = visible;
	}
}
