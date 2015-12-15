package sagex.phoenix.node;

public interface INode<ParentType> {
    public ParentType getParent();

    public String getId();
}
