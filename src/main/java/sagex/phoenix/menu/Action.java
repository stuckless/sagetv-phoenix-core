package sagex.phoenix.menu;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import sagex.phoenix.util.var.DynamicVariable;

public abstract class Action {
    protected static final Logger log = Logger.getLogger(Action.class);

    private String type;
    private DynamicVariable<String> action = new DynamicVariable<String>(String.class, null);

    public Action() {
        this.type=this.getClass().getSimpleName();
    }

    public DynamicVariable<String> action() {
        return action;
    }

    public abstract boolean invoke();

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
