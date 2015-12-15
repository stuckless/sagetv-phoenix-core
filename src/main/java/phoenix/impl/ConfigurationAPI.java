package phoenix.impl;

import org.apache.commons.lang.BooleanUtils;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.*;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.NamedValue;

import java.util.List;

/**
 * STV Api Calls related to the Configuration API
 *
 * @author seans
 */
@API(group = "config")
public class ConfigurationAPI {
    /**
     * Returns the Configured Property value for the client scope, or a default
     * value, if the property is not set.
     *
     * @param key      Configuration Key of the property
     * @param defValue Default Value to use if the property is not set.
     * @return property value if it is set, or defValue if it is not set
     * @throws RuntimeException if a property key is not configured and strict mode is
     *                          enabled
     */
    public String GetProperty(String key, Object defValue) {
        return Phoenix.getInstance().getConfigurationManager()
                .getClientProperty(key, (defValue == null) ? null : String.valueOf(defValue));
    }

    /**
     * Same as calling GetProperty(key, null)
     *
     * @param key property key or {@link IConfigurationElement}
     * @return property value
     */
    public String GetProperty(Object key) {
        if (key instanceof IConfigurationElement) {
            return GetProperty(((IConfigurationElement) key).getId(), null);
        } else {
            return GetProperty(String.valueOf(key), null);
        }
    }

    /**
     * Returns the Configured Property value for the server's scope, or a
     * default value, if the property is not set.
     *
     * @param key      Configuration Key of the property
     * @param defValue Default Value to use if the property is not set.
     * @return property value if it is set, or defValue if it is not set
     * @throws RuntimeException if a property key is not configured and strict mode is
     *                          enabled
     */
    public String GetServerProperty(String key, Object defValue) {
        return Phoenix.getInstance().getConfigurationManager()
                .getServerProperty(key, (defValue == null) ? null : String.valueOf(defValue));
    }

    /**
     * Same as calling GetProperty(key, null)
     *
     * @param key property key or {@link IConfigurationElement}
     * @return property value
     */
    public String GetServerProperty(Object key) {
        if (key instanceof IConfigurationElement) {
            return GetServerProperty(((IConfigurationElement) key).getId(), null);
        } else {
            return GetServerProperty(String.valueOf(key), null);
        }
    }

    /**
     * Returns the Configured Property value for the users's scope, or a default
     * value, if the property is not set.
     *
     * @param key      Configuration Key of the property
     * @param defValue Default Value to use if the property is not set.
     * @return property value if it is set, or defValue if it is not set
     * @throws RuntimeException if a property key is not configured and strict mode is
     *                          enabled
     */
    public String GetUserProperty(String key, Object defValue) {
        if (defValue == null) {
            return Phoenix.getInstance().getConfigurationManager().getUserProperty(key, null);
        } else {
            return Phoenix.getInstance().getConfigurationManager().getUserProperty(key, String.valueOf(defValue));
        }
    }

    /**
     * Same as calling GetProperty(key, null)
     *
     * @param key property key or {@link IConfigurationElement}
     * @return property value
     */
    public String GetUserProperty(Object key) {
        if (key instanceof IConfigurationElement) {
            return GetUserProperty(((IConfigurationElement) key).getId(), null);
        } else {
            return GetUserProperty(String.valueOf(key), null);
        }
    }

    /**
     * Sets the given property key to the given value for the client scope
     * <p/>
     * Setting a null property value will unset a property
     *
     * @param key   property key or {@link IConfigurationElement}
     * @param value property value
     */
    public void SetProperty(Object key, Object value) {
        if (key instanceof IConfigurationElement) {
            Phoenix.getInstance().getConfigurationManager()
                    .setProperty(((IConfigurationElement) key).getId(), (value == null) ? null : String.valueOf(value));
        } else {
            Phoenix.getInstance().getConfigurationManager()
                    .setProperty(String.valueOf(key), (value == null) ? null : String.valueOf(value));
        }
    }

    /**
     * Sets the given property key to the given value for the server scope
     * <p/>
     * Setting a null property value will unset a property
     *
     * @param key   property key or {@link IConfigurationElement}
     * @param value property value
     */
    public void SetServerProperty(Object key, Object value) {
        if (key instanceof IConfigurationElement) {
            Phoenix.getInstance().getConfigurationManager()
                    .setServerProperty(((IConfigurationElement) key).getId(), (value == null) ? null : String.valueOf(value));
        } else {
            Phoenix.getInstance().getConfigurationManager()
                    .setServerProperty(String.valueOf(key), (value == null) ? null : String.valueOf(value));
        }
    }

    /**
     * Sets the given property key to the given value for the user scope
     * <p/>
     * Setting a null property value will unset a property
     *
     * @param key   property key or {@link IConfigurationElement}
     * @param value property value
     */
    public void SetUserProperty(Object key, Object value) {
        if (key instanceof IConfigurationElement) {
            Phoenix.getInstance().getConfigurationManager()
                    .setUserProperty(((IConfigurationElement) key).getId(), (value == null) ? null : String.valueOf(value));
        } else {
            Phoenix.getInstance().getConfigurationManager()
                    .setUserProperty(String.valueOf(key), (value == null) ? null : String.valueOf(value));
        }
    }

    /**
     * Returns the Top level Configuration Groups. Think of Configuration Groups
     * like a Folder Structure. The method call always returns the Groups of the
     * root node.
     *
     * @return Array of IConconfigureElement nodes
     */
    public IConfigurationElement[] GetConfigurationGroups() {
        return Phoenix.getInstance().getConfigurationMetadataManager().getParentGroups();
    }

    /**
     * Returns the top level configuration group. This group contains the
     * configurated application groups in the configuration metadata system.
     *
     * @return {@link Group} Root Configuration Group
     */
    public Group GetConfigurationRoot() {
        return Phoenix.getInstance().getConfigurationMetadataManager().getMetadata();
    }

    /**
     * Returns the Configuration Group for the given group id
     *
     * @param groupId Group Id for the configuration
     * @return {@link Group} or null if there is no group.
     */
    public Group GetConfiguration(String groupId) {
        return (Group) GetConfigurationRoot().findElement(groupId);
    }

    /**
     * Returns the configuration metadata for the given property key or fieldId
     *
     * @param fieldId field's id (ie, property name)
     * @return Field
     */
    public Field GetField(String fieldId) {
        return (Field) GetConfigurationRoot().findElement(fieldId);
    }

    /**
     * return the {@link IConfigurationElement} for the given key. ie, this may
     * return a {@link Group} or a {@link Field}.
     *
     * @param key
     * @return
     */
    public IConfigurationElement GetElement(String key) {
        return GetConfigurationRoot().findElement(key);
    }

    /**
     * Returns the Parent Group to which this element belongs, or null, if there
     * is no parent.
     *
     * @param el {@link IConfigurationElement} configuration element (field or
     *           group)
     * @return parent {@link Group} or null if there is no parent.
     */
    public Group GetConfigurationParent(IConfigurationElement el) {
        if (el == null)
            return null;
        return (Group) el.getParent();
    }

    /**
     * Get all Configuration Elements of the given group. A Configuration
     * Element can either be a Group or Field.
     *
     * @param parent Parent Group from which you want to get the Configuration
     *               Elements
     * @return Array of IConfigurationElement nodes, or null if there are no
     * children, or if the parent is null
     */
    public IConfigurationElement[] GetConfigurationChildren(Group parent) {
        if (parent == null) {
            return null;
        }
        return parent.getVisibleItems();
    }

    /**
     * Returns true of the element is a Group Element. You can think of Group
     * elements like Folders. Group Elements contain Children that can either be
     * another Group or a Field
     *
     * @param e IConfigurationElement that you want to test
     * @return true if the element is a Group
     */
    public boolean IsConfigurationGroup(IConfigurationElement e) {
        if (e == null)
            return false;
        return e.getElementType() == IConfigurationElement.GROUP || e.getElementType() == IConfigurationElement.APPLICATION;
    }

    /**
     * Returns true if the Configuration Element is a top level application
     * element. Application Elements are Also Group Elements, but they are a
     * special type of Group. Application Elements are basically the parent of a
     * collection of related groups. ie, "sage" would be an Application Element
     * that contains all the configuration items for SageTV. "phoenix" would be
     * an Application Element that contains all the configuration items for
     * Phoenix.
     *
     * @param e IConfigurationElement that you are testing
     * @return true if this element is an Application Element
     */
    public boolean IsConfigurationApplication(IConfigurationElement e) {
        if (e == null)
            return false;
        return e.getElementType() == IConfigurationElement.APPLICATION;
    }

    /**
     * Returns true if this element is a Configuration Field. A Configuration
     * Field is a child of a Group and contains information about the
     * configurable item. It holds information such as it's default value, field
     * type, configuration key, etc.
     *
     * @param e {@link IConfigurationElement} that you are testing
     * @return true if this element is a Field
     */
    public boolean IsConfigurationField(IConfigurationElement e) {
        if (e == null)
            return false;
        return e.getElementType() == IConfigurationElement.FIELD;
    }

    /**
     * Returns the localized Label for the Configuration Element. This can be an
     * Application, Group, of Field.
     *
     * @param g Configuration Element from which you want to get a Localized
     *          Label
     * @return Localized label for the given Configuration Element
     */
    public String GetConfigurationLabel(IConfigurationElement g) {
        if (g == null)
            return "";
        return g.getLabel();
    }

    /**
     * Returns the localized Description for the Configuration Element. This can
     * be an Application, Group, of Field.
     *
     * @param g Configuration Element from which you want to get a Localized
     *          Description
     * @return Localized description for the given Configuration Element
     */
    public String GetConfigurationDescription(IConfigurationElement g) {
        if (g == null)
            return "";
        return g.getDescription();
    }

    /**
     * Returns the configuration element type as defined in {@link ConfigType}
     *
     * @param e {@link IConfigurationElement}
     * @return field's type as a string value
     */
    public String GetConfigurationFieldType(IConfigurationElement e) {
        if (e == null)
            return ConfigType.toCommonName(ConfigType.TEXT);
        return ConfigType.toCommonName(((Field) e).getType());
    }

    /**
     * Returns the field's default value as a String. A field's default value is
     * determined from the defaultValue attribute of the field in the
     * configuration metadata xml
     *
     * @param e {@link IConfigurationElement}
     * @return default value of the element as a String
     */
    public String GetConfigurationDefaultValue(IConfigurationElement e) {
        if (e == null)
            return "";
        return ((Field) e).getDefaultValue();
    }

    /**
     * Return the unique key that identifies this Configuration Field. This is
     * the key that this Field uses in it's property file. All
     * {@link IConfigurationElement}'s must be unique the system.
     *
     * @param e {@link IConfigurationElement}
     * @return Key for this Element
     */
    public String GetConfigurationKey(IConfigurationElement e) {
        if (e == null)
            return "";
        return e.getId();
    }

    /**
     * This is a debug method that can be used to reload the configuration and
     * configuration metadata without requiring a complete SageTV restart. The
     * main use for this would be when you are developing in the STV and you
     * need a new property, you can edit the metadata xml and then reload the
     * configuration so that you can carry forward and use that new
     * configuration item without have to restart SageTV.
     * <p/>
     * This API call will probably be hidden once the project is released.
     */
    public void ReloadConfiguration() {
        Phoenix.getInstance().getConfigurationMetadataManager().loadConfigurations();
        Phoenix.getInstance().getConfigurationManager().reload();
    }

    /**
     * Convenience Method for testing if a Configuration Item is the
     * "New Search" group item.
     *
     * @param el {@link IConfigurationElement}
     * @return true if the item is the "New Search" configuration group item
     */
    public boolean IsConfigurationNewSearchButton(IConfigurationElement el) {
        return el instanceof NewSearchGroup;
    }

    /**
     * Convenience Method for testing if a Configuration Item is a Search
     * Results group item.
     *
     * @param el {@link IConfigurationElement}
     * @return true if the item is a search result group
     */
    public boolean IsConfigurationSearchResult(IConfigurationElement el) {
        return el instanceof SearchResultGroup;
    }

    /**
     * Sets/Re-Sets the Search Criteria for a SearchResult Group
     *
     * @param text Search Criteria
     * @param el   {@link IConfigurationElement}
     */
    public void UpdateConfigurationSearch(String text, IConfigurationElement el) {
        if (IsConfigurationSearchResult(el)) {
            ((SearchResultGroup) el).setSearchString(text);
        }
    }

    /**
     * Add a New Search Result to the Search parent
     *
     * @param searchCriteria What to search on
     * @return SearchResultGroup containing the item results
     */
    public Group AddConfigurationSearch(String searchCriteria) {
        return NewSearchGroup.newSearch(searchCriteria);
    }

    /**
     * Remove a Search Result Group from the Search Tab
     *
     * @param result {@link IConfigurationElement}
     */
    public void RemoveConfigurationSearch(IConfigurationElement el) {
        NewSearchGroup.removeSearch((SearchResultGroup) el);
    }

    /**
     * Return true if the configuration group has children
     *
     * @param group
     * @return
     */
    public boolean HasConfigurationChildren(IConfigurationElement group) {
        if (group == null) {
            return false;
        }

        if (group instanceof Group) {
            IConfigurationElement children[] = GetConfigurationChildren((Group) group);
            return children != null && children.length > 0;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the configuration element is a boolean type
     *
     * @param el
     * @return
     */
    public boolean IsBoolean(IConfigurationElement el) {
        return IsType(el, ConfigType.BOOL.name());
    }

    /**
     * Return true if the configuration element is a number (ie, int, float,
     * etc)
     *
     * @param el
     * @return
     */
    public boolean IsNumber(IConfigurationElement el) {
        return IsType(el, ConfigType.NUMBER.name());
    }

    /**
     * Returns true if the configuration element is text or string
     *
     * @param el
     * @return
     */
    public boolean IsText(IConfigurationElement el) {
        return IsType(el, ConfigType.TEXT.name());
    }

    /**
     * Returns true if the configuration element is a Button.
     *
     * @param el
     * @return
     */
    public boolean IsButton(IConfigurationElement el) {
        return IsType(el, ConfigType.BUTTON.name());
    }

    /**
     * Returns true if the configuration is a single choice element. You can
     * call GetOptions(el) to get a list of possible options.
     *
     * @param el
     * @return
     */
    public boolean IsChoice(IConfigurationElement el) {
        return IsType(el, ConfigType.CHOICE.name());
    }

    /**
     * Returns true if the configuration element is a directory
     *
     * @param el
     * @return
     */
    public boolean IsDirectory(IConfigurationElement el) {
        return IsType(el, ConfigType.DIRECTORY.name());
    }

    /**
     * Returns true if the configuration element is a regular file
     *
     * @param el
     * @return
     */
    public boolean IsFile(IConfigurationElement el) {
        return IsType(el, ConfigType.FILE.name());
    }

    /**
     * Returns true if the configuration element is a MultiChoice or Multi
     * Select option. You can call GetOption(el) to get a list of possible multi
     * choice options.
     *
     * @param el
     * @return
     */
    public boolean IsMultiChoice(IConfigurationElement el) {
        return IsType(el, ConfigType.MULTICHOICE.name());
    }

    /**
     * Returns true if the type is a password. Password fields should be masked
     * out and are stored as Text.
     *
     * @param el
     * @return
     */
    public boolean IsPassword(IConfigurationElement el) {
        return IsType(el, ConfigType.PASSWORD.name());
    }

    /**
     * Generic type check method that relies on the {@link ConfigType} for
     * possible types.
     *
     * @param el
     * @param type
     * @return
     */
    public boolean IsType(IConfigurationElement el, String type) {
        ConfigType t = ConfigType.toConfigType(type);
        return (el != null && el instanceof Field && t == ((Field) el).getType());
    }

    /**
     * For MultiChoice type fields, this is the list separator that should be
     * used when storing multiple selections.
     *
     * @param f
     * @return
     */
    public String GetListSeparator(Field f) {
        return f.getListSeparator();
    }

    /**
     * Returns true if the configuration field has possible selection options.
     * For boolean types this will also return true, and you will have 2
     * possible options for "true" and "false".
     *
     * @param f
     * @return
     */
    public boolean HasOptions(Field f) {
        List opts = GetOptions(f);
        return opts != null && opts.size() > 0;
    }

    /**
     * Returns the Options for a given field. May be null or empty. For booleans
     * this will also return list of 2 items for "true" and "false"
     *
     * @param f
     * @return
     */
    public List GetOptions(Field f) {
        return f.getOptions();
    }

    /**
     * For a given {@link NamedValue} option return the option's label.
     *
     * @param option
     * @return
     */
    public String GetOptionLabel(NamedValue option) {
        return option.getName();
    }

    /**
     * For a given {@link NamedValue} option return the option's value.
     *
     * @param option
     * @return
     */
    public String GetOptionValue(NamedValue option) {
        return option.getValue();
    }

    /**
     * A convenience method to lookup an option's Label for a given value. For
     * example, you can find the label for a boolean field using
     * GetOptionLable(field, "true", "OK").
     *
     * @param field    Configuration {@link Field}
     * @param value    option value
     * @param defLabel default label to return if the option's label is null, or the
     *                 option does not exist.
     * @return
     */
    public String GetOptionLabel(Field field, Object value, String defLabel) {
        value = (value == null) ? null : String.valueOf(value);
        List<NamedValue> opts = field.getOptions();
        if (opts != null) {
            for (NamedValue nv : opts) {
                if (value.equals(nv.getValue())) {
                    return nv.getName();
                }
            }
        }
        return defLabel;
    }

    /**
     * Gets the {@link NamedValue} for the given value
     *
     * @param field
     * @param value
     * @return
     */
    public NamedValue GetOption(Field field, Object value) {
        value = (value == null) ? null : String.valueOf(value);
        List<NamedValue> opts = field.getOptions();
        if (opts != null) {
            for (NamedValue nv : opts) {
                if (value.equals(nv.getValue())) {
                    return nv;
                }
            }
        }
        return null;
    }

    /**
     * Returns the range of options for the given field, or null, if no options
     * are provided.
     *
     * @param field
     * @return
     */
    public String[] GetOptionRange(Field field) {
        List<NamedValue> opts = field.getOptions();
        if (opts != null && opts.size() > 0) {
            return new String[]{opts.get(0).getValue(), opts.get(opts.size() - 1).getValue()};
        }
        return null;
    }

    /**
     * Returns true the the option's value is in the field's curValue list. ie,
     * The option is selected because the field's current value is the same as
     * the option. This should work for both Single Choice and Multi Choice
     * options. For example, if the fields's current value is "12;24"
     *
     * @param field
     * @param curValue
     * @param option
     * @return
     */
    public boolean IsSelected(NamedValue option, Field field, Object curValue) {
        if (curValue == null || field == null || option == null)
            return false;
        curValue = String.valueOf(curValue);
        String values[] = null;
        if (IsMultiChoice(field)) {
            values = ((String) curValue).split(field.getListSeparator());
        } else {
            values = new String[]{(String) curValue};
        }
        for (String v : values) {
            if (v.equals(option.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a List of NamedValue object, that can be used as configuration
     * option choices. The data can be a list, map, array, etc, and it will
     * convert to the List of NamedValue objects.
     *
     * @param data
     * @return
     */
    public List<NamedValue> CreateOptionList(Object data) {
        return ConfigUtils.getOptions(data);
    }

    /**
     * Same as Toggle(Field) except uses the property Key.
     *
     * @param prop
     */
    public void Toggle(String prop) {
        Toggle(GetField(prop));
    }

    /**
     * For a boolean field it will toggle between true and false. For Choice
     * options, it will toggle to next value in the choice. ie, if the choices
     * are "1", "2", and "3", then calling Toggle when the value is "2" will set
     * the value to "3", and calling Toggle when the value is "3" will set the
     * value to "1".
     * <p/>
     * If field can't be toggled then nothing happens.
     *
     * @param f Field to toggle
     */
    public void Toggle(Field f) {
        if (f.getType() == ConfigType.BOOL) {
            SetProperty(f, !BooleanUtils.toBoolean(GetProperty(f)));
        } else if (HasOptions(f)) {
            String value = GetProperty(f);
            List<NamedValue> opts = f.getOptions();
            int pos = -1;
            for (int i = 0; i < opts.size(); i++) {
                NamedValue nv = opts.get(i);
                if (nv.getValue().equals(value)) {
                    pos = i + 1;
                    break;
                }
            }
            pos = pos % (opts.size());
            SetProperty(f, opts.get(pos).getValue());
        } else {
            // do nothing, we can't toggle
        }
    }
}
