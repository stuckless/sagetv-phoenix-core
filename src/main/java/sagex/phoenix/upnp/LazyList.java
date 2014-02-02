package sagex.phoenix.upnp;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class LazyList<E> extends AbstractList<E> {
	private List<E> list = new ArrayList<E>();
	private boolean loaded = false;

	public LazyList() {
		list.add(pleaseWaitItem());
	}

	@Override
	public E get(int arg0) {
		if (!loaded) {
			populateList();
		}
		return list.get(arg0);
	}

	protected void populateList() {
		// if we've already don this, then ignore the request
		if (loaded)
			return;
		loadList(this);
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	@Override
	public int size() {
		return list.size();
	}

	protected abstract E pleaseWaitItem();

	protected abstract void loadList(LazyList<E> list);

	@Override
	public void clear() {
		list.clear();
	}

	public void add(int index, E element) {
		list.add(index, element);
	}

	public E remove(int index) {
		return list.remove(index);
	}

	public E set(int index, E element) {
		return list.set(index, element);
	}
}
