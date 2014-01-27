package sagex.phoenix.vfs.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.util.CloneUtil;
import sagex.phoenix.util.PublicCloneable;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.sorters.MultiLevelComparator;
import sagex.phoenix.vfs.sorters.Sorter;

/**
 * View Presention organizes the sorts, filters, and groups for a given view level
 * 
 * @author seans
 *
 */
public class ViewPresentation implements PublicCloneable {
	private int level = 0;	
	
	private List<Filter> filters = new ArrayList<Filter>();
	private List<Sorter> sorters = new ArrayList<Sorter>();
	private List<Grouper> groupers = new ArrayList<Grouper>();
	private List<String> hints = new ArrayList<String>();

	/**
	 * Creates a ViewPresentation for the root level
	 */
	public ViewPresentation() {
		this(0);
	}

	/**
	 * Creates a ViewPresentation for a given level.  Root level, ie, top of the folder
	 * tree is 0.
	 * 
	 * @param level
	 */
	public ViewPresentation(int level) {
		this.setLevel(level);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ViewPresentation p = (ViewPresentation) super.clone();
		p.filters = CloneUtil.cloneList(filters);
		p.sorters = CloneUtil.cloneList(sorters);
		p.groupers = CloneUtil.cloneList(groupers);
		p.hints = new ArrayList<String>(hints);
		return p;
	}

	public boolean hasGroupers() {
		return groupers.size()>0;
	}

	public String getGroupName(IMediaResource r) {
		// TODO: Conditionally apply a grouper, that's really why we allow more than one
		String name = null;
		for (Grouper g: groupers) {
			name = g.getGroupName(r);
			if (!StringUtils.isEmpty(name)) break;
			
			name = g.getEmptyFolderName();
			if (!StringUtils.isEmpty(name)) break;
		}
		return name;
	}

	/**
	 * Returns true if any groups has prune singe item folders enabled
	 * 
	 * @return
	 */
	public boolean isPruningSingleItems() {
		for (Grouper g: groupers) {
			if (g.isPruningSingleItemFolders()) return true;
		}
		return false;
	}

	public List<String> getGroupNames(IMediaResource r) {
		List<String> groups = new ArrayList<String>();
		for (Grouper g: groupers) {
			boolean added=false;
			if (g.isMultiGrouper()) {
				List<String> list = g.getGroupNames(r);
				if (list.size()>0) {
					groups.addAll(list);
					added=true;
				}
			} else {
				String name = g.getGroupName(r);
				if (!StringUtils.isEmpty(name)) {
					groups.add(name);
					added=true;
				}
			}
			
			if (!added) {
				String name = g.getEmptyFolderName();
				if (!StringUtils.isEmpty(name)) {
					groups.add(name);
				}
			}
		}
		return groups;
	}
	
    public boolean canAccept(IMediaResource r) {
        if (filters.size() == 0) {
            return true;
        }

        for (Filter f : filters) {
            if (f.accept(r)) {
                return true;
            }
        }

        return false;
    }

	public boolean hasSorters() {
		return sorters.size()>0;
	}

	@SuppressWarnings("unchecked")
	public void sort(List<IMediaResource> set) {
		MultiLevelComparator mlc = new MultiLevelComparator(sorters);
		Collections.sort(set, mlc);
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public List<Sorter> getSorters() {
		return sorters;
	}

	public List<Grouper> getGroupers() {
		return groupers;
	}

	public Filter setFilter(Filter filter) {
		int pos = filters.indexOf(filter);
		if (pos==-1) {
			filters.add(filter);
		} else {
			filters.remove(pos);
			filters.add(pos, filter);
		}
		return filter;
	}

	/**
	 * A presentation can contain a hint, which can be used by the view renderer to
	 * render a specific presentation in a certain way.  For example, a tv folder
	 * may have views for group by series, group by season, an just episodes.  Each 
	 * view presentation in those views may contain hints that could be used by
	 * the renderer to show the series folders differently that the season grouped
	 * folders.
	 * 
	 * @return
	 */
	public List<String> getHints() {
		return hints;
	}
	
	/**
	 * Returns true if the presentation has the given rendering hint.
	 * 
	 * @param hint
	 * @return
	 */
	public boolean hasHint(String hint) {
		return hints.contains(hint);
	}

}