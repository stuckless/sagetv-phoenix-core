package sagex.phoenix.metadata.proxy;

public interface HasListChangedListeners<T> {
	public void addListChangedListener(IPropertyListChangedListener<T> listener);
}
