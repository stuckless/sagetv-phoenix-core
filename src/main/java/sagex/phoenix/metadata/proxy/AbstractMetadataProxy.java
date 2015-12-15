package sagex.phoenix.metadata.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.ISageMetadata;
import sagex.util.TypesUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Proxy class for Metadata. If you are simply storing your metadata as
 * strings then subclass this class and implement the required methods for
 * setting/getting metadata from your structure.
 *
 * @author seans
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMetadataProxy implements InvocationHandler, IPropertyListChangedListener, ISageMetadata {
    protected Logger log = Logger.getLogger(this.getClass());

    private Map<String, PropertyList> lists = new HashMap<String, PropertyList>();

    protected AbstractMetadataProxy() {
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return toString();
        } else if ("isSet".equals(method.getName())) {
            return isSet((SageProperty) args[0]);
        } else if ("clear".equals(method.getName())) {
            clear((SageProperty) args[0]);
            return null;
        } else if ("set".equals(method.getName())) {
            set((SageProperty) args[0], (String) args[1]);
            return null;
        } else if ("get".equals(method.getName())) {
            return get((SageProperty) args[0]);
        }

        SageProperty md = method.getAnnotation(SageProperty.class);
        if (md == null) {
            log.warn("Missing MD annotation on method: " + method.getName());
            return null;
        }

        String name = method.getName();
        if (name.startsWith("set")) {
            set(md, args[0]);
            return null;
        }

        if (name.startsWith("get") || name.startsWith("is")) {
            if (List.class.isAssignableFrom(method.getReturnType())) {
                PropertyList p = (PropertyList) lists.get(md.value());
                if (p == null) {
                    try {
                        // create the list
                        IPropertyListFactory factory = (IPropertyListFactory) Class.forName(md.listFactory()).newInstance();
                        p = factory.toList(get(md));
                        p.addListChangedListener(this);
                        p.setProperty(md);
                        lists.put(md.value(), p);
                    } catch (Throwable t) {
                        log.error("Failed to create ListFactory for List Property: " + md.value(), t);
                        throw t;
                    }
                }
                return p;
            }

            Object val = convertFromString(get(md), method);
            return val;
        }

        // account for non standard methods
        if (args != null && args.length > 0) {
            set(md, args[0]);
            return null;
        }

        return get(md);
    }

    public void clear(SageProperty sageProperty) {
        set(sageProperty, null);
    }

    protected Object convertFromString(String value, Method method) {
        return TypesUtil.fromString(value, method.getReturnType());
    }

    /**
     * returns true if the metadata is current set
     *
     * @param key
     * @return
     */
    public boolean isSet(SageProperty key) {
        return get(key) != null;
    }

    /**
     * sets the metadata as a string value
     *
     * @param key
     * @param value
     */
    public abstract void set(SageProperty key, String value);

    /**
     * gets the metadata as a string value
     *
     * @param key
     * @return
     */
    public abstract String get(SageProperty key);

    @Override
    public void propertyListChanged(PropertyList list) {
        set(list.getProperty(), list.getFactory().fromList(list));
    }

    public String toString() {
        return this.getClass().getName();
    }

    private void set(SageProperty prop, Object value) {
        String setVal = null;
        if (value != null) {
            setVal = TypesUtil.toString(value);
        }
        if (setVal == null)
            setVal = "";

        if (!prop.allowNULL() && StringUtils.isEmpty(setVal)) {
            if (!StringUtils.isEmpty(get(prop))) {
                RuntimeException re = new RuntimeException("Canot Set Null value for Sage Property: " + prop.value()
                        + "; Skipping Field");
                log.warn("Can't NULL to metadata field: " + prop.value(), re);
            }
        }

        set(prop, setVal);
    }
}
