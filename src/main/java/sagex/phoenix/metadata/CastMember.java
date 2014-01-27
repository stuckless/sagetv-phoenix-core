package sagex.phoenix.metadata;

import java.io.Serializable;

public class CastMember implements ICastMember, Serializable {
    private static final long serialVersionUID = 1L;
    private String name, role, image;

    public CastMember(ICastMember cm) {
    	this.name=cm.getName();
    	this.role=cm.getRole();
    	this.image=cm.getImage();
    }

    public CastMember(String name, String role) {
        super();
        this.name = name;
        this.role = role;
    }

    public CastMember(String name, String role, String image) {
        super();
        this.name = name;
        this.role = role;
        this.image = image;
    }

    public CastMember() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "CastMember [" + (image != null ? "image=" + image + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (role != null ? "role=" + role : "") + "]";
	}

}
