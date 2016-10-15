package sagex.phoenix.factory;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;

public abstract class Factory<T> extends BaseConfigurable implements HasName, HasLabel {
    /**
     * If this factory has errors, then this is set to true
     */
    private boolean hasErrors=false;
    /**
     * if this factory has errors, then this MIGHT be set to an error message
     */
    private String errorMessage=null;

    /**
     * Visible option name * {@value}
     */
    public static final String OPT_VISIBLE = "visible";

    /**
     * Name option name * {@value}
     */
    public static final String OPT_NAME = "name";

    /**
     * label option name * {@value}
     */
    public static final String OPT_LABEL = "label";

    /**
     * description option name
     */
    public static final String OPT_DESCRIPTION = "description";

    protected Set<String> tags = new TreeSet<String>();

    public Factory() {
        super();
        addOption(new ConfigurableOption(OPT_NAME, "Name", null, DataType.string));
        addOption(new ConfigurableOption(OPT_LABEL, "Label", null, DataType.string));
        addOption(new ConfigurableOption(OPT_DESCRIPTION, "Description", null, DataType.string));
        addOption(new ConfigurableOption(OPT_VISIBLE, "Visible", "true", DataType.bool, true, ListSelection.single,
                "true:Yes,false:No"));
    }

    public abstract T create(Set<ConfigurableOption> configurableOptions);

    public String getName() {
        return getOption(OPT_NAME).getString(null);
    }

    public void setName(String name) {
        getOption(OPT_NAME).value().setValue(name);
    }

    public String getLabel() {
        return getOption(OPT_LABEL).getString(null);
    }

    public String getDescription() {
        return getOption(OPT_DESCRIPTION).getString(null);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public Set<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        for (String t : tags) {
            if (t.equalsIgnoreCase(tag) || "*".equals(tag) || "*".equals(t)) {
                return true;
            }
        }
        return false;
    }

    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
    }

    public boolean isVisible() {
        return getOption(OPT_VISIBLE).getBoolean(true);
    }

    public void setVisible(boolean vis) {
        getOption(OPT_VISIBLE).value().set(true);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Factory<T> f = (Factory<T>) super.clone();
        f.tags = new TreeSet<String>(tags);
        return f;
    }

    public void setOptionValue(String optName, String value) {
        ConfigurableOption co = getOption(optName);
        if (co == null) {
            co = new ConfigurableOption(optName);
            addOption(co);
        }
        co.value().setValue(value);
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
