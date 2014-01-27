package sagex.phoenix.util.var;



/**
 * Variable that is bound to a configuration property.  Any sets are automatically reflected in the
 * property.
 * 
 * @author seans
 *
 * @param <T>
 */
public class ScopedConfigurationPropertyVariable<T> extends Variable<T> {
	public static enum Scope {Client, User, Server}

	private Scope scope;
	private String key;
	
	public ScopedConfigurationPropertyVariable(Scope scope, String key, Class<T> type) {
		super(type);
		this.scope=scope;
		this.key=key;
	}

	@Override
	public T get() {
		switch (scope) {
		case Client:
			return converter.toType(phoenix.config.GetProperty(key));
		case User:
			return converter.toType(phoenix.config.GetUserProperty(key));
		case Server:
			return converter.toType(phoenix.config.GetServerProperty(key));
		default:
			log.warn("Unhandled Scope: " + scope);
		}

		// default return
		return getConverter().toType(null);
	}
	
	@Override
	public void set(T value) {
		String val = getConverter().toString(value);
		
		switch (scope) {
		case Client:
			phoenix.config.SetProperty(key, val);
			break;
		case User:
			phoenix.config.SetUserProperty(key, val);
			break;
		case Server:
			phoenix.config.SetServerProperty(key, val);
			break;
		default:
			log.warn("Unhandled Scope: " + scope);
		}
	}

	public Scope getScope() {
		return scope;
	}

	public String getKey() {
		return key;
	}
}
