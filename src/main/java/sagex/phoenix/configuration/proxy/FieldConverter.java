package sagex.phoenix.configuration.proxy;

public interface FieldConverter<T> {
    public T toType(String in);

    public String toString(T in);
}
