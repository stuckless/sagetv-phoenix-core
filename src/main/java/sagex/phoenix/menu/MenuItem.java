package sagex.phoenix.menu;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.var.DynamicVariable;

public class MenuItem implements IMenuItem {

    /**
     * {@value}
     */
    public static final String STORE_ID = "phoenix.menus";

    /**
     * {@value}
     */
    public static final String FIELD_VISIBLE = "visible";

    /**
     * {@value}
     */
    public static final String FIELD_ISDEFAULT = "isDefault";

    /**
     * {@value}
     */
    public static final String FIELD_LABEL = "label";

    /**
     * {@value}
     */
    public static final String VAR_CONTEXT = "gMenuItemContextVar";

    protected Logger log = Logger.getLogger(this.getClass());

    protected Menu parent;
    protected String name;
    protected String reference;

    protected DynamicVariable<String> background = new DynamicVariable<String>(String.class, null);
    protected DynamicVariable<String> label = new DynamicVariable<String>(String.class, null);
    protected DynamicVariable<Boolean> visible = new DynamicVariable<Boolean>(Boolean.class, "true");
    protected DynamicVariable<Boolean> isDefault = new DynamicVariable<Boolean>(Boolean.class, "false");
    protected DynamicVariable<String> icon = new DynamicVariable<String>(String.class, null);
    protected DynamicVariable<String> secondaryIcon = new DynamicVariable<String>(String.class, null);
    protected DynamicVariable<String> description = new DynamicVariable<String>(String.class, null);
    protected DynamicVariable<String> linkedMenuId = new DynamicVariable<String>(String.class, null);

    protected Map<String, DynamicVariable<String>> fields = new HashMap<String, DynamicVariable<String>>();

    protected List<Action> actions = new LinkedList<Action>();

    protected Object userData;

    public MenuItem(Menu parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()).append("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", label=").append(label);
        if (reference!=null) {
            sb.append(", reference='").append(reference).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }

    public boolean performActions() {
        return performActions(null);
    }

    public boolean performActions(Object context) {
        log.debug("Performing Menu item Actions for MenuItem: " + this);

        if (context!=null) {
            String ctx = "gMenuItemContext";
            DynamicVariable<String> ctxVar = field(VAR_CONTEXT);
            if (ctxVar == null && parent != null) {
                ctxVar = parent.field(VAR_CONTEXT);
            }
            if (ctxVar != null) {
                ctx = ctxVar.get();
            }

            // if we have a context name, then set the passed context object for the menu actions
            if (ctx != null) {
                SageAddStaticContextAction addCtx = new SageAddStaticContextAction(ctx, context);
                addCtx.invoke();
            }
        }

        boolean ok = true;
        for (Action a : actions) {
            if (!a.invoke()) {
                ok = false;
                break;
            }
        }
        return ok;
    }


    public void field(String name, String value) {
        fields.put(name, new DynamicVariable<String>(String.class, value));
    }

    public void field(String name, DynamicVariable<String> value) {
        fields.put(name, value);
    }

    public DynamicVariable<String> field(String name) {
        if (!fields.containsKey(name)) {
            field(name, (String) null);
        }
        return fields.get(name);
    }

    public Map<String, DynamicVariable<String>> getFields() {
        return fields;
    }

    public Menu getParent() {
        return parent;
    }

    public DynamicVariable<String> background() {
        return background;
    }

    public DynamicVariable<String> label() {
        return label;
    }

    public DynamicVariable<Boolean> visible() {
        return visible;
    }

    public DynamicVariable<Boolean> isDefault() {
        return isDefault;
    }

    public DynamicVariable<String> icon() {
        return icon;
    }

    public DynamicVariable<String> secondaryIcon() {
        return secondaryIcon;
    }

    public DynamicVariable<String> description() {
        return description;
    }

    public DynamicVariable<String> linkedMenuId() {
        return linkedMenuId;
    }

    public void setParent(Menu parent) {
        this.parent = parent;
    }

    @Override
    public String getId() {
        return getName();
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public void visit(INodeVisitor<IMenuItem> visitor) {
        visitor.visit(this);
    }

    @Override
    public String getReference() {
        return reference;
    }

    public boolean isReference() {
        return reference!=null && !reference.isEmpty();
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void updateFrom(IMenuItem item) {
        this.setName(item.getName());
        this.label().setValue(item.label().getValue());
        this.visible().setValue(item.visible().getValue());
        this.isDefault().setValue(item.isDefault().getValue());
        this.background().setValue(item.background().getValue());
        this.description().setValue(item.description().getValue());
        this.icon().setValue(item.icon().getValue());
        this.linkedMenuId().setValue(item.linkedMenuId().getValue());
        this.secondaryIcon().setValue(item.secondaryIcon().getValue());
        this.fields.putAll(((MenuItem)item).fields);
        this.actions.addAll(((MenuItem)item).actions);
    }
}
