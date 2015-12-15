package sagex.phoenix.util;

import java.util.*;

public class SortedProperties extends Properties {
    public SortedProperties() {
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        Set s = new TreeSet();
        for (Enumeration e = super.keys(); e.hasMoreElements(); ) {
            s.add(e.nextElement());
        }

        Vector v = new Vector(s);
        return v.elements();
    }
}
