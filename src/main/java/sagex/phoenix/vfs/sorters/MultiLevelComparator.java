package sagex.phoenix.vfs.sorters;

import java.util.Comparator;
import java.util.List;

/**
 * Given a list of sorters, compare each, in order until a non-zero
 * comparison is made
 * 
 * @author seans
 * 
 * @param <T>
 */
public class MultiLevelComparator<T> implements Comparator<T> {
	private List<Comparator<T>> sorters = null;
	public MultiLevelComparator(List<Comparator<T>> sorters) {
		this.sorters = sorters;
	}
	
	@Override
	public int compare(T o1, T o2) {
		for (Comparator<T> c: sorters) {
			int v = c.compare(o1, o2);
			if (v!=0) return v;
		}
		return 0;
	}
}
