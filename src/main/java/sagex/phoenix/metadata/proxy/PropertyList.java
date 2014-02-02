package sagex.phoenix.metadata.proxy;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A list facade that backed by some fake "string" list of items, ie, a csv
 * string. This shows the backing string list as a java list and all changes the
 * java list and make to backing string list. This list requires and an Adapter
 * be used to adapt this java list to the backing string csv list.
 * 
 * The primary use for this list is to provide an automated adapter between a
 * SageTV metadata list, ie Actors, and a Java List.
 * 
 * @author seans
 */
public class PropertyList<E> extends AbstractList<E> implements HasListChangedListeners<E> {
	private List<E> list = new ArrayList<E>();
	private IPropertyListChangedListener<E> changedHandler = null;

	private IPropertyListFactory factory = null;
	private SageProperty property = null;

	public PropertyList(IPropertyListFactory factory) {
		this.factory = factory;
	}

	public IPropertyListFactory getFactory() {
		return factory;
	}

	public void notifyListeners() {
		if (changedHandler != null) {
			changedHandler.propertyListChanged(this);
		}
	}

	@Override
	public E get(int arg0) {
		return list.get(arg0);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public void add(int index, E element) {
		list.add(index, element);
		notifyListeners();
	}

	@Override
	public E remove(int index) {
		E o = list.remove(index);
		notifyListeners();
		return o;
	}

	@Override
	public E set(int index, E element) {
		E o = list.set(index, element);
		notifyListeners();
		return o;
	}

	@Override
	public void addListChangedListener(IPropertyListChangedListener<E> listener) {
		this.changedHandler = listener;
	}

	/**
	 * Used to add to the list without it firing events. Useful when you are
	 * building up the the list initially
	 * 
	 * @param element
	 */
	public void addNoNotify(E element) {
		list.add(element);
	}

	public void setProperty(SageProperty property) {
		this.property = property;
	}

	public SageProperty getProperty() {
		return property;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PropertyList[");
		if (property != null)
			sb.append("Property: ").append(property.value()).append("");
		sb.append(": Data: ").append(factory.fromList(this));
		sb.append("]");
		return sb.toString();
	}
}
