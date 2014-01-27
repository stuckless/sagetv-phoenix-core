package sagex.phoenix.configuration;

import org.apache.log4j.Logger;

import sagex.phoenix.node.INode;
import sagex.phoenix.util.var.DynamicVariable;

public abstract class AbstractElement implements IConfigurationElement, INode<Group> {
    protected static final Logger log = Logger.getLogger(AbstractElement.class);
    
    protected int                   elementType = -1;
    protected String                description;
    protected String                id;
    protected String                label;
    protected Group parent      = null;
    protected DynamicVariable<Boolean> visible=new DynamicVariable<Boolean>(Boolean.class, "true");

    public AbstractElement(int elementType) {
        this.elementType=elementType;
    }

    public AbstractElement(int elementType, String id) {
        this.elementType=elementType;
        this.id = id;
    }

    public Group getParent() {
        return parent;
    }

    public boolean isVisible() {
        return (visible==null ? true : visible.get());
    }

    public void setIsVisible(String visibleExpr) {
    	visible.setValue(visibleExpr);
    }
    
    public void setParent(Group parent) {
        this.parent = parent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getElementType() {
        return elementType;
    }

    public void setElementType(int elementType) {
        this.elementType = elementType;
    }

    public void visit(IConfigurationMetadataVisitor vis) {
        vis.accept(this);
    }
}
