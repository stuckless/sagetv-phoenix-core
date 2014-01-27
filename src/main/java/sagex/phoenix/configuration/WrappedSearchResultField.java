package sagex.phoenix.configuration;

public class WrappedSearchResultField extends Field {
    private Field item = null;
    public WrappedSearchResultField(Field f) {
        this.item=f;
    }
    public boolean equals(Object obj) {
        return item.equals(obj);
    }
    public String getDefaultValue() {
        return item.getDefaultValue();
    }
    public String getDescription() {
        try {
            // return the description as Group -> Key
            return item.getParent().getParent().getLabel() + " > " + item.getParent().getLabel() + "\n" + item.getDescription();
        } catch (Throwable t) {
            return item.getDescription();
        }
    }
    public int getElementType() {
        return item.getElementType();
    }
    public String getId() {
        return item.getId();
    }
    public String getLabel() {
        return item.getLabel();
    }
    public Group getParent() {
        return item.getParent();
    }
    public ConfigType getType() {
        return item.getType();
    }
    public int hashCode() {
        return item.hashCode();
    }
    public void setDefaultValue(String defaultValue) {
        item.setDefaultValue(defaultValue);
    }
    public void setDescription(String description) {
        item.setDescription(description);
    }
    public void setElementType(int elementType) {
        item.setElementType(elementType);
    }
    public void setId(String id) {
        item.setId(id);
    }
    public void setLabel(String label) {
        item.setLabel(label);
    }
    public void setParent(Group parent) {
        item.setParent(parent);
    }
    public void setType(ConfigType type) {
        item.setType(type);
    }
    public String toString() {
        return item.toString();
    }
    public void visit(IConfigurationMetadataVisitor vis) {
        item.visit(vis);
    }
}
