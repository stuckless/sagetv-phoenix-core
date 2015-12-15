package sagex.phoenix.vfs.groups;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.HasOptions;

/**
 * Configurable Grouper base class
 *
 * @author seans
 */
public class Grouper extends BaseConfigurable implements IGrouper, IMultiGrouper, Cloneable, HasLabel, HasName {
    private IGrouper grouper = null;

    private String emptyFolderName;
    private boolean multiGrouper = false;
    private boolean pruneSingeItemFolders;

    private String label;

    private String factoryId;

    private Set<String> tags = new TreeSet<String>();

    /**
     * Empty Groupname option name * {@value}
     */
    public static final String OPT_EMPTY_GROUPNAME = "empty-foldername";

    /**
     * Empty Groupname option name * {@value}
     */
    public static final String OPT_PRUNE_SINGLE_ITEM = "prune-single-item-groups";

    public Grouper(IGrouper grouper) {
        super();
        if (grouper == null) {
            throw new RuntimeException("Can't create a Grouper using a Null Comparator!");
        }
        this.grouper = grouper;
        addOption(new ConfigurableOption(OPT_EMPTY_GROUPNAME, "Empty Group Name", null, DataType.string));
        addOption(new ConfigurableOption(OPT_PRUNE_SINGLE_ITEM, "Promote single item groups", null, DataType.bool, true,
                ListSelection.single, "true:Yes,no:No"));
        if (grouper instanceof HasOptions) {
            for (ConfigurableOption co : ((HasOptions) grouper).getOptions()) {
                addOption(co);
            }
        }
        multiGrouper = (grouper instanceof IMultiGrouper);
    }

    @Override
    public String getGroupName(IMediaResource res) {
        if (isChanged()) {
            updateLocalValues();
        }

        return grouper.getGroupName(res);
    }

    private void updateLocalValues() {
        emptyFolderName = getOption(OPT_EMPTY_GROUPNAME).getString(null);
        pruneSingeItemFolders = getOption(OPT_PRUNE_SINGLE_ITEM).getBoolean(false);
        onUpdate();
        if (grouper instanceof HasOptions) {
            ((HasOptions) grouper).onUpdate(this);
        }
        setChanged(false);
    }

    protected void onUpdate() {
    }

    public String getEmptyFolderName() {
        return emptyFolderName;
    }

    public boolean isPruningSingleItemFolders() {
        return pruneSingeItemFolders;
    }

    public boolean isMultiGrouper() {
        return multiGrouper;
    }

    @Override
    public List<String> getGroupNames(IMediaResource res) {
        if (isChanged()) {
            updateLocalValues();
        }

        if (multiGrouper) {
            return ((IMultiGrouper) grouper).getGroupNames(res);
        } else {
            return Collections.emptyList();
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setFactoryId(String name) {
        this.factoryId = name;
    }

    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public String getName() {
        return factoryId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
