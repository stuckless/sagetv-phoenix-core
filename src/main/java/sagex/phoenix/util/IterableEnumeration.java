package sagex.phoenix.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IterableEnumeration<E> implements Iterator<E> {
    private Enumeration<E> enumeration;

    public IterableEnumeration(Enumeration<E> e) {
        this.enumeration = e;
    }

    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    public E next() {
        return enumeration.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot Remove Elements");
    }
}
