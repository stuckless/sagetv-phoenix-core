package sagex.phoenix.configuration.proxy;

public class ObjectConverter implements FieldConverter<Object> {
    @Override
    public Object toType(String in) {
        return in;
    }

    @Override
    public String toString(Object in) {
        return in == null ? null : String.valueOf(in);
    }
}
