package sagex.phoenix.metadata;

import java.io.Serializable;

public class CastMember implements ICastMember, Serializable {
    private static final long serialVersionUID = 1L;
    private String name, role, image;

    public CastMember(ICastMember cm) {
        this.name = cm.getName();
        this.role = cm.getRole();
        this.image = cm.getImage();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CastMember other = (CastMember) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        return true;
    }

}
