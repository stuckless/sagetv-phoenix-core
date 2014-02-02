package sagex.phoenix.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;

public class Container<ParentType, NodeType> implements IContainer<ParentType, NodeType>, INode<ParentType>, Iterable<NodeType>,
		HasLabel, HasName {
	protected List<NodeType> items = new ArrayList<NodeType>();
	private String id;
	private String label;
	private ParentType parent = null;

	public List<NodeType> getItems() {
		return items;
	}

	public void setItems(List<NodeType> items) {
		this.items = items;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParent(ParentType parent) {
		this.parent = parent;
	}

	@Override
	public boolean hasChildren() {
		return items.size() > 0;
	}

	@Override
	public int getChildCount() {
		return items.size();
	}

	@Override
	public NodeType getChild(int pos) {
		return items.get(pos);
	}

	@Override
	public Iterator<NodeType> iterator() {
		return items.iterator();
	}

	@Override
	public ParentType getParent() {
		return parent;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}
}
