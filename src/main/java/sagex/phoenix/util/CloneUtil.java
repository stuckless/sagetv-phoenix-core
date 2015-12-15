package sagex.phoenix.util;

import java.util.ArrayList;
import java.util.List;

public class CloneUtil {
    /**
     * Deep clones the items in a list. List items must implement
     * {@link PublicCloneable} which is a the same as {@link Cloneable} except
     * the method is declared public
     *
     * @param in input list
     * @return new list
     * @throws CloneNotSupportedException
     */
    public static <T extends PublicCloneable> List<T> cloneList(List<T> in) throws CloneNotSupportedException {
        List<T> out = new ArrayList<T>();

        for (T t : in) {
            out.add((T) t.clone());
        }

        return out;
    }
}
