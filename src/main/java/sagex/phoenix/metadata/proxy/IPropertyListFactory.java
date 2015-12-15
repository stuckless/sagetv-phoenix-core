package sagex.phoenix.metadata.proxy;

/**
 * Used to convert lists to strings and vice verse for specific types. Used in
 * the property serialization and deserialize for sage metadata. A ListFactory
 * implementation should accept the {@link SageProperty} in the constructor that
 * it can have access to the current property if needed.
 *
 * @author seans
 */
public interface IPropertyListFactory {
    public String encode(Object item);

    public Object decode(String item);

    public PropertyList toList(String data);

    public String fromList(PropertyList list);
}
