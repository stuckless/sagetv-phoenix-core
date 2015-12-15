package sagex.phoenix.configuration;

import sagex.phoenix.node.IContainer;
import sagex.phoenix.util.Hints;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Group extends AbstractElement implements Iterable<IConfigurationElement>, IContainer<Group, IConfigurationElement> {
    protected List<IConfigurationElement> elements = new LinkedList<IConfigurationElement>();
    private Hints hints = new Hints();

    public Group() {
        super(GROUP);
    }

    public Group(String id) {
        super(GROUP, id);
    }

    public Hints getHints() {
        return hints;
    }

    public void setHints(Hints hints) {
        this.hints = hints;
    }

    public void addElement(IConfigurationElement e) {
        elements.add(e);
        e.setParent(this);
    }

    public IConfigurationElement findElement(String id) {
        for (IConfigurationElement ce : elements) {
            if (ce.getId() != null && ce.getId().equals(id)) {
                return ce;
            }
            // APPLICATION appears to be a very high level grouping, so do the
            // recursive search here if it's either a GROUP or APPLICATION.
            if (ce.getElementType() == GROUP || ce.getElementType() == APPLICATION) {
                IConfigurationElement temp = ((Group) ce).findElement(id);
                if (temp != null) {
                    return temp;
                }
            }
        }
        return null;
    }

    public Iterator<IConfigurationElement> iterator() {
        return elements.iterator();
    }

    public void addAll(Group g) {
        for (IConfigurationElement ce : g) {
            addElement(ce);
        }
    }

    @Override
    public void visit(IConfigurationMetadataVisitor vis) {
        vis.accept(this);

        for (IConfigurationElement ce : this) {
            ce.visit(vis);
        }
    }

    public IConfigurationElement[] getChildren() {
        return elements.toArray(new IConfigurationElement[elements.size()]);
    }

    public IConfigurationElement[] getVisibleItems() {
        List<IConfigurationElement> children = new LinkedList<IConfigurationElement>();
        for (IConfigurationElement ce : elements) {
            if (ce.isVisible()) {
                children.add(ce);
            }
        }
        return children.toArray(new IConfigurationElement[children.size()]);
    }

    public void removeElement(SearchResultGroup el) {
        elements.remove(el);
    }

    @Override
    public boolean hasChildren() {
        return elements.size() > 0;
    }

    @Override
    public int getChildCount() {
        return elements.size();
    }

    @Override
    public IConfigurationElement getChild(int pos) {
        return elements.get(pos);
    }
}
