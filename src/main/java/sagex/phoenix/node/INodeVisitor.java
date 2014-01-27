package sagex.phoenix.node;

public interface INodeVisitor<T> {
	public void visit(T node);
}
