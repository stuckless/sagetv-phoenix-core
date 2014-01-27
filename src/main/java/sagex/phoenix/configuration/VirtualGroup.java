package sagex.phoenix.configuration;

/**
 * A VirtualGroup is used to collate a group of items without reparenting the items.
 * 
 * ie, any items added to a virtual group will be visible to the group, but it will
 * remove the item from it's previous parent.
 * 
 * VirtualGroups are a good way to organize items for a temporary display
 * 
 * @author seans
 *
 */
public class VirtualGroup extends Group {
    public VirtualGroup() {
    }

    public VirtualGroup(String id) {
        super(id);
    }

    @Override
    /**
     * Overrides the default addElement() by adding this element to the group collection,
     * BUT it DOES NOT reparent the item.  ie, the item will still retain it's original
     * parent
     */
    public void addElement(IConfigurationElement e) {
        elements.add(e);
    }
}
