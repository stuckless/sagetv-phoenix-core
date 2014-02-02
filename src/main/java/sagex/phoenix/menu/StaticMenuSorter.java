package sagex.phoenix.menu;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class StaticMenuSorter implements Comparator<IMenuItem> {
	private Map<String, Integer> sortMap = new HashMap<String, Integer>();

	public StaticMenuSorter(Menu menu, String sorts) {
		if (StringUtils.isEmpty(sorts)) {
			return;
		}

		String ids[] = sorts.split("\\s*,\\s*");
		for (String id : ids) {
			String parts[] = id.split("\\s*:\\s*");
			if (parts.length == 2) {
				sortMap.put(parts[0], NumberUtils.toInt(parts[1], Integer.MAX_VALUE));
			}
		}
	}

	private int getSort(String name) {
		Integer i = sortMap.get(name);
		// integer.maxvalue ensures that the unknown elements get pushed to the
		// end.
		if (i == null)
			i = Integer.valueOf(Integer.MAX_VALUE);
		return i;
	}

	@Override
	public int compare(IMenuItem o1, IMenuItem o2) {
		int i1 = getSort(o1.getName());
		int i2 = getSort(o2.getName());

		if (i1 < i2) {
			return -1;
		}

		if (i1 > i2) {
			return 1;
		}

		return 0;
	}
}
