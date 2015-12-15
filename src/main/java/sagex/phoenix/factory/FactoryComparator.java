package sagex.phoenix.factory;

import java.util.Comparator;

public class FactoryComparator implements Comparator<Factory<?>> {
    public static FactoryComparator INSTANCE = new FactoryComparator();

    public int compare(Factory<?> f1, Factory<?> f2) {
        if (f1 == null || f1.getLabel() == null)
            return -1;
        if (f1.getClass().equals(f2.getClass())) {
            if (f1.getLabel() == null)
                return 1;
            if (f2.getLabel() == null)
                return -1;
            return f1.getLabel().compareTo(f2.getLabel());
        }
        return 1;
    }
}
