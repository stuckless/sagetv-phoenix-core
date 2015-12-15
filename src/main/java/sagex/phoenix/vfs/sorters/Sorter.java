package sagex.phoenix.vfs.sorters;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.ConfigList;
import sagex.phoenix.vfs.util.HasOptions;

/**
 * A Configurable Sorter for Views
 *
 * @author seans
 */
public class Sorter extends BaseConfigurable implements Comparator<IMediaResource>, Cloneable, HasLabel, HasName {
    /**
     * Sort order option name.
     * <p/>
     * value can be 'asc' or 'desc' * * {@value}
     */
    public final static String OPT_SORT_ORDER = "sort-order";
    /**
     * Folders First option name * {@value}
     */
    public final static String OPT_FOLDERS_FIRST = "folders-first";

    /**
     * Sort Ascending value * {@value}
     */
    public final static String SORT_ASC = "asc";

    /**
     * Sort decending value * {@value}
     */
    public static final String SORT_DESC = "desc";

    private Comparator<IMediaResource> comparator = null;
    private boolean foldersFirst = true;
    private boolean ascending = true;

    private String name, label;

    private Set<String> tags = new TreeSet<String>();

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Sorter(Comparator<IMediaResource> sorter) {
        this.comparator = sorter;
        addOption(new ConfigurableOption(OPT_SORT_ORDER, "Sort Order", SORT_ASC, DataType.string, true, ListSelection.single,
                "asc:Ascending,desc:Descending"));
        addOption(ConfigList.BooleanOption(OPT_FOLDERS_FIRST, "Folders First", true));
        if (sorter instanceof HasOptions) {
            for (ConfigurableOption co : ((HasOptions) sorter).getOptions()) {
                addOption(co);
            }
        }
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (isChanged()) {
            updateLocalVariables();
        }

        int comp = 0;
        if (foldersFirst) {
            if (o1 instanceof IMediaFolder && o2 instanceof IMediaFolder) {
                comp = comparator.compare(o1, o2);
                if (!ascending) {
                    comp = comp * -1;
                }
            } else if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
                comp = comparator.compare(o1, o2);
                if (!ascending) {
                    comp = comp * -1;
                }
            } else if (o1 instanceof IMediaFolder && o2 instanceof IMediaFile) {
                comp = -1;
            } else if (o2 instanceof IMediaFolder && o1 instanceof IMediaFile) {
                comp = 1;
            } else {
                comp = 0;
            }
        } else {
            comp = comparator.compare(o1, o2);
            if (!ascending) {
                comp = comp * -1;
            }
        }

        return comp;
    }

    private void updateLocalVariables() {
        ascending = SORT_ASC.equals(getOption(OPT_SORT_ORDER).getString(SORT_ASC));
        foldersFirst = getOption(OPT_FOLDERS_FIRST).getBoolean(true);
        if (comparator instanceof HasOptions) {
            ((HasOptions) comparator).onUpdate(this);
        }
        onUpdate();
        setChanged(false);
    }

    protected void onUpdate() {
    }

    public Comparator<IMediaResource> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<IMediaResource> comparator) {
        this.comparator = comparator;
    }

    public boolean isFoldersFirst() {
        return foldersFirst;
    }

    public boolean isAscending() {
        return ascending;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
