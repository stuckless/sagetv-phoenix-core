package sagex.phoenix.node;

public interface IContainer<ParentType, NodeType> extends INode<ParentType> {
	public boolean hasChildren();

	public int getChildCount();

	public NodeType getChild(int pos);
}
