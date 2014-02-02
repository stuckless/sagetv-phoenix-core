package sagex.phoenix.util.var;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sagex.phoenix.util.var.ScopedConfigurationPropertyVariable.Scope;

/**
 * A Dynamic Variable will dynamically manage one of more other Variable types
 * by inspecting the value that is being set, and determing what the best
 * variable should be.
 * 
 * @author seans
 * 
 * @param <T>
 */
public class DynamicVariable<T> extends Variable<T> {
	private String value;
	private transient Variable<T> currentProperty;

	private transient Pattern propPattern = Pattern.compile("prop:([^:]*):(.*)", Pattern.CASE_INSENSITIVE);

	public DynamicVariable(Class<T> returnType) {
		this(returnType, null);
	}

	public DynamicVariable(Class<T> returnType, String value) {
		super(returnType);
		setValue(value);
	}

	public T get() {
		return currentProperty.get();
	}

	public void set(T value) {
		setValue(converter.toString(value));
	}

	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the dynamic variable. The value can be a formatted
	 * expressions, such as the following
	 * 
	 * <pre>
	 * "${SageExpression}" -- SageExpression is any valid sage expression that evaluates to a string, int, boolean, etc
	 * "prop::propertyKey" -- resolves the property value against a client
	 * "prop:client:propertyKey" -- resolves the property value against a client
	 * "prop:server:propertyKey" -- resolves the property value against the server
	 * "prop:user:propertyKey" -- resolves the property value against the current user
	 * "Some String" -- will set a static string value
	 * int|float|long|double -- will set a static numeric value (ie, 10, 10.5,etc)
	 * true|false -- will set a static boolean value to true or false
	 * 
	 * <pre>
	 * 
	 * NOTE: When you set the value to a property or expression, then get() return the computed value.
	 * if you need to get the actual value that was set in the {@link DynamicVariable} then you can
	 * use getValue()
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;

		if (value == null) {
			this.currentProperty = new Variable<T>(getType());
			return;
		}

		if (value.startsWith("${")) {
			this.currentProperty = new SageExpressionVariable<T>(value, null, getType());
		} else if (value.startsWith("prop:")) {
			Matcher m = propPattern.matcher(value);
			if (m.matches()) {
				String scope = m.group(1);
				String key = m.group(2);
				if (scope == null || scope.trim().length() == 0
						|| ScopedConfigurationPropertyVariable.Scope.Client.name().equalsIgnoreCase(scope)) {
					currentProperty = new ScopedConfigurationPropertyVariable<T>(Scope.Client, key, getType());
				} else if (ScopedConfigurationPropertyVariable.Scope.Server.name().equalsIgnoreCase(scope)) {
					currentProperty = new ScopedConfigurationPropertyVariable<T>(Scope.Server, key, getType());
				} else if (ScopedConfigurationPropertyVariable.Scope.User.name().equalsIgnoreCase(scope)) {
					currentProperty = new ScopedConfigurationPropertyVariable<T>(Scope.User, key, getType());
				} else {
					log.warn("invalid property scope: " + scope + " using client scope");
					currentProperty = new ScopedConfigurationPropertyVariable<T>(Scope.Client, key, getType());
				}
			} else {
				log.warn("Invalid Property Notation: " + value + "; Must be prop:[client:server:user]:key");
			}
		} else {
			currentProperty = new Variable<T>(getConverter().toType(value), getType());
		}
	}

	public Variable<T> getVariable() {
		return currentProperty;
	}

	/**
	 * Returns the value of the current Variable as string. If the value is
	 * null, then an empty string is returned.
	 * 
	 * This is to allow better use in the stv.
	 */
	public String toString() {
		return converter.toString(currentProperty.get());
	}
}
