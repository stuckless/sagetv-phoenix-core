package sagex.phoenix.event;

import sage.SageTVEventListener;
import sagex.phoenix.util.Loggers;

import java.lang.reflect.Method;
import java.util.Map;

public class ReflectionEventListener implements SageTVEventListener {
    private Object parent;
    private Method method;

    public ReflectionEventListener(Object parent, Method method) {
        this.parent = parent;
        this.method = method;
    }

    @Override
    public void sageEvent(String name, Map args) {
        Class cls[] = method.getParameterTypes();
        method.setAccessible(true);
        try {
            if (cls.length == 0) {
                method.invoke(parent, (Object[]) null);
            } else {
                if (cls.length == 2) {
                    method.invoke(parent, name, args);
                } else {
                    if (cls[0] == String.class) {
                        method.invoke(parent, name);
                    } else {
                        method.invoke(parent, args);
                    }
                }
            }
        } catch (Exception ex) {
            Loggers.LOG.warn("Failed to dispatch event for: " + name + " to method " + method.getName() + " in class "
                    + parent.getClass().getName(), ex);
        }
    }
}
