package sagex.phoenix.remote.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptException;

import sagex.phoenix.util.PhoenixScriptEngine;

public class JSMethodInvocationHandler implements InvocationHandler {
    private PhoenixScriptEngine eng;
    private Map<String, String> methodMap = new HashMap<String, String>();

    public JSMethodInvocationHandler(PhoenixScriptEngine eng, String interfaceMethod, String jsMethod) {
        this.eng = eng;
        methodMap.put(interfaceMethod, jsMethod);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        String jsMethod = methodMap.get(method.getName());
        if (jsMethod == null) {
            throw new NoSuchMethodException("No Javascript Method for " + method.getName());
        }

        Invocable inv = (Invocable) eng.getEngine();
        try {
            return inv.invokeFunction(jsMethod, args);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("The Java Method: " + method.getName() + " maps to a Javascript Method " + jsMethod
                    + " that does not exist.");
        } catch (ScriptException e) {
            throw e;
        }
    }
}
