package sagex.phoenix.factory;

import java.util.Comparator;

public class FactoryComparator implements Comparator<Factory<?>> {
    public static FactoryComparator INSTANCE = new FactoryComparator();

    public int compare(Factory<?> f1, Factory<?> f2) {
        if (f1==null && f2==null) return 0;
        if (f1==null) return -1;
        if (f2==null) return 1;
        if (f1.getClass().equals(f2.getClass())) {
            if (f1.getLabel()!=null && f2.getLabel()!=null) {
                // compare labels
                return f1.getLabel().compareTo(f2.getLabel());
            } else {
                // compare names
                return f1.getName().compareTo(f2.getName());
            }
        }
        return 1;
    }
}
