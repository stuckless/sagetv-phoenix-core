package sagex.phoenix.configuration;

import sagex.phoenix.node.INode;
import sagex.phoenix.util.Hints;

public interface IConfigurationElement extends INode<Group> {
    public static final int APPLICATION = 1;
    public static final int GROUP       = 2;
    public static final int FIELD       = 3;

    public int getElementType();

    public String getId();

    public String getLabel();

    public String getDescription();
    
    public Hints getHints();

    public Group getParent();
    public void setParent(Group parent);

    public void visit(IConfigurationMetadataVisitor vis);
    
    public boolean isVisible();
}
