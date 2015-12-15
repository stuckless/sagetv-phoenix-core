package sagex.phoenix.configuration.proxy;

public class TextConverter implements FieldConverter<String> {
    @Override
    public String toType(String in) {
        return in;
    }

    @Override
    public String toString(String in) {
        return in;
    }
}
