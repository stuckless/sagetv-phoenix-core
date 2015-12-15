package sagex.phoenix.cache;

public interface ICache<T> {
    public T get(Object id);

    public void put(Object id, T o);

    public int size();

    public int hits();

    public int misses();

    public void clear();

    public void remove(Object id);

    public void ejectStaleItems();
}
