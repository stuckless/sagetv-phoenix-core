package sagex.phoenix.menu;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple Action that holds a set of name/value pairs.  Implementations would need to know how to manage these.
 *
 * A Perform Action on these does nothing.
 */
public class NamedAction extends Action {
    Map<String, String> fields = new LinkedHashMap<>();

    public NamedAction() {
    }

    public String getName() {
        return action().get();
    }

    public void setName(String name) {
        action().set(name);
    }

    @Override
    public boolean invoke() {
        return true;
    }

    public void set(String name, String value) {
        fields.put(name, value);
    }

    public String get(String name) {
        return fields.get(name);
    }

    public Map<String,String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NamedAction{");
        sb.append("fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}
