package sagex.phoenix.configuration.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class GroupProxy {
    private static final Logger log = Logger.getLogger(GroupProxy.class);
    protected String groupPath = null;

    public GroupProxy() {
        AGroup grp = this.getClass().getAnnotation(AGroup.class);
        groupPath = grp.path();
    }
    
    protected void init() {
        for (Field f : this.getClass().getDeclaredFields()) {
        	if (Modifier.isStatic(f.getModifiers())) continue;
            AField fld = f.getAnnotation(AField.class);
            if (fld != null) {
                String name = fld.name();
                if (name.equals(AField.USE_FIELD_NAME)) {
                    name = f.getName();
                }
                try {
                    String key = null;
                    if (fld.fullKey().equals(AField.USE_PARENT_GROUP)) {
                        key = getGroupPath() + "/" + name;
                    } else {
                        key = fld.fullKey();
                    }
                    // set the key
                    Method m = f.getType().getMethod("setKey", String.class);
                    f.setAccessible(true);
                    m.setAccessible(true);
                    m.invoke(f.get(this), key);
                } catch (Exception e) {
                    log.warn("Failed to initialize GroupProxy!", e);
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Field does not have AField annotiation");
            }
        }
    }

    public String getGroupPath() {
        return groupPath;
    }

    private static Map<Class, Object> groups = new HashMap<Class, Object>();

    public static <T extends GroupProxy> T get(Class<T> cls) {
        T group = (T) groups.get(cls);
        if (group == null) {
            try {
                group = cls.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            groups.put(cls, group);
        }
        return group;
    }
}
