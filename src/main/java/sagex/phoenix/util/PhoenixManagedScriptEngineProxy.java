package sagex.phoenix.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy Class that enables an interface to be backed by a script.
 * 
 * @author sls
 * 
 * @param <T>
 */
public class PhoenixManagedScriptEngineProxy<T> implements InvocationHandler {
	private PhoenixManagedScriptEngine<T> engine = null;

	public PhoenixManagedScriptEngineProxy(File file, Class<T> type) {
		engine = new PhoenixManagedScriptEngine<T>(file, type);
	}

	@Override
	public Object invoke(Object sourcObject, Method method, Object[] methodArgs) throws Throwable {
		return method.invoke(engine.getObjectInstance(), methodArgs);
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(File file, Class<T> type) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(PhoenixManagedScriptEngineProxy.class.getClassLoader(),
				new Class[] { type }, new PhoenixManagedScriptEngineProxy<T>(file, type));
	}
}
